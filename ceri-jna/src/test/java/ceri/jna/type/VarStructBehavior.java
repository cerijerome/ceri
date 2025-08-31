package ceri.jna.type;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.array.ArrayUtil;
import ceri.common.function.Functions;
import ceri.common.math.MathUtil;
import ceri.jna.type.Struct.Fields;

public class VarStructBehavior {

	@Fields({ "dummy", "count", "array" })
	public static class ByteVar extends VarStruct {
		public int dummy;
		public byte count;
		public byte[] array = new byte[0];

		public static class ByRef extends ByteVar implements Structure.ByReference {
			public ByRef(int dummy, int... array) {
				super(dummy, array);
			}

			public ByRef(Pointer p) {
				super(p);
			}
		}

		public ByteVar(int dummy, int... array) {
			this.dummy = dummy;
			this.count = (byte) array.length;
			this.array = ArrayUtil.bytes.of(array);
		}

		public ByteVar(Pointer p) {
			super(p);
		}

		@Override
		protected void setVarArray(int count) {
			array = new byte[count];
		}

		@Override
		protected int varCount() {
			return MathUtil.ubyte(count);
		}
	}

	@Test
	public void shouldWriteAndReadArray() {
		ByteVar bv0 = new ByteVar(100, 0x80, 0, 0xff, 0x7f);
		ByteVar bv = deref(bv0, ByteVar::new);
		assertByteVar(bv, 100, 0x80, 0, 0xff, 0x7f);
	}

	@Test
	public void shouldWriteAndReadEmptyArray() {
		ByteVar bv0 = new ByteVar(100);
		ByteVar bv = deref(bv0, ByteVar::new);
		assertByteVar(bv, 100);
	}

	public static void assertByteVar(ByteVar bv, int dummy, int... bytes) {
		assertEquals(bv.dummy, dummy);
		assertEquals(bv.count, (byte) bytes.length);
		assertArray(bv.array, bytes);
	}

	private static <T extends Structure> T deref(T t, Functions.Function<Pointer, T> constructor) {
		return Struct.adapt(Struct.write(t), constructor);
	}
}
