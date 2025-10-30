package ceri.jna.util;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.PointerByReference;
import ceri.common.function.Functions;
import ceri.common.stream.Streams;
import ceri.common.util.Validate;
import ceri.jna.type.JnaSize;

/**
 * Utilities for pointers.
 */
public class Pointers {

	private Pointers() {}

	/**
	 * Validates an offset and length from a given pointer. If the pointer is null, only 0 length is
	 * allowed.
	 */
	public static Pointer validate(Pointer p, long offset, long len) {
		if (p == null) Jna.validateSlice(0, offset, len);
		else Validate.min(len, 0);
		Validate.min(offset, 0);
		return p;
	}

	/**
	 * Validates an offset and length from a given memory segment. If the pointer is null, only 0
	 * size is allowed.
	 */
	public static Pointer validate(Pointer p, long size, long offset, long len) {
		if (p == null) Validate.equal(size, 0);
		Jna.validateSlice(size, offset, len);
		return p;
	}

	/**
	 * Returns the native peer for a pointer, or 0L if null. Use with caution.
	 */
	public static long peer(Pointer p) {
		return Pointer.nativeValue(p);
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
			offset += JnaSize.POINTER.get();
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
		long peer0 = Pointers.peer(p0) + p0Offset;
		long peer1 = Pointers.peer(p1) + p1Offset;
		return (peer1 < peer0 + size) && (peer0 < peer1 + size);
	}

	/**
	 * Creates a fixed-length contiguous pointer array. For {@code void*} array types.
	 */
	public static Pointer[] mallocArray(int count) {
		return mallocArray(count, JnaSize.POINTER.get());
	}

	/**
	 * Creates and returns pointers to a fixed-length contiguous array of given type size. For
	 * {@code type*} array types.
	 */
	public static Pointer[] mallocArray(int count, int size) {
		return arrayByVal(GcMemory.malloc(count * size).m, count, size);
	}

	/**
	 * Creates a fixed-length contiguous pointer type array. For {@code void*} array types.
	 */
	public static <T extends PointerType> T[] mallocArray(Functions.Supplier<T> constructor,
		Functions.IntFunction<T[]> arrayFn, int count) {
		return arrayByVal(GcMemory.malloc(count * JnaSize.POINTER.get()).m, constructor, arrayFn,
			count);
	}

	/**
	 * Creates a zeroed fixed-length contiguous pointer array. For {@code void*} array types.
	 */
	public static Pointer[] callocArray(int count) {
		return callocArray(count, JnaSize.POINTER.get());
	}

	/**
	 * Creates and returns pointers to a zeroed fixed-length contiguous array of given type size.
	 * For {@code type*} array types.
	 */
	public static Pointer[] callocArray(int count, int size) {
		return arrayByVal(GcMemory.malloc(count * size).clear().m, count, size);
	}

	/**
	 * Creates a zeroed fixed-length contiguous pointer type array. For {@code void*} array types.
	 */
	public static <T extends PointerType> T[] callocArray(Functions.Supplier<T> constructor,
		Functions.IntFunction<T[]> arrayFn, int count) {
		return arrayByVal(GcMemory.malloc(count * JnaSize.POINTER.get()).clear().m, constructor,
			arrayFn, count);
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
	public static <T extends PointerType> T[] arrayByRef(Pointer p,
		Functions.Supplier<T> constructor, Functions.IntFunction<T[]> arrayFn) {
		return Jna.arrayByRef(p, adapt(constructor), arrayFn);
	}

	/**
	 * Creates a typed array constructed from a contiguous pointer array at the given pointer. If
	 * the pointer is null, or count is 0, an empty array is returned. Make sure count is unsigned
	 * (call JnaUtil.ubyte/ushort if needed).
	 */
	public static <T extends PointerType> T[] arrayByRef(Pointer p,
		Functions.Supplier<T> constructor, Functions.IntFunction<T[]> arrayFn, int count) {
		return Jna.arrayByRef(p, adapt(constructor), arrayFn, count);
	}

	/**
	 * Get the pointers for a fixed-length contiguous pointer array. Returns an array of null
	 * pointers if the pointer is null. For {@code void*} array types.
	 */
	public static Pointer[] arrayByVal(Pointer p, int count) {
		return arrayByVal(p, count, JnaSize.POINTER.get());
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
	public static <T extends PointerType> T[] arrayByVal(Pointer p,
		Functions.Supplier<T> constructor, Functions.IntFunction<T[]> arrayFn, int count) {
		return Streams.of(Pointers.arrayByVal(p, count)).map(Jna.typeFn(adapt(constructor)))
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
		return p == null ? null : p.getPointer(i * JnaSize.POINTER.get());
	}

	/**
	 * Get the pointer to index i of a contiguous pointer array. For {@code void*} array types.
	 */
	public static Pointer byVal(Pointer p, int i) {
		return byVal(p, i, JnaSize.POINTER.get());
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
	public static <T extends PointerType> T byVal(Pointer p, int i,
		Functions.Supplier<T> constructor) {
		return Jna.type(Pointers.byVal(p, i), adapt(constructor));
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
	public static <T extends PointerType> Functions.Function<Pointer, T>
		adapt(Functions.Supplier<T> constructor) {
		return p -> set(constructor.get(), p);
	}
}
