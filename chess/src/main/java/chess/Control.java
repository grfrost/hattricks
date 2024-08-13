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
    // See how we  combine these into a single weighted map
    // using createWeightedMap
    // piece squares tables for finding best position for each piece
    static byte[] pEval = {
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
            -5, 0, 5, 5, 5, 5, 0, -5,
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
    static void createWeightedMap() {
        System.out.print("int[] weightMap = new int[]{");
        for (int i = 0; i < 64; i++) {
            int p = map(pEval[i]);
            int n = map(nEval[i]);
            int b = map(bEval[i]);
            int r = map(rEval[i]);
            int q = map(qEval[i]);
            int k = map(kMidEval[i]);
            int w =  p << (PAWN * 4) | n << (KNIGHT * 4) | b << (BISHOP * 4) | r << (ROOK * 4) | q << (QUEEN * 4) | k << (KING * 4);
            if ((i %8) == 0){
                System.out.print("\n   ");
            }
            System.out.printf("0x%08x",w);
            if (i<63){
                System.out.print(", ");
            }
        }
        System.out.println("\n};");
    }
    // We have encoded tables for all 6 piece types above,
    // Below we offer the tables combined into an array of int where
    // each nibble 0->f at PIECE value offset (pawn =1) yields the value,
    // ((weighted[0-64])>>>PAWN)&0xf yields weighting for white pawn.
    // ((weighted[0-64])>>>KING)&0xf yields weighting for white king.
    // The tables above are for white.  By subtracting index from 63 we can get the black Values.
    // ((weighted[63-(0-64)])>>>PAWN) yields weighting for black pawn

    int[] weightMap = new int[]{
            0x0bd0d900, 0x0ae0ea00, 0x0ae0eb00, 0x09f0eb00, 0x09f0eb00, 0x0ae0eb00, 0x0ae0ea00, 0x0bd0d900,
            0x0be1ea10, 0x0a020d20, 0x0a020020, 0x090200d0, 0x090200d0, 0x0a020020, 0x0a020d20, 0x0be1ea10,
            0x0befeb10, 0x0a0000f0, 0x0a1012e0, 0x09102200, 0x09102200, 0x0a1012e0, 0x0a0000f0, 0x0befeb10,
            0x0bffeb00, 0x0a001100, 0x0a101200, 0x09102330, 0x09102330, 0x0a101200, 0x0a001100, 0x0bffeb00,
            0x0dffeb10, 0x0b000010, 0x0b102220, 0x0a102340, 0x0a102340, 0x0b102220, 0x0b000010, 0x0dffeb10,
            0x0eefeb20, 0x0d102120, 0x0d102230, 0x0d102250, 0x0d102250, 0x0d102230, 0x0d002120, 0x0eefeb20,
            0x03efea70, 0x03001d70, 0x00100070, 0x00000170, 0x00000170, 0x00000070, 0x03001d70, 0x03efea70,
            0x03d0d900, 0x05e0ea00, 0x02e0eb00, 0x00f1eb00, 0x00f1eb00, 0x02e0eb00, 0x05e0ea00, 0x03d0d900
    };

    int side();

    void side(int side);

    int ply();

    void ply(int ply);

    int start();
    void start(int start);
    int count();
    void count(int count);

    int weight(long idx);

    void weight(long idx, int weight);

    Schema<Control> schema = Schema.of(Control.class, control -> control
            .fields("ply", "side", "start","count").array("weight", 64)
    );

    static Control create(Accelerator acc) {
        var control = schema.allocate(acc);
        for (int i = 0; i < 64; i++) {
            control.weight(i, weightMap[i]);
        }
        return control;
    }
}
