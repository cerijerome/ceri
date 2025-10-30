package ceri.jna.clib.jna;

import static ceri.jna.clib.jna.CLib.caller;
import static ceri.jna.clib.jna.CLib.lib;
import com.sun.jna.Pointer;
import ceri.common.util.Os;
import ceri.jna.clib.jna.CUnistd.size_t;
import ceri.jna.reflect.CAnnotations.CInclude;

/**
 * Types and functions from {@code <sys/mman.h>}
 */
@CInclude("sys/mman.h")
public class CMman {
	public static final Pointer MAP_FAILED = Pointer.createConstant(-1L);
	/** Pages may not be accessed. */
	public static final int PROT_NONE = 0x00;
	/** Pages may be read. */
	public static final int PROT_READ = 0x01;
	/** Pages may be written. */
	public static final int PROT_WRITE = 0x02;
	/** Pages may be executed. */
	public static final int PROT_EXEC = 0x04;
	/** Updates are visible to other processes mapping the same region. */
	public static final int MAP_SHARED = 0x01;
	/** Updates are not visible to other processes mapping the same file. */
	public static final int MAP_PRIVATE = 0x02;
	/** Place the mapping at exactly that address. */
	public static final int MAP_FIXED = 0x10;
	/** The mapping is not backed by any file; its contents are initialized to zero. */
	public static final int MAP_ANONYMOUS;
	/** Do not reserve swap space for this mapping. */
	public static final int MAP_NORESERVE;

	private CMman() {}

	/**
	 * Creates a new mapping in the virtual address space of the calling process.
	 */
	public static Pointer mmap(Pointer addr, long len, int prot, int flags, int fd, int offset)
		throws CException {
		return caller.callType(() -> lib().mmap(addr, new size_t(len), prot, flags, fd, offset),
			"mmap", addr, len, prot, flags, offset);
	}

	/**
	 * Deletes the mappings for the specified address range, and causes further references to
	 * addresses within the range to generate invalid memory references.
	 */
	public static void munmap(Pointer addr, long len) throws CException {
		caller.verify(() -> lib().munmap(addr, new size_t(len)), "munmap", addr, len);
	}

	/* os-specific initialization */

	static {
		if (Os.info().mac) {
			MAP_ANONYMOUS = 0x1000;
			MAP_NORESERVE = 0x0040;
		} else {
			MAP_ANONYMOUS = 0x20;
			MAP_NORESERVE = 0x04000;
		}
	}
}
