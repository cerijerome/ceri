package ceri.common.util;

import java.util.HashMap;
import java.util.Map;
import ceri.common.text.NumberParser;
import ceri.common.text.StringUtil;

/**
 * Utility methods for handling primitives and primitive wrappers.
 */
public class PrimitiveUtil {
	private static final int DECIMAL_RADIX = 10;
	private static final Map<Class<?>, Class<?>> classMap = createMap();

	private PrimitiveUtil() {}

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

	public static <T> boolean lookupBoolean(Map<T, Boolean> map, T type) {
		return lookupBoolean(map, type, false);
	}

	public static <T> boolean lookupBoolean(Map<T, Boolean> map, T type, boolean def) {
		if (map == null) return def;
		Boolean value = map.get(type);
		if (value == null) return def;
		return value;
	}

	public static <T> char lookupChar(Map<T, Character> map, T type) {
		return lookupChar(map, type, (char) 0);
	}

	public static <T> char lookupChar(Map<T, Character> map, T type, char def) {
		if (map == null) return def;
		Character value = map.get(type);
		if (value == null) return def;
		return value;
	}

	public static <T> byte lookupByte(Map<T, Byte> map, T type) {
		return lookupByte(map, type, (byte) 0);
	}

	public static <T> byte lookupByte(Map<T, Byte> map, T type, byte def) {
		if (map == null) return def;
		Byte value = map.get(type);
		if (value == null) return def;
		return value;
	}

	public static <T> short lookupShort(Map<T, Short> map, T type) {
		return lookupShort(map, type, (short) 0);
	}

	public static <T> short lookupShort(Map<T, Short> map, T type, short def) {
		if (map == null) return def;
		Short value = map.get(type);
		if (value == null) return def;
		return value;
	}

	public static <T> int lookupInt(Map<T, Integer> map, T type) {
		return lookupInt(map, type, 0);
	}

	public static <T> int lookupInt(Map<T, Integer> map, T type, int def) {
		if (map == null) return def;
		Integer value = map.get(type);
		if (value == null) return def;
		return value;
	}

	public static <T> long lookupLong(Map<T, Long> map, T type) {
		return lookupLong(map, type, 0L);
	}

	public static <T> long lookupLong(Map<T, Long> map, T type, long def) {
		if (map == null) return def;
		Long value = map.get(type);
		if (value == null) return def;
		return value;
	}

	public static <T> float lookupFloat(Map<T, Float> map, T type) {
		return lookupFloat(map, type, (float) 0);
	}

	public static <T> float lookupFloat(Map<T, Float> map, T type, float def) {
		if (map == null) return def;
		Float value = map.get(type);
		if (value == null) return def;
		return value;
	}

	public static <T> double lookupDouble(Map<T, Double> map, T type) {
		return lookupDouble(map, type, 0.0);
	}

