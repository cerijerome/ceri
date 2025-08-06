package ceri.jna.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import ceri.common.collection.ImmutableUtil;
import ceri.common.reflect.Annotations;
import ceri.common.util.BasicUtil;

/**
 * Utilities for c-style enums.
 */
public class JnaEnum {
	private static final Map<Class<?>, Object[]> cachedEnums = new ConcurrentHashMap<>();
	private static final Map<Class<?>, Map<Integer, Enum<?>>> valueToEnumMap =
		new ConcurrentHashMap<>();
	private static final Map<Class<?>, Map<Enum<?>, Integer>> enumToValueMap =
		new ConcurrentHashMap<>();

	private JnaEnum() {}

	/**
	 * Provides an int value() for an enum class. By default the value is determined based on
	 * ordinal, and overridden by @Value on class and/or enums. Values are initialized on first call
	 * to value().
	 */
	public static interface Valued {
		default int value() {
			var cls = getClass();
			if (!cls.isEnum()) return 0;
			if (!initialized(cls)) initFromAnnotations(BasicUtil.unchecked(cls));
			return JnaEnum.enumValue((Enum<?>) this);
		}
	}

	/**
	 * Specifies the enum int value. Subsequent enums increment the value by 1. When declared on the
	 * enum class, the value is applied to the first enum.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.TYPE })
	public static @interface Value {
		public int value() default 0;
	}

	/**
	 * Looks up enum by value. Initializes mapping if not
	 */
	public static <T extends Enum<T> & Valued> T from(Class<T> cls, int value) {
		init(cls);
		var en = valueToEnumMap.get(cls).get(value);
		if (en != null) return BasicUtil.unchecked(en);
		var t = fromOrdinal(cls, value);
		return t != null && t.value() == value ? t : null;
	}

	/**
	 * Look up enum from ordinal value. Returns null if out of ordinal range. Caches enum array to
	 * avoid clone() on each call to Class.getEnumConstants().
	 */
	public static <T extends Enum<T>> T fromOrdinal(Class<T> cls, int ordinal) {
		Object[] objs = cachedEnums.computeIfAbsent(cls, Class::getEnumConstants);
		if (ordinal < 0 || ordinal >= objs.length) return null;
		return BasicUtil.unchecked(objs[ordinal]);
	}

	/* private */

	private static boolean initialized(Class<?> cls) {
		return valueToEnumMap.containsKey(cls);
	}

	private static int enumValue(Enum<?> en) {
		var value = enumToValueMap.get(en.getClass()).get(en);
		return value != null ? value : en.ordinal();
	}

	private static <T extends Enum<T> & Valued> void init(Class<T> cls) {
		if (initialized(cls)) return;
		var en = fromOrdinal(cls, 0);
		if (en != null) en.value(); // may call initFromAnnotations()
		if (initialized(cls)) return; // check again
		initFromValues(cls); // fallback to value lookup - assume override
	}

	private static <T extends Enum<T> & Valued> void initFromValues(Class<T> cls) {
		Map<Integer, Enum<?>> valueMap = new TreeMap<>();
		Map<Enum<?>, Integer> enumMap = new TreeMap<>();
		for (var t : cls.getEnumConstants()) {
			int value = t.value();
			if (value == t.ordinal()) continue; // no need to map ordinals
			valueMap.put(value, t);
			enumMap.put(t, value);
		}
		valueToEnumMap.put(cls, ImmutableUtil.wrapMap(valueMap));
		enumToValueMap.put(cls, ImmutableUtil.wrapMap(enumMap));
	}

	private static <T extends Enum<T> & Valued> void initFromAnnotations(Class<T> cls) {
		Map<Integer, Enum<?>> valueMap = new TreeMap<>();
		Map<Enum<?>, Integer> enumMap = new TreeMap<>();
		int value = Annotations.value(cls, Value.class, Value::value, 0);
		for (var t : cls.getEnumConstants()) {
			value = Annotations.value(t, Value.class, Value::value, value);
			if (value != t.ordinal()) { // no need to map ordinals
				valueMap.put(value, t);
				enumMap.put(t, value);
			}
			value++;
		}
		valueToEnumMap.put(cls, ImmutableUtil.wrapMap(valueMap));
		enumToValueMap.put(cls, ImmutableUtil.wrapMap(enumMap));
	}

}
