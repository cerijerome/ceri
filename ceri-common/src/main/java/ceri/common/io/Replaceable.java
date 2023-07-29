package ceri.common.io;

import java.io.Closeable;
import java.io.IOException;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;

/**
 * A delegate pass-through that allows the underlying delegate to be replaced. Calling replace()
 * closes the current delegate, whereas set() requires the caller to manage delegate lifecycle.
 */
public abstract class Replaceable<T extends Closeable> implements Closeable {
	private static final String NAME = "delegate";
	protected final Listeners<Exception> errorListeners = Listeners.of();
	private final String delegateName;
	private volatile T delegate = null;

	public static <T extends Closeable> Field<T> field() {
		return field(NAME);
	}

	public static <T extends Closeable> Field<T> field(String name) {
		return new Field<>(name);
	}

	/**
	 * An implementation to be used as a member field, with exposed methods. Useful for classes that
	 * cannot extend Replaceable.
	 */
	public static class Field<T extends Closeable> extends Replaceable<T> {
		protected Field(String delegateName) {
			super(delegateName);
		}

		@Override
		public <E extends Exception> void acceptIfSet(ExceptionConsumer<E, T> consumer) throws E {
			super.acceptIfSet(consumer);
		}

		@Override
		public <E extends Exception, R> R applyIfSet(ExceptionFunction<E, T, R> function, R def)
			throws E {
			return super.applyIfSet(function, def);
		}

		@Override
		public <E extends Exception> void acceptValid(ExceptionConsumer<E, T> consumer)
			throws IOException, E {
			super.acceptValid(consumer);
		}

		@Override
		public <E extends Exception, R> R applyValid(ExceptionFunction<E, T, R> function)
			throws IOException, E {
			return super.applyValid(function);
		}
	}

	protected Replaceable(String delegateName) {
		this.delegateName = delegateName;
	}

	/**
	 * Listen for errors on invoked calls.
	 */
	public Listenable<Exception> errors() {
		return errorListeners;
	}

	/**
	 * Close the current delegate, and set the new delegate. Does nothing if no change in delegate.
	 */
	public void replace(T delegate) throws IOException {
		if (this.delegate == delegate) return;
		close();
		set(delegate);
	}

	/**
	 * Sets the delegate. Does not close the current delegate.
	 */
	public void set(T delegate) {
		this.delegate = delegate;
	}

	@Override
	public void close() throws IOException {
		close(delegate);
	}

	/**
	 * Invokes the consumer with the current delegate. Does nothing if the delegate is not set.
	 */
	protected <E extends Exception> void acceptIfSet(ExceptionConsumer<E, T> consumer) throws E {
		applyIfSet(delegate -> {
			consumer.accept(delegate);
			return null;
		}, null);
	}

	/**
	 * Invokes the function with the current delegate. Returns the given default value if the
	 * delegate is not set.
	 */
	protected <E extends Exception, R> R applyIfSet(ExceptionFunction<E, T, R> function, R def)
		throws E {
		var delegate = this.delegate;
		if (delegate == null) return def;
		try {
			return function.apply(delegate);
		} catch (Exception e) {
			errorListeners.accept(e);
			throw e;
		}
	}

	/**
	 * Invokes the consumer with the current delegate. If the delegate is not set, an exception is
	 * thrown. Error listeners are notified of any exception thrown by the consumer.
	 */
	protected <E extends Exception> void acceptValid(ExceptionConsumer<E, T> consumer)
		throws IOException, E {
		applyValid(delegate -> {
			consumer.accept(delegate);
			return null;
		});
	}

	/**
	 * Invokes the function with the current delegate. If the delegate is not set, an exception is
	 * thrown. Error listeners are notified of any exception thrown by the function.
	 */
	@SuppressWarnings("resource")
	protected <E extends Exception, R> R applyValid(ExceptionFunction<E, T, R> function)
		throws IOException, E {
		var delegate = validDelegate();
		try {
			return function.apply(delegate);
		} catch (Exception e) {
			errorListeners.accept(e);
			throw e;
		}
	}

	/**
	 * Access the delegate. Throws exception if not set.
	 */
	private T validDelegate() throws IOException {
		var delegate = this.delegate;
		if (delegate == null) throw new NotSetException(delegateName);
		return delegate;
	}

	private void close(T delegate) throws IOException {
		if (delegate != null) delegate.close();
	}
}
