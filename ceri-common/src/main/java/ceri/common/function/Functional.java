package ceri.common.function;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import ceri.common.concurrent.Concurrent;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.util.Holder;

/**
 * Function utilities.
 */
public class Functional {
	private static final int MAX_RECURSIONS_DEF = 20;

	private Functional() {}

	/**
	 * Function adapters.
	 */
	public static class Adapt {
		private Adapt() {}

		/**
		 * Adapts a consumer as a function with fixed response.
		 */
		public static <E extends Exception, T, R> Excepts.Function<E, T, R>
			acceptFunction(Excepts.Consumer<? extends E, T> consumer, R response) {
			return t -> {
				if (consumer != null) consumer.accept(t);
				return response;
			};
		}

		/**
		 * Adapts a runnable as a supplier with fixed response.
		 */
		public static <E extends Exception, R> Excepts.Supplier<E, R>
			runSupplier(Excepts.Runnable<? extends E> runnable, R response) {
			return () -> {
				if (runnable != null) runnable.run();
				return response;
			};
		}
	}

	/**
	 * Interface that accepts various functions, passing an instance to the function.
	 */
	public interface Access<T> {
		/**
		 * Applies the function.
		 */
		<E extends Exception, R> R apply(Excepts.Function<E, ? super T, R> function) throws E;

		/**
		 * Accepts the consumer.
		 */
		default <E extends Exception> void accept(Excepts.Consumer<E, ? super T> consumer)
			throws E {
			apply(Adapt.acceptFunction(consumer, null));
		}

		/**
		 * Create an implementation with a fixed instance.
		 */
		static <T> Access<T> of(T t) {
			// return f -> f.apply(t); // not allowed
			return new Access<>() {
				@Override
				public <E extends Exception, R> R apply(Excepts.Function<E, ? super T, R> function)
					throws E {
					return function.apply(t);
				}
			};
		}
	}

	/**
	 * Gets result from function and operand; returns null if function or operand is null.
	 */
	public static <E extends Exception, T, R> R
		apply(Excepts.Function<E, ? super T, ? extends R> function, T t) throws E {
		return apply(function, t, null);
	}

	/**
	 * Gets result from function and operand; returns default if function or operand is null.
	 */
	public static <E extends Exception, T, R> R
		apply(Excepts.Function<E, ? super T, ? extends R> function, T t, R def) throws E {
		return function == null || t == null ? def : function.apply(t);
	}

	/**
	 * Gets result from function and operand; returns supplier result if function or operand is
	 * null.
	 */
	public static <E extends Exception, T, R> R applyGet(
		Excepts.Function<E, ? super T, ? extends R> function, T t, Excepts.Supplier<E, R> supplier)
		throws E {
		return function == null || t == null ? get(supplier) : function.apply(t);
	}

	/**
	 * Gets result from function and operand; returns default if function or operand is null.
	 */
	public static <E extends Exception, T> int
		applyAsInt(Excepts.ToIntFunction<E, ? super T> function, T t, int def) throws E {
		return function == null || t == null ? def : function.applyAsInt(t);
	}

	/**
	 * Gets result from function and operand; returns default if function or operand is null.
	 */
	public static <E extends Exception, T> long
		applyAsLong(Excepts.ToLongFunction<E, ? super T> function, T t, long def) throws E {
		return function == null || t == null ? def : function.applyAsLong(t);
	}

	/**
	 * Gets result from function and operand; returns default if function or operand is null.
	 */
	public static <E extends Exception, T> double
		applyAsDouble(Excepts.ToDoubleFunction<E, ? super T> function, T t, double def) throws E {
		return function == null || t == null ? def : function.applyAsDouble(t);
	}

	/**
	 * Calls consumer with operand and returns true; returns false if consumer or operand is null.
	 */
	public static <E extends Exception, T> boolean accept(Excepts.Consumer<E, ? super T> consumer,
		T t) throws E {
		if (consumer == null || t == null) return false;
		consumer.accept(t);
		return true;
	}

	/**
	 * Calls consumer with operand and returns true; returns false if consumer is null.
	 */
	public static <E extends Exception> boolean acceptInt(Excepts.IntConsumer<E> consumer, int i)
		throws E {
		if (consumer == null) return false;
		consumer.accept(i);
		return true;
	}

	/**
	 * Calls consumer with operand and returns true; returns false if consumer is null.
	 */
	public static <E extends Exception> boolean acceptLong(Excepts.LongConsumer<E> consumer, long l)
		throws E {
		if (consumer == null) return false;
		consumer.accept(l);
		return true;
	}

	/**
	 * Calls consumer with operand and returns true; returns false if consumer is null.
	 */
	public static <E extends Exception> boolean acceptDouble(Excepts.DoubleConsumer<E> consumer,
		double d) throws E {
		if (consumer == null) return false;
		consumer.accept(d);
		return true;
	}

