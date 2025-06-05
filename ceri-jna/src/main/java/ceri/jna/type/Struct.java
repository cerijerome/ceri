package ceri.jna.util;

import static ceri.common.reflect.ReflectUtil.publicField;
import static ceri.common.text.StringUtil.NEWLINE_REGEX;
import static ceri.common.text.StringUtil.format;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.collection.ImmutableUtil;
import ceri.common.reflect.AnnotationUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;

/**
 * Extends Structure to provide more array and general field support.
 */
public abstract class Struct extends Structure {
	private static final Map<Class<?>, Align> align = new ConcurrentHashMap<>();
	private static final Map<Class<?>, List<String>> fields = new ConcurrentHashMap<>();
	private static final JnaArgs ARGS = JnaArgs.builder().addDefault(false).build();
	private static final String INDENT = "\t";

	/**
	 * Annotation for declaring Structure field order. All fields must be named in subclasses, not
	 * just added fields. This allows fields to be inserted at any point, rather than appended to
	 * the end of the parent field list.
	 * <p/>
	 * Replace with @FieldOrder when moving to JNA5 (and check subclassed Structures)
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface Fields {
		String[] value();
	}

	/**
	 * Annotation for declaring a struct with {@code __attribute__ ((packed))}
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface Packed {}

	/**
	 * Structure alignment constants as an enum.
	 */
	public static enum Align {
		/** Use the platform default alignment. */
		platform(Structure.ALIGN_DEFAULT), // 0
		/** No alignment, place all fields on nearest 1-byte boundary */
		none(Structure.ALIGN_NONE), // 1
		/** Validated for 32-bit x86 linux/gcc; align field size, max 4 bytes */
		gnuc(Structure.ALIGN_GNUC), // 2
		/** Validated for w32/msvc; align on field size */
		msvc(Structure.ALIGN_MSVC); // 3

		public final int value;

		private Align(int value) {
			this.value = value;
		}
	}

