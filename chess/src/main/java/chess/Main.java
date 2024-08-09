package chess;


import hat.Accelerator;
import hat.ComputeContext;
import hat.KernelContext;
import hat.backend.Backend;
import hat.buffer.Buffer;
import hat.ifacemapper.Schema;

import java.lang.invoke.MethodHandles;
import java.lang.runtime.CodeReflection;

import static chess.ChessConstants.ALL_POINTS;
import static chess.ChessConstants.BISHOP_VALUE;
import static chess.ChessConstants.BLACK_BISHOP;
import static chess.ChessConstants.BLACK_KING;
import static chess.ChessConstants.BLACK_KNIGHT;
import static chess.ChessConstants.BLACK_PAWN;
import static chess.ChessConstants.BLACK_QUEEN;
import static chess.ChessConstants.BLACK_ROOK;
import static chess.ChessConstants.COLROWS;
import static chess.ChessConstants.DIAGS;
import static chess.ChessConstants.EMPTY_SQUARE;
import static chess.ChessConstants.ROOK_VALUE;
import static chess.ChessConstants.WHITE_BISHOP;
import static chess.ChessConstants.WHITE_KING;
import static chess.ChessConstants.WHITE_KNIGHT;
import static chess.ChessConstants.WHITE_PAWN;
import static chess.ChessConstants.WHITE_QUEEN;
import static chess.ChessConstants.WHITE_ROOK;
import static chess.ChessConstants.CompassDxDyMap;
import static chess.Terminal.algebraic;
import static chess.Terminal.piece;


public class Main {
    public static class Compute {

        @CodeReflection
        public static boolean isEmpty(byte squareBits) {
            return (squareBits & ChessConstants.PIECE_MASK) == 0;
        }

        @CodeReflection
        static boolean isPiece(int squareBits) {
            return (squareBits & ChessConstants.PIECE_MASK) != 0;
        }

        @CodeReflection
        public static boolean isWhite(byte squareBits) {
            return isPiece(squareBits) && ((squareBits & ChessConstants.WHITE_BIT) == ChessConstants.WHITE_BIT);
        }

        @CodeReflection
        public static boolean isBlack(byte squareBits) {
            return isPiece(squareBits) && ((squareBits & ChessConstants.WHITE_BIT) != ChessConstants.WHITE_BIT);
        }

        @CodeReflection
        public static boolean isOpponent(byte fromBits, byte toBits) {
            if (isPiece(toBits)) {
                int fromColorBit = (fromBits & ChessConstants.WHITE_BIT);
                int toColorBit = (toBits & ChessConstants.WHITE_BIT);
                return fromColorBit != toColorBit;
            }
            return false;
        }

        @CodeReflection
        public static boolean isEmptyOrOpponent(byte fromBits, byte toBits) {
            return isEmpty(toBits) || isOpponent(fromBits, toBits);
        }

        @CodeReflection
        public static boolean isComrade(byte fromBits, byte toBits) {
            if (isPiece(toBits)) {
                int fromColorBit = (fromBits & ChessConstants.WHITE_BIT);
                int toColorBit = (toBits & ChessConstants.WHITE_BIT);
                return fromColorBit == toColorBit;
            }
            return false;
        }


        @CodeReflection
        static boolean isOnBoard(int x, int y) {
            return (x < 8 && y < 8 && x >= 0 && y >= 0);
        }

        @CodeReflection
        static boolean isOffBoard(int x, int y) {
            return (x < 0 || y < 0 || x > 7 || y > 7);
        }

        @CodeReflection
        static boolean isPawn(int squareBits) {
            return (squareBits & ChessConstants.PIECE_MASK) == ChessConstants.PAWN_VALUE;
        }

        @CodeReflection
        static boolean isKnight(int squareBits) {
            return (squareBits & ChessConstants.PIECE_MASK) == ChessConstants.KNIGHT_VALUE;
        }

        @CodeReflection
        static boolean isBishop(int squareBits) {
            return (squareBits & ChessConstants.PIECE_MASK) == BISHOP_VALUE;
        }

        @CodeReflection
        static boolean isRook(int squareBits) {
            return (squareBits & ChessConstants.PIECE_MASK) == ROOK_VALUE;
        }

        @CodeReflection
        static boolean isKing(int squareBits) {
            return (squareBits & ChessConstants.PIECE_MASK) == ChessConstants.KING_VALUE;
        }


        @CodeReflection
        static boolean isQueen(int squareBits) {
            return (squareBits & ChessConstants.PIECE_MASK) == ChessConstants.QUEEN_VALUE;
        }


        @CodeReflection
        static boolean isOffBoard(int squareBits) {
            return squareBits == ChessConstants.OFF_BOARD_SQUARE;
        }


