package ceri.common.collection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import ceri.common.util.BasicUtil;

/**
 * Utility methods to test and manipulate arrays.
 * 
 * @see ceri.common.collection.CollectionUtil
 * @see ceri.common.util.PrimitiveUtil
 */
public class ArrayUtil {
	private static final Map<Class<?>, Object[]> emptyArrayMap = new WeakHashMap<>();

	public static final boolean[] EMPTY_BOOLEAN = new boolean[0];
	public static final byte[] EMPTY_BYTE = new byte[0];
	public static final char[] EMPTY_CHAR = new char[0];
	public static final short[] EMPTY_SHORT = new short[0];
	public static final int[] EMPTY_INT = new int[0];
	public static final long[] EMPTY_LONG = new long[0];
	public static final float[] EMPTY_FLOAT = new float[0];
	public static final double[] EMPTY_DOUBLE = new double[0];
	public static final String[] EMPTY_STRING = emptyArray(String.class);

	private ArrayUtil() {}

	/**
	 * Returns an immutable zero-size array for the given component type.
	 */
	public static <T> T[] emptyArray(Class<T> cls) {
		T[] array = BasicUtil.uncheckedCast(emptyArrayMap.get(cls));
		if (array == null) {
			array = create(cls, 0);
			emptyArrayMap.put(cls, array);
		}
		return array;
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
		return BasicUtil.uncheckedCast(emptyArray(cls).getClass());
	}

	/**
	 * Creates an array of given size based on array type component class.
	 * Compile-time typing for Array.newInstance().
	 */
	public static <T> T[] create(Class<T> type, int size) {
		if (type.isPrimitive()) throw new IllegalArgumentException("Primitive types not allowed: " +
			type);
		return BasicUtil.uncheckedCast(Array.newInstance(type, size));
	}

	/**
	 * Returns the array class of the type component superclass. Or the regular
	 * superclass if not an array type. For example:
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
		return emptyArray(componentSuperCls).getClass();
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
	 * Returns true if the given array contains all of the given elements in any
	 * order.
	 */
	@SafeVarargs
	public static <T> boolean containsAll(T[] array, T... ts) {
		List<T> arrayList = Arrays.asList(array);
		List<T> tList = Arrays.asList(ts);
		return arrayList.containsAll(tList);
	}

	/**
	 * Reverses the array.
	 */
	public static void reverse(Object array) {
		if (!isArray(array)) throw new IllegalArgumentException("Object is not an array: " + array);
		int length = Array.getLength(array);
		for (int i = 0; i < length / 2; i++) {
			Object tmp = Array.get(array, i);
			Array.set(array, i, Array.get(array, length - 1 - i));
			Array.set(array, length - 1 - i, tmp);
		}
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
	 * Copies values from one array to another. Less strict than
	 * System.arraycopy as it allows copying from/to primitive arrays, but also
	 * not as efficient.
	 */
	public static Object arrayCopy(Object from, int fromIndex, Object to, int toIndex, int length) {
		for (int i = 0; i < length; i++) {
			Array.set(to, toIndex + i, Array.get(from, fromIndex + i));
		}
		return to;
	}

	/**
	 * Creates a non-fixed size list of the given array.
	 */
	@SafeVarargs
	public static <T> List<T> asList(T... ts) {
		return CollectionUtil.addAll(new ArrayList<T>(ts.length), ts);
	}

	/**
	 * Returns true if the given object is an instance of an array. Returns
	 * false if the object is null.
	 */
	public static boolean isArray(Object obj) {
		if (obj == null) return false;
		return obj.getClass().isArray();
	}

}
