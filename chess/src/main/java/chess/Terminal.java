package chess;

import java.util.function.Consumer;

public class Terminal {
    StringBuilder stringBuilder = new StringBuilder();
    public static final String esc = "\u001b[";
    public static final String suffix = "m";
    public final static String escFG = esc + "38;";
    public final static String escBG = esc + "48;";
    static final char chessKingUnicode = 0x2654;

    public Terminal bg(int r, int g, int b, Consumer<Terminal> consumer) {
        str(escBG +  "2;" + r + ";" + g + ";" + b + suffix);
        consumer.accept(this);
        return str(escBG + "0"+suffix);
    }

    public Terminal fg(int r, int g, int b, Consumer<Terminal> consumer) {
        str(escFG +  "2;" + r + ";" + g + ";" + b + suffix);
        consumer.accept(this);
        return str(escFG + "0"+suffix);
    }

    public Terminal border(Consumer<Terminal> consumer) {
        return fg(0, 250, 0, fg -> fg.bg(128, 128, 128, consumer));
    }
    public Terminal square(int x,int y, Consumer<Terminal> consumer) {
        int grey = 64 + (((x + y) % 2) * 64);
        return bg(grey, grey, grey,consumer);
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
        space(3).border(_->str("| a  b  c  d  e  f  g  h |")).nl();
        for (int y = 0; y < 8; y++) {
            final int finaly = 7-y;
            border(_ -> space().ch(0x31 + finaly).space().bar());
            for (int x = 0; x < 8; x++) {
                byte squareBits = board.getSquareBits(x, y);
                square(x,y, _ -> {
                    space();
                    if (Main.Compute.isEmpty(squareBits)) {
                        space();
                    } else {
                        str(piece(squareBits));
                    }
                    space();
                });
            }
            border(_ -> bar()).nl();
        }
        space(3).border(bg -> bg.str("| a  b  c  d  e  f  g  h |")).nl();
        return this;
    }
    static String algebraic(int x, int y) {
        return Character.toString(x + 65 +32) + Integer.toString(8-y);
    }

    static String piece(byte squareBits) {
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
        int offset =   Main.Compute.isWhite(squareBits)?12:6;
        return Character.toString(chessKingUnicode + offset - (squareBits & Main.PIECE_MASK));
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
