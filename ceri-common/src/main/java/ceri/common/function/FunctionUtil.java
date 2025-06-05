package ceri.common.function;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.util.BasicUtil;
import ceri.common.util.Holder;

/**
 * Function utilities.
 */
public class FunctionUtil {
	private static final int MAX_RECURSIONS_DEF = 20;
	private static final Predicate<Object> TRUE_PREDICATE = _ -> true;
	public static final Runnable NULL_RUNNABLE = () -> {};
	private static final Consumer<Object> NULL_CONSUMER = _ -> {};

	private FunctionUtil() {}

	/**
	 * Provides a no-op consumer.
	 */
	public static <T> Consumer<T> nullConsumer() {
		return BasicUtil.uncheckedCast(NULL_CONSUMER);
	}

	/**
	 * Provides a predicate that is always true.
	 */
	public static <T> Predicate<T> truePredicate() {
		return BasicUtil.uncheckedCast(TRUE_PREDICATE);
	}

	/**
	 * Invokes the supplier and returns the supplied value, which may be null. If an exception
	 * occurs, it is suppressed, and null is returned. Interrupted exceptions will re-interrupt the
	 * current thread.
	 */
	public static <T> T getSilently(ExceptionSupplier<?, T> supplier) {
		return getSilently(supplier, null);
	}

	/**
	 * Invokes the supplier and returns the supplied value, which may be null. If an exception
	 * occurs, it is suppressed, and the error value is returned. Interrupted exceptions will
	 * re-interrupt the current thread.
	 */
	public static <T> T getSilently(ExceptionSupplier<?, ? extends T> supplier, T errorVal) {
		return getIt(supplier::get, errorVal);
	}

	/**
	 * Invokes the runnable and returns true. If an exception is thrown, it is suppressed, and false
	 * is returned. Interrupted exceptions will re-interrupt the thread.
	 */
	public static boolean runSilently(ExceptionRunnable<?> runnable) {
		return runIt(runnable::run);
	}

	/**
	 * Provide sequential access to the given values, repeating the last entry. If no values are
	 * given, the supplier will always return null.
	 */
	@SafeVarargs
	public static <T> Supplier<T> sequentialSupplier(T... ts) {
		return sequentialSupplier(Arrays.asList(ts));
	}

	/**
	 * Provide sequential access to the given values, repeating the last entry. If no values are
	 * given, the supplier will always return null.
	 */
	public static <T> Supplier<T> sequentialSupplier(Iterable<T> ts) {
		var i = ts.iterator();
		var holder = Holder.<T>mutable();
		return () -> {
			if (i.hasNext()) holder.set(i.next());
			return holder.value();
		};
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
		ExceptionFunction<E, ? super T, R> function) throws E {
		return safeApply(t, function, null);
	}

	/**
	 * Passes only non-null values to function.
	 */
	public static <E extends Exception, T, R> R safeApply(T t,
		ExceptionFunction<E, ? super T, ? extends R> function, R def) throws E {
		return t == null ? def : function.apply(t);
	}

	/**
	 * Passes only non-null values to function.
	 */
	public static <E extends Exception, T> int safeApplyAsInt(T t,
		ExceptionToIntFunction<E, ? super T> function, int def) throws E {
		return t == null ? def : function.applyAsInt(t);
	}

	/**
	 * Passes only non-null values to function.
	 */
	public static <E extends Exception, T, R> R safeApplyGet(T t,
		ExceptionFunction<E, ? super T, ? extends R> function, ExceptionSupplier<E, R> supplier)
		throws E {
		return t == null ? supplier.get() : function.apply(t);
	}

	/**
	 * Passes only non-null values to function.
	 */
	public static <E extends Exception, T> int safeApplyGetAsInt(T t,
		ExceptionToIntFunction<E, ? super T> function, ExceptionIntSupplier<E> supplier) throws E {
		return t == null ? supplier.getAsInt() : function.applyAsInt(t);
	}

