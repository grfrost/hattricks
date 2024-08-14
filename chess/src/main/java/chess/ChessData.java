package chess;

import hat.Accelerator;
import hat.buffer.Buffer;
import hat.ifacemapper.Schema;

import static chess.ChessConstants.BISHOP;
import static chess.ChessConstants.EMPTY_SQUARE;
import static chess.ChessConstants.KING;
import static chess.ChessConstants.KNIGHT;
import static chess.ChessConstants.PAWN;
import static chess.ChessConstants.QUEEN;
import static chess.ChessConstants.ROOK;
import static chess.ChessConstants.WHITE_BIT;

public interface ChessData extends Buffer {
    interface Board extends Struct {
        default void init(){
            int x=0;
            for (byte bits :new byte[]{ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK}){
                squareBits(x,(byte) (bits));
                squareBits(x+8,(byte) (PAWN));
                for (int i=16;i<48;i+=8) {
                    squareBits(x + i, (byte) (EMPTY_SQUARE));
                }
                squareBits(x+48,(byte) (WHITE_BIT|PAWN));
                squareBits(x+56,(byte) (WHITE_BIT|bits));
                x++;
            }
            // The score after init is zero, no move got us here, the prefix is 0
            // The number of moves available to white is 20 (same for black presumeably)
            score((short)0);
            moves((byte)20);
            prefix(0);
            from((byte) 0);
            to((byte) 0);
        }
        byte squareBits(long idx);

        void squareBits(long idx, byte squareBits);

        int parent();

        void parent(int parent);


        int prefix();
        void prefix(int prefix);

        short score();
        void score(short score);
        byte moves();

        void moves(byte moves);

        // The move that got us here from the parent
        byte from();
        byte to();
        void from(byte from);
        void to(byte to);
    }

    int length();

    Board board(long idx);

    Schema<ChessData> schema = Schema.of(ChessData.class, chessData -> chessData
            .arrayLen("length")//.pad(4)  // must be 4 if array has a long ?
            .array("board", square -> square
                    .array("squareBits", 64)
                    //                 4          4       2        1       1     1
                    .fields("parent", "prefix", "score", "moves","from","to")
                    .pad(3)
            ) // each board is 80 bytes.

    );

    static ChessData create(Accelerator acc, int length) {
        return schema.allocate(acc, length);
    }
}
