package ceri.serial.javax.test;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ValueCondition;
import ceri.common.event.Listenable;
import ceri.common.io.IoStreamUtil;
import ceri.common.io.StateChange;
import ceri.common.test.ResponseStream;
import ceri.common.test.TestInputStream;
import ceri.common.test.TestListeners;
import ceri.common.test.TestOutputStream;
import ceri.serial.javax.FlowControl;
import ceri.serial.javax.SerialConnector;

/**
 * A test connector for simulating serial connectors.
 * <p/>
 * TestInputStream actions can be set to provide data, control EOF, available() size, and generate
 * exceptions. TestOutputStream actions may also be set to generate exceptions. Alternatively, a
 * ResponseStream can be used to provide InputStream data based on written OutputStream data.
 * <p/>
 * Serial connector break bit, rts, dtr, flow control, connected, and broken state are tracked.
 * These values are set by SerialConnector calls, and may be manually set. Set a value to null to
 * generate a RuntimeException when the next set call is made. Sync conditions are also available to
 * wait for a setter to be called.
 * <p/>
 * Output data is collected and can be asserted. State changes can be signaled.
 */
public class SerialTestConnector implements SerialConnector {
	public final TestListeners<StateChange> listeners = TestListeners.of();
	public final BooleanCondition connectSync = BooleanCondition.of();
	public final ValueCondition<Boolean> dtrSync = ValueCondition.of();
	public final ValueCondition<Boolean> rtsSync = ValueCondition.of();
	public final ValueCondition<FlowControl> flowCtrlSync = ValueCondition.of();
	public final ValueCondition<Boolean> breakBitSync = ValueCondition.of();
	private final InputStream inActual;
	private final OutputStream outActual;
	public final TestInputStream in = new TestInputStream();
	public final TestOutputStream out = new TestOutputStream();
	// State variables - set to null to generate RuntimeException
	public volatile Boolean breakBit;
	public volatile Boolean rts;
	public volatile Boolean dtr;
	public volatile FlowControl flowControl;
	public volatile Boolean connected;
	public volatile Boolean broken;

	public static SerialTestConnector of() {
		return new SerialTestConnector();
	}

	protected SerialTestConnector() {
		inActual = IoStreamUtil.in(this::read, this::available);
		outActual = IoStreamUtil.out(this::write);
		reset(true);
	}

	public SerialTestConnector state(StateChange state) {
		listeners.accept(state);
		return this;
	}

	public void reset(boolean clearListeners) {
		if (clearListeners) listeners.clear();
		in.resetState();
		out.resetState();
		breakBit = false;
		rts = false;
		dtr = false;
		flowControl = FlowControl.none;
		connected = false;
		broken = false;
	}

	/**
	 * Sets the TestInputStream and TestOutputStream to delegate to the ResponseStream.
	 */
	@SuppressWarnings("resource")
	public void response(ResponseStream response) {
		in.in(response.in());
		out.out(response.out());
	}

	/* SerialConnector overrides */

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public void broken() {
		if (broken == null) throw new RuntimeException("broken = null");
		boolean notify = broken == FALSE;
		broken = true;
		if (notify) listeners.accept(StateChange.broken);
	}

	public void fixed() {
		boolean notify = broken == TRUE;
		broken = false;
		connected = true;
		if (notify) listeners.accept(StateChange.fixed);
	}

	@Override
	public OutputStream out() {
		return outActual;
	}

	@Override
	public InputStream in() {
		return inActual;
	}

	@Override
	public void connect() throws IOException {
		connectSync.signal();
		if (connected == null) throw new RuntimeException("connected = null");
		verifyUnbroken();
		connected = true;
	}

	@Override
	public void setDtr(boolean on) throws IOException {
		dtrSync.signal(on);
		if (dtr == null) throw new RuntimeException("dtr = null");
		verifyUnbroken();
		verifyConnected();
		dtr = on;
	}

	@Override
	public void setRts(boolean on) throws IOException {
		rtsSync.signal(on);
		if (rts == null) throw new RuntimeException("rts = null");
		verifyUnbroken();
		verifyConnected();
		rts = on;
	}

	@Override
	public void setFlowControl(FlowControl flowControl) throws IOException {
		flowCtrlSync.signal(flowControl);
		if (flowControl == null) throw new RuntimeException("flowControl = null");
		verifyUnbroken();
		verifyConnected();
		this.flowControl = flowControl;
	}

	@Override
	public void setBreakBit(boolean on) throws IOException {
		breakBitSync.signal(on);
		if (breakBit == null) throw new RuntimeException("breakBit = null");
		verifyUnbroken();
		verifyConnected();
		breakBit = on;
	}

	@Override
	public void close() {
		connected = false;
	}

	/* Support methods */

	private int available() throws IOException {
		verifyUnbroken();
		verifyConnected();
		return in.available();
	}

	private int read(byte[] b, int offset, int len) throws IOException {
		verifyUnbroken();
		verifyConnected();
		return in.read(b, offset, len);
	}

	private void write(byte[] b, int offset, int len) throws IOException {
		verifyUnbroken();
		verifyConnected();
		out.write(b, offset, len);
	}

	private void verifyConnected() throws IOException {
		if (connected != TRUE) throw new IOException("Not connected");
	}

	private void verifyUnbroken() throws IOException {
		if (broken == TRUE) throw new IOException("Connector is broken");
	}

}