        @CodeReflection
        public static void initTree(KernelContext kc, ChessData chessData) {
            if (kc.x < kc.maxX) {
                ChessData.Board board = chessData.board(kc.x);
                if (kc.x == 0) {
                    board.parent((short) -1);
                    board.firstChild((short) 10);
                } else if (kc.x < 10) {
                    board.parent((short) 0);
                    board.firstChild((short) ((kc.x - 1) * 10));  //1->10 2->20 etc
                }
            }
        }

        @CodeReflection
        static public void init(final ComputeContext cc, ChessData chessData) {
            cc.dispatchKernel(chessData.length(), kc -> Compute.initTree(kc, chessData));
        }
        @CodeReflection
        public static int validMoves(ChessData.Board board, byte fromBits, int fromx, int fromy, int moves, short[] movesArr) {
            int pieceValue = fromBits & ChessConstants.PIECE_MASK;
            if (pieceValue == ChessConstants.KING_VALUE) {
                for (int moveIdx = 7; moveIdx > 0; moveIdx--) {
                    int dxdy = 0b1111 & (CompassDxDyMap >>> (moveIdx * 4));
                    int dy = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                    dxdy >>>= 2;
                    int dx = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                    int tox = fromx + dx;
                    int toy = fromy + dy;
                    if (Compute.isOnBoard(tox, toy)) {
                        var toBits = board.squareBits(toy * 8 + tox);
                        if (Compute.isEmptyOrOpponent(fromBits, toBits)) {
                            movesArr[moves++] = (short) (fromx << 12 | fromy << 8 | tox << 4 | toy);
                        }
                    }
                }
            } else if (pieceValue == ChessConstants.PAWN_VALUE) {
                int forward = Compute.isWhite(fromBits) ? -1 : 1;
                int count = (fromy == (Compute.isWhite(fromBits) ? 6 : 1)) ? 4 : 3;  // four moves if home else three
                for (int moveIdx = 0; moveIdx < count; moveIdx++) {
                    int dxdy = 0b1111 & (ChessConstants.PawnDxDyMap >>> (moveIdx * 4));
                    int toy = fromy + (forward * ((dxdy & 0b11) - 1));
                    dxdy >>>= 2;
                    int tox = fromx + (dxdy & 0b11) - 1;
                    if (Compute.isOnBoard(tox, toy)) {
                        var toBits = board.squareBits(toy * 8 + tox);
                        if (((moveIdx > 1) && Compute.isEmpty(toBits)) || (moveIdx < 2) && Compute.isOpponent(fromBits, toBits)) {
                            movesArr[moves++] = (short) (fromx << 12 | fromy << 8 | tox << 4 | toy);
                        }
                    }
                }
            } else if (pieceValue == ChessConstants.KNIGHT_VALUE) {
                for (int moveIdx = 0; moveIdx < 8; moveIdx++) {
                    int dxdy = 0b1111 & (ChessConstants.KnightDxDyMap >>> (moveIdx * 4));
                    int toy = fromy + ((dxdy & 0b1)+1)*((dxdy&0b10)-1);
                    dxdy >>>= 2;
                    int tox = fromx + ((dxdy & 0b1)+1)*((dxdy&0b10)-1);
                    if (Compute.isOnBoard(tox, toy)) {
                        var toBits = board.squareBits(toy * 8 + tox);
                        if (Compute.isEmptyOrOpponent(fromBits,toBits)){
                            movesArr[moves++] = (short) (fromx << 12 | fromy << 8 | tox << 4 | toy);
                        }
                    }
                }
            } else {
                // sliders Rook, Queen, Bishop
                final int compassPoints = (pieceValue == BISHOP_VALUE) ? DIAGS : ((pieceValue == ROOK_VALUE) ? COLROWS : ALL_POINTS);

                // compassPoints have use 1 or 0 for bit for eligible move relative to fromx,fromy
                // We only encode the neighbours so the 3x3 grid has no center representation
                //                piece being moved
                //                     is here
                //                        v
                //              nw n ne e | w sw s se
                //                \ | | | | | | | /
                //                 \\ | | | | | //
                //                  ||| | v | |||
                //     diags   == 0b101_0___0_101
                //     colrows == 0b010_1___1_010
                //   allpoints == 0b111_1___1_111


                for (int moveIdx = 7; moveIdx > 0; moveIdx--) {
                    int moveBit = (1 << moveIdx); // so 0b10000000 -> 0b01000000 -> ... 0b00000001
                    // check if we can move this way
                    // i.e is bit is set for this compassPoint
                    if ((compassPoints & moveBit) == moveBit) {
                        // Now let's determine what a move in this dir looks like
                        int dxdy = 0b1111 & (CompassDxDyMap >>> (moveIdx * 4));
                        int dy = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                        dxdy >>>= 2;
                        int dx = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                        boolean blocked = false;
                        for (int slide = 1; !blocked && slide < 8; slide++) { //1,2,3,4,5,6,7
                            int tox = fromx + slide * dx;
                            int toy = fromy + slide * dy;
                            blocked = isOffBoard(tox, toy);
                            if (!blocked){
                                var toBits = board.squareBits(toy * 8 + tox);
                                if (isEmptyOrOpponent(fromBits, toBits)) {
                                    movesArr[moves++] = (short) (fromx << 12 | fromy << 8 | tox << 4 | toy);
                                    if (isOpponent(toBits, fromBits)) {
                                       blocked=true;
                                    }
                                }else{
                                    blocked=true;
                                }
                            }
                        }
                    }
                }
            }
            return moves;

        }

    }