	public static <T> double lookupDouble(Map<T, Double> map, T type, double def) {
		if (map == null) return def;
		Double value = map.get(type);
		if (value == null) return def;
		return value;
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Boolean valueOf(String value, Boolean def) {
		if (StringUtil.isBlank(value)) return def;
		return Boolean.valueOf(value.trim());
	}

	/**
	 * Converts a string to a value or returns null if the string cannot be parsed.
	 */
	public static Boolean booleanValue(String value) {
		return valueOf(value, (Boolean) null);
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Byte valueOf(String value, Byte def) {
		return valueOf(value, def, DECIMAL_RADIX);
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Byte valueOf(String value, Byte def, int radix) {
		try {
			if (StringUtil.isBlank(value)) return def;
			return NumberParser.parseByte(value.trim(), radix);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be decoded.
	 */
	public static Byte decode(String value, Byte def) {
		try {
			if (StringUtil.isBlank(value)) return def;
			return NumberParser.decodeByte(value.trim());
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Converts a string to a value or returns null if the string cannot be parsed.
	 */
	public static Byte byteValue(String value) {
		return valueOf(value, (Byte) null);
	}

	/**
	 * Converts a string to a value or returns null if the string cannot be parsed.
	 */
	public static Byte byteValue(String value, int radix) {
		return valueOf(value, (Byte) null, radix);
	}

	/**
	 * Converts a string to a value or returns null if the string cannot be decoded.
	 */
	public static Byte byteDecode(String value) {
		return decode(value, (Byte) null);
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Character valueOf(String value, Character def) {
		if (StringUtil.isBlank(value)) return def;
		return value.trim().charAt(0);
	}

	/**
	 * Converts a string to a value or returns null if the string cannot be parsed.
	 */
	public static Character charValue(String value) {
		return valueOf(value, (Character) null);
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Short valueOf(String value, Short def) {
		return valueOf(value, def, DECIMAL_RADIX);
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Short valueOf(String value, Short def, int radix) {
		try {
			if (StringUtil.isBlank(value)) return def;
			return NumberParser.parseShort(value, radix);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be decoded.
	 */
	public static Short decode(String value, Short def) {
		try {
			if (StringUtil.isBlank(value)) return def;
			return NumberParser.decodeShort(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Converts a string to a value or returns null if the string cannot be parsed.
	 */
	public static Short shortValue(String value) {
		return valueOf(value, (Short) null);
	}

	/**
	 * Converts a string to a value or returns null if the string cannot be parsed.
	 */
	public static Short shortValue(String value, int radix) {
		return valueOf(value, (Short) null, radix);
	}

	/**
	 * Converts a string to a value or returns null if the string cannot be decoded.
	 */
	public static Short shortDecode(String value) {
		return decode(value, (Short) null);
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Integer valueOf(String value, Integer def) {
		return valueOf(value, def, DECIMAL_RADIX);
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Integer valueOf(String value, Integer def, int radix) {
		try {
			if (StringUtil.isBlank(value)) return def;
			return NumberParser.parseInt(value, radix);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be decoded.
	 */
	public static Integer decode(String value, Integer def) {
		try {
			if (StringUtil.isBlank(value)) return def;
			return NumberParser.decodeInt(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Converts a string to a value or returns null if the string cannot be parsed.
	 */
	public static Integer intValue(String value) {
		return valueOf(value, (Integer) null);
	}

	/**
	 * Converts a string to a value or returns null if the string cannot be parsed.
	 */
	public static Integer intValue(String value, int radix) {
		return valueOf(value, (Integer) null, radix);
	}

	/**
	 * Converts a string to a value or returns null if the string cannot be decoded.
	 */
	public static Integer intDecode(String value) {
		return decode(value, (Integer) null);
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Long valueOf(String value, Long def) {
		return valueOf(value, def, DECIMAL_RADIX);
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Long valueOf(String value, Long def, int radix) {
		try {
			if (StringUtil.isBlank(value)) return def;
			return NumberParser.parseLong(value, radix);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be decoded.
	 */
	public static Long decode(String value, Long def) {
		try {
			if (StringUtil.isBlank(value)) return def;
			return NumberParser.decodeLong(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Converts a string to a value or returns null if the string cannot be parsed.
	 */
	public static Long longValue(String value) {
		return valueOf(value, (Long) null);
	}

	/**
	 * Converts a string to a value or returns null if the string cannot be parsed.
	 */
	public static Long longValue(String value, int radix) {
		return valueOf(value, (Long) null, radix);
	}

	/**
	 * Converts a string to a value or returns null if the string cannot be decoded.
	 */
	public static Long longDecode(String value) {
		return decode(value, (Long) null);
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Float valueOf(String value, Float def) {
		try {
			if (StringUtil.isBlank(value)) return def;
			return Float.valueOf(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Converts a string to a value or returns null if the string cannot be parsed.
	 */
	public static Float floatValue(String value) {
		return valueOf(value, (Float) null);
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Double valueOf(String value, Double def) {
		try {
			if (StringUtil.isBlank(value)) return def;
			return Double.valueOf(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Converts a string to a value or returns null if the string cannot be parsed.
	 */
	public static Double doubleValue(String value) {
		return valueOf(value, (Double) null);
	}

	/**
	 * Returns true if fromCls can be autoboxed to toCls.
	 */
	public static boolean isAutoBoxAssignable(Class<?> fromCls, Class<?> toCls) {
		toCls = toCls.isPrimitive() ? classMap.get(toCls) : toCls;
		fromCls = fromCls.isPrimitive() ? classMap.get(fromCls) : fromCls;
		return toCls.isAssignableFrom(fromCls);
	}

	/**
	 * Returns the corresponding primitive class for an object class. If the specified class is not
	 * an object class, exception is thrown.
	 */
	public static Class<?> primitiveClass(Class<?> objectCls) {
		if (objectCls.isPrimitive())
			throw new IllegalArgumentException("Class is already primitive type: " + objectCls);
		Class<?> primitiveCls = classMap.get(objectCls);
		if (primitiveCls == null) throw new IllegalArgumentException(
			"Class does not map to a primitive type: " + objectCls);
		return primitiveCls;
	}

	/**
	 * Returns the corresponding object class for a primitive class. If the specified class is not
	 * primitive, exception is thrown.
	 */
	public static Class<?> boxedClass(Class<?> primitiveCls) {
		if (!primitiveCls.isPrimitive())
			throw new IllegalArgumentException("Class is not primitive type: " + primitiveCls);
		return classMap.get(primitiveCls);
	}

	private static Map<Class<?>, Class<?>> createMap() {
		Map<Class<?>, Class<?>> classMap = new HashMap<>();
		add(classMap, boolean.class, Boolean.class);
		add(classMap, byte.class, Byte.class);
		add(classMap, char.class, Character.class);
		add(classMap, short.class, Short.class);
		add(classMap, int.class, Integer.class);
		add(classMap, long.class, Long.class);
		add(classMap, float.class, Float.class);
		add(classMap, double.class, Double.class);
		add(classMap, void.class, Void.class);
		return classMap;
	}

	private static void add(Map<Class<?>, Class<?>> classMap, Class<?> primitive, Class<?> object) {
		classMap.put(primitive, object);
		classMap.put(object, primitive);
	}

}
