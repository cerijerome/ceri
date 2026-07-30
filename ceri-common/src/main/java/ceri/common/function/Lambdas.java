package ceri.common.function;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import ceri.common.collect.Maps;
import ceri.common.reflect.Reflect;
import ceri.common.text.Strings;

/**
 * Utilities for functional interfaces.
 */
public class Lambdas {
	private static final Map<Object, Functions.Function<Object, String>> namers = Maps.syncWeak();
	private static final int LAMBDA_METHOD_MODS = Modifier.ABSTRACT | Modifier.PUBLIC;
	private static final int METHOD_MODS = LAMBDA_METHOD_MODS | Modifier.STATIC;
	private static final String ANON_LAMBDA_LABEL = "$$Lambda/";
	public static final String LAMBDA_SYMBOL = "\u03bb";
	public static final String LAMBDA_NAME_DEF = "[lambda]";

	private Lambdas() {}

	/**
	 * Returns the first lambda-compatible implemented interface, or null.
	 */
	public static Class<?> type(Object obj) {
		if (obj == null) return null;
		for (var iface : obj.getClass().getInterfaces())
			if (compatible(iface)) return iface;
		return null;
	}

	/**
	 * Returns true if the class is a lambda-compatible interface.
	 */
	public static boolean compatible(Class<?> cls) {
		return method(cls) != null;
	}

	/**
	 * Returns the single abstract method for a lambda-compatible interface, or null.
	 */
	public static Method method(Class<?> cls) {
		if (!cls.isInterface()) return null;
		Method last = null;
		for (var method : cls.getMethods()) {
			int mods = method.getModifiers();
			if ((mods & METHOD_MODS) != LAMBDA_METHOD_MODS) continue;
			if (last == null) last = method;
			else if (!compatible(method, last)) return null;
		}
		return last;
	}

	/**
	 * Register a global name for an object; can be used to name lambdas.
	 */
	public static <T> T register(T t, String format, Object... args) {
		var name = Strings.format(format, args);
		return register(t, _ -> name);
	}

	/**
	 * Register a global name supplier for an object; can be used to name lambdas.
	 */
	public static <T> T register(T t, Functions.Function<T, String> namer) {
		if (t != null) namers.put(t, Reflect.unchecked(namer));
		return t;
	}

	/**
	 * Retrieves an object's registered global name, or null.
	 */
	public static String registered(Object obj) {
		if (obj == null) return null;
		var namer = namers.get(obj);
		return namer == null ? null : namer.apply(obj);
	}

	/**
	 * Checks if the given object is an anonymous lamdba function.
	 */
	public static boolean isAnon(Object obj) {
		if (obj == null) return false;
		String s = obj.toString();
		return s != null && s.contains(ANON_LAMBDA_LABEL);
	}

	/**
	 * Returns the registered name if set, "[lambda]" if the given object is an anonymous lambda,
	 * otherwise toString.
	 */
	public static String name(Object obj) {
		return name(obj, () -> LAMBDA_NAME_DEF);
	}

	/**
	 * Returns the registered name if set, lambda symbol if the given object is an anonymous lambda,
	 * otherwise toString.
	 */
	public static String nameOrSymbol(Object obj) {
		return name(obj, () -> LAMBDA_SYMBOL);
	}

	/**
	 * Returns the registered name if set, or the lambda class name and hash, otherwise toString.
	 */
	public static String nameOrHash(Object obj) {
		return name(obj, () -> Reflect.simple(type(obj)) + "#"
			+ Integer.toHexString(System.identityHashCode(obj)));
	}

	// support

	private static boolean compatible(Method m1, Method m2) {
		if (!m1.getName().equals(m2.getName())) return false;
		if (m1.getReturnType() != m2.getReturnType()) return false;
		if (m1.getParameterCount() != m2.getParameterCount()) return false;
		var m1Args = m1.getParameters();
		var m2Args = m2.getParameters();
		for (int i = 0; i < m1Args.length; i++)
			if (m1Args[i].getType() != m2Args[i].getType()) return false;
		return true;
	}

	private static String name(Object obj, Functions.Supplier<String> op) {
		if (obj == null) return Strings.NULL;
		var registered = registered(obj);
		if (registered != null) return registered;
		var s = String.valueOf(obj);
		return s.contains(ANON_LAMBDA_LABEL) ? op.get() : s;
	}
}
