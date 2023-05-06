package ceri.serial.javax;

import java.io.IOException;

@SuppressWarnings("serial")
public class UnsupportedCommOperationException extends IOException {
	public UnsupportedCommOperationException(Throwable e) {
		super(e);
	}
}
