package ceri.common.collection;

import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.math.MathUtil.ushort;
import static ceri.common.text.StringUtil.NULL_STRING;
import static ceri.common.util.PrimitiveUtil.convertBooleans;
import static ceri.common.util.PrimitiveUtil.convertBytes;
import static ceri.common.util.PrimitiveUtil.convertChars;
import static ceri.common.util.PrimitiveUtil.convertDoubles;
import static ceri.common.util.PrimitiveUtil.convertFloats;
import static ceri.common.util.PrimitiveUtil.convertInts;
import static ceri.common.util.PrimitiveUtil.convertLongs;
import static ceri.common.util.PrimitiveUtil.convertShorts;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import ceri.common.util.BasicUtil;
import ceri.common.util.Hasher;
import ceri.common.validation.ValidationUtil;

/**
 * Utility methods to test and manipulate arrays.
 * @see ceri.common.collection.CollectionUtil
 * @see ceri.common.util.PrimitiveUtil
 */
public class ArrayUtil {
	public static final boolean[] EMPTY_BOOLEAN = new boolean[0];
	public static final byte[] EMPTY_BYTE = new byte[0];
	public static final char[] EMPTY_CHAR = new char[0];
	public static final short[] EMPTY_SHORT = new short[0];
	public static final int[] EMPTY_INT = new int[0];
	public static final long[] EMPTY_LONG = new long[0];
	public static final float[] EMPTY_FLOAT = new float[0];
	public static final double[] EMPTY_DOUBLE = new double[0];
	public static final String[] EMPTY_STRING = new String[0];
	public static final Object[] EMPTY_OBJECT = new Object[0];
	public static final Class<?>[] EMPTY_CLASS = new Class<?>[0];
	private static final Map<Class<?>, Function<Object, String>> toStringMap = toStringMap();

	private ArrayUtil() {}

	/**
	 * Returns true if index is within array.
	 */
	public static boolean isValidIndex(int arrayLength, int index) {
		return (index >= 0 && index < arrayLength);
	}

	/**
	 * Returns true if parameters are able to slice an array.
	 */
	public static boolean isValidSlice(int arrayLength, int offset, int length) {
		if (offset < 0 || offset > arrayLength) return false;
		if (length < 0 || offset + length > arrayLength) return false;
		return true;
	}

	/**
	 * Returns true if parameters are able to slice an array by range.
	 */
	public static boolean isValidRange(int arrayLength, int start, int end) {
		if (start < 0 || start > arrayLength) return false;
		if (end < start || end > arrayLength) return false;
		return true;
	}

	/**
	 * Returns true if the slice/range is the same as the whole.
	 */
	public static boolean fullSlice(int arrayLength, int offset, int length) {
		return offset == 0 && length == arrayLength;
	}

	/**
	 * Converts a collection to a new list by mapping elements from the original collection.
	 */
	@SafeVarargs
	public static <F, T> List<T> toList(Function<? super F, ? extends T> mapper, F... fs) {
		return CollectionUtil.toList(mapper, Arrays.asList(fs));
	}

	/**
	 * Returns an immutable zero-size array for the given component type.
	 */
	public static <T> T[] empty(Class<T> cls) {
		if (cls == String.class) return BasicUtil.uncheckedCast(EMPTY_STRING);
		if (cls == Object.class) return BasicUtil.uncheckedCast(EMPTY_OBJECT);
		return create(cls, 0);
	}

	/**
	 * Returns the typed component class of an array type.
	 */
	public static <T> Class<T> componentType(Class<T[]> cls) {
		return BasicUtil.uncheckedCast(cls.getComponentType());
	}

	/**
	 * Returns the compile-time array type component class.
	 */
	public static <T> Class<T[]> arrayType(Class<T> cls) {
		return BasicUtil.uncheckedCast(empty(cls).getClass());
	}

	/**
	 * Creates an array of given size based on array type component class. Compile-time typing for
	 * Array.newInstance().
	 */
	public static <T> T[] create(Class<T> type, int size) {
		if (type.isPrimitive())
			throw new IllegalArgumentException("Primitive types not allowed: " + type);
		return BasicUtil.uncheckedCast(Array.newInstance(type, size));
	}

	/**
	 * Returns the array class of the type component superclass. Or the regular superclass if not an
	 * array type. For example:
	 *
	 * <pre>
	 * Class<Integer[][]> =>
	 *   Class<Number[][]> =>
	 *     Class<Object[][]> =>
	 *       Class<Object[]> =>
	 *         Class<Object> =>
	 *           null
	 * </pre>
	 */
	public static Class<?> superclass(Class<?> cls) {
		Class<?> componentCls = cls.getComponentType();
		if (componentCls == null) return cls.getSuperclass();
		Class<?> componentSuperCls;
		if (componentCls.isArray()) componentSuperCls = superclass(componentCls);
		else componentSuperCls = componentCls.getSuperclass();
		if (componentSuperCls == null) return Object.class;
		return empty(componentSuperCls).getClass();
	}

	@SafeVarargs
	public static <T> boolean allEqual(T value, T... args) {
		if (value == null) return allNull(args);
		for (T arg : args)
			if (!value.equals(arg)) return false;
		return true;
	}

	@SafeVarargs
	public static <T> boolean allNull(T... args) {
		for (T arg : args)
			if (arg != null) return false;
		return true;
	}

	/**
	 * Returns a new array containing the given array with given items appended.
	 */
	@SafeVarargs
	public static <T> T[] addAll(T[] array, T... ts) {
		T[] newArray = Arrays.copyOf(array, array.length + ts.length);
		System.arraycopy(ts, 0, newArray, array.length, ts.length);
		return newArray;
	}

