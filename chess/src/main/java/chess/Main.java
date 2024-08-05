package chess;


import hat.Accelerator;
import hat.ComputeContext;
import hat.KernelContext;
import hat.buffer.Buffer;
import hat.ifacemapper.Schema;

import java.lang.runtime.CodeReflection;



public class Main {

    static public final int[] neighbourDxDy = new int[]{
            1, -1,
            0, -1,
            1, -1
            - 1, 0,
            1, 0,
            -1, 1,
            0, 1,
            1, 1
    };


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

    static char squareBitsToUnicode(byte squareBits) {
        final char whiteChessUnicodeK=0x2654;
        final char blackChessUnicodeK=whiteChessUnicodeK+6;
        final char whiteUnicode[] = new char[]{
                '0', '\u2659', '\u2658',  '\u2657', '\u2656',  '\u2655','\u2654',  ' '
        };
        final char blackUnicode[] = new char[]{
                '0', '\u265f', '\u265e',  '\u265d', '\u265c',  '\u265b','\u265a',  ' '
        };
        byte value = (byte)(squareBits&PIECE_MASK);
        return  value==0?' ':Compute.isWhite(squareBits)?whiteUnicode[value]:blackUnicode[value];
    }
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

    public enum TerminalColors {
        NONE("0"),
        BLACK("5;0"),
        DARKGREEN("5;22"),
        DARKBLUE("5;27"),
        GREY("5;247"),
        DARKGREY("5;244"),
        RED("5;1"),
        GREEN("5;77"),
        YELLOW("5;185"),
        BLUE("5;31"),
        WHITE("5;251"),
        ORANGE("5;208"),
        PURPLE("5;133");
        private final String prefix = "\u001b[";
        private final String suffix = "m";
        private final static String FG = "38;";
        private final static String BG = "48;";

        public final String fseq;
        public final String bseq;

        private TerminalColors(String seq) {
            this.fseq = prefix + FG + seq + suffix;
            this.bseq = prefix + BG + seq + suffix;
        }

        public String fg(String string) {
            return fseq + string + NONE.fseq;
        }

        public String fg(char ch) {
            return fseq + ch + NONE.fseq;
        }

        public String bg(String string) {
            return bseq + string + NONE.bseq;
        }

        public String bg(char ch) {
            return bseq + ch + NONE.bseq;
        }

        public static String fgbg(TerminalColors fg, TerminalColors bg, String string) {
            return bg.bseq + fg.fseq + string + NONE.fseq + NONE.bseq;
        }

