package ceri.jna.clib;

import java.io.IOException;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionIntFunction;
import ceri.common.io.Connector;

/**
 * Encapsulates a file descriptor as a closable resource.
 */
public interface FileDescriptor extends Connector {
	/** A stateless, no-op instance. */
	FileDescriptor.Fixable NULL = new Null() {};

	/**
	 * Provide access to the underlying descriptor value.
	 */
	// int fd() throws IOException;

	/**
	 * Use the file descriptor value.
	 */
	void accept(ExceptionIntConsumer<IOException> consumer) throws IOException;

	/**
	 * Use the file descriptor value.
	 */
	<T> T apply(ExceptionIntFunction<IOException, T> function) throws IOException;

	/**
	 * A file descriptor that is state-aware, with state change notifications.
	 */
	interface Fixable extends FileDescriptor, Connector.Fixable {}

	/**
	 * A stateless, no-op implementation.
	 */
	interface Null extends Connector.Null, FileDescriptor.Fixable {
		@Override
		default void accept(ExceptionIntConsumer<IOException> consumer) throws IOException {}

		@Override
		default <T> T apply(ExceptionIntFunction<IOException, T> function) throws IOException {
			return null;
		}
	}
}
