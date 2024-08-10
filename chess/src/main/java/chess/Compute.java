package chess;

import hat.ComputeContext;
import hat.KernelContext;

import java.lang.runtime.CodeReflection;

import static chess.ChessConstants.ALL_POINTS;
import static chess.ChessConstants.BISHOP;
import static chess.ChessConstants.COLROWS;
import static chess.ChessConstants.CompassDxDyMap;
import static chess.ChessConstants.DIAGS;
import static chess.ChessConstants.EMPTY_SQUARE;
import static chess.ChessConstants.PAWN;
import static chess.ChessConstants.PIECE_MASK;
import static chess.ChessConstants.QUEEN;
import static chess.ChessConstants.ROOK;
import static chess.ChessConstants.WHITE_BIT;

public class Compute {

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
        return (squareBits & ChessConstants.PIECE_MASK) == ChessConstants.PAWN;
    }

    @CodeReflection
    static boolean isKnight(int squareBits) {
        return (squareBits & ChessConstants.PIECE_MASK) == ChessConstants.KNIGHT;
    }

    @CodeReflection
    static boolean isBishop(int squareBits) {
        return (squareBits & ChessConstants.PIECE_MASK) == BISHOP;
    }

    @CodeReflection
    static boolean isRook(int squareBits) {
        return (squareBits & ChessConstants.PIECE_MASK) == ROOK;
    }

    @CodeReflection
    static boolean isKing(int squareBits) {
        return (squareBits & ChessConstants.PIECE_MASK) == ChessConstants.KING;
    }


    @CodeReflection
    static boolean isQueen(int squareBits) {
        return (squareBits & ChessConstants.PIECE_MASK) == ChessConstants.QUEEN;
    }



    @CodeReflection
    public static int validMoves(ChessData chessData, ChessData.Board board, byte fromBits, int fromx, int fromy, int moves, short[] movesArr) {
        int pieceValue = fromBits & ChessConstants.PIECE_MASK;
        if (pieceValue == ChessConstants.KING) {
            for (int moveIdx = 7; moveIdx > 0; moveIdx--) {
                int dxdy = 0b1111 & (CompassDxDyMap >>> (moveIdx * 4));
                int dy = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                dxdy >>>= 2;
                int dx = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                int tox = fromx + dx;
                int toy = fromy + dy;
                if (isOnBoard(tox, toy)) {
                    var toBits = board.squareBits(toy * 8 + tox);
                    if (isEmptyOrOpponent(fromBits, toBits)) {
                        movesArr[moves++] = (short) (fromx << 12 | fromy << 8 | tox << 4 | toy);
                    }
                }
            }
        } else if (pieceValue == ChessConstants.PAWN) {
            int forward = isWhite(fromBits) ? -1 : 1;
            int count = (fromy == (isWhite(fromBits) ? 6 : 1)) ? 4 : 3;  // four moves if home else three
            for (int moveIdx = 0; moveIdx < count; moveIdx++) {
                int dxdy = 0b1111 & (ChessConstants.PawnDxDyMap >>> (moveIdx * 4));
                int toy = fromy + (forward * ((dxdy & 0b11) - 1));
                dxdy >>>= 2;
                int tox = fromx + (dxdy & 0b11) - 1;
                if (isOnBoard(tox, toy)) {
                    var toBits = board.squareBits(toy * 8 + tox);
                    if (((moveIdx > 1) && isEmpty(toBits)) || (moveIdx < 2) && isOpponent(fromBits, toBits)) {
                        movesArr[moves++] = (short) (fromx << 12 | fromy << 8 | tox << 4 | toy);
                    }
                }
            }
        } else if (pieceValue == ChessConstants.KNIGHT) {
            for (int moveIdx = 0; moveIdx < 8; moveIdx++) {
                int dxdy = 0b1111 & (ChessConstants.KnightDxDyMap >>> (moveIdx * 4));
                int toy = fromy + ((dxdy & 0b1) + 1) * ((dxdy & 0b10) - 1);
                dxdy >>>= 2;
                int tox = fromx + ((dxdy & 0b1) + 1) * ((dxdy & 0b10) - 1);
                if (isOnBoard(tox, toy)) {
                    var toBits = board.squareBits(toy * 8 + tox);
                    if (isEmptyOrOpponent(fromBits, toBits)) {
                        movesArr[moves++] = (short) (fromx << 12 | fromy << 8 | tox << 4 | toy);
                    }
                }
            }
        } else {
            // sliders Rook, Queen, Bishop
            final int compassPoints = (pieceValue == BISHOP) ? DIAGS : ((pieceValue == ROOK) ? COLROWS : ALL_POINTS);

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
                        if (!blocked) {
                            var toBits = board.squareBits(toy * 8 + tox);
                            if (isEmptyOrOpponent(fromBits, toBits)) {
                                movesArr[moves++] = (short) (fromx << 12 | fromy << 8 | tox << 4 | toy);
                                if (isOpponent(toBits, fromBits)) {
                                    blocked = true;
                                }
                            } else {
                                blocked = true;
                            }
                        }
                    }
                }
            }
        }
        return moves;

    }

    @CodeReflection
    public static int countMoves(ChessData.Board board, byte fromBits, int fromx, int fromy) {
        int moves = 0;
        int pieceValue = fromBits & ChessConstants.PIECE_MASK;
        if (pieceValue == ChessConstants.KING) {
            for (int moveIdx = 7; moveIdx > 0; moveIdx--) {
                int dxdy = 0b1111 & (CompassDxDyMap >>> (moveIdx * 4));
                int dy = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                dxdy >>>= 2;
                int dx = (dxdy & 0b11) - 1;// 00->-1, 01->0, 10->1, 11->2
                int tox = fromx + dx;
                int toy = fromy + dy;
                if (isOnBoard(tox, toy)) {
                    var toBits = board.squareBits(toy * 8 + tox);
                    if (isEmptyOrOpponent(fromBits, toBits)) {
                        moves++;
                    }
                }
            }
        } else if (pieceValue == ChessConstants.PAWN) {
            int forward = isWhite(fromBits) ? -1 : 1;
            int count = (fromy == (isWhite(fromBits) ? 6 : 1)) ? 4 : 3;  // four moves if home else three
            for (int moveIdx = 0; moveIdx < count; moveIdx++) {
                int dxdy = 0b1111 & (ChessConstants.PawnDxDyMap >>> (moveIdx * 4));
                int toy = fromy + (forward * ((dxdy & 0b11) - 1));
                dxdy >>>= 2;
                int tox = fromx + (dxdy & 0b11) - 1;
                if (isOnBoard(tox, toy)) {
                    var toBits = board.squareBits(toy * 8 + tox);
                    if (((moveIdx > 1) && isEmpty(toBits)) || (moveIdx < 2) && isOpponent(fromBits, toBits)) {
                        moves++;
                    }
                }
            }
        } else if (pieceValue == ChessConstants.KNIGHT) {
            for (int moveIdx = 0; moveIdx < 8; moveIdx++) {
                int dxdy = 0b1111 & (ChessConstants.KnightDxDyMap >>> (moveIdx * 4));
                int toy = fromy + ((dxdy & 0b1) + 1) * ((dxdy & 0b10) - 1);
                dxdy >>>= 2;
                int tox = fromx + ((dxdy & 0b1) + 1) * ((dxdy & 0b10) - 1);
                if (isOnBoard(tox, toy)) {
                    var toBits = board.squareBits(toy * 8 + tox);
                    if (isEmptyOrOpponent(fromBits, toBits)) {
                        moves++;
                    }
                }
            }
        } else {
            // sliders Rook, Queen, Bishop
            final int compassPoints = (pieceValue == BISHOP) ? DIAGS : ((pieceValue == ROOK) ? COLROWS : ALL_POINTS);

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
                        if (!blocked) {
                            var toBits = board.squareBits(toy * 8 + tox);
                            if (isEmptyOrOpponent(fromBits, toBits)) {
                                moves++;
                                if (isOpponent(toBits, fromBits)) {
                                    blocked = true;
                                }
                            } else {
                                blocked = true;
                            }
                        }
                    }
                }
            }
        }
        return moves;

    }

    @CodeReflection
    public static void countMovesKernelCore(int id, ChessData chessData, Control control) {

            ChessData.Board board = chessData.board(id);
            byte side = (byte)control.side();
            int moves=0;
            int score = 0;
            for (int i = 0; i < 64; i++) {
                byte squareBits = board.squareBits(i);

                int piece = squareBits & PIECE_MASK;
                if (piece != EMPTY_SQUARE) {
                    int scoreMul = -1;
                    if (isComrade(side, squareBits)) {
                        int pieceMoves = countMoves(board, squareBits, i % 8, i / 8);
                        moves += pieceMoves;
                        if (piece != QUEEN) {
                            // Queen moves can overflow 4 bits.  I believe this is the only one that can.
                            // So to determine Queen moves
                            //     Qmove s = board moves - (sum of piece moves)
                            squareBits |= (byte) (pieceMoves << 4);
                        }
                        scoreMul = 1;
                        board.squareBits(i, squareBits);
                    }
                    // white and black pawns have separate weight slots wp=1 bp =0
                    if (piece==PAWN && isBlack(squareBits)){
                        piece--;
                    }
                    // now the piece value can be used an index into the weights
                    int weights = control.weight(i);
                    int shifted = (weights>>>piece*4)&0xf;
                    shifted *= scoreMul;
                    score+=shifted;
                }

            }
            board.moves((short) moves);
            board.score((short) score);

    }


    @CodeReflection
    public static void countMovesKernel(KernelContext kc, ChessData chessData, Control control) {
        if (kc.x < kc.maxX) {
            countMovesKernelCore(kc.x, chessData, control);
        }
    }
    @CodeReflection
    static public void countMovesCompute(final ComputeContext cc, ChessData chessData, Control control) {
        cc.dispatchKernel(1, kc -> countMovesKernel(kc, chessData,control));
    }

    @CodeReflection
    public static void initTree(KernelContext kc, ChessData chessData) {
        if (kc.x < kc.maxX) {
            ChessData.Board board = chessData.board(kc.x);
        }
    }

    @CodeReflection
    static public void init(final ComputeContext cc, ChessData chessData) {
        cc.dispatchKernel(chessData.length(), kc -> initTree(kc, chessData));
    }
}
