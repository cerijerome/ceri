package ceri.ffm.type;

import ceri.common.io.Buffers;
import ceri.common.io.Direction;
import ceri.ffm.test.FfmTesting;

public class BufferTypeTester {

	public static void main(String[] args) {
		var s = BufferType.INT.support(5, true);
		FfmTesting.bin(s.alloc(Buffers.INT.of(1, 2, 3)));
		var r = s.encodeAll(Direction.duplex, true, Buffers.INT.of(1, 2, 3), Buffers.INT.of(4, 5));
		FfmTesting.bin(r.value());
		var a = s.asArray(4, true).decode(r.value());
		FfmTesting.arg(a);
	}

}