	/**
	 * Returns true if the given array contains all of the given elements in any order.
	 */
	@SafeVarargs
	public static <T> boolean containsAll(T[] array, T... ts) {
		List<T> arrayList = asList(array);
		List<T> tList = Arrays.asList(ts);
		return arrayList.containsAll(tList);
	}

	/**
	 * Creates a fixed-size list view of the given sub-array, from start (inclusive) to end
	 * (exclusive).
	 */
	public static <T> List<T> asFixedList(T[] ts, int start) {
		return asFixedList(ts, start, ts.length);
	}

	/**
	 * Creates a fixed-size list view of the given sub-array, from start (inclusive) to end
	 * (exclusive).
	 */
	public static <T> List<T> asFixedList(T[] ts, int start, int end) {
		return Arrays.asList(ts).subList(start, end);
	}

	/**
	 * Creates a non fixed-size list of the given array. Use Arrays.asList() for fixed-size list.
	 */
	@SafeVarargs
	public static <T> List<T> asList(T... ts) {
		return CollectionUtil.addAll(new ArrayList<>(ts.length), ts);
	}

	/**
	 * Creates a non fixed-size list joining the given elements.
	 */
	public static <T> List<T> asList(T t, T[] ts) {
		List<T> list = CollectionUtil.addAll(new ArrayList<>(1 + ts.length), t);
		return CollectionUtil.addAll(list, ts);
	}

	/**
	 * Creates a non fixed-size list joining the given elements.
	 */
	@SafeVarargs
	public static <T> List<T> asList(T[] ts0, T... ts) {
		List<T> list = CollectionUtil.addAll(new ArrayList<>(ts0.length + ts.length), ts0);
		return CollectionUtil.addAll(list, ts);
	}

	/**
	 * Converts a primitive array to a list.
	 */
	public static List<Boolean> booleanList(boolean... array) {
		return asList(convertBooleans(array));
	}

	/**
	 * Converts a primitive array to a list.
	 */
	public static List<Character> charList(char... array) {
		return asList(convertChars(array));
	}

	/**
	 * Converts a primitive array to a list.
	 */
	public static List<Byte> byteList(byte... array) {
		return asList(convertBytes(array));
	}

	/**
	 * Converts a primitive array to a list.
	 */
	public static List<Short> shortList(short... array) {
		return asList(convertShorts(array));
	}

	/**
	 * Converts a primitive array to a list.
	 */
	public static List<Integer> intList(int... array) {
		return asList(convertInts(array));
	}

	/**
	 * Converts a primitive array to a list.
	 */
	public static List<Long> longList(long... array) {
		return asList(convertLongs(array));
	}

	/**
	 * Converts a primitive array to a list.
	 */
	public static List<Float> floatList(float... array) {
		return asList(convertFloats(array));
	}

	/**
	 * Converts a primitive array to a list.
	 */
	public static List<Double> doubleList(double... array) {
		return asList(convertDoubles(array));
	}

	/**
	 * Convenience method to create an array.
	 */
	@SafeVarargs
	public static <T> T[] array(T... ts) {
		return ts;
	}

	/**
	 * Convenience method to create an array.
	 */
	public static boolean[] booleans(boolean... ts) {
		return ts;
	}

	/**
	 * Convenience method to create an array.
	 */
	public static boolean[] booleans(int... ts) {
		var array = new boolean[ts.length];
		for (int i = 0; i < ts.length; i++)
			array[i] = ts[i] != 0;
		return array;
	}

	/**
	 * Convenience method to create an array.
	 */
	public static byte[] bytes(byte... ts) {
		return ts;
	}

	/**
	 * Convenience method to create an array.
	 */
	public static byte[] bytes(int... ts) {
		var array = new byte[ts.length];
		for (int i = 0; i < ts.length; i++)
			array[i] = (byte) ts[i];
		return array;
	}

	/**
	 * Convenience method to create an array.
	 */
	public static char[] chars(char... ts) {
		return ts;
	}

	/**
	 * Convenience method to create an array.
	 */
	public static char[] chars(int... ts) {
		var array = new char[ts.length];
		for (int i = 0; i < ts.length; i++)
			array[i] = (char) ts[i];
		return array;
	}

	/**
	 * Convenience method to create an array.
	 */
	public static short[] shorts(short... ts) {
		return ts;
	}

	/**
	 * Convenience method to create an array.
	 */
	public static short[] shorts(int... ts) {
		var array = new short[ts.length];
		for (int i = 0; i < ts.length; i++)
			array[i] = (short) ts[i];
		return array;
	}

	/**
	 * Convenience method to create an array.
	 */
	public static int[] ints(int... ts) {
		return ts;
	}

	/**
	 * Convenience method to create an array.
	 */
	public static long[] longs(long... ts) {
		return ts;
	}

	/**
	 * Convenience method to create an array.
	 */
	public static float[] floats(float... ts) {
		return ts;
	}

	/**
	 * Convenience method to create an array.
	 */
	public static float[] floats(double... ts) {
		var array = new float[ts.length];
		for (int i = 0; i < ts.length; i++)
			array[i] = (float) ts[i];
		return array;
	}

	/**
	 * Convenience method to create an array.
	 */
	public static double[] doubles(double... ts) {
		return ts;
	}

	/**
	 * Converts a number collection to a primitive array.
	 */
	public static byte[] bytes(Collection<? extends Number> collection) {
		byte[] result = new byte[collection.size()];
		int i = 0;
		for (Number number : collection)
			result[i++] = number.byteValue();
		return result;
	}

	/**
	 * Converts a number collection to a primitive array.
	 */
	public static short[] shorts(Collection<? extends Number> collection) {
		short[] result = new short[collection.size()];
		int i = 0;
		for (Number number : collection)
			result[i++] = number.shortValue();
		return result;
	}

