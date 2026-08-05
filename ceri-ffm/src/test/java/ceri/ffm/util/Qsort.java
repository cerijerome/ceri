package ceri.ffm.util;

import java.lang.foreign.Arena;
import ceri.common.array.RawArray;
import ceri.common.function.Closeables;
import ceri.common.function.Functions;
import ceri.common.function.Lambdas;
import ceri.common.test.Testing;
import ceri.ffm.clib.ffm.CException;
import ceri.ffm.clib.ffm.CUnistd.size_t;
import ceri.ffm.core.Caller;
import ceri.ffm.core.Library;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.type.Callback;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Primitive;

/**
 * Qsort using framework to compare with JNA and core FFM (0.10μs vs 0.09 core FFM vs 1.25μs JNA).
 */
public class Qsort {
	private static final Library<Qsort.Native> library = Library.of(Qsort.Native.class);
	private static final Caller<CException, Qsort.Native> caller = Caller.of(library);
	private static final Primitive.OfInt INT = Primitive.INT; // .order(ByteOrder.BIG_ENDIAN);
	private static long callbacks = 0;

	public static void main(String[] args) throws Throwable {
		int count = 1000000;
		int reps = 3;
		Functions.IntBiOperator[] ops = { //
			(i1, i2) -> Integer.compare(i1, i2), //
			(i1, i2) -> Integer.compare(i2, i1), //
			(i1, i2) -> Integer.compare(i1 & 3, i2 & 3) };
		int[] array = Testing.randomInts(count, 0, count);
		FfmTesting.title("Qsort framework");
		run(array, reps, ops);
		FfmTesting.title("Qsort core");
		QsortCore.run(array, reps, ops);
	}

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

	public static void qsort(Pointer.OfVoid base, int n, int size, Native.compar compar)
		throws CException {
		callbacks = 0;
		Qsort.caller.call(c -> c.lib().qsort(base, new size_t(n), new size_t(size), compar),
			"qsort", base, n, size, compar);
	}

	public static void run(int[] array, int repeats) throws Throwable {
		try (var cb0 = Native.compar.ofInt("cb0", (i1, i2) -> Integer.compare(i1, i2));
			var cb1 = Native.compar.ofInt("cb1", (i1, i2) -> Integer.compare(i2, i1));
			var cb2 = Native.compar.ofInt("cb2", (i1, i2) -> Integer.compare(i1 & 3, i2 & 3))) {
			run(array, repeats, cb0, cb1, cb2);
		}
	}

	public static void run(int[] array, int repeats, Functions.IntBiOperator... ops)
		throws Throwable {
		var compars = RawArray.adaptValues(Native.compar[]::new, ops,
			(c, o, i) -> c[i] = Native.compar.ofInt("cb" + i, o[i]));
		run(array, repeats, compars);
		Closeables.closeReversed(compars);
	}

	// support

	private static void run(int[] array, int repeats, Native.compar... compars) throws Throwable {
		for (int i = 0; i < repeats; i++) {
			for (var compar : compars)
				qsort(array, compar);
			System.out.println();
		}
	}

	private static void qsort(int[] array, Native.compar compar) throws Throwable {
		try (var arena = Arena.ofConfined()) {
			var base = Primitive.INT.pointerOfAll(arena, false, array).asVoid();
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
