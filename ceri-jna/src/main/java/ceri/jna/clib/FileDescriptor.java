package ceri.jna.clib;

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
	<E extends Exception> void accept(ExceptionIntConsumer<E> consumer) throws E;

	/**
	 * Use the file descriptor value.
	 */
	<T, E extends Exception> T apply(ExceptionIntFunction<E, T> function) throws E;

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
		public <E extends Exception> void accept(ExceptionIntConsumer<E> consumer) throws E {}

		@Override
		public <T, E extends Exception> T apply(ExceptionIntFunction<E, T> function) throws E {
			return null;
		}
	}
}
