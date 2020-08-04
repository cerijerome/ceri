package ceri.serial.javax.util;

import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ValueCondition;
import ceri.common.event.Listenable;
import ceri.common.io.StateChange;
import ceri.common.test.TestInputStream;
import ceri.common.test.TestListeners;
import ceri.common.test.TestOutputStream;
import ceri.serial.javax.FlowControl;
import ceri.serial.javax.SerialConnector;

/**
 * A test connector for testing serial calls. Actions can be set for input and output streams.
 * Output data is collected and can be asserted. State changes can be signaled.
 */
public class SerialTestConnector implements SerialConnector {
	public final TestListeners<StateChange> listeners = TestListeners.of();
	public final BooleanCondition connectSync = BooleanCondition.of();
	public final ValueCondition<Boolean> dtrSync = ValueCondition.of();
	public final ValueCondition<Boolean> rtsSync = ValueCondition.of();
	public final ValueCondition<FlowControl> flowCtrlSync = ValueCondition.of();
	public final ValueCondition<Boolean> breakBitSync = ValueCondition.of();
	public final TestInputStream in = new TestInputStream();
	public final TestOutputStream out = new TestOutputStream();
	private volatile Boolean broken = false; // null for RuntimeException

	// TODO: combine with ResponseSerialConnector

	public SerialTestConnector state(StateChange state) {
		listeners.accept(state);
		return this;
	}

	public SerialTestConnector reset(boolean clearListeners) {
		if (clearListeners) listeners.clear();
		in.resetState();
		out.resetState();
		return this;
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	public void broken(Boolean broken) {
		this.broken = broken;
	}

	@Override
	public void broken() {
		if (broken == null) throw new RuntimeException("broken = null");
		if (!broken) listeners.accept(StateChange.broken);
		broken = true;
	}

	public void fixed() {
		broken = false;
	}

	@Override
	public OutputStream out() {
		return out;
	}

	@Override
	public InputStream in() {
		return in;
	}

	@Override
	public void connect() {
		connectSync.signal();
	}

	@Override
	public void setDtr(boolean on) {
		dtrSync.signal(on);
	}

	@Override
	public void setRts(boolean on) {
		rtsSync.signal(on);
	}

	@Override
	public void setFlowControl(FlowControl flowControl) {
		flowCtrlSync.signal(flowControl);
	}

	@Override
	public void setBreakBit(boolean on) {
		breakBitSync.signal(on);
	}

	@Override
	public void close() {}

}