        public static String fgbg(TerminalColors fg, TerminalColors bg, char ch) {
            return bg.bseq + fg.fseq + ch + NONE.fseq + NONE.bseq;
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

            default void setSquareBits(int x, int y, byte squareBits) {
                squareBits(y * 8 + x, squareBits);
            }

            default byte getSquareBits(int x, int y) {
                return Compute.isOnBoard(x, y) ? squareBits(y * 8 + x) : OFF_BOARD_SQUARE;
            }

            default void validMoves(byte squareBits, int fromx, int fromy) {

                PIECE piece = PIECE.of(squareBits);
                String squareBitsStr = Integer.toBinaryString(squareBits);
                if (piece.count > 0) {
                    boolean[] blocked = new boolean[8];
                    for (int v = 1; v <= piece.count; v++) {
                        int compassBit = 0b1000_0000;
                        int compassBits = piece.compassBits&0xff;
                          for (int neighbourDxDyIdx = 0; neighbourDxDyIdx < 7; neighbourDxDyIdx++) {
                              String compassBitsStr = Integer.toBinaryString(compassBits);
                              String bitStr = Integer.toBinaryString(compassBit);

                              if (!blocked[neighbourDxDyIdx]) {
                                int x = fromx + v * neighbourDxDy[neighbourDxDyIdx * 2];
                                int y = fromy + v * neighbourDxDy[neighbourDxDyIdx * 2 + 1];

                                if ((compassBits & compassBit) == compassBit) {
                                    if (Compute.isOnBoard(x, y)) {
                                        var xyBits = getSquareBits(x, y);
                                        String xyBitsStr = Integer.toBinaryString(xyBits);

                                        if (Compute.isEmpty(xyBits)) {
                                            System.out.println(fromx + "," + fromy + " can move to " + x + "," + y);
                                        } else if (Compute.isOpponent(xyBits, squareBits)) {
                                            blocked[v] = true;
                                            System.out.println(fromx + "," + fromy + " can move/take on " + x + "," + y);
                                        } else {
                                            blocked[v] = true;
                                        }
                                    }
                                }
                            }

                            compassBit >>= 1;
                        }
                    }
                } else {

                }


            }

            default Board init() {
                for (int y = 2; y < 6; y++) {
                    for (int x = 0; x < 8; x++) {
                        setSquareBits(x, y, EMPTY_SQUARE);
                    }
                }

                for (int x = 0; x < 8; x++) {
                    setSquareBits(x, 1, (byte) (PAWN_VALUE | BLACK_BIT | HOME_BIT));
                    setSquareBits(x, 6, (byte) (PAWN_VALUE | WHITE_BIT | HOME_BIT));
                }

                setSquareBits(0, 0, (byte) (ROOK_VALUE | BLACK_BIT | HOME_BIT));
                setSquareBits(7, 0, (byte) (ROOK_VALUE | BLACK_BIT | HOME_BIT));
                setSquareBits(0, 7, (byte) (ROOK_VALUE | WHITE_BIT | HOME_BIT));
                setSquareBits(7, 7, (byte) (ROOK_VALUE | WHITE_BIT | HOME_BIT));

                setSquareBits(1, 0, (byte) (KNIGHT_VALUE | BLACK_BIT | HOME_BIT));
                setSquareBits(6, 0, (byte) (KNIGHT_VALUE | BLACK_BIT | HOME_BIT));
                setSquareBits(1, 7, (byte) (KNIGHT_VALUE | WHITE_BIT | HOME_BIT));
                setSquareBits(6, 7, (byte) (KNIGHT_VALUE | WHITE_BIT | HOME_BIT));

                setSquareBits(2, 0, (byte) (BISHOP_VALUE | BLACK_BIT | HOME_BIT));
                setSquareBits(5, 0, (byte) (BISHOP_VALUE | BLACK_BIT | HOME_BIT));
                setSquareBits(2, 7, (byte) (BISHOP_VALUE | WHITE_BIT | HOME_BIT));
                setSquareBits(5, 7, (byte) (BISHOP_VALUE | WHITE_BIT | HOME_BIT));

                setSquareBits(3, 0, (byte) (QUEEN_VALUE | BLACK_BIT | HOME_BIT));
                setSquareBits(3, 7, (byte) (QUEEN_VALUE | WHITE_BIT | HOME_BIT));

                setSquareBits(4, 0, (byte) (KING_VALUE | BLACK_BIT | HOME_BIT));
                setSquareBits(4, 7, (byte) (KING_VALUE | WHITE_BIT | HOME_BIT));
                return this;
            }

            default String asString() {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("   | 1  2  3  4  5  6  7  8 |").append('\n');
                for (int y = 0; y < 8; y++) {
                    char ch = (char) (0x61 + y);
                    stringBuilder.append(' ').append(ch).append(' ').append('|');
                    for (int x = 0; x < 8; x++) {
                        byte squareBits = this.getSquareBits(x, y);
                        var background = ((x + y) % 2 == 0) ? TerminalColors.GREY : TerminalColors.DARKGREY;
                        stringBuilder.append(background.bg(" "));
                        char unicode = squareBitsToUnicode(squareBits);
                          stringBuilder.append(TerminalColors.fgbg(Compute.isWhite(squareBits) ? TerminalColors.WHITE : TerminalColors.BLACK, background, unicode));
                        stringBuilder.append(background.bg(" "));
                    }
                    stringBuilder.append('|').append('\n');
                }
                stringBuilder.append("   | 1  2  3  4  5  6  7  8 |").append('\n');
                return stringBuilder.toString();
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
        OFFBOARD(OFF_BOARD_SQUARE, (byte) 0, false,0);


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
        System.out.println(board.asString());
    }
}
