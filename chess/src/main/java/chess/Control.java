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

public interface Control extends Buffer {


    int side();
    void side(int side);

    int plyStartIdx();
    void plyStartIdx(int plyStartIdx);
    int plyEndIdx();
    void plyEndIdx(int plyEndIdx);

    Schema<Control> schema = Schema.of(Control.class, control -> control
            .fields( "side", "plyStartIdx","plyEndIdx")
    );

    static Control create(Accelerator acc) {
        return schema.allocate(acc);
    }

    // Helpers not available on GPU!
    default void setBounds(int start, int end){
        plyStartIdx(start);
        plyEndIdx(end);
    }

    default void swapSide(){
        side((side() & WHITE_BIT) == WHITE_BIT ? EMPTY_SQUARE : WHITE_BIT);
    }
}
