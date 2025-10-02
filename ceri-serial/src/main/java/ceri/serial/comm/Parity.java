package ceri.serial.comm;

import ceri.common.data.Xcoder;
import ceri.serial.comm.jna.CSerial;

public enum Parity {
	none(CSerial.PARITY_NONE),
	odd(CSerial.PARITY_ODD),
	even(CSerial.PARITY_EVEN),
	mark(CSerial.PARITY_MARK),
	space(CSerial.PARITY_SPACE);

	private static final Xcoder.Type<Parity> xcoder = Xcoder.type(Parity.class);
	public final int value;

	public static Parity from(char c) {
		return switch (c) {
			case 'n', 'N' -> none;
			case 'o', 'O' -> odd;
			case 'e', 'E' -> even;
			case 'm', 'M' -> mark;
			case 's', 'S' -> space;
			default -> null;
		};
	}

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
