package chess;


import hat.Accelerator;
import hat.backend.Backend;
import hat.buffer.Buffer;

import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static chess.ChessConstants.BLACK_BIT;
import static chess.ChessConstants.SIDE_MASK;
import static chess.ChessConstants.WHITE_BIT;


public class Main {
    static void time(String label, Runnable r) {
        long start = System.currentTimeMillis();
        r.run();
        long end = System.currentTimeMillis();
        System.out.println(label + " " + (end - start) + " ms");
    }

    static void trace( PrintStream printStream,Consumer<PrintStream> printStreamConsumer) {
        if (printStream != null){
            printStreamConsumer.accept(printStream);
        }
    }

    static <T> T time(String label, Supplier<T> r) {
        long start = System.currentTimeMillis();
        T returnValue = r.get();
        long end = System.currentTimeMillis();
        System.out.println(label + " " + (end - start) + " ms");
        return returnValue;
    }

    public static boolean sanity(ChessData chessData, ChessData.Board board, int id){
        if (board.id() != id) {
            System.out.println("bad board id at " + id);
        }else {
            if (board.fromSqId() == board.toSqId()) {
                System.out.println("bad board at " + id);
            } else {
                ChessData.Board parent = chessData.board(board.parent());
                if (parent.id() != board.parent()) {
                    System.out.println("bad parent board at " + id);
                } else {
                    if (board.id() < parent.firstChildIdx()) {
                        System.out.println("not a child (< parent.firstChildIdx) " + id);

                    } else {
                        if (board.id() >= (parent.firstChildIdx() + parent.moves())) {
                            System.out.println("not a child (>= (parent.firstChildIdx + parent.moves)  " + id);
                        } else {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {


        boolean headless = Boolean.getBoolean("headless") || (args.length > 0 && args[0].equals("--headless"));
        Accelerator accelerator = new Accelerator(MethodHandles.lookup(), /*new JavaMultiThreadedBackend());*/ Backend.FIRST);
        // Viewer viewer = new Viewer();

        WeightTable weightTable = WeightTable.create(accelerator);
        // From chess wikipedia we learned that each board needs 5.5 bits to
        // encode moves for the next ply so 32-64 approx 48

        PlyTable plyTable = PlyTable.create(accelerator,5);

        ChessData chessData = ChessData.create(accelerator, 96, plyTable.length());



        Compute.test(chessData,weightTable);
        System.out.println(Buffer.getMemorySegment(chessData).byteSize() + " bytes ");
        ChessData.Board initBoard = chessData.board(0);
        initBoard.firstPositions(); // This sets up the board and initializes 'as if' we had run plyMoves.

        PlyTable.Ply initPly = plyTable.ply(0);
        initPly.init(0, BLACK_BIT, 0, 1);
        boolean useIntStream = true;
        if (!useIntStream) {
            accelerator.compute(cc -> Compute.createBoardsCompute(cc, chessData, plyTable,initPly.id(), weightTable));
        }
        PrintStream off = null;
        PrintStream on = null;


        for (int i = 0; i < 5; i++) {
            time("Move ", () -> {
                for (int id = 0; id < plyTable.length()-1; id++) {
                   final PlyTable.Ply ply = plyTable.ply(id);
                    trace(off, o->
                        o.println("Ply " + ply.id() + " side="+ply.side()+" boards=" + ply.fromBoardId() + "-" + ply.toBoardId() + " count=" + ply.size())
                    );
                    /*
                     *  Each ply has a fromBoardIdx and toBoardIdx accessors
                     *
                     *  Each board in a ply has firstChildIdx (holding index to board for firstMove) and moves (holding
                     *  the count of the number of moves) accessors.
                     *
                     *  To determine the space needed for the next ply we prefix scan the moves field of
                     *  each board in this ply, feeding the scanned value as firstChildIdx's back into
                     *  the board.
                     */

                     /*
                     *  Assume we just completed ply 'n' with 5 boards where fromBoardIdx=1 and toBoardIdx=5
                     *  And the move counts (moves()) of each of these boards are 20,31,42,30 and 70 moves
                     *
                     *  At this point firstChildIdx (fci below ) for each board is unknown.
                     *
                     *             |       0       |       1       |       2       |       3       |       4       |
                     *             |  fci  | moves |  fci  | moves |  fci  | moves |  fci  | moves |  fci  | moves |
                     *             |   ?   |   20  |   ?   |   31  |   ?   |   42  |   ?   |   30  |   ?   |   70  |
                     *
                     *  We initialize the first board in the ply's firstChildIdx with this ply's toBoardIdx
                     *<pre>
                     *   ply(n)    |       0       |       1       |       2       |       3       |       4       |
                     * toBoardIdx  |  fci  | moves |  fci  | moves |  fci  | moves |  fci  | moves |  fci  | moves |
                     *      6      |   6   |   20  |    ?  |   31  |   ?   |   42  |   ?   |   30  |   ?   |   70  |
                     *        \       ^
                     *         \    /
                     *           +
                     *</pre>
                     */
                     /*  We can now prefix scan all moves fields into firstChildIdx fields, by adding the sum of the previous board's
                     *  firstChildId()+moves() fields to populate this board's firstChildId().
                     *
                     * <pre>
                     *   ply(n)    |       0       |       1       |       2       |       3       |       4       |
                     * toBoardIdx  |  fci  | moves |  fci  | moves |  fci  | moves |  fci  | moves |  fci  | moves |
                     *      5      |   5   |   20  |   25  |   31  |   ?   |   42  |   ?   |   30  |   ?   |   70  |
                     *        \       ^ \     /        ^
                     *         \    /    \   /        /
                     *           +         + --------/
                     *
                     *
                     *    ply(n)   |       0       |       1       |       2       |       3       |       4       |
                     * toBoardIdx  |  fci  | moves |  fci  | moves |  fci  | moves |  fci  | moves |  fci  | moves |
                     *      5      |   5   |   20  |   25  |   30  |   55  |   42  |   97  |   30  |  127  |   70  |
                     *        \       ^ \     /        ^\     /        ^\     /        ^\     /        ^\     /
                     *         \    /    \   /        /  \   /        /  \   /        /  \   /        /  \   /
                     *           +         + --------/     + --------/     + --------/     + --------/     + ----> 199
                     *
                     *</pre>
                     */
                     /*
                     *  So now each firstChildIdx in each board of this ply is ready for populating the next ply
                     *
                     *  ply(n+1) is now defined as ply(n).toBoardIdx to ply(n).toBoardIdx+199
                     *
                     */
                    int plyIdx = time("Prefix", () -> {
                        int nextPlyEndIdx = ply.toBoardId();
                        trace(off,o->o.print("prefix -> "));
                        for (int boardId = ply.fromBoardId(); boardId < ply.toBoardId(); boardId++) {
                            int finalId = boardId;
                            ChessData.Board board = chessData.board(finalId);
                            board.firstChildIdx(nextPlyEndIdx);
                            trace(off,o->o.print(finalId + "{fc=" + board.firstChildIdx() + ",m=" + board.moves() + "} "));
                            nextPlyEndIdx += board.moves();
                        }
                        return nextPlyEndIdx;
                    });
                    int nextPlySize = plyIdx - ply.toBoardId();

                    time("Compute ", () -> {
                        if (useIntStream) {
                            //Here we use an IntStream to bypass compute on entrypoint.
                            // This way we get to fully control execution from Java.
                            IntStream.range(0, ply.size())
                                    //.parallel() // consider commenting this out if debugging
                                    .forEach(kid ->
                                            Compute.createBoardsForParentBoardId(chessData, (byte)ply.side(), weightTable, kid+ply.fromBoardId())
                                    );
                        } else {
                            accelerator.compute(cc -> Compute.createBoardsCompute(cc, chessData, plyTable, ply.id(), weightTable));
                        }
                    });

                    trace(on,o->o.println("ply id="+ply.id()+" side="+ply.side()+" from="+ply.fromBoardId()+" to="+ply.toBoardId()));
                    PlyTable.Ply newPly = plyTable.ply(ply.id()+1);
                    newPly.init(ply.id() + 1, ply.side() ^ SIDE_MASK, ply.toBoardId(), nextPlySize);

                    trace(on, o->o.println("ply id="+newPly.id()+" side="+newPly.side()+" from="+newPly.fromBoardId()+" to="+newPly.toBoardId()+"  chessData="+chessData.length()));
                }
            });

            PlyTable.Ply ply = plyTable.ply(plyTable.length()-1);

            int bestScore = Compute.isWhite((byte)ply.side())?Integer.MAX_VALUE:Integer.MIN_VALUE;
            int bestBoardId = 0;

            for (int id = ply.fromBoardId(); id < ply.toBoardId(); id++) {
                ChessData.Board board = chessData.board(id);
                if (sanity(chessData, board, id)) {
                    int gameScore = board.score();
                    if (   (Compute.isWhite((byte)ply.side()) && (gameScore < bestScore))
                        || (Compute.isBlack((byte)ply.side())) && (gameScore > bestScore)) {
                        bestScore= gameScore;
                        bestBoardId = id;
                    }
                }else{
                    throw new IllegalStateException("failed sanity");
                }
            }
            int boardId = bestBoardId;
            List<ChessData.Board> path = new ArrayList<>();
            while (boardId != 0){
                path.add(chessData.board(boardId));
                boardId = path.getLast().parent();
            }
            System.out.println(BoardRenderer.unicodeMin(path));

            ply.init(0, Compute.otherSide((byte)ply.side()), 0, 1);
            initBoard.select(path.getLast());
            System.out.println("---");
        }
    }
}
