package ceri.jna.clib;

import java.io.IOException;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionIntFunction;
import ceri.common.io.Connector;

/**
 * Encapsulates a file descriptor as a closable resource.
 */
public interface FileDescriptor extends Connector {

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
	 * A no-op file descriptor instance.
	 */
	FileDescriptor.Fixable NULL = new Null();

	/**
	 * A no-op file descriptor implementation.
	 */
	class Null extends Connector.Null implements FileDescriptor.Fixable {

		@Override
		public void accept(ExceptionIntConsumer<IOException> consumer) throws IOException {}

		@Override
		public <T> T apply(ExceptionIntFunction<IOException, T> function) throws IOException {
			return null;
		}
	}
}
