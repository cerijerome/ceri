package ceri.common.function;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
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
	 * Run and ignore any exceptions. Returns true if no exception occurred. Use judiciously, such
	 * as when closing an object, to squash noisy exceptions.
	 */
	public static <E extends Exception> boolean execSilently(ExceptionRunnable<E> runnable) {
		try {
			runnable.run();
			return true;
		} catch (Exception e) {
			// ignore
			return false;
		}
	}

	/**
	 * Casts object to given type and applies function if compatible. Otherwise returns null.
	 */
	public static <E extends Exception, T, R> R castApply(Class<T> cls, Object obj,
		ExceptionFunction<E, T, R> fn) throws E {
		if (cls == null || fn == null || !cls.isInstance(obj)) return null;
		return fn.apply(cls.cast(obj));
	}

	/**
	 * Casts object to given type and applies consumer if compatible. Returns true if consumed.
	 */
	public static <E extends Exception, T> boolean castAccept(Class<T> cls, Object obj,
		ExceptionConsumer<E, T> consumer) throws E {
		if (cls == null || consumer == null || !cls.isInstance(obj)) return false;
		consumer.accept(cls.cast(obj));
		return true;
	}

	/**
	 * Wraps a function, passing null values back to caller.
	 */
	public static <E extends Exception, T, R> ExceptionFunction<E, T, R>
		safe(ExceptionFunction<E, T, R> function) {
		return t -> safeApply(t, function);
	}

	/**
	 * Passes only non-null values to function.
	 */
	public static <E extends Exception, T, R> R safeApply(T t, ExceptionFunction<E, T, R> function)
		throws E {
		return t == null ? null : function.apply(t);
	}

	/**
	 * Passes only non-null values to consumer. Returns true if consumed.
	 */
	public static <E extends Exception, T> boolean safeAccept(T t, ExceptionConsumer<E, T> consumer)
		throws E {
		if (t == null) return false;
		consumer.accept(t);
		return true;
	}

	/**
	 * Passes only non-null values to consumer. Returns true if consumed.
	 */
	public static <E extends Exception, T> boolean safeAccept(T t, ExceptionPredicate<E, T> predicate,
		ExceptionConsumer<E, T> consumer) throws E {
		if (t == null || !predicate.test(t)) return false;
		consumer.accept(t);
		return true;
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
		w.unwrap(() -> iter.forEach(w.wrap(consumer)));
	}

	/**
	 * Executes for-each, allowing exception of given type to be thrown.
	 */
	public static <E extends Exception, T> void forEach(Stream<T> stream,
		ExceptionConsumer<E, ? super T> consumer) throws E {
		FunctionWrapper<E> w = FunctionWrapper.create();
		w.unwrap(() -> stream.forEach(w.wrap(consumer)));
	}

	/**
	 * Executes for-each, allowing exception of given type to be thrown.
	 */
	public static <E extends Exception, K, V> void forEach(Map<K, V> map,
		ExceptionBiConsumer<E, ? super K, ? super V> consumer) throws E {
		FunctionWrapper<E> w = FunctionWrapper.create();
		w.unwrap(() -> map.forEach(w.wrap(consumer)));
	}

	/**
	 * Executes map, allowing exception of given type to be thrown.
	 */
	public static <E extends Exception, T, R> Stream<R> map(Stream<T> stream,
		ExceptionFunction<E, ? super T, ? extends R> fn) throws E {
		FunctionWrapper<E> w = FunctionWrapper.create();
		return w.unwrap(() -> stream.map(w.wrap(fn)));
	}

	/**
	 * Executes map, allowing exception of given type to be thrown.
	 */
	public static <E extends Exception> IntStream map(IntStream stream,
		ExceptionIntUnaryOperator<E> fn) throws E {
		FunctionWrapper<E> w = FunctionWrapper.create();
		return w.unwrap(() -> stream.map(w.wrap(fn)));
	}

	/**
	 * Executes map, allowing exception of given type to be thrown.
	 */
	public static <E extends Exception, R> Stream<R> mapToObj(IntStream stream,
		ExceptionIntFunction<E, ? extends R> fn) throws E {
		FunctionWrapper<E> w = FunctionWrapper.create();
		return w.unwrap(() -> stream.mapToObj(w.wrap(fn)));
	}

	public static <T> Predicate<T> and(Predicate<T> lhs, Predicate<T> rhs) {
		return lhs == null ? rhs : rhs == null ? lhs : lhs.and(rhs);
	}

	public static <T> Predicate<T> or(Predicate<T> lhs, Predicate<T> rhs) {
		return lhs == null ? rhs : rhs == null ? lhs : lhs.or(rhs);
	}

	public static <T> Predicate<T> namedPredicate(Predicate<T> predicate, String name) {
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
	 * Converts a runnable to a function that returns true.
	 */
	public static <E extends Exception, T> ExceptionFunction<E, T, Boolean> asFunction(
		ExceptionRunnable<E> runnable) {
		return t -> {
			runnable.run();
			return Boolean.TRUE;
		};
	}

	/**
	 * Converts a supplier to a function that ignores input.
	 */
	public static <E extends Exception, T> ExceptionFunction<E, ?, T> asFunction(
		ExceptionSupplier<E, T> supplier) {
		return t -> supplier.get();
	}

	/**
	 * Converts a supplier to a function that ignores input.
	 */
	public static <E extends Exception> ExceptionToIntFunction<E, ?> asToIntFunction(
		ExceptionIntSupplier<E> supplier) {
		return t -> supplier.getAsInt();
	}


	public static <E extends Exception, T> ExceptionFunction<E, T, Boolean> asFunction(
		ExceptionConsumer<E, T> consumer) {
		return t -> {
			consumer.accept(t);
			return Boolean.TRUE;
		};
	}

	public static <E extends Exception> ExceptionIntFunction<E, Boolean> asIntFunction(
		ExceptionIntConsumer<E> consumer) {
		return i -> {
			consumer.accept(i);
			return Boolean.TRUE;
		};
	}

	public static <E extends Exception, T, U> ExceptionBiFunction<E, T, U, Boolean> asBiFunction(
		ExceptionBiConsumer<E, T, U> consumer) {
		return (t, u) -> {
			consumer.accept(t, u);
			return Boolean.TRUE;
		};
	}

}
