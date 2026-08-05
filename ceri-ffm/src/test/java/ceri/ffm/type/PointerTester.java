package ceri.ffm.type;

import ceri.ffm.clib.ffm.CUnistd.size_t;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.type.IntType.CLong;

public class PointerTester {

	public static void main(String[] args) {
		var pv = CLong.$.pointerOfAll(true, 1, 2, 3, 4, 5, 6, 7, 8, 9);
		var ps = size_t.$.pointerOfAll(true, 1, 2, 3, 4, 5, 6, 7, 8, 9);
		var pb = Primitive.BYTE.pointerOfAll(true, 1, -1, 2, -2, 3, -3, 4);
		var pi = Primitive.INT.pointerOfAll(true, 1, -1, 2, -2, 3, -3, 4);
		var m = PointerType.Raw.$.allocAll(true, pv, pb, pi);
		var m0 = Pointer.$.allocAll(true, pv, ps);
		FfmTesting.bin(pv);
		FfmTesting.bin(m);
		FfmTesting.bin(m0);
		var pa = Pointer.$.getArray(m, false);
		FfmTesting.bin(pa);
		var cl = pa[0].as(IntType.CLong.$).resize(16).getArray(true);
		FfmTesting.arg(cl);
	}

}
