package ceri.common.function;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import ceri.common.util.BasicUtil;
import ceri.common.util.EqualsUtil;

public class FunctionUtil {
	private static final int MAX_RECURSIONS_DEF = 20;
	private static final Predicate<Object> TRUE_PREDICATE = t -> true;
	private static final Consumer<Object> NULL_CONSUMER = t -> {};

	private FunctionUtil() {}

	public static <T> Consumer<T> nullConsumer() {
		return BasicUtil.uncheckedCast(NULL_CONSUMER);
	}

	public static <T> Predicate<T> truePredicate() {
		return BasicUtil.uncheckedCast(TRUE_PREDICATE);
	}

	/**
	 * Wraps a function that passes through null values.
	 */
	public static <T, R> Function<T, R> safe(Function<T, R> function) {
		return t -> t == null ? null : function.apply(t);
	}

	/**
	 * Passes only non-null values to consumer.
	 */
	public static <T> void safeAccept(T t, Consumer<T> consumer) {
		if (t != null) consumer.accept(t);
	}

	/**
	 * Passes only non-null values to consumer.
	 */
	public static <T> void safeAccept(T t, Predicate<T> predicate, Consumer<T> consumer) {
		if (t != null && predicate.test(t)) consumer.accept(t);
	}

	/**
	 * Execute the function until no change, or the maximum number of recursions is met.
	 */
	public static <T> T recurse(T t, Function<T, T> fn) {
		return recurse(t, fn, MAX_RECURSIONS_DEF);
	}

	/**
	 * Execute the function until no change, or the maximum number of recursions is met.
	 */
	public static <T> T recurse(T t, Function<T, T> fn, int max) {
		T last;
		while (max-- > 0) {
			last = t;
			t = fn.apply(t);
			if (EqualsUtil.equals(t, last)) break;
		}
		return t;
	}

	/**
	 * Executes for-each, allowing exception of given type to be thrown.
	 */
	public static <E extends Exception, T> void forEach(Iterable<T> iter,
		ExceptionConsumer<E, ? super T> consumer) throws E {
		FunctionWrapper<E> w = FunctionWrapper.create();
		w.handle(() -> iter.forEach(w.wrap(consumer)));
	}

	/**
	 * Executes for-each, allowing exception of given type to be thrown.
	 */
	public static <E extends Exception, T> void forEach(Stream<T> stream,
		ExceptionConsumer<E, ? super T> consumer) throws E {
		FunctionWrapper<E> w = FunctionWrapper.create();
		w.handle(() -> stream.forEach(w.wrap(consumer)));
	}

	/**
	 * Executes for-each, allowing exception of given type to be thrown.
	 */
	public static <E extends Exception, K, V> void forEach(Map<K, V> map,
		ExceptionBiConsumer<E, ? super K, ? super V> consumer) throws E {
		FunctionWrapper<E> w = FunctionWrapper.create();
		w.handle(() -> map.forEach(w.wrapBi(consumer)));
	}

	public static <E extends Exception, T> ExceptionFunction<E, T, Boolean>
		asFunction(ExceptionRunnable<E> runnable) {
		return t -> {
			runnable.run();
			return Boolean.TRUE;
		};
	}

	public static <E extends Exception, T> ExceptionFunction<E, T, Boolean>
		asFunction(ExceptionConsumer<E, T> consumer) {
		return t -> {
			consumer.accept(t);
			return Boolean.TRUE;
		};
	}

	public static <E extends Exception, T, U> ExceptionFunction<E, U, T>
		asFunction(ExceptionSupplier<E, T> supplier) {
		return t -> supplier.get();
	}

	public static <E extends Exception, T> ExceptionRunnable<E> asRunnable(T t,
		ExceptionConsumer<E, T> consumer) {
		return () -> consumer.accept(t);
	}

	public static <E extends Exception, T> ExceptionRunnable<E> asRunnable(T t,
		ExceptionFunction<E, T, ?> fn) {
		return () -> fn.apply(t);
	}

	public static <E extends Exception, T, U> ExceptionConsumer<E, T> asConsumer(
		ExceptionFunction<E, T, U> fn) {
		return t -> fn.apply(t);
	}
	
}
