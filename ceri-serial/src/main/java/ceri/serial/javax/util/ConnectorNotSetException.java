package ceri.serial.javax.util;

import java.io.IOException;

@SuppressWarnings("serial")
public class ConnectorNotSetException extends IOException {

	public ConnectorNotSetException(String message) {
		super(message);
	}

	public ConnectorNotSetException() {}

}
