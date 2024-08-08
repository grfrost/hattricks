package chess;


import hat.Accelerator;
import hat.ComputeContext;
import hat.KernelContext;
import hat.buffer.Buffer;
import hat.ifacemapper.Schema;

import java.lang.runtime.CodeReflection;

import static chess.Terminal.algebraic;
import static chess.Terminal.chessKingUnicode;
import static chess.Terminal.piece;


public class Main {

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
    public final static int compassMovesDxDy =  0b00_00__01_00__10_00__00_01__10_01__00_10__01_10__01_01;

    //      f = -1 for white +1 for black       0,2f   0,1f  -1,1f  1,1f
    //                                                <----------------->
    //                                          <----------------------->
    public final static int pawnMovesDxDy =  0b01_11__01_10__00_10__10_10;

    //                                           2, 1   1, 2   1, 2   2, 1   2, 1   1, 2   1, 2   2, 1
    //                                           -  -   -  -   +  -   +  -   -  +   -  +   +  +   +  +
    public final static int knightMovesDxDy = 0b11_01__10_11__00_11__11_10__11_01__10_11__00_11__11_10;


    public static final byte DIAGS = (byte) 0b1010_0101;
    public static final byte COLROWS = (byte) 0b0101_1010;
    public static final byte ALL_POINTS = (byte) 0b1111_1111;
    static public final byte EMPTY_SQUARE = (byte) 0b0000_0000;
    static public final byte PAWN_VALUE =   (byte) 0b0000_0001;
    static public final byte KNIGHT_VALUE = (byte) 0b0000_0010;
    static public final byte BISHOP_VALUE = (byte) 0b0000_0011;
    static public final byte ROOK_VALUE =   (byte) 0b0000_0100;
    static public final byte QUEEN_VALUE =  (byte) 0b0000_0101;
    static public final byte KING_VALUE =   (byte) 0b0000_0110;
    static public final byte WHITE_BIT =    (byte) 0b0000_1000;
    static public final byte PIECE_MASK =   (byte) 0b0000_0111;

    static public final byte OFF_BOARD_SQUARE = (byte) 0b1111_1111;
    public static class Compute {

        @CodeReflection
        public static boolean isEmpty(byte squareBits) {
            return (squareBits & PIECE_MASK) == 0;
        }
        @CodeReflection
        static boolean isPiece(int squareBits) {
            return (squareBits & PIECE_MASK) != 0;
        }
        @CodeReflection
        public static boolean isWhite(byte squareBits) {
            return isPiece(squareBits) && ((squareBits & WHITE_BIT) == WHITE_BIT);
        }

        @CodeReflection
        public static boolean isBlack(byte squareBits) {
            return isPiece(squareBits) && ((squareBits & WHITE_BIT) != WHITE_BIT);
        }

        @CodeReflection
        public static boolean isOpponent(byte mySquareBits, byte opponentSquareBits) {
            if (isPiece(mySquareBits)) {
                int myColor = (mySquareBits & WHITE_BIT);
                int opponentColor = (opponentSquareBits & WHITE_BIT);
                return myColor != opponentColor;
            }
            return false;
        }
        @CodeReflection
        public static boolean isMyPiece(byte squareBits, byte myColorBit) {
            if (isPiece(squareBits)) {
                int myColor = (squareBits & WHITE_BIT);
                int opponentColor = (myColorBit & WHITE_BIT);
                return myColor == opponentColor;
            }
            return false;
        }


        @CodeReflection
        static boolean isOnBoard(int x, int y) {
            return (x < 8 && y < 8 && x >= 0 && y >= 0);
        }

        @CodeReflection
        static boolean isPawn(int squareBits) {
            return (squareBits & PIECE_MASK) == PAWN_VALUE;
        }

        @CodeReflection
        static boolean isKnight(int squareBits) {
            return (squareBits & PIECE_MASK) == KNIGHT_VALUE;
        }

        @CodeReflection
        static boolean isBishop(int squareBits) {
            return (squareBits & PIECE_MASK) == BISHOP_VALUE;
        }

