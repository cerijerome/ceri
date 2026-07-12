package ceri.ffm.type;

import ceri.common.io.Direction;
import ceri.ffm.test.FfmTesting;

public class StringTypeTester {

	public static void main(String[] args) {
		String[] ss = { "abcdef", "g", "", "hijk", null, "lmnop", "" };
		var s = StringType.UTF16.support(10, true);
		var r = s.encodeAll(Direction.duplex, true, ss);
		var m = r.value();
		FfmTesting.bin(r.value());
		FfmTesting.arg(ss);
		Primitive.CHAR.writeAll(m, 4, false, 'C', 'D', 'E');
		r.resolve();
		FfmTesting.arg(ss);
		// FfmTesting.arg(s.decodeArray(m, 10, false));
	}

}
