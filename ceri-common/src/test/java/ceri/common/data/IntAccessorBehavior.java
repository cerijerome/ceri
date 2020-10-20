package ceri.common.data;

import static ceri.common.data.MaskTranscoder.mask;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;

public class IntAccessorBehavior {

	static class Holder {
		static final IntAccessor.Typed<Holder> iAcc =
			IntAccessor.typed(h -> h.iVal, (h, i) -> h.iVal = i);
		static final IntAccessor.Typed<Holder> sAcc =
			IntAccessor.typedUshort(h -> h.sVal, (h, s) -> h.sVal = s);
		static final IntAccessor.Typed<Holder> bAcc =
			IntAccessor.typedUbyte(h -> h.bVal, (h, b) -> h.bVal = b);

		int iVal = 0;
		short sVal = 0;
		byte bVal = 0;
	}

	@Test
	public void shouldFailForNullAccessors() {
		assertThrown(() -> IntAccessor.of(null, null).get());
		assertThrown(() -> IntAccessor.of(null, null).set(0));
	}

	@Test
	public void shouldFailForNullTypedAccessors() {
		assertThrown(() -> IntAccessor.typed(null, null).get(""));
		assertThrown(() -> IntAccessor.typed(null, null).set("", 0));
	}

	@Test
	public void shouldSetMaskedValues() {
		Holder h = new Holder();
		IntAccessor iAcc = Holder.iAcc.from(h);
		iAcc.set(0x1234);
		iAcc.mask(0xff00).set(0xabcd);
		assertEquals(h.iVal, 0xab34);
	}

	@Test
	public void shouldGetMaskedValues() {
		Holder h = new Holder();
		IntAccessor iAcc = Holder.iAcc.from(h);
		iAcc.set(0x123456);
		assertEquals(iAcc.mask(0xff00).get(), 0x3400);
	}

	@Test
	public void shouldAddValues() {
		Holder h = new Holder();
		IntAccessor iAcc = Holder.iAcc.from(h);
		iAcc.set(0x1234);
		iAcc.add(0x4321);
		assertEquals(h.iVal, 0x5335);
	}

	@Test
	public void shouldRemoveValues() {
		Holder h = new Holder();
		IntAccessor iAcc = Holder.iAcc.from(h);
		iAcc.set(0x1234);
		iAcc.remove(0x4321);
		assertEquals(h.iVal, 0x1014);
	}

	@Test
	public void shouldProvideGetOnlyAccess() {
		int[] x = { 7 };
		IntAccessor accessor = IntAccessor.getter(() -> x[0]);
		assertEquals(accessor.get(), 7);
		assertThrown(() -> accessor.set(0));
	}

	@Test
	public void shouldProvideSetOnlyAccess() {
		int[] x = { 7 };
		IntAccessor accessor = IntAccessor.setter(i -> x[0] = i);
		accessor.set(0);
		assertEquals(x[0], 0);
		assertThrown(() -> accessor.get());
	}

	@Test
	public void shouldAddTypedValues() {
		Holder h = new Holder();
		Holder.iAcc.set(h, 0x1200);
		assertEquals(h.iVal, 0x1200);
		Holder.iAcc.add(h, 0x0034);
		assertEquals(h.iVal, 0x1234);
	}

	@Test
	public void shouldRemoveTypedValues() {
		Holder h = new Holder();
		Holder.iAcc.set(h, 0x1234);
		assertEquals(h.iVal, 0x1234);
		Holder.iAcc.remove(h, 0x0034);
		assertEquals(h.iVal, 0x1200);
	}

	@Test
	public void shouldSetMaskedTypeValues() {
		Holder h = new Holder();
		Holder.iAcc.set(h, 0x1234);
		Holder.iAcc.mask(mask(0xff00, 0)).set(h, 0xabcd);
		assertEquals(h.iVal, 0xab34);
	}

	@Test
	public void shouldGetMaskedTypeValues() {
		Holder h = new Holder();
		Holder.iAcc.set(h, 0x123456);
		assertEquals(Holder.iAcc.mask(mask(0xff00, 0)).get(h), 0x3400);
	}

	@Test
	public void shouldAccessIntegerFields() {
		Holder h = new Holder();
		Holder.iAcc.set(h, 0xff);
		assertEquals(h.iVal, 0xff);
		assertEquals(Holder.iAcc.get(h), 0xff);
		IntAccessor.Typed<Holder> iAcc = IntAccessor.typed(t -> t.iVal, null);
		assertEquals(iAcc.get(h), 0xff);
	}

	@Test
	public void shouldAccessShortFields() {
		Holder h = new Holder();
		Holder.sAcc.set(h, 0xffff);
		assertEquals(h.sVal, (short) -1);
		assertEquals(Holder.sAcc.get(h), 0xffff);
		IntAccessor.Typed<Holder> sAcc = IntAccessor.typedUshort(t -> t.sVal, null);
		assertEquals(sAcc.get(h), 0xffff);
	}

	@Test
	public void shouldAccessByteFields() {
		Holder h = new Holder();
		Holder.bAcc.set(h, 0xff);
		assertEquals(h.bVal, (byte) -1);
		assertEquals(Holder.bAcc.get(h), 0xff);
		IntAccessor.Typed<Holder> bAcc = IntAccessor.typedUbyte(t -> t.bVal, null);
		assertEquals(bAcc.get(h), 0xff);
	}

	@Test
	public void shouldProvideInstanceAccessorFromTypedAccessor() {
		Holder h = new Holder();
		IntAccessor i = Holder.iAcc.from(h);
		IntAccessor s = Holder.sAcc.from(h);
		IntAccessor b = Holder.bAcc.from(h);
		i.set(0xffffff);
		s.set(0);
		s.set(0xffff);
		assertThrown(() -> s.set(0x10000));
		assertThrown(() -> s.set(-1));
		b.set(0);
		b.set(0xff);
		assertThrown(() -> b.set(0x100));
		assertThrown(() -> b.set(-1));
		assertEquals(h.iVal, 0xffffff);
		assertEquals(h.sVal, (short) -1);
		assertEquals(h.bVal, (byte) -1);
		assertEquals(i.get(), 0xffffff);
		assertEquals(s.get(), 0xffff);
		assertEquals(b.get(), 0xff);
	}

}