        @CodeReflection
        static boolean isRook(int squareBits) {
            return (squareBits & PIECE_MASK) == ROOK_VALUE;
        }

        @CodeReflection
        static boolean isKing(int squareBits) {
            return (squareBits & PIECE_MASK) == KING_VALUE;
        }


        @CodeReflection
        static boolean isQueen(int squareBits) {
            return (squareBits & PIECE_MASK) == QUEEN_VALUE;
        }


        @CodeReflection
        static boolean isOffBoard(int squareBits) {
            return squareBits == OFF_BOARD_SQUARE;
        }
        @CodeReflection
        static int compassBits(int squareBits) {
            int pv = (squareBits & PIECE_MASK);
            return pv>KNIGHT_VALUE?(pv == BISHOP_VALUE ? DIAGS : ((pv == ROOK_VALUE) ? COLROWS : ALL_POINTS)):0;
        }
        @CodeReflection
        static int compassCount(int squareBits) {
            int pv = (squareBits & PIECE_MASK);
            return (pv == KING_VALUE) ? 1 : pv > KNIGHT_VALUE ? 7 : 0;
        }
        @CodeReflection
        public static void initTree(KernelContext kc, ChessData chessData) {
            if (kc.x < kc.maxX) {
                ChessData.Board board = chessData.board(kc.x);
                if (kc.x == 0) {
                    board.parent((short) -1);
                    board.firstChild((short) 10);
                } else if (kc.x < 10) {
                    board.parent((short) 0);
                    board.firstChild((short) ((kc.x - 1) * 10));  //1->10 2->20 etc
                }
            }
        }

        @CodeReflection
        static public void init(final ComputeContext cc, ChessData chessData) {
            cc.dispatchKernel(chessData.length(), kc -> Compute.initTree(kc, chessData));
        }

       // public static int dxdy(int movesDxDy,int moveIndex) {
        //   return ;
       // }
    }

    public interface ChessData extends Buffer {
        interface Board extends Buffer.Struct {


            byte squareBits(long idx);

            void squareBits(long idx, byte squareBits);

            short parent();

            void parent(short parent);

            short firstChild();

            void firstChild(short firstChild);

            short score();

            void score(short score);

            short spare();

            void spare(short spare);

            default byte getSquareBits(int x, int y) {
                return Compute.isOnBoard(x, y) ? squareBits(y * 8 + x) : OFF_BOARD_SQUARE;
            }




            static String describe(byte squareBits){
                StringBuilder sb = new StringBuilder();
                if (Compute.isEmpty(squareBits)){
                    sb.append("EMPTY");
                }else {
                    int pieceValue = squareBits & PIECE_MASK;
                    if (Compute.isWhite(squareBits)){
                        sb.append("WHITE ");
                    }else{
                        sb.append("BLACK ");
                    }
                    switch (pieceValue){
                        case EMPTY_SQUARE:
                            sb.append("EMPTY");
                            break;
                        case PAWN_VALUE:
                            sb.append("PAWN");
                            break;
                        case ROOK_VALUE:
                            sb.append("ROOK");
                            break;
                        case KNIGHT_VALUE:
                            sb.append("KNIGHT");
                            break;
                        case BISHOP_VALUE:
                            sb.append("BISHOP");
                            break;
                        case QUEEN_VALUE:
                            sb.append("QUEEN");
                            break;
                        case KING_VALUE:
                            sb.append("KING");
                            break;
                    }
                    switch (Compute.compassBits(squareBits)){
                        case 0:break;
                        case ALL_POINTS:sb.append(" DIAGS and COLROWS ").append(Compute.compassCount(squareBits));break;
                        case DIAGS:sb.append(" DIAGS ").append(Compute.compassCount(squareBits));break;
                        case COLROWS:sb.append(" COLROWS ").append(Compute.compassCount(squareBits));break;
                    }

                }
                return sb.toString();
            }

