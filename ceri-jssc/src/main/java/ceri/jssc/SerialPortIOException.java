package ceri.jssc;

import java.io.IOException;
import jssc.SerialPortException;

public class SerialPortIOException extends IOException {
	private static final long serialVersionUID = -6486932266936831945L;
	public final SerialPortException serialPort;
	
	public SerialPortIOException(SerialPortException serialPort) {
		super(serialPort);
		this.serialPort = serialPort;
	}
	
}
