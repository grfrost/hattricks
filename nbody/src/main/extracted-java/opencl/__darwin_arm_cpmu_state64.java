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
 * struct __darwin_arm_cpmu_state64 {
 *     __uint64_t __ctrs[16];
 * }
 * }
 */
public class __darwin_arm_cpmu_state64 {

    __darwin_arm_cpmu_state64() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        MemoryLayout.sequenceLayout(16, opencl_h.C_LONG_LONG).withName("__ctrs")
    ).withName("__darwin_arm_cpmu_state64");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final SequenceLayout __ctrs$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("__ctrs"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * __uint64_t __ctrs[16]
     * }
     */
    public static final SequenceLayout __ctrs$layout() {
        return __ctrs$LAYOUT;
    }

    private static final long __ctrs$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * __uint64_t __ctrs[16]
     * }
     */
    public static final long __ctrs$offset() {
        return __ctrs$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * __uint64_t __ctrs[16]
     * }
     */
    public static MemorySegment __ctrs(MemorySegment struct) {
        return struct.asSlice(__ctrs$OFFSET, __ctrs$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * __uint64_t __ctrs[16]
     * }
     */
    public static void __ctrs(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, __ctrs$OFFSET, __ctrs$LAYOUT.byteSize());
    }

    private static long[] __ctrs$DIMS = { 16 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * __uint64_t __ctrs[16]
     * }
     */
    public static long[] __ctrs$dimensions() {
        return __ctrs$DIMS;
    }
    private static final VarHandle __ctrs$ELEM_HANDLE = __ctrs$LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * __uint64_t __ctrs[16]
     * }
     */
    public static long __ctrs(MemorySegment struct, long index0) {
        return (long)__ctrs$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * __uint64_t __ctrs[16]
     * }
     */
    public static void __ctrs(MemorySegment struct, long index0, long fieldValue) {
        __ctrs$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
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

