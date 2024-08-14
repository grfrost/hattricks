package chess;


import hat.Accelerator;
import hat.backend.Backend;
import hat.buffer.Buffer;

import java.lang.invoke.MethodHandles;
import java.util.stream.IntStream;

import static chess.ChessConstants.EMPTY_SQUARE;
import static chess.ChessConstants.WHITE_BIT;


public class Main {

    /*
     *    Computing the N ply table for chess is tricky on the GPU, as we have to tradeoff
     *    the amount of space we want to allocate and the number of kernel dispatches we will allow.
     *
     *    To hold the plyspace we allocate a ChessData list/array/pool of ply(n) ChessData.Boards
     *
     *    Board(0) will represent the current position. So at the start of the game this will be an inited board.
     *
     *    RNBQKBNR
     *    PPPPPPPP
     *    ........
     *    ........
     *    ........
     *    ........
     *    pppppppp
     *    rnbqkbnr
     *
     *    To allocate enough space for 'multiple plys' we could either reserve enough space initially so that each ChessData.Board
     *    in the ply could contain up to 218 child moves
     *
     *    From  https://www.chessprogramming.org/Encoding_Moves#MoveIndex
     *
     *         So for the standard chess piece set we have an upper bound of 12*8 + 8*2 + 13*2 + 14*2 + 27 + 8 == 201
     *         possible moves before considering promotions. Each knight promotion reduces it by 12-8=4.
     *         Each bishop promotion increases it by 1; rook promotion by 2; queen promotion by 15.
     *         In the parametrized worst case (all pawns promoted to either rooks or queens) we have n queen and 8-n rook promotions,
     *         so the upper bound is f(n) = 8*2 + 13*2 + 14*(2+8-n) + 27*(1+n) + 8 == 217 + 13n. f(n=3)=256 <= 2^8.
     *
     *         So for legal Chess960 positions with at most 3 queens on the board one can encode any legal move in just one byte.
     *         Note that whether the result can always fit in one byte is determined based solely one the position, so no additional
     *         information needs to be stored for disambiguation. This way it's easy to implement a fallback for the positions not
     *         supported by this encoding (this could be either to use 2 bytes for the move index or use legal move generation to ensure
     *         all values take one byte). One way to encode a position using this information is in the following way:"
     *
     *    So essentially each node in the plytree (plywood? :) ) has fixed slots for 218 children.
     *
     *    This tree spreads out really fast. Here is an approximation of the note count.
     *
     *    1  1                                          1
     *    2  1 + 218^1                                219
     *    3  1 + 218^1 + 218^2                        219 + 47524
     *    4  1 + 218^1 + 218^2 + 218^3                219 + 47524 + 10,360,232
     *    5  1 + 218^1 + 218^2 + 218^3 + 218^4        219 + 47524 + 10,360,232 + 2,258,530,576
     *
     *    Each node contains 64 bytes for the pieces... 4 bytes for parent ptr 2 bytes for score  2 bytes for board state ~72 bytes
     *
     *   So over 140 GB for 5 ply (that's only 3 user moves ahead)
     *
     *   This data structure has the huge advantage for parallel compute as we can determine the position of every node using
     *   just the ply number and the 'from' square
     *
     *   Another option is to pick a 'large upper bound of boards'
     *   And for each initial position
     *      Count the number of actual moves in each ply.
     *      Then assert we have enough ply space
     *      Then perform the moves for all boards in the ply and 'fill' in the moves in a second kernel.
     *      Count the number of moves ...
     *      assert space
     *      perform the moves.
     *
     *
     *    Computationally counting the # of moves is essentially the same as actually doing tne move.  Which is a pain.
     *
     *    But the space saved by this approach is huge.
     *
     *    Whilst the theoretical upper bound for moves for each board is 218 in practice the number is much smaller (by observation 20-40)
     *       Later self... this states that the average number of bits is 5.5 bits so 40 ?  32+ 8 -> 40 seems reasonable
     *       https://groups.google.com/g/rec.games.chess/c/RspnvkCEY7s/m/W4kUZ0uH7jMJ
     *
     *    So 5 ply
     *        1 + 40^1 + 40^2 + 40^3 + 40^4 + 40^5 +40^6 = 41 + 1,600 + 64,000 + 2,560,000+ 102,400,000  ~ 105 million
     *
     *    The board does now need two extra fields (8 bytes) so 80bytes * 105 million so 8Gb?
     *
     *    To calc the moves for each ply we essentially have one kernel walk the squares and compute the count of moves
     *    for a given side from the current position and store it in the board. This is embarrassingly parallel
     *
     *    We then need to determine space for the next ply. So we prefix scan each of these counts (feeding the scanned value as bases back
     *    into the board.
     *
     *    A board has two fields one for the local move count, and one for the resulting prefix scan.
     *
     *    So if we ended up with 5 boards with 20,31,42,30 and 70 moves, initially the prefix is ?
     *
     *           0      1      2      3      4
     *          ?,20   ?,31   ?,42   ?,30   ?,70
     *
     *    We then prefix scan all the boards in the prev ply
     *
     *          0,20  20,31  51,42  93,30 123,70
     *
     *    From this we can know that the next ply is the prefix from the last board in the prev ply + moves (so 70+123 = 193)
     *
     *    A doMove kernel populates the boards for the next ply for each existing board in the prev ply.
     *    Each kernel adds its prefix + move value (1..moves) to the ply index start then populates the board at that index.
     *
     *    It then scores each new board and counts the # of moves.
     *
     *
     *
     *
     */


