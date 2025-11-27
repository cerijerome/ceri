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
import ceri.common.array.Array;
import ceri.common.except.ExceptionAdapter;

public class CaptureState {
	private static final StructLayout CAPTURE_STATE_LAYOUT = Linker.Option.captureStateLayout();
	private static final String ERR_NO_FIELD = "errno";
	private static final VarHandle ERR_NO_HANDLE =
		CAPTURE_STATE_LAYOUT.varHandle(PathElement.groupElement(ERR_NO_FIELD));
	private static final MethodHandle STRERROR =
		Native.method("strerror", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
	public static final Linker.Option OPTION = Linker.Option.captureCallState(ERR_NO_FIELD);

	private CaptureState() {}

	/**
	 * Creates a capture argument.
	 */
	public static MemorySegment capture(SegmentAllocator allocator) {
		return allocator.allocate(CAPTURE_STATE_LAYOUT);
	}

	/**
	 * Verifies the captured state for non-zero errno from the argument array, or throws an
	 * exception with the error description.
	 */
	public static void validate(Object returnValue, Object[] args) throws LastErrorException {
		if (Array.isEmpty(args)) return;
		if (!(args[0] instanceof MemorySegment m)) return;
		if (returnValue instanceof Number n && n.longValue() >= 0) return;
		int errNo = errNo(m);
		if (errNo != 0) throw LastErrorException.full(errNo, strError(errNo));
	}

	// support

	private static int errNo(MemorySegment m) {
		return (int) ERR_NO_HANDLE.get(m, 0);
	}

	private static String strError(int errNo) {
		var m = ExceptionAdapter.shouldNotThrow.get(() -> invokeStrError(errNo));
		return Allocators.STRING.from(m, null, Integer.MAX_VALUE);
	}

	private static MemorySegment invokeStrError(int errNo) throws Throwable {
		return (MemorySegment) STRERROR.invokeExact(errNo);
	}
}
