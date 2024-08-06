package chess;


import hat.Accelerator;
import hat.ComputeContext;
import hat.KernelContext;
import hat.buffer.Buffer;
import hat.ifacemapper.Schema;

import java.lang.runtime.CodeReflection;


public class Main {

    static public final int neighbourDxDy = 0b10_10__00_10__01_10__10_00__01_00__10_01__00_01__01_01;
    //                                        -1 -1,  0 -1,  1 -1, -1, 0,  1, 0, -1  1,  0, 1,  1, 1

    static public final byte EMPTY_SQUARE = (byte) 0x00;
    static public final byte PAWN_VALUE = (byte) 0x01;
    static public final byte KNIGHT_VALUE = (byte) 0x02;
    static public final byte BISHOP_VALUE = (byte) 0x03;
    static public final byte ROOK_VALUE = (byte) 0x04;
    static public final byte QUEEN_VALUE = (byte) 0x05;
    static public final byte KING_VALUE = (byte) 0x06;
    static public final byte PIECE_MASK = (byte) 0b0000_0111;
    static public final byte HOME_BIT = (byte) 0b0000_1000;
    static public final byte BLACK_BIT = (byte) 0b0001_0000;
    static public final byte WHITE_BIT = (byte) 0b0010_0000;
    static public final byte COLOR_MASK = WHITE_BIT | BLACK_BIT;
    static public final byte OFF_BOARD_SQUARE = (byte) 0b1111_1111;
    static public final short MOVE_BIT = (short) 0b0000_0001_0000_0000;
    static public final short CAPTURE_BIT = (short) 0b0000_0010_0000_0000;

    //static public final long DIAG_BITS =          0b10011001_010011010_001011100_000111;


    public static class Compute {
        @CodeReflection
        public static boolean isWhite(byte squareBits) {
            return (squareBits & COLOR_MASK) == WHITE_BIT;
        }

        @CodeReflection
        public static boolean isBlack(byte squareBits) {
            return (squareBits & COLOR_MASK) == BLACK_BIT;
        }

        @CodeReflection
        public static boolean isOpponent(byte mySquareBits, byte opponentSquareBits) {
            return ((mySquareBits | opponentSquareBits) & COLOR_MASK) == COLOR_MASK;
        }

        @CodeReflection
        public static boolean isMyPiece(byte mySquareBits, byte opponentSquareBits) {
            return (mySquareBits & opponentSquareBits & COLOR_MASK) == mySquareBits;
        }

        @CodeReflection
        public static boolean isEmpty(byte squareBits) {
            return (squareBits & PIECE_MASK) == 0;
        }

        @CodeReflection
        public static boolean isHome(byte squareBits) {
            return (squareBits & HOME_BIT) == HOME_BIT;
        }

        @CodeReflection
        static boolean isEmpty(ChessData.Board board, int x, int y) {
            return isEmpty(board.squareBits(x + y * 8L));
        }

