package ceri.serial.jna;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;
import com.sun.jna.IntegerType;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.PointerByReference;

/**
 * Utilities for pointers.
 */
public class PointerUtil {

	private PointerUtil() {}

	/**
	 * An integer with pointer storage size.
	 */
	@SuppressWarnings("serial")
	public static class Int extends IntegerType {
		public Int() {
			this(0);
		}

		public Int(long value) {
			super(Native.POINTER_SIZE, value);
		}
	}

	/**
	 * Returns the native peer for a pointer, or 0L if null. Use with caution.
	 */
	public static long peer(Pointer p) {
		return p == null ? 0L : Pointer.nativeValue(p);
	}

	/**
	 * Returns a pointer from the native peer, or null if 0. Use with caution.
	 */
	public static Pointer pointer(long peer) {
		return peer == 0L ? null : new Pointer(peer);
	}

	/**
	 * Returns the pointer type's pointer, or null if the pointer type is null.
	 */
	public static Pointer pointer(PointerType type) {
		return type == null ? null : type.getPointer();
	}

	/**
	 * Gets the pointer offset by the given number of bytes.
	 */
	public static Pointer offset(Pointer p, long offset) {
		return p == null || offset == 0 ? p : p.share(offset);
	}

	/**
	 * Gets the difference between two pointers. Returns null if either pointer is null.
	 */
	public static Long diff(Pointer p0, Pointer p1) {
		return p0 == null || p1 == null ? null : peer(p0) - peer(p1);
	}

	/**
	 * Counts the number of pointers in a null-terminated contiguous pointer array.
	 */
	public static int count(Pointer p) {
		if (p == null) return 0;
		long offset = 0;
		for (int i = 0;; i++) {
			if (p.getPointer(offset) == null) return i;
			offset += Native.POINTER_SIZE;
		}
	}

	/**
	 * Detects if the regions overlap.
	 */
	public static boolean overlap(Pointer p0, Pointer p1, int size) {
		return overlap(p0, 0, p1, 0, size);
	}

	/**
	 * Detects if the regions overlap.
	 */
	public static boolean overlap(Pointer p0, long p0Offset, Pointer p1, long p1Offset, int size) {
		long peer0 = PointerUtil.peer(p0) + p0Offset;
		long peer1 = PointerUtil.peer(p1) + p1Offset;
		return (peer1 < peer0 + size) && (peer0 < peer1 + size);
	}

	/**
	 * Creates a fixed-length contiguous pointer array. For {@code void*} array types.
	 */
	public static Pointer[] mallocArray(int count) {
		return mallocArray(count, Native.POINTER_SIZE);
	}

	/**
	 * Creates and returns pointers to a fixed-length contiguous array of given type size. For
	 * {@code type*} array types.
	 */
	public static Pointer[] mallocArray(int count, int size) {
		return arrayByVal(JnaUtil.malloc(count * size), count, size);
	}

	/**
	 * Creates a fixed-length contiguous pointer type array. For {@code void*} array types.
	 */
	public static <T extends PointerType> T[] mallocArray(Supplier<T> constructor,
		IntFunction<T[]> arrayFn, int count) {
		return arrayByVal(JnaUtil.malloc(count * Native.POINTER_SIZE), constructor, arrayFn, count);
	}

	/**
	 * Creates a zeroed fixed-length contiguous pointer array. For {@code void*} array types.
	 */
	public static Pointer[] callocArray(int count) {
		return callocArray(count, Native.POINTER_SIZE);
	}

	/**
	 * Creates and returns pointers to a zeroed fixed-length contiguous array of given type size.
	 * For {@code type*} array types.
	 */
	public static Pointer[] callocArray(int count, int size) {
		return arrayByVal(JnaUtil.calloc(count * size), count, size);
	}

	/**
	 * Creates a zeroed fixed-length contiguous pointer type array. For {@code void*} array types.
	 */
	public static <T extends PointerType> T[] callocArray(Supplier<T> constructor,
		IntFunction<T[]> arrayFn, int count) {
		return arrayByVal(JnaUtil.calloc(count * Native.POINTER_SIZE), constructor, arrayFn, count);
	}

	/**
	 * Get an indirected null-terminated array of pointers. Returns an empty array if the pointer is
	 * null. For {@code struct**} array types.
	 */
	public static Pointer[] arrayByRef(Pointer p) {
		return p == null ? new Pointer[0] : p.getPointerArray(0);
	}

