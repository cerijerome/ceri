package ceri.x10.cm17a.device;

import java.io.IOException;
import ceri.common.event.Listenable;
import ceri.common.io.StateChange;
import ceri.serial.javax.SerialConnector;

public interface Cm17aConnector extends Listenable.Indirect<StateChange> {
	static Cm17aConnector NULL = new Null();

	void setRts(boolean on) throws IOException;

	void setDtr(boolean on) throws IOException;

	static Serial serial(SerialConnector connector) {
		return new Serial(connector);
	}

	static class Null implements Cm17aConnector {

		private Null() {}

		@Override
		public Listenable<StateChange> listeners() {
			return Listenable.ofNull();
		}

		@Override
		public void setRts(boolean on) {}

		@Override
		public void setDtr(boolean on) {}
	}

	static class Serial implements Cm17aConnector {
		private final SerialConnector connector;

		private Serial(SerialConnector connector) {
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
}
