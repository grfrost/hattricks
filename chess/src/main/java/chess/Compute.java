package chess;

import hat.ComputeContext;
import hat.KernelContext;

import java.lang.runtime.CodeReflection;

import static chess.ChessConstants.ALL_POINTS;
import static chess.ChessConstants.BISHOP;
import static chess.ChessConstants.BLACK_BIT;
import static chess.ChessConstants.CHECK;
import static chess.ChessConstants.COLROWS;
import static chess.ChessConstants.CompassDxDyMap;
import static chess.ChessConstants.DIAGS;
import static chess.ChessConstants.DxOrDyMASK;
import static chess.ChessConstants.DyDxMask_SHIFT;
import static chess.ChessConstants.EMPTY_SQUARE;
import static chess.ChessConstants.KNIGHT;
import static chess.ChessConstants.KnightDxDyMap;
import static chess.ChessConstants.MOVED;
import static chess.ChessConstants.MOVED_SHIFT;
import static chess.ChessConstants.PAWN;
import static chess.ChessConstants.PIECE_MASK;
import static chess.ChessConstants.PawnDxDyMap;
import static chess.ChessConstants.ROOK;
import static chess.ChessConstants.ROW_SHIFT;
import static chess.ChessConstants.SIDE_MASK;
import static chess.ChessConstants.WHITE_BIT;
import static chess.ChessConstants.WHITE_BIT_SHIFT;

public class Compute {
    @CodeReflection
    static byte pieceValue(byte squareBits) {
        return (byte) (squareBits & PIECE_MASK);
    }
    @CodeReflection
    public static boolean isSet(byte sqBits, byte bit) {
        return (sqBits & bit) == bit;
    }
    @CodeReflection
    public static byte set(byte bits, byte bit) {
        return (byte)(bits | bit);
    }
    @CodeReflection
    public  static byte reset(byte bits, byte bit) {
        return  (byte)(bits & (bit^0xff));
    }

    @CodeReflection
    static byte side(byte squareBits) {return (byte) (squareBits & SIDE_MASK);}

    @CodeReflection
    public static boolean isEmpty(byte squareBits) {
        return pieceValue(squareBits) == EMPTY_SQUARE;
    }

    @CodeReflection
    static boolean isPiece(byte squareBits) {
        return pieceValue(squareBits) != EMPTY_SQUARE;
    }

    @CodeReflection
    public static boolean isWhite(byte squareBits) {
        return side(squareBits) == WHITE_BIT;
    }
    @CodeReflection
    public static boolean isBlack(byte squareBits) {
        return side(squareBits) == BLACK_BIT;
    }

    @CodeReflection
    public static boolean isOpponent(byte fromBits, byte toBits) {
        return side(fromBits)!=side(toBits);
    }
    @CodeReflection
    public static boolean isComrade(byte fromBits, byte toBits) {
        return side(fromBits)==side(toBits);
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
        return pieceValue(squareBits) == PAWN;
    }

    @CodeReflection
    static boolean isKnight(byte squareBits) {
        return pieceValue(squareBits) == KNIGHT;
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
        return ChessConstants.DxDyMASK & (PawnDxDyMap >>> (moveIdx * DyDxMask_SHIFT));
    }
    @CodeReflection
    public static int knightDxDy( int moveIdx) {
        return ChessConstants.DxDyMASK & (KnightDxDyMap >>> (moveIdx * DyDxMask_SHIFT));
    }


    @CodeReflection
    public static boolean plySide(int side, byte bits ){
        return  isPiece(bits) && ((side&SIDE_MASK) == (bits&SIDE_MASK));
    }

    @CodeReflection
    public static boolean plyOtherSide(int side, byte bits ){
       return isPiece(bits) && ((side&SIDE_MASK) != (bits&SIDE_MASK));
    }

    /*
     * -1 if white, 1 if black
     * BLACK = 0b0001_0000  1<<4
     * WHITE = 0b0000_1000  1<<3
     *
     *                        >>>(WHITE_BIT_SHIFT-1)      &0b0000_0010     -1
     * WHITE=0b0000_1000 ->   0b0000_0001              -> 0b0000_00000 ->  0b1111_1111   -1
     * BLACK=0b0001_0000 ->   0b0000_0010              -> 0b0000_00010 ->  0b0000_0000    1
     */
    @CodeReflection
    public static int plyDir(int side) {
        if ((side & SIDE_MASK)==0){
            throw new IllegalStateException("WTF");
        }
        int shifted = side>>>(WHITE_BIT_SHIFT);
        int masked = shifted&2;
        int adjusted = masked-1;
        if (side==WHITE_BIT && adjusted!=-1){
            throw new IllegalStateException("WTF2");
        }
        if (side==BLACK_BIT && adjusted!=1){
            throw new IllegalStateException("WTF3");
        }
        return adjusted;
    }


    @CodeReflection
    public static int countMovesForSquare(Ply ply, ChessData.Board board, byte fromSqBits, int fromSqId) {
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
            int count = 4-((fromSqBits&MOVED)>>>MOVED_SHIFT);    // 3 or 4
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

            // We use compassPoints' 8 bits in to determine which of the points below are
            // eligible move relative to fromX,fromY. We only encode the neighbours
            // so the 3x3 grid has no center representation
            //                piece being moved
            //                     is here
            //                        v
            //              nw n ne e | w sw s se
            //                \ | | | | | | | /
            //                 \\ | | | | | //
            //                  ||| | v | |||
            //       diags == 0b101_0___0_101
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

        return moves;
    }


