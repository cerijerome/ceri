package ceri.serial.comm.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.io.ReplaceableInputStream;
import ceri.common.io.ReplaceableOutputStream;
import ceri.common.io.StateChange;
import ceri.log.util.LogUtil;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Serial;
import ceri.serial.comm.SerialParams;

/**
 * A serial port pass-through that allows the underlying connector to be replaced. The caller to
 * setSerial is responsible for close/connect when changing connectors.
 */
public class ReplaceableSerial implements Serial.Fixable {
	private static final Logger logger = LogManager.getLogger();
	private final Listeners<Exception> errorListeners = Listeners.of();
	private final Listeners<StateChange> listeners = Listeners.of();
	private final Consumer<StateChange> listener = this::listen;
	private final ReplaceableInputStream in = new ReplaceableInputStream();
	private final ReplaceableOutputStream out = new ReplaceableOutputStream();
	private volatile Serial.Fixable serial = null;

	public static ReplaceableSerial of() {
		return new ReplaceableSerial();
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
	public void setSerial(Serial.Fixable serial) {
		unlisten(this.serial);
		this.serial = serial;
		in.setInputStream(serial.in());
		out.setOutputStream(serial.out());
		serial.listeners().listen(listener);
	}

	@SuppressWarnings("resource")
	@Override
	public void broken() {
		runtimeSerial().broken();
	}

	@Override
	public void open() throws IOException {
		exec(Serial.Fixable::open);
	}

	@Override
	public String port() {
		return Serial.port(serial);
	}

	@Override
	public InputStream in() {
		return in;
	}

	@Override
	public OutputStream out() {
		return out;
	}

	@SuppressWarnings("resource")
	@Override
	public void inBufferSize(int size) {
		runtimeSerial().inBufferSize(size);
	}

	@SuppressWarnings("resource")
	@Override
	public int inBufferSize() {
		return runtimeSerial().inBufferSize();
	}

	@SuppressWarnings("resource")
	@Override
	public void outBufferSize(int size) {
		runtimeSerial().outBufferSize(size);
	}

	@SuppressWarnings("resource")
	@Override
	public int outBufferSize() {
		return runtimeSerial().outBufferSize();
	}

	@Override
	public void params(SerialParams params) throws IOException {
		exec(serial -> serial.params(params));
	}

	@SuppressWarnings("resource")
	@Override
	public SerialParams params() {
		return runtimeSerial().params();
	}

	@Override
	public void flowControl(Collection<FlowControl> flowControl) throws IOException {
		exec(serial -> serial.flowControl(flowControl));
	}

	@SuppressWarnings("resource")
	@Override
	public Set<FlowControl> flowControl() {
		return runtimeSerial().flowControl();
	}

	@Override
	public void brk(boolean on) throws IOException {
		exec(serial -> serial.brk(on));
	}

	@Override
	public void rts(boolean on) throws IOException {
		exec(serial -> serial.rts(on));
	}

	@Override
	public void dtr(boolean on) throws IOException {
		exec(serial -> serial.dtr(on));
	}

	@Override
	public boolean rts() throws IOException {
		return execGet(Serial::rts);
	}

	@Override
	public boolean dtr() throws IOException {
		return execGet(Serial::dtr);
	}

	@Override
	public boolean cd() throws IOException {
		return execGet(Serial::cd);
	}

	@Override
	public boolean cts() throws IOException {
		return execGet(Serial::cts);
	}

	@Override
	public boolean dsr() throws IOException {
		return execGet(Serial::dsr);
	}

	@Override
	public boolean ri() throws IOException {
		return execGet(Serial::ri);
	}

	@Override
	public void close() throws IOException {
		var serial = this.serial;
		if (serial == null) return;
		unlisten(serial);
		serial.close();
	}

	private void listen(StateChange state) {
		listeners.accept(state);
	}

	private void unlisten(Serial.Fixable serial) {
		if (serial == null) return;
		LogUtil.execute(logger, () -> serial.listeners().unlisten(listener));
	}

	private void exec(ExceptionConsumer<IOException, Serial.Fixable> consumer) throws IOException {
		execGet(serial -> {
			consumer.accept(serial);
			return null;
		});
	}

	@SuppressWarnings("resource")
	private <T> T execGet(ExceptionFunction<IOException, Serial.Fixable, T> function)
		throws IOException {
		try {
			return function.apply(serial());
		} catch (Exception e) {
			errorListeners.accept(e);
			throw e;
		}
	}

	private Serial.Fixable serial() throws IOException {
		var serial = this.serial;
		if (serial == null) throw new IOException("Serial port unavailable");
		return serial;
	}

	private Serial.Fixable runtimeSerial() {
		var serial = this.serial;
		if (serial == null) throw new IllegalStateException("Serial port unavailable");
		return serial;
	}

}
