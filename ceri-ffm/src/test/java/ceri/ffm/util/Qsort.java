package ceri.ffm.util;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.invoke.MethodHandle;
import java.nio.ByteOrder;
import ceri.common.array.RawArray;
import ceri.common.data.Bytes;
import ceri.common.function.Functions;
import ceri.common.function.Lambdas;
import ceri.ffm.core.Call;
import ceri.ffm.core.Layouts;
import ceri.ffm.reflect.Refine.Const;
import ceri.ffm.reflect.Refine.Order;
import ceri.ffm.type.Callback;
import ceri.ffm.type.IntType.size_t;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Primitive;

/**
 * Qsort using tools.
 */
public class Qsort {
	private static final Linker LINKER = Linker.nativeLinker();
	private static final Primitive.OfInt INT = Primitive.INT.order(ByteOrder.BIG_ENDIAN);
	private static final MethodHandle QSORT = LINKER.downcallHandle(
		LINKER.defaultLookup().findOrThrow("qsort"),
		FunctionDescriptor.ofVoid(Layouts.POINTER, Layouts.LONG, Layouts.LONG, Layouts.POINTER));

	public interface qsort_callback extends Callback {

		int invoke(Pointer<@Const @Order(Bytes.Order.big) Integer> p1,
			Pointer<@Const @Order(Bytes.Order.big) Integer> p2);

		static qsort_callback of(String name, Functions.IntBiOperator operator) {
			return Lambdas.register(
				(p1, p2) -> operator.applyAsInt(p1.reslice(1).get(), p2.reslice(1).get()), name);
		}
	}

	public interface Native {
		void qsort(Pointer.OfVoid p, size_t n, size_t size);
	}
	
	public static void main(String[] args) throws Throwable {
		var cb0 = qsort_callback.of("cb0", (i1, i2) -> Integer.compare(i1, i2));
		var cb1 = qsort_callback.of("cb1", (i1, i2) -> Integer.compare(i2, i1));
		var cb2 = qsort_callback.of("cb2", (i1, i2) -> Integer.compare(i1 & 3, i2 & 3));
		int[] array = { 0, 9, 3, 4, 6, 5, 1, 8, 2, 7 };
		System.out.println(RawArray.toString(qsort(array, cb0)));
		System.out.println(RawArray.toString(qsort(array, cb1)));
		System.out.println(RawArray.toString(qsort(array, cb2)));
		System.out.println(RawArray.toString(qsort(array, cb0)));
		System.out.println(RawArray.toString(qsort(array, cb1)));
		System.out.println(RawArray.toString(qsort(array, cb2)));
	}

	public static int[] qsort(int[] array, qsort_callback callback) throws Throwable {
		try (var arena = Arena.ofConfined()) {
			var m = INT.allocArray(arena, array, false);
			var stub = Call.upcall(callback);
			QSORT.invoke(m, array.length, INT.layoutSize(), stub);
			return INT.getArray(m, false);
		}
	}
}
