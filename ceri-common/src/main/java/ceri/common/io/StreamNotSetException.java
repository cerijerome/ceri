package ceri.common.io;

import java.io.IOException;

public class StreamNotSetException extends IOException {
	private static final long serialVersionUID = 4222882577886865396L;

	public StreamNotSetException(String message) {
		super(message);
	}

}