	/**
	 * Gets result from supplier; returns null if supplier is null.
	 */
	public static <E extends Exception, T> T get(Excepts.Supplier<E, T> supplier) throws E {
		return get(supplier, null);
	}

	/**
	 * Gets result from supplier; returns default if supplier is null.
	 */
	public static <E extends Exception, T> T get(Excepts.Supplier<E, T> supplier, T def) throws E {
		return supplier == null ? def : supplier.get();
	}

	/**
	 * Gets result from supplier; returns default if supplier is null.
	 */
	public static <E extends Exception> int getAsInt(Excepts.IntSupplier<E> supplier, int def)
		throws E {
		return supplier == null ? def : supplier.getAsInt();
	}

	/**
	 * Gets result from supplier; returns default if supplier is null.
	 */
	public static <E extends Exception> long getAsLong(Excepts.LongSupplier<E> supplier, long def)
		throws E {
		return supplier == null ? def : supplier.getAsLong();
	}

	/**
	 * Gets result from supplier; returns default if supplier is null.
	 */
	public static <E extends Exception> double getAsDouble(Excepts.DoubleSupplier<E> supplier,
		double def) throws E {
		return supplier == null ? def : supplier.getAsDouble();
	}

	/**
	 * Executes the runnable and returns true; returns false if the runnable is null.
	 */
	public static <E extends Exception> boolean run(Excepts.Runnable<E> runnable) throws E {
		if (runnable == null) return false;
		runnable.run();
		return true;
	}

	/**
	 * Invokes the supplier and returns the result, which may be null. Errors will be thrown, but
	 * exceptions will be suppressed, and null returned. Interrupted exceptions will re-interrupt
	 * the thread.
	 */
	public static <T> T muteGet(Throws.Supplier<T> supplier) {
		return muteGet(supplier, null);
	}

	/**
	 * Invokes the supplier and returns the result. Errors will be thrown, but exceptions will be
	 * suppressed, and the error value returned. Interrupted exceptions will re-interrupt the
	 * thread.
	 */
	public static <T> T muteGet(Throws.Supplier<? extends T> supplier, T errVal) {
		return muteGetIt(supplier, errVal);
	}

	/**
	 * Invokes the supplier and returns the result. Errors will be thrown, but exceptions will be
	 * suppressed, and the error value returned. Interrupted exceptions will re-interrupt the
	 * thread.
	 */
	public static int muteGetInt(Throws.IntSupplier supplier, int errVal) {
		return muteGetIt(supplier == null ? null : supplier::getAsInt, errVal);
	}

	/**
	 * Invokes the supplier and returns the result. Errors will be thrown, but exceptions will be
	 * suppressed, and the error value returned. Interrupted exceptions will re-interrupt the
	 * thread.
	 */
	public static long muteGetLong(Throws.LongSupplier supplier, long errVal) {
		return muteGetIt(supplier == null ? null : supplier::getAsLong, errVal);
	}

	/**
	 * Invokes the supplier and returns the result. Errors will be thrown, but exceptions will be
	 * suppressed, and the error value returned. Interrupted exceptions will re-interrupt the
	 * thread.
	 */
	public static double muteGetDouble(Throws.DoubleSupplier supplier, double errVal) {
		return muteGetIt(supplier == null ? null : supplier::getAsDouble, errVal);
	}

	/**
	 * Invokes the runnable and returns true. Errors will be thrown, but exceptions will be
	 * suppressed, and false returned. Interrupted exceptions will re-interrupt the thread.
	 */
	public static boolean muteRun(Throws.Runnable runnable) {
		return muteGetIt(runnable == null ? null : () -> {
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
	 * Executes the function until no change, or the maximum number of recursions is met.
	 */
	public static <E extends Exception, T> T recurse(Excepts.Function<E, ? super T, ? extends T> fn,
		T t) throws E {
		return recurse(fn, t, MAX_RECURSIONS_DEF);
	}

	/**
	 * Executes the function recursively until no change, or the max number of recursions is met.
	 */
	public static <E extends Exception, T> T recurse(Excepts.Function<E, ? super T, ? extends T> fn,
		T t, int max) throws E {
		while (max-- > 0) {
			T last = t;
			t = fn.apply(t);
			if (Objects.equals(t, last)) break;
		}
		return t;
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

	// support

	private static <T> T muteGetIt(Throws.Supplier<? extends T> supplier, T errVal) {
		try {
			if (supplier != null) return supplier.get();
		} catch (Error e) {
			throw e;
		} catch (RuntimeInterruptedException | InterruptedException e) {
			Concurrent.interrupt(); // reset interrupt since we ignore the exception
		} catch (Throwable _) {
			// ignored
		}
		return errVal;
	}
}
