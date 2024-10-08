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
 * typedef struct __darwin_ucontext {
 *     int uc_onstack;
 *     __darwin_sigset_t uc_sigmask;
 *     struct __darwin_sigaltstack uc_stack;
 *     struct __darwin_ucontext *uc_link;
 *     __darwin_size_t uc_mcsize;
 *     struct __darwin_mcontext64 *uc_mcontext;
 * } ucontext_t
 * }
 */
public class ucontext_t extends __darwin_ucontext {

    ucontext_t() {
        // Should not be called directly
    }
}