            default int validMoves(byte fromBits, int fromx, int fromy, int moves,short[] movesArr) {
                int pieceValue = fromBits&PIECE_MASK;

                if (pieceValue >= KNIGHT_VALUE) {
                    String pieceDescription = describe(fromBits);
                    System.out.println(pieceDescription);
                    int compassBits = Compute.compassBits(fromBits);
                    // compass bits has 1/0 for each eligible move relative to fromx,fromy
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
                    int slideCount = Compute.compassCount(fromBits); // 1 for K 7 for Q,R,B
                    for (int slide = 1; slide <= slideCount; slide++) { //1 or 1,2,3,4,5,6,7
                        for (int moveIdx = 7; moveIdx > 0; moveIdx--) {
                            int moveBit = (1<<moveIdx); // so 0b10000000 -> 0b01000000 -> ... 0b00000001
                            // check if the bit is set for this compassPoint in compassBits
                            // if so we can move /take in this dir
                            if ((compassBits&moveBit)==moveBit) {
                                // Now let's determine what a move in this dir looks like
                                int dxdy =0b1111&(compassMovesDxDy>>>(moveIdx * 4));
                                int dy = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                                dxdy>>>=2;
                                int dx = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2

                                int tox = fromx + slide* dx;
                                int toy = fromy + slide* dy;

                              //  final String[]  compassPoints = new String[]{"nw", "n", "ne", "w", "e", "sw", "s", "se"};
                               // System.out.println(compassPoints[moveIdx] + " x=" + toX + ", y=" + toY);
                                if (Compute.isOnBoard(tox, toy)) {
                                    var toBits = squareBits(toy * 8 + tox);
                                    if (Compute.isEmpty(toBits)) {
                                        int move = (fromx<<12|fromy<<8|tox<<4|toy);
                                        movesArr[moves++]=(short)move;
                                        System.out.println(piece(fromBits)+"@"+ algebraic(fromx, fromy)+" -> @"+algebraic(tox, toy));
                                        {
                                            int fromX = (move >>> 12) & 0xf;
                                            int fromY = (move >>> 8) & 0xf;
                                            int toX = (move >>> 4) & 0xf;
                                            int toY = (move >>> 0) & 0xf;
                                            System.out.println(piece(fromBits) + "@" + algebraic(fromX, fromY) + " -> @" + algebraic(toX, toY));
                                        }
                                    } else{
                                        if (Compute.isOpponent(toBits, fromBits)) {
                                            int move = (fromx << 12 | fromy << 8 | tox << 4 | toy);
                                            movesArr[moves++] = (short) move;
                                            System.out.println(piece(fromBits) + "@" + algebraic(fromx, fromy) + " x " + piece(toBits) + " @" + algebraic(tox, toy));
                                            {
                                                int fromX = (move >>> 12) & 0xf;
                                                int fromY = (move >>> 8) & 0xf;
                                                int toX = (move >>> 4) & 0xf;
                                                int toY = (move >>> 0) & 0xf;
                                                System.out.println(piece(fromBits) + "@" + algebraic(fromX, fromY) + " x " + piece(toBits) + " @" + algebraic(toX, toY));
                                            }
                                        }
                                        compassBits ^= (1<<moveIdx); //unset this compass index
                                    }
                                }else{
                                  //  System.out.println(note(fromx, fromy, " offboard can't move to ", toX, toY));
                                    compassBits ^= (1<<moveIdx); // unset this compass index
                                }
                            }
                        }
                    }
                } else if (pieceValue==PAWN_VALUE){
                    int forward = Compute.isWhite(fromBits)?-1:1;
                    int home = Compute.isWhite(fromBits)?6:1;


                    int count = (fromy==home)?4:3;  // four moves if home else three
                    for (int moveIdx=0; moveIdx<count; moveIdx++) {
                        int dxdy =0b1111&(pawnMovesDxDy>>>(moveIdx * 4));
                        int dy = (dxdy & 0b11) - 1; // 00->-1, 01->0, 10->1, 11->2
                        dxdy>>>=2;
                        int dx = (dxdy & 0b11) - 1; // 00->-1, 01->0, 10->1, 11->2

                        int tox = fromx + dx;
                        int toy = fromy + (forward* dy);

                        if (Compute.isOnBoard(tox, toy)) {
                            var toBits = squareBits(toy * 8 + tox);
                            if (moveIdx>1){
                                if (Compute.isEmpty(toBits)) {
                                    int move = (fromx<<12|fromy<<8|tox<<4|toy);
                                    movesArr[moves++]=(short)move;

                                    System.out.println(piece(fromBits)+"@"+algebraic(fromx, fromy)+ " -> @"+algebraic(toy, tox));
                                    {
                                        int fromX = (move >>> 12) & 0xf;

                                        int fromY = (move >>> 8) & 0xf;
                                        int toX = (move >>> 4) & 0xf;
                                        int toY = (move >>> 0) & 0xf;
                                        System.out.println(piece(fromBits) + "@" + algebraic(fromX, fromY) + " -> @" + algebraic(toX, toY));
                                    }
                                }else {
                                    //System.out.println(note(fromx, fromy, " can't move (blocked)to ", x, y));
                                }
                            }else{
                                if (Compute.isEmpty(toBits)) {
                                    //System.out.println(note(fromx, fromy, " can't take (nothing)  on ", x, y));
                                }else{
                                    if (Compute.isOpponent(toBits, fromBits)) {
                                        int move = (fromx<<12|fromy<<8|tox<<4|toy);
                                        movesArr[moves++]=(short)move;
                                        System.out.println(piece(fromBits)+" @"+algebraic(fromx, fromy)+ " x "+piece(toBits)+"  @"+algebraic(toy, tox));
                                        {
                                            int fromX = (move >>> 12) & 0xf;
                                            int fromY = (move >>> 8) & 0xf;
                                            int toX = (move >>> 4) & 0xf;
                                            int toY = (move >>> 0) & 0xf;
                                            System.out.println(piece(fromBits) + "@" + algebraic(fromX, fromY) + " x " + piece(toBits) + "  @" + algebraic(toX, toY));
                                        }
                                    } else {
                                      //  System.out.println(note(fromx, fromy, " can't take (own player)  on ", x, y));
                                    }

                                }
                            }
                        }else{
                           // System.out.println(note(fromx, fromy, " offboard can't move to ", x, y));
                        }
                    }

                }else if(pieceValue==KNIGHT_VALUE){

                    // 00=-1 01=0 10=1 11=2

                }
                return moves;

            }

