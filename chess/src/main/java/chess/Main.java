package chess;


import hat.Accelerator;
import hat.ComputeContext;
import hat.KernelContext;
import hat.buffer.Buffer;
import hat.ifacemapper.Schema;

import java.awt.Point;
import java.lang.runtime.CodeReflection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static chess.Main.Compute.isOpponent;


public class Main {


    public static class Compute {
        @CodeReflection
        public static boolean isWhite(byte squareBits) {
            return (squareBits & ChessData.Board.COLOR_MASK) == ChessData.Board.WHITE_BIT;
        }

        @CodeReflection
        public static boolean isBlack(byte squareBits) {
            return (squareBits & ChessData.Board.COLOR_MASK) == ChessData.Board.BLACK_BIT;
        }

        @CodeReflection
        public static boolean isOpponent(byte mySquareBits, byte opponentSquareBits) {
            return ((mySquareBits | opponentSquareBits) & ChessData.Board.COLOR_MASK) == ChessData.Board.COLOR_MASK;
        }

        @CodeReflection
        public static boolean isEmpty(byte squareBits) {
            return (squareBits & ChessData.Board.PIECE_MASK) == 0;
        }

        @CodeReflection
        public static boolean isHome(byte squareBits) {
            return (squareBits & ChessData.Board.HOME_BIT) == ChessData.Board.HOME_BIT;
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
            return (x < 8 && y < 8 && x >= 0 && y>= 0);
        }

        @CodeReflection
        static boolean isOnBoard(Point delta, int x, int y) {
            return isOnBoard(x + delta.x, y + delta.y);
        }

        @CodeReflection
        static boolean isPawn(int squareBits) {
            return (squareBits & ChessData.Board.PIECE_MASK) == ChessData.Board.PAWN;
        }

        @CodeReflection
        static boolean isKnight(int squareBits) {
            return (squareBits & ChessData.Board.PIECE_MASK) == ChessData.Board.KNIGHT;
        }

        @CodeReflection
        static boolean isBishop(int squareBits) {
            return (squareBits & ChessData.Board.PIECE_MASK) == ChessData.Board.BISHOP;
        }

        @CodeReflection
        static boolean isRook(int squareBits) {
            return (squareBits & ChessData.Board.PIECE_MASK) == ChessData.Board.ROOK;
        }

        @CodeReflection
        static boolean isKing(int squareBits) {
            return (squareBits & ChessData.Board.PIECE_MASK) == ChessData.Board.KING;
        }

        @CodeReflection
        static boolean isQueen(int squareBits) {
            return (squareBits & ChessData.Board.PIECE_MASK) == ChessData.Board.QUEEN;
        }
        @CodeReflection
        static boolean isEmpty(int squareBits) {
            return (squareBits & ChessData.Board.PIECE_MASK) == ChessData.Board.EMPTY;
        }

        @CodeReflection
        static boolean isOffBoard(int squareBits) {
            return squareBits == ChessData.Board.OFFBOARD;
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
            byte EMPTY = (byte) 0x00;
            byte PAWN = (byte) 0x01;
            byte KNIGHT = (byte) 0x02;
            byte BISHOP = (byte) 0x03;
            byte ROOK = (byte) 0x04;
            byte QUEEN = (byte) 0x06;
            byte KING = (byte) 0x0f;
            byte HOME_BIT = (byte) 0x80;
            byte WHITE_BIT = (byte) 0x40;
            byte BLACK_BIT = (byte) 0x20;
            byte CHECK_BIT = (byte) 0x10;
            byte PIECE_MASK = (byte) 0x0f;
            byte COLOR_MASK = (byte) 0x30;
            byte OFFBOARD = (byte) 0xff;
            short MOVE_BIT = (short) 0x0100;
            short CAPTURE_BIT = (short) 0x0200;

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
                return Compute.isOnBoard(x,y) ?squareBits(y * 8 + x):ChessData.Board.OFFBOARD;
            }

            default void validMoves(byte col){
                for (int y = 0; y < 7; y++) {
                    for (int x = 0; x < 7; x++) {
                         byte squareBits = getSquareBits(x, y);
                         if (!Compute.isEmpty(squareBits)) {
                             if (isOpponent(col, squareBits)) {
                                PIECE piece = PIECE.of(squareBits);
                                MoveList moveList  = piece.moveList;
                                if (piece.symmetrical){
                                    // ROOK,BISHOP,QUEEN,KING
                                   // boolean[] blocked =
                                }else{
                                    // PAWN, KNIGHT
                                }
                             }
                         }

                    }
                }
            }

