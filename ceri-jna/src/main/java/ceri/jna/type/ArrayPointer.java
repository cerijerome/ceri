package ceri.jna.type;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import ceri.common.function.Functions;
import ceri.common.util.Validate;
import ceri.jna.util.Jna;
import ceri.jna.util.Pointers;

/**
 * Holds a pointer to a typed array, and provides access to the array. The array may be
 * null-terminated by reference, fixed-length by reference, or fixed-length by value. Useful for
 * holding lists that need to be freed after use.
 */
public abstract class ArrayPointer<T> extends PointerType {

	/**
	 * Create for a null-terminated array by reference. For {@code type**} array types.
	 */
	public static <T> ArrayPointer<T> byRef(Pointer p, Functions.Function<Pointer, T> constructor,
		Functions.IntFunction<T[]> arrayFn) {
		return of(p, i -> Jna.byRef(p, i, constructor),
			() -> Jna.arrayByRef(p, constructor, arrayFn), () -> Pointers.count(p));
	}

	/**
	 * Create for a fixed-length array by reference. For {@code type**} array types.
	 */
	public static <T> ArrayPointer<T> byRef(Pointer p, Functions.Function<Pointer, T> constructor,
		Functions.IntFunction<T[]> arrayFn, int count) {
		Validate.min(count, 0);
		return of(p, i -> Jna.byRef(p, Validate.index(count, i), constructor),
			() -> Jna.arrayByRef(p, constructor, arrayFn, count), () -> count);
	}

	/**
	 * Create for a fixed-length struct array by value. A null struct instance is created to
	 * determine the size. For {@code struct*} array types.
	 */
	public static <T extends Structure> ArrayPointer<T> byVal(Pointer p,
		Functions.Function<Pointer, T> constructor, Functions.IntFunction<T[]> arrayFn, int count) {
		return byVal(p, constructor, arrayFn, count, Struct.size(constructor));
	}

	/**
	 * Create for a fixed-length array by value of given type size. For {@code type*} array types.
	 */
	public static <T> ArrayPointer<T> byVal(Pointer p, Functions.Function<Pointer, T> constructor,
		Functions.IntFunction<T[]> arrayFn, int count, int size) {
		Validate.min(count, 0);
		return of(p, i -> Jna.byVal(p, Validate.index(count, i), constructor, size),
			() -> Jna.arrayByVal(p, constructor, arrayFn, count, size), () -> count);
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

	private static <T> ArrayPointer<T> of(Pointer p, Functions.IntFunction<T> getIndexFn,
		Functions.Supplier<T[]> getAllFn, Functions.IntSupplier countFn) {
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
