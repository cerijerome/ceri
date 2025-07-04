package ceri.common.data;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static java.lang.Integer.MIN_VALUE;
import org.junit.Test;
import ceri.common.data.IntArray.Mutable;
import ceri.common.validation.ValidationUtil;

public class IntReaderBehavior {
	private static final boolean msb = ByteUtil.IS_BIG_ENDIAN;
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
		assertFalse(reader(0).readBool());
		assertTrue(reader(-1).readBool());
		assertEquals(reader(MIN_VALUE).readInt(), MIN_VALUE);
		assertEquals(reader(0x807f0001, 0xff000000).readLong(),
			msb ? 0x807f0001ff000000L : 0xff000000807f0001L);
		assertEquals(reader(0x807f0001).readFloat(), Float.intBitsToFloat(0x807f0001));
		assertEquals(reader(0x807f0001, 0xff000000).readDouble(),
			Double.longBitsToDouble(msb ? 0x807f0001ff000000L : 0xff000000807f0001L));
	}

	@Test
	public void shouldReadUnsignedValues() {
		assertEquals(reader(-1).readUint(), 0xffffffffL);
		assertEquals(reader(MIN_VALUE).readUint(), 0x80000000L);
	}

	@Test
	public void shouldReadIntAlignedValues() {
		assertEquals(reader(0x807f0001, 0xff000000).readLong(true), 0x807f0001ff000000L);
		assertEquals(reader(0x807f0001, 0xff000000).readLong(false), 0xff000000807f0001L);
		assertEquals(reader(0x807f0001, 0xff000000).readDouble(true),
			Double.longBitsToDouble(0x807f0001ff000000L));
		assertEquals(reader(0x807f0001, 0xff000000).readDouble(false),
			Double.longBitsToDouble(0xff000000807f0001L));
	}

	@Test
	public void shouldProvideDecodedStrings() {
		assertEquals(reader(cp).readString(4), "abc\ud83c\udc39");
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
		assertEquals(reader(0, -1, 2, -3, 4).readInto(ints), 3);
		assertArray(ints, 0, -1, 2);
		assertThrown(() -> reader(0, -1, 2, -3, 4).readInto(ints, 1, 3));
		assertThrown(() -> reader(0, -1).readInto(ints));
	}

	@Test
	public void shouldReadIntoIntReceiver() {
		int[] ints = new int[3];
		assertEquals(reader(0, -1, 2, -3, 4).readInto(Mutable.wrap(ints)), 3);
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
			assertEquals(reader.readInt(), b);
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
				ValidationUtil.validateIndex(length, pos);
				return ints[offset + pos++];
			}
		};
	}
}
