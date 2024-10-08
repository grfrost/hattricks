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
 * struct fpunchhole {
 *     unsigned int fp_flags;
 *     unsigned int reserved;
 *     off_t fp_offset;
 *     off_t fp_length;
 * }
 * }
 */
public class fpunchhole {

    fpunchhole() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        opencl_h.C_INT.withName("fp_flags"),
        opencl_h.C_INT.withName("reserved"),
        opencl_h.C_LONG_LONG.withName("fp_offset"),
        opencl_h.C_LONG_LONG.withName("fp_length")
    ).withName("fpunchhole");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfInt fp_flags$LAYOUT = (OfInt)$LAYOUT.select(groupElement("fp_flags"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int fp_flags
     * }
     */
    public static final OfInt fp_flags$layout() {
        return fp_flags$LAYOUT;
    }

    private static final long fp_flags$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int fp_flags
     * }
     */
    public static final long fp_flags$offset() {
        return fp_flags$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int fp_flags
     * }
     */
    public static int fp_flags(MemorySegment struct) {
        return struct.get(fp_flags$LAYOUT, fp_flags$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int fp_flags
     * }
     */
    public static void fp_flags(MemorySegment struct, int fieldValue) {
        struct.set(fp_flags$LAYOUT, fp_flags$OFFSET, fieldValue);
    }

    private static final OfInt reserved$LAYOUT = (OfInt)$LAYOUT.select(groupElement("reserved"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned int reserved
     * }
     */
    public static final OfInt reserved$layout() {
        return reserved$LAYOUT;
    }

    private static final long reserved$OFFSET = 4;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned int reserved
     * }
     */
    public static final long reserved$offset() {
        return reserved$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned int reserved
     * }
     */
    public static int reserved(MemorySegment struct) {
        return struct.get(reserved$LAYOUT, reserved$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned int reserved
     * }
     */
    public static void reserved(MemorySegment struct, int fieldValue) {
        struct.set(reserved$LAYOUT, reserved$OFFSET, fieldValue);
    }

    private static final OfLong fp_offset$LAYOUT = (OfLong)$LAYOUT.select(groupElement("fp_offset"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * off_t fp_offset
     * }
     */
    public static final OfLong fp_offset$layout() {
        return fp_offset$LAYOUT;
    }

    private static final long fp_offset$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * off_t fp_offset
     * }
     */
    public static final long fp_offset$offset() {
        return fp_offset$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * off_t fp_offset
     * }
     */
    public static long fp_offset(MemorySegment struct) {
        return struct.get(fp_offset$LAYOUT, fp_offset$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * off_t fp_offset
     * }
     */
    public static void fp_offset(MemorySegment struct, long fieldValue) {
        struct.set(fp_offset$LAYOUT, fp_offset$OFFSET, fieldValue);
    }

    private static final OfLong fp_length$LAYOUT = (OfLong)$LAYOUT.select(groupElement("fp_length"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * off_t fp_length
     * }
     */
    public static final OfLong fp_length$layout() {
        return fp_length$LAYOUT;
    }

    private static final long fp_length$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * off_t fp_length
     * }
     */
    public static final long fp_length$offset() {
        return fp_length$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * off_t fp_length
     * }
     */
    public static long fp_length(MemorySegment struct) {
        return struct.get(fp_length$LAYOUT, fp_length$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * off_t fp_length
     * }
     */
    public static void fp_length(MemorySegment struct, long fieldValue) {
        struct.set(fp_length$LAYOUT, fp_length$OFFSET, fieldValue);
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

