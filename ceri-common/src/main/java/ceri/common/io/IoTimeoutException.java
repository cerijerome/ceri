package ceri.common.io;

import java.io.IOException;

public class IoTimeoutException extends IOException {
	private static final long serialVersionUID = 4222882577886865396L;

	public IoTimeoutException(String message) {
		super(message);
	}

	public IoTimeoutException(Throwable e) {
		super(e);
	}

}
