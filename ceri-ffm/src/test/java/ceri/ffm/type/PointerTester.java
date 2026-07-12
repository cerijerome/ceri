package ceri.ffm.type;

import ceri.ffm.test.FfmTesting;

public class PointerTester {

	public static void main(String[] args) {
		var pv = Pointer.of(IntType.CLong.$.allocAll(true, 1, 2, 3, 4, 5, 6, 7, 8, 9));
		var ps = Pointer.of(IntType.size_t.$.allocAll(true, 1, 2, 3, 4, 5, 6, 7, 8, 9));
		var pb = Pointer.ofBytes(true, 1, -1, 2, -2, 3, -3, 4);
		var pi = Pointer.ofInts(true, 1, -1, 2, -2, 3, -3, 4);
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