        @CodeReflection
        static boolean isOpponent(ChessData.Board board, int x, int y, byte myBits) {
            byte opponentSquareBits = board.squareBits(x + y * 8L);
            return isOpponent(myBits, opponentSquareBits);
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
        static boolean isEmpty(int squareBits) {
            return (squareBits & PIECE_MASK) == EMPTY_SQUARE;
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
                PIECE piece = PIECE.of(squareBits);
                if (piece.count > 0) {
                    int compassBits = piece.compassBits & 0xff;
                    int blockedBits = compassBits ^ 0xff;
                    for (int v = 1; v <= piece.count; v++) {
                        int compassBit = 0b1000_0000;

                        //                     nw    n   ne   w     e   sw    s   se
                        int neighbourMask = 0b1111_0000_0000_0000_0000_0000_0000_0000;
                        for (int neighbourDxDyIdx = 7; neighbourDxDyIdx > 0; neighbourDxDyIdx--) {
                            int nb = (neighbourMask & neighbourDxDy);
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
                    squareBits((long)(1*8+x), (byte) (PAWN_VALUE | BLACK_BIT | HOME_BIT));
                    squareBits((long)(6*8+x), (byte) (PAWN_VALUE | WHITE_BIT | HOME_BIT));
                }

                squareBits((long)(0*8+0), (byte) (ROOK_VALUE | BLACK_BIT | HOME_BIT));
                squareBits((long)(0*8+7), (byte) (ROOK_VALUE | BLACK_BIT | HOME_BIT));
                squareBits((long)(7*8+0), (byte) (ROOK_VALUE | WHITE_BIT | HOME_BIT));
                squareBits((long)(7*8+7), (byte) (ROOK_VALUE | WHITE_BIT | HOME_BIT));

                squareBits((long)(0*8+1), (byte) (KNIGHT_VALUE | BLACK_BIT | HOME_BIT));
                squareBits((long)(0*8+6), (byte) (KNIGHT_VALUE | BLACK_BIT | HOME_BIT));
                squareBits((long)(7*8+1), (byte) (KNIGHT_VALUE | WHITE_BIT | HOME_BIT));
                squareBits((long)(7*8+6), (byte) (KNIGHT_VALUE | WHITE_BIT | HOME_BIT));

                squareBits((long)(0*8+2), (byte) (BISHOP_VALUE | BLACK_BIT | HOME_BIT));
                squareBits((long)(0*8+5), (byte) (BISHOP_VALUE | BLACK_BIT | HOME_BIT));
                squareBits((long)(7*8+2), (byte) (BISHOP_VALUE | WHITE_BIT | HOME_BIT));
                squareBits((long)(7*8+5), (byte) (BISHOP_VALUE | WHITE_BIT | HOME_BIT));

                squareBits((long)(0*8+3), (byte) (QUEEN_VALUE | BLACK_BIT | HOME_BIT));
                squareBits((long)(7*8+3), (byte) (QUEEN_VALUE | WHITE_BIT | HOME_BIT));

                squareBits((long)(0*8+4), (byte) (KING_VALUE | BLACK_BIT | HOME_BIT));
                squareBits((long)(7*8+4), (byte) (KING_VALUE | WHITE_BIT | HOME_BIT));
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

    public static final byte DIAGS = (byte) 0b1010_0101;
    public static final byte COLROWS = (byte) 0b0101_1010;
    public static final byte DIAGS_OR_COLROWS = (byte) (DIAGS | COLROWS);

    enum PIECE {
        EMPTY(EMPTY_SQUARE, (byte) 0, false, 0),
        PAWN(PAWN_VALUE, (byte) 0, false, 0),
        KNIGHT(KNIGHT_VALUE, (byte) 0, true, 0),
        BISHOP(BISHOP_VALUE, DIAGS, false, 7),
        ROOK(ROOK_VALUE, COLROWS, true, 7),
        QUEEN(QUEEN_VALUE, DIAGS_OR_COLROWS, false, 7),
        KING(KING_VALUE, DIAGS_OR_COLROWS, false, 1),
        OFFBOARD(OFF_BOARD_SQUARE, (byte) 0, false, 0);


        public final int value;
        public final byte compassBits;
        public final boolean knight;

        public final int count;

        PIECE(int value, byte compassBits, boolean knight, int count) {
            this.value = value;
            this.compassBits = compassBits;
            this.knight = knight;
            this.count = count;
        }

        static public PIECE of(int squareBits) {
            if (Compute.isKing(squareBits)) return KING;
            else if (Compute.isQueen(squareBits)) return QUEEN;
            else if (Compute.isRook(squareBits)) return ROOK;
            else if (Compute.isBishop(squareBits)) return BISHOP;
            else if (Compute.isKnight(squareBits)) return KNIGHT;
            else if (Compute.isPawn(squareBits)) return PAWN;
            else if (Compute.isOffBoard(squareBits)) return PIECE.OFFBOARD;
            else return EMPTY;
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
                if (Compute.isMyPiece(WHITE_BIT, squareBits)) {
                    board.validMoves(squareBits, x, y);
                }
            }
        }
        System.out.println(new Terminal().board(board));
    }
}
