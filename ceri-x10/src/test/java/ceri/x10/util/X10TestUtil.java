package ceri.x10.util;

import java.io.IOException;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.x10.command.Address;

public class X10TestUtil {

	private X10TestUtil() {}

	public static enum ErrorType {
		none,
		rt,
		rti,
		io;
	}

	public static void error(ErrorType type) throws IOException {
		if (type == ErrorType.rt) throw new RuntimeException("generated error");
		if (type == ErrorType.rti) throw new RuntimeInterruptedException("generated error");
		if (type == ErrorType.io) throw new IOException("generated error");
	}

	public static Address addr(String address) {
		return Address.from(address);
	}

}
