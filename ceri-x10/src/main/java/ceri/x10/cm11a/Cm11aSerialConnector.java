package ceri.x10.cm11a;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

public class Cm11aSerialConnector implements Cm11aConnector {
	private static final int CONNECTION_TIMEOUT_MS_DEF = 5000;
	private static final int BAUD = 4800;
	private static final int DATA_BITS = SerialPort.DATABITS_8;
	private static final int STOP_BITS = SerialPort.STOPBITS_1;
	private static final int PARITY = SerialPort.PARITY_NONE;
	private final SerialPort sp;
	private final InputStream in;
	private final OutputStream out;

	public Cm11aSerialConnector(String commPort) throws IOException {
		this(commPort, CONNECTION_TIMEOUT_MS_DEF);
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
		try {
			CommPortIdentifier cpi = portIdentifier(commPort);
			SerialPort sp = (SerialPort) cpi.open(getClass().getSimpleName(), connectionTimeoutMs);
			sp.setSerialPortParams(BAUD, DATA_BITS, STOP_BITS, PARITY);
			return sp;
		} catch (NoSuchPortException e) {
			throw new IOException(e);
		} catch (PortInUseException e) {
			throw new IOException(e);
		} catch (UnsupportedCommOperationException e) {
			throw new IOException(e);
		}
	}
	
	CommPortIdentifier portIdentifier(String commPort) throws NoSuchPortException {
		return CommPortIdentifier.getPortIdentifier(commPort);
	}

}
