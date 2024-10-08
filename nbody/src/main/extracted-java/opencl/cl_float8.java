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
 * union {
 *     cl_float s[8];
 *     struct {
 *         cl_float x;
 *         cl_float y;
 *         cl_float z;
 *         cl_float w;
 *     };
 *     struct {
 *         cl_float s0;
 *         cl_float s1;
 *         cl_float s2;
 *         cl_float s3;
 *         cl_float s4;
 *         cl_float s5;
 *         cl_float s6;
 *         cl_float s7;
 *     };
 *     struct {
 *         cl_float4 lo;
 *         cl_float4 hi;
 *     };
 * }
 * }
 */
public class cl_float8 {

    cl_float8() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.unionLayout(
        MemoryLayout.sequenceLayout(8, opencl_h.C_FLOAT).withName("s"),
        MemoryLayout.structLayout(
            opencl_h.C_FLOAT.withName("x"),
            opencl_h.C_FLOAT.withName("y"),
            opencl_h.C_FLOAT.withName("z"),
            opencl_h.C_FLOAT.withName("w")
        ).withName("$anon$1185:18"),
        MemoryLayout.structLayout(
            opencl_h.C_FLOAT.withName("s0"),
            opencl_h.C_FLOAT.withName("s1"),
            opencl_h.C_FLOAT.withName("s2"),
            opencl_h.C_FLOAT.withName("s3"),
            opencl_h.C_FLOAT.withName("s4"),
            opencl_h.C_FLOAT.withName("s5"),
            opencl_h.C_FLOAT.withName("s6"),
            opencl_h.C_FLOAT.withName("s7")
        ).withName("$anon$1186:18"),
        MemoryLayout.structLayout(
            cl_float4.layout().withName("lo"),
            cl_float4.layout().withName("hi")
        ).withName("$anon$1187:18")
    ).withName("$anon$1181:9");

    /**
     * The layout of this union
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final SequenceLayout s$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("s"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_float s[8]
     * }
     */
    public static final SequenceLayout s$layout() {
        return s$LAYOUT;
    }

