package ceri.common.io;

import java.io.IOException;

/**
 * Wraps an IOException as a runtime exception.
 */
public class RuntimeIoException extends RuntimeException {
	private static final long serialVersionUID = 3922679551995652749L;
	public final IOException ioException;

	public RuntimeIoException(String message, IOException e) {
		super(message, e);
		ioException = e;
	}

	public RuntimeIoException(IOException e) {
		super(e);
		ioException = e;
	}

}
