package ceri.common.data;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertStream;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static java.lang.Integer.MIN_VALUE;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.IntArray.Mutable;

public class IntReaderBehavior {
	private static final boolean msb = ByteUtil.BIG_ENDIAN;
	private static final String str = "abc\ud83c\udc39de";
	private static final int[] cp = str.codePoints().toArray();

	@Test
	public void shouldSkipInts() {
		assertRemaining(reader(1, 2, 3).skip(0), 1, 2, 3);
		assertRemaining(reader(1, 2, 3, 4, 5).skip(3), 4, 5);
		assertThrown(() -> reader(1, 2, 3).skip(4));
	}

	@Test
	public void shouldReadPrimitiveValues() {
		assertThat(reader(0).readBool(), is(false));
		assertThat(reader(-1).readBool(), is(true));
		assertThat(reader(MIN_VALUE).readInt(), is(MIN_VALUE));
		assertThat(reader(0x807f0001, 0xff000000).readLong(),
			is(msb ? 0x807f0001ff000000L : 0xff000000807f0001L));
		assertThat(reader(0x807f0001).readFloat(), is(Float.intBitsToFloat(0x807f0001)));
		assertThat(reader(0x807f0001, 0xff000000).readDouble(),
			is(Double.longBitsToDouble(msb ? 0x807f0001ff000000L : 0xff000000807f0001L)));
	}

	@Test
	public void shouldReadUnsignedValues() {
		assertThat(reader(-1).readUint(), is(0xffffffffL));
		assertThat(reader(MIN_VALUE).readUint(), is(0x80000000L));
	}

	@Test
	public void shouldReadIntAlignedValues() {
		assertThat(reader(0x807f0001, 0xff000000).readLong(true), is(0x807f0001ff000000L));
		assertThat(reader(0x807f0001, 0xff000000).readLong(false), is(0xff000000807f0001L));
		assertThat(reader(0x807f0001, 0xff000000).readDouble(true),
			is(Double.longBitsToDouble(0x807f0001ff000000L)));
		assertThat(reader(0x807f0001, 0xff000000).readDouble(false),
			is(Double.longBitsToDouble(0xff000000807f0001L)));
	}

	@Test
	public void shouldProvideDecodedStrings() {
		assertThat(reader(cp).readString(4), is("abc\ud83c\udc39"));
	}

	@Test
	public void shouldReadInts() {
		assertArray(reader(1, 2, 3).readInts(0));
		assertArray(reader(1, 2, 3).readInts(3), 1, 2, 3);
		assertThrown(() -> reader(1, 2, 3).readInts(4));
	}

	@Test
	public void shouldReadIntoIntArray() {
		int[] ints = new int[3];
		assertThat(reader(0, -1, 2, -3, 4).readInto(ints), is(3));
		assertArray(ints, 0, -1, 2);
		assertThrown(() -> reader(0, -1, 2, -3, 4).readInto(ints, 1, 3));
		assertThrown(() -> reader(0, -1).readInto(ints));
	}

	@Test
	public void shouldReadIntoIntReceiver() {
		int[] ints = new int[3];
		assertThat(reader(0, -1, 2, -3, 4).readInto(Mutable.wrap(ints)), is(3));
		assertArray(ints, 0, -1, 2);
		assertThrown(() -> reader(0, -1, 2, -3, 4).readInto(Mutable.wrap(ints), 1, 3));
		assertThrown(() -> reader(0, -1).readInto(Mutable.wrap(ints)));
	}

	@Test
	public void shouldStreamInts() {
		assertStream(reader(0, -1, 2, -3, 4).stream(5), 0, -1, 2, -3, 4);
		assertThrown(() -> reader(0, -1, 2).stream(5).toArray());
	}

	@Test
	public void shouldStreamUnsignedInts() {
		assertStream(reader(0, -1, 2, -3, 4).ustream(5), 0L, 0xffffffffL, 2L, 0xfffffffdL, 4L);
		assertThrown(() -> reader(0, -1, 2).ustream(5).toArray());
	}

	private static void assertRemaining(IntReader reader, int... ints) {
		for (int b : ints)
			assertThat(reader.readInt(), is(b));
		assertThrown(() -> reader.readInt());
	}

	private static IntReader reader(int... ints) {
		return reader(ints, 0, ints.length);
	}

	private static IntReader reader(int[] ints, int offset, int length) {
		return new IntReader() {
			private int pos = 0;

			@Override
			public int readInt() {
				ArrayUtil.validateIndex(length, pos);
				return ints[offset + pos++];
			}
		};
	}
}