    @CodeReflection
    public static void countMovesAndScoreBoard(Ply ply, WeightTable weightTable, ChessData.Board parentBoard, ChessData.Board board) {
        int moves = 0;
        int opponentScore = 0;
        int sideScore = 0;
        for (int fromSqId = 0; fromSqId < 64; fromSqId++) {
            byte fromSqBits = board.squareBits(fromSqId);
            if (!isEmpty(fromSqBits)) {
                byte pieceValue = pieceValue(fromSqBits);
                int pieceWeight =  weightTable.weight((pieceValue-1) /* pawn = 1 */ *64 +(isWhite(fromSqBits)?0:6*64));
                if (plySide(ply.side(),fromSqBits)) {
                    sideScore+=pieceWeight;
                 }else{
                    moves += countMovesForSquare(ply, board, fromSqBits, fromSqId);
                    opponentScore+=pieceWeight;
                }

            }
        }
        if (moves==0){
            throw new IllegalStateException("WTF");
        }
        board.score(sideScore+opponentScore+parentBoard.score());
        board.moves((byte) moves);
    }


    @CodeReflection
    public static void createBoard(ChessData chessData, Ply ply, WeightTable weightTable, ChessData.Board parentBoard, int parentId, int move, byte fromSqId, byte toSqId) {

        byte fromSqBits = parentBoard.squareBits(fromSqId);
        if (isEmpty(fromSqBits)){
            throw new IllegalStateException("WTF");
        }
        if (!plySide(ply.side(),fromSqBits)) {
            throw new IllegalStateException("WTF");
        }
        fromSqBits = Compute.set(fromSqBits,MOVED);
        int id = parentBoard.firstChildIdx()+move;
        ChessData.Board newBoard = chessData.board(id);
        newBoard.id(id);
        newBoard.parent(parentId);
        newBoard.move((byte) move);
        newBoard.fromSqId(fromSqId);
        newBoard.toSqId(toSqId);
        for (int sqId = 0; sqId < 64; sqId++) {
            byte parentSqBits = parentBoard.squareBits(sqId);
            parentSqBits = Compute.reset(parentSqBits,CHECK);
            newBoard.squareBits(sqId, parentSqBits);
        }

        newBoard.squareBits(fromSqId, EMPTY_SQUARE);
        newBoard.squareBits(toSqId, fromSqBits);  // it is not at home now



      //  String s = BoardRenderer.text(newBoard);
       // new Terminal().boardText(newBoard).toString();

        // We take the opportunity to mark the parents squareBits for the toSqId id as 'in check'
        // meaning that one of the children in the next ply can move to toSqId
        // This is somewhat scary as the children of this parent are currently being processed
        // in parallel, so multiple pieces might be able to move to it
        // and all the piece moves are currently in flight ie there are multiple threads possibly racing on this square
        // it is possible that a runtime might not like this.
        // However, as the mutation is flipping a single bit we don't expect anyone to see any 'invalid' bit patterns
        parentBoard.squareBits(toSqId,(byte)(parentBoard.squareBits(toSqId)|CHECK)); // danger will robinson, we are racing here!

        countMovesAndScoreBoard(ply, weightTable, parentBoard, newBoard);
        // traceOutCreateBoard(ply,parentBoard,newBoard,fromSqId,toSqId);
    }


    @CodeReflection
    public static int createBoards(ChessData chessData, Ply ply, WeightTable weightTable,
                                   int moves, ChessData.Board parentBoard, int parentBoardId, byte fromSqBits, byte fromSqId) {
         int fromX = fromSqId % 8;
        int fromY = fromSqId / 8;
        if (isEmpty(fromSqBits)){
            throw new IllegalStateException("WTF");
        }
        if (!plySide(ply.side(), fromSqBits)){
            throw new IllegalStateException("WTF");
        }
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
            int side = ply.side();
            int forward = plyDir(side);                //WHITE=-1 BLACK = 1
            int count = 4-((fromSqBits&MOVED)>>>MOVED_SHIFT);    // 3 or 4
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
                        createBoard(chessData,ply, weightTable, parentBoard, parentBoardId, moves, fromSqId, toSqId);
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

        return moves;
    }

    @CodeReflection
    public static void createBoardsForParentBoardId(ChessData chessData, Ply ply, WeightTable weightTable, int parentBoardId) {
        ChessData.Board parentBoard = chessData.board(parentBoardId);
       int moves = 0;
        for (byte fromSqId = 0; fromSqId < 64; fromSqId++) {
            byte fromSqBits = parentBoard.squareBits(fromSqId);
            if (plySide(ply.side(), fromSqBits)){
                moves = createBoards(chessData, ply, weightTable, moves, parentBoard, parentBoardId, fromSqBits, fromSqId);
            }
        }
        if (moves==0) {
            System.out.println("NO moves\n" + BoardRenderer.unicode(parentBoard));
        }
     //   System.out.println(BoardRenderer.unicode(parentBoard));

    //    System.out.println("board ="+parentBoard.parent()+"/"+parentBoardId+" total moves"+moves);
    }

    @CodeReflection
    public static void createBoardsKernel(KernelContext kc, ChessData chessData, Ply ply, WeightTable weightTable) {
        if (kc.x < kc.maxX) {
            int parentBoardId = kc.x+ply.fromBoardId();
            createBoardsForParentBoardId(chessData, ply, weightTable, parentBoardId);
        }
    }

    @CodeReflection
    static public void createBoardsCompute(final ComputeContext cc, ChessData chessData, Ply ply, WeightTable weightTable) {
        cc.dispatchKernel(ply.size(), kc -> createBoardsKernel(kc, chessData, ply, weightTable));
    }
}
