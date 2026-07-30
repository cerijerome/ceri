package ceri.jna.util;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import ceri.common.array.RawArray;
import ceri.common.function.Functions;
import ceri.common.function.Lambdas;
import ceri.common.test.Testing;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CUnistd.size_t;

/**
 * Qsort using framework to compare with FFM (1.25μs JNA vs 0.10μs FFM).
 */
public class Qsort {
	private static final JnaLibrary<Qsort.Native> library =
		JnaLibrary.of(Platform.C_LIBRARY_NAME, Qsort.Native.class);
	private static final Caller<CException> caller = Caller.of(CException::full);
	private static long callbacks = 0;

	public interface Native extends Library {

		interface compar extends Callback {
			int invoke(Pointer p1, Pointer p2);

			static compar ofInt(String name, Functions.IntBiOperator operator) {
				return Lambdas.register((p1, p2) -> {
					callbacks++;
					return operator.applyAsInt(p1.getInt(0), p2.getInt(0));
				}, name);
			}
		}

		void qsort(Pointer base, size_t n, size_t size, compar compar);
	}

	public static void main(String[] args) throws Throwable {
		var cb0 = Native.compar.ofInt("cb0", (i1, i2) -> Integer.compare(i1, i2));
		var cb1 = Native.compar.ofInt("cb1", (i1, i2) -> Integer.compare(i2, i1));
		var cb2 = Native.compar.ofInt("cb2", (i1, i2) -> Integer.compare(i1 & 3, i2 & 3));
		run(100000, 3, cb0, cb1, cb2);
	}

	public static void qsort(Pointer base, int n, int size, Native.compar compar)
		throws CException {
		callbacks = 0;
		Qsort.caller.call(() -> library.get().qsort(base, new size_t(n), new size_t(size), compar),
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
		try (var base = new Memory(array.length * Integer.BYTES)) {
			base.write(0, array, 0, array.length);
			var t = System.currentTimeMillis();
			qsort(base, array.length, Integer.BYTES, compar);
			t = System.currentTimeMillis() - t;
			print(t, base);
		}
	}

	private static void print(long t, Memory base) {
		int[] r = new int[(int) (base.size() / Integer.BYTES)];
		base.read(0, r, 0, r.length);
		double tc = (t * 1000.0) / Math.max(callbacks, 1);
		System.out.printf("time = %dms (%.2fμs x %d)  ", t, tc, callbacks);
		if (r.length <= 100) System.out.println(RawArray.toString(r));
		else System.out.printf("[%d, %d, %d, ... %d, %d, %d]%n", r[0], r[1], r[2], r[r.length - 3],
			r[r.length - 2], r[r.length - 1]);
	}
}
