package ceri.common.collection;

import static ceri.common.test.TestUtil.assertArray;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class ImmutableIntArrayBehavior {
	// Don't overwrite!
	private final int[] ints = { 0, -1, 1, Integer.MAX_VALUE, Integer.MIN_VALUE };

	@Test
	public void shouldStream() {
		ImmutableIntArray ia = ImmutableIntArray.wrap(ints);
		assertArray(ia.stream().toArray(), ints);
	}

	@Test
	public void shouldAppendInts() {
		ImmutableIntArray ia1 = ImmutableIntArray.wrap(0, 1, 2);
		ImmutableIntArray ia2 = ImmutableIntArray.wrap(3, 4, 5);
		assertArray(ia1.append(ImmutableIntArray.EMPTY).copy(), 0, 1, 2);
		assertArray(ia1.append(ia2).copy(), 0, 1, 2, 3, 4, 5);
		assertArray(ia1.append(ia2.copy()).copy(), 0, 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldIterateEachInt() {
		ImmutableIntArray ia = ImmutableIntArray.wrap(0, 1, 2, 10);
		StringBuilder b = new StringBuilder();
		ia.forEach(b::append);
		assertThat(b.toString(), is("01210"));
	}

	@Test
	public void shouldEqualMatchingImmutableIntArrayWithOffset() {
		int[] ints = { Integer.MIN_VALUE, -1, 1, Integer.MAX_VALUE, 0 };
		ImmutableIntArray ia = ImmutableIntArray.wrap(ints, 1, 3);
		ImmutableIntArray ia2 = ImmutableIntArray.wrap(ints, 0, 4);
		ImmutableIntArray ia3 = ImmutableIntArray.wrap(ints);
		ImmutableIntArray ia4 = ImmutableIntArray.wrap(-1, 1, -1);
		assertTrue(ia.equals(ia2, 1));
		assertTrue(ia.equals(ia3, 1, 3));
		assertTrue(ia3.equals(1, ia2, 1));
		assertFalse(ia2.equals(1, ia4, 0));
	}

	@Test
	public void shouldEqualMatchingIntArray() {
		ImmutableIntArray ia = ImmutableIntArray.wrap(-1, 1, Integer.MAX_VALUE);
		assertTrue(ia.equals(-1, 1, Integer.MAX_VALUE));
		assertTrue(ia.equals(new int[] { Integer.MIN_VALUE, -1, 1, Integer.MAX_VALUE }, 1));
		assertTrue(ia.equals(new int[] { Integer.MIN_VALUE, -1, 1, Integer.MAX_VALUE, 0 }, 1, 3));
		assertFalse(ia.equals(-1, 1, 0));
	}

	@Test
	public void shouldFollowTheEqualsContract() {
		ImmutableIntArray ia0 = ImmutableIntArray.wrap(-1, 1, Integer.MAX_VALUE);
		ImmutableIntArray ia1 = ImmutableIntArray.wrap(ints, 1, 3);
		ImmutableIntArray ia2 = ImmutableIntArray.copyOf(ints, 1, 3);
		ImmutableIntArray ia3 = ImmutableIntArray.wrap(ints).slice(1, 3);
		ImmutableIntArray ia4 = ImmutableIntArray.wrap(ints, 0, 3);
		ImmutableIntArray ia5 = ImmutableIntArray.wrap(ints, 1, 2);
		ImmutableIntArray ia6 = ImmutableIntArray.wrap(ints, 1, 0);
		ImmutableIntArray ia7 = ImmutableIntArray.wrap(ints, 5, 0);
		assertThat(ia0, is(ia1));
		assertThat(ia1, is(ia2));
		assertThat(ia1, is(ia3));
		assertThat(ia3, is(ia2));
		assertThat(ia1, is(not(ints)));
		assertThat(ia1, is(not(ia4)));
		assertThat(ia1, is(not(ia5)));
		assertThat(ia1, is(not(ia6)));
		assertThat(ia6, is(ia7));
		assertThat(ia0.hashCode(), is(ia1.hashCode()));
		assertThat(ia2.hashCode(), is(ia3.hashCode()));
		assertThat(ia3.toString(), is(ia1.toString()));
	}

	@Test
	public void shouldCopyTheIntArray() {
		ImmutableIntArray ia = ImmutableIntArray.wrap(ints);
		assertArray(ia.copy(3), Integer.MAX_VALUE, Integer.MIN_VALUE);
		assertArray(ia.copy(1, 2), -1, 1);
		assertArray(ia.copy(4, 0));
		int[] b2 = new int[5];
		ia.copyTo(b2);
		assertArray(b2, ints);
		b2 = new int[5];
		ia.copyTo(0, b2, 2, 2);
		assertArray(b2, 0, 0, 0, -1, 0);
	}

	@Test
	public void shouldReturnTheNextOffsetWhenCopyingToAIntArray() {
		ImmutableIntArray ia = ImmutableIntArray.wrap(ints);
		int[] b2 = new int[10];
		assertThat(ia.copyTo(b2), is(5));
		assertThat(ia.copyTo(b2, 4), is(9));
		assertThat(ia.copyTo(3, b2, 5), is(7));
		assertThat(ia.copyTo(3, b2, 5, 0), is(5));
		assertThat(ia.copyTo(1, b2, 3, 2), is(5));
	}

	@Test
	public void shouldAllowMultipleSlicesOfTheSameArray() {
		ImmutableIntArray ia = ImmutableIntArray.wrap(ints);
		ImmutableIntArray ia2 = ia.slice(0, 5);
		ImmutableIntArray ia3 = ia2.slice(2);
		ImmutableIntArray ia4 = ia3.slice(1, 1);
		assertArray(ia.copy(), ints);
		assertArray(ia2.copy(), ints);
		assertArray(ia3.copy(), 1, Integer.MAX_VALUE, Integer.MIN_VALUE);
		assertArray(ia4.copy(), Integer.MAX_VALUE);
	}

	@Test
	public void shouldOnlyAllowOffsetsAndLengthsWithinRange() {
		TestUtil.assertThrown(() -> ImmutableIntArray.wrap(ints, -1, 0));
		TestUtil.assertThrown(() -> ImmutableIntArray.wrap(ints, 0, -1));
		TestUtil.assertThrown(() -> ImmutableIntArray.wrap(ints, 6, 0));
		TestUtil.assertThrown(() -> ImmutableIntArray.wrap(ints, 5, 1));
		TestUtil.assertThrown(() -> ImmutableIntArray.wrap(ints, 0, 6));
		TestUtil.assertThrown(() -> ImmutableIntArray.wrap(ints, 3, 3));
		ImmutableIntArray ia = ImmutableIntArray.wrap(ints);
		TestUtil.assertThrown(() -> ia.at(-1));
		TestUtil.assertThrown(() -> ia.at(5));
		TestUtil.assertThrown(() -> ia.copy(-1, 0));
		TestUtil.assertThrown(() -> ia.copy(0, -1));
		TestUtil.assertThrown(() -> ia.copy(5, 1));
		TestUtil.assertThrown(() -> ia.copy(0, 6));
		TestUtil.assertThrown(() -> ia.copy(3, 3));
		int[] b2 = new int[10];
		TestUtil.assertThrown(() -> ia.copyTo(-1, b2, 0, 0));
		TestUtil.assertThrown(() -> ia.copyTo(0, b2, 0, -1));
		TestUtil.assertThrown(() -> ia.copyTo(5, b2, 0, 1));
		TestUtil.assertThrown(() -> ia.copyTo(0, b2, 0, 6));
		TestUtil.assertThrown(() -> ia.copyTo(3, b2, 0, 3));
		TestUtil.assertThrown(() -> ia.slice(-1, 0));
		TestUtil.assertThrown(() -> ia.slice(0, -1));
		TestUtil.assertThrown(() -> ia.slice(5, 1));
		TestUtil.assertThrown(() -> ia.slice(0, 6));
		TestUtil.assertThrown(() -> ia.slice(3, 3));
	}

	@Test
	public void shouldMakeAnImmutableCopyOfAGivenIntArray() {
		int[] b = { 0, -1, 1, Integer.MAX_VALUE, Integer.MIN_VALUE };
		ImmutableIntArray ia = ImmutableIntArray.copyOf(b);
		assertArray(ia.copy(), b);
		b[2] = 0;
		assertThat(ia.at(2), is(1));
	}

}
