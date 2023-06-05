package ceri.jna.clib;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionIntUnaryOperator;
import ceri.common.io.IoStreamUtil;

/**
 * Encapsulates a file descriptor as a closable resource.
 */
public interface FileDescriptor extends Closeable {
	/**
	 * Provide access to the underlying descriptor.
	 */
	// int fd() throws IOException;

	/**
	 * Returns an InputStream for the file descriptor.
	 */
	InputStream in();

	/**
	 * Returns an OutputStream for the file descriptor.
	 */
	OutputStream out();

	/**
	 * Apply the file descriptor.
	 */
	<E extends Exception> void accept(ExceptionIntConsumer<E> consumer) throws E;

	/**
	 * Apply the file descriptor.
	 */
	<E extends Exception> int applyAsInt(ExceptionIntUnaryOperator<E> operator) throws E;

	/**
	 * A stateless, no-op instance.
	 */
	FileDescriptor NULL = new FileDescriptor() {
		@Override
		public InputStream in() {
			return IoStreamUtil.nullIn;
		}

		@Override
		public OutputStream out() {
			return IoStreamUtil.nullOut;
		}

		@Override
		public <E extends Exception> void accept(ExceptionIntConsumer<E> consumer) throws E {}

		@Override
		public <E extends Exception> int applyAsInt(ExceptionIntUnaryOperator<E> operator)
			throws E {
			return 0;
		}

		@Override
		public void close() {}
	};
}
