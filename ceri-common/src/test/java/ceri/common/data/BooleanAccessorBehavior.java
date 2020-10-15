package ceri.common.data;

import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
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
		assertThat(h.val, is(true));
		assertThat(Holder.acc.get(h), is(true));
		Holder.acc.set(h, false);
		assertThat(h.val, is(false));
		assertThat(Holder.acc.get(h), is(false));
	}

	@Test
	public void shouldAccessIntegerFields() {
		Holder h = new Holder();
		Holder.iAcc.set(h, true);
		assertThat(h.iVal, is(1));
		assertThat(Holder.iAcc.get(h), is(true));
		Holder.iAcc.set(h, false);
		assertThat(h.iVal, is(0));
		assertThat(Holder.iAcc.get(h), is(false));
	}

	@Test
	public void shouldAccessShortFields() {
		Holder h = new Holder();
		Holder.sAcc.set(h, true);
		assertThat(h.sVal, is((short) 1));
		assertThat(Holder.sAcc.get(h), is(true));
		Holder.sAcc.set(h, false);
		assertThat(h.sVal, is((short) 0));
		assertThat(Holder.sAcc.get(h), is(false));
	}

	@Test
	public void shouldAccessByteFields() {
		Holder h = new Holder();
		Holder.bAcc.set(h, true);
		assertThat(h.bVal, is((byte) 1));
		assertThat(Holder.bAcc.get(h), is(true));
		Holder.bAcc.set(h, false);
		assertThat(h.bVal, is((byte) 0));
		assertThat(Holder.bAcc.get(h), is(false));
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
		assertThat(h.val, is(true));
		assertThat(h.iVal, is(1));
		assertThat(h.sVal, is((short) 1));
		assertThat(h.bVal, is((byte) 1));
		assertThat(a.get(), is(true));
		assertThat(i.get(), is(true));
		assertThat(s.get(), is(true));
		assertThat(b.get(), is(true));
	}

}
