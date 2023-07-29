package ceri.common.function;

import static ceri.common.validation.ValidationUtil.validateMin;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.util.BasicUtil;
import ceri.common.util.Counter;

/**
 * Function utilities.
 */
public class FunctionUtil {
	private static final int MAX_RECURSIONS_DEF = 20;
	private static final Predicate<Object> TRUE_PREDICATE = t -> true;
	private static final Consumer<Object> NULL_CONSUMER = t -> {};
	private static final String ANON_LAMBDA_LABEL = "$$Lambda$";
	private static final String LAMBDA_NAME_DEF = "[lambda]";

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
	public static <T> T getSilently(ExceptionSupplier<?, T> supplier, T errorVal) {
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
	 * Provide sequential access to the given values, repeating the last entry. At least one value
	 * must be supplied.
	 */
	@SafeVarargs
	public static <T> Supplier<T> sequentialSupplier(T... ts) {
		validateMin(ts.length, 1);
		Counter counter = Counter.of();
		return () -> {
			int n = counter.intCount();
			if (n < ts.length - 1) counter.inc();
			return ts[n];
		};
	}

	/**
	 * Passes only non-null values to function.
	 */
	public static <E extends Exception, T, R> R safeApply(T t, ExceptionFunction<E, T, R> function)
		throws E {
		return safeApply(t, function, null);
	}

	/**
	 * Passes only non-null values to function.
	 */
	public static <E extends Exception, T, R> R safeApply(T t, ExceptionFunction<E, T, R> function,
		R def) throws E {
		return t == null ? def : function.apply(t);
	}

	/**
	 * Passes only non-null values to function.
	 */
	public static <E extends Exception, T> int safeApplyAsInt(T t,
		ExceptionToIntFunction<E, T> function, int def) throws E {
		return t == null ? def : function.applyAsInt(t);
	}

	/**
	 * Passes only non-null values to function.
	 */
	public static <E extends Exception, T, R> R safeApplyGet(T t,
		ExceptionFunction<E, T, R> function, ExceptionSupplier<E, R> supplier) throws E {
		return t == null ? supplier.get() : function.apply(t);
	}

	/**
	 * Passes only non-null values to function.
	 */
	public static <E extends Exception, T> int safeApplyGetAsInt(T t,
		ExceptionToIntFunction<E, T> function, ExceptionIntSupplier<E> supplier) throws E {
		return t == null ? supplier.getAsInt() : function.applyAsInt(t);
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
	public static <E extends Exception, T> boolean safeAccept(T t,
		ExceptionPredicate<E, T> predicate, ExceptionConsumer<E, T> consumer) throws E {
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
	 * Execute the function recursively until no change, or the max number of recursions is met.
	 */
	public static <T> T recurse(T t, Function<T, T> fn, int max) {
		while (max-- > 0) {
			T last = t;
			t = fn.apply(t);
			if (Objects.equals(t, last)) break;
		}
		return t;
	}

	/**
	 * Executes for-each, allowing an exception of given type to be thrown.
	 */
	public static <E extends Exception, T> void forEach(Iterable<T> iter,
		ExceptionConsumer<E, ? super T> consumer) throws E {
		for (var t : iter)
			consumer.accept(t);
	}

	/**
	 * Executes for-each, allowing exception of given type to be thrown.
	 */
	public static <E extends Exception, T> void forEach(Stream<T> stream,
		ExceptionConsumer<E, ? super T> consumer) throws E {
		for (var i = stream.iterator(); i.hasNext();)
			consumer.accept(i.next());
	}

	/**
	 * Executes for-each, allowing exception of given type to be thrown.
	 */
	public static <E extends Exception> void forEach(IntStream stream,
		ExceptionIntConsumer<E> consumer) throws E {
		for (var i = stream.iterator(); i.hasNext();)
			consumer.accept(i.nextInt());
	}

	/**
	 * Executes for-each, allowing exception of given type to be thrown.
	 */
	public static <E extends Exception, K, V> void forEach(Map<K, V> map,
		ExceptionBiConsumer<E, ? super K, ? super V> consumer) throws E {
		for (var entry : map.entrySet())
			consumer.accept(entry.getKey(), entry.getValue());
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
	public static <T, U> Predicate<T> testing(Function<? super T, ? extends U> extractor,
		Predicate<? super U> predicate) {
		return t -> predicate.test(extractor.apply(t));
	}

	/**
	 * Provides an predicate from an int field accessor and int predicate.
	 */
	public static <T> Predicate<T> testingInt(ToIntFunction<? super T> extractor,
		IntPredicate predicate) {
		return t -> predicate.test(extractor.applyAsInt(t));
	}

	/**
	 * Checks if the given object is an anonymous lamdba function.
	 */
	public static boolean isAnonymousLambda(Object obj) {
		if (obj == null) return false;
		String s = obj.toString();
		return s != null && s.contains(ANON_LAMBDA_LABEL);
	}

	/**
	 * Returns "[lambda]" if the given object is an anonymous lambda, otherwise toString.
	 */
	public static String lambdaName(Object obj) {
		return lambdaName(obj, LAMBDA_NAME_DEF);
	}

	/**
	 * Returns given name if the given object is an anonymous lambda, otherwise toString.
	 */
	public static String lambdaName(Object obj, String anonNameDef) {
		String s = String.valueOf(obj);
		return s.contains(ANON_LAMBDA_LABEL) ? anonNameDef : s;
	}

	/**
	 * Overrides predicate toString() with given name.
	 */
	public static <T> Predicate<T> named(Predicate<T> predicate, String name) {
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
	 * Overrides predicate toString() with given name.
	 */
	public static IntPredicate namedInt(IntPredicate predicate, String name) {
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

	/**
	 * Converts a runnable to a function that ignores input and returns true.
	 */
	public static <E extends Exception, T> ExceptionFunction<E, T, Boolean>
		asFunction(ExceptionRunnable<E> runnable) {
		return t -> {
			runnable.run();
			return Boolean.TRUE;
		};
	}

	/**
	 * Converts a supplier to a function that ignores input.
	 */
	public static <E extends Exception, T> ExceptionFunction<E, ?, T>
		asFunction(ExceptionSupplier<E, T> supplier) {
		return t -> supplier.get();
	}

	/**
	 * Converts a supplier to a function that ignores input.
	 */
	public static <E extends Exception, T> ExceptionToIntFunction<E, T>
		asToIntFunction(ExceptionIntSupplier<E> supplier) {
		return t -> supplier.getAsInt();
	}

	/**
	 * Converts a consumer to a function that returns true.
	 */
	public static <E extends Exception, T> ExceptionFunction<E, T, Boolean>
		asFunction(ExceptionConsumer<E, T> consumer) {
		return t -> {
			consumer.accept(t);
			return Boolean.TRUE;
		};
	}

	/**
	 * Converts a consumer to a function that returns true.
	 */
	public static <E extends Exception> ExceptionIntFunction<E, Boolean>
		asIntFunction(ExceptionIntConsumer<E> consumer) {
		return i -> {
			consumer.accept(i);
			return Boolean.TRUE;
		};
	}

	/**
	 * Converts a bi-consumer to a bi-function that returns true.
	 */
	public static <E extends Exception, T, U> ExceptionBiFunction<E, T, U, Boolean>
		asBiFunction(ExceptionBiConsumer<E, T, U> consumer) {
		return (t, u) -> {
			consumer.accept(t, u);
			return Boolean.TRUE;
		};
	}

	/**
	 * Converts a runnable to a supplier that returns the given value.
	 */
	public static <E extends Exception, T> ExceptionSupplier<E, T>
		asSupplier(ExceptionRunnable<E> runnable, T t) {
		return () -> {
			runnable.run();
			return t;
		};
	}

	/**
	 * Converts a runnable to a supplier that returns null.
	 */
	public static <E extends Exception, T> ExceptionSupplier<E, T>
		asSupplier(ExceptionRunnable<E> runnable) {
		return () -> {
			runnable.run();
			return null;
		};
	}

	/**
	 * Invokes the runnable and returns true. If an exception is thrown, it will be suppressed, and
	 * false is returned. Interrupted exceptions will re-interrupt the thread.
	 */
	private static boolean runIt(ExceptionRunnable<Exception> runnable) {
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
	private static <T> T getIt(ExceptionSupplier<Exception, T> supplier, T errorVal) {
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
