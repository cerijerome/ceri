package ceri.common.function;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Utilities to name functional interfaces.
 */
public class Namer {
	private static final String ANON_LAMBDA_LABEL = "$$Lambda/";
	public static final String LAMBDA_SYMBOL = "\u03bb";
	public static final String LAMBDA_NAME_DEF = "[lambda]";

	private Namer() {}

	/**
	 * Checks if the given object is an anonymous lamdba function.
	 */
	public static boolean unnamedLambda(Object obj) {
		if (obj == null) return false;
		String s = obj.toString();
		return s != null && s.contains(ANON_LAMBDA_LABEL);
	}

	/**
	 * Returns lambda symbol if the given object is an anonymous lambda, otherwise toString.
	 */
	public static String lambdaSymbol(Object obj) {
		return lambda(obj, LAMBDA_SYMBOL);
	}

	/**
	 * Returns "[lambda]" if the given object is an anonymous lambda, otherwise toString.
	 */
	public static String lambda(Object obj) {
		return lambda(obj, LAMBDA_NAME_DEF);
	}

	/**
	 * Returns given name if the given object is an anonymous lambda, otherwise toString.
	 */
	public static String lambda(Object obj, String anonNameDef) {
		String s = String.valueOf(obj);
		return s.contains(ANON_LAMBDA_LABEL) ? anonNameDef : s;
	}

	/**
	 * Overrides toString() with given name.
	 */
	public static <T, R> Function<T, R> function(Function<T, R> function, String name) {
		return new Function<>() {
			@Override
			public R apply(T t) {
				return function.apply(t);
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}

	/**
	 * Overrides toString() with given name.
	 */
	public static <T> Consumer<T> consumer(Consumer<T> consumer, String name) {
		return new Consumer<>() {
			@Override
			public void accept(T t) {
				consumer.accept(t);
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}

	/**
	 * Overrides toString() with given name.
	 */
	public static <T> Supplier<T> supplier(Supplier<T> supplier, String name) {
		return new Supplier<>() {
			@Override
			public T get() {
				return supplier.get();
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}

	/**
	 * Overrides toString() with given name.
	 */
	public static <T> Predicate<T> predicate(Predicate<T> predicate, String name) {
		return new Predicate<>() {
			@Override
			public boolean test(T t) {
				return predicate.test(t);
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}

	/**
	 * Overrides toString() with given name.
	 */
	public static IntPredicate intPredicate(IntPredicate predicate, String name) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return predicate.test(value);
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}
}
