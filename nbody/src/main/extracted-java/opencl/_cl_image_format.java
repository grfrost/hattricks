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
 * struct _cl_image_format {
 *     cl_channel_order image_channel_order;
 *     cl_channel_type image_channel_data_type;
 * }
 * }
 */
public class _cl_image_format {

    _cl_image_format() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        opencl_h.C_INT.withName("image_channel_order"),
        opencl_h.C_INT.withName("image_channel_data_type")
    ).withName("_cl_image_format");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfInt image_channel_order$LAYOUT = (OfInt)$LAYOUT.select(groupElement("image_channel_order"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_channel_order image_channel_order
     * }
     */
    public static final OfInt image_channel_order$layout() {
        return image_channel_order$LAYOUT;
    }

    private static final long image_channel_order$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_channel_order image_channel_order
     * }
     */
    public static final long image_channel_order$offset() {
        return image_channel_order$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_channel_order image_channel_order
     * }
     */
    public static int image_channel_order(MemorySegment struct) {
        return struct.get(image_channel_order$LAYOUT, image_channel_order$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_channel_order image_channel_order
     * }
     */
    public static void image_channel_order(MemorySegment struct, int fieldValue) {
        struct.set(image_channel_order$LAYOUT, image_channel_order$OFFSET, fieldValue);
    }

    private static final OfInt image_channel_data_type$LAYOUT = (OfInt)$LAYOUT.select(groupElement("image_channel_data_type"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_channel_type image_channel_data_type
     * }
     */
    public static final OfInt image_channel_data_type$layout() {
        return image_channel_data_type$LAYOUT;
    }

    private static final long image_channel_data_type$OFFSET = 4;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_channel_type image_channel_data_type
     * }
     */
    public static final long image_channel_data_type$offset() {
        return image_channel_data_type$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_channel_type image_channel_data_type
     * }
     */
    public static int image_channel_data_type(MemorySegment struct) {
        return struct.get(image_channel_data_type$LAYOUT, image_channel_data_type$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_channel_type image_channel_data_type
     * }
     */
    public static void image_channel_data_type(MemorySegment struct, int fieldValue) {
        struct.set(image_channel_data_type$LAYOUT, image_channel_data_type$OFFSET, fieldValue);
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