    public interface ChessData extends Buffer {
        interface Board extends Buffer.Struct {


            byte squareBits(long idx);

            void squareBits(long idx, byte squareBits);

            short parent();

            void parent(short parent);

            short firstChild();

            void firstChild(short firstChild);

            short score();

            void score(short score);

            short spare();

            void spare(short spare);

            default void row(int row, byte... pieces) {
                int x = row * 8;
                if (pieces.length == 1) {
                    for (int i = 0; i < 8; i++) {
                        squareBits(x++, pieces[0]);
                    }
                } else {
                    for (byte piece : pieces) {
                        squareBits(x++, piece);
                    }
                }
            }

            default Board init() {
                int x = 0;
                row(0, BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_QUEEN, BLACK_KING, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK);
                row(1, BLACK_PAWN);
                row(2, EMPTY_SQUARE);
                row(3, EMPTY_SQUARE);
                row(4, EMPTY_SQUARE);
                row(5, EMPTY_SQUARE);
                row(6, WHITE_PAWN);
                row(7, WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP, WHITE_QUEEN, WHITE_KING, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK);
                return this;
            }
        }

        int length();

        ChessData.Board board(long idx);

        Schema<ChessData> schema = Schema.of(ChessData.class, chessData -> chessData
                .arrayLen("length").array("board", square -> square
                        .array("squareBits", 64).fields("parent", "firstChild", "score", "spare")
                )
        );

        static ChessData create(Accelerator acc, int length) {
            return schema.allocate(acc, length);
        }
    }

    static class ChessDataImpl implements ChessData {
        static class BoardImpl implements ChessData.Board {
            byte[] squareBitArr = new byte[64];
            short parent;
            short firstChild;
            short score;
            short spare;

            @Override
            public byte squareBits(long idx) {
                return squareBitArr[(int) idx];
            }

            @Override
            public void squareBits(long idx, byte squareBits) {
                this.squareBitArr[(int) idx] = squareBits;
            }

            @Override
            public short parent() {
                return parent;
            }

            @Override
            public void parent(short parent) {
                this.parent = parent;
            }

            @Override
            public short firstChild() {
                return firstChild;
            }

            @Override
            public void firstChild(short firstChild) {
                this.firstChild = firstChild;
            }

            @Override
            public short score() {
                return this.score;
            }

            @Override
            public void score(short score) {
                this.score = score;
            }

            @Override
            public short spare() {
                return spare;
            }

            @Override
            public void spare(short spare) {
                this.spare = spare;
            }

        }

        final Board[] boards;

        @Override
        public int length() {
            return boards.length;
        }

        @Override
        public Board board(long idx) {
            return boards[(int) idx];
        }

        ChessDataImpl(int length) {
            this.boards = new Board[length];
            for (int i = 0; i < length; i++) {
                this.boards[i] = new BoardImpl();
            }
        }
    }

    public static void main(String[] args) {
        boolean headless = Boolean.getBoolean("headless") || (args.length > 0 && args[0].equals("--headless"));
         Accelerator accelerator = new Accelerator(MethodHandles.lookup(), Backend.FIRST);
        ChessData chessData = //new ChessDataImpl(10001);
        ChessData.create(accelerator, 10001);//,101,1001,10001
        accelerator.compute(cc -> Compute.init(cc, chessData));
        ChessData.Board board = chessData.board(0).init();

        short[] movesArr = new short[64];
        int moves = 0;
        byte side = ChessConstants.WHITE_BIT;
        for (int i = 0; i < 64; i++) {
            byte squareBits = board.squareBits(i);
            if (Compute.isComrade(side, squareBits)) {
                moves = Compute.validMoves(board, squareBits,  i % 8, i / 8, moves, movesArr);
            }
        }
        System.out.println(new Terminal().board(board));
        for (int moveIdx = 0; moveIdx < moves; moveIdx++) {
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
