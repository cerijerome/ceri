package ceri.x10.cm17a.device;

import java.io.IOException;
import ceri.common.data.Bytes;
import ceri.common.data.IntArray;
import ceri.common.event.Listenable;
import ceri.common.except.ExceptionAdapter;
import ceri.common.io.StateChange;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.TestFixable;
import ceri.common.test.TestOutputStream;

/**
 * Recreates bytes from connector calls.
 */
public class Cm17aTestConnector extends TestFixable implements Cm17aConnector {
	private final TestOutputStream out;
	public final CallSync.Consumer<Boolean> rts = CallSync.consumer(false, true);
	public final CallSync.Consumer<Boolean> dtr = CallSync.consumer(false, true);
	private boolean reset = true;
	private int value = 0;
	private int bit = 0;

	public static Cm17aTestConnector of() {
		return new Cm17aTestConnector();
	}

	private Cm17aTestConnector() {
		super(null);
		out = TestOutputStream.of();
	}

	@Override
	public void reset() {
		out.resetState();
		dtr.reset();
		rts.reset();
		reset = true; // Will lose first bit if Processor does not send a reset.
		value = 0;
		bit = 0;
	}

	public void assertCodes(int... codes) throws IOException {
		var enc = IntArray.Encoder.of();
		for (int code : codes)
			enc.writeInts(0xd5, 0xaa, code >>> 8, code & 0xff, 0xad);
		Assert.read(out.from, enc.ints());
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public void dtr(boolean on) throws IOException {
		boolean rts = this.rts.value();
		boolean dtr = this.dtr.value();
		if (!reset && rts && !dtr && on) bit(true);
		this.dtr.accept(on, ExceptionAdapter.io);
		if (!rts && !on) reset = true;
		if (rts && on) standby();
	}

	@Override
	public void rts(boolean on) throws IOException {
		boolean rts = this.rts.value();
		boolean dtr = this.dtr.value();
		if (!reset && dtr && !rts && on) bit(false);
		this.rts.accept(on, ExceptionAdapter.io);
		if (!dtr && !on) reset = true;
		if (dtr && on) standby();
	}

	@Override
	public void close() throws IOException {
		out.close();
		super.close();
	}

	private void standby() {
		if (!reset) return; // already on standby
		reset = false;
		value = 0;
		bit = 0;
	}

	private void bit(boolean on) throws IOException {
		value |= Bytes.maskOfBitInt(on, Byte.SIZE - 1 - bit++);
		if (bit < Byte.SIZE) return;
		out.write(value);
		value = 0;
		bit = 0;
	}
}
