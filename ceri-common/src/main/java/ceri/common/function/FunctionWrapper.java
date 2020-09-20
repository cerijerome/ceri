package ceri.common.function;

import static ceri.common.function.FunctionUtil.asBiFunction;
import static ceri.common.function.FunctionUtil.asFunction;
import static ceri.common.function.FunctionUtil.asIntFunction;
import static ceri.common.function.FunctionUtil.asToIntFunction;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import ceri.common.util.BasicUtil;

/**
 * "Exception Wormhole" or "Exception Re-Gifting" (anti-)pattern.
 * <p/>
 * This helper adapts function types that throw checked exceptions, so that they can be used in code
 * that requires standard function types. Wrap adapts the function so that checked exceptions are
 * wrapped as runtime exceptions. Unwrap should be used to surround the executed code, as it
 * captures the wrapped exception and throw the original typed checked exception. The same wrapper
 * instance must be used to wrap then unwrap.
 */
public class FunctionWrapper<E extends Exception> {

	public static <E extends Exception> FunctionWrapper<E> create() {
		return new FunctionWrapper<>();
	}

	private FunctionWrapper() {}

	// Methods to adapt runnables/suppliers/consumers/functions to throw wrapped exceptions

	public Runnable wrap(ExceptionRunnable<E> runnable) {
		return () -> wrap(asFunction(runnable), null);
	}

	public <T> Supplier<T> wrap(ExceptionSupplier<E, T> supplier) {
		return () -> wrap(asFunction(supplier), null);
	}

	public IntSupplier wrap(ExceptionIntSupplier<E> supplier) {
		return () -> wrap(asToIntFunction(supplier), null);
	}

	public <T> Consumer<T> wrap(ExceptionConsumer<E, T> consumer) {
		return t -> wrap(asFunction(consumer), t);
	}

	public IntConsumer wrap(ExceptionIntConsumer<E> consumer) {
		return i -> wrap(asIntFunction(consumer), i);
	}

	public <T, U> BiConsumer<T, U> wrap(ExceptionBiConsumer<E, T, U> consumer) {
		return (t, u) -> wrap(asBiFunction(consumer), t, u);
	}

	public <T, R> Function<T, R> wrap(ExceptionFunction<E, T, R> function) {
		return t -> wrap(function, t);
	}

	public <R> IntFunction<R> wrap(ExceptionIntFunction<E, R> function) {
		return i -> wrap(function, i);
	}

	public <T> ToIntFunction<T> wrap(ExceptionToIntFunction<E, T> function) {
		return t -> wrap(function, t);
	}

	public IntUnaryOperator wrap(ExceptionIntUnaryOperator<E> function) {
		return i -> wrap(function, i);
	}

	public <T> Predicate<T> wrap(ExceptionPredicate<E, T> predicate) {
		return t -> wrap(predicate, t);
	}

	public IntPredicate wrap(ExceptionIntPredicate<E> predicate) {
		return t -> wrap(predicate, t);
	}

	// Methods to apply a function and wrap exceptions

	public <T, R> R wrap(ExceptionFunction<E, T, R> function, T t) {
		return wrapGet(() -> function.apply(t));
	}

	public <R> R wrap(ExceptionIntFunction<E, R> function, int i) {
		return wrapGet(() -> function.apply(i));
	}

	public <T> int wrap(ExceptionToIntFunction<E, T> function, T t) {
		return wrapGet(() -> function.applyAsInt(t));
	}

	public int wrap(ExceptionIntUnaryOperator<E> function, int i) {
		return wrapGet(() -> function.applyAsInt(i));
	}

	public <T, U, R> R wrap(ExceptionBiFunction<E, T, U, R> function, T t, U u) {
		return wrapGet(() -> function.apply(t, u));
	}

	public <T> boolean wrap(ExceptionPredicate<E, T> predicate, T t) {
		return wrapGet(() -> predicate.test(t));
	}

	public boolean wrap(ExceptionIntPredicate<? extends E> predicate, int i) {
		return wrapGet(() -> predicate.test(i));
	}

	// Methods to execute runnables/suppliers/consumers/functions and throw unwrapped exceptions

	public void unwrap(ExceptionRunnable<E> runnable) throws E {
		unwrapFunction(asFunction(runnable), null);
	}

	public <T> T unwrapSupplier(ExceptionSupplier<E, T> supplier) throws E {
		return unwrapFunction(asFunction(supplier), null);
	}

	public int unwrapIntSupplier(ExceptionIntSupplier<E> supplier) throws E {
		return unwrapToIntFunction(asToIntFunction(supplier), null);
	}

	public <T> void unwrapConsumer(ExceptionConsumer<E, T> consumer, T t) throws E {
		unwrapFunction(asFunction(consumer), t);
	}

	public void unwrapIntConsumer(ExceptionIntConsumer<E> consumer, int i) throws E {
		unwrapIntFunction(asIntFunction(consumer), i);
	}

	public <T, U> void unwrapBiConsumer(ExceptionBiConsumer<E, T, U> consumer, T t, U u) throws E {
		unwrapBiFunction(asBiFunction(consumer), t, u);
	}

	public <T, R> R unwrapFunction(ExceptionFunction<E, T, R> function, T t) throws E {
		return unwrapGet(() -> function.apply(t));
	}

	public <R> R unwrapIntFunction(ExceptionIntFunction<E, R> function, int i) throws E {
		return unwrapGet(() -> function.apply(i));
	}

	public <T> int unwrapToIntFunction(ExceptionToIntFunction<E, T> function, T t) throws E {
		return unwrapGet(() -> function.applyAsInt(t));
	}

	public int unwrapIntUnaryOperator(ExceptionIntUnaryOperator<E> function, int i) throws E {
		return unwrapGet(() -> function.applyAsInt(i));
	}

	public <T, U, R> R unwrapBiFunction(ExceptionBiFunction<E, T, U, R> function, T t, U u)
		throws E {
		return unwrapGet(() -> function.apply(t, u));
	}

	public <T> boolean unwrapPredicate(ExceptionPredicate<E, T> predicate, T t) throws E {
		return unwrapGet(() -> predicate.test(t));
	}

	public boolean unwrapIntPredicate(ExceptionIntPredicate<E> predicate, int i) throws E {
		return unwrapGet(() -> predicate.test(i));
	}

	// Methods to wrap/unwrap exceptions

	private <R> R wrapGet(ExceptionSupplier<? extends E, R> supplier) {
		try {
			return supplier.get();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new WrapperException(this, BasicUtil.uncheckedCast(e));
		}
	}

	private <R> R unwrapGet(ExceptionSupplier<? extends E, R> supplier) throws E {
		try {
			return supplier.get();
		} catch (WrapperException e) {
			if (this != e.wrapper)
				throw new IllegalStateException("Mis-matched agent: " + e.wrapper);
			throw BasicUtil.<E>uncheckedCast(e.getCause());
		}
	}

}
