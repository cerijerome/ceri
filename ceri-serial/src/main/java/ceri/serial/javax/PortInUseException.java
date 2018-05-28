package ceri.serial.javax;

import java.io.IOException;

public class PortInUseException extends IOException {
	private static final long serialVersionUID = 1872331673223268172L;

	public PortInUseException(String message, Throwable e) {
		super(message, e);
	}
	
	public PortInUseException(Throwable e) {
		super(e);
	}
	
}
