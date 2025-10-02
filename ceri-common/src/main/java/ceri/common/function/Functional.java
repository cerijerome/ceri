package ceri.common.function;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import ceri.common.concurrent.Concurrent;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.reflect.Reflect;
import ceri.common.util.Holder;

/**
 * Function utilities.
 */
public class Functional {
	private static final int MAX_RECURSIONS_DEF = 20;
	private static final Functions.Predicate<Object> TRUE_PREDICATE = _ -> true;
	public static final Functions.Runnable NULL_RUNNABLE = () -> {};
	private static final Functions.Consumer<Object> NULL_CONSUMER = _ -> {};

	private Functional() {}

	/**
	 * Provides a no-op consumer.
	 */
	public static <T> Functions.Consumer<T> nullConsumer() {
		return Reflect.unchecked(NULL_CONSUMER);
	}

	/**
	 * Provides a predicate that is always true.
	 */
	public static <T> Functions.Predicate<T> truePredicate() {
		return Reflect.unchecked(TRUE_PREDICATE);
	}

	/**
	 * Safely applies function. Returns null if function or operand is null.
	 */
	public static <E extends Exception, T, U> U
		apply(Excepts.Function<E, ? super T, ? extends U> function, T t) throws E {
		return function == null || t == null ? null : function.apply(t);
	}

	/**
	 * Invokes the supplier and returns the result, which may be null. Errors will be thrown, but
	 * exceptions will be suppressed, and null returned. Interrupted exceptions will re-interrupt
	 * the thread.
	 */
	public static <T> T getSilently(Excepts.Supplier<?, T> supplier) {
		return getSilently(supplier, null);
	}

	/**
	 * Invokes the supplier and returns the result. Errors will be thrown, but exceptions will be
	 * suppressed, and an error value returned. Interrupted exceptions will re-interrupt the thread.
	 */
	public static <T> T getSilently(Throws.Supplier<? extends T> supplier, T errorVal) {
		return getIt(supplier, errorVal);
	}

	/**
	 * Invokes the runnable and returns true. Errors will be thrown, but exceptions will be
	 * suppressed, and false returned. Interrupted exceptions will re-interrupt the thread.
	 */
	public static boolean runSilently(Throws.Runnable runnable) {
		return getSilently(() -> {
			runnable.run();
			return true;
		}, false);
	}

	/**
	 * Provide sequential access to the given values, repeating the last entry. If no values are
	 * given, the supplier will always return null.
	 */
	@SafeVarargs
	public static <T> Functions.Supplier<T> sequentialSupplier(T... ts) {
		return sequentialSupplier(Arrays.asList(ts));
	}

	/**
	 * Provide sequential access to the given values, repeating the last entry. If no values are
	 * given, the supplier will always return null.
	 */
	public static <T> Functions.Supplier<T> sequentialSupplier(Iterable<T> ts) {
		var i = ts.iterator();
		var holder = Holder.<T>mutable();
		return () -> {
			if (i.hasNext()) holder.set(i.next());
			return holder.value();
		};
	}

	/**
	 * Consumer called for each supplied value until null.
	 */
	public static <E extends Exception, T> void forEach(Excepts.Supplier<E, T> supplier,
		Excepts.Consumer<E, T> consumer) throws E {
		for (var t = supplier.get(); t != null; t = supplier.get())
			consumer.accept(t);
	}

	/**
	 * Creates an optional instance of the type; empty if the flag indicates not valid.
	 */
	public static <T> Optional<T> optional(boolean valid, T value) {
		return valid ? Optional.ofNullable(value) : Optional.empty();
	}

	/**
	 * Creates an optional instance with empty() if null (same as Optional.OfNullable).
	 */
	public static OptionalInt optional(Integer value) {
		return value == null ? OptionalInt.empty() : OptionalInt.of(value);
	}

	/**
	 * Creates an optional instance with empty() if null (same as Optional.OfNullable).
	 */
	public static OptionalLong optional(Long value) {
		return value == null ? OptionalLong.empty() : OptionalLong.of(value);
	}

	/**
	 * Creates an optional instance with empty() if null (same as Optional.OfNullable).
	 */
	public static OptionalDouble optional(Double value) {
		return value == null ? OptionalDouble.empty() : OptionalDouble.of(value);
	}

	/**
	 * Returns the value if present, otherwise null.
	 */
	public static Integer value(OptionalInt optional) {
		return optional.isEmpty() ? null : optional.getAsInt();
	}

	/**
	 * Returns the value if present, otherwise null.
	 */
	public static Long value(OptionalLong optional) {
		return optional.isEmpty() ? null : optional.getAsLong();
	}

