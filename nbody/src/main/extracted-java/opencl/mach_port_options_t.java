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
 * typedef struct mach_port_options {
 *     uint32_t flags;
 *     mach_port_limits_t mpl;
 *     union {
 *         uint64_t reserved[2];
 *         mach_port_name_t work_interval_port;
 *         mach_service_port_info_t service_port_info;
 *         mach_port_name_t service_port_name;
 *     };
 * } mach_port_options_t
 * }
 */
public class mach_port_options_t extends mach_port_options {

    mach_port_options_t() {
        // Should not be called directly
    }
}

