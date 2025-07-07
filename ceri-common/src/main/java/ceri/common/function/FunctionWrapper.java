package ceri.common.function;

import ceri.common.exception.ExceptionAdapter;
import ceri.common.util.BasicUtil;

/**
 * "Exception Wormhole" or "Exception Re-Gifting" (anti-)pattern.
 * <p/>
 * Adapts function types that throw checked exceptions, so that they can be used in code that
 * requires non-throwing function types. Wrap is used to adapt functions to throw wrapped runtime
 * exceptions. Unwrap is used to surround the executed code; exceptions are unwrapped to throw the
 * original typed checked exception. The same wrapper instance must be used to wrap then unwrap.
 */
public class FunctionWrapper<E extends Exception> {
	/** Use to wrap exceptions from functions. */
	public final ExceptionAdapter<WrapException> wrap =
		ExceptionAdapter.of(WrapException.class, t -> new WrapException(this, t));
	/** Use to unwrap exceptions from functions. */
	public final Unwrap unwrap = new Unwrap();

	@SuppressWarnings("serial")
	public static class WrapException extends RuntimeException {
		private final FunctionWrapper<?> wrapper;

		private WrapException(FunctionWrapper<?> wrapper, Throwable t) {
			super(t);
			this.wrapper = wrapper;
		}

		private WrapException validate(FunctionWrapper<?> wrapper) {
			if (this.wrapper == wrapper) return this;
			throw new IllegalStateException("Mis-matched agent: " + wrapper);
		}
	}

	/**
	 * Executes functions, unwrapping and throwing any wrapped exceptions.
	 */
	public class Unwrap {
		private Unwrap() {}

		/**
		 * Executes the function, unwrapping and throwing any wrapped exception.
		 */
		public void run(Excepts.Runnable<? extends E> runnable) throws E {
			try {
				runnable.run();
			} catch (WrapException e) {
				throw validate(e);
			}
		}

		/**
		 * Executes the function, unwrapping and throwing any wrapped exception.
		 */
		public <R> R get(Excepts.Supplier<? extends E, R> supplier) throws E {
			try {
				return supplier.get();
			} catch (WrapException e) {
				throw validate(e);
			}
		}

		/**
		 * Executes the function, unwrapping and throwing any wrapped exception.
		 */
		public boolean getBool(Excepts.BoolSupplier<? extends E> supplier) throws E {
			try {
				return supplier.getAsBool();
			} catch (WrapException e) {
				throw validate(e);
			}
		}

		/**
		 * Executes the function, unwrapping and throwing any wrapped exception.
		 */
		public byte getByte(Excepts.ByteSupplier<? extends E> supplier) throws E {
			try {
				return supplier.getAsByte();
			} catch (WrapException e) {
				throw validate(e);
			}
		}

		/**
		 * Executes the function, unwrapping and throwing any wrapped exception.
		 */
		public int getInt(Excepts.IntSupplier<? extends E> supplier) throws E {
			try {
				return supplier.getAsInt();
			} catch (WrapException e) {
				throw validate(e);
			}
		}

		/**
		 * Executes the function, unwrapping and throwing any wrapped exception.
		 */
		public long getLong(Excepts.LongSupplier<? extends E> supplier) throws E {
			try {
				return supplier.getAsLong();
			} catch (WrapException e) {
				throw validate(e);
			}
		}

		/**
		 * Executes the function, unwrapping and throwing any wrapped exception.
		 */
		public double getDouble(Excepts.DoubleSupplier<? extends E> supplier) throws E {
			try {
				return supplier.getAsDouble();
			} catch (WrapException e) {
				throw validate(e);
			}
		}
	}

	/**
	 * Create an instance to handle an exception type.
	 */
	public static <E extends Exception> FunctionWrapper<E> of() {
		return new FunctionWrapper<>();
	}

	private FunctionWrapper() {}

	private E validate(WrapException e) {
		return BasicUtil.unchecked(e.validate(this).getCause());
	}
}
