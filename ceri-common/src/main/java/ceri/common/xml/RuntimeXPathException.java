package ceri.common.xml;

@SuppressWarnings("serial")
public class RuntimeXPathException extends RuntimeException {

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
