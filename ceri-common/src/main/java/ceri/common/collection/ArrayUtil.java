package ceri.common.collection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import ceri.common.util.BasicUtil;

/**
 * Utility methods to test and manipulate arrays.
 *
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
	 * Performs a validation on index to an array.
	 */
	public static void validateIndex(int arrayLength, int index) {
		if (index >= 0 && index < arrayLength) return;
		throw new IndexOutOfBoundsException("Index must be 0-" + (arrayLength - 1) + ": " + index);
	}

	/**
	 * Performs a validation on parameters to slice an array.
	 */
	public static void validateSlice(int arrayLength, int offset, int length) {
		if (offset < 0 || offset > arrayLength)
			throw new IndexOutOfBoundsException("Offset must be 0-" + arrayLength + ": " + offset);
		if (length < 0 || offset + length > arrayLength) throw new IndexOutOfBoundsException(
			"Length must be 0-" + (arrayLength - offset) + ": " + length);
	}

	/**
	 * Performs a validation on parameters to slice an array by range.
	 */
	public static void validateRange(int arrayLength, int start, int end) {
		if (start < 0 || start > arrayLength)
			throw new IndexOutOfBoundsException("Start must be 0-" + arrayLength + ": " + start);
		if (end < start || end > arrayLength) throw new IndexOutOfBoundsException(
			"End must be " + start + "-" + arrayLength + ": " + end);
	}

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

	public static int[] intRange(int count) {
		return intRange(0, count);
	}

	public static int[] intRange(int start, int end) {
		int[] range = new int[end - start];
		for (int i = 0; i < range.length; i++)
			range[i] = start + i;
		return range;
	}

	public static long[] longRange(long count) {
		return longRange(0, count);
	}

	public static long[] longRange(long start, long end) {
		long[] range = new long[Math.toIntExact(end - start)];
		for (int i = 0; i < range.length; i++)
			range[i] = start + i;
		return range;
	}

	/**
	 * Converts the object array to a primitive array.
	 */
	public static boolean[] convertBooleans(Boolean[] array) {
		var ts = new boolean[array.length];
		for (int i = 0; i < array.length; i++)
			ts[i] = array[i];
		return ts;
	}

	/**
	 * Converts the primitive array to an object array.
	 */
	public static Boolean[] convertBooleans(boolean... array) {
		var ts = new Boolean[array.length];
		for (int i = 0; i < array.length; i++)
			ts[i] = array[i];
		return ts;
	}

	/**
	 * Converts the object array to a primitive array.
	 */
	public static byte[] convertBytes(Byte[] array) {
		var ts = new byte[array.length];
		for (int i = 0; i < array.length; i++)
			ts[i] = array[i];
		return ts;
	}

	/**
	 * Converts the primitive array to an object array.
	 */
	public static Byte[] convertBytes(byte... array) {
		var ts = new Byte[array.length];
		for (int i = 0; i < array.length; i++)
			ts[i] = array[i];
		return ts;
	}

	/**
	 * Converts the object array to a primitive array.
	 */
	public static char[] convertChars(Character[] array) {
		var ts = new char[array.length];
		for (int i = 0; i < array.length; i++)
			ts[i] = array[i];
		return ts;
	}

	/**
	 * Converts the primitive array to an object array.
	 */
	public static Character[] convertChars(char... array) {
		var ts = new Character[array.length];
		for (int i = 0; i < array.length; i++)
			ts[i] = array[i];
		return ts;
	}

	/**
	 * Converts the object array to a primitive array.
	 */
	public static short[] convertShorts(Short[] array) {
		var ts = new short[array.length];
		for (int i = 0; i < array.length; i++)
			ts[i] = array[i];
		return ts;
	}

	/**
	 * Converts the primitive array to an object array.
	 */
	public static Short[] convertShorts(short... array) {
		var ts = new Short[array.length];
		for (int i = 0; i < array.length; i++)
			ts[i] = array[i];
		return ts;
	}

	/**
	 * Converts the object array to a primitive array.
	 */
	public static int[] convertInts(Integer[] array) {
		var ts = new int[array.length];
		for (int i = 0; i < array.length; i++)
			ts[i] = array[i];
		return ts;
	}

	/**
	 * Converts the primitive array to an object array.
	 */
	public static Integer[] convertInts(int... array) {
		var ts = new Integer[array.length];
		for (int i = 0; i < array.length; i++)
			ts[i] = array[i];
		return ts;
	}

	/**
	 * Converts the object array to a primitive array.
	 */
	public static long[] convertLongs(Long[] array) {
		var ts = new long[array.length];
		for (int i = 0; i < array.length; i++)
			ts[i] = array[i];
		return ts;
	}

	/**
	 * Converts the primitive array to an object array.
	 */
	public static Long[] convertLongs(long... array) {
		var ts = new Long[array.length];
		for (int i = 0; i < array.length; i++)
			ts[i] = array[i];
		return ts;
	}

	/**
	 * Converts the object array to a primitive array.
	 */
	public static float[] convertFloats(Float[] array) {
		var ts = new float[array.length];
		for (int i = 0; i < array.length; i++)
			ts[i] = array[i];
		return ts;
	}

	/**
	 * Converts the primitive array to an object array.
	 */
	public static Float[] convertFloats(float... array) {
		var ts = new Float[array.length];
		for (int i = 0; i < array.length; i++)
			ts[i] = array[i];
		return ts;
	}

	/**
	 * Converts the object array to a primitive array.
	 */
	public static double[] convertDoubles(Double[] array) {
		var ts = new double[array.length];
		for (int i = 0; i < array.length; i++)
			ts[i] = array[i];
		return ts;
	}

	/**
	 * Converts the primitive array to an object array.
	 */
	public static Double[] convertDoubles(double... array) {
		var ts = new Double[array.length];
		for (int i = 0; i < array.length; i++)
			ts[i] = array[i];
		return ts;
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
	 * Checks if arrays are equals, using offsets and minimum overlapping length
	 */
	public static <T> boolean equals(T[] lhs, int lhsStart, T[] rhs, int rhsStart) {
		return equals(lhs, lhsStart, rhs, rhsStart,
			Math.min(lhs.length - lhsStart, rhs.length - rhsStart));
	}

	/**
	 * Checks if arrays are equals, using offsets and minimum overlapping length
	 */
	public static boolean equals(boolean[] lhs, int lhsStart, boolean[] rhs, int rhsStart) {
		return equals(lhs, lhsStart, rhs, rhsStart,
			Math.min(lhs.length - lhsStart, rhs.length - rhsStart));
	}

	/**
	 * Checks if arrays are equals, using offsets and minimum overlapping length
	 */
	public static boolean equals(byte[] lhs, int lhsStart, byte[] rhs, int rhsStart) {
		return equals(lhs, lhsStart, rhs, rhsStart,
			Math.min(lhs.length - lhsStart, rhs.length - rhsStart));
	}

	/**
	 * Checks if arrays are equals, using offsets and minimum overlapping length
	 */
	public static boolean equals(char[] lhs, int lhsStart, char[] rhs, int rhsStart) {
		return equals(lhs, lhsStart, rhs, rhsStart,
			Math.min(lhs.length - lhsStart, rhs.length - rhsStart));
	}

	/**
	 * Checks if arrays are equals, using offsets and minimum overlapping length
	 */
	public static boolean equals(short[] lhs, int lhsStart, short[] rhs, int rhsStart) {
		return equals(lhs, lhsStart, rhs, rhsStart,
			Math.min(lhs.length - lhsStart, rhs.length - rhsStart));
	}

	/**
	 * Checks if arrays are equals, using offsets and minimum overlapping length
	 */
	public static boolean equals(int[] lhs, int lhsStart, int[] rhs, int rhsStart) {
		return equals(lhs, lhsStart, rhs, rhsStart,
			Math.min(lhs.length - lhsStart, rhs.length - rhsStart));
	}

	/**
	 * Checks if arrays are equals, using offsets and minimum overlapping length
	 */
	public static boolean equals(long[] lhs, int lhsStart, long[] rhs, int rhsStart) {
		return equals(lhs, lhsStart, rhs, rhsStart,
			Math.min(lhs.length - lhsStart, rhs.length - rhsStart));
	}

	/**
	 * Checks if arrays are equals, using offsets and minimum overlapping length
	 */
	public static boolean equals(float[] lhs, int lhsStart, float[] rhs, int rhsStart) {
		return equals(lhs, lhsStart, rhs, rhsStart,
			Math.min(lhs.length - lhsStart, rhs.length - rhsStart));
	}

	/**
	 * Checks if arrays are equals, using offsets and minimum overlapping length
	 */
	public static boolean equals(double[] lhs, int lhsStart, double[] rhs, int rhsStart) {
		return equals(lhs, lhsStart, rhs, rhsStart,
			Math.min(lhs.length - lhsStart, rhs.length - rhsStart));
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
		for (int i = 0; i < length; i++) {
			Array.set(to, toIndex + i, Array.get(from, fromIndex + i));
		}
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

	/**
	 * Extends Arrays.deepToString to include any object type.
	 */
	public static String deepToString(Object obj) {
		if (obj == null) return String.valueOf((String) null);
		Class<?> cls = obj.getClass();
		if (!cls.isArray()) return String.valueOf(obj);
		Function<Object, String> fn = toStringMap.get(cls);
		if (fn != null) return fn.apply(obj);
		return Arrays.deepToString((Object[]) obj);
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
