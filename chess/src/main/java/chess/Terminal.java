package chess;

import java.util.function.Consumer;

import static chess.ChessConstants.BISHOP;
import static chess.ChessConstants.EMPTY_SQUARE;
import static chess.ChessConstants.KING;
import static chess.ChessConstants.KNIGHT;
import static chess.ChessConstants.PAWN;
import static chess.ChessConstants.PIECE_MASK;
import static chess.ChessConstants.QUEEN;
import static chess.ChessConstants.ROOK;

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
    public Terminal square(int x,int y, boolean highlight,int hx, int hy, Consumer<Terminal> consumer) {
        if (x==hx && y==hy && highlight) {
            return bg(64, 24, 24, consumer);
        }else {
            int grey = 64 + (((x + y) % 2) * 64);
            return bg(grey, grey, grey, consumer);
        }
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
    public Terminal intf(String format, int value) {
        return str(String.format(format,value));
    }

    public Terminal algebraic(String label, int squareIdx){

        var x = squareIdx%8;
        var y = squareIdx/8;
        return str(label).ch(':').str(algebraic(x,y));
    }
    public Terminal bar() {
        ch('|');
        return this;
    }

    public Terminal strln(String s) {
        return str(s).nl();
    }

    public Terminal lineHighlight(ChessData.Board board, boolean highlight,int squareIdx) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                byte squareBits = board.squareBits(y*8+x);
                square(x,y,highlight,squareIdx%8, squareIdx/8,_ -> {
                    if (Compute.isEmpty(squareBits)) {
                        space();
                    } else {
                        str(piece(squareBits));
                    }
                });
            }
            border(_ -> bar());
        }
        return this;
    }
    public  Terminal line(ChessData.Board board, int boardId) {
        intf("Score %4d,", board.score()).space();
        intf("BoardId %3d,",boardId).space().intf("Parent %3d,", board.parent()).space();
        intf("Moves %2d,", board.moves()).space().intf("FirstChildIdx %3d,", board.firstChildIdx()).space();
        algebraic("from", board.fromSquareIdx()).space().algebraic("to", board.toSquareIdx()).space();
        intf("ParentRelativeMove %2d,", board.move()).space();
        lineHighlight(board, false, 0);
        return this;
    }

    public Terminal board(ChessData.Board board, int boardId) {
        intf("Score %4d", board.score()).space();
        intf("BoardId %3d", boardId).space().intf("Parent %3d", board.parent()).space();
        intf("Moves %2d", board.moves()).space().intf("FirstChildIdx %3d", board.firstChildIdx()).space();
        algebraic("from", board.fromSquareIdx()).space(). algebraic("to", board.toSquareIdx()).space().nl();
        space(3).border(_->str("| a  b  c  d  e  f  g  h |")).nl();
        for (int y = 0; y < 8; y++) {
            final int finaly = 7-y;
            border(_ -> space().ch(0x31 + finaly).space().bar());
            for (int x = 0; x < 8; x++) {
                byte squareBits = board.squareBits(y*8+x);
                square(x,y,false,0,0, _ -> {
                    space();
                    if (Compute.isEmpty(squareBits)) {
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
        int offset =   Compute.isWhite(squareBits)?12:6;
        return Character.toString(chessKingUnicode + offset - (squareBits & PIECE_MASK));
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
                case PAWN:
                    sb.append("PAWN");
                    break;
                case ROOK:
                    sb.append("ROOK");
                    break;
                case KNIGHT:
                    sb.append("KNIGHT");
                    break;
                case BISHOP:
                    sb.append("BISHOP");
                    break;
                case QUEEN:
                    sb.append("QUEEN");
                    break;
                case KING:
                    sb.append("KING");
                    break;
            }

        }
        return sb.toString();
    }
    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
