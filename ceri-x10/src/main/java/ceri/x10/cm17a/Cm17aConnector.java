package ceri.x10.cm17a;

import java.io.Closeable;
import java.io.IOException;
import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;

public class Cm17aConnector implements Closeable {
	private final SerialPort sp;

	public Cm17aConnector(String commPort, int connectionTimeoutMs) throws IOException {
		sp = openSerialPort(commPort, connectionTimeoutMs);
	}

	public void setRts(boolean on) {
		sp.setRTS(on);
	}
	
	public void setDtr(boolean on) {
		sp.setDTR(on);
	}
	
	@Override
	public void close() {
		sp.close();
	}

	private SerialPort openSerialPort(String commPort, int connectionTimeoutMs) throws IOException {
		try {
			CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(commPort);
			SerialPort sp = (SerialPort) cpi.open(getClass().getSimpleName(), connectionTimeoutMs);
			return sp;
		} catch (NoSuchPortException e) {
			throw new IOException(e);
		} catch (PortInUseException e) {
			throw new IOException(e);
		}
	}
}
