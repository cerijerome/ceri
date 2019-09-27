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
			IntAccessor.typedShort(h -> h.sVal, (h, s) -> h.sVal = s);
		static final IntAccessor.Typed<Holder> bAcc =
			IntAccessor.typedByte(h -> h.bVal, (h, b) -> h.bVal = b);

		int iVal = 0;
		short sVal = 0;
		byte bVal = 0;
	}

	@Test
	public void shouldFailForNullAccessors() {
		TestUtil.assertThrown(() -> IntAccessor.of(null).get());
		TestUtil.assertThrown(() -> IntAccessor.of(null, null).set(0));
	}

	@Test
	public void shouldFailForNullTypedAccessors() {
		TestUtil.assertThrown(() -> IntAccessor.typed(null).get(""));
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
		IntAccessor.Typed<Holder> iAcc = IntAccessor.typed(t -> t.iVal);
		assertThat(iAcc.get(h), is(0xff));
	}

	@Test
	public void shouldAccessShortFields() {
		Holder h = new Holder();
		Holder.sAcc.set(h, 0xffffff);
		assertThat(h.sVal, is((short) -1));
		assertThat(Holder.sAcc.get(h), is(0xffff));
		IntAccessor.Typed<Holder> sAcc = IntAccessor.typedShort(t -> t.sVal);
		assertThat(sAcc.get(h), is(0xffff));
	}

	@Test
	public void shouldAccessByteFields() {
		Holder h = new Holder();
		Holder.bAcc.set(h, 0xffffff);
		assertThat(h.bVal, is((byte) -1));
		assertThat(Holder.bAcc.get(h), is(0xff));
		IntAccessor.Typed<Holder> bAcc = IntAccessor.typedByte(t -> t.bVal);
		assertThat(bAcc.get(h), is(0xff));
	}

	@Test
	public void shouldProvideInstanceAccessorFromTypedAccessor() {
		Holder h = new Holder();
		IntAccessor i = Holder.iAcc.from(h);
		IntAccessor s = Holder.sAcc.from(h);
		IntAccessor b = Holder.bAcc.from(h);
		i.set(0xffffff);
		s.set(0xffffff);
		b.set(0xffffff);
		assertThat(h.iVal, is(0xffffff));
		assertThat(h.sVal, is((short) -1));
		assertThat(h.bVal, is((byte) -1));
		assertThat(i.get(), is(0xffffff));
		assertThat(s.get(), is(0xffff));
		assertThat(b.get(), is(0xff));
	}

}
