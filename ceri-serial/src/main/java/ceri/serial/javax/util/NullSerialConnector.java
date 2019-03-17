package ceri.serial.javax.util;

import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.event.Listenable;
import ceri.common.event.NullListenable;
import ceri.common.io.IoStreamUtil;
import ceri.common.io.StateChange;
import ceri.serial.javax.FlowControl;
import ceri.serial.javax.SerialConnector;

/**
 * A no-op serial connector.
 */
public class NullSerialConnector implements SerialConnector {
	private final Listenable<StateChange> listenable = NullListenable.of();
	private final InputStream in;
	private final OutputStream out;

	public static NullSerialConnector of() {
		return new NullSerialConnector();
	}

	private NullSerialConnector() {
		in = IoStreamUtil.nullIn();
		out = IoStreamUtil.nullOut();
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listenable;
	}

	@Override
	public void broken() {}

	@Override
	public void connect() {}

	@Override
	public void close() {}

	@Override
	public InputStream in() {
		return in;
	}

	@Override
	public OutputStream out() {
		return out;
	}

	@Override
	public void setBreakBit(boolean on) {}

	@Override
	public void setDtr(boolean on) {}

	@Override
	public void setFlowControl(FlowControl flowControl) {}

	@Override
	public void setRts(boolean on) {}
}
