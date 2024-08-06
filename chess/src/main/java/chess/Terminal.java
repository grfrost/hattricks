package chess;

import java.util.function.Consumer;

public class Terminal {
    StringBuilder stringBuilder = new StringBuilder();
    public static final String esc = "\u001b[";
    public static final String suffix = "m";
    public final static String escFG = esc + "38;";
    public final static String escBG = esc + "48;";
    static final char chessKingUnicode = 0x2654;
    public static String rgb(int rgb) {
        return "5;" + rgb + suffix;
    }
    public static String rgb(int r, int g, int b) {
        return "2;" + r + ";" + g + ";" + b + suffix;
    }
    public static String fg(int rgb) {
        return escFG + rgb(rgb);
    }
    public static String fg(int r, int g, int b) {
        return escFG + rgb(r, g, b);
    }
    public static String bg(int r, int g, int b) {
        return escBG + rgb(r, g, b);
    }
    public static String bg(int rgb) {
        return escBG + rgb(rgb);
    }
    public Terminal bg(int r, int g, int b, Consumer<Terminal> consumer) {
        str(bg(r, g, b));
        consumer.accept(this);
        return str(escBG + "0"+suffix);
    }
    public Terminal fg(int rgb, Consumer<Terminal> consumer) {
        str(fg(rgb));
        consumer.accept(this);
        return str(escFG + "0"+suffix);
    }
    public Terminal fg(int r, int g, int b, Consumer<Terminal> consumer) {
        str(fg(r, g, b));
        consumer.accept(this);
        return str(escFG + "0"+suffix);
    }

    public Terminal redOnGreen(Consumer<Terminal> consumer) {
        return fg(250, 0, 0, fg -> fg.bg(0, 100, 0, consumer));
    }
    public Terminal onGrey(int level, Consumer<Terminal> consumer) {
        return bg(level, level, level,consumer);
    }
    public Terminal bg(int rgb, Consumer<Terminal> consumer) {
        str(bg(rgb));
        consumer.accept(this);
        return str(escBG + "0"+suffix);
    }

    public Terminal ch(char ch) {
        stringBuilder.append(ch);
        return this;
    }

    public Terminal ch(int ch) {
        return ch((char) ch);
    }

    public Terminal chln(char ch) {
        return ch(ch).nl();
    }

    public Terminal str(String s) {
        stringBuilder.append(s);
        return this;
    }

    public Terminal nl() {
        ch('\n');
        return this;
    }

    public Terminal space() {
        ch(' ');
        return this;
    }

    public Terminal space(int n) {
        while (n-- > 0) {
            space();
        }
        return this;
    }

    public Terminal bar() {
        ch('|');
        return this;
    }

    public Terminal strln(String s) {
        return str(s).nl();
    }
    public Terminal board(Main.ChessData.Board board) {
        space(3).redOnGreen(_->str("| a  b  c  d  e  f  g  h |")).nl();
        for (int y = 0; y < 8; y++) {
            final int finaly = y;
            redOnGreen( _ -> space().ch(0x31 + finaly).space().bar());
            for (int x = 0; x < 8; x++) {
                byte squareBits = board.getSquareBits(x, y);
                onGrey(64 + (((x + y) % 2) * 64), _ -> {
                    space();
                    if (Main.Compute.isEmpty(squareBits)) {
                        space();
                    } else {
                         /*
                          * Note order for unicode chess pieces descend P -> K
                          *
                          * WHITE P'\u2659', N'\u2658', B'\u2657', R'\u2656', Q'\u2655', K'\u2654',
                          * BLACK P'\u265f', N'\u265e', B'\u265d', R'\u265c', Q'\u265b', K'\u265a',
                          *
                          * Also if we add 6 to the WHITE unicode we get BLACK unicode of same piece
                          *
                          * Our square bit values ascend  P=1,N=2,B=3,R=4,Q=5,K=6,
                          *
                          * So
                          *   chessKingUnicode+6-value converts our 'value' to white unicode
                          *   chessKingUnicode+12-value converts our 'value' to black unicode
                          */

                        ch(Main.Compute.isWhite(squareBits)
                                ? chessKingUnicode + 6 - (squareBits & Main.PIECE_MASK)
                                : chessKingUnicode + 6 + 6 - (squareBits & Main.PIECE_MASK));
                    }
                    space();
                });
            }
            redOnGreen(_ -> bar()).nl();
        }
        space(3).redOnGreen(bg -> bg.str("| a  b  c  d  e  f  g  h |")).nl();
        return this;
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
