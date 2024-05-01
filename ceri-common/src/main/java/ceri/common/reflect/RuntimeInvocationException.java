package ceri.common.reflect;

/**
 * Exception thrown by application code when creation of an object fails.
 */
public class RuntimeInvocationException extends RuntimeException {
	private static final long serialVersionUID = -1052089970865047643L;

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