            default Board init() {
                for (int y = 2; y < 6; y++) {
                    for (int x = 0; x < 8; x++) {
                        squareBits((long)(y*8+x), EMPTY_SQUARE);
                    }
                }

                for (int x = 0; x < 8; x++) {
                    squareBits((long)(1*8+x), (byte) (PAWN_VALUE ));
                  //  squareBits((long)(6*8+x), (byte) (PAWN_VALUE | WHITE_BIT ));
                }

                squareBits((long)(0*8+0), (byte) (ROOK_VALUE));
                squareBits((long)(0*8+7), (byte) (ROOK_VALUE));
                squareBits((long)(7*8+0), (byte) (ROOK_VALUE | WHITE_BIT ));
                squareBits((long)(7*8+7), (byte) (ROOK_VALUE | WHITE_BIT ));

                squareBits((long)(0*8+1), (byte) (KNIGHT_VALUE ));
                squareBits((long)(0*8+6), (byte) (KNIGHT_VALUE ));
                squareBits((long)(7*8+1), (byte) (KNIGHT_VALUE | WHITE_BIT ));
                squareBits((long)(7*8+6), (byte) (KNIGHT_VALUE | WHITE_BIT ));

                squareBits((long)(0*8+2), (byte) (BISHOP_VALUE ));
                squareBits((long)(0*8+5), (byte) (BISHOP_VALUE ));
                squareBits((long)(7*8+2), (byte) (BISHOP_VALUE | WHITE_BIT ));
                squareBits((long)(7*8+5), (byte) (BISHOP_VALUE | WHITE_BIT ));

                squareBits((long)(0*8+3), (byte) (QUEEN_VALUE ));
                squareBits((long)(7*8+3), (byte) (QUEEN_VALUE | WHITE_BIT ));

                squareBits((long)(0*8+4), (byte) (KING_VALUE ));
                squareBits((long)(7*8+4), (byte) (KING_VALUE | WHITE_BIT ));
                return this;
            }


        }


