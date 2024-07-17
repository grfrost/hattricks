package life;

import hat.Accelerator;
import hat.buffer.Buffer;
import hat.buffer.BufferAllocator;
import hat.ifacemapper.Schema;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public interface LifeData extends Buffer {
    int width();

    void width(int width);

    int height();

    void height(int height);

    int length();

    void length(int length);

    byte array(long idx);

    void array(long idx, byte b);

    Schema<LifeData> schema = Schema.of(LifeData.class, lifeData -> lifeData
            .fields("width", "height")
            .arrayLen("length").array("array"));

    static LifeData create(MethodHandles.Lookup lookup, BufferAllocator bufferAllocator, int width, int height) {
        var instance = schema.allocate(lookup, bufferAllocator, width * height * 2);
        instance.length(width * height * 2);
        instance.width(width);
        instance.height(height);
        return instance;
    }

    static LifeData create(Accelerator accelerator, int width, int height) {
        return create(accelerator.lookup, accelerator, width, height);
    }

    ValueLayout valueLayout = JAVA_BYTE;
    long headerOffset = JAVA_INT.byteOffset() * 3;

    default LifeData copySliceTo(byte[] bytes, int to) {
        long offset = headerOffset + to* valueLayout.byteOffset();
        MemorySegment.copy(Buffer.getMemorySegment(this), valueLayout, offset, bytes, 0, length() / 2);
        return this;
    }
}
