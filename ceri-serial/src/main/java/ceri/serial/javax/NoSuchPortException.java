package ceri.serial.javax;

import java.io.IOException;

@SuppressWarnings("serial")
public class NoSuchPortException extends IOException {
	public NoSuchPortException(String message, Throwable e) {
		super(message, e);
	}
}
