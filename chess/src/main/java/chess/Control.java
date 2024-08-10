package chess;

import hat.Accelerator;
import hat.buffer.Buffer;
import hat.ifacemapper.Schema;

import static chess.ChessConstants.BISHOP;
import static chess.ChessConstants.KING;
import static chess.ChessConstants.KNIGHT;
import static chess.ChessConstants.PAWN;
import static chess.ChessConstants.QUEEN;
import static chess.ChessConstants.ROOK;

public interface Control extends Buffer {
    // piece squares tables for finding best position for each piece
    static byte[] wpEval = {
            0, 0, 0, 0, 0, 0, 0, 0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5, 5, 10, 25, 25, 10, 5, 5,
            0, 0, 0, 20, 20, 0, 0, 0,
            5, -5, -10, 0, 0, -10, -5, 5,
            5, 10, 10, -20, -20, 10, 10, 5,
            0, 0, 0, 0, 0, 0, 0, 0
    };
    static byte[] bpEval = {
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 10, 10, -20, -20, 10, 10, 5,
            5, -5, -10, 0, 0, -10, -5, 5,
            0, 0, 0, 20, 20, 0, 0, 0,
            5, 5, 10, 25, 25, 10, 5, 5,
            10, 10, 20, 30, 30, 20, 10, 10,
            50, 50, 50, 50, 50, 50, 50, 50,
            0, 0, 0, 0, 0, 0, 0, 0
    };

    static byte[] nEval = {
            -50, -40, -30, -30, -30, -30, -40, -50,
            -40, -20, 0, 0, 0, 0, -20, -40,
            -30, 0, 10, 15, 15, 10, 0, -30,
            -30, 5, 15, 20, 20, 15, 5, -30,
            -30, 0, 15, 20, 20, 15, 0, -30,
            -30, 5, 10, 15, 15, 10, 5, -30,
            -40, -20, 0, 5, 5, 0, -20, -40,
            -50, -40, -30, -30, -30, -30, -40, -50
    };

    static byte[] bEval = {
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 5, 5, 10, 10, 5, 5, -10,
            -10, 0, 10, 10, 10, 10, 0, -10,
            -10, 10, 10, 10, 10, 10, 10, -10,
            -10, 5, 0, 0, 0, 0, 5, -10,
            -20, -10, -10, -10, -10, -10, -10, -20
    };

    static byte[] rEval = {
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 10, 10, 10, 10, 10, 10, 5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            0, 0, 0, 5, 5, 0, 0, 0
    };

    static final byte[] qEval = {
            -20, -10, -10, -5, -5, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -5, 0, 5, 5, 5, 5, 0, -5,
            0, 0, 5, 5, 5, 5, 0, -5,
            -10, 5, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 0, 0, 0, 0, -10,
            -20, -10, -10, -5, -5, -10, -10, -20
    };

    static final byte[] kMidEval = {
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -20, -30, -30, -40, -40, -30, -30, -20,
            -10, -20, -20, -20, -20, -20, -20, -10,
            20, 20, 0, 0, 0, 0, 20, 20,
            20, 30, 10, 0, 0, 10, 30, 20
    };

    // was going to implement later; piece square table for endgame king movement
    static final byte[] kEndEval = {
            -50, -40, -30, -20, -20, -30, -40, -50,
            -30, -20, -10, 0, 0, -10, -20, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -30, 0, 0, 0, 0, -30, -30,
            -50, -30, -30, -30, -30, -30, -30, -50
    };


    int side();

    void side(int side);

    int ply();

    void ply(int ply);

    int weight(long idx);

    void weight(long idx, int weight);

    Schema<Control> schema = Schema.of(Control.class, control -> control
            .fields("ply", "side").array("weight", 64)
    );

    static int map(int v) {
        return switch (v) {
            case 0 -> 0;
            case 5 -> 1;
            case -5 -> -1;
            case 10 -> 2;
            case -10 -> -2;
            case 15 -> 2;
            case -15 -> -2;
            case 20 -> 3;
            case -20 -> -3;
            case 25 -> 4;
            case -25 -> -4;
            case 30 -> 5;
            case -30 -> -5;
            case 40 -> 6;
            case -40 -> -6;
            case 50 -> 7;
            case -50 -> -7;
            default -> throw new IllegalStateException("Unexpected value: " + v);
        } & 0xf;
    }

    static Control create(Accelerator acc) {
        var control = schema.allocate(acc);
        for (int i = 0; i < 64; i++) {
            int wp = map(wpEval[i]);
            int bp = map(bpEval[i]);
            int n = map(nEval[i]);
            int b = map(bEval[i]);
            int r = map(rEval[i]);
            int q = map(qEval[i]);
            int k = map(kMidEval[i]);
            int w = wp | bp << (PAWN * 4) | n << (KNIGHT * 4) | b << (BISHOP * 4) | r << (ROOK * 4) | q << (QUEEN * 4) | k << (KING * 4);

            // System.out.printf("%2x %2x %2x %2x %2x %2x %08x\n", k & 0xff, q & 0xff, r & 0xff, b & 0xff, n & 0xff, bp & 0xff, wp &0xff, w);
            control.weight(i, w);
            // System.out.printf("%2d %2d %2d %2d %2d %2d \n", p, r, n, b, q, k);

        }
        return control;

    }
}
