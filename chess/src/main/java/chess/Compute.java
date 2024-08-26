package chess;

import hat.ComputeContext;
import hat.KernelContext;

import java.lang.runtime.CodeReflection;

import static chess.ChessConstants.ALL_POINTS;
import static chess.ChessConstants.BISHOP;
import static chess.ChessConstants.COLROWS;
import static chess.ChessConstants.CompassDxDyMap;
import static chess.ChessConstants.DIAGS;
import static chess.ChessConstants.DxOrDyMASK;
import static chess.ChessConstants.EMPTY_SQUARE;
import static chess.ChessConstants.ROOK;
import static chess.ChessConstants.WHITE_BIT;

public class Compute {
    @CodeReflection
    static byte pieceValue(byte squareBits) {
        return (byte) (squareBits & ChessConstants.PIECE_MASK);
    }

    @CodeReflection
    static byte side(byte squareBits) {
        return (byte) (squareBits & WHITE_BIT);
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

    //int dy = (dxdy & DxOrDyMASK) - 1;// 00->-1, 01->0, 10->1, 11->2
    //dxdy >>>= 2;
    //int dx = (dxdy & DxOrDyMASK) - 1;// 00->-1, 01->0, 10->1, 11->2

    @CodeReflection
    public static int compassDy(int dxdy) {
        return (dxdy & DxOrDyMASK) - 1;
    }

    @CodeReflection
    public static int compassDx(int dxdy) {
        return compassDy(dxdy >>> 2);
    }
    @CodeReflection
    public static int pawnDy(int dxdy) {
        return (dxdy & DxOrDyMASK) - 1;
    }

    @CodeReflection
    public static int pawnDx(int dxdy) {
        return pawnDy(dxdy >>> 2);
    }

    @CodeReflection
    public static int knightDy(int dxdy) {
        return ((dxdy & 0b1) + 1) * ((dxdy & 0b10) - 1); // +-2 | +-1
    }

    @CodeReflection
    public static int knightDx(int dxdy) {
        return knightDy(dxdy >>> 2);
    }

    @CodeReflection
    static boolean validDir(int compassPoints, int moveIdx) {
        return ((compassPoints >>> moveIdx) & 1) == 1;
    }

    @CodeReflection
    public static int compassDxDy( int dirIdx) {
        return ChessConstants.DxDyMASK & (CompassDxDyMap >>> (dirIdx * 4));
    }
    @CodeReflection
    public static int pawnDxDy( int moveIdx) {
        return ChessConstants.DxDyMASK & (ChessConstants.PawnDxDyMap >>> (moveIdx * 4));
    }
    @CodeReflection
    public static int knightDxDy( int moveIdx) {
        return ChessConstants.DxDyMASK & (ChessConstants.KnightDxDyMap >>> (moveIdx * 4));
    }
    public static void traceCountMovesForSquare(Ply ply, ChessData.Board board, byte fromBits, int fromSqId) {
        System.out.print("     void countMovesForSquare(ply");
        System.out.print("{" + ply.fromBoardId() + "-" + ply.toBoardId() + " size=" + ply.size() + "}");
        System.out.print(", board");
        System.out.print("{ firstChildIdx=" + board.firstChildIdx() + ", moves=" + board.moves() + "}");
        System.out.println(", fromBits=" + Terminal.piece(fromBits) + ", fromSqId=" + fromSqId + ")");
        System.out.println("     " + new Terminal().lineHighlight(board, true, fromSqId));
    }

    public static void traceOutCountMovesForSquare(Ply ply, ChessData.Board board, byte fromBits, int fromSqId, int moves) {
        System.out.print("     " + moves + "<- void countMovesForSquare(ply");
        System.out.print("{" + ply.fromBoardId() + "-" + ply.toBoardId() + " size=" + ply.size() + "}");
        System.out.print(", board");
        System.out.print("{ firstChildIdx=" + board.firstChildIdx() + ", moves=" + board.moves() + "}");
        System.out.println(", fromBits=" + Terminal.piece(fromBits) + ", fromSqId=" + fromSqId + ")");
    }

    @CodeReflection
    public static int countMovesForSquare(Ply ply, ChessData.Board board, byte fromSqBits, int fromSqId) {
        //   traceCountMovesForSquare(ply, board, fromBits, fromSqId);
        int fromx = fromSqId % 8;
        int fromy = fromSqId / 8;
        int moves = 0;
        int sideMul = (ply.side() >>> 2) - 1;  //WHITE=b1000 -> 0b0010 == 2 (-1) -> 1 BLACK=b0000 -> 0b0000 == 0 (-1) -> -1
        int otherSideMul = sideMul * -1;
        byte fromPieceValue = pieceValue(fromSqBits);
        if (isKing(fromPieceValue)) {
            for (int dirIdx = 0; dirIdx < 8; dirIdx++) {
                int dxdy = compassDxDy(dirIdx);
                int tox = fromx + compassDx(dxdy);
                int toy = fromy + compassDy(dxdy);
                if (isOnBoard(tox, toy) && isEmptyOrOpponent(fromSqBits, board.squareBits(toy * 8 + tox))) {
                    moves++;
                }
            }
        } else if (isPawn(fromPieceValue)) {
            int count = (fromy == (isWhite(fromSqBits) ? 6 : 1)) ? 4 : 3;  // four moves if home else three
            for (int moveIdx = 0; moveIdx < count; moveIdx++) {
                int dxdy = pawnDxDy(moveIdx);
                int toy = fromy + otherSideMul * pawnDy(dxdy);
                int tox = fromx + pawnDx(dxdy);
                if (isOnBoard(tox, toy)) {
                    byte toBits = board.squareBits(toy * 8 + tox);
                    if (((moveIdx > 1) && isEmpty(toBits)) || (moveIdx < 2) && isOpponent(fromSqBits, toBits)) {
                        moves++;
                    }
                }
            }
        } else if (isKnight(fromPieceValue)) {
            for (int moveIdx = 0; moveIdx < 8; moveIdx++) {
                int dxdy = knightDxDy(moveIdx);
                int toy = fromy + knightDy(dxdy); // dy +-2 || dy +- 2
                int tox = fromx + knightDx(dxdy); // dx +-2 || dy += 2
                if (isOnBoard(tox, toy) && isEmptyOrOpponent(fromSqBits, board.squareBits(toy * 8 + tox))) {
                    moves++;
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


            for (int dirIdx = 0; dirIdx < 8; dirIdx++) {
                // check if we can move this way i.e is bit is set for the compassPoints of this piece
                if (validDir(compassPoints, dirIdx)) {
                    // Now let's determine what a move in this dir looks like
                    int dxdy = compassDxDy(dirIdx);
                    int dy = compassDy(dxdy);
                    int dx = compassDx(dxdy);
                    boolean blocked = false;
                    for (int radius = 1; !blocked && radius<8; radius++) { //1,2,3,4,5,6,7
                        int tox = fromx + radius * dx;
                        int toy = fromy + radius * dy;
                        blocked = isOffBoard(tox, toy);
                        if (!blocked) {
                            byte toBits = board.squareBits(toy * 8 + tox);
                            if (isEmptyOrOpponent(fromSqBits, toBits)) {
                                moves++;
                                if (isOpponent(fromSqBits, toBits)) {
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
        //  traceOutCountMovesForSquare(ply, board, fromBits, fromSqId, moves);
        return moves;
    }

    public static void traceCountMovesAndScoreBoard(Ply ply, WeightTable weightTable, ChessData.Board newBoard) {
        System.out.print("    void countMovesForBoard(chessData, ply");
        System.out.print("{" + ply.fromBoardId() + "-" + ply.toBoardId() + " size=" + ply.size() + "}");
        System.out.print(", weightTable, newBoard");
        System.out.println("{firstChildIdx=" + newBoard.firstChildIdx() + ", moves=" + newBoard.moves() + "})");

    }

    public static void traceOutCountMovesAndScoreBoard(Ply ply, WeightTable weightTable, ChessData.Board newBoard) {
        System.out.print("    <-- void countMovesForBoard(chessData, ply");
        System.out.print("{" + ply.fromBoardId() + "-" + ply.toBoardId() + " size=" + ply.size() + "}");
        System.out.print(", weightTable, newBoard");
        System.out.println("{firstChildIdx=" + newBoard.firstChildIdx() + ", moves=" + newBoard.moves() + "})");
    }

    @CodeReflection
    public static void countMovesAndScoreBoard(Ply ply, WeightTable weightTable, ChessData.Board parentBoard, ChessData.Board newBoard) {
        // traceCountMovesAndScoreBoard(ply, weightTable, newBoard);

        int moves = 0;
        int score = 0;
        for (int sqId = 0; sqId < 64; sqId++) {
            byte squareBits = newBoard.squareBits(sqId);
            byte piece = pieceValue(squareBits);
            if (!isEmpty(piece)) {
                // Because the weight array masks are valid for WHITE.  We invert the weightIndex table  (63-idx) for black
                int weightIndex = isWhite(squareBits) ? sqId : 63 - sqId;
                // now the piece value can be used an index into the weights table
                int weights = weightTable.weight(weightIndex);
                // shift and mask to get the weight for this piece remember pawn=1, knight=2 etc
                int pieceWeight = (weights >>> (piece * 4)) & ChessConstants.WEIGHT_MASK;
                if (pieceWeight > 7) {// there must be an easier way ;)
                    pieceWeight = (7 - pieceWeight); // 8-F =>  -1,-2,-3,-4,-5,-6,-7
                    pieceWeight = pieceWeight + 8;  //          7, 6, 5, 4, 3, 2, 1
                    pieceWeight = pieceWeight * -1;  // 8-F  => -7,-6,-5,-4,-3,-2,-1
                } else {
                    pieceWeight = pieceWeight;     // 0-7      => 0,1,2,3,4,5,6,7
                    pieceWeight = 7 - pieceWeight;   // 0-7      => 7,6,5,4,3,2,1,0
                }
                score += pieceWeight * piece;
                if (isComrade((byte) (ply.side() ^ WHITE_BIT), squareBits)) {
                    moves += countMovesForSquare(ply, newBoard, squareBits, sqId);
                }

            }
        }
        int sideMul = (ply.side() >>> 2) - 1;  //WHITE=b1000 -> 0b0010 == 2 (-1) -> 1 BLACK=b0000 -> 0b0000 == 0 (-1) -> -1
        newBoard.boardScore(score * sideMul);
        newBoard.moves((byte) moves);
        newBoard.gameScore(score + sideMul * parentBoard.gameScore());
    }

    public static void traceCreateBoard(Ply ply, ChessData.Board parentBoard, ChessData.Board newBoard, byte fromSqId, byte toSqId) {
        System.out.print("   void createBoard(chessData, ply");
        System.out.print("{" + ply.fromBoardId() + "-" + ply.toBoardId() + " size=" + ply.size() + "}");
        System.out.print(", weightTable, parentBoard");
        System.out.print("{firstChildIdx=" + parentBoard.firstChildIdx() + ", moves=" + parentBoard.moves() + "}");
        System.out.print(", newBoard");
        System.out.print("{firstChildIdx=" + newBoard.firstChildIdx() + ", moves=" + newBoard.moves() + "}");
        System.out.println(", fromIdx=" + fromSqId + ", toIdx=" + toSqId + ")");
    }

    public static void traceOutCreateBoard(Ply ply, ChessData.Board parentBoard, ChessData.Board newBoard, byte fromSqId, byte toSqId) {
        System.out.print("   <-- void createBoard(chessData, ply");
        System.out.print("{" + ply.fromBoardId() + "-" + ply.toBoardId() + " size=" + ply.size() + "}");
        System.out.print(", weightTable, parentBoard");
        System.out.print("{firstChildIdx=" + parentBoard.firstChildIdx() + ", moves=" + parentBoard.moves() + "}");
        System.out.print(", newBoard");
        System.out.print("{firstChildIdx=" + newBoard.firstChildIdx() + ", moves=" + newBoard.moves() + "}");
        System.out.println(", fromIdx=" + fromSqId + ", toIdx=" + toSqId + ")");
    }

    @CodeReflection
    public static void createBoard(Ply ply, WeightTable weightTable, ChessData.Board parentBoard, ChessData.Board newBoard, byte fromSqId, byte toSqId) {
        // traceCreateBoard(ply,parentBoard,newBoard,fromSqId,toSqId);
        newBoard.fromSqId(fromSqId);
        newBoard.toSqId(toSqId);
        for (int sqId = 0; sqId < 64; sqId++) {
            newBoard.squareBits(sqId, parentBoard.squareBits(sqId));
        }
        newBoard.squareBits(fromSqId, EMPTY_SQUARE);
        newBoard.squareBits(toSqId, parentBoard.squareBits(fromSqId));
        countMovesAndScoreBoard(ply, weightTable, parentBoard, newBoard);
        // traceOutCreateBoard(ply,parentBoard,newBoard,fromSqId,toSqId);
    }

    public static void traceCreateBoards(Ply ply, int moves, ChessData.Board parentBoard, int parentBoardId, byte fromSquareBits, int fromSqId) {
        System.out.print("  void createBoards(chessData, ply");
        System.out.print("{" + ply.fromBoardId() + "-" + ply.toBoardId() + " size=" + ply.size() + "}");
        System.out.print(", weightTable, moves=" + moves + ", parentBoard");
        System.out.print("{firstChildIdx=" + parentBoard.firstChildIdx() + ", moves=" + parentBoard.moves() + "}");
        System.out.println(", parentBoardId=" + parentBoardId + ", fromsSquareBits, fromSqId=" + fromSqId + ")");
        System.out.println("  " + new Terminal().lineHighlight(parentBoard, true, fromSqId));
    }

    public static void traceOutCreateBoards(Ply ply, int moves, ChessData.Board parentBoard, int parentBoardId, byte fromSquareBits, int fromSqId) {
        System.out.print("  " + moves + " <- void createBoards(chessData, ply");
        System.out.print("{" + ply.fromBoardId() + "-" + ply.toBoardId() + " size=" + ply.size() + "}");
        System.out.print(", weightTable, moves=" + moves + ", parentBoard");
        System.out.print("{firstChildIdx=" + parentBoard.firstChildIdx() + ", moves=" + parentBoard.moves() + "}");
        System.out.println(", parentBoardId=" + parentBoardId + ", fromsSquareBits, fromSqId=" + fromSqId + ")");
    }


    @CodeReflection
    public static int createBoards(ChessData chessData, Ply ply, WeightTable weightTable, int moves, ChessData.Board parentBoard, int parentBoardId, byte fromSqBits, byte fromSqId) {
        //  traceCreateBoards(ply,moves,parentBoard,parentBoardId,fromSquareBits,fromSqId);
        int sideMul = (ply.side() >>> 2) - 1;  //WHITE=b1000 -> 0b0010 == 2 (-1) -> 1 BLACK=b0000 -> 0b0000 == 0 (-1) -> -1
        int otherSideMul = sideMul * -1;
        int fromx = fromSqId % 8;
        int fromy = fromSqId / 8;
        byte fromPieceValue = pieceValue(fromSqBits);

        if (isKing(fromPieceValue)) {
            for (int dirIdx = 0; dirIdx < 8; dirIdx++) {
                int dxdy = compassDxDy(dirIdx);
                int tox = fromx + compassDx(dxdy);
                int toy = fromy + compassDy(dxdy);
                if (isOnBoard(tox, toy)) {
                    byte toSqId = (byte) (toy * 8 + tox);
                    if (isEmptyOrOpponent(fromSqBits, parentBoard.squareBits(toSqId))) {
                        ChessData.Board newBoard = chessData.board(parentBoard.firstChildIdx() + moves);
                        newBoard.parent(parentBoardId);
                        newBoard.move((byte) moves);
                        createBoard(ply, weightTable, parentBoard, newBoard, fromSqId, toSqId);
                        moves++;
                    }
                }
            }
        } else if (isPawn(fromPieceValue)) {
            int count = (fromy == (isWhite(fromSqBits) ? 6 : 1)) ? 4 : 3;  // four moves if home else three
            for (int moveIdx = 0; moveIdx < count; moveIdx++) {
                int dxdy = pawnDxDy(moveIdx);
                int toy = fromy + otherSideMul * pawnDy(dxdy);
                int tox = fromx + pawnDx(dxdy);
                if (isOnBoard(tox, toy)) {
                    byte toSqId = (byte) (toy * 8 + tox);
                    byte toSqBits = parentBoard.squareBits(toSqId);
                    if (((moveIdx > 1) && isEmpty(toSqBits)) || (moveIdx < 2) && isOpponent(fromSqBits, toSqBits)) {
                        ChessData.Board newBoard = chessData.board(parentBoard.firstChildIdx() + moves);
                        newBoard.parent(parentBoardId);
                        newBoard.move((byte) moves);
                        createBoard(ply, weightTable, parentBoard, newBoard, fromSqId, toSqId);
                        moves++;
                    }
                }
            }
        } else if (isKnight(fromPieceValue)) {
            for (int moveIdx = 0; moveIdx < 8; moveIdx++) {
                int dxdy = knightDxDy(moveIdx);
                int toy = fromy + knightDy(dxdy);
                int tox = fromx + knightDx(dxdy);
                if (isOnBoard(tox, toy)) {
                    byte toSqId = (byte) (toy * 8 + tox);
                    if (isEmptyOrOpponent(fromSqBits, parentBoard.squareBits(toSqId))) {
                        ChessData.Board newBoard = chessData.board(parentBoard.firstChildIdx() + moves);
                        newBoard.parent(parentBoardId);
                        newBoard.move((byte) moves);
                        createBoard(ply, weightTable, parentBoard, newBoard, fromSqId, toSqId);
                        moves++;
                    }
                }
            }
        } else {
            // sliders Rook, Queen, Bishop
            final int compassPoints = isBishop(fromPieceValue) ? DIAGS : isRook(fromPieceValue) ? COLROWS : ALL_POINTS;

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


            for (int dirIdx = 0; dirIdx < 8; dirIdx++) {
                if (validDir(compassPoints, dirIdx)) {
                    // Now let's determine what a move in this dir looks like
                    int dxdy = ChessConstants.DxDyMASK & (CompassDxDyMap >>> (dirIdx * 4));
                    int dy = compassDy(dxdy);
                    int dx = compassDx(dxdy);
                    boolean blockedOrOffBoard = false;
                    for (int radius = 1; !blockedOrOffBoard && radius < 8; radius++) {
                        int tox = fromx + radius * dx;
                        int toy = fromy + radius * dy;
                        blockedOrOffBoard = isOffBoard(tox, toy);
                        if (!blockedOrOffBoard) {
                            byte toSqId = (byte)(toy*8+tox);
                            var toSquareBits = parentBoard.squareBits(toSqId);
                            if (isEmptyOrOpponent(fromSqBits, toSquareBits)) {
                                ChessData.Board newBoard = chessData.board(parentBoard.firstChildIdx()+moves);
                                newBoard.parent(parentBoardId);
                                newBoard.move((byte)moves);
                                createBoard( ply,weightTable, parentBoard,newBoard, fromSqId, toSqId);
                                moves++;
                                if (isOpponent(fromSqBits, toSquareBits)) {
                                    blockedOrOffBoard = true;
                                }
                            }else{
                                blockedOrOffBoard=true;
                            }
                        }
                    }
                }
            }
        }
        // traceOutCreateBoards(ply,moves,parentBoard,parentBoardId,fromSquareBits,fromSqId);
        return moves;
    }


    public static void traceInDoMovesKernelCore(Ply ply, ChessData.Board board, int parentBoardId) {
        System.out.print(" void doMovesKernelCore(chessData, ply");
        System.out.print("{" + ply.fromBoardId() + "-" + ply.toBoardId() + " size=" + ply.size() + "}");
        System.out.print(", weightTable,  parentBoardId=" + parentBoardId + ", <board");
        System.out.println("{firstChildIdx=" + board.firstChildIdx() + ", moves()=" + board.moves() + "}>)");
    }

    public static void traceOutDoMovesKernelCore(Ply ply, ChessData.Board board, int parentBoardId, int moves) {
        System.out.print(" " + moves + " <-- void doMovesKernelCore(chessData, ply");
        System.out.print("{" + ply.fromBoardId() + "-" + ply.toBoardId() + " size=" + ply.size() + "}");
        System.out.print(", weightTable,  parentBoardId=" + parentBoardId + ", <board");
        System.out.println("{firstChildIdx=" + board.firstChildIdx() + ", moves()=" + board.moves() + "}>)");
    }

    @CodeReflection
    public static void doMovesKernelCore(ChessData chessData, Ply ply, WeightTable weightTable, int parentBoardId) {
        ChessData.Board parentBoard = chessData.board(parentBoardId);
        //  traceInDoMovesKernelCore(ply,parentBoard, parentBoardId);
        int moves = 0;
        for (byte sqId = 0; sqId < 64; sqId++) {
            byte squareBits = parentBoard.squareBits(sqId);
            if (isComrade((byte) ply.side(), squareBits)) {
                moves = createBoards(chessData, ply, weightTable, moves, parentBoard, parentBoardId, squareBits, sqId);
            }
        }
        //  traceOutDoMovesKernelCore(ply,parentBoard, parentBoardId, moves);

    }

    @CodeReflection
    public static void doMovesKernel(KernelContext kc, ChessData chessData, Ply ply, WeightTable weightTable) {
        if (kc.x < kc.maxX) {
            doMovesKernelCore(chessData, ply, weightTable, kc.x + ply.fromBoardId());
        }
    }

    @CodeReflection
    static public void doMovesCompute(final ComputeContext cc, ChessData chessData, Ply ply, WeightTable weightTable) {
        cc.dispatchKernel(ply.size(), kc -> doMovesKernel(kc, chessData, ply, weightTable));
    }
}
