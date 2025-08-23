package ceri.x10.util;

public class X10Util {
	private static final int NYBBLE_BITS = 4;
	private static final int NYBBLE_MASK = 0xf;
	public static final int DIM_MAX_PERCENT = 100;

	private X10Util() {}

	// TODO: move to ByteUtil

	public static int fromNybble(int value, int nybble) {
		return (value >>> (nybble * NYBBLE_BITS)) & NYBBLE_MASK;
	}

	public static int toNybble(int value, int nybble) {
		return (value & NYBBLE_MASK) << (nybble * NYBBLE_BITS);
	}

	public static int octet(int n1, int n0) {
		return toNybble(n1, 1) | toNybble(n0, 0);
	}
}