    public static void main(String[] args) {
        boolean headless = Boolean.getBoolean("headless") || (args.length > 0 && args[0].equals("--headless"));
        Accelerator accelerator = new Accelerator(MethodHandles.lookup(), Backend.FIRST);

        Control control = Control.create(accelerator);
        int ply5 = 1 + 40 + (40 * 40) + (40 * 40 * 40) + (40 * 40 * 40 * 40) + (40 * 40 * 40 * 40 * 40);
        ChessData chessData = ChessData.create(accelerator, ply5);
        System.out.println(Buffer.getMemorySegment(chessData).byteSize() + " bytes ");
        ChessData.Board initBoard = chessData.board(0);
        initBoard.init();
        System.out.println(new Terminal().board(initBoard, 0));
        control.ply(0);
        control.side(WHITE_BIT);
        control.start(0);
        control.count(1);
        boolean intStream = true;
        // doMovesCompute assumes that all control.count() moves starting at index control.start() in the last control.ply()
        // has it's moveCount and prefix set appropriately
        //  accelerator.compute(cc -> Compute.doMovesCompute(cc, chessData, control));
        if (intStream) {
            IntStream.range(0, 1).forEach(id -> Compute.doMovesKernelCore(id, chessData, control));
        } else {
            accelerator.compute(cc -> Compute.doMovesCompute(cc, chessData, control));
        }
        for (int ply = 1; ply < 3; ply++) {
            // This is a prefix scan on boards control.start().. control.count() + control.start()
            // ideally we could use the GPU for this....
            int accum = 0;
            for (int i = control.start(); i < control.count() + control.start(); i++) {
                var board = chessData.board(i);
                board.prefix(i + accum);
                accum += board.moves();
            }
            // accum now has the 'size' of the ply for the next layer
            control.start(control.count() + control.start());
            control.count(accum);
            control.ply(ply);
            if (intStream) {
                IntStream.range(0, accum).forEach(id -> Compute.doMovesKernelCore(id, chessData, control));
            } else {
                accelerator.compute(cc -> Compute.doMovesCompute(cc, chessData, control));
            }
            IntStream.range(0, accum).forEach(id -> {
                        var boardid = control.start() + id;
                        System.out.println(new Terminal().board(chessData.board(boardid), boardid));
                    }
            );
            control.side((control.side() & WHITE_BIT) == WHITE_BIT ? EMPTY_SQUARE : WHITE_BIT);
        }
    }
}
