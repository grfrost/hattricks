package chess;


import hat.Accelerator;
import hat.backend.Backend;
import hat.buffer.Buffer;

import java.lang.invoke.MethodHandles;

import static chess.ChessConstants.BISHOP;
import static chess.ChessConstants.EMPTY_SQUARE;
import static chess.ChessConstants.KING;
import static chess.ChessConstants.KNIGHT;
import static chess.ChessConstants.PAWN;
import static chess.ChessConstants.QUEEN;
import static chess.ChessConstants.ROOK;
import static chess.ChessConstants.WHITE_BIT;
import static chess.Terminal.algebraic;
import static chess.Terminal.piece;


public class Main {




    public static void main(String[] args) {
        boolean headless = Boolean.getBoolean("headless") || (args.length > 0 && args[0].equals("--headless"));
         Accelerator accelerator = new Accelerator(MethodHandles.lookup(), Backend.FIRST);
         //3 ply requires 64+64*64+64*64*64
         int ply3 = 1+64+64*64+64*64*64;
        Control control =  Control.create(accelerator);
        ChessData chessData =  ChessData.create(accelerator, ply3);
        System.out.println(Buffer.getMemorySegment(chessData).byteSize()+" bytes ");
        accelerator.compute(cc -> Compute.init(cc, chessData));

        ChessData.Board board = chessData.board(0);
        int x=0;
        for (byte bits :new byte[]{ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK}){
            board.squareBits(x,(byte) (bits));
            board.squareBits(x+8,(byte) (PAWN));
            board.squareBits(x+16,(byte) (EMPTY_SQUARE));
            board.squareBits(x+24,(byte) (EMPTY_SQUARE));
            board.squareBits(x+32,(byte) (EMPTY_SQUARE));
            board.squareBits(x+48,(byte) (EMPTY_SQUARE));
            board.squareBits(x+48,(byte) (WHITE_BIT|PAWN));
            board.squareBits(x+56,(byte) (WHITE_BIT|bits));
            x++;
        }
        control.ply(0);
        control.side(WHITE_BIT);
        accelerator.compute(cc-> Compute.countMovesCompute(cc, chessData,control));
        System.out.println(" found "+board.moves());
/*
        int moves =0;
        byte side = ChessConstants.WHITE_BIT;
        for (int i = 0; i < 64; i++) {
            byte squareBits = board.squareBits(i);
            if (Compute.isComrade(side, squareBits)) {
                moves+=Compute.countMoves(board, squareBits,  i % 8, i / 8);
            }
        }
        board.moves((short)moves);
*/
        short[] movesArr = new short[board.moves()];
        int movec = 0;
        for (int i = 0; i < 64; i++) {
            byte squareBits = board.squareBits(i);
            if (Compute.isComrade((byte)control.side(), squareBits)) {
                movec = Compute.validMoves(chessData, board, squareBits,  i % 8, i / 8, movec, movesArr);
            }
        }
        System.out.println(new Terminal().board(board));
        System.out.println("Score = "+board.score());
        for (int moveIdx = 0; moveIdx < board.moves(); moveIdx++) {
            int move = movesArr[moveIdx];
            int fromx = (move >>> 12) & 0xf;
            int fromy = (move >>> 8) & 0xf;
            int from = fromy * 8 + fromx;
            int tox = (move >>> 4) & 0xf;
            int toy = (move >>> 0) & 0xf;
            int to = toy * 8 + tox;
            byte fromBits = board.squareBits(from);
            byte toBits = board.squareBits(to);
            if (Compute.isPiece(toBits)) {
                System.out.println(piece(fromBits) + "@" + algebraic(fromx, fromy) + " x " + piece(toBits) + " @" + algebraic(tox, toy));
            } else {
                System.out.println(piece(fromBits) + "@" + algebraic(fromx, fromy) + " -> @" + algebraic(tox, toy));
            }
        }
    }
}