        int length();

        ChessData.Board board(long idx);

        Schema<ChessData> schema = Schema.of(ChessData.class, chessData -> chessData
                .arrayLen("length").array("board", square -> square
                        .array("square", 64).fields("parent", "firstChild", "score", "spare")
                )
        );

        static ChessData create(Accelerator acc, int length) {
            return schema.allocate(acc, length);
        }
    }

    static class ChessDataImpl implements ChessData {
        static class BoardImpl implements ChessData.Board {
            byte[] squareBitArr = new byte[64];
            short parent;
            short firstChild;
            short score;
            short spare;

            @Override
            public byte squareBits(long idx) {
                return squareBitArr[(int) idx];
            }

            @Override
            public void squareBits(long idx, byte squareBits) {
                this.squareBitArr[(int) idx] = squareBits;
            }

            @Override
            public short parent() {
                return parent;
            }

            @Override
            public void parent(short parent) {
                this.parent = parent;
            }

            @Override
            public short firstChild() {
                return firstChild;
            }

            @Override
            public void firstChild(short firstChild) {
                this.firstChild = firstChild;
            }

            @Override
            public short score() {
                return this.score;
            }

            @Override
            public void score(short score) {
                this.score = score;
            }

            @Override
            public short spare() {
                return spare;
            }

            @Override
            public void spare(short spare) {
                this.spare = spare;
            }

        }

        final Board[] boards;

        @Override
        public int length() {
            return boards.length;
        }

        @Override
        public Board board(long idx) {
            return boards[(int) idx];
        }

        ChessDataImpl(int length) {
            this.boards = new Board[length];
            for (int i = 0; i < length; i++) {
                this.boards[i] = new BoardImpl();
            }
        }
    }

    public static void main(String[] args) {
        boolean headless = Boolean.getBoolean("headless") || (args.length > 0 && args[0].equals("--headless"));
        // Accelerator accelerator = new Accelerator(MethodHandles.lookup(), Backend.FIRST);
        ChessData chessData = new ChessDataImpl(10001);
        //ChessData.create(accelerator, 10001);//,101,1001,10001
        //accelerator.compute(cc -> Compute.init(cc, chessData));
        ChessData.Board board = chessData.board(0).init();
        short[] movesArr = new short[64];
        int moves=0;
        byte side = WHITE_BIT;
        for (int i = 0; i < 64; i++) {
            int x = i % 8;
            int y = i / 8;
            byte squareBits = board.getSquareBits(x, y);
            if (Compute.isMyPiece(squareBits, side)) {
                moves= board.validMoves(squareBits, x, y, moves, movesArr);
            }
        }
        System.out.println(new Terminal().board(board));
        for (int moveIdx = 0; moveIdx < moves; moveIdx++) {
            int  move  = movesArr[moveIdx];
            int fromx = (move>>>12)&0xf;
            int fromy = (move>>>8)&0xf;
            int tox = (move>>>4)&0xf;
            int toy = (move>>>0)&0xf;
            byte fromBits = board.getSquareBits(fromx, fromy);
            byte toBits = board.getSquareBits(tox, toy);
            if (Compute.isPiece(toBits)) {
                System.out.println(piece(fromBits) + "@" + algebraic(fromx, fromy) + " x " + piece(toBits) + " @" + algebraic(tox, toy));
            }else{
                System.out.println(piece(fromBits) + "@" + algebraic(fromx, fromy) + " -> @" + algebraic(tox, toy));
            }
        }
    }
}
