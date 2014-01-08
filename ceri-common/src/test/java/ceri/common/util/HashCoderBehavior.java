package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class HashCoderBehavior {
	private static final byte b = 0x77;
	private static final short s = 0x7777;
	private static final int i = 0x7777_7777;
	private static final long l = 0x7777_7777_7777_7777L;
	
	@Test
	public void shouldHaveDifferentValuesByPrimitiveType() {
		int bHash = HashCoder.create().add(b).hashCode();
		int sHash = HashCoder.create().add(s).hashCode();
		int iHash = HashCoder.create().add(i).hashCode();
		int lHash = HashCoder.create().add(l).hashCode();
		assertThat(bHash, not(sHash));
		assertThat(sHash, not(iHash));
		assertThat(iHash, not(lHash));
		assertThat(lHash, not(bHash));
	}
	
	@Test
	public void shouldHaveSameValueForAutoboxedPrimitives() {
		int autoBoxHash = HashCoder.hash(b, s, i, l);
		int primitiveHash = HashCoder.create().add(b).add(s).add(i).add(l).hashCode();
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
