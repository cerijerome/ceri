package ceri.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.FunctionUtil;

/**
 * A delegate pass-through that allows the underlying delegate to be replaced. Calling replace()
 * closes the current delegate, whereas set() requires the caller to manage delegate lifecycle.
 */
public class Replaceable<T extends Closeable> implements Closeable {
	private static final String NAME = "delegate";
	protected final Listeners<Exception> errorListeners = Listeners.of();
	private final String delegateName;
	private volatile T delegate = null;

	public Replaceable() {
		this(NAME);
	}
	
	protected Replaceable(String delegateName) {
		this.delegateName = delegateName;
	}
	
	public Listenable<Exception> errorListeners() {
		return errorListeners;
	}

	/**
	 * Close the current delegate, and set the new delegate.
	 */
	public void replace(T delegate) throws IOException {
		close();
		set(delegate);
	}

	/**
	 * Sets the delegate. Does not close the current delegate.
	 */
	@SuppressWarnings("resource")
	public void set(T delegate) {
		Objects.requireNonNull(delegate);
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
		FunctionUtil.safeAccept(delegate, consumer);
	}

	/**
	 * Invokes the function with the current delegate. Returns the given default value if the
	 * delegate is not set.
	 */
	protected <E extends Exception, R> R applyIfSet(ExceptionFunction<E, T, R> function, R def)
		throws E {
		return FunctionUtil.safeApply(delegate, function, def);
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
