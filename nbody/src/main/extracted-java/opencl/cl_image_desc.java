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
 * typedef struct _cl_image_desc {
 *     cl_mem_object_type image_type;
 *     size_t image_width;
 *     size_t image_height;
 *     size_t image_depth;
 *     size_t image_array_size;
 *     size_t image_row_pitch;
 *     size_t image_slice_pitch;
 *     cl_uint num_mip_levels;
 *     cl_uint num_samples;
 *     cl_mem buffer;
 * } cl_image_desc
 * }
 */
public class cl_image_desc extends _cl_image_desc {

    cl_image_desc() {
        // Should not be called directly
    }
}

