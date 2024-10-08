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
 * struct flock {
 *     off_t l_start;
 *     off_t l_len;
 *     pid_t l_pid;
 *     short l_type;
 *     short l_whence;
 * }
 * }
 */
public class flock {

    flock() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        opencl_h.C_LONG_LONG.withName("l_start"),
        opencl_h.C_LONG_LONG.withName("l_len"),
        opencl_h.C_INT.withName("l_pid"),
        opencl_h.C_SHORT.withName("l_type"),
        opencl_h.C_SHORT.withName("l_whence")
    ).withName("flock");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfLong l_start$LAYOUT = (OfLong)$LAYOUT.select(groupElement("l_start"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * off_t l_start
     * }
     */
    public static final OfLong l_start$layout() {
        return l_start$LAYOUT;
    }

    private static final long l_start$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * off_t l_start
     * }
     */
    public static final long l_start$offset() {
        return l_start$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * off_t l_start
     * }
     */
    public static long l_start(MemorySegment struct) {
        return struct.get(l_start$LAYOUT, l_start$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * off_t l_start
     * }
     */
    public static void l_start(MemorySegment struct, long fieldValue) {
        struct.set(l_start$LAYOUT, l_start$OFFSET, fieldValue);
    }

    private static final OfLong l_len$LAYOUT = (OfLong)$LAYOUT.select(groupElement("l_len"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * off_t l_len
     * }
     */
    public static final OfLong l_len$layout() {
        return l_len$LAYOUT;
    }

    private static final long l_len$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * off_t l_len
     * }
     */
    public static final long l_len$offset() {
        return l_len$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * off_t l_len
     * }
     */
    public static long l_len(MemorySegment struct) {
        return struct.get(l_len$LAYOUT, l_len$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * off_t l_len
     * }
     */
    public static void l_len(MemorySegment struct, long fieldValue) {
        struct.set(l_len$LAYOUT, l_len$OFFSET, fieldValue);
    }

    private static final OfInt l_pid$LAYOUT = (OfInt)$LAYOUT.select(groupElement("l_pid"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * pid_t l_pid
     * }
     */
    public static final OfInt l_pid$layout() {
        return l_pid$LAYOUT;
    }

    private static final long l_pid$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * pid_t l_pid
     * }
     */
    public static final long l_pid$offset() {
        return l_pid$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * pid_t l_pid
     * }
     */
    public static int l_pid(MemorySegment struct) {
        return struct.get(l_pid$LAYOUT, l_pid$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * pid_t l_pid
     * }
     */
    public static void l_pid(MemorySegment struct, int fieldValue) {
        struct.set(l_pid$LAYOUT, l_pid$OFFSET, fieldValue);
    }

    private static final OfShort l_type$LAYOUT = (OfShort)$LAYOUT.select(groupElement("l_type"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * short l_type
     * }
     */
    public static final OfShort l_type$layout() {
        return l_type$LAYOUT;
    }

    private static final long l_type$OFFSET = 20;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * short l_type
     * }
     */
    public static final long l_type$offset() {
        return l_type$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * short l_type
     * }
     */
    public static short l_type(MemorySegment struct) {
        return struct.get(l_type$LAYOUT, l_type$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * short l_type
     * }
     */
    public static void l_type(MemorySegment struct, short fieldValue) {
        struct.set(l_type$LAYOUT, l_type$OFFSET, fieldValue);
    }

    private static final OfShort l_whence$LAYOUT = (OfShort)$LAYOUT.select(groupElement("l_whence"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * short l_whence
     * }
     */
    public static final OfShort l_whence$layout() {
        return l_whence$LAYOUT;
    }

    private static final long l_whence$OFFSET = 22;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * short l_whence
     * }
     */
    public static final long l_whence$offset() {
        return l_whence$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * short l_whence
     * }
     */
    public static short l_whence(MemorySegment struct) {
        return struct.get(l_whence$LAYOUT, l_whence$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * short l_whence
     * }
     */
    public static void l_whence(MemorySegment struct, short fieldValue) {
        struct.set(l_whence$LAYOUT, l_whence$OFFSET, fieldValue);
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