	/**
	 * Passes only non-null values to consumer. Returns true if consumed.
	 */
	public static <E extends Exception, T> boolean safeAccept(T t,
		ExceptionConsumer<E, ? super T> consumer) throws E {
		if (t == null) return false;
		consumer.accept(t);
		return true;
	}

	/**
	 * Passes only non-null values to consumer. Returns true if consumed.
	 */
	public static <E extends Exception, T> boolean safeAccept(T t,
		ExceptionPredicate<E, ? super T> predicate, ExceptionConsumer<E, ? super T> consumer)
		throws E {
		if (t == null || !predicate.test(t)) return false;
		consumer.accept(t);
		return true;
	}

	/**
	 * Execute the function until no change, or the maximum number of recursions is met.
	 */
	public static <T> T recurse(T t, Function<? super T, ? extends T> fn) {
		return recurse(t, fn, MAX_RECURSIONS_DEF);
	}

	/**
	 * Execute the function recursively until no change, or the max number of recursions is met.
	 */
	public static <T> T recurse(T t, Function<? super T, ? extends T> fn, int max) {
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
	public static <E extends Exception, T> boolean any(ExceptionPredicate<E, ? super T> predicate,
		T... ts) throws E {
		return any(predicate, Arrays.asList(ts));
	}

	/**
	 * Returns true if the predicate matches any of the given values.
	 */
	public static <E extends Exception, T> boolean any(ExceptionPredicate<E, ? super T> predicate,
		Iterable<T> ts) throws E {
		for (var t : ts)
			if (predicate.test(t)) return true;
		return false;
	}

	/**
	 * Returns true if the predicate matches all of the given values.
	 */
	@SafeVarargs
	public static <E extends Exception, T> boolean all(ExceptionPredicate<E, ? super T> predicate,
		T... ts) throws E {
		return all(predicate, Arrays.asList(ts));
	}

	/**
	 * Returns true if the predicate matches all of the given values.
	 */
	public static <E extends Exception, T> boolean all(ExceptionPredicate<E, ? super T> predicate,
		Iterable<T> ts) throws E {
		for (var t : ts)
			if (!predicate.test(t)) return false;
		return true;
	}

	/**
	 * Combines nullable predicates with logical AND.
	 */
	public static <T> Predicate<T> and(Predicate<T> lhs, Predicate<T> rhs) {
		return lhs == null ? rhs : rhs == null ? lhs : lhs.and(rhs);
	}

	/**
	 * Combines nullable predicates with logical OR.
	 */
	public static <T> Predicate<T> or(Predicate<T> lhs, Predicate<T> rhs) {
		return lhs == null ? rhs : rhs == null ? lhs : lhs.or(rhs);
	}

	/**
	 * Provides a predicate from a field accessor and predicate for the field type.
	 */
	public static <T, U> Predicate<T> testing(Function<T, U> extractor,
		Predicate<? super U> predicate) {
		return t -> predicate.test(extractor.apply(t));
	}

	/**
	 * Provides an predicate from an int field accessor and int predicate.
	 */
	public static <T> Predicate<T> testingInt(ToIntFunction<T> extractor, IntPredicate predicate) {
		return t -> predicate.test(extractor.applyAsInt(t));
	}

	/**
	 * Invokes the runnable and returns true. If an exception is thrown, it will be suppressed, and
	 * false is returned. Interrupted exceptions will re-interrupt the thread.
	 */
	private static boolean runIt(ExceptionRunnable<?> runnable) {
		return getIt(() -> {
			runnable.run();
			return true;
		}, false);
	}

	/**
	 * Invokes the supplier and returns the result. If an exception is thrown, it will be
	 * suppressed, and the error value returned. Interrupted exceptions will re-interrupt the
	 * thread.
	 */
	private static <T> T getIt(ExceptionSupplier<Exception, ? extends T> supplier, T errorVal) {
		try {
			return supplier.get();
		} catch (RuntimeInterruptedException | InterruptedException e) {
			ConcurrentUtil.interrupt(); // reset interrupt since we ignore the exception
		} catch (Exception e) {
			// ignored
		}
		return errorVal;
	}
}
