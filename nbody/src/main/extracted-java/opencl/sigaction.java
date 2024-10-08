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
 * struct sigaction {
 *     union __sigaction_u __sigaction_u;
 *     sigset_t sa_mask;
 *     int sa_flags;
 * }
 * }
 */
public class sigaction {

    sigaction() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        __sigaction_u.layout().withName("__sigaction_u"),
        opencl_h.C_INT.withName("sa_mask"),
        opencl_h.C_INT.withName("sa_flags")
    ).withName("sigaction");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final GroupLayout __sigaction_u$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("__sigaction_u"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * union __sigaction_u __sigaction_u
     * }
     */
    public static final GroupLayout __sigaction_u$layout() {
        return __sigaction_u$LAYOUT;
    }

    private static final long __sigaction_u$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * union __sigaction_u __sigaction_u
     * }
     */
    public static final long __sigaction_u$offset() {
        return __sigaction_u$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * union __sigaction_u __sigaction_u
     * }
     */
    public static MemorySegment __sigaction_u(MemorySegment struct) {
        return struct.asSlice(__sigaction_u$OFFSET, __sigaction_u$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * union __sigaction_u __sigaction_u
     * }
     */
    public static void __sigaction_u(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, __sigaction_u$OFFSET, __sigaction_u$LAYOUT.byteSize());
    }

    private static final OfInt sa_mask$LAYOUT = (OfInt)$LAYOUT.select(groupElement("sa_mask"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * sigset_t sa_mask
     * }
     */
    public static final OfInt sa_mask$layout() {
        return sa_mask$LAYOUT;
    }

    private static final long sa_mask$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * sigset_t sa_mask
     * }
     */
    public static final long sa_mask$offset() {
        return sa_mask$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * sigset_t sa_mask
     * }
     */
    public static int sa_mask(MemorySegment struct) {
        return struct.get(sa_mask$LAYOUT, sa_mask$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * sigset_t sa_mask
     * }
     */
    public static void sa_mask(MemorySegment struct, int fieldValue) {
        struct.set(sa_mask$LAYOUT, sa_mask$OFFSET, fieldValue);
    }

    private static final OfInt sa_flags$LAYOUT = (OfInt)$LAYOUT.select(groupElement("sa_flags"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int sa_flags
     * }
     */
    public static final OfInt sa_flags$layout() {
        return sa_flags$LAYOUT;
    }

    private static final long sa_flags$OFFSET = 12;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int sa_flags
     * }
     */
    public static final long sa_flags$offset() {
        return sa_flags$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int sa_flags
     * }
     */
    public static int sa_flags(MemorySegment struct) {
        return struct.get(sa_flags$LAYOUT, sa_flags$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int sa_flags
     * }
     */
    public static void sa_flags(MemorySegment struct, int fieldValue) {
        struct.set(sa_flags$LAYOUT, sa_flags$OFFSET, fieldValue);
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

