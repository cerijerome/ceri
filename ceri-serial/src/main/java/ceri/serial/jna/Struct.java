package ceri.serial.jna;

import static ceri.common.reflect.ReflectUtil.publicField;
import static ceri.common.text.StringUtil.NEWLINE_REGEX;
import static ceri.common.text.StringUtil.format;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;

/**
 * Extends Structure to provide more array and general field support.
 */
public abstract class Struct extends Structure {
	private static final String INDENT = "\t";
	private static final int MAX_ARRAY = 8;

	public static enum Align {
		/** Use the platform default alignment. */
		platform(Structure.ALIGN_DEFAULT), // 0
		/** No alignment, place all fields on nearest 1-byte boundary */
		none(Structure.ALIGN_NONE), // 1
		/** validated for 32-bit x86 linux/gcc; align field size, max 4 bytes */
		gnuc(Structure.ALIGN_GNUC), // 2
		/** validated for w32/msvc; align on field size */
		msvc(Structure.ALIGN_MSVC); // 3

		final int value;

		private Align(int value) {
			this.value = value;
		}
	}

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
		if (array != null) for (T t : array)
			read(t, fieldNames);
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
		if (array != null) for (T t : array)
			write(t, fieldNames);
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
	 * Adapts one structure to another at the same pointer. The given structure must be synchronized
	 * with memory before calling this method.
	 */
	public static <T extends Structure> T adapt(Structure from, Function<Pointer, T> constructor) {
		if (from == null) return null;
		return Struct.read(constructor.apply(from.getPointer()));
	}

	/**
	 * Returns a typed contiguous array by value mapped to the given pointer. If count is 0, an
	 * empty array is returned. Make sure count is unsigned (call ubyte/ushort if needed).
	 */
	public static <T extends Structure> T[] arrayByVal(Pointer p, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayConstructor, int count) {
		if (count == 0) return arrayConstructor.apply(0);
		if (p != null) return arrayByVal(constructor.apply(p), arrayConstructor, count);
		throw new IllegalArgumentException("Null pointer but count > 0: " + count);
	}

	/**
	 * Creates a typed contiguous array by value. If count is 0, an empty array is returned. Make
	 * sure count is unsigned (call ubyte/ushort if needed).
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
	 * unsigned (call ubyte/ushort if needed).
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
	protected Struct() {
		this(null);
	}

	/**
	 * Use this constructor to initialize from a pointer.
	 */
	protected Struct(Pointer p) {
		this(p, Align.platform);
	}

