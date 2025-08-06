package ceri.common.exception;

import java.io.IOException;
import java.util.function.Function;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.function.Functions;
import ceri.common.function.Throws;
import ceri.common.io.IoExceptions;
import ceri.common.reflect.Reflect;

/**
 * Adapter that wraps non-assignable exceptions.
 */
public class ExceptionAdapter<E extends Exception> implements Functions.Function<Throwable, E> {
	/** Makes no change to exceptions. */
	public static ExceptionAdapter<Exception> none = of(Exception.class, Exception::new);
	/** Wraps unexpected exceptions in illegal state exception. */
	public static ExceptionAdapter<RuntimeException> shouldNotThrow =
		of(RuntimeException.class, e -> new IllegalStateException("Should not throw exception", e));
	/** Wraps checked exceptions as runtime. */
	public static ExceptionAdapter<RuntimeException> runtime =
		of(RuntimeException.class, RuntimeException::new);
	/** Wraps checked exceptions as illegal argument. */
	public static ExceptionAdapter<IllegalArgumentException> illegalArg =
		of(IllegalArgumentException.class, IllegalArgumentException::new);
	/** Wraps checked exceptions as IO. */
	public static final ExceptionAdapter<IOException> io = of(IOException.class, IOException::new);
	/** Wraps checked exceptions as runtime IO. */
	public static final ExceptionAdapter<IoExceptions.Runtime> runtimeIo =
		of(IoExceptions.Runtime.class, IoExceptions.Runtime::new);
	private final Class<E> cls;
	private final Function<Throwable, E> fn;

	public static <E extends Exception> ExceptionAdapter<E> of(Class<E> cls) {
		var constructor = Reflect.validConstructor(cls, Throwable.class);
		return of(cls, e -> Reflect.create(constructor, e));
	}

	public static <E extends Exception> ExceptionAdapter<E> of(Class<E> cls,
		Functions.Function<Throwable, E> fn) {
		return new ExceptionAdapter<>(cls, fn);
	}

	private ExceptionAdapter(Class<E> cls, Functions.Function<Throwable, E> fn) {
		this.cls = cls;
		this.fn = fn;
	}

	@Override
	public E apply(Throwable t) {
		if (t instanceof Error e) throw e;
		if (cls.isInstance(t)) return cls.cast(t);
		return fn.apply(t);
	}

	public void run(Throws.Runnable runnable) throws E {
		try {
			runnable.run();
		} catch (Error | RuntimeException e) {
			throw e;
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (Throwable t) {
			throw apply(t);
		}
	}

	public <T> T get(Throws.Supplier<T> supplier) throws E {
		try {
			return supplier.get();
		} catch (Error | RuntimeException e) {
			throw e;
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (Throwable t) {
			throw apply(t);
		}
	}

	public boolean getBool(Throws.BoolSupplier supplier) throws E {
		try {
			return supplier.getAsBool();
		} catch (Error | RuntimeException e) {
			throw e;
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (Throwable t) {
			throw apply(t);
		}
	}

	public byte getByte(Throws.ByteSupplier supplier) throws E {
		try {
			return supplier.getAsByte();
		} catch (RuntimeException e) {
			throw e;
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (Throwable t) {
			throw apply(t);
		}
	}

	public int getInt(Throws.IntSupplier supplier) throws E {
		try {
			return supplier.getAsInt();
		} catch (RuntimeException e) {
			throw e;
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (Throwable t) {
			throw apply(t);
		}
	}

	public long getLong(Throws.LongSupplier supplier) throws E {
		try {
			return supplier.getAsLong();
		} catch (RuntimeException e) {
			throw e;
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (Throwable t) {
			throw apply(t);
		}
	}

	public double getDouble(Throws.DoubleSupplier supplier) throws E {
		try {
			return supplier.getAsDouble();
		} catch (RuntimeException e) {
			throw e;
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (Throwable t) {
			throw apply(t);
		}
	}
}
