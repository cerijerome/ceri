package ceri.common.reflect;

/**
 * Exception thrown by application code when creation of an object fails.
 */
@SuppressWarnings("serial")
public class RuntimeInvocationException extends RuntimeException {

	public RuntimeInvocationException(String message) {
		this(message, null);
	}

	public RuntimeInvocationException(Throwable cause) {
		this(null, cause);
	}

	public RuntimeInvocationException(String message, Throwable cause) {
		super(message, cause);
	}
}