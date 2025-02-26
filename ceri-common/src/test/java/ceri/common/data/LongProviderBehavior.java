package ceri.common.data;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.Arrays;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.LongProvider.Reader;
import ceri.common.data.LongReceiverBehavior.Holder;
import ceri.common.math.MathUtil;
import ceri.common.test.Captor;

public class LongProviderBehavior {
	private static final LongProvider lp = provider(0, -1, 2, -3, 4, -5, 6, -7, 8, -9);

	/* LongProvider tests */

	@Test
	public void testOf() {
		assertArray(LongProvider.of(Long.MAX_VALUE, Long.MIN_VALUE), Long.MAX_VALUE,
			Long.MIN_VALUE);
	}

	@Test
	public void testCopyOf() {
		long[] longs = { Long.MAX_VALUE, Long.MIN_VALUE };
		var of = LongProvider.of(longs);
		var copyOf = LongProvider.copyOf(longs);
		Arrays.fill(longs, (byte) 0);
		assertArray(of.copy(0), 0, 0);
		assertArray(copyOf.copy(0), 0x7fffffffffffffffL, 0x8000000000000000L);
	}

	@Test
	public void testToHex() {
		assertEquals(LongProvider.toHex(lp),
			"[0x0,0xffffffffffffffff,0x2,0xfffffffffffffffd,0x4,0xfffffffffffffffb,0x6,...](10)");
	}

	@Test
	public void testToString() {
		assertEquals(LongProvider.toString(lp), "[0,-1,2,-3,4,-5,6,...](10)");
		assertEquals(LongProvider.toString(MathUtil::ubyte, lp), "[0,255,2,253,4,251,6,...](10)");
	}

	@Test
	public void shouldProvideAnEmptyInstance() {
		assertEquals(LongProvider.empty().length(), 0);
		assertTrue(LongProvider.empty().isEmpty());
		assertThrown(() -> LongProvider.empty().getLong(0));
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
		assertFalse(lp.isEmpty());
		assertTrue(LongProvider.empty().isEmpty());
	}

	@Test
	public void shouldProvidePrimitiveValues() {
		assertEquals(lp.getLong(1), -1L);
		assertEquals(lp.getLong(2), 2L);
		assertEquals(lp.getDouble(3), Double.longBitsToDouble(-3L));
	}

	@Test
	public void shouldSliceProvidedLongRange() {
		assertTrue(lp.slice(10).isEmpty());
		assertArray(lp.slice(5, 0).copy(0));
		assertEquals(lp.slice(0), lp);
		assertEquals(lp.slice(0, 10), lp);
		assertThrown(() -> lp.slice(1, 10));
		assertThrown(() -> lp.slice(0, 9));
	}

	@Test
	public void shouldProvideACopyOfLongs() {
		assertArray(lp.copy(5, 0));
		assertArray(lp.copy(5, 3), -5, 6, -7);
	}

	@Test
	public void shouldCopyToLongArray() {
		long[] longs = new long[5];
		assertEquals(lp.copyTo(1, longs), 6);
		assertArray(longs, -1, 2, -3, 4, -5);
		assertThrown(() -> lp.copyTo(6, longs));
		assertThrown(() -> lp.copyTo(-1, longs));
		assertThrown(() -> lp.copyTo(1, longs, 0, 6));
	}

	@Test
	public void shouldCopyToReceiver() {
		Holder h = Holder.of(5);
		assertEquals(lp.copyTo(1, h.receiver), 6);
		assertArray(h.longs, -1, 2, -3, 4, -5);
		assertThrown(() -> lp.copyTo(6, h.receiver));
		assertThrown(() -> lp.copyTo(-1, h.receiver));
		assertThrown(() -> lp.copyTo(1, h.receiver, 0, 6));
	}

	@Test
	public void shouldStreamLongs() {
		assertStream(lp.stream(0), 0, -1, 2, -3, 4, -5, 6, -7, 8, -9);
		assertThrown(() -> lp.stream(0, 11));
	}

	@Test
	public void shouldDetermineIfIntsAreEqual() {
		assertTrue(lp.isEqualTo(5, -5, 6, -7, 8, -9));
		assertFalse(lp.isEqualTo(5, -5, 6, -7, 8, 9));
		long[] longs = ArrayUtil.longs(0, -1, 2, -3, 4);
		assertTrue(lp.isEqualTo(0, longs));
		assertFalse(lp.isEqualTo(0, longs, 0, 6));
		assertFalse(lp.isEqualTo(9, -9, 0));
	}

