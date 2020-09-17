package ceri.x10.cm11a.device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.event.Listenable;
import ceri.common.io.IoStreamUtil;
import ceri.common.io.StateChange;
import ceri.serial.javax.SerialConnector;
import ceri.serial.javax.SerialPortParams;

public interface Cm11aConnector extends Listenable.Indirect<StateChange> {
	static Cm11aConnector NULL = new Cm11aConnector() {
		@Override
		public Listenable<StateChange> listeners() {
			return Listenable.ofNull();
		}

		@Override
		public InputStream in() {
			return IoStreamUtil.nullIn();
		}

		@Override
		public OutputStream out() {
			return IoStreamUtil.nullOut();
		}
	};

	InputStream in();

	OutputStream out();

	static Serial serial(SerialConnector connector) {
		return new Serial(connector);
	}

	static class Serial implements Cm11aConnector {
		public static final SerialPortParams PARAMS = SerialPortParams.of(4800);
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
		public InputStream in() {
			return connector.in();
		}

		@Override
		public OutputStream out() {
			return connector.out();
		}
	}
}
