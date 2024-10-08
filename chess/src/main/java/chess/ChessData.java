package chess;

import hat.Accelerator;
import hat.buffer.Buffer;
import hat.ifacemapper.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static chess.ChessConstants.BISHOP;
import static chess.ChessConstants.BLACK_BIT;
import static chess.ChessConstants.BLACK_PAWN;
import static chess.ChessConstants.EMPTY_SQUARE;
import static chess.ChessConstants.KING;
import static chess.ChessConstants.KNIGHT;
import static chess.ChessConstants.PAWN;
import static chess.ChessConstants.QUEEN;
import static chess.ChessConstants.ROOK;
import static chess.ChessConstants.WHITE_BIT;
import static chess.ChessConstants.WHITE_PAWN;

public interface ChessData extends Buffer {
    interface Board extends Struct {

        byte squareBits(long idx);
        void squareBits(long idx, byte squareBits);
        int id();
        void id(int id);
        int parent();
        void parent(int parent);
        int score();
        void score(int score);
      /*  short sideScore();
        void sideScore(short sideScore);
        short opponentScore();
        void opponentScore(short opponentScore); */
        int firstChildIdx();
        void firstChildIdx(int firstChildIdx);
        byte moves();
        void moves(byte moves);
        // The move that got us here  (parent relative)
        byte fromSqId();
        byte toSqId();
        void fromSqId(byte fromSqId);
        void toSqId(byte toSqId);
        // Our move number (parent.firstChild relative)
        void move(byte move);
        byte move();

        default void firstPositions(){
                int x = 0;
                for (byte bits : new byte[]{ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK}) {
                    squareBits(x + 0, (byte) (BLACK_BIT|bits));
                    squareBits(x + 8, BLACK_PAWN);
                    squareBits(x + 16, EMPTY_SQUARE);
                    squareBits(x + 24, EMPTY_SQUARE);
                    squareBits(x + 32, EMPTY_SQUARE);
                    squareBits(x + 40, EMPTY_SQUARE);
                    squareBits(x + 48, WHITE_PAWN);
                    squareBits(x + 56, (byte) (WHITE_BIT | bits));
                    x++;
                }
                id(0);
            parent(0);
            score(0);  // The score after init is zero,
            moves((byte)20);  // The number of moves available to white is 28 =  8 pawn, 4 knight
            firstChildIdx(1); // the first child will be 1
            fromSqId((byte)0);   // no move got us here,
            toSqId((byte)0);     // no move got us here
            move((byte)0);    // no move got us here
            System.out.println(BoardRenderer.unicode(this));
        }

        default void select(Board board){
            for (int sqid = 0; sqid < 64; sqid++) {
                squareBits(sqid, board.squareBits(sqid));
            }
            firstChildIdx(1);
            fromSqId(board.fromSqId());
            toSqId(board.toSqId());
            score(board.score());
            moves(board.moves());
            id(0);
            parent(0);
            System.out.println(BoardRenderer.unicode(this));
        }


    }

    int length();

    Board board(long idx);

    Schema<ChessData> schema = Schema.of(ChessData.class, chessData -> chessData
            .arrayLen("length")//.pad(4)  // must be 4 if array has a long ?
            .array("board", square -> square
                    .array("squareBits", 64)
                    //              4     4        4        4               1       1           1        1
                    .fields("id", "parent", "score","firstChildIdx","moves","fromSqId","toSqId", "move")
                   // .pad(2) //80
            )

    );

    static ChessData create(Accelerator acc, int plyGuess, int ply) {
        int length = 1;
        int plyPow=plyGuess;
        for (int i = 1; i < ply; i++) {
            length+=plyPow;
            plyPow*=plyGuess;
        }
        return schema.allocate(acc, length);
    }



}
