package ceri.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionSupplier;
import ceri.common.function.RuntimeCloseable;

/**
 * Utilities for AutoCloseable resources.
 */
public class CloseableUtil {

	private CloseableUtil() {}

	/**
	 * Reference to a closeable resource; use as a method return to avoid resource leak warnings.
	 */
	public static class Ref<T extends AutoCloseable> {
		public final T ref;

		private Ref(T ref) {
			this.ref = ref;
		}
	}

	/**
	 * Return a closeable reference, to avoid resource leak warnings.
	 */
	public static <T extends AutoCloseable> Ref<T> ref(T closeable) {
		return new Ref<>(closeable);
	}

	/**
	 * Executes an action on the given closable resource; closes the instance and re-throws if an
	 * exception is thrown.
	 */
	public static <E extends Exception, T extends AutoCloseable> T execOrClose(T t,
		ExceptionConsumer<E, T> execFn) throws E {
		try {
			if (t != null) execFn.accept(t);
			return t;
		} catch (RuntimeException e) {
			close(t);
			throw e;
		} catch (Exception e) {
			close(t);
			throw BasicUtil.<E>uncheckedCast(e);
		}
	}

	/**
	 * Closes a closeable resource. Notifies the error consumer if an exception occurs, and returns
	 * false.
	 */
	public static boolean close(Consumer<Exception> errorConsumer, AutoCloseable closeable) {
		if (closeable == null) return true;
		try {
			closeable.close();
			return true;
		} catch (RuntimeInterruptedException | InterruptedException e) {
			Thread.currentThread().interrupt(); // reset interrupt since we ignore the exception
			if (errorConsumer != null) errorConsumer.accept(e);
		} catch (Exception e) {
			if (errorConsumer != null) errorConsumer.accept(e);
		}
		return false;
	}

	/**
	 * Closes resources in reverse order. Notifies the error consumer if any exceptions occur, and
	 * returns false.
	 */
	public static boolean closeAll(Consumer<Exception> errorConsumer, AutoCloseable... closeables) {
		return closeAll(errorConsumer, Arrays.asList(closeables));
	}

	/**
	 * Closes resources in reverse order. Notifies the error consumer if any exceptions occur, and
	 * returns false.
	 */
	@SuppressWarnings("resource")
	public static boolean closeAll(Consumer<Exception> errorConsumer,
		List<? extends AutoCloseable> closeables) {
		boolean success = true;
		for (int i = closeables.size() - 1; i >= 0; i--)
			if (!close(errorConsumer, closeables.get(i))) success = false;
		return success;
	}

	/**
	 * Closes a closeable resource. Returns false if an exception occurred.
	 */
	public static boolean close(AutoCloseable closeable) {
		return close(null, closeable);
	}

	/**
	 * Closes resources in reverse order. Returns false if an exception occurred.
	 */
	public static boolean closeAll(AutoCloseable... closeables) {
		return closeAll(Arrays.asList(closeables));
	}

	/**
	 * Closes resources in reverse order. Returns false if an exception occurred.
	 */
	public static boolean closeAll(List<? extends AutoCloseable> closeables) {
		return closeAll(null, closeables);
	}

	/**
	 * Convert multiple resources to a single closeable.
	 */
	public static RuntimeCloseable closeable(Consumer<Exception> errorConsumer,
		AutoCloseable... closeables) {
		return () -> closeAll(errorConsumer, closeables);
	}

	/**
	 * Convert multiple resources to a single closeable.
	 */
	public static RuntimeCloseable closeable(AutoCloseable... closeables) {
		return closeable(null, closeables);
	}

	/**
	 * Constructs an immutable list of closeable instances from constructors. If any exception
	 * occurs the already created instances will be closed.
	 */
	@SafeVarargs
	public static <E extends Exception, T extends AutoCloseable> Enclosed<RuntimeException, List<T>>
		create(ExceptionSupplier<E, T>... constructors) throws E {
		return create(Arrays.asList(constructors));
	}

	/**
	 * Constructs an immutable list of closeable instances from constructors. If any exception
	 * occurs the already created instances will be closed.
	 */
	@SuppressWarnings("resource")
	public static <E extends Exception, T extends AutoCloseable> Enclosed<RuntimeException, List<T>>
		create(Iterable<ExceptionSupplier<E, T>> constructors) throws E {
		List<T> results = new ArrayList<>();
		try {
			for (var constructor : constructors)
				results.add(constructor.get());
			return Enclosed.ofAll(Collections.unmodifiableList(results));
		} catch (Exception e) {
			CloseableUtil.closeAll(results);
			throw e;
		}
	}

	/**
	 * Constructs an immutable list of closeable instances from each input object and the
	 * constructor. If any exception occurs the already created instances will be closed.
	 */
	@SafeVarargs
	public static <E extends Exception, T, R extends AutoCloseable>
		Enclosed<RuntimeException, List<R>>
		create(ExceptionFunction<E, T, R> constructor, T... inputs) throws E {
		return create(constructor, Arrays.asList(inputs));
	}

	/**
	 * Constructs an immutable list of closeable instances from each input object and the
	 * constructor. If any exception occurs the already created instances will be closed.
	 */
	public static <E extends Exception, T, R extends AutoCloseable>
		Enclosed<RuntimeException, List<R>>
		create(ExceptionFunction<E, T, R> constructor, Iterable<T> inputs) throws E {
		return Enclosed.ofAll(createList(constructor, inputs));
	}

	/**
	 * Constructs an immutable list of closeable instances from each input object and the
	 * constructor. If any exception occurs the already created instances will be closed.
	 */
	@SafeVarargs
	public static <E extends Exception, T, R extends AutoCloseable> List<R>
		createList(ExceptionFunction<E, T, R> constructor, T... inputs) throws E {
		return createList(constructor, Arrays.asList(inputs));
	}

	/**
	 * Constructs an immutable list of closeable instances from each input object and the
	 * constructor. If any exception occurs the already created instances will be closed.
	 */
	@SuppressWarnings("resource")
	public static <E extends Exception, T, R extends AutoCloseable> List<R>
		createList(ExceptionFunction<E, T, R> constructor, Iterable<T> inputs) throws E {
		List<R> results = new ArrayList<>();
		try {
			for (T input : inputs)
				results.add(constructor.apply(input));
			return Collections.unmodifiableList(results);
		} catch (Exception e) {
			CloseableUtil.closeAll(results);
			throw e;
		}
	}

	/**
	 * Constructs an array of closeable instances from each input object and the constructor. If any
	 * exception occurs the already created instances will be closed.
	 */
	@SafeVarargs
	public static <E extends Exception, T, R extends AutoCloseable> R[] createArray(
		IntFunction<R[]> arrayFn, ExceptionFunction<E, T, R> constructor, T... inputs) throws E {
		return createArray(arrayFn, constructor, Arrays.asList(inputs));
	}

	/**
	 * Constructs an array of closeable instances from each input object and the constructor. If any
	 * exception occurs the already created instances will be closed.
	 */
	public static <E extends Exception, T, R extends AutoCloseable> R[] createArray(
		IntFunction<R[]> arrayFn, ExceptionFunction<E, T, R> constructor, Collection<T> inputs)
		throws E {
		R[] results = arrayFn.apply(inputs.size());
		int i = 0;
		try {
			for (T input : inputs)
				results[i++] = constructor.apply(input);
			return results;
		} catch (Exception e) {
			CloseableUtil.closeAll(results);
			throw e;
		}
	}

}
