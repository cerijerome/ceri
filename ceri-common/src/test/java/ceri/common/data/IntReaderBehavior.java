package ceri.common.data;

import static java.lang.Integer.MIN_VALUE;
import org.junit.Test;
import ceri.common.data.IntArray.Mutable;
import ceri.common.test.Assert;
import ceri.common.util.Validate;

public class IntReaderBehavior {
	private static final boolean msb = ByteUtil.IS_BIG_ENDIAN;
	private static final String str = "abc\ud83c\udc39de";
	private static final int[] cp = str.codePoints().toArray();

	@Test
	public void shouldSkipInts() {
		assertRemaining(reader(1, 2, 3).skip(0), 1, 2, 3);
		assertRemaining(reader(1, 2, 3, 4, 5).skip(3), 4, 5);
		Assert.thrown(() -> reader(1, 2, 3).skip(4));
	}

	@Test
	public void shouldReadPrimitiveValues() {
		Assert.no(reader(0).readBool());
		Assert.yes(reader(-1).readBool());
		Assert.equal(reader(MIN_VALUE).readInt(), MIN_VALUE);
		Assert.equal(reader(0x807f0001, 0xff000000).readLong(),
			msb ? 0x807f0001ff000000L : 0xff000000807f0001L);
		Assert.equal(reader(0x807f0001).readFloat(), Float.intBitsToFloat(0x807f0001));
		Assert.equal(reader(0x807f0001, 0xff000000).readDouble(),
			Double.longBitsToDouble(msb ? 0x807f0001ff000000L : 0xff000000807f0001L));
	}

	@Test
	public void shouldReadUnsignedValues() {
		Assert.equal(reader(-1).readUint(), 0xffffffffL);
		Assert.equal(reader(MIN_VALUE).readUint(), 0x80000000L);
	}

	@Test
	public void shouldReadIntAlignedValues() {
		Assert.equal(reader(0x807f0001, 0xff000000).readLong(true), 0x807f0001ff000000L);
		Assert.equal(reader(0x807f0001, 0xff000000).readLong(false), 0xff000000807f0001L);
		Assert.equal(reader(0x807f0001, 0xff000000).readDouble(true),
			Double.longBitsToDouble(0x807f0001ff000000L));
		Assert.equal(reader(0x807f0001, 0xff000000).readDouble(false),
			Double.longBitsToDouble(0xff000000807f0001L));
	}

	@Test
	public void shouldProvideDecodedStrings() {
		Assert.equal(reader(cp).readString(4), "abc\ud83c\udc39");
	}

	@Test
	public void shouldReadInts() {
		Assert.array(reader(1, 2, 3).readInts(0));
		Assert.array(reader(1, 2, 3).readInts(3), 1, 2, 3);
		Assert.thrown(() -> reader(1, 2, 3).readInts(4));
	}

	@Test
	public void shouldReadIntoIntArray() {
		int[] ints = new int[3];
		Assert.equal(reader(0, -1, 2, -3, 4).readInto(ints), 3);
		Assert.array(ints, 0, -1, 2);
		Assert.thrown(() -> reader(0, -1, 2, -3, 4).readInto(ints, 1, 3));
		Assert.thrown(() -> reader(0, -1).readInto(ints));
	}

	@Test
	public void shouldReadIntoIntReceiver() {
		int[] ints = new int[3];
		Assert.equal(reader(0, -1, 2, -3, 4).readInto(Mutable.wrap(ints)), 3);
		Assert.array(ints, 0, -1, 2);
		Assert.thrown(() -> reader(0, -1, 2, -3, 4).readInto(Mutable.wrap(ints), 1, 3));
		Assert.thrown(() -> reader(0, -1).readInto(Mutable.wrap(ints)));
	}

	@Test
	public void shouldStreamInts() {
		Assert.stream(reader(0, -1, 2, -3, 4).stream(5), 0, -1, 2, -3, 4);
		Assert.thrown(() -> reader(0, -1, 2).stream(5).toArray());
	}

	@Test
	public void shouldStreamUnsignedInts() {
		Assert.stream(reader(0, -1, 2, -3, 4).ustream(5), 0L, 0xffffffffL, 2L, 0xfffffffdL, 4L);
		Assert.thrown(() -> reader(0, -1, 2).ustream(5).toArray());
	}

	private static void assertRemaining(IntReader reader, int... ints) {
		for (int b : ints)
			Assert.equal(reader.readInt(), b);
		Assert.thrown(() -> reader.readInt());
	}

	private static IntReader reader(int... ints) {
		return reader(ints, 0, ints.length);
	}

	private static IntReader reader(int[] ints, int offset, int length) {
		return new IntReader() {
			private int pos = 0;

			@Override
			public int readInt() {
				Validate.index(length, pos);
				return ints[offset + pos++];
			}
		};
	}
}
