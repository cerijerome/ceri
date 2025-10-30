package ceri.common.data;

import java.util.Arrays;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.data.LongProvider.Reader;
import ceri.common.data.LongReceiverBehavior.Holder;
import ceri.common.math.Maths;
import ceri.common.test.Assert;
import ceri.common.test.Captor;

public class LongProviderBehavior {
	private static final LongProvider lp = provider(0, -1, 2, -3, 4, -5, 6, -7, 8, -9);

	/* LongProvider tests */

	@Test
	public void testOf() {
		Assert.array(LongProvider.of(Long.MAX_VALUE, Long.MIN_VALUE), Long.MAX_VALUE,
			Long.MIN_VALUE);
	}

	@Test
	public void testCopyOf() {
		long[] longs = { Long.MAX_VALUE, Long.MIN_VALUE };
		var of = LongProvider.of(longs);
		var copyOf = LongProvider.copyOf(longs);
		Arrays.fill(longs, (byte) 0);
		Assert.array(of.copy(0), 0, 0);
		Assert.array(copyOf.copy(0), 0x7fffffffffffffffL, 0x8000000000000000L);
	}

	@Test
	public void testToHex() {
		Assert.equal(LongProvider.toHex(lp),
			"[0x0,0xffffffffffffffff,0x2,0xfffffffffffffffd,0x4,0xfffffffffffffffb,0x6,...](10)");
	}

	@Test
	public void testToString() {
		Assert.equal(LongProvider.toString(lp), "[0,-1,2,-3,4,-5,6,...](10)");
		Assert.equal(LongProvider.toString(Maths::ubyte, lp), "[0,255,2,253,4,251,6,...](10)");
	}

	@Test
	public void shouldProvideAnEmptyInstance() {
		Assert.equal(LongProvider.empty().length(), 0);
		Assert.yes(LongProvider.empty().isEmpty());
		Assert.thrown(() -> LongProvider.empty().getLong(0));
	}

	@Test
	public void shouldIterateValues() {
		Captor.OfLong captor = Captor.ofLong();
		for (long l : LongProvider.empty())
			captor.accept(l);
		captor.verifyLong();
		for (long l : provider(-1, 0, 1, Long.MIN_VALUE, Long.MAX_VALUE, 0xffffffff))
			captor.accept(l);
		captor.verifyLong(-1, 0, 1, Long.MIN_VALUE, Long.MAX_VALUE, 0xffffffff);
	}

	@Test
	public void shouldDetermineIfEmpty() {
		Assert.no(lp.isEmpty());
		Assert.yes(LongProvider.empty().isEmpty());
	}

	@Test
	public void shouldProvidePrimitiveValues() {
		Assert.equal(lp.getLong(1), -1L);
		Assert.equal(lp.getLong(2), 2L);
		Assert.equal(lp.getDouble(3), Double.longBitsToDouble(-3L));
	}

	@Test
	public void shouldSliceProvidedLongRange() {
		Assert.yes(lp.slice(10).isEmpty());
		Assert.array(lp.slice(5, 0).copy(0));
		Assert.equal(lp.slice(0), lp);
		Assert.equal(lp.slice(0, 10), lp);
		Assert.thrown(() -> lp.slice(1, 10));
		Assert.thrown(() -> lp.slice(0, 9));
	}

	@Test
	public void shouldProvideACopyOfLongs() {
		Assert.array(lp.copy(5, 0));
		Assert.array(lp.copy(5, 3), -5, 6, -7);
	}

	@Test
	public void shouldCopyToLongArray() {
		long[] longs = new long[5];
		Assert.equal(lp.copyTo(1, longs), 6);
		Assert.array(longs, -1, 2, -3, 4, -5);
		Assert.thrown(() -> lp.copyTo(6, longs));
		Assert.thrown(() -> lp.copyTo(-1, longs));
		Assert.thrown(() -> lp.copyTo(1, longs, 0, 6));
	}

	@Test
	public void shouldCopyToReceiver() {
		Holder h = Holder.of(5);
		Assert.equal(lp.copyTo(1, h.receiver), 6);
		Assert.array(h.longs, -1, 2, -3, 4, -5);
		Assert.thrown(() -> lp.copyTo(6, h.receiver));
		Assert.thrown(() -> lp.copyTo(-1, h.receiver));
		Assert.thrown(() -> lp.copyTo(1, h.receiver, 0, 6));
	}

	@Test
	public void shouldStreamLongs() {
		Assert.stream(lp.stream(0), 0, -1, 2, -3, 4, -5, 6, -7, 8, -9);
		Assert.thrown(() -> lp.stream(0, 11));
	}

	@Test
	public void shouldDetermineIfIntsAreEqual() {
		Assert.yes(lp.isEqualTo(5, -5, 6, -7, 8, -9));
		Assert.no(lp.isEqualTo(5, -5, 6, -7, 8, 9));
		long[] longs = Array.longs.of(0, -1, 2, -3, 4);
		Assert.yes(lp.isEqualTo(0, longs));
		Assert.no(lp.isEqualTo(0, longs, 0, 6));
		Assert.no(lp.isEqualTo(9, -9, 0));
	}

