package ceri.ffm.type;

import static ceri.ffm.test.FfmTesting.A;
import static ceri.ffm.test.FfmTesting.print;
import ceri.common.array.RawArray;
import ceri.ffm.test.FfmTesting.Gen;
import ceri.ffm.type.Struct.Fields;

public class StructTester {
	private static final Struct.Support<T0> S0 = Struct.support(T0.class);
	private static final Struct.Support<T1> S1 = Struct.support(T1.class);
	private static final Struct.Support<T2> S2 = Struct.support(T2.class);

	@Fields({ "s", "iii", "l" })
	public static class T0 extends Struct<T0> {
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
		System.out.println(S0);
		System.out.println(S0.desc());
		var tt0 = S0.arrayVal(3);
		for (int i = 0; i < tt0.length; i++)
			tt0[i].gen(i);
		var m = S0.allocAll(A, true, tt0);
		print(m);
		var x = S0.getArray(m, false);
		RawArray.deepForEach(x, t -> System.out.println(t));

		var t2 = S2.val(2).gen(0);
		System.out.println(t2);
	}

	public static void testT2() {
		System.out.println(S2);
		System.out.println(S2.desc());

		var t2 = S2.val(3);
		t2.gen(0);
		var m = S2.alloc(A, t2);
		print(m);
	}

	public static void testT1() {
		System.out.println(S1);
		System.out.println(S1.desc());

		var t1 = new T1();
		var m = S1.alloc(A, t1);
		print(m);

		t1 = S1.val();
		m = S1.alloc(A, t1);
		print(m);

		t1.bb = "testing...\0".getBytes();
		m = S1.alloc(A, t1);
		print(m);
	}
}