            default Board init() {
                for (int y = 2; y < 6; y++) {
                    for (int x = 0; x < 8; x++) {
                        setSquareBits(x, y, EMPTY);
                    }
                }

                for (int x = 0; x < 8; x++) {
                    setSquareBits(x, 1, (byte) (PAWN | BLACK_BIT | HOME_BIT));
                    setSquareBits(x, 6, (byte) (PAWN | WHITE_BIT | HOME_BIT));
                }

                setSquareBits(0,0, (byte) (ROOK | BLACK_BIT | HOME_BIT));
                setSquareBits(7,0, (byte) (ROOK | BLACK_BIT | HOME_BIT));
                setSquareBits(0,7, (byte) (ROOK | WHITE_BIT | HOME_BIT));
                setSquareBits(7,7, (byte) (ROOK | WHITE_BIT | HOME_BIT));

                setSquareBits(1,0, (byte) (KNIGHT | BLACK_BIT | HOME_BIT));
                setSquareBits(6,0, (byte) (KNIGHT | BLACK_BIT | HOME_BIT));
                setSquareBits(1,7, (byte) (KNIGHT | WHITE_BIT | HOME_BIT));
                setSquareBits(6,7, (byte) (KNIGHT | WHITE_BIT | HOME_BIT));

                setSquareBits(2,0, (byte) (BISHOP | BLACK_BIT | HOME_BIT));
                setSquareBits(5,0, (byte) (BISHOP | BLACK_BIT | HOME_BIT));
                setSquareBits(2,7, (byte) (BISHOP | WHITE_BIT | HOME_BIT));
                setSquareBits(5,7, (byte) (BISHOP | WHITE_BIT | HOME_BIT));

                setSquareBits(3,0, (byte) (QUEEN | BLACK_BIT | HOME_BIT));
                setSquareBits(3,7, (byte) (QUEEN | WHITE_BIT | HOME_BIT));

                setSquareBits(4,0, (byte) (KING | BLACK_BIT | HOME_BIT));
                setSquareBits(4,7, (byte) (KING | WHITE_BIT | HOME_BIT));
                return this;
            }

