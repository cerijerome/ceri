package ceri.ffm.type;

import static ceri.ffm.test.FfmTesting.A;
import ceri.common.array.RawArray;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.test.FfmTesting.Gen;
import ceri.ffm.type.Group.Fields;

public class StructTester {
	
	@Fields({ "s", "iii", "l" })
	public static class T0 extends Struct<T0> {
		public static final Struct.Support<T0> $ = Struct.support(T0.class);
		public short s;
		public int[][] iii = new int[2][2];
		public long l;
		// public @Dims(8) String str;

		public T0 gen(int n) {
			s = Gen.s('a', n);
			iii[0][0] = Gen.i('A', n);
			iii[0][1] = Gen.i('A', n + 1);
			iii[1][0] = Gen.i('A', n + 2);
			iii[1][1] = Gen.i('A', n + 3);
			l = Gen.l('f', n);
			return this;
		}
	}

	@Fields({ "t0", "i", "bb" })
	public static class T1 extends Struct<T1> {
		public static final Struct.Support<T1> $ = Struct.support(T1.class);
		public T0[] t0 = new T0[2];
		public int i;
		public byte[] bb;

		public T1 gen(int n) {
			t0[0].gen(n);
			t0[1].gen(n + 1);
			i = Gen.i('M', n);
			for (int j = 0; j < bb.length; j++)
				bb[j] = Gen.b('m', n + j);
			return this;
		}
	}

	@Fields({ "t0", "i", "tt" })
	public static class T2 extends Struct<T2> {
		public static final Struct.Support<T2> $ = Struct.support(T2.class);
		public T0 t0;
		public int i;
		public T0[] tt;

		public T2 gen(int n) {
			t0.gen(n);
			i = Gen.i('M', n);
			for (int j = 0; j < tt.length; j++)
				tt[j].gen(n + j + 1);
			return this;
		}
	}

	public static void main(String[] args) {
		testT0();
		testT1();
		testT2();
	}

	public static void testT0() {
		FfmTesting.out(T0.$);
		var tt0 = T0.$.arrayInit(3);
		for (int i = 0; i < tt0.length; i++)
			tt0[i].gen(i);
		var m = T0.$.allocAll(A, true, tt0);
		FfmTesting.bin(m);
		var x = T0.$.getArray(m, false);
		RawArray.deepForEach(x, t -> System.out.println(t));

		var t2 = T2.$.init(2).gen(0);
		FfmTesting.out(t2);
	}

	public static void testT2() {
		FfmTesting.out(T2.$);

		var t2 = T2.$.init(3);
		t2.gen(0);
		var m = T2.$.alloc(A, t2);
		FfmTesting.bin(m);
	}

	public static void testT1() {
		FfmTesting.out(T1.$);

		var t1 = new T1();
		var m = T1.$.alloc(A, t1);
		FfmTesting.bin(m);

		t1 = T1.$.val();
		m = T1.$.alloc(A, t1);
		FfmTesting.bin(m);

		t1.bb = "testing...\0".getBytes();
		m = T1.$.alloc(A, t1);
		FfmTesting.bin(m);
	}
}
