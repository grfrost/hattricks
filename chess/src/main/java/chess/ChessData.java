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

        byte squareBits(long idx);

        void squareBits(long idx, byte squareBits);
        int id();

        void id(int id);

        int parent();

        void parent(int parent);


        short score();
        void score(short score);

        int firstChildIdx();
        void firstChildIdx(int firstChildIdx);

        byte moves();
        void moves(byte moves);

        // The move that got us here from the parent
        byte fromSquareIdx();
        byte toSquareIdx();
        void fromSquareIdx(byte fromSquareIdx);
        void toSquareIdx(byte toSquareIdx);

        // Our move number (relative to parent.firstChild)
        void move(byte move);
        byte move();
    }

    int length();

    Board board(long idx);

    Schema<ChessData> schema = Schema.of(ChessData.class, chessData -> chessData
            .arrayLen("length")//.pad(4)  // must be 4 if array has a long ?
            .array("board", square -> square
                    .array("squareBits", 64)
                    //               4     4          4             1        1    1     1        2 == 82
                    .fields("id","parent", "firstChildIdx","moves","fromSquareIdx","toSquareIdx", "move", "score")
                    .pad(2) //84
            ) // each board is 80 bytes.

    );

    static ChessData create(Accelerator acc, int length) {
        return schema.allocate(acc, length);
    }
}
