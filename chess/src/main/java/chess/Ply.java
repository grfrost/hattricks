package chess;

import hat.Accelerator;
import hat.buffer.Buffer;
import hat.ifacemapper.Schema;

public interface Ply extends Buffer {
    int id();

    void id(int id);

    int side();

    void side(int side);

    int fromBoardId();

    void fromBoardId(int fromBoardId);

    int toBoardId();

    void toBoardId(int toBoardId);

    int size();

    void size(int size);

    Schema<Ply> schema = Schema.of(Ply.class, ply -> ply
            .fields("id", "side", "fromBoardId", "toBoardId", "size")
    );

    static Ply create(Accelerator acc) {
        return schema.allocate(acc);
    }

    default void init(int id, int side, int fromBoardId, int size) {
        id(id);
        side(side);
        fromBoardId(fromBoardId);
        toBoardId(fromBoardId + size);
        size(size);
    }

    default String dump(ChessData chessData, String tag) {

        StringBuilder stringBuilder = new StringBuilder();
        for (int id = fromBoardId(); id < toBoardId(); id++) {
            ChessData.Board board = chessData.board(id);
            stringBuilder.append(id()).append(':').append(tag).append(BoardRenderer.line(board)).append('\n');
        }
        return stringBuilder.toString();

    }
}
