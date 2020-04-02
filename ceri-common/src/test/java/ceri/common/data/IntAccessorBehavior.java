package ceri.common.data;

import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.test.TestUtil;

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
		TestUtil.assertThrown(() -> IntAccessor.of(null, null).get());
		TestUtil.assertThrown(() -> IntAccessor.of(null, null).set(0));
	}

	@Test
	public void shouldFailForNullTypedAccessors() {
		TestUtil.assertThrown(() -> IntAccessor.typed(null, null).get(""));
		TestUtil.assertThrown(() -> IntAccessor.typed(null, null).set("", 0));
	}

	@Test
	public void shouldSetMaskedValues() {
		Holder h = new Holder();
		IntAccessor iAcc = Holder.iAcc.from(h);
		iAcc.set(0x1234);
		iAcc.mask(0xff00).set(0xabcd);
		assertThat(h.iVal, is(0xab34));
	}

	@Test
	public void shouldGetMaskedValues() {
		Holder h = new Holder();
		IntAccessor iAcc = Holder.iAcc.from(h);
		iAcc.set(0x123456);
		assertThat(iAcc.mask(0xff00).get(), is(0x3400));
	}

	@Test
	public void shouldAddValues() {
		Holder h = new Holder();
		IntAccessor iAcc = Holder.iAcc.from(h);
		iAcc.set(0x1234);
		iAcc.add(0x4321);
		assertThat(h.iVal, is(0x5335));
	}

	@Test
	public void shouldRemoveValues() {
		Holder h = new Holder();
		IntAccessor iAcc = Holder.iAcc.from(h);
		iAcc.set(0x1234);
		iAcc.remove(0x4321);
		assertThat(h.iVal, is(0x1014));
	}

	@Test
	public void shouldProvideGetOnlyAccess() {
		int[] x = { 7 };
		IntAccessor accessor = IntAccessor.getter(() -> x[0]);
		assertThat(accessor.get(), is(7));
		assertThrown(() -> accessor.set(0));
	}
	
	@Test
	public void shouldProvideSetOnlyAccess() {
		int[] x = { 7 };
		IntAccessor accessor = IntAccessor.setter(i -> x[0] = i);
		accessor.set(0);
		assertThat(x[0], is(0));
		assertThrown(() -> accessor.get());
	}
	
	@Test
	public void shouldSetMaskedTypeValues() {
		Holder h = new Holder();
		Holder.iAcc.set(h, 0x1234);
		Holder.iAcc.mask(0xff00).set(h, 0xabcd);
		assertThat(h.iVal, is(0xab34));
	}

	@Test
	public void shouldGetMaskedTypeValues() {
		Holder h = new Holder();
		Holder.iAcc.set(h, 0x123456);
		assertThat(Holder.iAcc.mask(0xff00).get(h), is(0x3400));
	}

	@Test
	public void shouldAccessIntegerFields() {
		Holder h = new Holder();
		Holder.iAcc.set(h, 0xff);
		assertThat(h.iVal, is(0xff));
		assertThat(Holder.iAcc.get(h), is(0xff));
		IntAccessor.Typed<Holder> iAcc = IntAccessor.typed(t -> t.iVal, null);
		assertThat(iAcc.get(h), is(0xff));
	}

	@Test
	public void shouldAccessShortFields() {
		Holder h = new Holder();
		Holder.sAcc.set(h, 0xffff);
		assertThat(h.sVal, is((short) -1));
		assertThat(Holder.sAcc.get(h), is(0xffff));
		IntAccessor.Typed<Holder> sAcc = IntAccessor.typedUshort(t -> t.sVal, null);
		assertThat(sAcc.get(h), is(0xffff));
	}

	@Test
	public void shouldAccessByteFields() {
		Holder h = new Holder();
		Holder.bAcc.set(h, 0xff);
		assertThat(h.bVal, is((byte) -1));
		assertThat(Holder.bAcc.get(h), is(0xff));
		IntAccessor.Typed<Holder> bAcc = IntAccessor.typedUbyte(t -> t.bVal, null);
		assertThat(bAcc.get(h), is(0xff));
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
		assertThat(h.iVal, is(0xffffff));
		assertThat(h.sVal, is((short) -1));
		assertThat(h.bVal, is((byte) -1));
		assertThat(i.get(), is(0xffffff));
		assertThat(s.get(), is(0xffff));
		assertThat(b.get(), is(0xff));
	}

}
