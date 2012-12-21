/*
 * Created on Jun 12, 2004
 */
package ceri.common.reflect;

/**
 * Exception thrown by application code when creation of an object
 * fails.
 */
public class CreateException extends Exception {
	private static final long serialVersionUID = -1052089970865047643L;

	public CreateException(String message) {
		this(message, null);
	}

	public CreateException(Throwable cause) {
		this(null, cause);
	}

	public CreateException(String message, Throwable cause) {
		super(message, cause);
	}

}