	@Test
	public void shouldDetermineIfProvidedIntsAreEqual() {
		Assert.yes(lp.isEqualTo(0, lp));
		Assert.yes(lp.isEqualTo(5, lp, 5));
		Assert.yes(lp.isEqualTo(5, lp, 5, 3));
		Assert.yes(lp.isEqualTo(1, provider(-1, 2, -3)));
		Assert.no(lp.isEqualTo(1, provider(1, 2, -3)));
		Assert.no(lp.isEqualTo(0, provider(1, 2, 3), 0, 4));
		Assert.no(lp.isEqualTo(9, provider(1, 2, 3)));
	}

	@Test
	public void shouldDetermineIfContains() {
		Assert.equal(lp.contains(-1, 2, -3), true);
		Assert.equal(lp.contains(-1, 2, 3), false);
	}

	@Test
	public void shouldDetermineIndexOfLongs() {
		Assert.equal(lp.indexOf(0, -1, 2, -3), 1);
		Assert.equal(lp.indexOf(0, -1, 2, 3), -1);
		Assert.equal(lp.indexOf(8, -1, 2, -3), -1);
		Assert.equal(lp.indexOf(0, Array.longs.of(-1, 2, -3), 0, 4), -1);
	}

	@Test
	public void shouldDetermineIndexOfProvidedLongs() {
		Assert.equal(lp.indexOf(0, provider(-1, 2, -3)), 1);
		Assert.equal(lp.indexOf(0, provider(-1, 2, 3)), -1);
		Assert.equal(lp.indexOf(8, provider(-1, 2, -3)), -1);
		Assert.equal(lp.indexOf(0, provider(-1, 2, -3), 0, 4), -1);
	}

	@Test
	public void shouldDetermineLastIndexOfBytes() {
		LongProvider lp = provider(0, -1, 2, -1, 0, 2, -1, 0);
		Assert.equal(lp.lastIndexOf(0, 2, -1), 5);
		Assert.equal(lp.lastIndexOf(0, 2, 1), -1);
		Assert.equal(lp.lastIndexOf(7, 0, -1), -1);
		Assert.equal(lp.lastIndexOf(0, Array.longs.of(2, -1, 0), 0, 4), -1);
	}

	@Test
	public void shouldDetermineLastIndexOfProviderBytes() {
		LongProvider lp = provider(0, -1, 2, -1, 0, 2, -1, 0);
		Assert.equal(lp.lastIndexOf(0, provider(2, -1)), 5);
		Assert.equal(lp.lastIndexOf(0, provider(2, 1)), -1);
		Assert.equal(lp.lastIndexOf(7, provider(0, -1)), -1);
		Assert.equal(lp.lastIndexOf(0, provider(2, -1, 0), 0, 4), -1);
	}

	@Test
	public void shouldProvideReaderAccessToLongs() {
		Assert.array(lp.reader(5).readLongs(), -5, 6, -7, 8, -9);
		Assert.array(lp.reader(5, 0).readLongs());
		Assert.array(lp.reader(10, 0).readLongs());
		Assert.thrown(() -> lp.reader(10, 1));
		Assert.thrown(() -> lp.reader(11, 0));
	}

	/* LongProvider.Reader tests */

	@Test
	public void shouldReadLong() {
		Assert.equal(lp.reader(1).readLong(), -1L);
		Assert.thrown(() -> lp.reader(1, 0).readLong());
	}

	@Test
	public void shouldReadIntoLongArray() {
		long[] longs = new long[4];
		lp.reader(5).readInto(longs);
		Assert.array(longs, -5, 6, -7, 8);
	}

	@Test
	public void shouldReadIntoLongReceiver() {
		long[] longs = new long[4];
		LongReceiver lr = LongArray.Mutable.wrap(longs);
		lp.reader(5).readInto(lr);
		Assert.array(longs, -5, 6, -7, 8);
	}

	@Test
	public void shouldStreamReaderLongs() {
		Assert.stream(lp.reader(6).stream(), 6, -7, 8, -9);
	}

	@Test
	public void shouldReturnReaderLongProvider() {
		Assert.equal(lp.reader(0).provider(), lp);
		Assert.yes(lp.reader(5, 0).provider().isEmpty());
		Assert.thrown(() -> lp.reader(5).provider()); // slice() fails
	}

	@Test
	public void shouldSliceReader() {
		Reader r0 = lp.reader(6);
		Reader r1 = r0.slice();
		Reader r2 = r0.slice(3);
		Assert.thrown(() -> r0.slice(5));
		Assert.thrown(() -> r0.slice(-2));
		Assert.array(r0.readLongs(), 6, -7, 8, -9);
		Assert.array(r1.readLongs(), 6, -7, 8, -9);
		Assert.array(r2.readLongs(), 6, -7, 8);
	}

	/* Support methods */

	public static LongProvider provider(long... longs) {
		return new LongProvider() {
			@Override
			public long getLong(int index) {
				return longs[index];
			}

			@Override
			public int length() {
				return longs.length;
			}
		};
	}
}