	/**
	 * Get an indirected fixed-length array of pointers. Returns an array of null pointers if the
	 * pointer is null. For {@code struct**} array types.
	 */
	public static Pointer[] arrayByRef(Pointer p, int count) {
		return p == null ? new Pointer[count] : p.getPointerArray(0, count);
	}

	/**
	 * Creates a typed array constructed from a contiguous null-terminated pointer array at the
	 * given pointer. If the pointer is null, or count is 0, an empty array is returned.
	 */
	public static <T extends PointerType> T[] arrayByRef(Pointer p, Supplier<T> constructor,
		IntFunction<T[]> arrayFn) {
		return JnaUtil.arrayByRef(p, adapt(constructor), arrayFn);
	}

	/**
	 * Creates a typed array constructed from a contiguous pointer array at the given pointer. If
	 * the pointer is null, or count is 0, an empty array is returned. Make sure count is unsigned
	 * (call JnaUtil.ubyte/ushort if needed).
	 */
	public static <T extends PointerType> T[] arrayByRef(Pointer p, Supplier<T> constructor,
		IntFunction<T[]> arrayFn, int count) {
		return JnaUtil.arrayByRef(p, adapt(constructor), arrayFn, count);
	}

	/**
	 * Get the pointers for a fixed-length contiguous pointer array. Returns an array of null
	 * pointers if the pointer is null. For {@code void*} array types.
	 */
	public static Pointer[] arrayByVal(Pointer p, int count) {
		return arrayByVal(p, count, Native.POINTER_SIZE);
	}

	/**
	 * Get the pointers for a fixed-length contiguous array of given type size. Returns an array of
	 * null pointers if the pointer is null. For {@code struct*} array types.
	 */
	public static Pointer[] arrayByVal(Pointer p, int count, int size) {
		Pointer[] array = new Pointer[count];
		for (int i = 0; i < count; i++)
			array[i] = byVal(p, i, size);
		return array;
	}

	/**
	 * Creates a typed array from a fixed-length contiguous pointer type array. Returns an array of
	 * null pointers if the pointer is null. For {@code void*} array types.
	 */
	public static <T extends PointerType> T[] arrayByVal(Pointer p, Supplier<T> constructor,
		IntFunction<T[]> arrayFn, int count) {
		return Stream.of(PointerUtil.arrayByVal(p, count)).map(JnaUtil.typeFn(adapt(constructor)))
			.toArray(arrayFn);
	}

	/**
	 * Get an indirected pointer. Returns null if the pointer is null. For {@code type**} types.
	 */
	public static Pointer byRef(Pointer p) {
		return byRef(p, 0);
	}

	/**
	 * Get index i of an indirected array of pointers. Returns null if the pointer is null. For
	 * {@code struct**} array types.
	 */
	public static Pointer byRef(Pointer p, int i) {
		return p == null ? null : p.getPointer(i * Native.POINTER_SIZE);
	}

	/**
	 * Get the pointer to index i of a contiguous pointer array. For {@code void*} array types.
	 */
	public static Pointer byVal(Pointer p, int i) {
		return byVal(p, i, Native.POINTER_SIZE);
	}

	/**
	 * Get the pointer to index i of a contiguous array of given type size. For {@code struct*}
	 * array types.
	 */
	public static Pointer byVal(Pointer p, int i, int size) {
		return p == null ? null : p.share(i * size, size);
	}

	/**
	 * Creates a type from index i of a contiguous pointer type array. Returns null if the pointer
	 * is null. For {@code void*} array types.
	 */
	public static <T extends PointerType> T byVal(Pointer p, int i, Supplier<T> constructor) {
		return JnaUtil.type(PointerUtil.byVal(p, i), adapt(constructor));
	}

	/**
	 * Sets the pointer type's pointer value. Useful to combine with construction.
	 */
	public static <T extends PointerType> T set(T t, Pointer p) {
		if (t != null) t.setPointer(p);
		return t;
	}

	/**
	 * Sets the pointer type's pointer value. Useful to combine with construction.
	 */
	public static <T extends PointerType> T set(T t, PointerByReference p) {
		return p == null ? t : set(t, p.getValue());
	}

	/**
	 * Adapts no-arg pointer type constructor to take a pointer argument.
	 */
	public static <T extends PointerType> Function<Pointer, T> adapt(Supplier<T> constructor) {
		return p -> set(constructor.get(), p);
	}

}
