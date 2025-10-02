package ceri.common.data;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertThrown;
import static java.lang.Long.MAX_VALUE;
import java.util.Arrays;
import org.junit.Test;
import ceri.common.data.LongArray.Mutable;
import ceri.common.function.Excepts.Consumer;
import ceri.common.util.Validate;

public class LongWriterBehavior {

	@Test
	public void shouldSkipLongs() {
		assertLongs(3, w -> w.skip(0), 0, 0, 0);
		assertLongs(5, w -> w.writeLong(1).skip(3).writeLong(MAX_VALUE), 1, 0, 0, 0, MAX_VALUE);
	}

	@Test
	public void shouldWriteDouble() {
		assertLongs(3, w -> w.writeDouble(Double.longBitsToDouble(0xfedcba9876L)), 0xfedcba9876L, 0,
			0);
	}

	@Test
	public void shouldFillLongs() {
		assertLongs(3, w -> w.fill(0, 0xff), 0, 0, 0);
		assertLongs(3, w -> w.fill(2, 0xff), 0xff, 0xff, 0);
		assertThrown(() -> writer(3).fill(4, 0xff));
	}

	@Test
	public void shouldWriteFromIntArray() {
		assertLongs(3, w -> w.writeLongs(1, 2, 3), 1, 2, 3);
		assertThrown(() -> writer(3).writeLongs(1, 2, 3, 4));
	}

	@Test
	public void shouldWriteFromIntProvider() {
		assertLongs(3, w -> w.writeFrom(Mutable.wrap(1, 2, 3)), 1, 2, 3);
		assertThrown(() -> writer(3).writeFrom(Mutable.wrap(1, 2, 3, 4)));
	}

	/**
	 * Creates a LongWriter wrapping a fixed-size lon array, executes the action on the
	 * LongReceiver, and asserts the longs in the array.
	 */
	private static <E extends Exception> void assertLongs(int size,
		Consumer<E, LongWriter<?>> action, long... longs) throws E {
		var holder = Holder.of(size);
		action.accept(holder.writer);
		assertArray(holder.longs, longs);
	}

	/**
	 * Class to hold a long array and a simple LongReceiver wrapper.
	 */
	public static class Holder {
		public final long[] longs;
		public final SimpleLongWriter writer;

		public static Holder of(int size) {
			long[] longs = new long[size];
			return new Holder(longs, new SimpleLongWriter(longs, 0, longs.length));
		}

		private Holder(long[] longs, SimpleLongWriter writer) {
			this.longs = longs;
			this.writer = writer;
		}

		public Holder reset() {
			Arrays.fill(longs, 0);
			writer.reset();
			return this;
		}
	}

	private static LongWriter<?> writer(int size) {
		return writer(new long[size]);
	}

	/**
	 * Simple LongWriter implementation, wrapping longs.
	 */
	public static LongWriter<?> writer(long[] longs) {
		return writer(longs, 0, longs.length);
	}

	/**
	 * Returns a simple LongWriter implementation, wrapping longs.
	 */
	public static LongWriter<?> writer(long[] longs, int offset, int length) {
		return new SimpleLongWriter(longs, offset, length);
	}

	/**
	 * Simple LongWriter implementation, wrapping longs.
	 */
	public static class SimpleLongWriter implements LongWriter<SimpleLongWriter> {
		private final long[] longs;
		private final int offset;
		private final int length;
		private int index = 0;

		private SimpleLongWriter(long[] longs, int offset, int length) {
			this.longs = longs;
			this.offset = offset;
			this.length = length;
		}

		@Override
		public SimpleLongWriter writeLong(long value) {
			Validate.index(length, index);
			longs[offset + index++] = value;
			return this;
		}

		public SimpleLongWriter reset() {
			index = 0;
			return this;
		}
	}
}
