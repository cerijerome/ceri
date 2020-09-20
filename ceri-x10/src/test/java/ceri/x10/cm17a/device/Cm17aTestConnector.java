package ceri.x10.cm17a.device;

import java.io.IOException;
import ceri.common.data.ByteUtil;
import ceri.common.data.IntArray.Encoder;
import ceri.common.event.Listenable;
import ceri.common.io.StateChange;
import ceri.common.test.Capturer;
import ceri.common.test.TestListeners;
import ceri.x10.util.X10TestUtil;
import ceri.x10.util.X10TestUtil.ErrorType;

/**
 * Recreates bytes from connector calls.
 */
public class Cm17aTestConnector implements Cm17aConnector {
	public final TestListeners<StateChange> listeners = TestListeners.of();
	public final Capturer.Int bytes = Capturer.ofInt();
	private volatile ErrorType dtrError = ErrorType.none;
	private volatile ErrorType rtsError = ErrorType.none;
	private boolean rts = false;
	private boolean dtr = false;
	private boolean reset = true;
	private int value = 0;
	private int bit = 0;

	/**
	 * Resets the state. A full reset sets the rts/dtr reset state to power-off state. Processor
	 * only sets rts/dtr to standby on start/error; outside of these cases, the first bit would be
	 * lost with a full reset.
	 */
	public void reset(boolean full) {
		listeners.clear();
		bytes.reset();
		errors(ErrorType.none, ErrorType.none);
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

	public void assertCodes(int... codes) {
		Encoder enc = Encoder.of();
		for (int code : codes)
			enc.writeInts(0xd5, 0xaa, code >>> 8, code & 0xff, 0xad);
		bytes.verifyInt(enc.ints());
		bytes.reset();
	}

	public void errors(ErrorType dtrError, ErrorType rtsError) {
		this.dtrError = dtrError;
		this.rtsError = rtsError;
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public void setDtr(boolean on) throws IOException {
		X10TestUtil.error(dtrError);
		if (!reset && rts && !dtr && on) bit(true);
		dtr = on;
		if (!rts && !dtr) reset = true;
		if (rts && dtr) standby();
	}

	@Override
	public void setRts(boolean on) throws IOException {
		X10TestUtil.error(rtsError);
		if (!reset && dtr && !rts && on) bit(false);
		rts = on;
		if (!rts && !dtr) reset = true;
		if (rts && dtr) standby();
	}

	private void standby() {
		if (!reset) return; // already on standby
		reset = false;
		value = 0;
		bit = 0;
	}

	private void bit(boolean on) {
		value |= ByteUtil.maskOfBitInt(on, Byte.SIZE - 1 - bit++);
		if (bit < Byte.SIZE) return;
		bytes.accept(value);
		value = 0;
		bit = 0;
	}
}
