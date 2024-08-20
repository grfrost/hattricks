package chess;

import hat.Accelerator;
import hat.buffer.Buffer;
import hat.ifacemapper.Schema;

public interface PlyTable extends Buffer {
     interface  Ply extends Buffer.Struct{
        int side();
        void side(int side);
        int startIdx();
        void startIdx(int plyStartIdx);
        int size();
        void size(int boards);
    }
    Ply ply(long idx);
    int max();
    int idx();
    void idx(int idx);
    Schema<PlyTable> schema = Schema.of(PlyTable.class, table -> table
            .field("idx").arrayLen("max").array("ply", ply->ply.fields("side", "startIdx", "size"))
    );

    static PlyTable create(Accelerator acc, int max) {
        return schema.allocate(acc, max);
    }
}
