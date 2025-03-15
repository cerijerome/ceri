package ceri.common.io;

import java.io.IOException;

/**
 * An exception thrown when a delegate has not been set.
 */
@SuppressWarnings("serial")
public class NotSetException extends IOException {

	public NotSetException() {
		this("delegate");
	}

	public NotSetException(String message) {
		super(message);
	}
}
