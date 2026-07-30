package ceri.ffm.util;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import ceri.common.array.RawArray;
import ceri.common.function.Functions;
import ceri.common.function.Lambdas;
import ceri.common.reflect.Handles;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Segments;
import ceri.ffm.type.Primitive;

/**
 * Qsort from basic principles.
 */
public class QsortCore {
	private static final Linker LINKER = Linker.nativeLinker();
	private static final Primitive.OfInt INT = Primitive.INT; // .order(ByteOrder.BIG_ENDIAN);
	private static final MethodHandle QSORT = LINKER.downcallHandle(
		LINKER.defaultLookup().findOrThrow("qsort"),
		FunctionDescriptor.ofVoid(Layouts.POINTER, Layouts.LONG, Layouts.LONG, Layouts.POINTER));
	private static FunctionDescriptor COMPAR_DESC =
		FunctionDescriptor.of(Layouts.INT, Layouts.POINTER, Layouts.POINTER);
	private static final MethodHandle NATIVE_CALLBACK = Handles.staticMethod(QsortCore.class,
		"nativeCallback", int.class, compar.class, MemorySegment.class, MemorySegment.class);
	private static long callbacks = 0;

	public interface compar {
		int invoke(MemorySegment m1, MemorySegment m2);

		static compar ofInt(String name, Functions.IntBiOperator operator) {
			return Lambdas.register((m1, m2) -> {
				callbacks++;
				var i1 = INT.getInt(Segments.reslice(m1, INT.layout()), 0);
				var i2 = INT.getInt(Segments.reslice(m2, INT.layout()), 0);
				return operator.applyAsInt(i1, i2);
			}, name);
		}
	}

	public static void qsort(MemorySegment base, long n, long size, MemorySegment stub)
		throws Throwable {
		callbacks = 0;
		QSORT.invokeExact(base, n, size, stub);
	}

	public static int nativeCallback(compar compar, MemorySegment m1, MemorySegment m2) {
		try {
			return compar.invoke(m1, m2);
		} catch (Throwable t) {
			System.err.println("nativeCallback: exiting due to error");
			t.printStackTrace(System.err);
			System.exit(-1);
			return 0;
		}
	}

	public static void run(int[] array, int repeats, Functions.IntBiOperator... ops)
		throws Throwable {
		var compars = RawArray.adaptValues(compar[]::new, ops,
			(c, o, i) -> c[i] = compar.ofInt("cb" + i, o[i]));
		run(array, repeats, compars);
	}

	// support

	private static void run(int[] array, int repeats, compar... compars) throws Throwable {
		for (int i = 0; i < repeats; i++) {
			for (var compar : compars)
				qsort(array, compar);
			System.out.println();
		}
	}

	private static void qsort(int[] array, compar compar) throws Throwable {
		try (var arena = Arena.ofConfined()) {
			var base = INT.allocArray(arena, array, false);
			var stub = stub(compar, arena);
			var t = System.currentTimeMillis();
			qsort(base, array.length, INT.layoutSize(), stub);
			t = System.currentTimeMillis() - t;
			print(t, base);
		}
	}

	private static void print(long t, MemorySegment base) {
		int[] r = INT.getArray(base, false);
		double tc = (t * 1000.0) / Math.max(callbacks, 1);
		System.out.printf("time = %dms (%.2fμs x %d)  ", t, tc, callbacks);
		if (r.length <= 100) System.out.println(RawArray.toString(r));
		else System.out.printf("[%d, %d, %d, ... %d, %d, %d]%n", r[0], r[1], r[2], r[r.length - 3],
			r[r.length - 2], r[r.length - 1]);
	}

	private static MemorySegment stub(compar compar, Arena arena) {
		return LINKER.upcallStub(NATIVE_CALLBACK.bindTo(compar), COMPAR_DESC, arena);
	}
}
