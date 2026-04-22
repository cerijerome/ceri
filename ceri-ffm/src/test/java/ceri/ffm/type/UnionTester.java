package ceri.ffm.type;

import ceri.ffm.core.Segments;
import ceri.ffm.reflect.Refine.Align;
import ceri.ffm.reflect.Refine.Packed;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.type.Group.Fields;

public class UnionTester {

	@Fields({ "s", "b" })
	@Packed
	public static class S0 extends Struct<S0> {
		public static final Struct.Support<S0> $ = Struct.support(S0.class);
		public short s;
		public byte b;
	}

	@Fields({ "l", "s", "s0" })
	@Packed
	public static class U0 extends Union<U0> {
		public static final Union.Support<U0> $ = Union.support(U0.class);
		public long l;
		public short s;
		public S0[] s0 = new S0[2];
	}

	@Fields({ "s", "b", "u0" })
	@Packed
	public static class S1 extends Struct<S1> {
		public static final Struct.Support<S1> $ = Struct.support(S1.class);
		public short s;
		public byte b;
		public @Align(2) U0 u0;
	}

	public static void main(String[] args) {
		FfmTesting.out(S1.$);
		FfmTesting.out(U0.$);
		FfmTesting.out(S0.$);

		S1 s = S1.$.init();
		s.u0.l = 0xfedcba9876543210L;
		FfmTesting.out(s);

		var m = S1.$.alloc(Segments.auto(), s);
		FfmTesting.bin(m);
		FfmTesting.out(s);

		s.u0.active(2).read();
		FfmTesting.out(s);

		s.u0.s0[0].s = (short) 0xfedc;
		s.u0.s0[0].b = (byte) 0xba;
		s.u0.s0[1].s = (short) 0x9876;
		s.u0.s0[1].b = (byte) 0x54;

		s.u0.write().active(0).read();
		FfmTesting.out(s);
		FfmTesting.bin(m);
	}
}
