package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import org.junit.Test;
import ceri.common.test.Captor;

public class IndexRangesBehavior {
	private static final int MAX = Integer.MAX_VALUE;

	@Test
	public void shouldProvideStringRepresentation() {
		assertString(r(1, 3).add(5, 5).add(7, 8), "[1-3,5,7,8]");
	}

	@Test
	public void shouldCreateFromString() {
		assertRanges(IndexRanges.from("[]"));
		assertRanges(IndexRanges.from("[ 9 ]"), 9);
		assertRanges(IndexRanges.from("[6-8]"), 6, 7, 8);
		assertRanges(IndexRanges.from("[6 , 7]"), 6, 7);
		assertRanges(IndexRanges.from("[2-3, 5-6]"), 2, 3, 5, 6);
		assertRanges(IndexRanges.from("[1, 3-4, 5]"), 1, 3, 4, 5);
		assertThrown(() -> IndexRanges.from("1"));
		assertThrown(() -> IndexRanges.from("[1,]"));
		assertThrown(() -> IndexRanges.from("[1 - 2]"));
		assertThrown(() -> IndexRanges.from("[2-1]"));
	}

	@Test
	public void shouldAddRanges() {
		var r = IndexRanges.of();
		r.add(6);
		r.add(1, 2);
		r.add(r(8, 9).add(4));
		r.add(3, 4);
		assertRanges(r, 1, 2, 3, 4, 6, 8, 9);
		assertEquals(r.ranges(), 3);
	}

	@Test
	public void shouldRemoveRanges() {
		var r = IndexRanges.of();
		r.remove(1, 9);
		r.add(2, 13);
		r.remove(0, 1);
		r.remove(14, 15);
		r.remove(5, 6);
		r.remove(r(9, 10).add(2, 3));
		r.remove(12);
		r.remove(13);
		assertRanges(r, 4, 7, 8, 11);
		assertEquals(r.ranges(), 3);
	}

	@Test
	public void shouldShiftRangesLeft() {
		assertRanges(r(3, 5).shift(0), 3, 4, 5);
		assertRanges(r(3, 5).shift(-3), 0, 1, 2);
		assertRanges(r(3, 5).shift(-4), 0, 1);
		assertRanges(r(3, 5).shift(-5), 0);
		assertRanges(r(3, 5).shift(-6));
		assertRanges(r(3, 5).add(7, 8).shift(-6), 1, 2);

	}

	@Test
	public void shouldShiftRangesRight() {
		assertRanges(r(3, 5).shift(0), 3, 4, 5);
		assertRanges(r(3, 5).shift(MAX - 5), MAX - 2, MAX - 1, MAX);
		assertRanges(r(3, 5).shift(MAX - 4), MAX - 1, MAX);
		assertRanges(r(3, 5).shift(MAX - 3), MAX);
		assertRanges(r(3, 5).shift(MAX - 2));
		assertRanges(r(3, 5).add(7, 8).shift(MAX - 6), MAX - 3, MAX - 2, MAX - 1);

	}

	@Test
	public void shouldCountIndexes() {
		assertTrue(IndexRanges.of().isEmpty());
		assertFalse(IndexRanges.of().add(1, 1).isEmpty());
		assertEquals(IndexRanges.of().count(), 0);
		assertEquals(r(1, 10).add(20, 20).add(30, 33).count(), 15);
		assertEquals(r(0, MAX).count(), Integer.MIN_VALUE);
	}

	@Test
	public void shouldReturnLimits() {
		assertEquals(IndexRanges.of().first(), -1);
		assertEquals(IndexRanges.of().last(), -1);
		assertEquals(r(0, 0).first(), 0);
		assertEquals(r(0, 0).last(), 0);
		assertEquals(r(0, MAX).first(), 0);
		assertEquals(r(0, MAX).last(), MAX);
		assertEquals(r(MAX, MAX).last(), MAX);
		assertEquals(r(MAX, MAX).last(), MAX);
	}

	@Test
	public void shouldConsumeIndexes() {
		var c = Captor.ofInt();
		r(0, 1).add(3, 3).add(MAX - 1, MAX).forEachInt(c::accept);
		c.verifyInt(0, 1, 3, MAX - 1, MAX);
	}

	@Test
	public void shouldFailIterationIfModified() {
		var r = r(2, 3).add(7, 9);
		var i = r.iterator();
		iterate(i, 3);
		r.remove(7, 7);
		assertThrown(IllegalStateException.class, i::nextInt);
		r.remove(8, 9);
		assertThrown(IllegalStateException.class, i::nextInt);
		r.add(6, 6);
		assertThrown(IllegalStateException.class, i::nextInt);
		r.add(7, 7);
		assertThrown(NoSuchElementException.class, i::nextInt);
	}

	@Test
	public void shouldUseBinarySearchIfLargeEnough() {
		var r =
			IndexRanges.of(1, 10).add(5, 6).add(1, 1).add(8, 9).add(1, 2).add(7, 9).remove(4, 7);
		assertRanges(r, 1, 2, 8, 9);
		assertEquals(r.ranges(), 2);
	}

	@Test
	public void shouldGrowArrays() {
		var r = IndexRanges.of(100, 1).add(1, 2);
		assertRanges(r, 1, 2);
		r.add(3, 3).add(5, 5).add(7, 7);
		assertRanges(r, 1, 2, 3, 5, 7);
		assertEquals(r.ranges(), 3);
	}

	@Test
	public void shouldCopy() {
		var r = IndexRanges.of(0, 1).add(1, 3).add(6, 7);
		var copy = r.copy();
		r.add(4);
		assertRanges(r, 1, 2, 3, 4, 6, 7);
		assertRanges(copy, 1, 2, 3, 6, 7);
	}
	
	private void iterate(PrimitiveIterator.OfInt iterator, int n) {
		while (n-- > 0)
			iterator.next();
	}

	private static IndexRanges r(int start, int end) {
		return IndexRanges.of().add(start, end);
	}

	private static void assertRanges(IndexRanges r, int... indexes) {
		assertArray(r.stream().toArray(), indexes);
	}
}
