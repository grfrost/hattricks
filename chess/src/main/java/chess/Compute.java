package chess;

import hat.Accelerator;
import hat.ComputeContext;
import hat.KernelContext;

import java.lang.runtime.CodeReflection;
import java.util.stream.IntStream;

import static chess.ChessConstants.ALL_POINTS;
import static chess.ChessConstants.BISHOP;
import static chess.ChessConstants.COLROWS;
import static chess.ChessConstants.CompassDxDyMap;
import static chess.ChessConstants.DIAGS;
import static chess.ChessConstants.EMPTY_SQUARE;
import static chess.ChessConstants.ROOK;
import static chess.ChessConstants.WHITE_BIT;

public class Compute {
    @CodeReflection
    static byte pieceValue(byte squareBits) {
        return (byte)(squareBits & ChessConstants.PIECE_MASK);
    }

    @CodeReflection
    static byte side(byte squareBits) {
        return (byte)(squareBits & WHITE_BIT);
    }
    @CodeReflection
    public static boolean isEmpty(byte squareBits) {
        return pieceValue(squareBits) == EMPTY_SQUARE;
    }

    @CodeReflection
    static boolean isPiece(byte squareBits) {
        return pieceValue(squareBits) != 0;
    }

    @CodeReflection
    public static boolean isWhite(byte squareBits) {
        return isPiece(squareBits) && (side(squareBits) == ChessConstants.WHITE_BIT);
    }

    @CodeReflection
    public static boolean isOpponent(byte fromBits, byte toBits) {
        return isPiece(toBits) &&  side(fromBits)!=side(toBits);
    }
    @CodeReflection
    public static boolean isComrade(byte fromBits, byte toBits) {
        return isPiece(toBits) &&  side(fromBits)==side(toBits);
    }
    @CodeReflection
    public static boolean isEmptyOrOpponent(byte fromBits, byte toBits) {
        return isEmpty(toBits) || isOpponent(fromBits, toBits);
    }




    @CodeReflection
    static boolean isOnBoard(int x, int y) {
        return (x < 8 && y < 8 && x >= 0 && y >= 0);
    }

    @CodeReflection
    static boolean isOffBoard(int x, int y) {
        return (x < 0 || y < 0 || x > 7 || y > 7);
    }

    @CodeReflection
    static boolean isPawn(byte squareBits) {
        return pieceValue(squareBits) == ChessConstants.PAWN;
    }

    @CodeReflection
    static boolean isKnight(byte squareBits) {
        return pieceValue(squareBits) == ChessConstants.KNIGHT;
    }

    @CodeReflection
    static boolean isBishop(byte squareBits) {
        return pieceValue(squareBits) == BISHOP;
    }

    @CodeReflection
    static boolean isRook(byte squareBits) {
        return pieceValue(squareBits) == ROOK;
    }

    @CodeReflection
    static boolean isKing(byte squareBits) {
        return pieceValue(squareBits) == ChessConstants.KING;
    }


    @CodeReflection
    static boolean isQueen(byte squareBits) {
        return pieceValue(squareBits) == ChessConstants.QUEEN;
    }


