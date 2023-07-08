package ceri.common.io;

import java.io.IOException;

/**
 * An exception thrown when a delegate has not been set. 
 */
public class NotSetException extends IOException {
	private static final long serialVersionUID = 4222882577886865396L;

	public NotSetException() {
		this("delegate");
	}
	
	public NotSetException(String message) {
		super(message);
	}
}
