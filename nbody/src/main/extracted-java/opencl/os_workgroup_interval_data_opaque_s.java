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
 * struct os_workgroup_interval_data_opaque_s {
 *     uint32_t sig;
 *     char opaque[56];
 * }
 * }
 */
public class os_workgroup_interval_data_opaque_s {

    os_workgroup_interval_data_opaque_s() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        opencl_h.C_INT.withName("sig"),
        MemoryLayout.sequenceLayout(56, opencl_h.C_CHAR).withName("opaque")
    ).withName("os_workgroup_interval_data_opaque_s");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfInt sig$LAYOUT = (OfInt)$LAYOUT.select(groupElement("sig"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint32_t sig
     * }
     */
    public static final OfInt sig$layout() {
        return sig$LAYOUT;
    }

    private static final long sig$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint32_t sig
     * }
     */
    public static final long sig$offset() {
        return sig$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint32_t sig
     * }
     */
    public static int sig(MemorySegment struct) {
        return struct.get(sig$LAYOUT, sig$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint32_t sig
     * }
     */
    public static void sig(MemorySegment struct, int fieldValue) {
        struct.set(sig$LAYOUT, sig$OFFSET, fieldValue);
    }

    private static final SequenceLayout opaque$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("opaque"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * char opaque[56]
     * }
     */
    public static final SequenceLayout opaque$layout() {
        return opaque$LAYOUT;
    }

    private static final long opaque$OFFSET = 4;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * char opaque[56]
     * }
     */
    public static final long opaque$offset() {
        return opaque$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * char opaque[56]
     * }
     */
    public static MemorySegment opaque(MemorySegment struct) {
        return struct.asSlice(opaque$OFFSET, opaque$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * char opaque[56]
     * }
     */
    public static void opaque(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, opaque$OFFSET, opaque$LAYOUT.byteSize());
    }

    private static long[] opaque$DIMS = { 56 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * char opaque[56]
     * }
     */
    public static long[] opaque$dimensions() {
        return opaque$DIMS;
    }
    private static final VarHandle opaque$ELEM_HANDLE = opaque$LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * char opaque[56]
     * }
     */
    public static byte opaque(MemorySegment struct, long index0) {
        return (byte)opaque$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * char opaque[56]
     * }
     */
    public static void opaque(MemorySegment struct, long index0, byte fieldValue) {
        opaque$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
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

