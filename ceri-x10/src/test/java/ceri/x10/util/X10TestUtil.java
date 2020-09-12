package ceri.x10.util;

import ceri.x10.command.Address;

public class X10TestUtil {

	private X10TestUtil() {}

	public static Address addr(String address) {
		return Address.from(address);
	}

}
