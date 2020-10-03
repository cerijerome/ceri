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

	InputStream in();

	OutputStream out();

	static Cm11aConnector ofNull() {
		return new Null();
	}

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

	static class Null implements Cm11aConnector {
		private final Listenable<StateChange> listeners = Listenable.ofNull();
		private final InputStream in = IoStreamUtil.nullIn();
		private final OutputStream out = IoStreamUtil.nullOut();

		private Null() {}

		@Override
		public Listenable<StateChange> listeners() {
			return listeners;
		}

		@Override
		public InputStream in() {
			return in;
		}

		@Override
		public OutputStream out() {
			return out;
		}
	}

}
