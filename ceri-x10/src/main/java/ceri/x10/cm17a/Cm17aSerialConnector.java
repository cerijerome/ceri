package ceri.x10.cm17a;

import java.io.IOException;
import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;

public class Cm17aSerialConnector implements Cm17aConnector {
	private static final int CONNECTION_TIMEOUT_MS_DEF = 5000;
	private final SerialPort sp;

	public Cm17aSerialConnector(String commPort) throws IOException {
		this(commPort, CONNECTION_TIMEOUT_MS_DEF);
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
		try {
			CommPortIdentifier cpi = portIdentifier(commPort);
			SerialPort sp = (SerialPort) cpi.open(getClass().getSimpleName(), connectionTimeoutMs);
			return sp;
		} catch (NoSuchPortException e) {
			throw new IOException(e);
		} catch (PortInUseException e) {
			throw new IOException(e);
		}
	}

	CommPortIdentifier portIdentifier(String commPort) throws NoSuchPortException {
		return CommPortIdentifier.getPortIdentifier(commPort);
	}

}
