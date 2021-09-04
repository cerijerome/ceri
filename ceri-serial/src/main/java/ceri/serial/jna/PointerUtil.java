package ceri.serial.jna;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.PointerByReference;

/**
 * Utilities for pointers.
 */
public class PointerUtil {

	private PointerUtil() {}

	/**
	 * Get index i of indirected pointer array. 
	 */
	public static Pointer ref(Pointer p, int i) {
		return p == null ? null : p.getPointer(i * Pointer.SIZE);
	}

	/**
	 * Returns the pointer, or null.
	 */
	public static Pointer pointer(PointerType type) {
		return type == null ? null : type.getPointer();
	}

	/**
	 * Convenience method set a pointer. Can combine with construction.
	 */
	public static <T extends PointerType> T set(T t, Pointer p) {
		t.setPointer(p);
		return t;
	}

	/**
	 * Convenience method set a pointer. Can combine with construction.
	 */
	public static <T extends PointerType> T set(T t, PointerByReference p) {
		return set(t, p.getValue());
	}

	/**
	 * Encapsulates a pointer to a single type.
	 */
	public static <T extends PointerType> PointerRef<T> ref(Pointer p, Supplier<T> constructor) {
		return PointerRef.of(p, adapt(constructor));
	}

	/**
	 * Encapsulates a pointer to a typed null-terminated array.
	 */
	public static <T extends PointerType> PointerRef<T> nullTermArrayRef(Pointer p,
		Supplier<T> constructor, IntFunction<T[]> arrayConstructor) {
		return PointerRef.ofNullTermArray(p, adapt(constructor), arrayConstructor);
	}

	/**
	 * Encapsulates a pointer to a typed fixed-size array.
	 */
	public static <T extends PointerType> PointerRef<T> arrayRef(Pointer p, Supplier<T> constructor,
		IntFunction<T[]> arrayConstructor, int count) {
		return PointerRef.ofArray(p, adapt(constructor), arrayConstructor, count);
	}

	/**
	 * Creates a typed array constructed from a contiguous null-terminated pointer array at the
	 * given pointer. If the pointer is null, or count is 0, an empty array is returned.
	 */
	public static <T extends PointerType> T[] arrayByRef(Pointer p, Supplier<T> constructor,
		IntFunction<T[]> arrayConstructor) {
		return JnaUtil.arrayByRef(p, adapt(constructor), arrayConstructor);
	}

	/**
	 * Creates a typed array constructed from a contiguous pointer array at the given pointer. If
	 * the pointer is null, or count is 0, an empty array is returned. Make sure count is unsigned
	 * (call JnaUtil.ubyte/ushort if needed).
	 */
	public static <T extends PointerType> T[] arrayByRef(Pointer p, Supplier<T> constructor,
		IntFunction<T[]> arrayConstructor, int count) {
		return JnaUtil.arrayByRef(p, adapt(constructor), arrayConstructor, count);
	}

	/**
	 * Convenience constructor for a pointer type at given pointer.
	 */
	private static <T extends PointerType> Function<Pointer, T> adapt(Supplier<T> constructor) {
		return p -> set(constructor.get(), p);
	}

}
