package ceri.common.io;

public class IoTimeoutException extends RuntimeException {
	private static final long serialVersionUID = 4222882577886865396L;

	public IoTimeoutException(String message) {
		super(message);
	}

	public IoTimeoutException(Throwable e) {
		super(e);
	}

}
