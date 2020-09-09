package ceri.x10.cm11a.device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.event.Listenable;
import ceri.common.io.StateChange;
import ceri.serial.javax.SerialConnector;
import ceri.serial.javax.SerialPortParams;

public class Cm11aSerialAdapter implements Cm11aConnector {
	public static final SerialPortParams SERIAL_PARAMS = SerialPortParams.of(4800);
	private final SerialConnector connector;

	public static Cm11aSerialAdapter of(SerialConnector connector) {
		return new Cm11aSerialAdapter(connector);
	}

	private Cm11aSerialAdapter(SerialConnector connector) {
		this.connector = connector;
	}

	public void broken() {
		connector.broken();
	}

	public void connect() throws IOException {
		connector.connect();
	}

	@Override
	public Listenable<StateChange> listeners() {
		return connector.listeners();
	}

	@Override
	public InputStream in() {
		return connector.in();
	}

	@Override
	public OutputStream out() {
		return connector.out();
	}

}
