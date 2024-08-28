package chess;

import hat.Accelerator;
import hat.buffer.Buffer;
import hat.ifacemapper.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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

        int parent();

        void parent(int parent);

        int gameScore();
        void gameScore(int gameScore);
        short sideScore();
        void sideScore(short sideScore);
        short opponentScore();
        void opponentScore(short opponentScore);

        int firstChildIdx();
        void firstChildIdx(int firstChildIdx);

        byte moves();
        void moves(byte moves);

        // The move that got us here from the parent
        byte fromSqId();
        byte toSqId();
        void fromSqId(byte fromSqId);
        void toSqId(byte toSqId);

        // Our move number (relative to parent.firstChild)
        void move(byte move);
        byte move();

        default void firstPositions(){
                int x = 0;
                for (byte bits : new byte[]{ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK}) {
                    squareBits(x, (byte) (bits));
                    squareBits(x + 8, (byte) (PAWN));
                    for (int i = 16; i < 48; i += 8) {
                        squareBits(x + i, (byte) (EMPTY_SQUARE));
                    }
                    squareBits(x + 48, (byte) (WHITE_BIT | PAWN));
                    squareBits(x + 56, (byte) (WHITE_BIT | bits));
                    x++;
                }

            gameScore(0);  // The score after init is zero,
            sideScore((short)0);
            opponentScore((short)0);
            moves((byte)20);  // The number of moves available to white is 28 =  8 pawn, 4 knight
            firstChildIdx(0); // the first child will be 1
            fromSqId((byte)0);   // no move got us here,
            toSqId((byte)0);     // no move got us here
            move((byte)0);    // no move got us here
        }

        default void select(Board board){
            for (int sqid = 0; sqid < 64; sqid++) {
                squareBits(sqid, board.squareBits(sqid));
            }
            fromSqId(board.fromSqId());
            toSqId(board.toSqId());
            gameScore(board.gameScore());
            sideScore(board.sideScore());
            opponentScore(board.opponentScore());
            moves((byte) board.moves());
            parent(0);
           // System.out.print("\033[H\033[2J");
            //System.out.flush();
            System.out.println(new Terminal().board(this, 0));
        }
    }

    int length();

    Board board(long idx);

    Schema<ChessData> schema = Schema.of(ChessData.class, chessData -> chessData
            .arrayLen("length")//.pad(4)  // must be 4 if array has a long ?
            .array("board", square -> square
                    .array("squareBits", 64)
                    //              4         4               1       1           1        1       2           2                 4
                    .fields("parent", "firstChildIdx","moves","fromSqId","toSqId", "move", "sideScore","opponentScore", "gameScore")
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

    class BoardAndId{
        Board board;
        int id;
        public BoardAndId(Board board, int id) {
            this.board = board;
            this.id = id;
        }
    }

    default Stack<BoardAndId> getPath(int boardId){
        Stack<BoardAndId> path = new Stack<>();
        do {
            path.push(new BoardAndId(board(boardId), boardId));
            boardId = path.peek().board.parent();
        }while (boardId != 0);
        path.push(new BoardAndId(board(boardId), boardId));
        return path;
    }
}
