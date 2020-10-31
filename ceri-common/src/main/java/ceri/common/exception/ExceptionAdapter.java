package ceri.common.exception;

import java.lang.reflect.Constructor;
import java.util.function.Function;
import ceri.common.function.ExceptionBooleanSupplier;
import ceri.common.function.ExceptionIntSupplier;
import ceri.common.function.ExceptionLongSupplier;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;
import ceri.common.reflect.ReflectUtil;

/**
 * Adapter that wraps non-assignable exceptions.
 */
public class ExceptionAdapter<E extends Exception> implements Function<Throwable, E> {
	public static ExceptionAdapter<Exception> NULL = of(Exception.class, Exception::new);
	public static ExceptionAdapter<RuntimeException> RUNTIME =
		of(RuntimeException.class, RuntimeException::new);
	public static ExceptionAdapter<IllegalArgumentException> ILLEGAL_ARGUMENT =
		of(IllegalArgumentException.class, IllegalArgumentException::new);
	private final Class<E> cls;
	private final Function<Throwable, E> fn;

	public static <E extends Exception> ExceptionAdapter<E> of(Class<E> cls) {
		Constructor<E> constructor = ReflectUtil.constructor(cls, Throwable.class);
		return of(cls, e -> ReflectUtil.create(constructor, e));
	}

	public static <E extends Exception> ExceptionAdapter<E> of(Class<E> cls,
		Function<Throwable, E> fn) {
		return new ExceptionAdapter<>(cls, fn);
	}

	private ExceptionAdapter(Class<E> cls, Function<Throwable, E> fn) {
		this.cls = cls;
		this.fn = fn;
	}

	@Override
	public E apply(Throwable t) {
		if (cls.isInstance(t)) return cls.cast(t);
		return fn.apply(t);
	}

	public void run(ExceptionRunnable<?> runnable) throws E {
		try {
			runnable.run();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw apply(e);
		}
	}

	public <T> T get(ExceptionSupplier<?, T> supplier) throws E {
		try {
			return supplier.get();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw apply(e);
		}
	}

	public boolean getBoolean(ExceptionBooleanSupplier<?> supplier) throws E {
		try {
			return supplier.getAsBoolean();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw apply(e);
		}
	}

	public int getInt(ExceptionIntSupplier<?> supplier) throws E {
		try {
			return supplier.getAsInt();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw apply(e);
		}
	}

	public long getLong(ExceptionLongSupplier<?> supplier) throws E {
		try {
			return supplier.getAsLong();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw apply(e);
		}
	}

}
