package ceri.serial.jssc;

import java.io.IOException;
import jssc.SerialPortTimeoutException;

public class SerialPortTimeoutIOException extends IOException {
	private static final long serialVersionUID = -6486932266936831945L;
	public final SerialPortTimeoutException serialPortTimeout;
	
	public SerialPortTimeoutIOException(SerialPortTimeoutException serialPortTimeout) {
		super(serialPortTimeout);
		this.serialPortTimeout = serialPortTimeout;
	}
	
}