	/**
	 * Read a struct/union field and cast to type.
	 */
	public static <T> T readField(Structure struct, String name) {
		if (struct == null) return null;
		return BasicUtil.uncheckedCast(struct.readField(name));
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
	 * Determines the size of a struct using its constructor. Creates an instance from a null
	 * pointer to determine the size.
	 */
	public static <T extends Structure> int size(Function<Pointer, T> constructor) {
		Objects.requireNonNull(constructor);
		return size(constructor.apply(null));
	}

	/**
	 * Safely determines the size of a struct. Returns 0 if null.
	 */
	public static int size(Structure t) {
		return t == null ? 0 : t.size();
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
	 * Auto-reads the given struct array.
	 */
	public static <T extends Structure> T[] readAuto(T[] array) {
		if (array != null) for (T t : array)
			readAuto(t);
		return array;
	}

	/**
	 * Convenience method to auto-read struct fields.
	 */
	public static <T extends Structure> T readAuto(T t) {
		if (t != null) t.autoRead();
		return t;
	}

	/**
	 * Auto-writes the given struct array.
	 */
	public static <T extends Structure> T[] writeAuto(T[] array) {
		if (array != null) for (T t : array)
			writeAuto(t);
		return array;
	}

	/**
	 * Convenience method to auto-write struct fields.
	 */
	public static <T extends Structure> T writeAuto(T t) {
		if (t != null) t.autoWrite();
		return t;
	}

	/**
	 * Adapts one structure to another at the same pointer, and calls autoRead(). The given
	 * structure must be synchronized with memory before calling this method.
	 */
	public static <T extends Structure> T adapt(Structure from, Function<Pointer, T> constructor) {
		return from == null ? null : readAuto(constructor.apply(from.getPointer()));
	}

	/**
	 * Copies a structure to another memory location with auto-read. The given structure must be
	 * synchronized with memory before calling this method.
	 */
	public static <T extends Structure> T copy(T from, Pointer to,
		Function<Pointer, T> constructor) {
		var t = constructor.apply(to);
		if (from != null) JnaUtil.memmove(t.getPointer(), 0, from.getPointer(), 0, from.size());
		return readAuto(t);
	}

	/**
	 * Copies the contents of one structure to another. From and to structures are both synchronized
	 * with memory. Returns the receiving structure.
	 */
	public static <T extends Structure> T copy(T from, T to) {
		if (from == to || from == null || to == null) return to;
		writeAuto(from);
		return copyFrom(from.getPointer(), to);
	}

	/**
	 * Copies the data from the pointer to the structure. The structure is synchronized with memory.
	 */
	public static <T extends Structure> T copyFrom(Pointer from, T to) {
		if (from == null || to == null || to.getPointer().equals(from)) return to;
		JnaUtil.memmove(to.getPointer(), 0, from, 0, to.size());
		return readAuto(to);
	}

	/**
	 * Copies the data from the structure to the pointer. The structure is synchronized with memory.
	 */
	public static <T extends Structure> T copyTo(T from, Pointer to) {
		if (from == null || to == null || from.getPointer().equals(to)) return from;
		writeAuto(from);
		JnaUtil.memmove(to, 0, from.getPointer(), 0, from.size());
		return from;
	}

	/**
	 * Creates a typed contiguous array. If count is 0, an empty array is returned. Creates a type
	 * instance from a null pointer first, to determine size. For {@code struct*} array types.
	 */
	public static <T extends Structure> T[] mallocArray(Function<Pointer, T> constructor,
		IntFunction<T[]> arrayFn, int count) {
		if (count == 0) return arrayFn.apply(0);
		return JnaUtil.mallocArray(constructor, arrayFn, count, size(constructor));
	}

	/**
	 * Creates a zeroed typed contiguous array. If count is 0, an empty array is returned. Creates a
	 * type instance from a null pointer first, to determine size. For {@code struct*} array types.
	 */
	public static <T extends Structure> T[] callocArray(Function<Pointer, T> constructor,
		IntFunction<T[]> arrayFn, int count) {
		if (count == 0) return arrayFn.apply(0);
		return JnaUtil.callocArray(constructor, arrayFn, count, size(constructor));
	}

	/**
	 * Creates a typed contiguous array at pointer. If count is 0, an empty array is returned. Make
	 * sure count is unsigned (call ubyte/ushort if needed). {@code autoRead()} is called on each
	 * array item. For {@code struct*} array types.
	 */
	public static <T extends Structure> T[] arrayByVal(Pointer p, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayFn, int count) {
		if (count == 0) return arrayFn.apply(0);
		return arrayByVal(readAuto(JnaUtil.type(p, constructor)), arrayFn, count);
	}

	/**
	 * Creates a typed contiguous array. If count is 0, an empty array is returned. Make sure count
	 * is unsigned (call ubyte/ushort if needed). {@code autoRead()} is called on each array item.
	 */
	public static <T extends Structure> T[] arrayByVal(Supplier<T> constructor,
		IntFunction<T[]> arrayFn, int count) {
		if (count == 0) return arrayFn.apply(0);
		return arrayByVal(readAuto(constructor.get()), arrayFn, count);
	}

	/**
	 * Creates a typed contiguous array, with the given type at index 0. If the type was
	 * constructed, the memory will be resized to fit the array. If allocated by native code, the
	 * array will map to the pointer. {@code autoRead()} is called by Structure code on each new
	 * item of the array.
	 * <p/>
	 * If count is 0, an empty array is returned. If the type is null, an array of nulls is
	 * returned. Make sure count is unsigned (call ubyte/ushort if needed).
	 */
	public static <T extends Structure> T[] arrayByVal(T t, IntFunction<T[]> arrayFn, int count) {
		if (count == 0 || t == null) return arrayFn.apply(count);
		return BasicUtil.uncheckedCast(t.toArray(count));
	}

	/**
	 * Creates a type from index i of a contiguous type array. Returns null if the pointer is null.
	 * {@code autoRead()} is called on the instance. Creates an empty struct to determine struct
	 * size. For {@code struct*} array types.
	 */
	public static <T extends Structure> T byVal(Pointer p, int i,
		Function<Pointer, T> constructor) {
		return readAuto(JnaUtil.byVal(p, i, constructor, size(constructor)));
	}

	/**
	 * Returns true if the class implements Structure.ByReference.
	 */
	public static boolean isByRef(Class<?> cls) {
		if (cls == null) return false;
		return Structure.ByReference.class.isAssignableFrom(cls);
	}

	/**
	 * Returns true if the class implements Structure.ByValue.
	 */
	public static boolean isByVal(Class<?> cls) {
		if (cls == null) return false;
		return Structure.ByValue.class.isAssignableFrom(cls);
	}

	/**
	 * Returns true if the array is contiguous.
	 */
	public static <T extends Structure> boolean isByVal(T[] array) {
		if (array == null || (array.length > 0 && array[0] == null)) return false;
		if (array.length <= 1) return true;
		Pointer p0 = pointer(array);
		int size = array[0].size();
		for (int i = 1; i < array.length; i++) {
			if (array[i] == null) return false;
			if (!p0.share(i * size).equals(array[i].getPointer())) return false;
		}
		return true;
	}

	/**
	 * Provides a compact string suitable for error messages.
	 */
	public static String compactString(Structure t) {
		return String.format("%s(@%x+%x)", ReflectUtil.nestedName(t.getClass()),
			Pointer.nativeValue(t.getPointer()), t.size());
	}

	/**
	 * Provides an expanded string representation of a structure.
	 */
	static String toString(Structure s, List<String> fields, ToIntFunction<String> fieldOffsetFn) {
		if (s == null) return StringUtil.NULL_STRING;
		Class<?> cls = s.getClass();
		StringBuilder b = new StringBuilder();
		format(b, "%s(%s) {%n", ReflectUtil.nestedName(cls), JnaArgs.string(s.getPointer()));
		for (String name : fields) {
			Field f = Objects.requireNonNull(ReflectUtil.publicField(cls, name));
			int offset = fieldOffsetFn.applyAsInt(name);
			b.append(INDENT).append(fieldString(s, f, offset));
		}
		return b.append("}").toString();
	}

	/**
	 * Constructor for a new struct without initialization.
	 */
	protected Struct() {
		this(null);
		setAlignType(ALIGN_DEFAULT);
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

	@Override
	public String toString() {
		return toString(this, getFieldOrder(), this::fieldOffset);
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
		return JnaUtil.bytes(getPointer(), offset, length);
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
		IntFunction<T[]> arrayFn) {
		return arrayByRef(fieldOffset(fieldName), constructor, arrayFn);
	}

	/**
	 * Returns a typed array from a null-terminated inline pointer array at given offset.
	 */
	protected <T> T[] arrayByRef(int offset, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayFn) {
		return JnaUtil.arrayByRef(getPointer().share(offset), constructor, arrayFn);
	}

	/**
	 * Returns a typed array from inline pointer array at given field.
	 */
	protected <T> T[] arrayByRef(String fieldName, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayFn, int count) {
		return arrayByRef(fieldOffset(fieldName), constructor, arrayFn, count);
	}

	/**
	 * Returns a typed array from inline pointer array at given offset.
	 */
	protected <T> T[] arrayByRef(int offset, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayFn, int count) {
		return JnaUtil.arrayByRef(getPointer().share(offset), constructor, arrayFn, count);
	}

	/**
	 * Returns an inline structure array at given field.
	 */
	protected <T extends Structure> T[] arrayByVal(String fieldName,
		Function<Pointer, T> constructor, IntFunction<T[]> arrayFn, int count) {
		return arrayByVal(fieldOffset(fieldName), constructor, arrayFn, count);
	}

	/**
	 * Returns an inline structure array at given offset.
	 */
	protected <T extends Structure> T[] arrayByVal(int offset, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayFn, int count) {
		return arrayByVal(getPointer().share(offset), constructor, arrayFn, count);
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
	protected List<String> getFieldOrder() {
		return fields.computeIfAbsent(getClass(), Struct::annotatedFields);
	}

	@Override
	protected void setAlignType(int alignType) {
		if (alignType == Align.platform.value) alignType =
			align.computeIfAbsent(getClass(), Struct::annotatedAlignment).value;
		super.setAlignType(alignType);
	}
	
	private static String fieldString(Structure s, Field f, int offset) {
		String type = ReflectUtil.nestedName(f.getType());
		String value = ARGS.arg(ReflectUtil.publicFieldValue(s, f));
		return String.format("+%02x: %s %s = %s%n", offset, type, f.getName(), prefix(value));
	}

	private static String prefix(String s) {
		return NEWLINE_REGEX.matcher(s).replaceAll("$1" + INDENT);
	}

	private static List<String> annotatedFields(Class<?> cls) {
		cls = structClass(cls);
		String[] fields = AnnotationUtil.value(cls, Fields.class, Fields::value);
		if (fields != null) return ImmutableUtil.wrapAsList(fields);
		throw new IllegalStateException(
			String.format("@%s({...}) or getFieldOrder() must be declared on %s",
				Fields.class.getSimpleName(), ReflectUtil.name(cls)));
	}

	private static Align annotatedAlignment(Class<?> cls) {
		boolean packed = AnnotationUtil.annotation(structClass(cls), Struct.Packed.class) != null;
		return packed ? Align.none : Align.platform;
	}

	private static Class<?> structClass(Class<?> cls) {
		return isByRef(cls) || isByVal(cls) ? cls.getSuperclass() : cls;
	}
}