    public static void traceCountMovesForSquare( PlyTable.Ply ply,  ChessData.Board board, byte fromBits, int fromSquareIdx) {
        System.out.print("     void countMovesForSquare(ply");
        System.out.print("{startIdx()=" + ply.startIdx() + ", size()=" + ply.size()+"}");
        System.out.print(", board");
        System.out.print("{id()=" + board.id() + ", firstChildIdx()=" + board.firstChildIdx() + ", moves()=" + board.moves()+"}");
        System.out.println(", fromBits=" + fromBits + ", fromSquareIdx=" + fromSquareIdx+")");
    }
    @CodeReflection
    public static int countMovesForSquare(PlyTable.Ply ply, ChessData.Board board, byte fromBits, int fromSquareIdx) {
        traceCountMovesForSquare(ply, board, fromBits, fromSquareIdx);
        int fromx = fromSquareIdx%8;
        int fromy = fromSquareIdx/8;
        int moves = 0;
        byte plySide = (byte)(ply.side()&WHITE_BIT);
        byte fromSide = side(fromBits);

        byte fromPieceValue = pieceValue(fromBits);
        if (isKing(fromPieceValue)) {
            for (int moveIdx = 7; moveIdx > 0; moveIdx--) {
                int dxdy = 0b1111 & (CompassDxDyMap >>> (moveIdx * 4));
                int dy = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                dxdy >>>= 2;
                int dx = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                int tox = fromx + dx;
                int toy = fromy + dy;
                if (isOnBoard(tox, toy)) {
                    byte toBits = board.squareBits(toy * 8 + tox);
                    byte toSide = side(toBits);

                    if (isEmptyOrOpponent(fromBits, toBits)) {
                        moves++;
                    }
                }
            }
        } else if (isPawn(fromPieceValue)) {
            int forward = isWhite(fromBits) ? -1 : 1;
            int count = (fromy == (isWhite(fromBits) ? 6 : 1)) ? 4 : 3;  // four moves if home else three
            for (int moveIdx = 0; moveIdx < count; moveIdx++) {
                int dxdy = 0b1111 & (ChessConstants.PawnDxDyMap >>> (moveIdx * 4));
                int toy = fromy + (forward * ((dxdy & 0b11) - 1));
                dxdy >>>= 2;
                int tox = fromx + (dxdy & 0b11) - 1;
                if (isOnBoard(tox, toy)) {
                    byte toBits = board.squareBits(toy * 8 + tox);
                    if (((moveIdx > 1) && isEmpty(toBits)) || (moveIdx < 2) && isOpponent(fromBits, toBits)) {
                        moves++;
                    }
                }
            }
        } else if (isKnight(fromPieceValue)) {
            for (int moveIdx = 0; moveIdx < 8; moveIdx++) {
                int dxdy = 0b1111 & (ChessConstants.KnightDxDyMap >>> (moveIdx * 4));
                int toy = fromy + ((dxdy & 0b1) + 1) * ((dxdy & 0b10) - 1);
                dxdy >>>= 2;
                int tox = fromx + ((dxdy & 0b1) + 1) * ((dxdy & 0b10) - 1);
                if (isOnBoard(tox, toy)) {
                    byte toBits = board.squareBits(toy * 8 + tox);
                    if (isEmptyOrOpponent(fromBits, toBits)) {
                        moves++;
                    }
                }
            }
        } else {
            // sliders Rook, Queen, Bishop
            final int compassPoints = isBishop(fromPieceValue) ? DIAGS : isRook(fromPieceValue) ? COLROWS : /* MUST BE QUEEN */ALL_POINTS;

            // compassPoints have use 1 or 0 for bit for eligible move relative to fromx,fromy
            // We only encode the neighbours so the 3x3 grid has no center representation
            //                piece being moved
            //                     is here
            //                        v
            //              nw n ne e | w sw s se
            //                \ | | | | | | | /
            //                 \\ | | | | | //
            //                  ||| | v | |||
            //     diags   == 0b101_0___0_101
            //     colrows == 0b010_1___1_010
            //   allpoints == 0b111_1___1_111


            for (int moveIdx = 7; moveIdx > 0; moveIdx--) {
                int moveBit = (1 << moveIdx); // so 0b10000000 -> 0b01000000 -> ... 0b00000001
                // check if we can move this way
                // i.e is bit is set for this compassPoint
                if ((compassPoints & moveBit) == moveBit) {
                    // Now let's determine what a move in this dir looks like
                    int dxdy = 0b1111 & (CompassDxDyMap >>> (moveIdx * 4));
                    int dy = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                    dxdy >>>= 2;
                    int dx = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                    boolean blocked = false;
                    for (int slide = 1; !blocked && slide < 8; slide++) { //1,2,3,4,5,6,7
                        int tox = fromx + slide * dx;
                        int toy = fromy + slide * dy;
                        blocked = isOffBoard(tox, toy);
                        if (!blocked) {
                            byte toBits = board.squareBits(toy * 8 + tox);
                            if (isEmptyOrOpponent(fromBits, toBits)) {
                                moves++;
                                if (isOpponent(toBits, fromBits)) {
                                    blocked = true;
                                }
                            } else {
                                blocked = true;
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    public static void traceCountMovesForBoard(ChessData chessData, PlyTable.Ply ply,  WeightTable weightTable,ChessData.Board newBoard) {
        System.out.print("    void countMovesForBoard(chessData, ply");
        System.out.print("{startIdx()=" + ply.startIdx() + ", size()=" + ply.size()+"}");
        System.out.print(", weightTable, newBoard");
        System.out.println("{id()=" + newBoard.id() + ", firstChildIdx()=" + newBoard.firstChildIdx() + ", moves()=" + newBoard.moves()+"})");
    }
    @CodeReflection
    public static void countMovesForBoard(ChessData chessData, PlyTable.Ply ply, WeightTable weightTable, ChessData.Board newBoard) {
        traceCountMovesForBoard(chessData, ply, weightTable, newBoard);
        byte side =(byte)ply.side();

        int moves=0;
        int score=0;
        for (int squareIdx = 0; squareIdx < 64; squareIdx++) {
            byte squareBits = newBoard.squareBits(squareIdx);
            byte piece = pieceValue(squareBits);
            if (!isEmpty(piece)) {
                // The weight array masks are valid for WHITE.  We need to invert the index for black
                int weightIndex = isWhite(squareBits)?squareIdx:63-squareIdx;
                int scoreMul = -1;
                if (isComrade(side, squareBits)) {
                    moves += countMovesForSquare(ply,newBoard, squareBits, squareIdx);
                    scoreMul = 1;
                }
                // now the piece value can be used an index into the weights
                int weights = weightTable.weight(weightIndex);
                int shifted = (weights>>>piece*4)&0xf;
                shifted *= scoreMul;
                score+=shifted;
            }
        }
        if (moves == 0){
            throw new IllegalStateException("no moves");
        }
        newBoard.moves((byte) moves);
        newBoard.score((short) score);

    }

    public static void traceCreateBoard(ChessData chessData, PlyTable.Ply ply,  WeightTable weightTable,ChessData.Board parentBoard,int parentBoardId, int move, int newBoardId, int fromSquareIdx, int toSquareIdx) {
        System.out.print("   void createBoard(chessData, ply");
        System.out.print("{startIdx()=" + ply.startIdx() + ", size()=" + ply.size()+"}");
        System.out.print(", weightTable, parentBoard");
        System.out.print("{id()=" + parentBoard.id() + ", firstChildIdx()=" + parentBoard.firstChildIdx() + ", moves()=" + parentBoard.moves()+"}");
        System.out.println(", parentBoardId="+parentBoardId+" move="+move+", newBoardId="+newBoardId+", fromSquareIdx="+fromSquareIdx+", toSquareIdx="+toSquareIdx+")");
    }
    @CodeReflection
    public static void createBoard(ChessData chessData, PlyTable.Ply ply, WeightTable weightTable, ChessData.Board parentBoard, int parentBoardId,  int move,  int newBoardId,
                                   int fromSquareIdx, int toSquareIdx){
        traceCreateBoard(chessData,ply,weightTable,parentBoard,parentBoardId, move,newBoardId,fromSquareIdx,toSquareIdx);

        var newBoard = chessData.board(newBoardId);
        if (newBoard.id() != 0 ){
            if (newBoard.id()!= newBoardId) {
                throw new IllegalStateException("WTF");
            }
        }
        newBoard.id(newBoardId);
        newBoard.parent(parentBoard.id());
        newBoard.fromSquareIdx((byte)fromSquareIdx);
        newBoard.toSquareIdx((byte)toSquareIdx);
        newBoard.move((byte)move);

        for (int squareIdx=0; squareIdx<64; squareIdx++) {
            newBoard.squareBits(squareIdx, parentBoard.squareBits(squareIdx));
        }
        newBoard.squareBits(fromSquareIdx, EMPTY_SQUARE);
        newBoard.squareBits(toSquareIdx, parentBoard.squareBits(fromSquareIdx));
        countMovesForBoard(chessData, ply,weightTable, newBoard);
    }
    public static void traceCreateBoards(ChessData chessData,  PlyTable.Ply ply,  WeightTable weightTable, int moves,  ChessData.Board parentBoard, int parentBoardId,byte fromSquareBits, int fromSquareIdx) {
        System.out.print("  void createBoards(chessData, ply");
        System.out.print("{startIdx()=" + ply.startIdx() + ", size()=" + ply.size()+"}");
        System.out.print(", weightTable, moves="+moves+", parentBoard");
        System.out.print("{id()=" + parentBoard.id() + ", firstChildIdx()=" + parentBoard.firstChildIdx() + ", moves()=" + parentBoard.moves()+"}");
        System.out.println(", parentBoardId="+parentBoardId+", fromsSquareBits, fromSquareIdx=" + fromSquareIdx+")");
    }

        @CodeReflection
    public static int createBoards(ChessData chessData,  PlyTable.Ply ply,  WeightTable weightTable, int moves,  ChessData.Board parentBoard, int parentBoardId, byte fromSquareBits, int fromSquareIdx) {
        traceCreateBoards(chessData,ply,weightTable,moves,parentBoard,parentBoardId,fromSquareBits,fromSquareIdx);

        int fromx = fromSquareIdx%8;
        int fromy = fromSquareIdx/8;
        byte fromPieceValue = pieceValue(fromSquareBits);
        int boardFirstChildIdx = parentBoard.firstChildIdx();
        int boardIdBase = ply.startIdx()+boardFirstChildIdx;

        if (isKing(fromPieceValue)) {
            for (int moveIdx = 7; moveIdx > 0; moveIdx--) {
                int dxdy = 0b1111 & (CompassDxDyMap >>> (moveIdx * 4));
                int dy = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                dxdy >>>= 2;
                int dx = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                int tox = fromx + dx;
                int toy = fromy + dy;
                if (isOnBoard(tox, toy)) {
                    var toSquareIdx = toy*8+tox;
                    var toSquareBits = parentBoard.squareBits(toSquareIdx);
                    if (isEmptyOrOpponent(fromSquareBits, toSquareBits)) {
                        createBoard(chessData, ply,weightTable, parentBoard,parentBoardId,moves,  boardIdBase+moves,fromSquareIdx,toSquareIdx);
                        moves++;
                    }
                }
            }
        } else if (isPawn(fromPieceValue)) {
            int forward = isWhite(fromSquareBits) ? -1 : 1;
            int count = (fromy == (isWhite(fromSquareBits) ? 6 : 1)) ? 4 : 3;  // four moves if home else three
            for (int moveIdx = 0; moveIdx < count; moveIdx++) {
                int dxdy = 0b1111 & (ChessConstants.PawnDxDyMap >>> (moveIdx * 4));
                int toy = fromy + (forward * ((dxdy & 0b11) - 1));
                dxdy >>>= 2;
                int tox = fromx + (dxdy & 0b11) - 1;
                if (isOnBoard(tox, toy)) {
                    var toSquareIdx = toy*8+tox;
                    var toSquareBits = parentBoard.squareBits(toSquareIdx);
                    if (((moveIdx > 1) && isEmpty(toSquareBits)) || (moveIdx < 2) && isOpponent(fromSquareBits, toSquareBits)) {
                        createBoard(chessData, ply,weightTable, parentBoard, parentBoardId,moves, boardIdBase+moves,fromSquareIdx, toSquareIdx);
                        moves++;
                    }
                }
            }
        } else if (isKnight(fromPieceValue)) {
            for (int moveIdx = 0; moveIdx < 8; moveIdx++) {
                int dxdy = 0b1111 & (ChessConstants.KnightDxDyMap >>> (moveIdx * 4));
                int toy = fromy + ((dxdy & 0b1) + 1) * ((dxdy & 0b10) - 1);
                dxdy >>>= 2;
                int tox = fromx + ((dxdy & 0b1) + 1) * ((dxdy & 0b10) - 1);
                if (isOnBoard(tox, toy)) {
                    var toSquareIdx = toy*8+tox;
                    var toSquareBits = parentBoard.squareBits(toSquareIdx);
                    if (isEmptyOrOpponent(fromSquareBits, toSquareBits)) {
                        createBoard(chessData, ply,weightTable, parentBoard, parentBoardId, moves, boardIdBase+moves,fromSquareIdx, toSquareIdx );
                        moves++;
                    }
                }
            }
        } else {
            // sliders Rook, Queen, Bishop
            final int compassPoints = isBishop(fromPieceValue) ? DIAGS : isRook(fromPieceValue)? COLROWS : ALL_POINTS;

            // compassPoints have use 1 or 0 for bit for eligible move relative to fromx,fromy
            // We only encode the neighbours so the 3x3 grid has no center representation
            //                piece being moved
            //                     is here
            //                        v
            //              nw n ne e | w sw s se
            //                \ | | | | | | | /
            //                 \\ | | | | | //
            //                  ||| | v | |||
            //     diags   == 0b101_0___0_101
            //     colrows == 0b010_1___1_010
            //   allpoints == 0b111_1___1_111


            for (int moveIdx = 7; moveIdx > 0; moveIdx--) {
                int moveBit = (1 << moveIdx); // so 0b10000000 -> 0b01000000 -> ... 0b00000001
                // check if we can move this way
                // i.e is bit is set for this compassPoint
                if ((compassPoints & moveBit) == moveBit) {
                    // Now let's determine what a move in this dir looks like
                    int dxdy = 0b1111 & (CompassDxDyMap >>> (moveIdx * 4));
                    int dy = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                    dxdy >>>= 2;
                    int dx = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                    boolean blocked = false;
                    for (int slide = 1; !blocked && slide < 8; slide++) { //1,2,3,4,5,6,7
                        int tox = fromx + slide * dx;
                        int toy = fromy + slide * dy;
                        blocked = isOffBoard(tox, toy);
                        if (!blocked) {
                            var toSquareIdx = toy*8+tox;
                            var toSquareBits = parentBoard.squareBits(toSquareIdx);
                            if (isEmptyOrOpponent(fromSquareBits, toSquareBits)) {
                                createBoard(chessData, ply,weightTable,parentBoard,parentBoardId, moves, boardIdBase+moves, fromSquareIdx, toSquareIdx );
                                moves++;
                                if (isOpponent(toSquareBits, fromSquareBits)) {
                                    blocked = true;
                                }
                            } else {
                                blocked = true;
                            }
                        }
                    }
                }
            }
        }
      return moves;
    }


    public static void traceDoMovesKernelCore(ChessData chessData, PlyTable.Ply ply,  WeightTable weightTable,ChessData.Board board, int boardId) {
        System.out.print(" void doMovesKernelCore(chessData, ply");
        System.out.print("{startIdx()=" + ply.startIdx() + ", size()=" + ply.size()+"}");
        System.out.print(", weightTable,  boardId=" + boardId+", <board");
        System.out.println("{id()=" + board.id() + ", firstChildIdx()=" + board.firstChildIdx() + ", moves()=" + board.moves()+"}>)");
    }

    @CodeReflection
    public static void doMovesKernelCore(ChessData chessData, PlyTable.Ply ply,  WeightTable weightTable, int boardId) {
        ChessData.Board board = chessData.board(boardId);
        board.id(boardId);
        traceDoMovesKernelCore(chessData,ply,weightTable,board, boardId);
        int moves = 0;
        for (int squareIdx = 0; squareIdx < 64; squareIdx++) {
            byte squareBits = board.squareBits(squareIdx);
            if (isComrade((byte) ply.side(), squareBits)) {
                moves = createBoards(chessData, ply, weightTable, moves, board, boardId, squareBits, squareIdx);
            }
        }
    }

    @CodeReflection
    public static void doMovesKernel(KernelContext kc, ChessData chessData, PlyTable plyTable, WeightTable weightTable) {
        if (kc.x < kc.maxX) {
            PlyTable.Ply ply  =plyTable.ply(plyTable.idx());
            doMovesKernelCore( chessData,  ply,weightTable, kc.x+ply.startIdx());
        }
    }
    @CodeReflection
    static public void doMovesCompute(final ComputeContext cc, ChessData chessData, PlyTable plyTable, WeightTable weightTable) {
        // We can't pass ply because HAT kernels expect Buffer roots.
        cc.dispatchKernel(plyTable.ply(plyTable.idx()).size(), kc -> doMovesKernel(kc, chessData, plyTable, weightTable));
    }

    public static void plyMoves(Accelerator accelerator, boolean useIntStream, ChessData chessData, PlyTable plyTable, WeightTable weightTable) {
        if (useIntStream) {
            PlyTable.Ply ply =plyTable.ply(plyTable.idx());
            IntStream.range(0, ply.size())
                    .forEach(id ->
                        Compute.doMovesKernelCore(chessData, ply, weightTable, id+ply.startIdx())
                    );
        } else {
            accelerator.compute(cc -> Compute.doMovesCompute(cc, chessData, plyTable,weightTable));
        }
    }

   // public static int flipBit(byte bitToFlip, byte bits) {
    //    byte inv = ; //  0b00101000 ^ 0b00100000 = 0b00001000
     //   return inv;
   // }
}
