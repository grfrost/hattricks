package chess;


import hat.Accelerator;
import hat.ComputeContext;
import hat.KernelContext;
import hat.backend.Backend;
import hat.buffer.Buffer;
import hat.ifacemapper.Schema;

import java.awt.Point;
import java.lang.invoke.MethodHandles;
import java.lang.runtime.CodeReflection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main {
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


    static final Point[] pawnHomeMoves = new Point[]{
            new Point(0, 1),
            new Point(0, 2)
    };

    static final Point[] pawnMoves = new Point[]{
            new Point(0, 1)
    };
    static final Point[] pawnCaptures = new Point[]{
            new Point(1, 1),
            new Point(-11, 1)
    };
    static final Point[] knightMoves = new Point[]{
            new Point(2, 1),
            new Point(-2, 1),
            new Point(2, -1),
            new Point(-2, -1)
    };
    static final Point[] bishopMoves = new Point[]{
            new Point(1, 1),
            new Point(2, 2),
            new Point(3, 3),
            new Point(4, 4),
            new Point(5, 5),
            new Point(6, 6),
            new Point(7, 7),
            new Point(-1, 1),
            new Point(-2, 2),
            new Point(-3, 3),
            new Point(-4, 4),
            new Point(-5, 5),
            new Point(-6, 6),
            new Point(-7, 7),
            new Point(-1, -1),
            new Point(-2, -2),
            new Point(-3, -3),
            new Point(-4, -4),
            new Point(-5, -5),
            new Point(-6, -6),
            new Point(-7, -7),
            new Point(1, -1),
            new Point(2, -2),
            new Point(3, -3),
            new Point(4, -4),
            new Point(5, -5),
            new Point(6, -6),
            new Point(7, -7)
    };
    static final Point[] rookMoves = new Point[]{
            new Point(0, 1),
            new Point(0, 2),
            new Point(0, 3),
            new Point(0, 4),
            new Point(0, 5),
            new Point(0, 6),
            new Point(0, 7),
            new Point(0, -1),
            new Point(0, -2),
            new Point(0, -3),
            new Point(0, -4),
            new Point(0, -5),
            new Point(0, -6),
            new Point(0, -7),
            new Point(-1, 0),
            new Point(-2, 0),
            new Point(-3, 0),
            new Point(-4, 0),
            new Point(-5, 0),
            new Point(-6, 0),
            new Point(-7, 0),
            new Point(1, 0),
            new Point(2, 0),
            new Point(3, 0),
            new Point(4, 0),
            new Point(5, 0),
            new Point(6, 0),
            new Point(7, 0)
    };

    static final Point[] queenMoves = new Point[]{
            new Point(1, 1),
            new Point(2, 2),
            new Point(3, 3),
            new Point(4, 4),
            new Point(5, 5),
            new Point(6, 6),
            new Point(7, 7),
            new Point(-1, 1),
            new Point(-2, 2),
            new Point(-3, 3),
            new Point(-4, 4),
            new Point(-5, 5),
            new Point(-6, 6),
            new Point(-7, 7),
            new Point(-1, -1),
            new Point(-2, -2),
            new Point(-3, -3),
            new Point(-4, -4),
            new Point(-5, -5),
            new Point(-6, -6),
            new Point(-7, -7),
            new Point(1, -1),
            new Point(2, -2),
            new Point(3, -3),
            new Point(4, -4),
            new Point(5, -5),
            new Point(6, -6),
            new Point(7, -7),
            new Point(0, 1),
            new Point(0, 2),
            new Point(0, 3),
            new Point(0, 4),
            new Point(0, 5),
            new Point(0, 6),
            new Point(0, 7),
            new Point(0, -1),
            new Point(0, -2),
            new Point(0, -3),
            new Point(0, -4),
            new Point(0, -5),
            new Point(0, -6),
            new Point(0, -7),
            new Point(-1, 0),
            new Point(-2, 0),
            new Point(-3, 0),
            new Point(-4, 0),
            new Point(-5, 0),
            new Point(-6, 0),
            new Point(-7, 0),
            new Point(1, 0),
            new Point(2, 0),
            new Point(3, 0),
            new Point(4, 0),
            new Point(5, 0),
            new Point(6, 0),
            new Point(7, 0)

    };
    static final Point[] kingMoves = new Point[]{
            new Point(-1, 1),
            new Point(0, 1),
            new Point(1, 1),
            new Point(-1, 0),
            new Point(1, 0),
            new Point(-1, 1),
            new Point(-1, -1),
            new Point(0, -1),
            new Point(-1, -1),
    };

    enum PIECE {
        EMPTY(0, ' ', ' '),
        PAWN(1, '\u2659', '\u265f', pawnHomeMoves, pawnMoves, pawnCaptures),
        KNIGHT(2, '\u2658', '\u265e', knightMoves),
        BISHOP(3, '\u2657', '\u265d', bishopMoves),
        ROOK(4, '\u2656', '\u265c', rookMoves),
        QUEEN(6, '\u2655', '\u265b', queenMoves),
        KING(7, '\u2654', '\u265a', kingMoves);

        static boolean isPawn(int square) {
            return PAWN.is(square);
        }

        static boolean isKnight(int square) {
            return KNIGHT.is(square);
        }

        static boolean isBishop(int square) {
            return BISHOP.is(square);
        }

        static boolean isRook(int square) {
            return ROOK.is(square);
        }

        static boolean isKing(int square) {
            return (KING.is(square));
        }

        static boolean isQueen(int square) {
            return (QUEEN.is(square));
        }

        public final char whiteUnicode;
        public final char blackUnicode;
        public final int value;
        public final Point dxyHomeMoves[];
        public final Point dxyMoves[];
        public final Point dxyCaptures[];

        PIECE(int value, char whiteUnicode, char blackUnicode, Point[] dxyHomeMoves, Point[] dxyMoves, Point[] dxyCaptures) {
            this.value = value;
            this.whiteUnicode = whiteUnicode;
            this.blackUnicode = blackUnicode;
            this.dxyHomeMoves = dxyHomeMoves;
            this.dxyMoves = dxyMoves;
            this.dxyCaptures = dxyCaptures;
        }

        PIECE(int value, char whiteUnicode, char blackUnicode, Point[] moves) {
            this(value, whiteUnicode, blackUnicode, moves, moves, moves);
        }

        PIECE(int value, char whiteUnicode, char blackUnicode) {
            this(value, whiteUnicode, blackUnicode, new Point[]{}, new Point[]{}, new Point[]{});
        }

        public boolean is(int square) {
            return ((square & 0xf) == value);
        }

        static public boolean isWhite(int square) {
            return ((square & Main.ChessData.Board.WHITE_BIT) == Main.ChessData.Board.WHITE_BIT);
        }

        public char unicode(int square) {
            return isWhite(square) ? whiteUnicode : blackUnicode;
        }

        static public PIECE of(int bits) {
            if (isKing(bits)) return PIECE.KING;
            else if (isQueen(bits)) return PIECE.QUEEN;
            else if (isRook(bits)) return PIECE.ROOK;
            else if (isBishop(bits)) return PIECE.BISHOP;
            else if (isKnight(bits)) return PIECE.KNIGHT;
            else if (isPawn(bits)) return PIECE.PAWN;
            else return EMPTY;
        }

        void asString() {

        }
    }


    public interface ChessData extends Buffer {
        interface Board extends Buffer.Struct {
            byte EMPTY = (byte) PIECE.EMPTY.value;
            byte PAWN = (byte) PIECE.PAWN.value;
            byte KNIGHT = (byte) PIECE.KNIGHT.value;
            byte BISHOP = (byte) PIECE.BISHOP.value;
            byte ROOK = (byte) PIECE.ROOK.value;
            byte QUEEN = (byte) PIECE.QUEEN.value;
            byte KING = (byte) PIECE.KING.value;
            byte HOME_BIT = (byte) 0x80;
            byte WHITE_BIT = (byte) 0x40;
            byte BLACK_BIT = (byte) 0x20;
            byte CHECK_BIT = (byte) 0x10;

            byte square(long idx);

            void square(long idx, byte square);

            short parent();

            void parent(short parent);

            short firstChild();

            void firstChild(short firstChild);

            short score();

            void score(short score);

            short spare();

            void spare(short spare);

            default Board init() {
                for (int row = 2; row < 6; row++) {
                    for (int col = 0; col < 8; col++) {
                        square(row * 8 + col, EMPTY);
                    }
                }
                for (int col = 0; col < 8; col++) {
                    square((long) (1 * 8) + col, (byte) (PAWN | BLACK_BIT | HOME_BIT));
                    square((long) (6 * 8) + col, (byte) (PAWN | WHITE_BIT | HOME_BIT));
                }

                square((long) (0 * 8) + 0, (byte) (ROOK | BLACK_BIT | HOME_BIT));
                square((long) (0 * 8) + 7, (byte) (ROOK | BLACK_BIT | HOME_BIT));
                square((long) (7 * 8) + 0, (byte) (ROOK | WHITE_BIT | HOME_BIT));
                square((long) (7 * 8) + 7, (byte) (ROOK | WHITE_BIT | HOME_BIT));
                square((long) (0 * 8) + 1, (byte) (KNIGHT | BLACK_BIT | HOME_BIT));
                square((long) (0 * 8) + 6, (byte) (KNIGHT | BLACK_BIT | HOME_BIT));
                square((long) (7 * 8) + 1, (byte) (KNIGHT | WHITE_BIT | HOME_BIT));
                square((long) (7 * 8) + 6, (byte) (KNIGHT | WHITE_BIT | HOME_BIT));
                square((long) (0 * 8) + 2, (byte) (BISHOP | BLACK_BIT | HOME_BIT));
                square((long) (0 * 8) + 5, (byte) (BISHOP | BLACK_BIT | HOME_BIT));
                square((long) (7 * 8) + 2, (byte) (BISHOP | WHITE_BIT | HOME_BIT));
                square((long) (7 * 8) + 5, (byte) (BISHOP | WHITE_BIT | HOME_BIT));
                square((long) (0 * 8) + 3, (byte) (QUEEN | BLACK_BIT | HOME_BIT));
                square((long) (7 * 8) + 3, (byte) (QUEEN | WHITE_BIT | HOME_BIT));
                square((long) (0 * 8) + 4, (byte) (KING | BLACK_BIT | HOME_BIT));
                square((long) (7 * 8) + 4, (byte) (KING | WHITE_BIT | HOME_BIT));
                return this;
            }

            default String asString() {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("   | 1  2  3  4  5  6  7  8 |").append('\n');
                for (int row = 0; row < 8; row++) {
                    char ch = (char) (0x61 + row);
                    stringBuilder.append(' ').append(ch).append(' ').append('|');
                    for (int col = 0; col < 8; col++) {
                        byte square = this.square(row * 8 + col);
                        PIECE piece = PIECE.of(square);
                        var background = ((col + row) % 2 == 0) ? TerminalColors.GREY : TerminalColors.DARKGREY;
                        var foreground = ((square & WHITE_BIT) == WHITE_BIT) ? TerminalColors.WHITE : TerminalColors.BLACK;
                        stringBuilder.append(background.bg(" "));
                        stringBuilder.append(TerminalColors.fgbg(foreground, background, piece.unicode(square)));
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

    static class ChessDataImpl implements ChessData {
        static class BoardImpl implements ChessData.Board {
            byte[] squares = new byte[64];
            short parent;
            short firstChild;
            short score;
            short spare;

            @Override
            public byte square(long idx) {
                return squares[(int) idx];
            }

            @Override
            public void square(long idx, byte square) {
                squares[(int) idx] = square;
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

    interface MoveList extends Buffer {
        interface XY extends Buffer.Struct {
            int x();

            void x(int x);

            int y();

            void y(int y);
        }

        int length();

        XY xy(long idx);

        Schema<MoveList> schema = Schema.of(MoveList.class, chessData -> chessData
                .arrayLen("length").array("xy", xy -> xy.fields("x", "y")));

        static MoveList create(Accelerator acc, int length, PIECE piece) {
            switch (piece) {
                case PIECE.PAWN: {
                    break;
                }
            }
            return schema.allocate(acc, length);
        }
    }

    public static class Compute {
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

    static boolean isEmpty(ChessData.Board board, int x, int y){
        byte squareBits = board.square(x + y* 8L);
        return (squareBits & ChessData.Board.EMPTY)==ChessData.Board.EMPTY;
    }
    static boolean isOppositeColor(ChessData.Board board, int x, int y, byte myBits){
        byte squareBits = board.square(x + y* 8L);
        return ((myBits&ChessData.Board.WHITE_BIT)==ChessData.Board.WHITE_BIT)||((myBits&ChessData.Board.BLACK_BIT)==ChessData.Board.BLACK_BIT);
    }
    static boolean isValid(Point delta,  int x, int y){
        return (x+ delta.x <8 && y+ delta.y <8 && x+delta.x >=0 && y+ delta.y >=0);
    }

    public static void main(String[] args) {
        boolean headless = Boolean.getBoolean("headless") || (args.length > 0 && args[0].equals("--headless"));
       // Accelerator accelerator = new Accelerator(MethodHandles.lookup(), Backend.FIRST);
        ChessData chessData = new ChessDataImpl(10001);
        //ChessData.create(accelerator, 10001);//,101,1001,10001
        //accelerator.compute(cc -> Compute.init(cc, chessData));
        ChessData.Board board = chessData.board(0).init();
        for (int i = 0; i < 64; i++) {
            byte squareBits = board.square(i);
            PIECE piece = PIECE.of(squareBits);
            int x = i%8;
            int y = i/8;
            Map<Integer, List<Point>>indexToPossibleMoves = new HashMap<>();
            switch (piece){
                case PIECE.PAWN: {
                    List<Point> possibleMoves = new ArrayList<>();
                    if ((squareBits & ChessData.Board.HOME_BIT)==ChessData.Board.HOME_BIT){
                        if ((squareBits & ChessData.Board.WHITE_BIT)==ChessData.Board.WHITE_BIT){
                         //   if ()
                            //byte toSquareBits = board.square();
                            possibleMoves.add(new Point(x, y));
                        }
                    }
                    break;
                }
                case PIECE.KNIGHT: {
                    break;
                }
                default:{

                }
            }

        }
        System.out.println(board.asString());
    }
}
