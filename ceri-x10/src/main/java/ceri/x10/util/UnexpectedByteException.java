package ceri.x10.util;

import java.io.IOException;

/**
 * Exception when waiting for a specific byte input but a different value is received.
 */
public class UnexpectedByteException extends IOException {
	private static final long serialVersionUID = -1397888268813085217L;
	public final byte actual;
	public final byte expected;

	public UnexpectedByteException(int expected, int actual) {
		super("Expected 0x" + Integer.toHexString(expected & 0xff) + " but found 0x" +
			Integer.toHexString(actual & 0xff));
		this.expected = (byte) expected;
		this.actual = (byte) actual;
	}

	public UnexpectedByteException(int actual) {
		super("Unexpected byte found 0x" + Integer.toHexString(actual & 0xff));
		this.expected = 0;
		this.actual = (byte) actual;
	}

}
