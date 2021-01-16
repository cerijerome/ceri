package ceri.common.data;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.IntProvider.Reader;
import ceri.common.data.IntReceiverBehavior.Holder;
import ceri.common.test.Captor;

public class IntProviderBehavior {
	private static final boolean msb = ByteUtil.BIG_ENDIAN;
	private static final IntProvider ip = provider(0, -1, 2, -3, 4, -5, 6, -7, 8, -9);
	private static final String str = "abc\ud83c\udc39de";
	private static final int[] cp = str.codePoints().toArray();

	/* IntProvider tests */

	@Test
	public void testToHex() {
		assertEquals(IntProvider.toHex(ip), "[0x0, 0xffffffff, 0x2, 0xfffffffd, 0x4, " +
			"0xfffffffb, 0x6, 0xfffffff9, 0x8, 0xfffffff7](10)");
		assertEquals(IntProvider.toHex(ip, 3), "[0x0, 0xffffffff, ...](10)");
	}

	@Test
	public void testToString() {
		assertEquals(IntProvider.toString(ip), "[0, -1, 2, -3, 4, -5, 6, -7, 8, -9](10)");
		assertEquals(IntProvider.toString(ip, 3), "[0, -1, ...](10)");
	}

	@Test
	public void shouldProvideAnEmptyInstance() {
		assertEquals(IntProvider.empty().length(), 0);
		assertTrue(IntProvider.empty().isEmpty());
		assertThrown(() -> IntProvider.empty().getInt(0));
	}

	@Test
	public void shouldIterateValues() {
		Captor.Int captor = Captor.ofInt();
		for (int i : IntProvider.empty())
			captor.accept(i);
		captor.verifyInt();
		for (int i : provider(-1, 0, 1, Integer.MIN_VALUE, Integer.MAX_VALUE, 0xffffffff))
			captor.accept(i);
		captor.verifyInt(-1, 0, 1, Integer.MIN_VALUE, Integer.MAX_VALUE, 0xffffffff);
	}

	@Test
	public void shouldDetermineIfEmpty() {
		assertFalse(ip.isEmpty());
		assertTrue(IntProvider.empty().isEmpty());
	}

	@Test
	public void shouldProvidePrimitiveValues() {
		assertFalse(ip.getBool(0));
		assertTrue(ip.getBool(1));
		assertEquals(ip.getInt(1), -1);
		assertEquals(ip.getLong(1), msb ? 0xffffffff00000002L : 0x2ffffffffL);
		assertEquals(ip.getFloat(2), Float.intBitsToFloat(2));
		assertEquals(ip.getDouble(1),
			Double.longBitsToDouble(msb ? 0xffffffff00000002L : 0x2ffffffffL));
	}

	@Test
	public void shouldProvideUnsignedValues() {
		assertEquals(ip.getUint(1), 0xffffffffL);
		assertEquals(ip.getUint(2), 2L);
	}

	@Test
	public void shouldProvideIntAlignedValues() {
		assertEquals(ip.getLong(1, true), 0xffffffff00000002L);
		assertEquals(ip.getLong(1, false), 0x2ffffffffL);
		assertEquals(ip.getDouble(1, true), Double.longBitsToDouble(0xffffffff00000002L));
		assertEquals(ip.getDouble(1, false), Double.longBitsToDouble(0x2ffffffffL));
	}

	@Test
	public void shouldProvideDecodedStrings() {
		assertEquals(provider(cp).getString(0), str);
	}

	@Test
	public void shouldSliceProvidedIntRange() {
		assertTrue(ip.slice(10).isEmpty());
		assertArray(ip.slice(5, 0).copy(0));
		assertEquals(ip.slice(0), ip);
		assertEquals(ip.slice(0, 10), ip);
		assertThrown(() -> ip.slice(1, 10));
		assertThrown(() -> ip.slice(0, 9));
	}

	@Test
	public void shouldProvideACopyOfInts() {
		assertArray(ip.copy(5, 0));
		assertArray(ip.copy(5, 3), -5, 6, -7);
	}

	@Test
	public void shouldCopyToIntArray() {
		int[] ints = new int[5];
		assertEquals(ip.copyTo(1, ints), 6);
		assertArray(ints, -1, 2, -3, 4, -5);
		assertThrown(() -> ip.copyTo(6, ints));
		assertThrown(() -> ip.copyTo(-1, ints));
		assertThrown(() -> ip.copyTo(1, ints, 0, 6));
	}

	@Test
	public void shouldCopyToReceiver() {
		Holder h = Holder.of(5);
		assertEquals(ip.copyTo(1, h.receiver), 6);
		assertArray(h.ints, -1, 2, -3, 4, -5);
		assertThrown(() -> ip.copyTo(6, h.receiver));
		assertThrown(() -> ip.copyTo(-1, h.receiver));
		assertThrown(() -> ip.copyTo(1, h.receiver, 0, 6));
	}

	@Test
	public void shouldStreamInts() {
		assertStream(ip.stream(0), 0, -1, 2, -3, 4, -5, 6, -7, 8, -9);
		assertThrown(() -> ip.stream(0, 11));
	}

	@Test
	public void shouldStreamUnsignedInts() {
		assertStream(ip.ustream(0), 0L, 0xffffffffL, 2L, 0xfffffffdL, 4L, 0xfffffffbL, 6L,
			0xfffffff9L, 8L, 0xfffffff7L);
		assertThrown(() -> ip.ustream(0, 11));
	}

