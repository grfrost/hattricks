package chess;


import hat.Accelerator;
import hat.ComputeContext;
import hat.KernelContext;
import hat.backend.Backend;
import hat.buffer.Buffer;
import hat.ifacemapper.Schema;

import java.lang.invoke.MethodHandles;
import java.lang.runtime.CodeReflection;

import static chess.Main.ChessData.Board.BISHOP;
import static chess.Main.ChessData.Board.BLACK_BIT;
import static chess.Main.ChessData.Board.EMPTY;
import static chess.Main.ChessData.Board.KING;
import static chess.Main.ChessData.Board.KNIGHT;
import static chess.Main.ChessData.Board.PAWN;
import static chess.Main.ChessData.Board.QUEEN;
import static chess.Main.ChessData.Board.ROOK;
import static chess.Main.ChessData.Board.WHITE_BIT;


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
        final static String FG = "38;";
        final static String BG = "48;";
        public final String seq;

        private TerminalColors(String seq) {
            this.seq = seq;
        }

        public String fg(String string){
            return "\u001b[" + FG+seq + "m"+string+"\u001b[" + FG+ "0m";
        }
        public String bg(String string){
            return "\u001b[" + BG+seq + "m"+string+"\u001b[" + BG+  "0m";
        }
        public String fg(char ch){
            return "\u001b[" + FG+seq + "m"+ch+"\u001b[" + FG+ "0m";
        }
        public String bg(char ch){
            return "\u001b[" + BG+seq + "m"+ch+"\u001b[" + BG+  "0m";
        }

    }


    public interface ChessData extends Buffer {
        public interface Board extends Buffer.Struct {
            public static final byte EMPTY = (byte) 0x00;
            public static final byte PAWN = (byte) 0x01;
            public static final byte KNIGHT = (byte) 0x02;
            public static final byte BISHOP = (byte) 0x03;
            public static final byte ROOK = (byte) 0x04;
            public static final byte QUEEN = (byte) 0x06;
            public static final byte KING = (byte) 0x07;
            public static final byte HOME_BIT = (byte) 0x80;
            public static final byte WHITE_BIT = (byte) 0x40;
            public static final byte BLACK_BIT = (byte) 0x20;
            public static final byte CHECK_BIT = (byte) 0x10;
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

            default String asString() {
                Board board = this;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("   | 1  2  3  4  5  6  7  8  |").append('\n');
                for (int row = 0; row < 8; row++) {
                    char ch = (char) (0x61 + row);
                    stringBuilder.append(' ').append(ch).append(' ').append('|');
                    for (int col = 0; col < 8; col++) {
                        byte square = this.square(row * 8 + col);

                        var background = ((col+row) % 2 == 0)?TerminalColors.BLACK: TerminalColors.DARKGREY;
                       // stringBuilder.append(color);
                        //https://en.wikipedia.org/wiki/Chess_symbols_in_Unicode
                        stringBuilder.append(background.bg(" "));
                        if (square == EMPTY) {
                            stringBuilder.append(background.bg(" "));
                        }else if ((square&0xf) == PAWN) {
                                stringBuilder.append(background.bg((square&WHITE_BIT)==WHITE_BIT?'\u2659':'\u265f'));
                        }else if ((square&0xf) == ROOK) {
                            stringBuilder.append(background.bg((square&WHITE_BIT)==WHITE_BIT?'\u2656':'\u265c'));
                        }else if ((square&0xf) == BISHOP) {
                            stringBuilder.append(background.bg((square&WHITE_BIT)==WHITE_BIT?'\u2657':'\u265D'));
                        }else if ((square&0xf) == KNIGHT) {
                            stringBuilder.append(background.bg((square&WHITE_BIT)==WHITE_BIT?'\u2658':'\u265e'));
                        }else if ((square&0xf) == QUEEN) {
                            stringBuilder.append(background.bg((square&WHITE_BIT)==WHITE_BIT?'\u2655':'\u265b'));
                        }else if ((square&0xf) == KING) {
                            stringBuilder.append(background.bg((square&WHITE_BIT)==WHITE_BIT?'\u2654':'\u265a'));
                        } else {
                            stringBuilder.append("?");
                        }
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


    public static class Compute {
        @CodeReflection
        public static void initBoard(ChessData.Board board) {
            for (int row = 2; row < 6; row++) {
              for (int col = 0; col < 8; col++) {
                  board.square(row * 8 + col, EMPTY);
              }
            }
            for (int col = 0; col < 8; col++) {
                board.square((long)(1 * 8) + col, (byte)(PAWN|BLACK_BIT));
                board.square((long)(6 * 8) + col, (byte)(PAWN|WHITE_BIT));
            }

            board.square((long)(0 * 8) + 0, (byte)(ROOK|BLACK_BIT));
            board.square((long)(0 * 8) + 7, (byte)(ROOK|BLACK_BIT));
            board.square((long)(7 * 8) + 0, (byte)(ROOK|WHITE_BIT));
            board.square((long)(7 * 8) + 7, (byte)(ROOK|WHITE_BIT));
            board.square((long)(0 * 8) + 1, (byte)(KNIGHT|BLACK_BIT));
            board.square((long)(0 * 8) + 6, (byte)(KNIGHT|BLACK_BIT));
            board.square((long)(7 * 8) + 1, (byte)(KNIGHT|WHITE_BIT));
            board.square((long)(7 * 8) + 6, (byte)(KNIGHT|WHITE_BIT));
            board.square((long)(0 * 8) + 2, (byte)(BISHOP|BLACK_BIT));
            board.square((long)(0 * 8) + 5, (byte)(BISHOP|BLACK_BIT));
            board.square((long)(7 * 8) + 2, (byte)(BISHOP|WHITE_BIT));
            board.square((long)(7 * 8) + 5, (byte)(BISHOP|WHITE_BIT));
            board.square((long)(0 * 8) + 3, (byte)(QUEEN|BLACK_BIT));
            board.square((long)(7 * 8) + 3, (byte)(QUEEN|WHITE_BIT));
            board.square((long)(0 * 8) + 4, (byte)(KING|BLACK_BIT));
            board.square((long)(7 * 8) + 4, (byte)(KING|WHITE_BIT));


        }
        @CodeReflection
        public static void initData(KernelContext kc, ChessData chessData) {
            if (kc.x < kc.maxX) {
                ChessData.Board board = chessData.board(kc.x);
                initBoard( board);
            }
        }


        @CodeReflection
        static public void init(final ComputeContext cc, ChessData chessData) {
            cc.dispatchKernel(chessData.length(), kc -> Compute.initData(kc, chessData));
        }
    }




    public static void main(String[] args) {
        boolean headless = Boolean.getBoolean("headless") || (args.length > 0 && args[0].equals("--headless"));
        Accelerator accelerator = new Accelerator(MethodHandles.lookup(), Backend.FIRST);
        ChessData chessData = ChessData.create(accelerator, 10001);//,101,1001,10001
        accelerator.compute(cc->Compute.init(cc, chessData));
        ChessData.Board board = chessData.board(0);
        System.out.println(board.asString());
    }
}