	/**
	 * Use this constructor to initialize from a pointer.
	 */
	protected Struct(Pointer p, Align align) {
		super(p, align.value);
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
	 * Returns an inline byte array at given field.
	 */
	protected byte[] byteArray(String fieldName, int length) {
		return byteArray(fieldOffset(fieldName), length);
	}

	/**
	 * Returns an inline byte array at given offset.
	 */
	protected byte[] byteArray(int offset, int length) {
		return JnaUtil.byteArray(getPointer(), offset, length);
	}

	/**
	 * Returns an inline pointer array at given field.
	 */
	protected Pointer[] pointerArray(String fieldName, int length) {
		return pointerArray(fieldOffset(fieldName), length);
	}

	/**
	 * Returns an inline null-terminated pointer array at given field.
	 */
	protected Pointer[] pointerArray(String fieldName) {
		return pointerArray(fieldOffset(fieldName));
	}

	/**
	 * Returns an inline null-terminated pointer array at given offset.
	 */
	protected Pointer[] pointerArray(int offset) {
		return getPointer().getPointerArray(offset);
	}

	/**
	 * Returns an inline pointer array at given offset.
	 */
	protected Pointer[] pointerArray(int offset, int length) {
		return getPointer().getPointerArray(offset, length);
	}

	/**
	 * Returns a typed array from a null-terminated inline pointer array at given field.
	 */
	protected <T> T[] arrayByRef(String fieldName, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayConstructor) {
		return arrayByRef(fieldOffset(fieldName), constructor, arrayConstructor);
	}

	/**
	 * Returns a typed array from a null-terminated inline pointer array at given offset.
	 */
	protected <T> T[] arrayByRef(int offset, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayConstructor) {
		return JnaUtil.arrayByRef(getPointer().share(offset), constructor, arrayConstructor);
	}

	/**
	 * Returns a typed array from inline pointer array at given field.
	 */
	protected <T> T[] arrayByRef(String fieldName, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayConstructor, int count) {
		return arrayByRef(fieldOffset(fieldName), constructor, arrayConstructor, count);
	}

	/**
	 * Returns a typed array from inline pointer array at given offset.
	 */
	protected <T> T[] arrayByRef(int offset, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayConstructor, int count) {
		return JnaUtil.arrayByRef(getPointer().share(offset), constructor, arrayConstructor, count);
	}

	/**
	 * Returns an inline structure array at given field.
	 */
	protected <T extends Structure> T[] arrayByVal(String fieldName,
		Function<Pointer, T> constructor, IntFunction<T[]> arrayConstructor, int count) {
		return arrayByVal(fieldOffset(fieldName), constructor, arrayConstructor, count);
	}

	/**
	 * Returns an inline structure array at given offset.
	 */
	protected <T extends Structure> T[] arrayByVal(int offset, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayConstructor, int count) {
		return arrayByVal(getPointer().share(offset), constructor, arrayConstructor, count);
	}

	/**
	 * Convenience method to get the last field name.
	 */
	protected String lastName() {
		List<String> fieldNames = BasicUtil.uncheckedCast(getFieldOrder());
		return fieldNames.get(fieldNames.size() - 1);
	}

	/**
	 * Convenience method to get the last field offset.
	 */
	protected int lastOffset() {
		return fieldOffset(lastName());
	}

	/**
	 * Convenience method to get the named field type.
	 */
	protected Field lastField() {
		return field(lastName());
	}

	/**
	 * Convenience method to get the named field type.
	 */
	protected Field field(String name) {
		return publicField(getClass(), name);
	}

	@Override
	protected List<Field> getFieldList() {
		return BasicUtil.uncheckedCast(super.getFieldList());
	}

	@Override
	protected List<Field> getFields(boolean force) {
		return BasicUtil.uncheckedCast(super.getFields(force));
	}

	@Override
	protected abstract List<String> getFieldOrder();

	@Override
	public String toString() {
		return toString(this, INDENT, MAX_ARRAY);
	}

	protected static String toString(Struct s, String prefix, int maxArray) {
		StringBuilder b = new StringBuilder();
		var p = s.getPointer();
		var size = p instanceof Memory ? "" : "+" + Integer.toHexString(s.size());
		format(b, "%s(%s%s) {%n", ReflectUtil.name(s.getClass()), JnaUtil.print(p), size);
		for (String name : s.getFieldOrder()) {
			Field f = ReflectUtil.publicField(s.getClass(), name);
			if (f == null) continue;
			int offset = s.fieldOffset(name);
			String type = ReflectUtil.name(f.getType());
			String value = string(ReflectUtil.publicValue(s, name), prefix, maxArray);
			format(b.append(prefix), "+%x:@%x %s %s = %s%n", offset,
				Pointer.nativeValue(p.share(offset)), type, f.getName(), prefix(value, prefix));
		}
		return b.append("}").toString();
	}

	private static String prefix(String s, String prefix) {
		return NEWLINE_REGEX.matcher(s).replaceAll("$1" + prefix);
	}

	private static String string(Object obj, String prefix, int maxArray) {
		if (obj == null) return "null";
		Class<?> cls = obj.getClass();
		if (cls.isArray()) return arrayString(obj, prefix, maxArray);
		if (cls == Byte.class) return "0x" + StringUtil.toHex((Byte) obj);
		if (cls == Short.class) return "0x" + StringUtil.toHex((Short) obj);
		if (cls == Integer.class) return "0x" + StringUtil.toHex((Integer) obj);
		if (cls == Long.class) return "0x" + StringUtil.toHex((Long) obj);
		if (Pointer.class.isAssignableFrom(cls)) return JnaUtil.print((Pointer) obj);
		if (Struct.class.isAssignableFrom(cls)) return toString((Struct) obj, prefix, maxArray);
		return String.valueOf(obj);
	}

	private static String arrayString(Object array, String prefix, int maxArray) {
		int n = Array.getLength(array);
		StringBuilder b = new StringBuilder("[");
		int max = n <= maxArray ? n : maxArray - 1;
		for (int i = 0; i < max; i++)
			b.append(i > 0 ? ", " : "").append(string(Array.get(array, i), prefix, maxArray));
		if (n > maxArray) b.append(", ... (").append(n).append(")");
		return b.append("]").toString();
	}
}