	@Test
	public void shouldDetermineIfIntsAreEqual() {
		assertTrue(ip.isEqualTo(5, -5, 6, -7, 8, -9));
		assertFalse(ip.isEqualTo(5, -5, 6, -7, 8, 9));
		int[] ints = ArrayUtil.ints(0, -1, 2, -3, 4);
		assertTrue(ip.isEqualTo(0, ints));
		assertFalse(ip.isEqualTo(0, ints, 0, 6));
		assertFalse(ip.isEqualTo(9, -9, 0));
	}

	@Test
	public void shouldDetermineIfProvidedIntsAreEqual() {
		assertTrue(ip.isEqualTo(0, ip));
		assertTrue(ip.isEqualTo(5, ip, 5));
		assertTrue(ip.isEqualTo(5, ip, 5, 3));
		assertTrue(ip.isEqualTo(1, provider(-1, 2, -3)));
		assertFalse(ip.isEqualTo(1, provider(1, 2, -3)));
		assertFalse(ip.isEqualTo(0, provider(1, 2, 3), 0, 4));
		assertFalse(ip.isEqualTo(9, provider(1, 2, 3)));
	}

	@Test
	public void shouldDetermineIndexOfInts() {
		assertEquals(ip.indexOf(0, -1, 2, -3), 1);
		assertEquals(ip.indexOf(0, -1, 2, 3), -1);
		assertEquals(ip.indexOf(8, -1, 2, -3), -1);
		assertEquals(ip.indexOf(0, ArrayUtil.ints(-1, 2, -3), 0, 4), -1);
	}

	@Test
	public void shouldDetermineIndexOfProvidedInts() {
		assertEquals(ip.indexOf(0, provider(-1, 2, -3)), 1);
		assertEquals(ip.indexOf(0, provider(-1, 2, 3)), -1);
		assertEquals(ip.indexOf(8, provider(-1, 2, -3)), -1);
		assertEquals(ip.indexOf(0, provider(-1, 2, -3), 0, 4), -1);
	}

	@Test
	public void shouldDetermineLastIndexOfBytes() {
		IntProvider ip = provider(0, -1, 2, -1, 0, 2, -1, 0);
		assertEquals(ip.lastIndexOf(0, 2, -1), 5);
		assertEquals(ip.lastIndexOf(0, 2, 1), -1);
		assertEquals(ip.lastIndexOf(7, 0, -1), -1);
		assertEquals(ip.lastIndexOf(0, ArrayUtil.ints(2, -1, 0), 0, 4), -1);
	}

	@Test
	public void shouldDetermineLastIndexOfProviderBytes() {
		IntProvider ip = provider(0, -1, 2, -1, 0, 2, -1, 0);
		assertEquals(ip.lastIndexOf(0, provider(2, -1)), 5);
		assertEquals(ip.lastIndexOf(0, provider(2, 1)), -1);
		assertEquals(ip.lastIndexOf(7, provider(0, -1)), -1);
		assertEquals(ip.lastIndexOf(0, provider(2, -1, 0), 0, 4), -1);
	}

	@Test
	public void shouldProvideReaderAccessToInts() {
		assertArray(ip.reader(5).readInts(), -5, 6, -7, 8, -9);
		assertArray(ip.reader(5, 0).readInts());
		assertArray(ip.reader(10, 0).readInts());
		assertThrown(() -> ip.reader(10, 1));
		assertThrown(() -> ip.reader(11, 0));
	}

	/* IntProvider.Reader tests */

	@Test
	public void shouldReadInt() {
		assertEquals(ip.reader(1).readInt(), -1);
		assertThrown(() -> ip.reader(1, 0).readInt());
	}

	@Test
	public void shouldReadLong() {
		assertEquals(ip.reader(6).readLong(true), 0x6fffffff9L);
		assertEquals(ip.reader(6).readLong(false), 0xfffffff900000006L);
	}

	@Test
	public void shouldReadStrings() {
		assertEquals(provider(cp).reader(2, 3).readString(), "c\ud83c\udc39d");
	}

	@Test
	public void shouldReadIntoIntArray() {
		int[] ints = new int[4];
		ip.reader(5).readInto(ints);
		assertArray(ints, -5, 6, -7, 8);
	}

	@Test
	public void shouldReadIntoIntReceiver() {
		int[] ints = new int[4];
		IntReceiver br = IntArray.Mutable.wrap(ints);
		ip.reader(5).readInto(br);
		assertArray(ints, -5, 6, -7, 8);
	}

	@Test
	public void shouldStreamReaderInts() {
		assertStream(ip.reader(6).stream(), 6, -7, 8, -9);
		assertThrown(() -> ip.reader(0).ustream(11));
	}

	@Test
	public void shouldStreamReaderUnsignedInts() {
		assertStream(ip.reader(6).ustream(), 6L, 0xfffffff9L, 8L, 0xfffffff7L);
		assertThrown(() -> ip.reader(0).ustream(11));
	}

	@Test
	public void shouldReturnReaderIntProvider() {
		assertEquals(ip.reader(0).provider(), ip);
		assertTrue(ip.reader(5, 0).provider().isEmpty());
		assertThrown(() -> ip.reader(5).provider()); // slice() fails
	}

	@Test
	public void shouldSliceReader() {
		Reader r0 = ip.reader(6);
		Reader r1 = r0.slice();
		Reader r2 = r0.slice(3);
		assertThrown(() -> r0.slice(5));
		assertThrown(() -> r0.slice(-2));
		assertArray(r0.readInts(), 6, -7, 8, -9);
		assertArray(r1.readInts(), 6, -7, 8, -9);
		assertArray(r2.readInts(), 6, -7, 8);
	}

	/* Support methods */

	public static IntProvider provider(int... ints) {
		return new IntProvider() {
			@Override
			public int getInt(int index) {
				return ints[index];
			}

			@Override
			public int length() {
				return ints.length;
			}
		};
	}
}
