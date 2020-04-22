package ceri.serial.javax.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.function.ExceptionRunnable;
import ceri.common.io.ReplaceableInputStream;
import ceri.common.io.ReplaceableOutputStream;
import ceri.common.io.StateChange;
import ceri.serial.javax.FlowControl;
import ceri.serial.javax.SerialConnector;

/**
 * A serial connector pass-through that allows the underlying connector to be replaced.
 * The caller to setConnector is responsible for close/connect when changing connectors.
 */
public class ReplaceableSerialConnector implements SerialConnector {
	private static final Logger logger = LogManager.getLogger();
	private final Listeners<Exception> errorListeners = new Listeners<>();
	private final Listeners<StateChange> listeners = new Listeners<>();
	private final Consumer<StateChange> listener = this::listen;
	private final ReplaceableInputStream in = new ReplaceableInputStream();
	private final ReplaceableOutputStream out = new ReplaceableOutputStream();
	private volatile SerialConnector con = null;

	public static ReplaceableSerialConnector of() {
		return new ReplaceableSerialConnector();
	}

	public Listenable<Exception> errorListeners() {
		return errorListeners;
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@SuppressWarnings("resource")
	public void setConnector(SerialConnector con) {
		unlisten(this.con);
		this.con = con;
		in.setInputStream(con.in());
		out.setOutputStream(con.out());
		con.listeners().listen(listener);
	}

	@SuppressWarnings("resource")
	@Override
	public void broken() {
		try {
			con().broken();
		} catch (IOException e) {
			errorListeners.accept(e);
		} catch (RuntimeException e) {
			errorListeners.accept(e);
			throw e;
		}
	}

	@Override
	public void connect() throws IOException {
		exec(() -> con().connect());
	}

	@Override
	public void close() throws IOException {
		SerialConnector con = this.con;
		if (con == null) return;
		exec(() -> con().close());
		unlisten(con);
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
	public void setBreakBit(boolean on) throws IOException {
		exec(() -> con().setBreakBit(on));
	}

	@Override
	public void setDtr(boolean on) throws IOException {
		exec(() -> con().setDtr(on));
	}

	@Override
	public void setFlowControl(FlowControl flowControl) throws IOException {
		exec(() -> con().setFlowControl(flowControl));
	}

	@Override
	public void setRts(boolean on) throws IOException {
		exec(() -> con().setRts(on));
	}

	private void listen(StateChange state) {
		listeners.accept(state);
	}

	private void unlisten(SerialConnector con) {
		if (con == null) return;
		try {
			con.listeners().unlisten(listener);
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private void exec(ExceptionRunnable<IOException> runnable) throws IOException {
		try {
			runnable.run();
		} catch (Exception e) {
			errorListeners.accept(e);
			throw e;
		}
	}

	private SerialConnector con() throws IOException {
		if (con == null) throw new ConnectorNotSetException();
		return con;
	}

}
