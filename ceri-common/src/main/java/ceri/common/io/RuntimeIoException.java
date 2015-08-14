package ceri.common.io;

/**
 * Wraps an IOException as a runtime exception.
 */
public class RuntimeIoException extends RuntimeException {
	private static final long serialVersionUID = 3922679551995652749L;

	public RuntimeIoException(String message, Throwable e) {
		super(message, e);
	}

	public RuntimeIoException(String message) {
		super(message);
	}

	public RuntimeIoException(Throwable e) {
		super(e);
	}

}
