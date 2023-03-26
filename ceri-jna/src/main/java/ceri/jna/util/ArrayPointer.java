package ceri.jna.util;

import static ceri.common.collection.ArrayUtil.validateIndex;
import static ceri.common.validation.ValidationUtil.validateMin;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;

/**
 * Holds a pointer to a typed array, and provides access to the array. The array may be
 * null-terminated by reference, fixed-length by reference, or fixed-length by value. Useful for
 * holding lists that need to be freed after use.
 */
public abstract class ArrayPointer<T> extends PointerType {

	/**
	 * Create for a null-terminated array by reference. For {@code type**} array types.
	 */
	public static <T> ArrayPointer<T> byRef(Pointer p, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayFn) {
		return of(p, i -> JnaUtil.byRef(p, i, constructor),
			() -> JnaUtil.arrayByRef(p, constructor, arrayFn), () -> PointerUtil.count(p));
	}

	/**
	 * Create for a fixed-length array by reference. For {@code type**} array types.
	 */
	public static <T> ArrayPointer<T> byRef(Pointer p, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayFn, int count) {
		validateMin(count, 0);
		return of(p, i -> JnaUtil.byRef(p, valid(i, count), constructor),
			() -> JnaUtil.arrayByRef(p, constructor, arrayFn, count), () -> count);
	}

	/**
	 * Create for a fixed-length struct array by value. A null struct instance is created to
	 * determine the size. For {@code struct*} array types.
	 */
	public static <T extends Structure> ArrayPointer<T> byVal(Pointer p,
		Function<Pointer, T> constructor, IntFunction<T[]> arrayFn, int count) {
		return byVal(p, constructor, arrayFn, count, Struct.size(constructor));
	}

	/**
	 * Create for a fixed-length array by value of given type size. For {@code type*} array types.
	 */
	public static <T> ArrayPointer<T> byVal(Pointer p, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayFn, int count, int size) {
		validateMin(count, 0);
		return of(p, i -> JnaUtil.byVal(p, valid(i, count), constructor, size),
			() -> JnaUtil.arrayByVal(p, constructor, arrayFn, count, size), () -> count);
	}

	private ArrayPointer(Pointer p) {
		super(p);
	}

	/**
	 * Provides access to index i of the array. The index is validated for fixed-length arrays.
	 */
	public abstract T get(int i);

	/**
	 * Provides access to the typed array.
	 */
	public abstract T[] get();

	/**
	 * Provides the array length. Calculated for
	 */
	public abstract int count();

	@Override
	public void setPointer(Pointer p) {
		throw new UnsupportedOperationException("Pointer cannot be moved");
	}

	private static int valid(int i, int count) {
		validateIndex(count, i);
		return i;
	}

	private static <T> ArrayPointer<T> of(Pointer p, IntFunction<T> getIndexFn,
		Supplier<T[]> getAllFn, IntSupplier countFn) {
		return new ArrayPointer<>(p) {
			@Override
			public T get(int i) {
				return getIndexFn.apply(i);
			}

			@Override
			public T[] get() {
				return getAllFn.get();
			}

			public int count() {
				return countFn.getAsInt();
			}
		};
	}
}
