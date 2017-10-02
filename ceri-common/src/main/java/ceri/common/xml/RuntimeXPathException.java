package ceri.common.xml;

public class RuntimeXPathException extends RuntimeException {
	private static final long serialVersionUID = -3371522416442356233L;

	public RuntimeXPathException(String message) {
		super(message);
	}

	public RuntimeXPathException(Throwable cause) {
		super(cause);
	}

	public RuntimeXPathException(String message, Throwable cause) {
		super(message, cause);
	}

}
