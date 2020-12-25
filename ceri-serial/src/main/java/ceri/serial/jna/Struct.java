package ceri.serial.jna;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.util.BasicUtil;

/**
 * Extends Structure to read fields when constructed from a pointer. Also makes array handling more
 * robust/typed.
 */
public abstract class Struct extends Structure {

	/**
	 * Returns the pointer to the first element of the array, or null if empty. The array is not
	 * required to be contiguous.
	 */
	public static Pointer pointer(Structure[] array) {
		if (array == null || array.length == 0) return null;
		return pointer(array[0]);
	}

	/**
	 * Returns the pointer, or null.
	 */
	public static Pointer pointer(Structure struct) {
		return struct == null ? null : struct.getPointer();
	}

	/**
	 * Reads the fields for given struct array.
	 */
	public static <T extends Structure> T[] read(T[] array, String... fieldNames) {
		if (array != null) for (T t : array) read(t, fieldNames);
		return array;
	}

	/**
	 * Convenience method to read fields from memory. Useful with constructor from pointer. Specify
	 * field names to read, or none for all fields.
	 */
	public static <T extends Structure> T read(T t, String... fieldNames) {
		if (t == null) return null;
		if (fieldNames.length == 0) t.read();
		else for (var name : fieldNames)
			t.readField(name);
		return t;
	}

	/**
	 * Writes the fields for given struct array.
	 */
	public static <T extends Structure> T[] write(T[] array, String... fieldNames) {
		if (array != null) for (T t : array) write(t, fieldNames);
		return array;
	}

	/**
	 * Convenience method to write fields to memory. Useful with argument to native call. Specify
	 * field names to write, or none for all fields.
	 */
	public static <T extends Structure> T write(T t, String... fieldNames) {
		if (t == null) return null;
		if (fieldNames.length == 0) t.write();
		else for (var name : fieldNames)
			t.writeField(name);
		return t;
	}

	/**
	 * Returns a typed contiguous array by value mapped to the given pointer. If count is 0, an
	 * empty array is returned. Make sure count is unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	public static <T extends Structure> T[] arrayByVal(Pointer p, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayConstructor, int count) {
		if (count == 0) return arrayConstructor.apply(0);
		if (p != null) return arrayByVal(constructor.apply(p), arrayConstructor, count);
		throw new IllegalArgumentException("Null pointer but count > 0: " + count);
	}

	/**
	 * Creates a typed contiguous array by value. If count is 0, an empty array is returned. Make
	 * sure count is unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	public static <T extends Structure> T[] arrayByVal(Supplier<T> constructor,
		IntFunction<T[]> arrayConstructor, int count) {
		if (count == 0) return arrayConstructor.apply(0);
		return arrayByVal(constructor.get(), arrayConstructor, count);
	}

	/**
	 * Returns a typed contiguous array by value with the given type at index 0. If the type was
	 * constructed, the memory will be resized to fit the array. If allocated by native code, the
	 * array will map to the pointer. If count is 0, an empty array is returned. Make sure count is
	 * unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	public static <T extends Structure> T[] arrayByVal(T t, IntFunction<T[]> arrayConstructor,
		int count) {
		if (count == 0) return arrayConstructor.apply(0);
		if (t != null) return BasicUtil.uncheckedCast(t.toArray(count));
		throw new IllegalArgumentException("Null pointer but count > 0: " + count);
	}

	/**
	 * Constructor for a new struct without initialization.
	 */
	protected Struct() {}

	/**
	 * Use this constructor to initialize from a pointer.
	 */
	protected Struct(Pointer p) {
		super(p);
	}

	@Override
	public void read() {
		super.read();
	}

	@Override
	public void write() {
		super.write();
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
	 * Returns the pointer offset to the field.
	 */
	protected Pointer fieldPointer(String name) {
		return getPointer().share(fieldOffset(name));
	}

	/**
	 * Returns a byte array copied from the end of the structure. Useful to get a variable-length
	 * byte array after read(). Make sure length is unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	protected byte[] varByteArray(int len) {
		return JnaUtil.byteArray(varPointer(), 0, len);
	}

	/**
	 * Returns a typed array constructed from a contiguous null-terminated pointer array at the end
	 * of the structure. Useful to get a variable-length typed array after read().
	 */
	protected <T extends Struct> T[] varArrayByRef(Function<Pointer, T> constructor,
		IntFunction<T[]> arrayConstructor) {
		return JnaUtil.arrayByRef(varPointer(), constructor, arrayConstructor);
	}

	/**
	 * Returns a typed array constructed from a contiguous pointer array at the end of the
	 * structure. Useful to get a variable-length typed array after read(). Make sure count is
	 * unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	protected <T extends Struct> T[] varArrayByRef(Function<Pointer, T> constructor,
		IntFunction<T[]> arrayConstructor, int count) {
		return JnaUtil.arrayByRef(varPointer(), constructor, arrayConstructor, count);
	}

	/**
	 * Returns a typed contiguous array by value mapped to the end of the structure. Useful to get a
	 * variable-length typed array after read(). Make sure count is unsigned (call
	 * JnaUtil.ubyte/ushort if needed).
	 */
	protected <T extends Struct> T[] varArrayByVal(Function<Pointer, T> constructor,
		IntFunction<T[]> arrayConstructor, int count) {
		return arrayByVal(varPointer(), constructor, arrayConstructor, count);
	}

	/**
	 * Returns a pointer to the end of the structure. Useful to get variable-length data or pointer
	 * arrays.
	 */
	protected Pointer varPointer() {
		return getPointer().share(size());
	}

	/**
	 * Returns fields with option to skip the named field. Useful to call from overridden
	 * getFields(force) when the last field is a 0+ variable-size array.
	 */
	protected List<Field> varFields(boolean force, boolean skip, String name) {
		if (!force) return null;
		List<Field> fields = BasicUtil.uncheckedCast(super.getFields(force));
		if (skip) fields.removeIf(f -> name.equals(f.getName()));
		return fields;
	}

}
