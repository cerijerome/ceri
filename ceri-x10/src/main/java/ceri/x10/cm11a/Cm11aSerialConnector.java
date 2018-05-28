package ceri.x10.cm11a;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.serial.javax.SerialPort;
import ceri.serial.javax.SerialPortParams;

public class Cm11aSerialConnector implements Cm11aConnector {
	private static final SerialPortParams params = SerialPortParams.of(4800);
	private final SerialPort sp;
	private final InputStream in;
	private final OutputStream out;

	public Cm11aSerialConnector(String commPort) throws IOException {
		this(commPort, SerialPort.CONNECTION_TIMEOUT_MS_DEF);
	}

	public Cm11aSerialConnector(String commPort, int connectionTimeoutMs) throws IOException {
		sp = openSerialPort(commPort, connectionTimeoutMs);
		in = sp.getInputStream();
		out = sp.getOutputStream();
	}

	@Override
	public InputStream in() {
		return in;
	}

	@Override
	public OutputStream out() {
		return out;
	}

	@Override
	public void close() {
		sp.close();
	}

	private SerialPort openSerialPort(String commPort, int connectionTimeoutMs) throws IOException {
		SerialPort sp = SerialPort.open(commPort, getClass().getSimpleName(), connectionTimeoutMs);
		sp.setParams(params);
		return sp;
	}

}
