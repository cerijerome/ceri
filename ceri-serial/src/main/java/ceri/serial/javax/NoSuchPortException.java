package ceri.serial.javax;

import java.io.IOException;

public class NoSuchPortException extends IOException {
	private static final long serialVersionUID = 8240829182781044622L;

	public NoSuchPortException(String message, Throwable e) {
		super(message, e);
	}
}
