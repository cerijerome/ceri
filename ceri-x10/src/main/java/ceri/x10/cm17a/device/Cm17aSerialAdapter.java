package ceri.x10.cm17a.device;

import java.io.IOException;
import ceri.common.event.Listenable;
import ceri.common.io.StateChange;
import ceri.serial.javax.SerialConnector;

public class Cm17aSerialAdapter implements Cm17aConnector {
	private final SerialConnector connector;

	public static Cm17aSerialAdapter of(SerialConnector connector) {
		return new Cm17aSerialAdapter(connector);
	}

	private Cm17aSerialAdapter(SerialConnector connector) {
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
	public void setDtr(boolean on) throws IOException {
		connector.setDtr(on);
	}

	@Override
	public void setRts(boolean on) throws IOException {
		connector.setRts(on);
	}

}