	/**
	 * Converts a number collection to a primitive array.
	 */
	public static int[] ints(Collection<? extends Number> collection) {
		int[] result = new int[collection.size()];
		int i = 0;
		for (Number number : collection)
			result[i++] = number.intValue();
		return result;
	}

	/**
	 * Converts a number collection to a primitive array.
	 */
	public static long[] longs(Collection<? extends Number> collection) {
		long[] result = new long[collection.size()];
		int i = 0;
		for (Number number : collection)
			result[i++] = number.longValue();
		return result;
	}

	/**
	 * Converts a number collection to a primitive array.
	 */
	public static float[] floats(Collection<? extends Number> collection) {
		float[] result = new float[collection.size()];
		int i = 0;
		for (Number number : collection)
			result[i++] = number.floatValue();
		return result;
	}

	/**
	 * Converts a number collection to a primitive array.
	 */
	public static double[] doubles(Collection<? extends Number> collection) {
		double[] result = new double[collection.size()];
		int i = 0;
		for (Number number : collection)
			result[i++] = number.doubleValue();
		return result;
	}

	/**
	 * Creates an int array with values 0 to (count - 1).
	 */
	public static int[] intRange(int count) {
		return intRange(0, count);
	}

	/**
	 * Creates an int array with values (start) to (end - 1).
	 */
	public static int[] intRange(int start, int end) {
		int[] range = new int[end - start];
		for (int i = 0; i < range.length; i++)
			range[i] = start + i;
		return range;
	}

	/**
	 * Creates a long array with values 0 to (count - 1).
	 */
	public static long[] longRange(long count) {
		return longRange(0, count);
	}

	/**
	 * Creates a long array with values (start) to (end - 1).
	 */
	public static long[] longRange(long start, long end) {
		long[] range = new long[Math.toIntExact(end - start)];
		for (int i = 0; i < range.length; i++)
			range[i] = start + i;
		return range;
	}

	/**
	 * Reverses the array.
	 */
	public static <T> T reverseArray(T array) {
		if (!isArray(array)) throw new IllegalArgumentException("Object is not an array: " + array);
		int length = Array.getLength(array);
		for (int i = 0; i < length / 2; i++) {
			Object tmp = Array.get(array, i);
			Array.set(array, i, Array.get(array, length - 1 - i));
			Array.set(array, length - 1 - i, tmp);
		}
		return array;
	}

	/**
	 * Reverses the array.
	 */
	@SafeVarargs
	public static <T> T[] reverse(T... ts) {
		for (int i = 0; i < ts.length / 2; i++) {
			var tmp = ts[i];
			ts[i] = ts[ts.length - 1 - i];
			ts[ts.length - 1 - i] = tmp;
		}
		return ts;
	}

	/**
	 * Reverses the array.
	 */
	public static boolean[] reverseBooleans(boolean... ts) {
		for (int i = 0; i < ts.length / 2; i++) {
			var tmp = ts[i];
			ts[i] = ts[ts.length - 1 - i];
			ts[ts.length - 1 - i] = tmp;
		}
		return ts;
	}

	/**
	 * Reverses the array.
	 */
	public static char[] reverseChars(char... ts) {
		for (int i = 0; i < ts.length / 2; i++) {
			var tmp = ts[i];
			ts[i] = ts[ts.length - 1 - i];
			ts[ts.length - 1 - i] = tmp;
		}
		return ts;
	}

	/**
	 * Reverses the array.
	 */
	public static byte[] reverseBytes(byte... ts) {
		for (int i = 0; i < ts.length / 2; i++) {
			var tmp = ts[i];
			ts[i] = ts[ts.length - 1 - i];
			ts[ts.length - 1 - i] = tmp;
		}
		return ts;
	}

	/**
	 * Reverses the array.
	 */
	public static short[] reverseShorts(short... ts) {
		for (int i = 0; i < ts.length / 2; i++) {
			var tmp = ts[i];
			ts[i] = ts[ts.length - 1 - i];
			ts[ts.length - 1 - i] = tmp;
		}
		return ts;
	}

	/**
	 * Reverses the array.
	 */
	public static int[] reverseInts(int... ts) {
		for (int i = 0; i < ts.length / 2; i++) {
			var tmp = ts[i];
			ts[i] = ts[ts.length - 1 - i];
			ts[ts.length - 1 - i] = tmp;
		}
		return ts;
	}

	/**
	 * Reverses the array.
	 */
	public static long[] reverseLongs(long... ts) {
		for (int i = 0; i < ts.length / 2; i++) {
			var tmp = ts[i];
			ts[i] = ts[ts.length - 1 - i];
			ts[ts.length - 1 - i] = tmp;
		}
		return ts;
	}

	/**
	 * Reverses the array.
	 */
	public static float[] reverseFloats(float... ts) {
		for (int i = 0; i < ts.length / 2; i++) {
			var tmp = ts[i];
			ts[i] = ts[ts.length - 1 - i];
			ts[ts.length - 1 - i] = tmp;
		}
		return ts;
	}

