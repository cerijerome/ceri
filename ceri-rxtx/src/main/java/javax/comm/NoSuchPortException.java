package javax.comm;

public class NoSuchPortException extends Exception {
	private static final long serialVersionUID = 8240829182781044622L;

	NoSuchPortException(String message, gnu.io.NoSuchPortException e) {
		super(message, e);
	}
}
