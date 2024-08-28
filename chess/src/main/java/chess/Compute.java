package chess;

import hat.ComputeContext;
import hat.KernelContext;

import java.lang.runtime.CodeReflection;

import static chess.ChessConstants.ALL_POINTS;
import static chess.ChessConstants.BISHOP;
import static chess.ChessConstants.CHECK;
import static chess.ChessConstants.COLROWS;
import static chess.ChessConstants.CompassDxDyMap;
import static chess.ChessConstants.DIAGS;
import static chess.ChessConstants.DxOrDyMASK;
import static chess.ChessConstants.DyDxMask_SHIFT;
import static chess.ChessConstants.EMPTY_SQUARE;
import static chess.ChessConstants.NOT_AT_HOME;
import static chess.ChessConstants.NOT_AT_HOME_SHIFT;
import static chess.ChessConstants.ROOK;
import static chess.ChessConstants.ROW_SHIFT;
import static chess.ChessConstants.WEIGHT_MASK_SHIFT;
import static chess.ChessConstants.WHITE_BIT;

public class Compute {
    @CodeReflection
    static byte pieceValue(byte squareBits) {
        return (byte) (squareBits & ChessConstants.PIECE_MASK);
    }
    @CodeReflection
    public static boolean isSet(byte sqBits, byte bit) {
        return (sqBits & bit) == bit;
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

    /*
     * int dy = (dxdy & DxOrDyMASK) - 1;// 00->-1, 01->0, 10->1, 11->2
     *
     * dxdy >>>= 2;
     *int dx = (dxdy & DxOrDyMASK) - 1;// 00->-1, 01->0, 10->1, 11->2
     */
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
        return ChessConstants.DxDyMASK & (CompassDxDyMap >>> (dirIdx * DyDxMask_SHIFT));
    }
    @CodeReflection
    public static int pawnAtHomeDxDy( int moveIdx) {
        return ChessConstants.DxDyMASK & (ChessConstants.PawnDxDyMap >>> (moveIdx * DyDxMask_SHIFT));
    }
    @CodeReflection
    public static int knightDxDy( int moveIdx) {
        return ChessConstants.DxDyMASK & (ChessConstants.KnightDxDyMap >>> (moveIdx * DyDxMask_SHIFT));
    }


    @CodeReflection
    public static boolean plySide(int side, int bits ){
        boolean sameSide = (side  & WHITE_BIT) == (bits & WHITE_BIT);
        return sameSide;
    }

