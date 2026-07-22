package ceri.ffm.util;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.ByteOrder;
import java.util.Map;
import ceri.common.array.RawArray;
import ceri.common.collect.Maps;
import ceri.common.function.Functions;
import ceri.common.function.Lambdas;
import ceri.common.reflect.Handles;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Segments;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Primitive;

public class Qsort {
	private static final Linker LINKER = Linker.nativeLinker();
	private static final Primitive.OfInt INT = Primitive.INT.order(ByteOrder.BIG_ENDIAN);
	private static final MethodHandle QSORT = LINKER.downcallHandle(
		LINKER.defaultLookup().findOrThrow("qsort"), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS,
			ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
	private static final MethodHandle GENERAL =
		Handles.staticMethod(Qsort.class, "general", Object.class, Callback.class, Object[].class);
	private static final Map<Callback, MemorySegment> stubs = Maps.syncWeak();

	public interface qsort_callback extends Callback {
		static FunctionDescriptor DESC =
			FunctionDescriptor.of(Layouts.INT, Layouts.POINTER, Layouts.POINTER);

		int invoke(Pointer.OfInt p1, Pointer.OfInt p2);

		static qsort_callback of(String name, Functions.IntBiOperator operator) {
			return Lambdas.register((p1, p2) -> operator.applyAsInt(p1.get(), p2.get()), name);
		}
	}

	public static void main(String[] args) throws Throwable {
		var cb0 = qsort_callback.of("cb0", (i1, i2) -> Integer.compare(i1, i2));
		var cb1 = qsort_callback.of("cb1", (i1, i2) -> Integer.compare(i2, i1));
		var cb2 = qsort_callback.of("cb2", (i1, i2) -> Integer.compare(i1 & 3, i2 & 3));
		var cb3 = qsort_callback.of("cb3", (i1, i2) -> Integer.compare(i1, i2));
		int[] array = { 0, 9, 3, 4, 6, 5, 1, 8, 2, 7 };
		System.out.println(RawArray.toString(qsort(array, cb0)));
		System.out.println(RawArray.toString(qsort(array, cb1)));
		System.out.println(RawArray.toString(qsort(array, cb2)));
		System.out.println(RawArray.toString(qsort(array, cb3)));
		System.out.println(RawArray.toString(qsort(array, cb0)));
		System.out.println(RawArray.toString(qsort(array, cb1)));
		System.out.println(RawArray.toString(qsort(array, cb2)));
		System.out.println(RawArray.toString(qsort(array, cb3)));
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
		var cb = (qsort_callback) callback;
		var p1 = INT.pointer(Segments.reslice((MemorySegment) args[0], INT.layout()));
		var p2 = INT.pointer(Segments.reslice((MemorySegment) args[1], INT.layout()));
		return cb.invoke(p1, p2);
	}

	// support
	
	private static MemorySegment stub(Callback callback, FunctionDescriptor desc, Class<?> rtnType,
		Class<?>... argTypes) {
		return stubs.computeIfAbsent(callback, c -> createStub(c, desc, rtnType, argTypes));
	}
	
	private static MemorySegment createStub(Callback callback, FunctionDescriptor desc,
		Class<?> rtnType, Class<?>... argTypes) {
		System.out.println("Binding GENERAL to " + Lambdas.name(callback) + ": " + desc);
		var handle = GENERAL.bindTo(callback).asVarargsCollector(Object[].class)
			.asType(MethodType.methodType(rtnType, argTypes));
		return upcallStub(handle, desc);
	}

	@SuppressWarnings("resource")
	private static MemorySegment upcallStub(MethodHandle handle, FunctionDescriptor desc) {
		return LINKER.upcallStub(handle, desc, Arena.ofAuto());
	}
}
