package ceri.common.except;

import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;

/**
 * Captures typed checked exceptions to throw later. Useful for lambda functions that only allow
 * runtime exceptions.
 */
public class ExceptionCaptor<E extends Exception> {
	private E exception;

	public static <E extends Exception, T> T
		apply(Functions.Function<? super ExceptionCaptor<E>, T> function) throws E {
		var e = ExceptionCaptor.<E>of();
		return e.verify(function.apply(e));
	}

	/**
	 * Creates a new instance.
	 */
	public static <E extends Exception> ExceptionCaptor<E> of() {
		return new ExceptionCaptor<>();
	}

	private ExceptionCaptor() {}

	/**
	 * Call after execution.
	 */
	public void verify() throws E {
		verify(null);
	}

	/**
	 * Call after execution.
	 */
	public <T> T verify(T value) throws E {
		if (exception != null) throw exception;
		return value;
	}

	/**
	 * Executes and captures the typed exception if thrown. Returns false if captured.
	 */
	public boolean run(Excepts.Runnable<? extends E> runnable) {
		try {
			exception = null;
			if (runnable == null) return false;
			runnable.run();
			return true;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			exception = Reflect.unchecked(e);
			return false;
		}
	}

	/**
	 * Executes and captures the typed exception if thrown. Returns default if captured.
	 */
	public <T> T get(Excepts.Supplier<? extends E, T> supplier, T def) {
		try {
			exception = null;
			return supplier == null ? def : supplier.get();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			exception = Reflect.unchecked(e);
			return def;
		}
	}

	/**
	 * Executes and captures the typed exception if thrown. Returns default if captured.
	 */
	public boolean getBool(Excepts.BoolSupplier<? extends E> supplier, boolean def) {
		try {
			exception = null;
			return supplier == null ? def : supplier.getAsBool();
		} catch (Error | RuntimeException e) {
			throw e;
		} catch (Exception e) {
			exception = Reflect.unchecked(e);
			return def;
		}
	}

	/**
	 * Executes and captures the typed exception if thrown. Returns default if captured.
	 */
	public byte getByte(Excepts.ByteSupplier<? extends E> supplier, byte def) {
		try {
			return supplier.getAsByte();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			exception = Reflect.unchecked(e);
			return def;
		}
	}

	/**
	 * Executes and captures the typed exception if thrown. Returns default if captured.
	 */
	public int getInt(Excepts.IntSupplier<? extends E> supplier, int def) {
		try {
			return supplier.getAsInt();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			exception = Reflect.unchecked(e);
			return def;
		}
	}

	/**
	 * Executes and captures the typed exception if thrown. Returns default if captured.
	 */
	public long getLong(Excepts.LongSupplier<? extends E> supplier, long def) {
		try {
			return supplier.getAsLong();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			exception = Reflect.unchecked(e);
			return def;
		}
	}

	/**
	 * Executes and captures the typed exception if thrown. Returns default if captured.
	 */
	public double getDouble(Excepts.DoubleSupplier<? extends E> supplier, double def) {
		try {
			return supplier.getAsDouble();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			exception = Reflect.unchecked(e);
			return def;
		}
	}
}