            default String asString() {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("   | 1  2  3  4  5  6  7  8 |").append('\n');
                for (int y = 0; y < 8; y++) {
                    char ch = (char) (0x61 + y);
                    stringBuilder.append(' ').append(ch).append(' ').append('|');
                    for (int x = 0; x < 8; x++) {
                        byte squareBits = this.getSquareBits(x,y);
                        PIECE piece = PIECE.of(squareBits);
                        var background = ((x + y) % 2 == 0) ? TerminalColors.GREY : TerminalColors.DARKGREY;
                        stringBuilder.append(background.bg(" "));
                        stringBuilder.append(TerminalColors.fgbg(Compute.isWhite(squareBits) ? TerminalColors.WHITE : TerminalColors.BLACK, background, piece.unicode(Compute.isWhite(squareBits))));
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


    enum PIECE {
        EMPTY(ChessData.Board.EMPTY,false,false, false, ' ', ' ', new MoveListImpl()),
        PAWN(ChessData.Board.PAWN,false,false,false, '\u2659', '\u265f', new MoveListImpl()
                .add(0, 1, ChessData.Board.WHITE_BIT | ChessData.Board.MOVE_BIT)
                .add(0, 2, ChessData.Board.WHITE_BIT | ChessData.Board.HOME_BIT | ChessData.Board.MOVE_BIT)
                .add(0, -1, ChessData.Board.BLACK_BIT | ChessData.Board.MOVE_BIT)
                .add(0, -2, ChessData.Board.BLACK_BIT | ChessData.Board.HOME_BIT | ChessData.Board.MOVE_BIT)
                .add(1, 1, ChessData.Board.WHITE_BIT | ChessData.Board.CAPTURE_BIT)
                .add(-1, 1, ChessData.Board.WHITE_BIT | ChessData.Board.CAPTURE_BIT)
                .add(1, -1, ChessData.Board.BLACK_BIT | ChessData.Board.CAPTURE_BIT)
                .add(-1, -1, ChessData.Board.BLACK_BIT | ChessData.Board.CAPTURE_BIT)
        ),
        KNIGHT(ChessData.Board.KNIGHT, false, false, false, '\u2658', '\u265e', new MoveListImpl()
                .add(2, 1)
                .add(-2, 1)
                .add(2, -1)
                .add(-2, -1)
        ),
        BISHOP(ChessData.Board.BISHOP,true,true, false, '\u2657', '\u265d', new MoveListImpl().add(false, true, 8)),
        ROOK(ChessData.Board.ROOK,true, false, true, '\u2656', '\u265c', new MoveListImpl().add(true, false, 8)),
        QUEEN(ChessData.Board.QUEEN,true,true, true, '\u2655', '\u265b', new MoveListImpl().add(true, true, 1)),
        KING(ChessData.Board.KING,true, true, true,  '\u2654', '\u265a', new MoveListImpl().add(true, true, 2)),
        OFFBOARD(ChessData.Board.OFFBOARD,false,false, false, ' ', ' ', new MoveListImpl());

        public final char whiteUnicode;
        public final char blackUnicode;
        public final int value;
        public final boolean symmetrical;
        public final boolean diags;
        public final boolean rowcols;
        public final MoveList moveList;


        PIECE(int value, boolean symmetrical, boolean diags, boolean rowcols,char whiteUnicode, char blackUnicode, MoveList moveList) {
            this.value = value;
            this.symmetrical = symmetrical;
            this.diags = diags;
            this.rowcols = rowcols;
            this.whiteUnicode = whiteUnicode;
            this.blackUnicode = blackUnicode;
            this.moveList = moveList;
        }

        public char unicode(boolean white) {
            return white ? whiteUnicode : blackUnicode;
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

    interface MoveList extends Buffer {
        interface XY extends Buffer.Struct {
            byte x();

            void x(byte x);

            byte y();

            void y(byte y);

            short ctrl();

            void ctrl(short ctrl);
        }

        int length();

        XY xy(long idx);

        Schema<MoveList> schema = Schema.of(MoveList.class, chessData -> chessData
                .arrayLen("length").array("xy", xy -> xy.fields("x", "y", "ctrl")));

        static MoveList create(Accelerator acc, int length, PIECE piece) {
            var moveList = schema.allocate(acc,piece.moveList.length());
            for (int i = 0; i < length; i++) {
               XY from = piece.moveList.xy(i);
               XY to = moveList.xy(i);
               to.x(from.x());
               to.y(from.y());
               to.ctrl(from.ctrl());
           }
            return moveList;
        }
    }



    public static class MoveListImpl implements MoveList {
        public static class XYImpl implements MoveList.XY {
            byte x;
            byte y;
            short ctrl;

            @Override
            public byte x() {
                return x;
            }

            @Override
            public void x(byte x) {
                this.x = x;
            }

            @Override
            public byte y() {
                return y;
            }

            @Override
            public void y(byte y) {
                this.y = y;
            }

            @Override
            public short ctrl() {
                return ctrl;
            }

            @Override
            public void ctrl(short ctrl) {
                this.ctrl = ctrl;
            }

            public XYImpl(int x, int y, int ctrl) {
                x((byte) x);
                y((byte) y);
                ctrl((short) ctrl);
            }
        }

        List<MoveList.XY> xys = new ArrayList<>();

        @Override
        public int length() {
            return xys.size();
        }

        @Override
        public MoveList.XY xy(long idx) {
            return xys.get((int) idx);
        }

        MoveListImpl add(int x, int y, int ctrl) {
            xys.add(new MoveListImpl.XYImpl(x, y, ctrl));
            return this;
        }

        MoveListImpl add(int x, int y) {
            return add(x, y, ChessData.Board.MOVE_BIT | ChessData.Board.CAPTURE_BIT);
        }

        MoveListImpl add(boolean colrows, boolean diags, int n) {
            for (int v = 1; v< n; v++){
                if (diags){
                    add(-v, v);
                    add(v, -v);
                }
                if (colrows){
                    add(0, v);
                    add(0, -v);
                    add(v, 0);
                    add(-v, 0);
                }
            }
            return this;
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
            byte squareBits = board.squareBits(i);
            PIECE piece = PIECE.of(squareBits);
            int x = i % 8;
            int y = i / 8;
            Map<Integer, List<Point>> indexToPossibleMoves = new HashMap<>();
            switch (piece) {
                case PIECE.PAWN: {
                    List<Point> possibleMoves = new ArrayList<>();

                    if (Compute.isWhite(squareBits))
                        if ((squareBits & ChessData.Board.HOME_BIT) == ChessData.Board.HOME_BIT) {
                            if ((squareBits & ChessData.Board.WHITE_BIT) == ChessData.Board.WHITE_BIT) {
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
                default: {

                }
            }

        }
        System.out.println(board.asString());
    }
}
