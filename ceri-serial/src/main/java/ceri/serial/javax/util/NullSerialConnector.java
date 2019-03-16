package ceri.serial.javax.util;

import java.io.InputStream;
import java.io.OutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.io.StateChange;
import ceri.common.io.IoStreamUtil;
import ceri.serial.javax.FlowControl;
import ceri.serial.javax.SerialConnector;

/**
 * A no-op serial connector.
 */
public class NullSerialConnector implements SerialConnector {
	private static final Logger logger = LogManager.getLogger();
	private final Listeners<StateChange> listeners = new Listeners<>();
	private final InputStream in;
	private final OutputStream out;

	public static NullSerialConnector of() {
		return new NullSerialConnector();
	}

	private NullSerialConnector() {
		in = IoStreamUtil.nullIn();
		out = IoStreamUtil.nullOut();
		logger.debug("Created");
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public void broken() {}

	@Override
	public void connect() {}

	@Override
	public void close() {
		logger.debug("Closed");
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
	public void setBreakBit(boolean on) {}

	@Override
	public void setDtr(boolean on) {}

	@Override
	public void setFlowControl(FlowControl flowControl) {}

	@Override
	public void setRts(boolean on) {}
}