	/**
	 * Reverses the array.
	 */
	public static double[] reverseDoubles(double... ts) {
		for (int i = 0; i < ts.length / 2; i++) {
			var tmp = ts[i];
			ts[i] = ts[ts.length - 1 - i];
			ts[ts.length - 1 - i] = tmp;
		}
		return ts;
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static <T> T[] copyOf(T[] array, int offset, IntFunction<T[]> constructor) {
		return copyOf(array, offset, Math.max(0, array.length - offset), constructor);
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static boolean[] copyOf(boolean[] array, int offset) {
		return copyOf(array, offset, Math.max(0, array.length - offset));
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static byte[] copyOf(byte[] array, int offset) {
		return copyOf(array, offset, Math.max(0, array.length - offset));
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static char[] copyOf(char[] array, int offset) {
		return copyOf(array, offset, Math.max(0, array.length - offset));
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static short[] copyOf(short[] array, int offset) {
		return copyOf(array, offset, Math.max(0, array.length - offset));
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static int[] copyOf(int[] array, int offset) {
		return copyOf(array, offset, Math.max(0, array.length - offset));
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static long[] copyOf(long[] array, int offset) {
		return copyOf(array, offset, Math.max(0, array.length - offset));
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static float[] copyOf(float[] array, int offset) {
		return copyOf(array, offset, Math.max(0, array.length - offset));
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static double[] copyOf(double[] array, int offset) {
		return copyOf(array, offset, Math.max(0, array.length - offset));
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static <T> T[] copyOf(T[] array, int offset, int length, IntFunction<T[]> constructor) {
		return arrayCopy(array, array.length, offset, length, constructor.apply(length));
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static boolean[] copyOf(boolean[] array, int offset, int length) {
		return arrayCopy(array, array.length, offset, length, new boolean[length]);
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static byte[] copyOf(byte[] array, int offset, int length) {
		return arrayCopy(array, array.length, offset, length, new byte[length]);
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static char[] copyOf(char[] array, int offset, int length) {
		return arrayCopy(array, array.length, offset, length, new char[length]);
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static short[] copyOf(short[] array, int offset, int length) {
		return arrayCopy(array, array.length, offset, length, new short[length]);
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static int[] copyOf(int[] array, int offset, int length) {
		return arrayCopy(array, array.length, offset, length, new int[length]);
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static long[] copyOf(long[] array, int offset, int length) {
		return arrayCopy(array, array.length, offset, length, new long[length]);
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static float[] copyOf(float[] array, int offset, int length) {
		return arrayCopy(array, array.length, offset, length, new float[length]);
	}

	/**
	 * Creates a new array and copies original values to it. Offset can be negative and outside
	 * range of original array. The overlap is copied to the new array.
	 */
	public static double[] copyOf(double[] array, int offset, int length) {
		return arrayCopy(array, array.length, offset, length, new double[length]);
	}

	/**
	 * Copies from one array to another, using the maximum length without overflowing the arrays.
	 * Returns the destination offset after copying.
	 */
	public static <T> int copy(T[] from, int fromOffset, T[] to, int toOffset) {
		return copy(from, fromOffset, to, toOffset,
			Math.min(from.length - fromOffset, to.length - toOffset));
	}

	/**
	 * Copies from one array to another, using the maximum length without overflowing the arrays.
	 * Returns the destination offset after copying.
	 */
	public static int copy(boolean[] from, int fromOffset, boolean[] to, int toOffset) {
		return copy(from, fromOffset, to, toOffset,
			Math.min(from.length - fromOffset, to.length - toOffset));
	}

	/**
	 * Copies from one array to another, using the maximum length without overflowing the arrays.
	 * Returns the destination offset after copying.
	 */
	public static int copy(byte[] from, int fromOffset, byte[] to, int toOffset) {
		return copy(from, fromOffset, to, toOffset,
			Math.min(from.length - fromOffset, to.length - toOffset));
	}

	/**
	 * Copies from one array to another, using the maximum length without overflowing the arrays.
	 * Returns the destination offset after copying.
	 */
	public static int copy(char[] from, int fromOffset, char[] to, int toOffset) {
		return copy(from, fromOffset, to, toOffset,
			Math.min(from.length - fromOffset, to.length - toOffset));
	}

	/**
	 * Copies from one array to another, using the maximum length without overflowing the arrays.
	 * Returns the destination offset after copying.
	 */
	public static int copy(short[] from, int fromOffset, short[] to, int toOffset) {
		return copy(from, fromOffset, to, toOffset,
			Math.min(from.length - fromOffset, to.length - toOffset));
	}

	/**
	 * Copies from one array to another, using the maximum length without overflowing the arrays.
	 * Returns the destination offset after copying.
	 */
	public static int copy(int[] from, int fromOffset, int[] to, int toOffset) {
		return copy(from, fromOffset, to, toOffset,
			Math.min(from.length - fromOffset, to.length - toOffset));
	}

	/**
	 * Copies from one array to another, using the maximum length without overflowing the arrays.
	 * Returns the destination offset after copying.
	 */
	public static int copy(long[] from, int fromOffset, long[] to, int toOffset) {
		return copy(from, fromOffset, to, toOffset,
			Math.min(from.length - fromOffset, to.length - toOffset));
	}

	/**
	 * Copies from one array to another, using the maximum length without overflowing the arrays.
	 * Returns the destination offset after copying.
	 */
	public static int copy(float[] from, int fromOffset, float[] to, int toOffset) {
		return copy(from, fromOffset, to, toOffset,
			Math.min(from.length - fromOffset, to.length - toOffset));
	}

	/**
	 * Copies from one array to another, using the maximum length without overflowing the arrays.
	 * Returns the destination offset after copying.
	 */
	public static int copy(double[] from, int fromOffset, double[] to, int toOffset) {
		return copy(from, fromOffset, to, toOffset,
			Math.min(from.length - fromOffset, to.length - toOffset));
	}

	/**
	 * Copies from one array to another. Returns the destination offset after copying.
	 */
	public static <T> int copy(T[] from, int fromOffset, T[] to, int toOffset, int length) {
		System.arraycopy(from, fromOffset, to, toOffset, length);
		return toOffset + length;
	}

	/**
	 * Copies from one array to another. Returns the destination offset after copying.
	 */
	public static int copy(boolean[] from, int fromOffset, boolean[] to, int toOffset, int length) {
		System.arraycopy(from, fromOffset, to, toOffset, length);
		return toOffset + length;
	}

	/**
	 * Copies from one array to another. Returns the destination offset after copying.
	 */
	public static int copy(byte[] from, int fromOffset, byte[] to, int toOffset, int length) {
		System.arraycopy(from, fromOffset, to, toOffset, length);
		return toOffset + length;
	}

	/**
	 * Copies from one array to another. Returns the destination offset after copying.
	 */
	public static int copy(char[] from, int fromOffset, char[] to, int toOffset, int length) {
		System.arraycopy(from, fromOffset, to, toOffset, length);
		return toOffset + length;
	}

	/**
	 * Copies from one array to another. Returns the destination offset after copying.
	 */
	public static int copy(short[] from, int fromOffset, short[] to, int toOffset, int length) {
		System.arraycopy(from, fromOffset, to, toOffset, length);
		return toOffset + length;
	}

	/**
	 * Copies from one array to another. Returns the destination offset after copying.
	 */
	public static int copy(int[] from, int fromOffset, int[] to, int toOffset, int length) {
		System.arraycopy(from, fromOffset, to, toOffset, length);
		return toOffset + length;
	}

	/**
	 * Copies from one array to another. Returns the destination offset after copying.
	 */
	public static int copy(long[] from, int fromOffset, long[] to, int toOffset, int length) {
		System.arraycopy(from, fromOffset, to, toOffset, length);
		return toOffset + length;
	}

	/**
	 * Copies from one array to another. Returns the destination offset after copying.
	 */
	public static int copy(float[] from, int fromOffset, float[] to, int toOffset, int length) {
		System.arraycopy(from, fromOffset, to, toOffset, length);
		return toOffset + length;
	}

	/**
	 * Copies from one array to another. Returns the destination offset after copying.
	 */
	public static int copy(double[] from, int fromOffset, double[] to, int toOffset, int length) {
		System.arraycopy(from, fromOffset, to, toOffset, length);
		return toOffset + length;
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static <T> int fill(T[] array, int offset, T value) {
		return fill(array, offset, array.length - offset, value);
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static <T> int fill(T[] array, int offset, int length, T value) {
		if (length > 0) Arrays.fill(array, offset, offset + length, value); // checks range
		return offset + length;
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static int fill(boolean[] array, int offset, boolean value) {
		return fill(array, offset, array.length - offset, value);
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static int fill(boolean[] array, int offset, int length, boolean value) {
		if (length > 0) Arrays.fill(array, offset, offset + length, value); // checks range
		return offset + length;
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static int fill(byte[] array, int offset, int value) {
		return fill(array, offset, array.length - offset, value);
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static int fill(byte[] array, int offset, int length, int value) {
		if (length > 0) Arrays.fill(array, offset, offset + length, (byte) value); // checks range
		return offset + length;
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static int fill(char[] array, int offset, char value) {
		return fill(array, offset, array.length - offset, value);
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static int fill(char[] array, int offset, int length, char value) {
		if (length > 0) Arrays.fill(array, offset, offset + length, value); // checks range
		return offset + length;
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static int fill(short[] array, int offset, int value) {
		return fill(array, offset, array.length - offset, value);
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static int fill(short[] array, int offset, int length, int value) {
		if (length > 0) Arrays.fill(array, offset, offset + length, (short) value); // checks range
		return offset + length;
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static int fill(int[] array, int offset, int value) {
		return fill(array, offset, array.length - offset, value);
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static int fill(int[] array, int offset, int length, int value) {
		if (length > 0) Arrays.fill(array, offset, offset + length, value); // checks range
		return offset + length;
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static int fill(long[] array, int offset, long value) {
		return fill(array, offset, array.length - offset, value);
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static int fill(long[] array, int offset, int length, long value) {
		if (length > 0) Arrays.fill(array, offset, offset + length, value); // checks range
		return offset + length;
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static int fill(float[] array, int offset, float value) {
		return fill(array, offset, array.length - offset, value);
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static int fill(float[] array, int offset, int length, float value) {
		if (length > 0) Arrays.fill(array, offset, offset + length, value); // checks range
		return offset + length;
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static int fill(double[] array, int offset, double value) {
		return fill(array, offset, array.length - offset, value);
	}

	/**
	 * Fills array with value. Returns array offset after filling.
	 */
	public static int fill(double[] array, int offset, int length, double value) {
		if (length > 0) Arrays.fill(array, offset, offset + length, value); // checks range
		return offset + length;
	}

	/**
	 * Returns value at index, or null if out of range.
	 */
	public static <T> T at(T[] array, int i) {
		return at(array, i, null);
	}

	/**
	 * Returns value at index, or default if out of range, or array is null.
	 */
	public static <T> T at(T[] array, int i, T def) {
		if (array == null || i < 0 || i >= array.length) return def;
		return array[i];
	}

	/**
	 * Returns value at index, or default if out of range, or array is null.
	 */
	public static boolean at(boolean[] array, int i, boolean def) {
		if (array == null || i < 0 || i >= array.length) return def;
		return array[i];
	}

	/**
	 * Returns value at index, or default if out of range, or array is null.
	 */
	public static byte at(byte[] array, int i, byte def) {
		if (array == null || i < 0 || i >= array.length) return def;
		return array[i];
	}

	/**
	 * Returns value at index, or default if out of range, or array is null.
	 */
	public static char at(char[] array, int i, char def) {
		if (array == null || i < 0 || i >= array.length) return def;
		return array[i];
	}

	/**
	 * Returns value at index, or default if out of range, or array is null.
	 */
	public static short at(short[] array, int i, short def) {
		if (array == null || i < 0 || i >= array.length) return def;
		return array[i];
	}

	/**
	 * Returns value at index, or default if out of range, or array is null.
	 */
	public static int at(int[] array, int i, int def) {
		if (array == null || i < 0 || i >= array.length) return def;
		return array[i];
	}

	/**
	 * Returns value at index, or default if out of range, or array is null.
	 */
	public static long at(long[] array, int i, long def) {
		if (array == null || i < 0 || i >= array.length) return def;
		return array[i];
	}

	/**
	 * Returns value at index, or default if out of range, or array is null.
	 */
	public static float at(float[] array, int i, float def) {
		if (array == null || i < 0 || i >= array.length) return def;
		return array[i];
	}

	/**
	 * Returns value at index, or default if out of range, or array is null.
	 */
	public static double at(double[] array, int i, double def) {
		if (array == null || i < 0 || i >= array.length) return def;
		return array[i];
	}

	/**
	 * Varargs method for Arrays.deepHashCode().
	 */
	public static int deepHash(Object... objs) {
		return Arrays.deepHashCode(objs);
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(boolean[] a, int offset) {
		return hash(a, offset, a != null ? a.length - offset : 0);
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(boolean[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		var hasher = Hasher.of();
		while (length-- > 0)
			hasher.hash(a[offset++]);
		return hasher.code();
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(byte[] a, int offset) {
		return hash(a, offset, a != null ? a.length - offset : 0);
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(byte[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		var hasher = Hasher.of();
		while (length-- > 0)
			hasher.hash(a[offset++]);
		return hasher.code();
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(char[] a, int offset) {
		return hash(a, offset, a != null ? a.length - offset : 0);
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(char[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		var hasher = Hasher.of();
		while (length-- > 0)
			hasher.hash(a[offset++]);
		return hasher.code();
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(short[] a, int offset) {
		return hash(a, offset, a != null ? a.length - offset : 0);
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(short[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		var hasher = Hasher.of();
		while (length-- > 0)
			hasher.hash(a[offset++]);
		return hasher.code();
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(int[] a, int offset) {
		return hash(a, offset, a != null ? a.length - offset : 0);
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(int[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		var hasher = Hasher.of();
		while (length-- > 0)
			hasher.hash(a[offset++]);
		return hasher.code();
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(long[] a, int offset) {
		return hash(a, offset, a != null ? a.length - offset : 0);
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(long[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		var hasher = Hasher.of();
		while (length-- > 0)
			hasher.hash(a[offset++]);
		return hasher.code();
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(float[] a, int offset) {
		return hash(a, offset, a != null ? a.length - offset : 0);
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(float[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		var hasher = Hasher.of();
		while (length-- > 0)
			hasher.hash(a[offset++]);
		return hasher.code();
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(double[] a, int offset) {
		return hash(a, offset, a != null ? a.length - offset : 0);
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(double[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		var hasher = Hasher.of();
		while (length-- > 0)
			hasher.hash(a[offset++]);
		return hasher.code();
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(Object[] a, int offset) {
		return hash(a, offset, a != null ? a.length - offset : 0);
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int hash(Object[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		var hasher = Hasher.of();
		while (length-- > 0)
			hasher.hash(a[offset++]);
		return hasher.code();
	}

	/**
	 * Checks if arrays are equals, using offsets and length
	 */
	public static <T> boolean equals(T[] lhs, int lhsStart, T[] rhs, int rhsStart, int len) {
		return Arrays.equals(lhs, lhsStart, lhsStart + len, rhs, rhsStart, rhsStart + len);
	}

	/**
	 * Checks if arrays are equals, using offsets and length
	 */
	public static boolean equals(boolean[] lhs, int lhsStart, boolean[] rhs, int rhsStart,
		int len) {
		return Arrays.equals(lhs, lhsStart, lhsStart + len, rhs, rhsStart, rhsStart + len);
	}

	/**
	 * Checks if arrays are equals, using offsets and length
	 */
	public static boolean equals(char[] lhs, int lhsStart, char[] rhs, int rhsStart, int len) {
		return Arrays.equals(lhs, lhsStart, lhsStart + len, rhs, rhsStart, rhsStart + len);
	}

	/**
	 * Checks if arrays are equals, using offsets and length
	 */
	public static boolean equals(byte[] lhs, int lhsStart, byte[] rhs, int rhsStart, int len) {
		return Arrays.equals(lhs, lhsStart, lhsStart + len, rhs, rhsStart, rhsStart + len);
	}

	/**
	 * Checks if arrays are equals, using offsets and length
	 */
	public static boolean equals(short[] lhs, int lhsStart, short[] rhs, int rhsStart, int len) {
		return Arrays.equals(lhs, lhsStart, lhsStart + len, rhs, rhsStart, rhsStart + len);
	}

	/**
	 * Checks if arrays are equals, using offsets and length
	 */
	public static boolean equals(int[] lhs, int lhsStart, int[] rhs, int rhsStart, int len) {
		return Arrays.equals(lhs, lhsStart, lhsStart + len, rhs, rhsStart, rhsStart + len);
	}

	/**
	 * Checks if arrays are equals, using offsets and length
	 */
	public static boolean equals(long[] lhs, int lhsStart, long[] rhs, int rhsStart, int len) {
		return Arrays.equals(lhs, lhsStart, lhsStart + len, rhs, rhsStart, rhsStart + len);
	}

	/**
	 * Checks if arrays are equals, using offsets and length
	 */
	public static boolean equals(float[] lhs, int lhsStart, float[] rhs, int rhsStart, int len) {
		return Arrays.equals(lhs, lhsStart, lhsStart + len, rhs, rhsStart, rhsStart + len);
	}

	/**
	 * Checks if arrays are equals, using offsets and length
	 */
	public static boolean equals(double[] lhs, int lhsStart, double[] rhs, int rhsStart, int len) {
		return Arrays.equals(lhs, lhsStart, lhsStart + len, rhs, rhsStart, rhsStart + len);
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(boolean[] array, int off) {
		return toString(array, off, array != null ? array.length - off : 0);
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(boolean[] array, int off, int len) {
		return toString(array, off, len, (b, a, i) -> b.append(a[i]));
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(byte[] array, int off) {
		return toString(array, off, array != null ? array.length - off : 0);
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(byte[] array, int off, int len) {
		return toString(array, off, len, (b, a, i) -> b.append(a[i]));
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(char[] array, int off) {
		return toString(array, off, array != null ? array.length - off : 0);
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(char[] array, int off, int len) {
		return toString(array, off, len, (b, a, i) -> b.append(a[i]));
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(short[] array, int off) {
		return toString(array, off, array != null ? array.length - off : 0);
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(short[] array, int off, int len) {
		return toString(array, off, len, (b, a, i) -> b.append(a[i]));
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(int[] array, int off) {
		return toString(array, off, array != null ? array.length - off : 0);
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(int[] array, int off, int len) {
		return toString(array, off, len, (b, a, i) -> b.append(a[i]));
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(long[] array, int off) {
		return toString(array, off, array != null ? array.length - off : 0);
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(long[] array, int off, int len) {
		return toString(array, off, len, (b, a, i) -> b.append(a[i]));
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(float[] array, int off) {
		return toString(array, off, array != null ? array.length - off : 0);
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(float[] array, int off, int len) {
		return toString(array, off, len, (b, a, i) -> b.append(a[i]));
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(double[] array, int off) {
		return toString(array, off, array != null ? array.length - off : 0);
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(double[] array, int off, int len) {
		return toString(array, off, len, (b, a, i) -> b.append(a[i]));
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(Object[] array, int off) {
		return toString(array, off, array != null ? array.length - off : 0);
	}

	/**
	 * Arrays.toString for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toString(Object[] array, int off, int len) {
		return toString(array, off, len, (b, a, i) -> b.append(a[i]));
	}

	/**
	 * Hex string for array. Returns "null" for invalid array slice.
	 */
	public static String toHex(byte[] array) {
		return toHex(array, 0);
	}

	/**
	 * Hex string for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toHex(byte[] array, int off) {
		return toHex(array, off, array != null ? array.length - off : 0);
	}

	/**
	 * Hex string for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toHex(byte[] array, int off, int len) {
		return toString(array, off, len,
			(b, a, i) -> b.append("0x").append(Integer.toHexString(ubyte(a[i]))));
	}

	/**
	 * Hex string for array. Returns "null" for invalid array slice.
	 */
	public static String toHex(short[] array) {
		return toHex(array, 0);
	}

	/**
	 * Hex string for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toHex(short[] array, int off) {
		return toHex(array, off, array != null ? array.length - off : 0);
	}

	/**
	 * Hex string for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toHex(short[] array, int off, int len) {
		return toString(array, off, len,
			(b, a, i) -> b.append("0x").append(Integer.toHexString(ushort(a[i]))));
	}

	/**
	 * Hex string for array. Returns "null" for invalid array slice.
	 */
	public static String toHex(int[] array) {
		return toHex(array, 0);
	}

	/**
	 * Hex string for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toHex(int[] array, int off) {
		return toHex(array, off, array != null ? array.length - off : 0);
	}

	/**
	 * Hex string for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toHex(int[] array, int off, int len) {
		return toString(array, off, len,
			(b, a, i) -> b.append("0x").append(Integer.toHexString(a[i])));
	}

	/**
	 * Hex string for array. Returns "null" for invalid array slice.
	 */
	public static String toHex(long[] array) {
		return toHex(array, 0);
	}

	/**
	 * Hex string for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toHex(long[] array, int off) {
		return toHex(array, off, array != null ? array.length - off : 0);
	}

	/**
	 * Hex string for sub-array. Returns "null" for invalid array slice.
	 */
	public static String toHex(long[] array, int off, int len) {
		return toString(array, off, len,
			(b, a, i) -> b.append("0x").append(Long.toHexString(a[i])));
	}

	/**
	 * Extends Arrays.deepToString to include any object type.
	 */
	public static String deepToString(Object obj) {
		if (obj == null) return NULL_STRING;
		Class<?> cls = obj.getClass();
		if (!cls.isArray()) return String.valueOf(obj);
		Function<Object, String> fn = toStringMap.get(cls);
		if (fn != null) return fn.apply(obj);
		return Arrays.deepToString((Object[]) obj);
	}

	/**
	 * Finds the first index of values within the array. Returns -1 if not found.
	 */
	@SafeVarargs
	public static <T> int indexOf(T[] array, int start, T... values) {
		return indexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the first index of values within the array. Returns -1 if not found.
	 */
	public static int indexOf(boolean[] array, int start, boolean... values) {
		return indexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the first index of values within the array. Returns -1 if not found.
	 */
	public static int indexOf(byte[] array, int start, byte... values) {
		return indexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the first index of values within the array. Returns -1 if not found.
	 */
	public static int indexOf(char[] array, int start, char... values) {
		return indexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the first index of values within the array. Returns -1 if not found.
	 */
	public static int indexOf(short[] array, int start, short... values) {
		return indexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the first index of values within the array. Returns -1 if not found.
	 */
	public static int indexOf(int[] array, int start, int... values) {
		return indexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the first index of values within the array. Returns -1 if not found.
	 */
	public static int indexOf(long[] array, int start, long... values) {
		return indexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the first index of values within the array. Returns -1 if not found.
	 */
	public static int indexOf(float[] array, int start, float... values) {
		return indexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the first index of values within the array. Returns -1 if not found.
	 */
	public static int indexOf(double[] array, int start, double... values) {
		return indexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the last index of values within the array. Returns -1 if not found.
	 */
	@SafeVarargs
	public static <T> int lastIndexOf(T[] array, int start, T... values) {
		return lastIndexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the last index of values within the array. Returns -1 if not found.
	 */
	public static int lastIndexOf(boolean[] array, int start, boolean... values) {
		return lastIndexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the last index of values within the array. Returns -1 if not found.
	 */
	public static int lastIndexOf(byte[] array, int start, byte... values) {
		return lastIndexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the last index of values within the array. Returns -1 if not found.
	 */
	public static int lastIndexOf(char[] array, int start, char... values) {
		return lastIndexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the last index of values within the array. Returns -1 if not found.
	 */
	public static int lastIndexOf(short[] array, int start, short... values) {
		return lastIndexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the last index of values within the array. Returns -1 if not found.
	 */
	public static int lastIndexOf(int[] array, int start, int... values) {
		return lastIndexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the last index of values within the array. Returns -1 if not found.
	 */
	public static int lastIndexOf(long[] array, int start, long... values) {
		return lastIndexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the last index of values within the array. Returns -1 if not found.
	 */
	public static int lastIndexOf(float[] array, int start, float... values) {
		return lastIndexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Finds the last index of values within the array. Returns -1 if not found.
	 */
	public static int lastIndexOf(double[] array, int start, double... values) {
		return lastIndexOf(array, start, array.length, values, values.length, ArrayUtil::equals);
	}

	/**
	 * Returns the last element in the array.
	 */
	public static <T> T last(T[] array) {
		return array[array.length - 1];
	}

	/**
	 * Returns the last element in the array.
	 */
	public static boolean last(boolean[] array) {
		return array[array.length - 1];
	}

	/**
	 * Returns the last element in the array.
	 */
	public static byte last(byte[] array) {
		return array[array.length - 1];
	}

	/**
	 * Returns the last element in the array.
	 */
	public static char last(char[] array) {
		return array[array.length - 1];
	}

	/**
	 * Returns the last element in the array.
	 */
	public static short last(short[] array) {
		return array[array.length - 1];
	}

	/**
	 * Returns the last element in the array.
	 */
	public static int last(int[] array) {
		return array[array.length - 1];
	}

	/**
	 * Returns the last element in the array.
	 */
	public static long last(long[] array) {
		return array[array.length - 1];
	}

	/**
	 * Returns the last element in the array.
	 */
	public static float last(float[] array) {
		return array[array.length - 1];
	}

	/**
	 * Returns the last element in the array.
	 */
	public static double last(double[] array) {
		return array[array.length - 1];
	}

	/**
	 * Copies values from one array to another. Less strict than System.arraycopy as it allows
	 * copying from/to primitive arrays, but also not as efficient.
	 */
	public static Object arrayCopy(Object from, int fromIndex, Object to, int toIndex, int length) {
		while (length-- > 0)
			Array.set(to, toIndex++, Array.get(from, fromIndex++));
		return to;
	}

	/**
	 * Returns true if the given object is an instance of an array. Returns false if the object is
	 * null.
	 */
	public static boolean isArray(Object obj) {
		if (obj == null) return false;
		return obj.getClass().isArray();
	}

	private static interface ArrayEquals<T> {
		boolean equals(T lhs, int lhsStart, T rhs, int rhsStart, int len);
	}

	private static <T> int indexOf(T array, int start, int arrayLen, T values, int valuesLen,
		ArrayEquals<T> equalsFn) {
		ValidationUtil.validateSlice(arrayLen, start, 0);
		for (int i = start; i <= arrayLen - valuesLen; i++)
			if (equalsFn.equals(array, i, values, 0, valuesLen)) return i;
		return -1;
	}

	private static <T> int lastIndexOf(T array, int start, int arrayLen, T values, int valuesLen,
		ArrayEquals<T> equalsFn) {
		ValidationUtil.validateSlice(arrayLen, start, 0);
		for (int i = arrayLen - valuesLen; i >= start; i--)
			if (equalsFn.equals(array, i, values, 0, valuesLen)) return i;
		return -1;
	}

	private static interface Appender<T> {
		void append(StringBuilder b, T a, int i);
	}

	private static <T> String toString(T a, int off, int len, Appender<T> appendFn) {
		if (a == null || !isValidSlice(Array.getLength(a), off, len)) return "null";
		if (len == 0) return "[]";
		StringBuilder b = new StringBuilder();
		appendFn.append(b.append('['), a, off);
		for (int i = 1; i < len; i++)
			appendFn.append(b.append(", "), a, off + i);
		return b.append(']').toString();
	}

	private static <T> T arrayCopy(T src, int srcSize, int offset, int length, T dest) {
		int from = Math.max(0, offset);
		int to = (from - offset);
		int len = Math.min(srcSize - from, length - to);
		if (len > 0) System.arraycopy(src, from, dest, to, len);
		return dest;
	}

	private static Map<Class<?>, Function<Object, String>> toStringMap() {
		return Map.of( //
			boolean[].class, obj -> Arrays.toString((boolean[]) obj), //
			char[].class, obj -> Arrays.toString((char[]) obj), //
			byte[].class, obj -> Arrays.toString((byte[]) obj), //
			short[].class, obj -> Arrays.toString((short[]) obj), //
			int[].class, obj -> Arrays.toString((int[]) obj), //
			long[].class, obj -> Arrays.toString((long[]) obj), //
			float[].class, obj -> Arrays.toString((float[]) obj), //
			double[].class, obj -> Arrays.toString((double[]) obj));
	}
}
