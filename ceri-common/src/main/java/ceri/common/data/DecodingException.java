package ceri.common.data;

import java.io.IOException;

public class DecodingException extends IOException {
	private static final long serialVersionUID = -4279886814654251929L;

	public DecodingException(String message) {
		super(message);
	}

	public DecodingException(Throwable e) {
		super(e);
	}

	public DecodingException(String message, Throwable e) {
		super(message, e);
	}

}
