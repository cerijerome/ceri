package ceri.common.data;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;

public class BooleanAccessorBehavior {

	static class Holder {
		static final BooleanAccessor.Typed<Holder> acc =
			BooleanAccessor.typed(h -> h.val, (h, v) -> h.val = v);
		static final BooleanAccessor.Typed<Holder> iAcc =
			BooleanAccessor.typedInt(h -> h.iVal, (h, i) -> h.iVal = i);
		static final BooleanAccessor.Typed<Holder> sAcc =
			BooleanAccessor.typedShort(h -> h.sVal, (h, s) -> h.sVal = s);
		static final BooleanAccessor.Typed<Holder> bAcc =
			BooleanAccessor.typedByte(h -> h.bVal, (h, b) -> h.bVal = b);

		boolean val = false;
		int iVal = 0;
		short sVal = 0;
		byte bVal = 0;
	}

	@Test
	public void shouldAccessBooleanFields() {
		Holder h = new Holder();
		Holder.acc.set(h, true);
		assertTrue(h.val);
		assertTrue(Holder.acc.get(h));
		Holder.acc.set(h, false);
		assertFalse(h.val);
		assertFalse(Holder.acc.get(h));
	}

	@Test
	public void shouldAccessIntegerFields() {
		Holder h = new Holder();
		Holder.iAcc.set(h, true);
		assertEquals(h.iVal, 1);
		assertTrue(Holder.iAcc.get(h));
		Holder.iAcc.set(h, false);
		assertEquals(h.iVal, 0);
		assertFalse(Holder.iAcc.get(h));
	}

	@Test
	public void shouldAccessShortFields() {
		Holder h = new Holder();
		Holder.sAcc.set(h, true);
		assertEquals(h.sVal, (short) 1);
		assertTrue(Holder.sAcc.get(h));
		Holder.sAcc.set(h, false);
		assertEquals(h.sVal, (short) 0);
		assertFalse(Holder.sAcc.get(h));
	}

	@Test
	public void shouldAccessByteFields() {
		Holder h = new Holder();
		Holder.bAcc.set(h, true);
		assertEquals(h.bVal, (byte) 1);
		assertTrue(Holder.bAcc.get(h));
		Holder.bAcc.set(h, false);
		assertEquals(h.bVal, (byte) 0);
		assertFalse(Holder.bAcc.get(h));
	}

	@Test
	public void shouldProvideInstanceAccessorFromTypedAccessor() {
		Holder h = new Holder();
		BooleanAccessor a = Holder.acc.from(h);
		BooleanAccessor i = Holder.iAcc.from(h);
		BooleanAccessor s = Holder.sAcc.from(h);
		BooleanAccessor b = Holder.bAcc.from(h);
		a.set(true);
		i.set(true);
		s.set(true);
		b.set(true);
		assertTrue(h.val);
		assertEquals(h.iVal, 1);
		assertEquals(h.sVal, (short) 1);
		assertEquals(h.bVal, (byte) 1);
		assertTrue(a.get());
		assertTrue(i.get());
		assertTrue(s.get());
		assertTrue(b.get());
	}

}
