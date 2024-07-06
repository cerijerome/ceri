package ceri.log.registry;

import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.RuntimeCloseable;
import ceri.common.property.BaseProperties;

public interface Registry {
	/** A no-op, stateless instance. */
	Registry NULL = () -> Opened.NULL;

	/**
	 * Locks the registry for reads/writes until close() is called on the returned accessor.
	 */
	Opened open();

	/**
	 * Convenience method. Opens the registry, provides property access to the reader, closes the
	 * registry, and returns the reader result.
	 */
	default <E extends Exception, T> T read(ExceptionFunction<E, BaseProperties, T> reader)
		throws E {
		try (var properties = open()) {
			return reader.apply(properties);
		}
	}

	/**
	 * Convenience method. Opens the registry, provides property access to the writer, and closes
	 * the registry.
	 */
	default <E extends Exception> void write(ExceptionConsumer<E, BaseProperties> writer) throws E {
		try (var properties = open()) {
			writer.accept(properties);
		}
	}

	/**
	 * The registry accessor
	 */
	abstract class Opened extends BaseProperties implements RuntimeCloseable {
		/** A no-op stateless instance. */
		public static final Opened NULL = new Opened(BaseProperties.NULL) {
			@Override
			public void close() {}
		};

		protected Opened(BaseProperties properties, String... prefix) {
			super(properties, prefix);
		}
	}

}
