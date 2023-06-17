package ceri.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionSupplier;

/**
 * Utilities for AutoCloseable resources.
 */
public class CloseableUtil {

	private CloseableUtil() {}

	/**
	 * Reference to a closeable resource, to help prevent resource leak warnings.
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
	 * Executes an action on the given closable instance; closes the instance if an exception is
	 * thrown.
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
	 * Closes a closeable stream. Returns false if this resulted in an error.
	 */
	public static boolean close(AutoCloseable closeable) {
		if (closeable == null) return false;
		try {
			closeable.close();
			return true;
		} catch (RuntimeInterruptedException | InterruptedException e) {
			Thread.currentThread().interrupt(); // reset interrupt since we ignore the exception
			return false;
		} catch (Exception e) {
			return false;
		}
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
	@SuppressWarnings("resource")
	public static boolean closeAll(List<? extends AutoCloseable> closeables) {
		boolean success = true;
		for (int i = closeables.size() - 1; i >= 0; i--)
			if (!close(closeables.get(i))) success = false;
		return success;
	}

	/**
	 * Convert multiple resources to a single closeable.
	 */
	public static AutoCloseable closeable(AutoCloseable... closeables) {
		return () -> closeAll(closeables);
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
