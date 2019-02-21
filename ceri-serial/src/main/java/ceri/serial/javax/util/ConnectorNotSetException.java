package ceri.serial.javax.util;

import java.io.IOException;

public class ConnectorNotSetException extends IOException {
	private static final long serialVersionUID = 4222882577886865395L;

	public ConnectorNotSetException(String message) {
		super(message);
	}

	public ConnectorNotSetException() {}

}
