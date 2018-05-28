package ceri.x10.cm17a;

import java.io.IOException;
import ceri.serial.javax.SerialPort;

public class Cm17aSerialConnector implements Cm17aConnector {
	private final SerialPort sp;

	public Cm17aSerialConnector(String commPort) throws IOException {
		this(commPort, SerialPort.CONNECTION_TIMEOUT_MS_DEF);
	}

	public Cm17aSerialConnector(String commPort, int connectionTimeoutMs) throws IOException {
		sp = openSerialPort(commPort, connectionTimeoutMs);
	}

	@Override
	public void setRts(boolean on) {
		sp.setRTS(on);
	}

	@Override
	public void setDtr(boolean on) {
		sp.setDTR(on);
	}

	@Override
	public void close() {
		sp.close();
	}

	private SerialPort openSerialPort(String commPort, int connectionTimeoutMs) throws IOException {
		return SerialPort.open(commPort, getClass().getSimpleName(), connectionTimeoutMs);
	}

}
