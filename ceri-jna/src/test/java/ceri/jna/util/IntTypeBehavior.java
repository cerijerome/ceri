package ceri.jna.util;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.ptr.ByReference;
import ceri.common.util.CloseableUtil;

public class IntTypeBehavior {
	private Memory m = null;

	@SuppressWarnings("serial")
	private static class U8 extends IntType {
		public U8() {
			super(1, 0, true);
		}
	}

	@SuppressWarnings("serial")
	private static class U16 extends IntType {
		public U16() {
			super(2, 0, true);
		}
	}

	@SuppressWarnings("serial")
	private static class U32 extends IntType {
		public static class ByRef extends ByReference {
			public ByRef() {
				super(4);
			}
		}

		public U32() {
			super(4, 0, true);
		}
	}

	@SuppressWarnings("serial")
	private static class U64 extends IntType {
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
	public void shouldIgnoreNullType() {
		m = new Memory(8);
		assertEquals(IntType.set(null, 100), null);
		assertEquals(IntType.read(m, 0, null), null);
		assertEquals(IntType.read(m, 0, () -> null), null);
		assertEquals(IntType.write(m, 0, null), null);
	}

	@Test
	public void shouldApplyOperator() {
		var u16 = new U16();
		assertEquals(u16.apply(l -> l - 1).number(), (short) 0xffff);
	}

	@Test
	public void shouldReadFromPointerType() {
		m = new Memory(8);
		var ref = PointerUtil.set(new U32.ByRef(), m);
		IntType.write(ref, IntType.set(new U32(), 0x80000001));
		var u32 = IntType.read(ref, U32::new);
		assertEquals(u32.longValue(), 0x80000001L);
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
