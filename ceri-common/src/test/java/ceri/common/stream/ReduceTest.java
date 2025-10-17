package ceri.common.stream;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertOverflow;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.Test;

public class ReduceTest {
	private static final int IMAX = Integer.MAX_VALUE;
	private static final long LMAX = Long.MAX_VALUE;

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Reduce.class, Reduce.Ints.class, Reduce.Longs.class,
			Reduce.Doubles.class);
	}

	@Test
	public void testIntMin() {
		assertEquals(Streams.ints().reduce(Reduce.Ints.min()), null);
		assertEquals(Streams.ints(1, -1, 0).reduce(Reduce.Ints.min()), -1);
	}

	@Test
	public void testIntSumExact() {
		assertEquals(Streams.ints().reduce(Reduce.Ints.sumExact()), null);
		assertEquals(Streams.ints(IMAX - 1, 1).reduce(Reduce.Ints.sumExact()), IMAX);
		assertOverflow(() -> Streams.ints(IMAX, 1).reduce(Reduce.Ints.sumExact()));
	}

	@Test
	public void testLongMin() {
		assertEquals(Streams.longs().reduce(Reduce.Longs.min()), null);
		assertEquals(Streams.longs(1, -1, 0).reduce(Reduce.Longs.min()), -1L);
	}

	@Test
	public void testLongSumExact() {
		assertEquals(Streams.longs().reduce(Reduce.Longs.sumExact()), null);
		assertEquals(Streams.longs(LMAX - 1, 1).reduce(Reduce.Longs.sumExact()), LMAX);
		assertOverflow(() -> Streams.longs(LMAX, 1).reduce(Reduce.Longs.sumExact()));
	}

	@Test
	public void testLongBitReduction() {
		assertEquals(Streams.longs(3, 9, 0).reduce(Reduce.Longs.or()), 11L);
		assertEquals(Streams.longs(3, 9, -1).reduce(Reduce.Longs.and()), 1L);
		assertEquals(Streams.longs(3, 9, 0).reduce(Reduce.Longs.xor()), 10L);
	}

	@Test
	public void testDoubleMin() {
		assertEquals(Streams.doubles().reduce(Reduce.Doubles.min()), null);
		assertEquals(Streams.doubles(1, -1, 0).reduce(Reduce.Doubles.min()), -1.0);
	}

	@Test
	public void testMin() {
		assertEquals(Streams.<Integer>of().reduce(Reduce.min()), null);
		assertEquals(Streams.of(1, -1, 0).reduce(Reduce.min()), -1);
	}

	@Test
	public void testMax() {
		assertEquals(Streams.<Integer>of().reduce(Reduce.max()), null);
		assertEquals(Streams.of(-1, 1, 0).reduce(Reduce.max()), 1);
	}
}