    /*
     * -1 if white, 1 if black
     *                 >>2        -1      *-1
     * WHITE=b1000 -> 0b0010 -> 0b0001 ->  -1
     * BLACK=b0000 -> 0b0000 -> 0b1111 ->   1
     */
    @CodeReflection
    public static int plyDir(int side) {
        int shifted = side>>>2;
        int adjusted = shifted-1;
        return adjusted*-1;
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
        int fromX = fromSqId % 8;
        int fromY = fromSqId / 8;
        int moves = 0;
         byte fromPieceValue = pieceValue(fromSqBits);
        if (isKing(fromPieceValue)) {
            for (int dirIdx = 0; dirIdx < 8; dirIdx++) {
                int dxdy = compassDxDy(dirIdx);
                int toX = fromX + compassDx(dxdy);
                int toY = fromY + compassDy(dxdy);
                if (isOnBoard(toX, toY)){
                    byte toSqId = (byte)((toY<<ROW_SHIFT) + toX);
                    byte toSqBits= board.squareBits(toSqId);
                    if (isEmptyOrOpponent(fromSqBits, toSqBits)) {
                        moves++;
                    }
                }
            }
        } else if (isPawn(fromPieceValue)) {
            int forward = plyDir(ply.side());                //WHITE=-1 BLACK = 1
            int count = 4-((fromSqBits&NOT_AT_HOME)>>>NOT_AT_HOME_SHIFT);    // 3 or 4
            boolean blocked = false;
            for (int moveIdx = 0; !blocked && moveIdx<count; moveIdx++) {
                int dxdy = pawnAtHomeDxDy(moveIdx);
                int toY = fromY + forward * pawnDy(dxdy);
                int toX = fromX + pawnDx(dxdy);
                if (isOnBoard(toX, toY)) {
                    byte toSqId= (byte)((toY<<ROW_SHIFT) + toX);
                    byte toSqBits = board.squareBits(toSqId);
                    if (toX!=fromX) { // we can take!
                        if (isOpponent(fromSqBits, toSqBits)) {
                            moves++;
                        }
                    }else{
                        if (isEmpty(toSqBits)) { // we can move
                            moves++;
                        } else {
                            blocked = true;
                        }
                    }
                }
            }
        } else if (isKnight(fromPieceValue)) {
            for (int moveIdx = 0; moveIdx < 8; moveIdx++) {
                int dxdy = knightDxDy(moveIdx);
                int toY = fromY + knightDy(dxdy); // dy +-2 || dy +- 2
                int toX = fromX + knightDx(dxdy); // dx +-2 || dy += 2
                if (isOnBoard(toX, toY)){
                    byte toSqId= (byte)((toY<<ROW_SHIFT) + toX);
                    byte toSqBits = board.squareBits(toSqId);
                    if (isEmptyOrOpponent(fromSqBits,toSqBits)) {
                        moves++;
                    }
                }
            }
        } else {
            // sliders Rook, Queen, Bishop
            final int compassPoints = isBishop(fromPieceValue) ? DIAGS : isRook(fromPieceValue) ? COLROWS : /* MUST BE QUEEN */ALL_POINTS;

            // compassPoints have use 1 or 0 for bit for eligible move relative to fromX,fromY
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
                    // Get the dx,dy for this move in this dir
                    int dxdy = compassDxDy(dirIdx);
                    int dy = compassDy(dxdy);
                    int dx = compassDx(dxdy);
                    boolean blocked = false;
                    for (int r = 1; !blocked && r<8; r++) { //1,2,3,4,5,6,7
                        int toX = fromX + r * dx;
                        int toY = fromY + r * dy;
                        blocked = isOffBoard(toX, toY);
                        if (!blocked) {
                            byte toSqId = (byte)((toY<<ROW_SHIFT) + toX);
                            byte toSqBits = board.squareBits(toSqId);
                            if (isEmptyOrOpponent(fromSqBits, toSqBits)) {
                                moves++;
                                if (isOpponent(fromSqBits, toSqBits)) {
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
    public static void countMovesAndScoreBoard(Ply ply, WeightTable weightTable, ChessData.Board parentBoard, ChessData.Board board) {
        // traceCountMovesAndScoreBoard(ply, weightTable, newBoard);
        int moves = 0;
        int opponentScore = 0;
        int sideScore = 0;
        for (int sqId = 0; sqId < 64; sqId++) {
            byte fromSqBits = board.squareBits(sqId);
            if (!isEmpty(fromSqBits)) {
                byte piece = pieceValue(fromSqBits);
                int pieceWeight =  weightTable.weight((piece-1) /* pawn = 1 */ *64 +(isWhite(fromSqBits)?0:6*64));
                if (plySide(ply.side(),fromSqBits)) {
                    sideScore+=pieceWeight*piece;
                    moves += countMovesForSquare(ply, board, fromSqBits, sqId);
                }else{
                    opponentScore+=pieceWeight*piece;
                }
            }
        }
        board.sideScore((short)sideScore);
        board.opponentScore((short)opponentScore);
        board.moves((byte) moves);
        board.gameScore((sideScore-opponentScore) - parentBoard.gameScore());
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
    public static void createBoard(ChessData chessData, Ply ply, WeightTable weightTable, ChessData.Board parentBoard, int parentBoardId, int move, byte fromSqId, byte toSqId) {
        // traceCreateBoard(ply,parentBoard,newBoard,fromSqId,toSqId);
        ChessData.Board newBoard = chessData.board(parentBoard.firstChildIdx() + move);
        newBoard.parent(parentBoardId);
        newBoard.move((byte) move);
        newBoard.fromSqId(fromSqId);
        newBoard.toSqId(toSqId);
        for (int sqId = 0; sqId < 64; sqId++) {
            newBoard.squareBits(sqId, parentBoard.squareBits(sqId));
        }
        newBoard.squareBits(fromSqId, EMPTY_SQUARE);
        newBoard.squareBits(toSqId, (byte)(parentBoard.squareBits(fromSqId)|NOT_AT_HOME));  // it is not at home now

        // We take the opportunity to mark the parents squareBits for the toSqId id as 'in check'
        // meaning that parent(toSqId) in the next ply can move here  to it
        // this is somewhat scary as the parent is shared.. multiple pieces might be able to move to it
        // and all the piece moves are currently in flight ie there are multiple threads possibly racing on this square
        // it is possible that a runtime might not like this.
        // However, as the mutation is flipping a single bit we don't expect anyone to see any 'invalid' bit patterns
        parentBoard.squareBits(toSqId,(byte)(parentBoard.squareBits(toSqId)|CHECK)); // danger we are racing here!


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
    public static int createBoards(ChessData chessData, Ply ply, WeightTable weightTable,
                                   int moves, ChessData.Board parentBoard, int parentBoardId, byte fromSqBits, byte fromSqId) {
        //  traceCreateBoards(ply,moves,parentBoard,parentBoardId,fromSquareBits,fromSqId);

        int fromX = fromSqId % 8;
        int fromY = fromSqId / 8;
        byte fromPieceValue = pieceValue(fromSqBits);

        if (isKing(fromPieceValue)) {
            for (int dirIdx = 0; dirIdx < 8; dirIdx++) {
                int dxdy = compassDxDy(dirIdx);
                int toX = fromX + compassDx(dxdy);
                int toY = fromY + compassDy(dxdy);
                if (isOnBoard(toX, toY)) {
                    byte toSqId = (byte)((toY<<ROW_SHIFT) + toX);
                    byte toSqBits = parentBoard.squareBits(toSqId);
                    if (isEmptyOrOpponent(fromSqBits, toSqBits)) {
                        createBoard(chessData,ply, weightTable, parentBoard,parentBoardId,moves,  fromSqId, toSqId);
                        moves++;
                    }
                }
            }
        } else if (isPawn(fromPieceValue)) {
            int forward = plyDir(ply.side());                //WHITE=-1 BLACK = 1
            int count = 4-((fromSqBits&NOT_AT_HOME)>>>NOT_AT_HOME_SHIFT);    // 3 or 4
            boolean blocked = false;
            for (int moveIdx = 0; !blocked && moveIdx<count; moveIdx++) {  //takes
                int dxdy = pawnAtHomeDxDy(moveIdx);
                int dy = pawnDy(dxdy);
                int toY = fromY + forward * dy;
                int toX = fromX + pawnDx(dxdy);
                if (isOnBoard(toX, toY)) {
                    byte toSqId= (byte)((toY<<ROW_SHIFT) + toX);
                    byte toSqBits = parentBoard.squareBits(toSqId);
                    if (toX!=fromX) {
                        if (isOpponent(fromSqBits, toSqBits)) {
                            createBoard(chessData, ply, weightTable, parentBoard, parentBoardId, moves, fromSqId, toSqId);
                            moves++;
                        }
                    }else{
                        if (isEmpty(toSqBits)) {
                            createBoard(chessData,ply, weightTable, parentBoard,parentBoardId,moves, fromSqId, toSqId);
                            moves++;
                        } else {
                            blocked = true;
                        }
                    }
                }
            }

        } else if (isKnight(fromPieceValue)) {
            for (int moveIdx = 0; moveIdx < 8; moveIdx++) {
                int dxdy = knightDxDy(moveIdx);
                int toY = fromY + knightDy(dxdy);
                int toX = fromX + knightDx(dxdy);
                if (isOnBoard(toX, toY)) {
                    byte toSqId= (byte)((toY<<ROW_SHIFT) + toX);
                    if (isEmptyOrOpponent(fromSqBits, parentBoard.squareBits(toSqId))) {
                        createBoard(chessData,ply, weightTable, parentBoard, parentBoardId,moves, fromSqId, toSqId);
                        moves++;
                    }
                }
            }
        } else {
            // sliders Rook, Queen, Bishop
            final int compassPoints = isBishop(fromPieceValue) ? DIAGS : isRook(fromPieceValue) ? COLROWS : ALL_POINTS;

            // compassPoints have use 1 or 0 for bit for eligible move relative to fromX,fromY
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
                    int dxdy = compassDxDy(dirIdx);
                    int dy = compassDy(dxdy);
                    int dx = compassDx(dxdy);
                    boolean blockedOrOffBoard = false;
                    for (int r = 1; !blockedOrOffBoard && r < 8; r++) {
                        int toX = fromX + r * dx;
                        int toY = fromY + r * dy;
                        blockedOrOffBoard = isOffBoard(toX, toY);
                        if (!blockedOrOffBoard) {
                            byte toSqId = (byte)((toY<<ROW_SHIFT)+toX);
                            var toSqBits = parentBoard.squareBits(toSqId);
                            if (isEmptyOrOpponent(fromSqBits, toSqBits)) {
                                createBoard(chessData, ply,weightTable, parentBoard,parentBoardId,moves, fromSqId, toSqId);
                                moves++;
                                if (isOpponent(fromSqBits, toSqBits)) {
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
            if (plySide(ply.side(), squareBits)){
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
