package chess;

class ChessConstants {
    // DxDy move lists
    // We can encode up to 8 4 bit patterns comprised of pairs of bits such that  00=-1 01=0 10=1 11=2
    // In each case the encodings allow -1,0,1,2
    // We use this pattern to map the four bits to two dx,dy's
    //  int dxdy = (0b1111 & moveList) >>> (idx*4);
    //  int dy = (dxdy & 0b11) - 1;
    //  dxdy>>>=2;
    //  int dx = (dxdy & 0b11) - 1;
    //
    //                                              nw     n      ne     w      e      sw      s     se
    //                                            -1,-1,  0,-1,  1,-1  -1, 0   1, 0, -1, 1,  0, 1   1, 1
    public final static int compassMovesDxDy = 0b00_00__01_00__10_00__00_01__10_01__00_10__01_10__01_01;

    //      f = -1 for white +1 for black       0,2f   0,1f  -1,1f  1,1f
    //                                                <----------------->
    //                                          <----------------------->
    public final static int pawnMovesDxDy = 0b01_11__01_10__00_10__10_10;

    //                                           2, 1   1, 2   1, 2   2, 1   2, 1   1, 2   1, 2   2, 1
    //                                           -  -   -  -   +  -   +  -   -  +   -  +   +  +   +  +
    public final static int knightMovesDxDy = 0b11_01__10_11__00_11__11_10__11_01__10_11__00_11__11_10;


    public static final byte DIAGS = (byte) 0b1010_0101;
    public static final byte COLROWS = (byte) 0b0101_1010;
    public static final byte ALL_POINTS = (byte) 0b1111_1111;
    static public final byte EMPTY_SQUARE = (byte) 0b0000_0000;
    static public final byte PAWN_VALUE = (byte) 0b0000_0001;
    static public final byte KNIGHT_VALUE = (byte) 0b0000_0010;
    static public final byte BISHOP_VALUE = (byte) 0b0000_0011;
    static public final byte ROOK_VALUE = (byte) 0b0000_0100;
    static public final byte QUEEN_VALUE = (byte) 0b0000_0101;
    static public final byte KING_VALUE = (byte) 0b0000_0110;
    static public final byte WHITE_BIT = (byte) 0b0000_1000;
    static public final byte BLACK_PAWN = (byte) PAWN_VALUE;
    static public final byte BLACK_KNIGHT = (byte) KNIGHT_VALUE;
    static public final byte BLACK_BISHOP = (byte) BISHOP_VALUE;
    static public final byte BLACK_ROOK = (byte) ROOK_VALUE;
    static public final byte BLACK_QUEEN = (byte) QUEEN_VALUE;
    static public final byte BLACK_KING = (byte) KNIGHT_VALUE;
    static public final byte WHITE_PAWN = (byte) PAWN_VALUE | WHITE_BIT;
    static public final byte WHITE_KNIGHT = (byte) KNIGHT_VALUE | WHITE_BIT;
    static public final byte WHITE_BISHOP = (byte) BISHOP_VALUE | WHITE_BIT;
    static public final byte WHITE_ROOK = (byte) ROOK_VALUE | WHITE_BIT;
    static public final byte WHITE_QUEEN = (byte) QUEEN_VALUE | WHITE_BIT;
    static public final byte WHITE_KING = (byte) KNIGHT_VALUE | WHITE_BIT;
    static public final byte PIECE_MASK = (byte) 0b0000_0111;

    static public final byte OFF_BOARD_SQUARE = (byte) 0b1111_1111;
}
