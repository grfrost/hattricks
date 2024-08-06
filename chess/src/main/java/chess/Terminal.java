package chess;

public class Terminal {
    StringBuilder stringBuilder = new StringBuilder();
    public static final String esc = "\u001b[";
    public static final String suffix = "m";
    public final static String escFG = esc + "38;";
    public final static String escBG = esc + "48;";
    static final char chessKingUnicode = 0x2654;
    public static String rgb(int r, int g, int b) {
        return "2;" + r + ";" + g + ";" + b + ";m";
    }

    public static String fg(int r, int g, int b) {
        return escFG + rgb(r, g, b);
    }

    public static String bg(int r, int g, int b) {
        return escBG + rgb(r, g, b);
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

    public Terminal bar() {
        ch('|');
        return this;
    }

    public Terminal strln(String s) {
        return str(s).nl();
    }

    static char squareBitsToUnicode(byte squareBits) {

    /*
     Note order for unicode chess pieces descend P -> K

     WHITE P'\u2659', N'\u2658', B'\u2657', R'\u2656', Q'\u2655', K'\u2654',
     BLACK P'\u265f', N'\u265e', B'\u265d', R'\u265c', Q'\u265b', K'\u265a',

     Also if we add 6 to the WHITE unicode we get BLACK unicode of same piece

     Our square bit values ascend  P=1,N=2,B=3,R=4,Q=5,K=6,

     So
       chessKingUnicode+6-value converts our 'value' to white unicode
       chessKingUnicode+12-value converts our 'value' to black unicode
     */
        byte value = (byte) (squareBits & Main.PIECE_MASK);
        return (char) (value == 0 ? ' ' : Main.Compute.isWhite(squareBits) ? chessKingUnicode + 6 - value : chessKingUnicode + 6 + 6 - value);

    }

    public Terminal board(Main.ChessData.Board board) {
        strln("   | a  b  c  d  e  f  g  h |");
        for (int y = 0; y < 8; y++) {
            space().ch(0x31 + y).space().bar();
            for (int x = 0; x < 8; x++) {
                byte squareBits = board.getSquareBits(x, y);
                var background = ((x + y) % 2 == 0) ? Colors.GREY : Colors.DARKGREY;
                str(background.bg(" "));
                char unicode = squareBitsToUnicode(squareBits);
                str(Colors.fgbg(Main.Compute.isWhite(squareBits)
                                ? Colors.WHITE
                                : Colors.BLACK,
                        background, unicode));
                str(background.bg(" "));
            }
            bar().nl();
        }
        strln("   | a  b  c  d  e  f  g  h |");
return this;
    }

    public static enum Colors {
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

        public final String fseq;
        public final String bseq;

        private Colors(String seq) {
            this.fseq = escFG + seq + suffix;
            this.bseq = escBG + seq + suffix;
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

        public static String fgbg(Colors fg, Colors bg, String string) {
            return bg.bseq + fg.fseq + string + NONE.fseq + NONE.bseq;
        }

        public static String fgbg(Colors fg, Colors bg, char ch) {
            return bg.bseq + fg.fseq + ch + NONE.fseq + NONE.bseq;
        }
    }
    @Override
    public String toString(){
        return stringBuilder.toString();
    }
}
