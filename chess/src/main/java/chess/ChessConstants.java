package chess;

class ChessConstants {
    // DxDy move bit patterns
    // In 32 bits we can encode a max of 8 4-bit patterns each of which is comprised of pairs of bits
    // For non knight moves we generally only need dx,dy values of -1,0,1,2
    // Each pair of bits are interpreted as  00=-1 01=0 10=1 11=2 (essentially we take the decimal equiv and subtract 1)
    // After unmapping a pair we have (dx,dy)'s encodings of valid moves
    //
    // So for example for compassPoint based moves (nw 7,n 6,ne 5,w 4,e 3,sw 2,s 1,se 0) we use this mask
    //        nw     n      ne     w      e      sw      s     se
    //       -1,-1,  0,-1,  1,-1  -1, 0   1, 0, -1, 1,  0, 1   1, 1
    //     0b00_00__01_00__10_00__00_01__10_01__00_10__01_10__01_01;
    //
    // To get dx,dy for a given point we just shift, mask (AND) and subtract -1
    //
    // So for the dy of sw (point 2) we logical shift the pattern 2 nibbles (8 bits), mask the least sig 2 bits and subtract 1
    //     int dy = ((compassPointDxDy >>> (compassPoint*4)) & 0b11) - 1;
    // for dx we logical shift the pattern 2 nibbles + 2 (10 bits) mask the least sig 2 bits and subtract 1
    //     int dx = (dxdy & 0b11) - 1;
    //
    //                                             nw     n      ne     w      e      sw      s     se
    //                                           -1,-1,  0,-1,  1,-1  -1, 0   1, 0, -1, 1,  0, 1   1, 1
    public final static int CompassDxDyMap   = 0b00_00__01_00__10_00__00_01__10_01__00_10__01_10__01_01;


    // For pawn moves we use this mask           0b01_11__01_10__00_10__10_10;
    //      f = -1 for white +1 for black           0,2f   0,1f  -1,1f   1,1f
    //      for pawns in home position we use 4    <------------------------>
    //      for other pawns we only use these 3           <----------------->
    //
    // We precalculate f (1 or -1) based on the home position
    //      For white pawn
    //          f=-1
    //          On rank 6    moveCount = 4
    //          otherwise    moveCount = 3
    //      For black pawn
    //          f=1
    //          On rank 1    moveCount = 4
    //          otherwise    moveCount = 3
    //
    public final static int PawnDxDyMap = 0b01_11__01_10__00_10__10_10;

    // For knights we have 8 positions each pair of bits for permutations of +/- 1 and 2
    // Here we use high bit (of two) for the sign and (low bit)+1 for value (1 or 2)
    //
    //          |    |-1-2|     |+1-2|    |        |    |1011|     |0011|    |
    //          |-2-1|    |     |    |+2-1|        |1110|    |     |    |0110|
    //          |    |    |  x  |    |    |    ->  |    |    |  x  |    |    |
    //          |-2+1|    |     |    |+2+1|        |1100|    |     |    |0100|
    //          |    |-1+2|     |+1+2|    |        |    |1001|     |0001|    |
    //
    //
    //                                        -2,-1  -1,-2  +1,-2  +2,-1  +2,+1  +1,+2  -1,+2  -2,+1
    public final static int KnightDxDyMap = 0b11_10__10_11__00_11__01_10__01_00__00_01__10_01__11_00;

    public static final int DyORDxMask_SHIFT = 2;
    public static final int DxOrDyMASK = 0b11;
    public static final int DyDxMask_SHIFT = 4;
    public static final int DxDyMASK = 0b1111;
    public static final int WEIGHT_MASK_SHIFT = 4;
    public static final int WEIGHT_MASK = 0b1111;

    public static final byte DIAGS = (byte) 0b1010_0101;
    public static final byte COLROWS = (byte) 0b0101_1010;
    public static final byte ALL_POINTS = (byte) 0b1111_1111;
    static public final byte EMPTY_SQUARE = (byte) 0b0000_0000;
    static public final byte PAWN = (byte) 0b0000_0001;
    static public final byte KNIGHT = (byte) 0b0000_0010;
    static public final byte BISHOP = (byte) 0b0000_0011;
    static public final byte ROOK = (byte) 0b0000_0100;
    static public final byte QUEEN = (byte) 0b0000_0101;
    static public final byte KING = (byte) 0b0000_0110;
    static public final int WHITE_BIT_SHIFT = 3;
    static public final byte WHITE_BIT = (byte) (1 << WHITE_BIT_SHIFT);
    static public final byte PIECE_MASK = (byte) 0b0000_0111;
    static public final int NOT_AT_HOME_SHIFT = 4;
    static public final byte NOT_AT_HOME = (byte) (1<< NOT_AT_HOME_SHIFT);
    static public final int CHECK_SHIFT = 5;
    static public final byte CHECK = (byte) (1<<CHECK_SHIFT);

}
