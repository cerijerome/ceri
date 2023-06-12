package ceri.serial.comm;

import ceri.common.data.TypeTranscoder;
import ceri.serial.comm.jna.CSerial;

public enum Parity {
	none(CSerial.PARITY_NONE),
	odd(CSerial.PARITY_ODD),
	even(CSerial.PARITY_EVEN),
	mark(CSerial.PARITY_MARK),
	space(CSerial.PARITY_SPACE);

	private static final TypeTranscoder<Parity> xcoder =
		TypeTranscoder.of(t -> t.value, Parity.class);
	public final int value;

	public static Parity from(int value) {
		return xcoder.decode(value);
	}

	private Parity(int value) {
		this.value = value;
	}
	
	public int bits() {
		return this == none ? 0 : 1;
	}
}
