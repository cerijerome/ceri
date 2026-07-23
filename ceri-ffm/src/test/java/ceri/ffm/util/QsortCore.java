package ceri.ffm.util;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.nio.ByteOrder;
import java.util.Map;
import ceri.common.array.RawArray;
import ceri.common.collect.Maps;
import ceri.common.function.Functions;
import ceri.common.function.Lambdas;
import ceri.common.reflect.Handles;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Segments;
import ceri.ffm.type.Callback;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Primitive;

/**
 * Qsort from basic principles.
 */
public class QsortCore {
	private static final Linker LINKER = Linker.nativeLinker();
	private static final Primitive.OfInt INT = Primitive.INT.order(ByteOrder.BIG_ENDIAN);
	private static final MethodHandle QSORT = LINKER.downcallHandle(
		LINKER.defaultLookup().findOrThrow("qsort"),
		FunctionDescriptor.ofVoid(Layouts.POINTER, Layouts.LONG, Layouts.LONG, Layouts.POINTER));
	private static final MethodHandle GENERAL = Handles.staticMethod(QsortCore.class, "general",
		Object.class, Callback.class, Object[].class);
	private static final Map<Callback, MemorySegment> stubs = Maps.syncWeak();

	public interface qsort_callback extends Callback {
		static MethodHandle HANDLE = Handles.method(qsort_callback.class, METHOD_NAME, int.class,
			Pointer.OfInt.class, Pointer.OfInt.class);
		static FunctionDescriptor DESC =
			FunctionDescriptor.of(Layouts.INT, Layouts.POINTER, Layouts.POINTER);

		int invoke(Pointer.OfInt p1, Pointer.OfInt p2);

		static qsort_callback of(String name, Functions.IntBiOperator operator) {
			return Lambdas.register((p1, p2) -> operator.applyAsInt(p1.get(), p2.get()), name);
		}
	}

	public static void main(String[] args) throws Throwable {
		try (var cb0 = qsort_callback.of("cb0", (i1, i2) -> Integer.compare(i1, i2));
			var cb1 = qsort_callback.of("cb1", (i1, i2) -> Integer.compare(i2, i1));
			var cb2 = qsort_callback.of("cb2", (i1, i2) -> Integer.compare(i1 & 3, i2 & 3))) {
			int[] array = { 0, 9, 3, 4, 6, 5, 1, 8, 2, 7 };
			System.out.println(RawArray.toString(qsort(array, cb0)));
			System.out.println(RawArray.toString(qsort(array, cb1)));
			System.out.println(RawArray.toString(qsort(array, cb2)));
			System.out.println(RawArray.toString(qsort(array, cb0)));
			System.out.println(RawArray.toString(qsort(array, cb1)));
			System.out.println(RawArray.toString(qsort(array, cb2)));
		}
	}

	public static int[] qsort(int[] array, qsort_callback callback) throws Throwable {
		try (var arena = Arena.ofConfined()) {
			var m = INT.allocArray(arena, array, false);
			var stub = stub(callback, qsort_callback.DESC, int.class, MemorySegment.class,
				MemorySegment.class);
			QSORT.invoke(m, array.length, INT.layoutSize(), stub);
			return INT.getArray(m, false);
		}
	}

	public static Object general(Callback callback, Object... args) {
		// convert native args to local args
		var p1 = INT.pointer(Segments.reslice((MemorySegment) args[0], INT.layout()));
		var p2 = INT.pointer(Segments.reslice((MemorySegment) args[1], INT.layout()));
		Object[] localArgs = { callback, p1, p2 };
		return Handles.invoke(qsort_callback.HANDLE, localArgs);
	}

	// support

	private static MemorySegment stub(Callback callback, FunctionDescriptor desc, Class<?> rtnType,
		Class<?>... argTypes) {
		return stubs.computeIfAbsent(callback, c -> createStub(c, desc, rtnType, argTypes));
	}

	private static MemorySegment createStub(Callback callback, FunctionDescriptor desc,
		Class<?> rtnType, Class<?>... argTypes) {
		System.out.println("Binding GENERAL to " + Lambdas.name(callback) + ": " + desc);
		var handle = Handles.asType(GENERAL.bindTo(callback).asVarargsCollector(Object[].class),
			rtnType, argTypes);
		return upcallStub(handle, desc);
	}

	@SuppressWarnings("resource")
	private static MemorySegment upcallStub(MethodHandle handle, FunctionDescriptor desc) {
		return LINKER.upcallStub(handle, desc, Arena.ofAuto());
	}
}
