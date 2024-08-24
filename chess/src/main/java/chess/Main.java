package chess;


import hat.Accelerator;
import hat.backend.Backend;
import hat.backend.JavaMultiThreadedBackend;
import hat.buffer.Buffer;

import java.lang.invoke.MethodHandles;
import java.util.stream.IntStream;

import static chess.ChessConstants.WHITE_BIT;


public class Main {
    public static void main(String[] args) {
        boolean headless = Boolean.getBoolean("headless") || (args.length > 0 && args[0].equals("--headless"));
        Accelerator accelerator = new Accelerator(MethodHandles.lookup(), /*new JavaMultiThreadedBackend());*/ Backend.FIRST);
        // Viewer viewer = new Viewer();

        WeightTable weightTable = WeightTable.create(accelerator);
        // From chess wikipedia we learned that on average each board needs 5.5 bits to encode # of moves so 32-64 approx 48
        ChessData chessData = ChessData.create(accelerator, 48, 5);
        Ply ply = Ply.create(accelerator);
        System.out.println(Buffer.getMemorySegment(chessData).byteSize() + " bytes ");
        ChessData.Board initBoard = chessData.board(0);
        initBoard.firstPositions(); // This sets up the board and initializes 'as if' we had run plyMoves.
        ply.init(0, WHITE_BIT, 0, 1);
        boolean useIntStream = true;
        if (!useIntStream) {
            accelerator.compute(cc -> Compute.doMovesCompute(cc, chessData, ply, weightTable));
        }
        long start = System.currentTimeMillis();

        while (ply.id() < 5) {
            System.out.println("Ply " + ply.id() + " boards " + ply.fromBoardId() + " - " + ply.toBoardId()+ " count = "+ply.size());
            long prefixStart = System.currentTimeMillis();
            int nextPlyEndIdx = ply.toBoardId();
            //  System.out.print("prefix -> ");
            for (int id = ply.fromBoardId(); id < ply.toBoardId(); id++) {
                ChessData.Board board = chessData.board(id);
                board.firstChildIdx(nextPlyEndIdx);
                //    System.out.print(id + "{fc=" + board.firstChildIdx() + ",m=" + board.moves() + "} ");
                nextPlyEndIdx += board.moves(); // include current board
            }
            System.out.println("Prefix " + (System.currentTimeMillis() - prefixStart) + " ms");
            int nextPlySize = nextPlyEndIdx - ply.toBoardId();

            long plyStart = System.currentTimeMillis();
            //    System.out.print(ply.dump(chessData, "1"));
            /*
             * plyMoves() requires that board.moves for each boards move field
             * (boardId between ply.fromBoardId() and ply.toBoardId()) be set appropriately
             * board initialization does this for the start of game
             * after that we depend on the previous loop's execution of plyMoves()
             */

            if (useIntStream) {
                //Here we bypass compute on entrypoint.  This way we get to fully control execution from Java.
                IntStream.range(0, ply.size()).parallel()
                        .forEach(id ->
                                Compute.doMovesKernelCore(chessData, ply, weightTable, id + ply.fromBoardId())
                        );
            } else {
                accelerator.compute(cc -> Compute.doMovesCompute(cc, chessData, ply, weightTable));
            }
            System.out.println("Ply compute " + (System.currentTimeMillis() - plyStart) + "ms");
            /*
             * Now we need to perform a prefix scan on board.moves field
             * between ply.startIdx() and ply.endIdx()
             * Ideally we could use the GPU for this as prefix scans from within a
             * kernel can use groupwide lane cooperation and local memory.
             */
            //    System.out.print(ply.dump(chessData, "2"));
            //   int nextPlySize = prefixSum(chessData, ply);


            //System.out.println();
            //System.out.print(ply.dump(chessData, "3"));
            ply.init(ply.id() + 1, ply.side() ^ WHITE_BIT, ply.toBoardId(), nextPlySize);
            System.out.println("-----------------------------------------------------");
        }
        System.out.println("ms" + (System.currentTimeMillis() - start));
        int minScore = Integer.MAX_VALUE;
        int maxScore = Integer.MIN_VALUE;
        int minBoardId = 0;
        int maxBoardId = 0;

       for (int id = ply.fromBoardId(); id < ply.toBoardId(); id++) {
            ChessData.Board board = chessData.board(id);
            if (board.score() < minScore) {
                minScore = board.score();
                minBoardId=id;
            }
           if (board.score() >= maxScore) {
               maxScore = board.score();
               maxBoardId=id;
           }

        }
     //  System.out.print("minScore = " + minScore + "minBoard = "+ minBoardId + "maxScore = " + maxScore + "maxBoard = "+ maxBoardId);
     //  System.out.println(new Terminal().board(chessData.board(minBoardId), minBoardId));
    //  System.out.println(new Terminal().board(chessData.board(maxBoardId), maxBoardId));
/*
       System.out.println("MaxLine");
       var board = chessData.board(maxBoardId);
        System.out.println(new Terminal().board(board));
       while (board.parent()!=0){
           board = chessData.board(board.parent());
           System.out.println(new Terminal().board(board));
       }
*/
        //System.out.println("MinLine");
        var board = chessData.board(minBoardId);
        //System.out.println(new Terminal().board(board));
        while (board.parent()!=0){
            board = chessData.board(board.parent());

        }
        System.out.println(new Terminal().board(board));
    }
}
