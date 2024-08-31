package chess;

import java.util.function.Consumer;

import static chess.ChessConstants.BISHOP;
import static chess.ChessConstants.CHECK;
import static chess.ChessConstants.KING;
import static chess.ChessConstants.KNIGHT;
import static chess.ChessConstants.PAWN;
import static chess.ChessConstants.PIECE_MASK;
import static chess.ChessConstants.QUEEN;
import static chess.ChessConstants.ROOK;
import static chess.ChessConstants.ROW_SHIFT;

public class BoardRenderer {
    StringBuilder stringBuilder = new StringBuilder();
    public static final String esc = "\u001b[";
    public static final String suffix = "m";
    public final static String escFG = esc + "38;";
    public final static String escBG = esc + "48;";
    static final char chessKingUnicode = 0x2654;

    public BoardRenderer bg(int r, int g, int b, Consumer<BoardRenderer> consumer) {
        str(escBG + "2;" + r + ";" + g + ";" + b + suffix);
        consumer.accept(this);
        return str(escBG + "0" + suffix);
    }

    public BoardRenderer fg(int r, int g, int b, Consumer<BoardRenderer> consumer) {
        str(escFG + "2;" + r + ";" + g + ";" + b + suffix);
        consumer.accept(this);
        return str(escFG + "0" + suffix);
    }

    public BoardRenderer border(Consumer<BoardRenderer> consumer) {
        return fg(0, 250, 0, fg -> fg.bg(128, 128, 128, consumer));
    }

    public BoardRenderer square(int x, int y, boolean highlight, int hx, int hy, Consumer<BoardRenderer> consumer) {
        if (x == hx && y == hy && highlight) {
            return bg(64, 24, 24, consumer);
        } else {
            int grey = 64 + (((x + y) % 2) * 64);
            return bg(grey, grey, grey, consumer);
        }
    }


    public BoardRenderer ch(char ch) {
        stringBuilder.append(ch);
        return this;
    }

    public BoardRenderer ch(int ch) {
        return ch((char) ch);
    }

    public BoardRenderer chln(char ch) {
        return ch(ch).nl();
    }

    public BoardRenderer str(String s) {
        stringBuilder.append(s);
        return this;
    }

    public BoardRenderer nl() {
        return ch('\n');
    }

    public BoardRenderer ohome() {
        return ch('<');
    }

    public BoardRenderer chome() {
        return ch('>');
    }

    public BoardRenderer space() {
        return ch(' ');
    }

    public BoardRenderer space(int n) {
        while (n-- > 0) {
            space();
        }
        return this;
    }
    public BoardRenderer either(boolean test, Consumer<BoardRenderer> yes, Consumer<BoardRenderer> no) {
        if(test){
            yes.accept(this);
        } else {
            no.accept(this);
        }
        return this;
    }
    public BoardRenderer ifPiece(byte squareBits, Consumer<BoardRenderer> piece, Consumer<BoardRenderer> empty ) {
        if (Compute.isEmpty(squareBits)) {
            empty.accept(this);
        } else {
           piece.accept(this);
        }
        return this;
    }
    public BoardRenderer ifInCheck(byte squareBits, Consumer<BoardRenderer> inCheck, Consumer<BoardRenderer> notInCheck ) {
        if (Compute.isSet(squareBits, CHECK)) {
            inCheck.accept(this);
        } else {
            notInCheck.accept(this);
        }
        return this;
    }


    public BoardRenderer intf(String format, int value) {
        return str(String.format(format, value));
    }

    public BoardRenderer algebraic(String label, int squareIdx) {
        return str(label).ch(':').str(algebraic(squareIdx % 8, squareIdx / 8));
    }

    public BoardRenderer algebraic(String label, int fromSqId, int toSqId) {
        return str(label)
                .str(algebraic(fromSqId % 8, fromSqId / 8))
                .str("->")
                .str(algebraic(toSqId % 8, toSqId / 8));

    }

    public BoardRenderer bar() {
        return ch('|');
    }

    public BoardRenderer strln(String s) {
        return str(s).nl();
    }

    public BoardRenderer spaceOrPiece(byte squareBits) {
        ifPiece(squareBits,
                then-> str(piece(squareBits)),
                otherwise->ifInCheck(squareBits,
                        inCheck->ch('.'),
                        notInCheck->space()
                )
        );
        return this;
    }

    private BoardRenderer lineHighlightUnicode(ChessData.Board board, boolean highlight, int squareIdx) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                byte squareBits = board.squareBits((y << ROW_SHIFT) + x);
                square(x, y, highlight, squareIdx % 8, squareIdx / 8, _ -> {
                    spaceOrPiece(squareBits);
                });
            }
            border(_ -> bar());
        }
        return this;
    }
    BoardRenderer detail(ChessData.Board board){
        intf("score=%5d", board.score()).space();
        intf("id=%6d", board.id()).space().intf("parent %3d", board.parent()).space();
        intf("moves %2d", board.moves()).space().intf("firstChildIdx %3d", board.firstChildIdx()).space();
        algebraic("from", board.fromSqId()).space().algebraic("to", board.toSqId()).space().nl();
        return this;
    }

    private BoardRenderer lineUnicode(ChessData.Board board) {
        return lineHighlightUnicode(board, false, 0).detail(board);
    }

    private BoardRenderer boardText(ChessData.Board board) {
        algebraic("from", board.fromSqId()).space().algebraic("to", board.toSqId()).space().nl();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                byte sqBits = board.squareBits((y << ROW_SHIFT) + x);
                space();
                ifPiece(sqBits,
                        then -> str(textPiece(sqBits)),
                        otherwise -> ifInCheck(sqBits,
                                inCheck -> ch('.'),
                                notInCheck -> space())
                        );
                space();
            }
            nl();
        }
        return this;
    }


    private BoardRenderer board(ChessData.Board board) {
        detail(board);
        space(3).border(_ -> str("| a  b  c  d  e  f  g  h |")).nl();
        for (int y = 0; y < 8; y++) {
            final int finaly = 7 - y;
            border(_ -> space().ch(0x31 + finaly).space().bar());
            for (int x = 0; x < 8; x++) {
                byte sqBits = board.squareBits((y << ROW_SHIFT) + x);
                square(x, y, false, 0, 0, _ -> {
                    space().spaceOrPiece(sqBits).space();
                });
            }
            border(_ -> bar()).nl();
        }
        space(3).border(bg -> bg.str("| a  b  c  d  e  f  g  h |")).nl();
        return this;
    }

    static String algebraic(int x, int y) {
        return Character.toString(x + 65 + 32) + Integer.toString(8 - y);
    }

    static String piece(byte sqBits) {
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
        int offset = Compute.isWhite(sqBits) ? 12 : 6;
        return Character.toString(chessKingUnicode + offset - (sqBits & PIECE_MASK));
    }

    static String textPiece(byte sqBits) {
        int offset = Compute.isWhite(sqBits) ? 0 : 32;
        char ch = switch (Compute.pieceValue(sqBits)) {
            case PAWN -> 'P';
            case KNIGHT -> 'N';
            case BISHOP -> 'B';
            case ROOK -> 'R';
            case QUEEN -> 'Q';
            case KING -> 'K';
            default -> throw new IllegalStateException("Unexpected value: " + Compute.pieceValue(sqBits));
        };
        return Character.toString(ch + offset);
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
    static String unicode(ChessData.Board board){
        return new BoardRenderer().board(board).toString();
    }
    static String text(ChessData.Board board){
        return new BoardRenderer().boardText(board).toString();
    }
    static String line(ChessData.Board board){
        return new BoardRenderer().lineUnicode(board).toString();
    }
}
