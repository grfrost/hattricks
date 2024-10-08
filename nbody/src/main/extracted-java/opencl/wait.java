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
 * union wait {
 *     int w_status;
 *     struct {
 *         unsigned int w_Termsig : 7;
 *         unsigned int w_Coredump : 1;
 *         unsigned int w_Retcode : 8;
 *         unsigned int w_Filler : 16;
 *     } w_T;
 *     struct {
 *         unsigned int w_Stopval : 8;
 *         unsigned int w_Stopsig : 8;
 *         unsigned int w_Filler : 16;
 *     } w_S;
 * }
 * }
 */
public class wait {

    wait() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.unionLayout(
        opencl_h.C_INT.withName("w_status"),
        wait.w_T.layout().withName("w_T"),
        wait.w_S.layout().withName("w_S")
    ).withName("wait");

    /**
     * The layout of this union
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfInt w_status$LAYOUT = (OfInt)$LAYOUT.select(groupElement("w_status"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int w_status
     * }
     */
    public static final OfInt w_status$layout() {
        return w_status$LAYOUT;
    }

    private static final long w_status$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int w_status
     * }
     */
    public static final long w_status$offset() {
        return w_status$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int w_status
     * }
     */
    public static int w_status(MemorySegment union) {
        return union.get(w_status$LAYOUT, w_status$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int w_status
     * }
     */
    public static void w_status(MemorySegment union, int fieldValue) {
        union.set(w_status$LAYOUT, w_status$OFFSET, fieldValue);
    }

    /**
     * {@snippet lang=c :
     * struct {
     *     unsigned int w_Termsig : 7;
     *     unsigned int w_Coredump : 1;
     *     unsigned int w_Retcode : 8;
     *     unsigned int w_Filler : 16;
     * }
     * }
     */
    public static class w_T {

        w_T() {
            // Should not be called directly
        }

        private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
            MemoryLayout.paddingLayout(4)
        ).withName("$anon$199:2");

        /**
         * The layout of this struct
         */
        public static final GroupLayout layout() {
            return $LAYOUT;
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

    private static final GroupLayout w_T$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("w_T"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * struct {
     *     unsigned int w_Termsig : 7;
     *     unsigned int w_Coredump : 1;
     *     unsigned int w_Retcode : 8;
     *     unsigned int w_Filler : 16;
     * } w_T
     * }
     */
    public static final GroupLayout w_T$layout() {
        return w_T$LAYOUT;
    }

    private static final long w_T$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * struct {
     *     unsigned int w_Termsig : 7;
     *     unsigned int w_Coredump : 1;
     *     unsigned int w_Retcode : 8;
     *     unsigned int w_Filler : 16;
     * } w_T
     * }
     */
    public static final long w_T$offset() {
        return w_T$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * struct {
     *     unsigned int w_Termsig : 7;
     *     unsigned int w_Coredump : 1;
     *     unsigned int w_Retcode : 8;
     *     unsigned int w_Filler : 16;
     * } w_T
     * }
     */
    public static MemorySegment w_T(MemorySegment union) {
        return union.asSlice(w_T$OFFSET, w_T$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * struct {
     *     unsigned int w_Termsig : 7;
     *     unsigned int w_Coredump : 1;
     *     unsigned int w_Retcode : 8;
     *     unsigned int w_Filler : 16;
     * } w_T
     * }
     */
    public static void w_T(MemorySegment union, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, union, w_T$OFFSET, w_T$LAYOUT.byteSize());
    }

    /**
     * {@snippet lang=c :
     * struct {
     *     unsigned int w_Stopval : 8;
     *     unsigned int w_Stopsig : 8;
     *     unsigned int w_Filler : 16;
     * }
     * }
     */
    public static class w_S {

        w_S() {
            // Should not be called directly
        }

        private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
            MemoryLayout.paddingLayout(4)
        ).withName("$anon$218:2");

        /**
         * The layout of this struct
         */
        public static final GroupLayout layout() {
            return $LAYOUT;
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

    private static final GroupLayout w_S$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("w_S"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * struct {
     *     unsigned int w_Stopval : 8;
     *     unsigned int w_Stopsig : 8;
     *     unsigned int w_Filler : 16;
     * } w_S
     * }
     */
    public static final GroupLayout w_S$layout() {
        return w_S$LAYOUT;
    }

    private static final long w_S$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * struct {
     *     unsigned int w_Stopval : 8;
     *     unsigned int w_Stopsig : 8;
     *     unsigned int w_Filler : 16;
     * } w_S
     * }
     */
    public static final long w_S$offset() {
        return w_S$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * struct {
     *     unsigned int w_Stopval : 8;
     *     unsigned int w_Stopsig : 8;
     *     unsigned int w_Filler : 16;
     * } w_S
     * }
     */
    public static MemorySegment w_S(MemorySegment union) {
        return union.asSlice(w_S$OFFSET, w_S$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * struct {
     *     unsigned int w_Stopval : 8;
     *     unsigned int w_Stopsig : 8;
     *     unsigned int w_Filler : 16;
     * } w_S
     * }
     */
    public static void w_S(MemorySegment union, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, union, w_S$OFFSET, w_S$LAYOUT.byteSize());
    }

    /**
     * Obtains a slice of {@code arrayParam} which selects the array element at {@code index}.
     * The returned segment has address {@code arrayParam.address() + index * layout().byteSize()}
     */
    public static MemorySegment asSlice(MemorySegment array, long index) {
        return array.asSlice(layout().byteSize() * index);
    }

    /**
     * The size (in bytes) of this union
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

