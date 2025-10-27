package ceri.common.stream;

import org.junit.Test;
import ceri.common.test.Assert;

public class ReduceTest {
	private static final int IMAX = Integer.MAX_VALUE;
	private static final long LMAX = Long.MAX_VALUE;

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Reduce.class, Reduce.Ints.class, Reduce.Longs.class,
			Reduce.Doubles.class);
	}

	@Test
	public void testIntMin() {
		Assert.equal(Streams.ints().reduce(Reduce.Ints.min()), null);
		Assert.equal(Streams.ints(1, -1, 0).reduce(Reduce.Ints.min()), -1);
	}

	@Test
	public void testIntSumExact() {
		Assert.equal(Streams.ints().reduce(Reduce.Ints.sumExact()), null);
		Assert.equal(Streams.ints(IMAX - 1, 1).reduce(Reduce.Ints.sumExact()), IMAX);
		Assert.overflow(() -> Streams.ints(IMAX, 1).reduce(Reduce.Ints.sumExact()));
	}

	@Test
	public void testLongMin() {
		Assert.equal(Streams.longs().reduce(Reduce.Longs.min()), null);
		Assert.equal(Streams.longs(1, -1, 0).reduce(Reduce.Longs.min()), -1L);
	}

	@Test
	public void testLongSumExact() {
		Assert.equal(Streams.longs().reduce(Reduce.Longs.sumExact()), null);
		Assert.equal(Streams.longs(LMAX - 1, 1).reduce(Reduce.Longs.sumExact()), LMAX);
		Assert.overflow(() -> Streams.longs(LMAX, 1).reduce(Reduce.Longs.sumExact()));
	}

	@Test
	public void testLongBitReduction() {
		Assert.equal(Streams.longs(3, 9, 0).reduce(Reduce.Longs.or()), 11L);
		Assert.equal(Streams.longs(3, 9, -1).reduce(Reduce.Longs.and()), 1L);
		Assert.equal(Streams.longs(3, 9, 0).reduce(Reduce.Longs.xor()), 10L);
	}

	@Test
	public void testDoubleMin() {
		Assert.equal(Streams.doubles().reduce(Reduce.Doubles.min()), null);
		Assert.equal(Streams.doubles(1, -1, 0).reduce(Reduce.Doubles.min()), -1.0);
	}

	@Test
	public void testMin() {
		Assert.equal(Streams.<Integer>of().reduce(Reduce.min()), null);
		Assert.equal(Streams.of(1, -1, 0).reduce(Reduce.min()), -1);
	}

	@Test
	public void testMax() {
		Assert.equal(Streams.<Integer>of().reduce(Reduce.max()), null);
		Assert.equal(Streams.of(-1, 1, 0).reduce(Reduce.max()), 1);
	}
}
