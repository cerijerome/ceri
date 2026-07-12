package ceri.ffm.core;

import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.StructLayout;
import java.lang.invoke.VarHandle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ffm.clib.ffm.CException;
import ceri.ffm.clib.ffm.CString;

/**
 * Support for capturing last error codes.
 */
public class LastError {
	private static final Logger logger = LogManager.getFormatterLogger();
	private static final StructLayout CAPTURE_STATE_LAYOUT = Linker.Option.captureStateLayout();
	private static final String ERRNO_FIELD = "errno";
	private static final VarHandle ERRNO_HANDLE =
		CAPTURE_STATE_LAYOUT.varHandle(PathElement.groupElement(ERRNO_FIELD));
	public static final Linker.Option OPTION = Linker.Option.captureCallState(ERRNO_FIELD);
	private static final String UNKNOWN = "Unknown error";
	private static final String OK_MESSAGE = "OK";
	public static final int OK = 0;
	private static final ThreadLocal<Integer> lastError = ThreadLocal.withInitial(() -> OK);

	private LastError() {}

	/**
	 * Creates an error code capture argument.
	 */
	public static MemorySegment capture(SegmentAllocator allocator) {
		return allocator.allocate(CAPTURE_STATE_LAYOUT);
	}

	/**
	 * Extracts the error code from the call argument after the call, and saves to thread-local.
	 */
	public static int save(MemorySegment arg) {
		int errno = (int) ERRNO_HANDLE.get(arg, 0);
		lastError.set(errno);
		return errno;
	}

	/**
	 * Returns the error code currently saved to thread-local.
	 */
	public static int get() {
		return lastError.get();
	}

	/**
	 * Looks up the description of the error code; returns empty string if not found.
	 */
	public static String message(int code) {
		if (code == OK) return OK_MESSAGE;
		try {
			var s = CString.strerror(code);
			return s.startsWith(UNKNOWN) ? "" : s;
		} catch (CException | RuntimeException e) {
			logger.warn(e.getMessage());
			return "";
		}
	}
}
