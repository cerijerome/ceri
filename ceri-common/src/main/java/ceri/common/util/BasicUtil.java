/*
 * Created on Jul 31, 2005
 */
package ceri.common.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;

/**
 * Basic utility methods.
 */
public class BasicUtil {
	private static final Pattern PACKAGE_REGEX =
		Pattern.compile("(?<![\\w$])([a-z$])[a-z0-9_$]+\\.");
	private final static Map<Class<?>, Object> loadedClasses = new WeakHashMap<>();

	private BasicUtil() {}

	/**
	 * Stops the warning for an unused parameter. Use only when absolutely necessary.
	 */
	public static void unused(@SuppressWarnings("unused") Object... o) {}

	/**
	 * Are you really sure you need to call this? If you're not sure why you need to call this
	 * method you may be hiding a coding error. Performs an unchecked cast from an object to the
	 * given type, preventing a warning. Sometimes necessary for collections, etc. Will not prevent
	 * a runtime cast exception.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T uncheckedCast(Object o) {
		return (T) o;
	}

	/**
	 * Abbreviates package names, e.g. abc.def.MyClass -> a.d.MyClass
	 */
	public static String abbreviatePackages(String stackTrace) {
		if (stackTrace == null) return null;
		return PACKAGE_REGEX.matcher(stackTrace).replaceAll(m -> "$1.");
	}

	/**
	 * Executes a try-with-resources from type, action function, and close function.
	 */
	public static <E extends Exception, T> void tryWithResources(T t,
		ExceptionConsumer<E, T> actionFn, ExceptionConsumer<E, T> closeFn) throws E {
		try {
			if (t != null) actionFn.accept(t);
		} finally {
			if (t != null) closeFn.accept(t);
		}
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static <T> T defaultValue(T value, T def) {
		return value != null ? value : def;
	}

	/**
	 * Returns a value based on condition of null, true, false.
	 */
	public static <T> T conditional(Boolean condition, T trueValue, T falseValue) {
		return conditional(condition, trueValue, falseValue, null);
	}

	/**
	 * Returns a value based on condition of null, true, false.
	 */
	public static <T> T conditional(Boolean condition, T trueValue, T falseValue, T nullValue) {
		if (condition == null) return nullValue;
		return condition ? trueValue : falseValue;
	}

	/**
	 * Returns a value based on condition.
	 */
	public static int conditionalInt(boolean condition, int trueValue, int falseValue) {
		return condition ? trueValue : falseValue;
	}

	/**
	 * Returns a value based on condition.
	 */
	public static long conditionalLong(boolean condition, long trueValue, long falseValue) {
		return condition ? trueValue : falseValue;
	}

	/**
	 * Make a system beep sound
	 */
	public static void beep() {
		// Disabled to avoid annoying unit test sound...
		// Toolkit.getDefaultToolkit().beep();
	}

	/**
	 * Convenience method that calls Enum.valueOf and returns null if no match.
	 */
	public static <T extends Enum<T>> T valueOf(Class<T> cls, String value) {
		return valueOf(cls, value, null);
	}

	/**
	 * Convenience method that calls Enum.valueOf and returns default value if no match.
	 */
	public static <T extends Enum<T>> T valueOf(Class<T> cls, String value, T def) {
		if (value == null || cls == null) return def;
		try {
			return Enum.valueOf(cls, value);
		} catch (IllegalArgumentException e) {
			return def;
		}
	}

	/**
	 * Finds the first enum matching the filter.
	 */
	public static <T extends Enum<T>> T find(Class<T> cls, Predicate<T> filter) {
		return find(cls, filter, null);
	}

	/**
	 * Finds the first enum matching the filter.
	 */
	public static <T extends Enum<T>> T find(Class<T> cls, Predicate<T> filter, T def) {
		return BasicUtil.enums(cls).stream().filter(filter).findFirst().orElse(def);
	}

	/**
	 * Convenience method that returns all enum constants as a list.
	 */
	public static <T extends Enum<T>> List<T> enums(Class<T> cls) {
		return Arrays.asList(cls.getEnumConstants());
	}

	/**
	 * Convenience method that returns all enum constants as a list in reverse order.
	 */
	public static <T extends Enum<T>> List<T> enumsReversed(Class<T> cls) {
		List<T> list = enums(cls);
		Collections.reverse(list);
		return list;
	}

	/**
	 * Makes an iterator compatible with a for-each loop.
	 */
	public static <T> Iterable<T> forEach(final Iterator<T> iterator) {
		return () -> iterator;
	}

	/**
	 * Casts object to given type or returns null if not compatible.
	 */
	public static <T> T castOrNull(Class<T> cls, Object obj) {
		if (!cls.isInstance(obj)) return null;
		return cls.cast(obj);
	}

	/**
	 * Checks if the given map is null or empty.
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * Checks if the given collection is null or empty.
	 */
	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * Execute runnable and convert non-runtime exceptions to runtime.
	 */
	public static void runtimeRun(ExceptionRunnable<?> runnable) {
		ExceptionAdapter.RUNTIME.run(runnable);
	}

	/**
	 * Execute callable and convert non-runtime exceptions to runtime.
	 */
	public static <T> T runtimeCall(ExceptionSupplier<?, T> callable) {
		return ExceptionAdapter.RUNTIME.get(callable);
	}

	/**
	 * Makes sure a Class<?> is loaded. Use when static initialization is required but only the
	 * Class<?> is referenced.
	 */
	public static <T> Class<T> forceInit(Class<T> cls) {
		if (loadedClasses.containsKey(cls)) return cls;
		load(cls.getName(), cls.getClassLoader());
		loadedClasses.put(cls, null);
		return cls;
	}

	static void load(String className, ClassLoader classLoader) {
		try {
			Class.forName(className, true, classLoader);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find Class<?> " + className, e);
		}
	}

}
