package ceri.ffm.core;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import ceri.common.function.Functional;
import ceri.common.text.Strings;
import ceri.ffm.type.StringType;

/**
 * Support for capturing last error codes.
 */
public class LastError {
	private static final StructLayout CAPTURE_STATE_LAYOUT = Linker.Option.captureStateLayout();
	private static final String ERRNO_FIELD = "errno";
	private static final VarHandle ERRNO_HANDLE =
		CAPTURE_STATE_LAYOUT.varHandle(PathElement.groupElement(ERRNO_FIELD));
	private static final MethodHandle STRERROR =
		Native.method("strerror", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
	private static final int STRERROR_MAX = 1024;
	public static final Linker.Option OPTION = Linker.Option.captureCallState(ERRNO_FIELD);
	private static final String UNKNOWN = "Unknown error";
	public static final int OK = 0;
	private static final ThreadLocal<Integer> lastError = ThreadLocal.withInitial(() -> OK);

	private LastError() {}

	public static void main(String[] args) {
		System.out.println(message(3));
		System.out.println(message(7));
		System.out.println(message(Integer.MAX_VALUE));
	}

	public record State(int code, String message) {
		public static final State OK = new State(0, "OK");

		/**
		 * Throws an exception if the error code is non-zero.
		 */
		public void validate() throws LastError.Exception {
			if (code() != 0) throw LastError.Exception.full(code(), message());
		}
	}

	@SuppressWarnings("serial")
	public static class Exception extends RuntimeException {
		public final int code;

		public static LastError.Exception full(int code, String format, Object... args) {
			return new Exception(code, "[" + code + "] " + Strings.format(format, args));
		}

		public static LastError.Exception of(int code, String format, Object... args) {
			return new Exception(code, Strings.format(format, args));
		}

		private Exception(int code, String message) {
			super(message);
			this.code = code;
		}
	}

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
	 * Looks up the description of the error code.
	 */
	public static String message(int code) {
		if (code == OK) return "OK";
		var s = Functional.muteGet(() -> strerror(code), "");
		return s.startsWith(UNKNOWN) ? "" : s;
	}

	// support

	private static String strerror(int code) throws Throwable {
		var m = (MemorySegment) STRERROR.invokeExact(code);
		m = Memory.reslice(m, 0L, STRERROR_MAX);
		return StringType.DEFAULT.get(m, true);
	}
}
