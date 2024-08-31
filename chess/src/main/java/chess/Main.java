package chess;


import hat.Accelerator;
import hat.backend.Backend;
import hat.buffer.Buffer;

import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static chess.ChessConstants.WHITE_BIT;


public class Main {
    static void time(String label, Runnable r) {
        long start = System.currentTimeMillis();
        r.run();
        long end = System.currentTimeMillis();
        System.out.println(label + " " + (end - start) + " ms");
    }

    static void trace(boolean tracing, PrintStream printStream,Consumer<PrintStream> printStreamConsumer) {
        if (tracing){
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

    public static void main(String[] args) {
        boolean headless = Boolean.getBoolean("headless") || (args.length > 0 && args[0].equals("--headless"));
        Accelerator accelerator = new Accelerator(MethodHandles.lookup(), /*new JavaMultiThreadedBackend());*/ Backend.FIRST);
        // Viewer viewer = new Viewer();

        WeightTable weightTable = WeightTable.create(accelerator);
        // From chess wikipedia we learned that on average each board needs 5.5 bits to encode # of moves so 32-64 approx 48
        ChessData chessData = ChessData.create(accelerator, 96, 5);
        Ply ply = Ply.create(accelerator);
        System.out.println(Buffer.getMemorySegment(chessData).byteSize() + " bytes ");
        ChessData.Board initBoard = chessData.board(0);
        initBoard.firstPositions(); // This sets up the board and initializes 'as if' we had run plyMoves.
        ply.init(0, WHITE_BIT, 0, 1);
        boolean useIntStream = true;
        if (!useIntStream) {
            accelerator.compute(cc -> Compute.createBoardsCompute(cc, chessData, ply, weightTable));
        }
        boolean timing = false;
        boolean tracing = false;
        long totalMs = 0;
        for (int i = 0; i < 32; i++) {
            time("Move ", () -> {
                while (ply.id() < 5) {
                    trace(tracing, System.out, o->{
                        o.println("Ply " + ply.id() + " boards " + ply.fromBoardId() + " - " + ply.toBoardId() + " count = " + ply.size());
                    });
                    /*
                     *  To determine the space needed for the next ply we prefix scan the moves field of each board in
                     *  this ply, feeding the scanned value as firstChildIdx's back into the board.
                     *
                     *  Each ply has a fromBoardIdx and toBoardIdx accessors
                     *
                     *  Each board in a ply has firstChildIdx (holding index to board for firstMove) and moves (holding
                     *  the count of the number of moves) accessors.
                     *
                     *  Assume we just completed ply 'n' with 5 boards (fromBoardIdx=1, toBoardIdx=5)
                     *  with move counts (moves) of 20,31,42,30 and 70 moves
                     *
                     *  At this point firstChildIdx =0 and is unknown. We use fci for 'firstChildIdx' below.
                     *
                     *             |       0       |       1       |       2       |       3       |       4       |
                     *             |  fci  | moves |  fci  | moves |  fci  | moves |  fci  | moves |  fci  | moves |
                     *             |   ?   |   20  |   ?   |   31  |   ?   |   42  |   ?   |   30  |   ?   |   70  |
                     *
                     *  We first initialize the first board in the ply's firstChildIdx with this ply's toBoardIdx
                     *<pre>
                     *   ply(n)    |       0       |       1       |       2       |       3       |       4       |
                     * toBoardIdx  |  fci  | moves |  fci  | moves |  fci  | moves |  fci  | moves |  fci  | moves |
                     *      6      |   6   |   20  |    ?  |   31  |   ?   |   42  |   ?   |   30  |   ?   |   70  |
                     *        \       ^
                     *         \    /
                     *           +
                     *</pre>
                     *  We can now prefix scan all moves fields into firstChildIdx fields, by adding the sum of the previous board's
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
                     *
                     *  So now each firstChildId() in each board of this ply is now set up for populating the next ply
                     *
                     *  The ply(n+1) range is now defined as ply(n).toBoardIdx to ply(n).toBoardIdx+199
                     *
                     */
                    int plyIdx = time("Prefix", () -> {
                        int nextPlyEndIdx = ply.toBoardId();
                        trace(tracing,System.out,o->o.print("prefix -> "));
                        for (int id = ply.fromBoardId(); id < ply.toBoardId(); id++) {
                            int finalId = id;
                            ChessData.Board board = chessData.board(id);
                            board.firstChildIdx(nextPlyEndIdx);
                            trace(tracing,System.out,o->o.print(finalId + "{fc=" + board.firstChildIdx() + ",m=" + board.moves() + "} "));
                            nextPlyEndIdx += board.moves(); // include current board
                        }
                        return nextPlyEndIdx;
                    });
                    int nextPlySize = plyIdx - ply.toBoardId();

                    /*
                     * createBoardsXXXX() requires that board.moves for each boards move field
                     * (boardId between ply.fromBoardId() and ply.toBoardId()) be set appropriately
                     * board initialization does this for the start of game
                     * after that we depend on the previous loop's execution of plyMoves()
                     */
                    time("Compute ", () -> {
                        if (useIntStream) {
                            //Here we bypass compute on entrypoint.  This way we get to fully control execution from Java.
                            IntStream.range(0, ply.size())
                                    .parallel()
                                    .forEach(id -> {
                                                int parentBoardId = id+ply.fromBoardId();
                                                Compute.createBoardsForParentBoardId(chessData, ply, weightTable, parentBoardId);
                                            }
                                    );
                        } else {
                            accelerator.compute(cc -> Compute.createBoardsCompute(cc, chessData, ply, weightTable));
                        }
                    });
                    System.out.println("ply id="+ply.id()+" from="+ply.fromBoardId()+" to="+ply.toBoardId());
                  ply.init(ply.id() + 1, ply.side() ^ WHITE_BIT, ply.toBoardId(), nextPlySize);
                  System.out.println("ply id="+ply.id()+" from="+ply.fromBoardId()+" to="+ply.toBoardId()+"  chessData="+chessData.length());

                }
            });


            int minScore = Integer.MAX_VALUE;
            int maxScore = Integer.MIN_VALUE;
            int minBoardId = 0;
            int maxBoardId = 0;

            for (int id = ply.fromBoardId(); id < ply.toBoardId(); id++) {
                ChessData.Board board = chessData.board(id);
                if (board.id() != id) {
                    System.out.println("bad board id at " + id);
                }else {
                    if (board.fromSqId() == board.toSqId()) {
                        System.out.println("bad board at " + id);
                    } else {
                        var gameScore = board.score();
                        if (gameScore < minScore) {
                            minScore = gameScore;
                            minBoardId = id;
                        }
                        if (gameScore > maxScore) {
                            maxScore = gameScore;
                            maxBoardId = id;
                        }
                    }
                }
            }
            //  System.out.print("minScore = " + minScore + "minBoard = "+ minBoardId + "maxScore = " + maxScore + "maxBoard = "+ maxBoardId);
            //  System.out.println(new Terminal().board(chessData.board(minBoardId), minBoardId));
            // System.out.println(new Terminal().board(chessData.board(maxBoardId), maxBoardId));

            var pathStack = chessData.getPath((ply.side()!=WHITE_BIT)?maxBoardId:minBoardId);
            String indent = "            ";
            System.out.println(BoardRenderer.unicode(pathStack));
            var root = pathStack.pop();
            System.out.println("    Root ->"+ BoardRenderer.line(root));
            var selected = pathStack.pop();
            System.out.println("Selected ->"+BoardRenderer.line(selected));
          //  while (pathStack.size()>0){
            //    var from = pathStack.pop();
             //   System.out.println("    Path ->"+BoardRenderer.line(from));
           // }

            ply.init(0, ply.side(), 0, 1); // this assumes we did odd plys!  fix this
            initBoard.select(selected);
            System.out.println("---");
        }
    }
}
