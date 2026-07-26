package ceri.ffm.util;

import java.lang.foreign.Arena;
import ceri.common.array.RawArray;
import ceri.common.function.Functions;
import ceri.common.function.Lambdas;
import ceri.common.test.Testing;
import ceri.ffm.clib.ffm.CException;
import ceri.ffm.core.Caller;
import ceri.ffm.core.Library;
import ceri.ffm.type.Callback;
import ceri.ffm.type.IntType.size_t;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Primitive;

/**
 * Qsort using framework.
 */
public class Qsort {
	public static final Library<Qsort.Native> library = Library.of(Qsort.Native.class);
	public static final Caller<CException, Qsort.Native> caller = Caller.of(library);
	private static final Primitive.OfInt INT = Primitive.INT; // .order(ByteOrder.BIG_ENDIAN);
	private static long callbacks = 0;

	public interface Native {

		interface compar extends Callback {
			int invoke(Pointer.OfVoid p1, Pointer.OfVoid p2);

			static compar ofInt(String name, Functions.IntBiOperator operator) {
				return Lambdas.register((p1, p2) -> {
					callbacks++;
					return operator.applyAsInt(p1.asInt().reslice().get(),
						p2.asInt().reslice().get());
				}, name);
			}
		}

		void qsort(Pointer.OfVoid base, size_t n, size_t size, compar compar);
	}

	// TODO: why is cb2 so much faster in QsortCore?
	
	public static void main(String[] args) throws Throwable {
		try (var cb0 = Native.compar.ofInt("cb0", (i1, i2) -> Integer.compare(i1, i2));
			var cb1 = Native.compar.ofInt("cb1", (i1, i2) -> Integer.compare(i2, i1));
			var cb2 = Native.compar.ofInt("cb2", (i1, i2) -> Integer.compare(i1 & 3, i2 & 3))) {
			run(1000000, 3, cb0, cb1, cb2);
		}
	}

	public static void qsort(Pointer.OfVoid base, int n, int size, Native.compar compar)
		throws CException {
		callbacks = 0;
		Qsort.caller.call(c -> c.lib().qsort(base, new size_t(n), new size_t(size), compar),
			"qsort", base, n, size, compar);
	}

	// support

	private static void run(int count, int repeats, Native.compar... compars) throws Throwable {
		int[] array = Testing.randomInts(count, 0, count);
		for (int i = 0; i < repeats; i++) {
			for (var compar : compars)
				qsort(array, compar);
			System.out.println();
		}
	}

	private static void qsort(int[] array, Native.compar compar) throws Throwable {
		try (var arena = Arena.ofConfined()) {
			var base = Pointer.ofInts(arena, false, array).asVoid();
			var t = System.currentTimeMillis();
			qsort(base, array.length, INT.layoutSize(), compar);
			t = System.currentTimeMillis() - t;
			print(t, base);
		}
	}

	private static void print(long t, Pointer.OfVoid base) {
		int[] r = base.asInt().getArray(false);
		double tc = (t * 1000.0) / Math.max(callbacks, 1);
		System.out.printf("time = %dms (%.2fμs x %d)  ", t, tc, callbacks);
		if (r.length <= 100) System.out.println(RawArray.toString(r));
		else System.out.printf("[%d, %d, %d, ... %d, %d, %d]%n", r[0], r[1], r[2], r[r.length - 3],
			r[r.length - 2], r[r.length - 1]);
	}
}
