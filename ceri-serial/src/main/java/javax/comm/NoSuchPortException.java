package javax.comm;

public class NoSuchPortException extends Exception {
	private static final long serialVersionUID = 8240829182781044622L;

	public NoSuchPortException(String message, purejavacomm.NoSuchPortException e) {
		super(message, e);
	}
}
