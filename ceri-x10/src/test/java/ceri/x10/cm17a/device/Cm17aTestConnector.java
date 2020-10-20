package ceri.x10.cm17a.device;

import static ceri.common.function.FunctionUtil.execSilently;
import static ceri.common.test.AssertUtil.assertRead;
import java.io.Closeable;
import java.io.IOException;
import ceri.common.data.ByteStream;
import ceri.common.data.ByteUtil;
import ceri.common.data.IntArray.Encoder;
import ceri.common.event.Listenable;
import ceri.common.io.PipedStream;
import ceri.common.io.StateChange;
import ceri.common.test.ErrorGen;
import ceri.common.test.TestListeners;

/**
 * Recreates bytes from connector calls.
 */
public class Cm17aTestConnector implements Cm17aConnector, Closeable {
	public final TestListeners<StateChange> listeners = TestListeners.of();
	private final PipedStream pipedOut;
	public final ByteStream.Reader from;
	public final ErrorGen dtrError = ErrorGen.of();
	public final ErrorGen rtsError = ErrorGen.of();
	private boolean rts = false;
	private boolean dtr = false;
	private boolean reset = true;
	private int value = 0;
	private int bit = 0;

	public static Cm17aTestConnector of() {
		return new Cm17aTestConnector();
	}

	@SuppressWarnings("resource")
	private Cm17aTestConnector() {
		pipedOut = PipedStream.of();
		from = ByteStream.reader(pipedOut.in());
	}

	/**
	 * Resets the state. A full reset sets the rts/dtr reset state to power-off state. Processor
	 * only sets rts/dtr to standby on start/error; outside of these cases, the first bit would be
	 * lost with a full reset.
	 */
	public void reset(boolean full) {
		listeners.clear();
		execSilently(pipedOut::clear);
		dtrError.reset();
		rtsError.reset();
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
		assertRead(from, enc.ints());
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public void setDtr(boolean on) throws IOException {
		dtrError.generateIo();
		if (!reset && rts && !dtr && on) bit(true);
		dtr = on;
		if (!rts && !dtr) reset = true;
		if (rts && dtr) standby();
	}

	@Override
	public void setRts(boolean on) throws IOException {
		rtsError.generateIo();
		if (!reset && dtr && !rts && on) bit(false);
		rts = on;
		if (!rts && !dtr) reset = true;
		if (rts && dtr) standby();
	}

	@Override
	public void close() {
		pipedOut.close();
	}

	private void standby() {
		if (!reset) return; // already on standby
		reset = false;
		value = 0;
		bit = 0;
	}

	@SuppressWarnings("resource")
	private void bit(boolean on) throws IOException {
		value |= ByteUtil.maskOfBitInt(on, Byte.SIZE - 1 - bit++);
		if (bit < Byte.SIZE) return;
		pipedOut.out().write(value);
		value = 0;
		bit = 0;
	}
}
