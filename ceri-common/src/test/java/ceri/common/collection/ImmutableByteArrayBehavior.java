package ceri.common.collection;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ImmutableByteArrayBehavior {
	private final byte[] b = { 0, -1, 1, Byte.MAX_VALUE, Byte.MIN_VALUE }; // Don't overwrite!
	
	
	@Test
	public void shouldFollowTheEqualsContract() {
		ImmutableByteArray ba0 = ImmutableByteArray.wrap((byte)-1, (byte)1, Byte.MAX_VALUE);
		ImmutableByteArray ba1 = ImmutableByteArray.wrap(b, 1, 3);
		ImmutableByteArray ba2 = ImmutableByteArray.copyOf(b, 1, 3);
		ImmutableByteArray ba3 = ImmutableByteArray.wrap(b).slice(1, 3);
		ImmutableByteArray ba4 = ImmutableByteArray.wrap(b, 0, 3);
		ImmutableByteArray ba5 = ImmutableByteArray.wrap(b, 1, 2);
		ImmutableByteArray ba6 = ImmutableByteArray.wrap(b, 1, 0);
		ImmutableByteArray ba7 = ImmutableByteArray.wrap(b, 5, 0);
		assertThat(ba0, is(ba1));
		assertThat(ba1, is(ba2));
		assertThat(ba1, is(ba3));
		assertThat(ba3, is(ba2));
		assertThat(ba1, is(not(b)));
		assertThat(ba1, is(not(ba4)));
		assertThat(ba1, is(not(ba5)));
		assertThat(ba1, is(not(ba6)));
		assertThat(ba6, is(ba7));
		assertThat(ba0.hashCode(), is(ba1.hashCode()));
		assertThat(ba2.hashCode(), is(ba3.hashCode()));
		assertThat(ba3.toString(), is(ba1.toString()));
	}

	@Test
	public void shouldCopyTheByteArray() {
		ImmutableByteArray ba = ImmutableByteArray.wrap(b);
		assertArray(ba.copy(3), Byte.MAX_VALUE, Byte.MIN_VALUE);
		assertArray(ba.copy(1, 2), (byte)-1, (byte)1);
		assertArray(ba.copy(4, 0));
		byte[] b2 = new byte[5];
		ba.copyTo(b2);
		assertArray(b2, b);
		b2 = new byte[5];
		ba.copyTo(0, b2, 2, 2);
		assertArray(b2, (byte)0, (byte)0, (byte)0, (byte)-1, (byte)0);
	}

	@Test
	public void shouldReturnTheNextOffsetWhenCopyingToAByteArray() {
		ImmutableByteArray ba = ImmutableByteArray.wrap(b);
		byte[] b2 = new byte[10];
		assertThat(ba.copyTo(b2), is(5));
		assertThat(ba.copyTo(b2, 4), is(9));
		assertThat(ba.copyTo(3, b2, 5), is(7));
		assertThat(ba.copyTo(3, b2, 5, 0), is(5));
		assertThat(ba.copyTo(1, b2, 3, 2), is(5));
	}
	
	@Test
	public void shouldAllowMultipleSlicesOfTheSameArray() {
		ImmutableByteArray ba = ImmutableByteArray.wrap(b);
		ImmutableByteArray ba2 = ba.slice(0, 5);
		ImmutableByteArray ba3 = ba2.slice(2);
		ImmutableByteArray ba4 = ba3.slice(1, 1);
		assertArray(ba.copy(), b);
		assertArray(ba2.copy(), b);
		assertArray(ba3.copy(), (byte)1, Byte.MAX_VALUE, Byte.MIN_VALUE);
		assertArray(ba4.copy(), Byte.MAX_VALUE);
	}

	@Test
	public void shouldOnlyAllowOffsetsAndLengthsWithinRange() {
		assertException(() -> ImmutableByteArray.wrap(b, -1, 0));
		assertException(() -> ImmutableByteArray.wrap(b, 0, -1));
		assertException(() -> ImmutableByteArray.wrap(b, 6, 0));
		assertException(() -> ImmutableByteArray.wrap(b, 5, 1));
		assertException(() -> ImmutableByteArray.wrap(b, 0, 6));
		assertException(() -> ImmutableByteArray.wrap(b, 3, 3));
		ImmutableByteArray ba = ImmutableByteArray.wrap(b);
		assertException(() -> ba.at(-1));
		assertException(() -> ba.at(5));
		assertException(() -> ba.copy(-1, 0));
		assertException(() -> ba.copy(0, -1));
		assertException(() -> ba.copy(5, 1));
		assertException(() -> ba.copy(0, 6));
		assertException(() -> ba.copy(3, 3));
		byte[] b2 = new byte[10];
		assertException(() -> ba.copyTo(-1, b2, 0, 0));
		assertException(() -> ba.copyTo(0, b2, 0, -1));
		assertException(() -> ba.copyTo(5, b2, 0, 1));
		assertException(() -> ba.copyTo(0, b2, 0, 6));
		assertException(() -> ba.copyTo(3, b2, 0, 3));
		assertException(() -> ba.slice(-1, 0));
		assertException(() -> ba.slice(0, -1));
		assertException(() -> ba.slice(5, 1));
		assertException(() -> ba.slice(0, 6));
		assertException(() -> ba.slice(3, 3));
	}

	@Test
	public void shouldMakeAnImmutableCopyOfAGivenByteArray() {
		byte[] b = { 0, -1, 1, Byte.MAX_VALUE, Byte.MIN_VALUE };
		ImmutableByteArray ba = ImmutableByteArray.copyOf(b);
		assertArray(ba.copy(), b);
		b[2] = 0;
		assertThat(ba.at(2), is((byte)1));
	}

}
