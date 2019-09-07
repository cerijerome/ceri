package ceri.common.factory;

/**
 * Runtime exception thrown when creation fails.
 */
public class FactoryException extends RuntimeException {
	private static final long serialVersionUID = -3404942802328899314L;

	public FactoryException(String message) {
		this(message, null);
	}

	public FactoryException(Throwable cause) {
		this(null, cause);
	}

	public FactoryException(String message, Throwable cause) {
		super(message, cause);
	}

}