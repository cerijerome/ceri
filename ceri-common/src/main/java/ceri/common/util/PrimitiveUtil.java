package ceri.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for handling primitives and primitive wrappers.
 */
public class PrimitiveUtil {
	private static final int DECIMAL_RADIX = 10;
	private static final Map<Class<?>, Class<?>> classMap = createMap();

	private PrimitiveUtil() {}

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
		if (BasicUtil.isEmpty(value)) return def;
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
			if (BasicUtil.isEmpty(value)) return def;
			return Byte.valueOf(value.trim(), radix);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Byte decode(String value, Byte def) {
		try {
			if (BasicUtil.isEmpty(value)) return def;
			return Byte.decode(value.trim());
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
	 * Converts a string to a value or returns null if the string cannot be parsed.
	 */
	public static Byte byteDecode(String value) {
		return decode(value, (Byte) null);
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Character valueOf(String value, Character def) {
		if (BasicUtil.isEmpty(value)) return def;
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
			if (BasicUtil.isEmpty(value)) return def;
			return Short.valueOf(value, radix);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Short decode(String value, Short def) {
		try {
			if (BasicUtil.isEmpty(value)) return def;
			return Short.decode(value);
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
	 * Converts a string to a value or returns null if the string cannot be parsed.
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
			if (BasicUtil.isEmpty(value)) return def;
			return Integer.valueOf(value, radix);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Integer decode(String value, Integer def) {
		try {
			if (BasicUtil.isEmpty(value)) return def;
			return Integer.decode(value);
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
	 * Converts a string to a value or returns null if the string cannot be parsed.
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
			if (BasicUtil.isEmpty(value)) return def;
			return Long.valueOf(value, radix);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Long decode(String value, Long def) {
		try {
			if (BasicUtil.isEmpty(value)) return def;
			return Long.decode(value);
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
	 * Converts a string to a value or returns null if the string cannot be parsed.
	 */
	public static Long longDecode(String value) {
		return decode(value, (Long) null);
	}

	/**
	 * Converts a string to a value or returns the default value if the string cannot be parsed.
	 */
	public static Float valueOf(String value, Float def) {
		try {
			if (BasicUtil.isEmpty(value)) return def;
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
			if (BasicUtil.isEmpty(value)) return def;
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
