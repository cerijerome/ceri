package ceri.serial.jna;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.serial.jna.Struct.Fields;

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
			this.array = bytes(array);
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
			return ubyte(count);
		}
	}

	@Test
	public void shouldWriteAndReadArray() {
		ByteVar bv0 = new ByteVar(100, 0x80, 0, 0xff, 0x7f);
		ByteVar bv = JnaTestUtil.ref(bv0, ByteVar::new);
		assertByteVar(bv, 100, 0x80, 0, 0xff, 0x7f);
	}

	@Test
	public void shouldWriteAndReadEmptyArray() {
		ByteVar bv0 = new ByteVar(100);
		ByteVar bv = JnaTestUtil.ref(bv0, ByteVar::new);
		assertByteVar(bv, 100);
	}

	public static void assertByteVar(ByteVar bv, int dummy, int... bytes) {
		assertEquals(bv.dummy, dummy);
		assertEquals(bv.count, (byte) bytes.length);
		assertArray(bv.array, bytes);
	}

}
