package chess;

import hat.Accelerator;
import hat.buffer.Buffer;
import hat.ifacemapper.Schema;

public interface PlyTable extends Buffer {
    interface Ply extends Buffer.Struct {
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

        default void init(int id, int side, int fromBoardId, int size) {
            id(id);
            side(side);
            fromBoardId(fromBoardId);
            toBoardId(fromBoardId + size);
            size(size);
        }
    }

    int length();
    Ply ply(long idx);
    int currentId();
    void currentId(int currentId);

    Schema<PlyTable> schema = Schema.of(PlyTable.class, plyTable -> plyTable
            .arrayLen("length").array("ply", ply->ply
                .fields("id", "side", "fromBoardId", "toBoardId", "size")
            ).field("currentId")
    );

    static PlyTable create(Accelerator acc, int length) {
        return schema.allocate(acc, length);
    }

}
