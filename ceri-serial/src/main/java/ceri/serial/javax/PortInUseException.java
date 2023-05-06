package ceri.serial.javax;

import java.io.IOException;

@SuppressWarnings("serial")
public class PortInUseException extends IOException {

	public PortInUseException(String message, Throwable e) {
		super(message, e);
	}

	public PortInUseException(Throwable e) {
		super(e);
	}

}
