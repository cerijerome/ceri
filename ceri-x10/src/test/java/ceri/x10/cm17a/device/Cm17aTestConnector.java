package ceri.x10.cm17a.device;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import static ceri.common.test.AssertUtil.assertRead;
import java.io.Closeable;
import java.io.IOException;
import ceri.common.data.ByteUtil;
import ceri.common.data.IntArray.Encoder;
import ceri.common.event.Listenable;
import ceri.common.io.StateChange;
import ceri.common.test.CallSync;
import ceri.common.test.TestListeners;
import ceri.common.test.TestOutputStream;

/**
 * Recreates bytes from connector calls.
 */
public class Cm17aTestConnector implements Cm17aConnector, Closeable {
	public final TestListeners<StateChange> listeners = TestListeners.of();
	private final TestOutputStream out;
	public final CallSync.Accept<Boolean> rts = CallSync.consumer(false, true);
	public final CallSync.Accept<Boolean> dtr = CallSync.consumer(false, true);
	private boolean reset = true;
	private int value = 0;
	private int bit = 0;

	public static Cm17aTestConnector of() {
		return new Cm17aTestConnector();
	}

	private Cm17aTestConnector() {
		out = TestOutputStream.of();
	}

	/**
	 * Resets the state. A full reset sets the rts/dtr reset state to power-off state. Processor
	 * only sets rts/dtr to standby on start/error; outside of these cases, the first bit would be
	 * lost with a full reset.
	 */
	public void reset(boolean full) {
		listeners.clear();
		out.resetState();
		dtr.reset();
		rts.reset();
		if (full) reset = true; // Will lose first bit if Processor does not send a reset.
		value = 0;
		bit = 0;
	}

	public void send(String s) throws IOException {
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (ch == 'd') setDtr(false);
			if (ch == 'D') setDtr(true);
			if (ch == 'r') setRts(false);
			if (ch == 'R') setRts(true);
		}
	}

	public void assertCodes(int... codes) throws IOException {
		Encoder enc = Encoder.of();
		for (int code : codes)
			enc.writeInts(0xd5, 0xaa, code >>> 8, code & 0xff, 0xad);
		assertRead(out.from, enc.ints());
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public void setDtr(boolean on) throws IOException {
		boolean rts = this.rts.value();
		boolean dtr = this.dtr.value();
		if (!reset && rts && !dtr && on) bit(true);
		this.dtr.accept(on, IO_ADAPTER);
		if (!rts && !on) reset = true;
		if (rts && on) standby();
	}

	@Override
	public void setRts(boolean on) throws IOException {
		boolean rts = this.rts.value();
		boolean dtr = this.dtr.value();
		if (!reset && dtr && !rts && on) bit(false);
		this.rts.accept(on, IO_ADAPTER);
		if (!dtr && !on) reset = true;
		if (dtr && on) standby();
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

	private void standby() {
		if (!reset) return; // already on standby
		reset = false;
		value = 0;
		bit = 0;
	}

	private void bit(boolean on) throws IOException {
		value |= ByteUtil.maskOfBitInt(on, Byte.SIZE - 1 - bit++);
		if (bit < Byte.SIZE) return;
		out.write(value);
		value = 0;
		bit = 0;
	}
}
