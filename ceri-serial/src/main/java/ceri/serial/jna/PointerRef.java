package ceri.serial.jna;

import java.util.function.Function;
import java.util.function.IntFunction;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

/**
 * A reference to a type pointer or array of type pointers. Allows a count to be stored with the
 * reference to generate arrays when needed. If a count is not specified any created arrays assume
 * null termination. Can be used to wrap a native-allocated list reference, to be freed after use.
 */
public class PointerRef<T> extends PointerType {
	private static final int NULL_TERM_COUNT = -1;
	private final Function<Pointer, T> constructor;
	private final IntFunction<T[]> arrayConstructor;
	private final int count;

	/**
	 * Encapsulates a pointer to a single type.
	 */
	public static <T> PointerRef<T> of(Pointer p, Function<Pointer, T> constructor) {
		return new PointerRef<>(p, constructor, null, 1);
	}

	/**
	 * Encapsulates a pointer to a typed null-terminated array.
	 */
	public static <T> PointerRef<T> ofNullTermArray(Pointer p, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayConstructor) {
		return new PointerRef<>(p, constructor, arrayConstructor, 0);
	}

	/**
	 * Encapsulates a pointer to a typed fixed-size array.
	 */
	public static <T> PointerRef<T> ofArray(Pointer p, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayConstructor, int count) {
		return new PointerRef<>(p, constructor, arrayConstructor, count);
	}

	private PointerRef(Pointer p, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayConstructor, int count) {
		setPointer(p);
		this.constructor = constructor;
		this.arrayConstructor = arrayConstructor;
		this.count = count;
	}

	/**
	 * Returns the stored array count value. Returns -1 if null-terminated array.   
	 */
	public int count() {
		return count;
	}

	/**
	 * Returns true if the is a reference to a null-terminated pointer array.
	 */
	public boolean isNullTerm() {
		return count == NULL_TERM_COUNT;
	}

	/**
	 * Constructs a single type from the pointer reference.
	 */
	public T value() {
		return constructor.apply(getPointer());
	}

	/**
	 * Constructs an array from the pointer reference. Structure types will need to call
	 * Struct.read() to populate values from memory.
	 */
	public T[] array() {
		if (arrayConstructor == null)
			throw new UnsupportedOperationException("Typed arrays are not supported");
		if (isNullTerm()) return JnaUtil.arrayByRef(getPointer(), constructor, arrayConstructor);
		return JnaUtil.arrayByRef(getPointer(), constructor, arrayConstructor, count);
	}

}
