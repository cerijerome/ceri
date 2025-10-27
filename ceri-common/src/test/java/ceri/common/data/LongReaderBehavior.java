package ceri.common.data;

import org.junit.Test;
import ceri.common.data.LongArray.Mutable;
import ceri.common.test.Assert;
import ceri.common.util.Validate;

public class LongReaderBehavior {

	@Test
	public void shouldSkipLongs() {
		assertRemaining(reader(1, 2, 3).skip(0), 1, 2, 3);
		assertRemaining(reader(1, 2, 3, 4, 5).skip(3), 4, 5);
		Assert.thrown(() -> reader(1, 2, 3).skip(4));
	}

	@Test
	public void shouldReadDouble() {
		Assert.equal(reader(0xfedcba9876L).readDouble(), Double.longBitsToDouble(0xfedcba9876L));
	}

	@Test
	public void shouldReadLongs() {
		Assert.array(reader(1, 2, 3).readLongs(0));
		Assert.array(reader(1, 2, 3).readLongs(3), 1, 2, 3);
		Assert.thrown(() -> reader(1, 2, 3).readLongs(4));
	}

	@Test
	public void shouldReadIntoIntArray() {
		long[] longs = new long[3];
		Assert.equal(reader(0, -1, 2, -3, 4).readInto(longs), 3);
		Assert.array(longs, 0, -1, 2);
		Assert.thrown(() -> reader(0, -1, 2, -3, 4).readInto(longs, 1, 3));
		Assert.thrown(() -> reader(0, -1).readInto(longs));
	}

	@Test
	public void shouldReadIntoIntReceiver() {
		long[] longs = new long[3];
		Assert.equal(reader(0, -1, 2, -3, 4).readInto(Mutable.wrap(longs)), 3);
		Assert.array(longs, 0, -1, 2);
		Assert.thrown(() -> reader(0, -1, 2, -3, 4).readInto(Mutable.wrap(longs), 1, 3));
		Assert.thrown(() -> reader(0, -1).readInto(Mutable.wrap(longs)));
	}

	@Test
	public void shouldStreamLongs() {
		Assert.stream(reader(0, -1, 2, -3, 4).stream(5), 0, -1, 2, -3, 4);
		Assert.thrown(() -> reader(0, -1, 2).stream(5).toArray());
	}

	private static void assertRemaining(LongReader reader, long... longs) {
		for (long l : longs)
			Assert.equal(reader.readLong(), l);
		Assert.thrown(() -> reader.readLong());
	}

	private static LongReader reader(long... longs) {
		return reader(longs, 0, longs.length);
	}

	private static LongReader reader(long[] longs, int offset, int length) {
		return new LongReader() {
			private int pos = 0;

			@Override
			public long readLong() {
				Validate.index(length, pos);
				return longs[offset + pos++];
			}
		};
	}
}
