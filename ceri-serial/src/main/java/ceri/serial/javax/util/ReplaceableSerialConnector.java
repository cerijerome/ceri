package ceri.serial.javax.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.function.ExceptionRunnable;
import ceri.common.io.ReplaceableInputStream;
import ceri.common.io.ReplaceableOutputStream;
import ceri.common.io.StateChange;
import ceri.log.util.LogUtil;
import ceri.serial.javax.FlowControl;
import ceri.serial.javax.SerialConnector;

/**
 * A serial connector pass-through that allows the underlying connector to be replaced. The caller
 * to setConnector is responsible for close/connect when changing connectors.
 */
public class ReplaceableSerialConnector implements SerialConnector {
	private final Listeners<Exception> errorListeners = Listeners.of();
	private final Listeners<StateChange> listeners = Listeners.of();
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

	/**
	 * Sets the serial connector. Does not close the current connector.
	 */
	@SuppressWarnings("resource")
	public void setConnector(SerialConnector con) {
		unlisten(this.con);
		this.con = con;
		in.set(con.in());
		out.set(con.out());
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
	public void open() throws IOException {
		exec(() -> con().open());
	}

	@Override
	public void close() throws IOException {
		SerialConnector con = this.con;
		if (con == null) return;
		unlisten(con);
		exec(con::close);
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
	public void breakBit(boolean on) throws IOException {
		exec(() -> con().breakBit(on));
	}

	@Override
	public void dtr(boolean on) throws IOException {
		exec(() -> con().dtr(on));
	}

	@Override
	public void flowControl(FlowControl flowControl) throws IOException {
		exec(() -> con().flowControl(flowControl));
	}

	@Override
	public void rts(boolean on) throws IOException {
		exec(() -> con().rts(on));
	}

	private void listen(StateChange state) {
		listeners.accept(state);
	}

	private void unlisten(SerialConnector con) {
		if (con == null) return;
		LogUtil.runSilently(() -> con.listeners().unlisten(listener));
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
