package ceri.serial.jna;

import static ceri.common.exception.ExceptionUtil.exceptionf;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.util.BasicUtil;

/**
 * Utilities for c-style enums.
 */
public class JnaEnum {
	private static final Map<Class<?>, Map<Integer, Enum<?>>> valueToEnumMap =
		new ConcurrentHashMap<>();
	private static final Map<Class<?>, Map<Enum<?>, Integer>> enumToValueMap =
		new ConcurrentHashMap<>();

	private JnaEnum() {}

	/**
	 * Provides int value() for an enum class. Values are initialized on first access.
	 */
	public static interface Valued {
		default int value() {
			var cls = getClass();
			if (!cls.isEnum()) return 0;
			init(cls);
			return JnaEnum.enumValue((Enum<?>) this);
		}
	}

	/**
	 * Specifies the enum int value, and whether to allow duplicate values. Subsequent enums
	 * increment the value by 1. When declared on the enum class, this specifies this is applied to
	 * the first enum.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.TYPE })
	public static @interface Value {
		/**
		 * The value for the enum. Subsequent enums increment by 1.
		 */
		public int value() default 0;

		/**
		 * Determines if duplicate values are allowed from this point.
		 */
		public boolean duplicates() default false;
	}

	/**
	 * Looks up enum by value.
	 */
	public static <T extends Enum<T> & Valued> T from(Class<T> cls, int value) {
		init(cls);
		var valueMap = valueToEnumMap.get(cls);
		if (valueMap == null) return null;
		var en = valueMap.get(value);
		return en == null ? null : BasicUtil.uncheckedCast(en);
	}

	/**
	 * Looks up enum by ordinal value.
	 */
	public static <T extends Enum<T>> T fromOrdinal(Class<T> cls, int ordinal) {
		T[] enums = cls.getEnumConstants();
		if (ordinal < 0 || ordinal >= enums.length) return null;
		return enums[ordinal];
	}

	private static int enumValue(Enum<?> en) {
		var enumMap = enumToValueMap.get(en.getClass());
		if (enumMap == null) return en.ordinal();
		var value = enumMap.get(en);
		return value == null ? en.ordinal() : value;
	}

	private static void init(Class<?> cls) {
		if (valueToEnumMap.containsKey(cls)) return;
		ExceptionAdapter.RUNTIME.run(() -> initEnums(cls));
	}

	private static record CurrentValue(int value, boolean duplicates) {}

	private static void initEnums(Class<?> cls) throws NoSuchFieldException {
		Map<Integer, Enum<?>> valueMap = new TreeMap<>();
		Map<Enum<?>, Integer> enumMap = new TreeMap<>();
		CurrentValue current = currentValue(cls);
		for (var en : (Enum<?>[]) cls.getEnumConstants()) {
			current = currentValue(cls, en, current);
			var dup = valueMap.put(current.value, en);
			if (dup != null && !current.duplicates) throw exceptionf( //
				"Duplicate value for %s and %s: 0x%3$x (%3$d)", en, dup, current.value);
			enumMap.put(en, current.value);
			current = new CurrentValue(current.value + 1, current.duplicates);
		}
		valueToEnumMap.put(cls, Collections.unmodifiableMap(valueMap));
		enumToValueMap.put(cls, Collections.unmodifiableMap(enumMap));
	}

	private static CurrentValue currentValue(Class<?> cls) {
		Value value = cls.getAnnotation(Value.class);
		if (value == null) return new CurrentValue(0, false);
		return new CurrentValue(value.value(), value.duplicates());
	}

	private static CurrentValue currentValue(Class<?> cls, Enum<?> en, CurrentValue current)
		throws NoSuchFieldException {
		Value value = cls.getField(en.name()).getAnnotation(Value.class);
		if (value == null) return current;
		return new CurrentValue(value.value(), value.duplicates());
	}
}