    private static final long s$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_float s[8]
     * }
     */
    public static final long s$offset() {
        return s$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_float s[8]
     * }
     */
    public static MemorySegment s(MemorySegment union) {
        return union.asSlice(s$OFFSET, s$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_float s[8]
     * }
     */
    public static void s(MemorySegment union, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, union, s$OFFSET, s$LAYOUT.byteSize());
    }

    private static long[] s$DIMS = { 8 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * cl_float s[8]
     * }
     */
    public static long[] s$dimensions() {
        return s$DIMS;
    }
    private static final VarHandle s$ELEM_HANDLE = s$LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * cl_float s[8]
     * }
     */
    public static float s(MemorySegment union, long index0) {
        return (float)s$ELEM_HANDLE.get(union, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * cl_float s[8]
     * }
     */
    public static void s(MemorySegment union, long index0, float fieldValue) {
        s$ELEM_HANDLE.set(union, 0L, index0, fieldValue);
    }

    private static final OfFloat x$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("$anon$1185:18"), groupElement("x"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_float x
     * }
     */
    public static final OfFloat x$layout() {
        return x$LAYOUT;
    }

    private static final long x$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_float x
     * }
     */
    public static final long x$offset() {
        return x$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_float x
     * }
     */
    public static float x(MemorySegment union) {
        return union.get(x$LAYOUT, x$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_float x
     * }
     */
    public static void x(MemorySegment union, float fieldValue) {
        union.set(x$LAYOUT, x$OFFSET, fieldValue);
    }

    private static final OfFloat y$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("$anon$1185:18"), groupElement("y"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_float y
     * }
     */
    public static final OfFloat y$layout() {
        return y$LAYOUT;
    }

    private static final long y$OFFSET = 4;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_float y
     * }
     */
    public static final long y$offset() {
        return y$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_float y
     * }
     */
    public static float y(MemorySegment union) {
        return union.get(y$LAYOUT, y$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_float y
     * }
     */
    public static void y(MemorySegment union, float fieldValue) {
        union.set(y$LAYOUT, y$OFFSET, fieldValue);
    }

    private static final OfFloat z$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("$anon$1185:18"), groupElement("z"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_float z
     * }
     */
    public static final OfFloat z$layout() {
        return z$LAYOUT;
    }

    private static final long z$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_float z
     * }
     */
    public static final long z$offset() {
        return z$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_float z
     * }
     */
    public static float z(MemorySegment union) {
        return union.get(z$LAYOUT, z$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_float z
     * }
     */
    public static void z(MemorySegment union, float fieldValue) {
        union.set(z$LAYOUT, z$OFFSET, fieldValue);
    }

    private static final OfFloat w$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("$anon$1185:18"), groupElement("w"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_float w
     * }
     */
    public static final OfFloat w$layout() {
        return w$LAYOUT;
    }

    private static final long w$OFFSET = 12;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_float w
     * }
     */
    public static final long w$offset() {
        return w$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_float w
     * }
     */
    public static float w(MemorySegment union) {
        return union.get(w$LAYOUT, w$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_float w
     * }
     */
    public static void w(MemorySegment union, float fieldValue) {
        union.set(w$LAYOUT, w$OFFSET, fieldValue);
    }

    private static final OfFloat s0$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("$anon$1186:18"), groupElement("s0"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_float s0
     * }
     */
    public static final OfFloat s0$layout() {
        return s0$LAYOUT;
    }

    private static final long s0$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_float s0
     * }
     */
    public static final long s0$offset() {
        return s0$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_float s0
     * }
     */
    public static float s0(MemorySegment union) {
        return union.get(s0$LAYOUT, s0$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_float s0
     * }
     */
    public static void s0(MemorySegment union, float fieldValue) {
        union.set(s0$LAYOUT, s0$OFFSET, fieldValue);
    }

    private static final OfFloat s1$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("$anon$1186:18"), groupElement("s1"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_float s1
     * }
     */
    public static final OfFloat s1$layout() {
        return s1$LAYOUT;
    }

    private static final long s1$OFFSET = 4;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_float s1
     * }
     */
    public static final long s1$offset() {
        return s1$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_float s1
     * }
     */
    public static float s1(MemorySegment union) {
        return union.get(s1$LAYOUT, s1$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_float s1
     * }
     */
    public static void s1(MemorySegment union, float fieldValue) {
        union.set(s1$LAYOUT, s1$OFFSET, fieldValue);
    }

    private static final OfFloat s2$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("$anon$1186:18"), groupElement("s2"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_float s2
     * }
     */
    public static final OfFloat s2$layout() {
        return s2$LAYOUT;
    }

    private static final long s2$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_float s2
     * }
     */
    public static final long s2$offset() {
        return s2$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_float s2
     * }
     */
    public static float s2(MemorySegment union) {
        return union.get(s2$LAYOUT, s2$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_float s2
     * }
     */
    public static void s2(MemorySegment union, float fieldValue) {
        union.set(s2$LAYOUT, s2$OFFSET, fieldValue);
    }

    private static final OfFloat s3$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("$anon$1186:18"), groupElement("s3"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_float s3
     * }
     */
    public static final OfFloat s3$layout() {
        return s3$LAYOUT;
    }

    private static final long s3$OFFSET = 12;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_float s3
     * }
     */
    public static final long s3$offset() {
        return s3$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_float s3
     * }
     */
    public static float s3(MemorySegment union) {
        return union.get(s3$LAYOUT, s3$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_float s3
     * }
     */
    public static void s3(MemorySegment union, float fieldValue) {
        union.set(s3$LAYOUT, s3$OFFSET, fieldValue);
    }

    private static final OfFloat s4$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("$anon$1186:18"), groupElement("s4"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_float s4
     * }
     */
    public static final OfFloat s4$layout() {
        return s4$LAYOUT;
    }

    private static final long s4$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_float s4
     * }
     */
    public static final long s4$offset() {
        return s4$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_float s4
     * }
     */
    public static float s4(MemorySegment union) {
        return union.get(s4$LAYOUT, s4$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_float s4
     * }
     */
    public static void s4(MemorySegment union, float fieldValue) {
        union.set(s4$LAYOUT, s4$OFFSET, fieldValue);
    }

    private static final OfFloat s5$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("$anon$1186:18"), groupElement("s5"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_float s5
     * }
     */
    public static final OfFloat s5$layout() {
        return s5$LAYOUT;
    }

    private static final long s5$OFFSET = 20;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_float s5
     * }
     */
    public static final long s5$offset() {
        return s5$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_float s5
     * }
     */
    public static float s5(MemorySegment union) {
        return union.get(s5$LAYOUT, s5$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_float s5
     * }
     */
    public static void s5(MemorySegment union, float fieldValue) {
        union.set(s5$LAYOUT, s5$OFFSET, fieldValue);
    }

    private static final OfFloat s6$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("$anon$1186:18"), groupElement("s6"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_float s6
     * }
     */
    public static final OfFloat s6$layout() {
        return s6$LAYOUT;
    }

    private static final long s6$OFFSET = 24;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_float s6
     * }
     */
    public static final long s6$offset() {
        return s6$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_float s6
     * }
     */
    public static float s6(MemorySegment union) {
        return union.get(s6$LAYOUT, s6$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_float s6
     * }
     */
    public static void s6(MemorySegment union, float fieldValue) {
        union.set(s6$LAYOUT, s6$OFFSET, fieldValue);
    }

    private static final OfFloat s7$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("$anon$1186:18"), groupElement("s7"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_float s7
     * }
     */
    public static final OfFloat s7$layout() {
        return s7$LAYOUT;
    }

    private static final long s7$OFFSET = 28;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_float s7
     * }
     */
    public static final long s7$offset() {
        return s7$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_float s7
     * }
     */
    public static float s7(MemorySegment union) {
        return union.get(s7$LAYOUT, s7$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_float s7
     * }
     */
    public static void s7(MemorySegment union, float fieldValue) {
        union.set(s7$LAYOUT, s7$OFFSET, fieldValue);
    }

    private static final GroupLayout lo$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("$anon$1187:18"), groupElement("lo"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_float4 lo
     * }
     */
    public static final GroupLayout lo$layout() {
        return lo$LAYOUT;
    }

    private static final long lo$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_float4 lo
     * }
     */
    public static final long lo$offset() {
        return lo$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_float4 lo
     * }
     */
    public static MemorySegment lo(MemorySegment union) {
        return union.asSlice(lo$OFFSET, lo$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_float4 lo
     * }
     */
    public static void lo(MemorySegment union, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, union, lo$OFFSET, lo$LAYOUT.byteSize());
    }

    private static final GroupLayout hi$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("$anon$1187:18"), groupElement("hi"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * cl_float4 hi
     * }
     */
    public static final GroupLayout hi$layout() {
        return hi$LAYOUT;
    }

    private static final long hi$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * cl_float4 hi
     * }
     */
    public static final long hi$offset() {
        return hi$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * cl_float4 hi
     * }
     */
    public static MemorySegment hi(MemorySegment union) {
        return union.asSlice(hi$OFFSET, hi$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * cl_float4 hi
     * }
     */
    public static void hi(MemorySegment union, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, union, hi$OFFSET, hi$LAYOUT.byteSize());
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

