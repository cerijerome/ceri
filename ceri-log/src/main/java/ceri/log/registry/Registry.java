package ceri.log.registry;

import java.util.function.Consumer;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.property.TypedProperties;

/**
 * Specifies a registry for persistent storage and retrieval of properties.
 */
public interface Registry {
	/** A no-op, stateless instance. */
	Registry NULL = new Null() {};

	/**
	 * Queue a registry update. The registry determines when to execute queued updates, which may be
	 * in a separate thread.
	 */
	default void queue(Consumer<TypedProperties> update) {
		queue(new Object(), update);
	}

	/**
	 * Replace any currently queued updates from the given source with the given update. The
	 * registry determines when to execute queued updates, which may be in a separate thread.
	 */
	void queue(Object source, Consumer<TypedProperties> update);

	/**
	 * Executes the function immediately, passing the registry properties.
	 */
	<E extends Exception, T> T apply(ExceptionFunction<E, TypedProperties, T> function) throws E;

	/**
	 * Executes the consumer immediately, passing the registry properties.
	 */
	default <E extends Exception> void accept(ExceptionConsumer<E, TypedProperties> consumer)
		throws E {
		apply(p -> {
			consumer.accept(p);
			return null;
		});
	}

	/**
	 * Return a registry with access relative to a sub-group.
	 */
	Registry sub(String... subs);

	/**
	 * A no-op, stateless implementation.
	 */
	interface Null extends Registry {
		@Override
		default void queue(Object source, Consumer<TypedProperties> update) {}

		@Override
		default <E extends Exception, T> T apply(ExceptionFunction<E, TypedProperties, T> function)
			throws E {
			return function.apply(TypedProperties.NULL);
		}

		@Override
		default Registry sub(String... subs) {
			return this;
		}
	}
}
