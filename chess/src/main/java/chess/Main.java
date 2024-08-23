package chess;


import hat.Accelerator;
import hat.backend.Backend;
import hat.buffer.Buffer;

import java.lang.invoke.MethodHandles;
import java.util.stream.IntStream;

import static chess.ChessConstants.WHITE_BIT;


public class Main {
    private static int prefixSum(ChessData chessData, Ply ply) {
        int nextPlyEndIdx = ply.toBoardId();
        System.out.print("prefix -> ");
        for (int id = ply.fromBoardId(); id < ply.toBoardId(); id++) {
            ChessData.Board board = chessData.board(id);
            board.firstChildIdx(nextPlyEndIdx);
            System.out.print(id + "{fc=" + board.firstChildIdx() + ",m=" + board.moves() + "} ");
            nextPlyEndIdx += board.moves(); // include current board
        }
        return nextPlyEndIdx - ply.toBoardId();
    }

    public static void main(String[] args) {
        boolean headless = Boolean.getBoolean("headless") || (args.length > 0 && args[0].equals("--headless"));
        Accelerator accelerator = new Accelerator(MethodHandles.lookup(), Backend.FIRST);
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
        while (ply.id() < 2) {
            System.out.print(ply.dump(chessData, "1"));
            /*
             * plyMoves() requires that board.moves for each boards move field
             * (boardId between ply.fromBoardId() and ply.toBoardId()) be set appropriately
             * board initialization does this for the start of game
             * after that we depend on the previous loop's execution of plyMoves()
             */

            if (useIntStream) {
                //Here we bypass compute on entrypoint.  This way we get to fully control execution from Java.
                IntStream.range(0, ply.size())
                        .forEach(id ->
                                Compute.doMovesKernelCore(chessData, ply, weightTable, id + ply.fromBoardId())
                        );
            } else {
                accelerator.compute(cc -> Compute.doMovesCompute(cc, chessData, ply, weightTable));
            }

            /*
             * Now we need to perform a prefix scan on board.moves field
             * between ply.startIdx() and ply.endIdx()
             * Ideally we could use the GPU for this as prefix scans from within a
             * kernel can use groupwide lane cooperation and local memory.
             */
          //  System.out.print(ply.dump(chessData, "2"));
            int nextPlySize = prefixSum(chessData, ply);


            //System.out.println();
            //System.out.print(ply.dump(chessData, "3"));
            ply.init(ply.id() + 1, ply.side() ^ WHITE_BIT, ply.toBoardId(), nextPlySize);

            System.out.println("next ply idx=" + ply.id() + " start=" + ply.fromBoardId() + "->" + ply.toBoardId());
            System.out.println("-----------------------------------------------------");
        }
    }
}
