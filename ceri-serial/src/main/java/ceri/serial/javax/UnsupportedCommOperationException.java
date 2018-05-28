package ceri.serial.javax;

import java.io.IOException;

public class UnsupportedCommOperationException extends IOException {
	private static final long serialVersionUID = -7476232496591452602L;

	public UnsupportedCommOperationException(Throwable e) {
		super(e);
	}
}
