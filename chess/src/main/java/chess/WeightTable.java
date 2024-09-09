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

public interface WeightTable extends Buffer {

    /*
     * We have combined the weight tables for [p]awn, k[n]ight, [b]ishop, [q]ueen [k]ing below
     *
     * Given that our pieces are PAWN=1, NIGHT=2 ... KING=5 we subtract 1 from the piece value
     * then multiply by 64 to get the 'base offset' for a pieces table
     *
     * For white pieces we just add sqid (0-63) to this base offset.
     *
     * Because the tables are for 'white' for black pieces we add (63-sqid) to the base offset
     */

     byte[] pnbrqk = {
            //64 * (PAWN - 1) = 0
            0,  0,  0,   0,   0,  0,  0, 0,           //  0
            5, 10, 10, -20, -20, 10, 10, 5,           //  8
            5, -5, -10, 0, 0, -10, -5, 5,             // 16
            0, 0, 0, 20, 20, 0, 0, 0,                 // 24
            5, 5, 10, 25, 25, 10, 5, 5,               // 32
            10, 10, 20, 30, 30, 20, 10, 10,           // 40
            50, 50, 50, 50, 50, 50, 50, 50,           // 48
            0, 0, 0, 0, 0, 0, 0, 0,                   // 56


            //64 * (KNIGHT - 1) = 64
            -50, -40, -30, -30, -30, -30, -40, -50,   //  0
            -40, -20, 0, 0, 0, 0, -20, -40,           //  8
            -30, 0, 10, 15, 15, 10, 0, -30,           // 16
            -30, 5, 15, 20, 20, 15, 5, -30,           // 24
            -30, 0, 15, 20, 20, 15, 0, -30,           // 32
            -30, 5, 10, 15, 15, 10, 5, -30,           // 40
            -40, -20, 0, 5, 5, 0, -20, -40,           // 48
            -50, -40, -30, -30, -30, -30, -40, -50,   // 56
            //64 * (BISHOP - 1) = 128
            -20, -10, -10, -10, -10, -10, -10, -20,   //  0
            -10, 0, 0, 0, 0, 0, 0, -10,               //  8
            -10, 0, 5, 10, 10, 5, 0, -10,             // 16
            -10, 5, 5, 10, 10, 5, 5, -10,             // 24
            -10, 0, 10, 10, 10, 10, 0, -10,           // 32
            -10, 10, 10, 10, 10, 10, 10, -10,         // 40
            -10, 5, 0, 0, 0, 0, 5, -10,               // 48
            -20, -10, -10, -10, -10, -10, -10, -20,   // 56
            //64 * (ROOK - 1) = 192
            0, 0, 0, 0, 0, 0, 0, 0,                   //  0
            5, 10, 10, 10, 10, 10, 10, 5,             //  8
            -5, 0, 0, 0, 0, 0, 0, -5,                 // 16
            -5, 0, 0, 0, 0, 0, 0, -5,                 // 24
            -5, 0, 0, 0, 0, 0, 0, -5,                 // 32
            -5, 0, 0, 0, 0, 0, 0, -5,                 // 40
            -5, 0, 0, 0, 0, 0, 0, -5,                 // 48
            0, 0, 0, 5, 5, 0, 0, 0,                   // 56
            //64 * (QUEEN - 1) = 256
            -20, -10, -10, -5, -5, -10, -10, -20,     //  0
            -10, 0, 0, 0, 0, 0, 0, -10,               //  8
            -10, 0, 5, 5, 5, 5, 0, -10,               // 16
            -5, 0, 5, 5, 5, 5, 0, -5,                 // 24
            -5, 0, 5, 5, 5, 5, 0, -5,                 // 32
            -10, 5, 5, 5, 5, 5, 0, -10,               // 40
            -10, 0, 5, 0, 0, 0, 0, -10,               // 48
            -20, -10, -10, -5, -5, -10, -10, -20,     // 56
            //64 * (KING - 1) = 320
            -30, -40, -40, -50, -50, -40, -40, -30,   //  0
            -30, -40, -40, -50, -50, -40, -40, -30,   //  8
            -30, -40, -40, -50, -50, -40, -40, -30,   // 16
            -30, -40, -40, -50, -50, -40, -40, -30,   // 24
            -20, -30, -30, -40, -40, -30, -30, -20,   // 32
            -10, -20, -20, -20, -20, -20, -20, -10,   // 40
            20, 20, 0, 0, 0, 0, 20, 20,               // 48
            20, 30, 10, 0, 0, 10, 30, 20              // 56
    };

    byte weight(long idx);

    void weight(long idx, byte weight);

    Schema<WeightTable> schema = Schema.of(WeightTable.class, control -> control
            .array("weight", 64*6)
    );

    static WeightTable create(Accelerator acc) {
        var weightTable = schema.allocate(acc);
           for (int i = 0; i < pnbrqk.length; i++) {
               weightTable.weight(i, pnbrqk[i]);
           }
        return weightTable;
    }
}
