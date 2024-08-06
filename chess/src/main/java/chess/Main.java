package chess;


import hat.Accelerator;
import hat.ComputeContext;
import hat.KernelContext;
import hat.buffer.Buffer;
import hat.ifacemapper.Schema;

import java.lang.runtime.CodeReflection;


public class Main {

    static public final int compassDxDy = 0b00_00__01_00__10_00__00_01__10_01__00_10__01_10__01_01;
    //                                        -1 -1,  0 -1,  1 -1, -1, 0,  1, 0, -1  1,  0, 1,  1, 1
    //                                          nw     n      ne     w      e      sw     s      se
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
        public static boolean isWhite(byte squareBits) {
            return (squareBits & WHITE_BIT) == WHITE_BIT;
        }

        @CodeReflection
        public static boolean isBlack(byte squareBits) {
            return (squareBits & WHITE_BIT) != WHITE_BIT;
        }

        @CodeReflection
        public static boolean isOpponent(byte mySquareBits, byte opponentSquareBits) {
            return ((mySquareBits ^ opponentSquareBits) & WHITE_BIT) == WHITE_BIT;
        }

        @CodeReflection
        public static boolean isEmpty(byte squareBits) {
            return (squareBits & PIECE_MASK) == 0;
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
        static boolean isPiece(int squareBits) {
            return (squareBits & PIECE_MASK) != 0;
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

            static String algebraic(int x, int y) {
                return Character.toString(x + 65) + Integer.toString(y + 1);
            }

            static String note(int fromx, int fromy, String msg, int tox, int toy) {
                return algebraic(fromx, fromy) + msg + algebraic(tox, toy);
            }

            default void validMoves(byte squareBits, int fromx, int fromy) {
              //  PIECE piece = PIECE.of(squareBits);
                int pieceValue = squareBits&PIECE_MASK;
                if (pieceValue >= KNIGHT_VALUE) {
                    int compassBits = (pieceValue==BISHOP_VALUE?DIAGS:((pieceValue==ROOK_VALUE)?COLROWS:ALL_POINTS));
                    int blockedBits = compassBits ^ 0xff;
                    int count = pieceValue<KING_VALUE?7:1;
                    for (int v = 1; v <= count; v++) {
                        int compassBit = 0b1000_0000;

                        //                     nw    n   ne   w     e   sw    s   se
                        int neighbourMask = 0b1111_0000_0000_0000_0000_0000_0000_0000;
                        for (int neighbourDxDyIdx = 7; neighbourDxDyIdx > 0; neighbourDxDyIdx--) {
                            int nb = (neighbourMask & compassDxDy);
                            int nbshifted = nb >>> (neighbourDxDyIdx * 4);
                            int x = ((nbshifted >>> 2) & 0b11) - 1;
                            int y = (nbshifted & 0b11) - 1;
                            var compassPoints = new String[]{"nw","n", "ne", "w", "e", "sw", "s","se"};
                            System.out.println(compassPoints[neighbourDxDyIdx]+" x=" + x + ", y=" + y);

                            if (!Compute.isOnBoard(x, y)) {
                                //    System.out.println(fromx + "," + fromy + " board bounds blocks  " + x + "," + y);
                            } else if ((compassBit & blockedBits) == compassBit) {
                                //  System.out.println(fromx + "," + fromy + " pattern blocks  " + x + "," + y);
                            } else {
                                var xyBits = squareBits(y*8+x);
                                String xyBitsStr = Integer.toBinaryString(xyBits);
                                if (Compute.isEmpty(xyBits)) {
                                    System.out.println(note(fromx, fromy, " can move to ", x, y));
                                } else if (Compute.isOpponent(xyBits, squareBits)) {
                                    blockedBits |= compassBit;
                                    System.out.println(note(fromx, fromy, " can move/take to ", x, y));
                                } else {
                                    blockedBits |= compassBit;
                                }
                            }
                            compassBit >>>= 1;
                        }
                    }
                } else {

                }


            }

            default Board init() {
                for (int y = 2; y < 6; y++) {
                    for (int x = 0; x < 8; x++) {
                        squareBits((long)(y*8+x), EMPTY_SQUARE);
                    }
                }

                for (int x = 0; x < 8; x++) {
                    squareBits((long)(1*8+x), (byte) (PAWN_VALUE ));
                    squareBits((long)(6*8+x), (byte) (PAWN_VALUE | WHITE_BIT ));
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
        for (int i = 0; i < 64; i++) {
            int x = i % 8;
            int y = i / 8;
            byte squareBits = board.getSquareBits(x, y);
            if (!Compute.isEmpty(squareBits)) {
                if (Compute.isWhite(squareBits)) {
                    board.validMoves(squareBits, x, y);
                }
            }
        }
        System.out.println(new Terminal().board(board));
    }
}
