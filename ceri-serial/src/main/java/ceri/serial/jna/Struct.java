package ceri.serial.jna;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.util.BasicUtil;

/**
 * Extends Structure to read fields when constructed from a pointer. Also makes array handling more
 * robust/typed.
 */
public abstract class Struct extends Structure {

	/**
	 * Creates a typed array of structures from reference pointer for a null-terminated array.
	 */
	public static <T extends Struct> T[] arrayByRef(Pointer p, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayConstructor) {
		if (p == null) return arrayConstructor.apply(0);
		Pointer[] refs = p.getPointerArray(0);
		return Stream.of(refs).map(constructor).toArray(arrayConstructor);
	}

	/**
	 * Creates a typed array of structures from reference pointer. If count is 0, returns empty
	 * array. Make sure count field is unsigned (call ubyte/ushort if needed).
	 */
	public static <T extends Struct> T[] arrayByRef(Pointer p, int count,
		Function<Pointer, T> constructor, IntFunction<T[]> arrayConstructor) {
		if (count == 0) return arrayConstructor.apply(0);
		if (p == null)
			throw new IllegalArgumentException("Null pointer but non-zero count: " + count);
		Pointer[] refs = p.getPointerArray(0, count);
		return Stream.of(refs).map(constructor).toArray(arrayConstructor);
	}

	/**
	 * Creates a typed array of structures from type. If count is 0, returns empty array. Make
	 * sure count field is unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	public static <T extends Struct> T[] array(T t, int count,
		Function<Pointer, T> constructor, IntFunction<T[]> arrayConstructor) {
		return array(JnaUtil.pointer(t), count, constructor, arrayConstructor);
	}

	/**
	 * Creates a typed array of structures from pointer. If count is 0, returns empty array. Make
	 * sure count field is unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	public static <T extends Struct> T[] array(Pointer p, int count,
		Function<Pointer, T> constructor, IntFunction<T[]> arrayConstructor) {
		if (count == 0) return arrayConstructor.apply(0);
		if (p != null) return array(count, constructor.apply(p), arrayConstructor);
		throw new IllegalArgumentException("Null pointer but non-zero count: " + count);
	}

	/**
	 * Creates a typed array of given structure. If count is 0, returns empty array. Make sure count
	 * field is unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	public static <T extends Struct> T[] array(int count, Supplier<T> constructor,
		IntFunction<T[]> arrayConstructor) {
		return array(count, constructor.get(), arrayConstructor);
	}

	/**
	 * Creates a typed array of given structure. If count is 0, returns empty array. Make sure count
	 * field is unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	public static <T extends Struct> T[] array(int count, T p, IntFunction<T[]> arrayConstructor) {
		if (count == 0) return arrayConstructor.apply(0);
		if (p != null) return BasicUtil.uncheckedCast(p.toArray(count));
		throw new IllegalArgumentException("Null pointer but non-zero count: " + count);
	}

	/**
	 * Constructor for a new struct without initialization.
	 */
	protected Struct() {}

	/**
	 * Use this constructor to initialize from a pointer. Read() is called automatically. If arrays
	 * must be initialized during construction, use constructor Struct(p, false).
	 */
	protected Struct(Pointer p) {
		this(p, true);
	}

	/**
	 * Use this constructor with read = false if arrays must be initialized in the calling
	 * constructor. Calling constructor should explicitly call read().
	 */
	protected Struct(Pointer p, boolean read) {
		super(p);
		if (read) read();
	}

	/**
	 * Overrides default behavior, allowing zero-length arrays with no instances created.
	 */
	@Override
	public Structure[] toArray(Structure[] array) {
		if (array.length == 0) return array;
		return super.toArray(array);
	}

	/**
	 * Returns byte array for field, from remaining bytes in structure. Make sure length field is
	 * unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	protected byte[] fieldByteArrayRem(String name, int structLen) {
		int offset = fieldOffset(name);
		if (offset >= structLen) return new byte[0];
		return getPointer().getByteArray(offset, structLen - offset);
	}

	/**
	 * Returns the pointer offset to the field.
	 */
	protected Pointer fieldPointer(String name) {
		int offset = fieldOffset(name);
		return getPointer().share(offset);
	}

	/**
	 * Creates a typed array of structures at given field. Used for marker field[0] with length
	 * given in another field value. Make sure count field is unsigned (call JnaUtil.ubyte/ushort if
	 * needed).
	 */
	protected <T extends Struct> T[] fieldArray(String name, int count,
		Function<Pointer, T> constructor, IntFunction<T[]> arrayConstructor) {
		if (count == 0) return arrayConstructor.apply(0);
		return array(fieldPointer(name), count, constructor, arrayConstructor);
	}

	/**
	 * Creates a typed array of structures referenced by given field pointer array. Used for marker
	 * *field[0] with length given in another field value. Make sure count field is unsigned (call
	 * JnaUtil.ubyte/ushort if needed).
	 */
	protected <T extends Struct> T[] fieldArrayByRef(String name, int count,
		Function<Pointer, T> constructor, IntFunction<T[]> arrayConstructor) {
		if (count == 0) return arrayConstructor.apply(0);
		return arrayByRef(fieldPointer(name), count, constructor, arrayConstructor);
	}

}
