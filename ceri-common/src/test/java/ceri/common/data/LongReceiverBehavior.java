package ceri.common.data;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.Arrays;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.ExceptionConsumer;

public class LongReceiverBehavior {

	/* LongReceiver tests */

	@Test
	public void shouldProvideAnEmptyInstance() {
		assertEquals(LongReceiver.empty().length(), 0);
		assertThrown(() -> LongReceiver.empty().setLong(0, 0));
	}

	@Test
	public void shouldDetermineIfEmpty() {
		assertFalse(receiver(3).isEmpty());
		assertTrue(receiver(0).isEmpty());
		assertTrue(LongReceiver.empty().isEmpty());
	}

	@Test
	public void shouldSliceReceivingIntRange() {
		long[] longs = new long[5];
		LongReceiver br = receiver(longs, 0, longs.length);
		assertTrue(br.slice(5).isEmpty());
		assertTrue(br.slice(4, 0).isEmpty());
		assertEquals(br.slice(0), br);
		assertThrown(() -> br.slice(1, 4));
		assertThrown(() -> br.slice(0, 4));
	}

	@Test
	public void shouldFillLongs() {
		assertLongs(5, br -> assertEquals(br.fill(2, 0xff), 5), 0, 0, 0xff, 0xff, 0xff);
		assertThrown(() -> receiver(5).fill(3, 3, 0));
	}

	@Test
	public void shouldSetDouble() {
		assertLongs(2,
			br -> assertEquals(br.setDouble(1, Double.longBitsToDouble(0x123456789L)), 2), 0,
			0x123456789L);
	}

	@Test
	public void shouldCopyFromLongArray() {
		assertLongs(5, br -> assertEquals(br.setLongs(1, 1, 2, 3), 4), 0, 1, 2, 3, 0);
		assertThrown(() -> receiver(5).setLongs(4, 1, 2, 3));
	}

	@Test
	public void shouldCopyFromLongProvider() {
		LongProvider bp = LongProviderBehavior.provider(1, 2, 3);
		assertLongs(5, br -> assertEquals(br.copyFrom(1, bp), 4), 0, 1, 2, 3, 0);
		assertThrown(() -> receiver(5).copyFrom(4, bp));
	}

	@Test
	public void shouldProvideWriterAccessToLongs() {
		assertLongs(5, br -> br.writer(3).fill(0xff), 0, 0, 0, 0xff, 0xff);
		assertLongs(5, br -> br.writer(3, 0).fill(0xff), 0, 0, 0, 0, 0);
		assertLongs(5, br -> br.writer(5).fill(0xff), 0, 0, 0, 0, 0);
		assertThrown(() -> receiver(5).writer(6));
		assertThrown(() -> receiver(5).writer(1, 5));
		assertThrown(() -> receiver(5).writer(-1));
	}

	/* LongReceiver.Writer tests */

	@Test
	public void shouldWriteLong() {
		assertLongs(3, br -> br.writer(0).writeLong(1).writeLong(2), 1, 2, 0);
		assertThrown(() -> receiver(3).writer(1, 0).writeLong(2));
	}

	@Test
	public void shouldWriteFromLongArray() {
		long[] longs = ArrayUtil.longs(1, 2, 3, 4, 5);
		assertLongs(5, br -> br.writer(1).writeFrom(longs, 1, 3), 0, 2, 3, 4, 0);
	}

	@Test
	public void shouldWriteFromLongProvider() {
		LongProvider bp = LongArray.Immutable.wrap(1, 2, 3, 4, 5);
		assertLongs(5, br -> br.writer(1).writeFrom(bp, 1, 3), 0, 2, 3, 4, 0);
	}

	@Test
	public void shouldSkipLongs() {
		assertLongs(5, br -> br.writer(1).skip(2).fill(1), 0, 0, 0, 1, 1);
	}

	@Test
	public void shouldReturnWriterLongProvider() {
		LongReceiver br = receiver(ArrayUtil.longs(1, 2, 3, 4, 5));
		assertEquals(br.writer(0).receiver(), br);
		assertTrue(br.writer(5, 0).receiver().isEmpty());
		assertThrown(() -> br.writer(2).receiver());
	}

	@Test
	public void shouldSliceWriter() {
		assertLongs(5, br -> br.writer(2).slice().fill(1), 0, 0, 1, 1, 1);
		assertLongs(5, br -> br.writer(2).slice(2).fill(1), 0, 0, 1, 1, 0);
		assertThrown(() -> receiver(5).writer(2).slice(4));
		assertThrown(() -> receiver(5).writer(2).slice(-1));
	}

	/* Support methods */

	/**
	 * Creates a LongReceiver wrapping a fixed-size long array, executes the action on the
	 * LongReceiver, and asserts the longs in the array.
	 */
	private static <E extends Exception> void assertLongs(int size,
		ExceptionConsumer<E, LongReceiver> action, long... longs) throws E {
		Holder holder = Holder.of(size);
		action.accept(holder.receiver);
		assertArray(holder.longs, longs);
	}

	/**
	 * Class to hold a long array and a simple LongReceiver wrapper.
	 */
	public static class Holder {
		public final long[] longs;
		public final LongReceiver receiver;

		public static LongReceiverBehavior.Holder of(int size) {
			long[] longs = new long[size];
			return new Holder(longs, receiver(longs));
		}

		private Holder(long[] longs, LongReceiver receiver) {
			this.longs = longs;
			this.receiver = receiver;
		}

		public Holder clear() {
			Arrays.fill(longs, 0);
			return this;
		}
	}

	private static LongReceiver receiver(int size) {
		return receiver(new long[size]);
	}

	/**
	 * Simple LongReceiver implementation, wrapping longs.
	 */
	public static LongReceiver receiver(long[] longs) {
		return receiver(longs, 0, longs.length);
	}

	/**
	 * Simple LongReceiver implementation, wrapping longs.
	 */
	public static LongReceiver receiver(long[] longs, int offset, int length) {
		return new LongReceiver() {
			@Override
			public int length() {
				return length;
			}

			@Override
			public int setLong(int index, long value) {
				longs[offset + index] = value;
				return index + 1;
			}
		};
	}
}
