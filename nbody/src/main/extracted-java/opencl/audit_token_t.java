// Generated by jextract

package opencl;

import java.lang.invoke.*;
import java.lang.foreign.*;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.lang.foreign.ValueLayout.*;
import static java.lang.foreign.MemoryLayout.PathElement.*;

/**
 * {@snippet lang=c :
 * struct {
 *     unsigned int val[8];
 * }
 * }
 */
public class audit_token_t {

    audit_token_t() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        MemoryLayout.sequenceLayout(8, opencl_h.C_INT).withName("val")
    ).withName("$anon$482:9");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final SequenceLayout val$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("val"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int val[8]
     * }
     */
    public static final SequenceLayout val$layout() {
        return val$LAYOUT;
    }

    private static final long val$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int val[8]
     * }
     */
    public static final long val$offset() {
        return val$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int val[8]
     * }
     */
    public static MemorySegment val(MemorySegment struct) {
        return struct.asSlice(val$OFFSET, val$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int val[8]
     * }
     */
    public static void val(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, val$OFFSET, val$LAYOUT.byteSize());
    }

    private static long[] val$DIMS = { 8 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * unsigned int val[8]
     * }
     */
    public static long[] val$dimensions() {
        return val$DIMS;
    }
    private static final VarHandle val$ELEM_HANDLE = val$LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * unsigned int val[8]
     * }
     */
    public static int val(MemorySegment struct, long index0) {
        return (int)val$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * unsigned int val[8]
     * }
     */
    public static void val(MemorySegment struct, long index0, int fieldValue) {
        val$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
    }

    /**
     * Obtains a slice of {@code arrayParam} which selects the array element at {@code index}.
     * The returned segment has address {@code arrayParam.address() + index * layout().byteSize()}
     */
    public static MemorySegment asSlice(MemorySegment array, long index) {
        return array.asSlice(layout().byteSize() * index);
    }

    /**
     * The size (in bytes) of this struct
     */
    public static long sizeof() { return layout().byteSize(); }

    /**
     * Allocate a segment of size {@code layout().byteSize()} using {@code allocator}
     */
    public static MemorySegment allocate(SegmentAllocator allocator) {
        return allocator.allocate(layout());
    }

    /**
     * Allocate an array of size {@code elementCount} using {@code allocator}.
     * The returned segment has size {@code elementCount * layout().byteSize()}.
     */
    public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
        return reinterpret(addr, 1, arena, cleanup);
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code elementCount * layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena, Consumer<MemorySegment> cleanup) {
        return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
    }
}

