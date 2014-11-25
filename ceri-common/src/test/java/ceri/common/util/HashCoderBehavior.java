package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class HashCoderBehavior {
	private static final boolean bl = true;
	private static final boolean bl2 = false;
	private static final char c = '7';
	private static final byte b = 0x77;
	private static final short s = 0x7777;
	private static final int i = 0x7777_7777;
	private static final long l = 0x7777_7777_7777_7777L;
	private static final float f = 0.7777f;
	private static final double d = 0.7777_7777;

	@Test
	public void shouldHaveDifferentValuesByPrimitiveType() {
		int blHash = HashCoder.create().add(bl).hashCode();
		int cHash = HashCoder.create().add(c).hashCode();
		int bHash = HashCoder.create().add(b).hashCode();
		int sHash = HashCoder.create().add(s).hashCode();
		int iHash = HashCoder.create().add(i).hashCode();
		int lHash = HashCoder.create().add(l).hashCode();
		int fHash = HashCoder.create().add(f).hashCode();
		int dHash = HashCoder.create().add(d).hashCode();
		int nHash = HashCoder.create().add((Object)null).hashCode();
		assertThat(blHash, not(cHash));
		assertThat(cHash, not(bHash));
		assertThat(bHash, not(sHash));
		assertThat(sHash, not(iHash));
		assertThat(iHash, not(lHash));
		assertThat(lHash, not(fHash));
		assertThat(fHash, not(dHash));
		assertThat(dHash, not(nHash));
		assertThat(nHash, not(blHash));
	}

	@Test
	public void shouldHaveSameValueForAutoboxedPrimitives() {
		int autoBoxHash = HashCoder.hash(bl2, c, b, s, i, l, f, d);
		int primitiveHash =
			HashCoder.create().add(bl2).add(c).add(b).add(s).add(i).add(l).add(f).add(d).hashCode();
		assertThat(autoBoxHash, is(primitiveHash));
	}

	@Test
	public void shouldIterateOverArray() {
		int[] values = { 1, 2, 3 };
		int primitiveHash = HashCoder.hash(1, 2, 3);
		int arrayHash = HashCoder.create().add(values).hashCode();
		assertThat(primitiveHash, is(arrayHash));
	}

}
