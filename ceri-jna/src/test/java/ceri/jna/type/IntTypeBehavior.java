package ceri.jna.type;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.util.CloseableUtil;
import ceri.jna.util.GcMemory;

public class IntTypeBehavior {
	private Memory m = null;

	@SuppressWarnings("serial")
	private static class U8 extends IntType<U8> {
		public U8() {
			super(1, 0, true);
		}
	}

	@SuppressWarnings("serial")
	private static class U16 extends IntType<U16> {
		public U16() {
			super(2, 0, true);
		}
	}

	@SuppressWarnings("serial")
	private static class U32 extends IntType<U32> {
		public static class ByRef extends IntType.ByRef<U32> {
			public ByRef() {
				super(U32::new);
			}

			public ByRef(U32 u32) {
				super(U32::new, u32);
			}

			public ByRef(Pointer p) {
				super(U32::new, p);
			}
		}

		public U32() {
			this(0);
		}

		public U32(int value) {
			super(4, value, true);
		}
	}

	@SuppressWarnings("serial")
	private static class U64 extends IntType<U64> {
		public U64() {
			super(8, 0, true);
		}
	}

	@After
	public void after() {
		CloseableUtil.close(m);
		m = null;
	}

	@Test
	public void shouldProvideStringRepresentation() {
		var ref = new U32.ByRef();
		ref.setValue(Integer.MIN_VALUE);
		assertFind(ref, "-214.*\\|0x80000000@.*\\+4");
	}

	@Test
	public void shouldSetDefaultRef() {
		var ref = new U32.ByRef((U32) null);
		assertEquals(ref.getValue(), new U32());
	}

	@Test
	public void shouldSetRefPointer() {
		var ref0 = new U32.ByRef();
		var ref = new U32.ByRef(ref0.getPointer());
		ref0.setValue(111);
		assertEquals(ref.getValue(), new U32(111));
	}

	@Test
	public void shouldGetLongValue() {
		assertEquals(IntType.get(null), null);
		assertEquals(IntType.get(new U32(-1)), 0xffffffffL);
	}

	@Test
	public void shouldReadIntoValue() {
		var m = GcMemory.malloc(5);
		assertEquals(IntType.write(new U32(111), m.m, 1), new U32(111));
		var val = new U32();
		assertEquals(IntType.readInto(val, m.m, 1), new U32(111));
		assertEquals(val, new U32(111));
	}

	@Test
	public void shouldIgnoreNullType() {
		m = new Memory(8);
		assertEquals(IntType.set(null, 100), null);
		assertEquals(IntType.readInto(null, null), null);
		assertEquals(IntType.readInto(null, null, 0L), null);
		assertEquals(IntType.write(null, null), null);
		assertEquals(IntType.write(null, null, 0L), null);
	}

	@Test
	public void shouldApplyOperator() {
		var u16 = new U16();
		assertEquals(u16.apply(l -> l - 1).number(), (short) 0xffff);
	}

	@Test
	public void shouldReadFromPointer() {
		m = new Memory(8);
		IntType.set(new U8(), 0x88).write(m, 0);
		assertEquals(new U8().read(m, 0).number(), (byte) 0x88);
		IntType.set(new U16(), 0x9999).write(m, 0);
		assertEquals(new U16().read(m, 0).number(), (short) 0x9999);
		IntType.set(new U32(), 0xaaaaaaaa).write(m, 0);
		assertEquals(new U32().read(m, 0).number(), 0xaaaaaaaa);
		IntType.set(new U64(), 0xbbbbbbbb_bbbbbbbbL).write(m, 0);
		assertEquals(new U64().read(m, 0).number(), 0xbbbbbbbb_bbbbbbbbL);
	}
}
