package ceri.jna.util;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.jna.test.JnaTestUtil.mem;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.jna.util.Struct.Fields;

public class UnionBehavior {

	@Fields({ "i", "p", "bb", "ss", "ii", "ll" })
	public static class TestUnion extends Union {
		public int i;
		public Pointer p;
		public byte[] bb = new byte[9];
		public short[] ss = new short[5];
		public int[] ii = new int[3];
		public long[] ll = new long[2];

		public TestUnion(int i, Pointer p, int... bytes) {
			this.i = i;
			this.p = p;
			for (int k = 0; k < Math.min(bb.length, bytes.length); k++)
				bb[k] = (byte) bytes[k];
		}

		public TestUnion(Pointer p) {
			super(p);
		}
	}

	@Test
	public void testTypeForNullUnion() {
		assertEquals(Union.type(null, "x"), null);
	}

	@Test
	public void testSetUnionTypeByName() {
		var t0 = new TestUnion(123, mem(5, 6, 7).m, -1, -2, -3, -4);
		var p = Struct.write(Union.type(t0, "i")).getPointer();
		var t = Struct.read(new TestUnion(p));
		assertEquals(t.i, 123);
	}

	@Test
	public void testReadTypedField() {
		var t = new TestUnion(123, mem(5, 6, 7).m, -1, -2, -3, -4);
		t.writeField("i");
		assertEquals(Struct.readField(null, "i"), null);
		assertEquals(Struct.readField(t, "i"), 123);
		t.writeField("bb");
		assertArray(Struct.<byte[]>readField(t, "bb"), -1, -2, -3, -4, 0, 0, 0, 0, 0);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		String s = new TestUnion(123, mem(5, 6, 7).m, -1, -2, -3).toString();
		assertFind(s, "(?s)TestUnion.*" //
			+ "\\Q+00: int i = 123\\E.*" //
			+ "\\Q+00: Pointer p = \\E.*" //
			+ "\\Q+00: byte[] bb = [\\E.*" //
			+ "\\Q+00: short[] ss = [\\E.*" //
			+ "\\Q+00: int[] ii = [\\E.*" //
			+ "\\Q+00: long[] ll = [\\E.*");
	}
}
