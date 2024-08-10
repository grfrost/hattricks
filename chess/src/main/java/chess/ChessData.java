package chess;

import hat.Accelerator;
import hat.buffer.Buffer;
import hat.ifacemapper.Schema;

public interface ChessData extends Buffer {
    interface Board extends Struct {

        byte squareBits(long idx);

        void squareBits(long idx, byte squareBits);

        int parent();

        void parent(int parent);

        short score();

        void score(short score);

        short moves();

        void moves(short moves);

       // long moveBits();

       // void moveBits(long moveBits);
    }

    int length();

    Board board(long idx);

    Schema<ChessData> schema = Schema.of(ChessData.class, chessData -> chessData
            .arrayLen("length")//.pad(4)  // must be 4 if array has a long ?
            .array("board", square -> square
                    //.field("moveBits")
                    .array("squareBits", 64)
                    .fields("parent", "score", "moves")
            )

    );

    static ChessData create(Accelerator acc, int length) {
        return schema.allocate(acc, length);
    }
}
