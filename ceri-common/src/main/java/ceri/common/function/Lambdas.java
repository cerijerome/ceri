package ceri.common.function;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import ceri.common.util.BasicUtil;

/**
 * Utilities for functional interfaces.
 */
public class Lambdas {
	private static final Map<Object, Functions.Function<Object, String>> namers =
		Collections.synchronizedMap(new WeakHashMap<>());
	private static final String ANON_LAMBDA_LABEL = "$$Lambda/";
	public static final String LAMBDA_SYMBOL = "\u03bb";
	public static final String LAMBDA_NAME_DEF = "[lambda]";

	private Lambdas() {}

	/**
	 * Register a global name for an object; typically used for naming lambdas.
	 */
	public static <T> T register(T t, String name) {
		return register(t, _ -> name);
	}

	/**
	 * Register a global name supplier for an object; typically used for naming lambdas.
	 */
	public static <T> T register(T t, Functions.Function<T, String> namer) {
		if (t != null) namers.put(t, BasicUtil.unchecked(namer));
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
	 * Returns the registered name if set, lambda symbol if the given object is an anonymous lambda,
	 * otherwise toString.
	 */
	public static String nameOrSymbol(Object obj) {
		return lambda(obj, LAMBDA_SYMBOL);
	}

	/**
	 * Returns the registered name if set, "[lambda]" if the given object is an anonymous lambda,
	 * otherwise toString.
	 */
	public static String name(Object obj) {
		return lambda(obj, LAMBDA_NAME_DEF);
	}

	private static String lambda(Object obj, String anonNameDef) {
		var registered = registered(obj);
		if (registered != null) return registered;
		String s = String.valueOf(obj);
		return s.contains(ANON_LAMBDA_LABEL) ? anonNameDef : s;
	}
}
