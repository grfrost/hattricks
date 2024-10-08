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
 *     uint64_t address;
 *     boolean_t deallocate : 8;
 *     mach_msg_copy_options_t copy : 8;
 *     unsigned int pad1 : 8;
 *     mach_msg_descriptor_type_t type : 8;
 *     mach_msg_size_t size;
 * }
 * }
 */
public class mach_msg_ool_descriptor64_t {

    mach_msg_ool_descriptor64_t() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        opencl_h.align(opencl_h.C_LONG_LONG, 4).withName("address"),
        MemoryLayout.paddingLayout(4),
        opencl_h.C_INT.withName("size")
    ).withName("$anon$312:9");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfLong address$LAYOUT = (OfLong)$LAYOUT.select(groupElement("address"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t address
     * }
     */
    public static final OfLong address$layout() {
        return address$LAYOUT;
    }

    private static final long address$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t address
     * }
     */
    public static final long address$offset() {
        return address$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t address
     * }
     */
    public static long address(MemorySegment struct) {
        return struct.get(address$LAYOUT, address$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t address
     * }
     */
    public static void address(MemorySegment struct, long fieldValue) {
        struct.set(address$LAYOUT, address$OFFSET, fieldValue);
    }

    private static final OfInt size$LAYOUT = (OfInt)$LAYOUT.select(groupElement("size"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * mach_msg_size_t size
     * }
     */
    public static final OfInt size$layout() {
        return size$LAYOUT;
    }

    private static final long size$OFFSET = 12;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * mach_msg_size_t size
     * }
     */
    public static final long size$offset() {
        return size$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * mach_msg_size_t size
     * }
     */
    public static int size(MemorySegment struct) {
        return struct.get(size$LAYOUT, size$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * mach_msg_size_t size
     * }
     */
    public static void size(MemorySegment struct, int fieldValue) {
        struct.set(size$LAYOUT, size$OFFSET, fieldValue);
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