	/**
	 * Returns the value if present, otherwise null.
	 */
	public static Double value(OptionalDouble optional) {
		return optional.isEmpty() ? null : optional.getAsDouble();
	}

	/**
	 * Passes only non-null values to function.
	 */
	public static <E extends Exception, T, R> R safeApply(T t,
		Excepts.Function<E, ? super T, R> function) throws E {
		return safeApply(t, function, null);
	}

	/**
	 * Passes only non-null values to function.
	 */
	public static <E extends Exception, T, R> R safeApply(T t,
		Excepts.Function<E, ? super T, ? extends R> function, R def) throws E {
		return t == null ? def : function.apply(t);
	}

	/**
	 * Passes only non-null values to function.
	 */
	public static <E extends Exception, T> int safeApplyAsInt(T t,
		Excepts.ToIntFunction<E, ? super T> function, int def) throws E {
		return t == null ? def : function.applyAsInt(t);
	}

	/**
	 * Passes only non-null values to function.
	 */
	public static <E extends Exception, T, R> R safeApplyGet(T t,
		Excepts.Function<E, ? super T, ? extends R> function, Excepts.Supplier<E, R> supplier)
		throws E {
		return t == null ? supplier.get() : function.apply(t);
	}

	/**
	 * Passes only non-null values to function.
	 */
	public static <E extends Exception, T> int safeApplyGetAsInt(T t,
		Excepts.ToIntFunction<E, ? super T> function, Excepts.IntSupplier<E> supplier) throws E {
		return t == null ? supplier.getAsInt() : function.applyAsInt(t);
	}

	/**
	 * Passes only non-null values to consumer. Returns true if consumed.
	 */
	public static <E extends Exception, T> boolean safeAccept(T t,
		Excepts.Consumer<E, ? super T> consumer) throws E {
		if (t == null) return false;
		consumer.accept(t);
		return true;
	}

	/**
	 * Passes only non-null values to consumer. Returns true if consumed.
	 */
	public static <E extends Exception, T> boolean safeAccept(T t,
		Excepts.Predicate<E, ? super T> predicate, Excepts.Consumer<E, ? super T> consumer)
		throws E {
		if (t == null || !predicate.test(t)) return false;
		consumer.accept(t);
		return true;
	}

	/**
	 * Execute the function until no change, or the maximum number of recursions is met.
	 */
	public static <T> T recurse(T t, Functions.Function<? super T, ? extends T> fn) {
		return recurse(t, fn, MAX_RECURSIONS_DEF);
	}

	/**
	 * Execute the function recursively until no change, or the max number of recursions is met.
	 */
	public static <T> T recurse(T t, Functions.Function<? super T, ? extends T> fn, int max) {
		while (max-- > 0) {
			T last = t;
			t = fn.apply(t);
			if (Objects.equals(t, last)) break;
		}
		return t;
	}

	/**
	 * Returns true if the predicate matches any of the given values.
	 */
	@SafeVarargs
	public static <E extends Exception, T> boolean any(Excepts.Predicate<E, ? super T> predicate,
		T... ts) throws E {
		return any(predicate, Arrays.asList(ts));
	}

	/**
	 * Returns true if the predicate matches any of the given values.
	 */
	public static <E extends Exception, T> boolean any(Excepts.Predicate<E, ? super T> predicate,
		Iterable<T> ts) throws E {
		for (var t : ts)
			if (predicate.test(t)) return true;
		return false;
	}

	/**
	 * Returns true if the predicate matches all of the given values.
	 */
	@SafeVarargs
	public static <E extends Exception, T> boolean all(Excepts.Predicate<E, ? super T> predicate,
		T... ts) throws E {
		return all(predicate, Arrays.asList(ts));
	}

	/**
	 * Returns true if the predicate matches all of the given values.
	 */
	public static <E extends Exception, T> boolean all(Excepts.Predicate<E, ? super T> predicate,
		Iterable<T> ts) throws E {
		for (var t : ts)
			if (!predicate.test(t)) return false;
		return true;
	}

	/**
	 * Invokes the supplier and returns the result. Errors will be thrown, but exceptions will be
	 * suppressed, and an error value returned. Interrupted exceptions will re-interrupt the thread.
	 */
	private static <T> T getIt(Throws.Supplier<? extends T> supplier, T errorVal) {
		try {
			return supplier.get();
		} catch (Error e) {
			throw e;
		} catch (RuntimeInterruptedException | InterruptedException e) {
			Concurrent.interrupt(); // reset interrupt since we ignore the exception
		} catch (Throwable _) {
			// ignored
		}
		return errorVal;
	}
}