	@Test
	public void shouldDetermineIfProvidedIntsAreEqual() {
		assertTrue(lp.isEqualTo(0, lp));
		assertTrue(lp.isEqualTo(5, lp, 5));
		assertTrue(lp.isEqualTo(5, lp, 5, 3));
		assertTrue(lp.isEqualTo(1, provider(-1, 2, -3)));
		assertFalse(lp.isEqualTo(1, provider(1, 2, -3)));
		assertFalse(lp.isEqualTo(0, provider(1, 2, 3), 0, 4));
		assertFalse(lp.isEqualTo(9, provider(1, 2, 3)));
	}

	@Test
	public void shouldDetermineIfContains() {
		assertEquals(lp.contains(-1, 2, -3), true);
		assertEquals(lp.contains(-1, 2, 3), false);
	}

	@Test
	public void shouldDetermineIndexOfLongs() {
		assertEquals(lp.indexOf(0, -1, 2, -3), 1);
		assertEquals(lp.indexOf(0, -1, 2, 3), -1);
		assertEquals(lp.indexOf(8, -1, 2, -3), -1);
		assertEquals(lp.indexOf(0, ArrayUtil.longs(-1, 2, -3), 0, 4), -1);
	}

	@Test
	public void shouldDetermineIndexOfProvidedLongs() {
		assertEquals(lp.indexOf(0, provider(-1, 2, -3)), 1);
		assertEquals(lp.indexOf(0, provider(-1, 2, 3)), -1);
		assertEquals(lp.indexOf(8, provider(-1, 2, -3)), -1);
		assertEquals(lp.indexOf(0, provider(-1, 2, -3), 0, 4), -1);
	}

	@Test
	public void shouldDetermineLastIndexOfBytes() {
		LongProvider lp = provider(0, -1, 2, -1, 0, 2, -1, 0);
		assertEquals(lp.lastIndexOf(0, 2, -1), 5);
		assertEquals(lp.lastIndexOf(0, 2, 1), -1);
		assertEquals(lp.lastIndexOf(7, 0, -1), -1);
		assertEquals(lp.lastIndexOf(0, ArrayUtil.longs(2, -1, 0), 0, 4), -1);
	}

	@Test
	public void shouldDetermineLastIndexOfProviderBytes() {
		LongProvider lp = provider(0, -1, 2, -1, 0, 2, -1, 0);
		assertEquals(lp.lastIndexOf(0, provider(2, -1)), 5);
		assertEquals(lp.lastIndexOf(0, provider(2, 1)), -1);
		assertEquals(lp.lastIndexOf(7, provider(0, -1)), -1);
		assertEquals(lp.lastIndexOf(0, provider(2, -1, 0), 0, 4), -1);
	}

	@Test
	public void shouldProvideReaderAccessToLongs() {
		assertArray(lp.reader(5).readLongs(), -5, 6, -7, 8, -9);
		assertArray(lp.reader(5, 0).readLongs());
		assertArray(lp.reader(10, 0).readLongs());
		assertThrown(() -> lp.reader(10, 1));
		assertThrown(() -> lp.reader(11, 0));
	}

	/* LongProvider.Reader tests */

	@Test
	public void shouldReadLong() {
		assertEquals(lp.reader(1).readLong(), -1L);
		assertThrown(() -> lp.reader(1, 0).readLong());
	}

	@Test
	public void shouldReadIntoLongArray() {
		long[] longs = new long[4];
		lp.reader(5).readInto(longs);
		assertArray(longs, -5, 6, -7, 8);
	}

	@Test
	public void shouldReadIntoLongReceiver() {
		long[] longs = new long[4];
		LongReceiver lr = LongArray.Mutable.wrap(longs);
		lp.reader(5).readInto(lr);
		assertArray(longs, -5, 6, -7, 8);
	}

	@Test
	public void shouldStreamReaderLongs() {
		assertStream(lp.reader(6).stream(), 6, -7, 8, -9);
	}

	@Test
	public void shouldReturnReaderLongProvider() {
		assertEquals(lp.reader(0).provider(), lp);
		assertTrue(lp.reader(5, 0).provider().isEmpty());
		assertThrown(() -> lp.reader(5).provider()); // slice() fails
	}

	@Test
	public void shouldSliceReader() {
		Reader r0 = lp.reader(6);
		Reader r1 = r0.slice();
		Reader r2 = r0.slice(3);
		assertThrown(() -> r0.slice(5));
		assertThrown(() -> r0.slice(-2));
		assertArray(r0.readLongs(), 6, -7, 8, -9);
		assertArray(r1.readLongs(), 6, -7, 8, -9);
		assertArray(r2.readLongs(), 6, -7, 8);
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
