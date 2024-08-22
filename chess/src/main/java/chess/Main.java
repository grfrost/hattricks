package chess;


import hat.Accelerator;
import hat.backend.Backend;
import hat.buffer.Buffer;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.stream.IntStream;

import static chess.ChessConstants.BISHOP;
import static chess.ChessConstants.EMPTY_SQUARE;
import static chess.ChessConstants.KING;
import static chess.ChessConstants.KNIGHT;
import static chess.ChessConstants.PAWN;
import static chess.ChessConstants.QUEEN;
import static chess.ChessConstants.ROOK;
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
       // Viewer viewer = new Viewer();

        WeightTable weightTable = WeightTable.create(accelerator);
        // From chess wikipedia we learned that on average each board needs 5.5 bits to encode # of moves so 32-40
        ChessData chessData = ChessData.create(accelerator,
                        1                           // ply 0
                        + 40                        //     1
                        + (40 * 40)                 //     2
                        + (40 * 40 * 40)            //     3
                        + (40 * 40 * 40 * 40)       //     4
                        + (40 * 40 * 40 * 40 * 40)  //     5
        );
        PlyTable plyTable = PlyTable.create(accelerator, 5);
        System.out.println(Buffer.getMemorySegment(chessData).byteSize() + " bytes ");
        ChessData.Board initBoard = chessData.board(0);
        // This sets up the board 'as if' we had run plyMoves.
        {
            int x = 0;
            for (byte bits : new byte[]{ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK}) {
                initBoard.squareBits(x, (byte) (bits));
                initBoard.squareBits(x + 8, (byte) (PAWN));
                for (int i = 16; i < 48; i += 8) {
                    initBoard.squareBits(x + i, (byte) (EMPTY_SQUARE));
                }
                initBoard.squareBits(x + 48, (byte) (WHITE_BIT | PAWN));
                initBoard.squareBits(x + 56, (byte) (WHITE_BIT | bits));
                x++;
            }
        }
        initBoard.score((short)0);  // The score after init is zero,
        initBoard.moves((byte)20);  // The number of moves available to white is 20 =  8 pawn, 4 knight
        initBoard.firstChildIdx(1); // the first child will be 1
        initBoard.fromSquareIdx((byte) 0);   // no move got us here,
        initBoard.toSquareIdx((byte) 0);     // no move got us here
        initBoard.move((byte)0);    // no move got us here

        plyTable.idx(0);
        PlyTable.Ply ply = plyTable.ply(0);
        ply.side(WHITE_BIT);
        ply.startIdx(0);
        ply.size(1);
        for (int boardIdx = ply.startIdx(); boardIdx < (ply.startIdx()+ply.size()); boardIdx++) {
            ChessData.Board board = chessData.board(boardIdx);
            System.out.println("+"+new Terminal().line(board, boardIdx));
        }
        while (plyTable.idx()<2) {
            /*
             * plyMoves() requires that board.moves for each boards move field
             * (boardId between ply.startIdx() and ply.endIdx()) be set appropriately
             * board initialization does this for the start of game
             * after that we depend on the previous loop's execution of doMovesCompute()
             * to provide this information
             */

            Compute.plyMoves(accelerator, true, chessData, plyTable,weightTable);
            /*
             * Now we need to perform a prefix scan on board.moves field
             * between ply.startIdx() and ply.endIdx()
             * Ideally we could use the GPU for this as prefix scans from within a
             * kernel can use groupwide lane cooperation and local memory.
             *
             */

            int nextPlySize = 0;
            for (int boardId = ply.startIdx(); boardId < (ply.startIdx()+ply.size()); boardId++) {
                ChessData.Board board = chessData.board(boardId);
              //  System.out.println("looking at " + boardIdx+ " ?= "+board.id());
                board.firstChildIdx(nextPlySize+ply.startIdx()+ply.size()); // set the prefix value
                nextPlySize += board.moves(); // include current board
            }
            if (nextPlySize == 0) {
                throw new IllegalStateException("no moves?");
            }


            /*
             * Imagine that after the last round we had only four boards in a ply
             *
             * board.firstChild is initialized to 0 and moves has been calculated by the last round for each board
             *           bd0                  bd1                bd2                 bd3
             *  firstChild moves    firstChild moves    firstChild moves    firstChild moves
             *         0    20              0    25            0    22             0     15
             *
             *
             * After a prefix scan
             *
             *           bd0                 bd1                 bd2                 bd3
             *  firstChild moves    firstChild moves    firstChild moves    firstChild moves      nextPlySize =
             *          0    20            20    25            45    22           67     15         82
             *
             * The resulting nextPlySize has the 'size' of the next ply
             *
             * So we set up the ply.startIdx() and ply.endIdx() for the next round
             * and flip the sides.
             */

            for (int boardIdx = ply.startIdx(); boardIdx < (ply.startIdx()+ply.size()); boardIdx++) {
                ChessData.Board board = chessData.board(boardIdx);
                System.out.println(new Terminal().line(board, boardIdx));
            }
            System.out.println("-----------------------------------------------------");
            int plyIdx = plyTable.idx();
            plyTable.idx(plyIdx+1);
            int nextPlyIdx = plyTable.idx();
            PlyTable.Ply nextPly = plyTable.ply(nextPlyIdx);
            nextPly.startIdx(ply.startIdx()+ply.size());
            nextPly.size(nextPlySize);
            nextPly.side( (byte)(ply.side() ^WHITE_BIT));
            ply =nextPly;
        }
    }
}
