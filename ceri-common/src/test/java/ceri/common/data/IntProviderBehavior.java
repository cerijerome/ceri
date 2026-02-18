package ceri.common.data;

import java.util.Arrays;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.data.IntReceiverBehavior.Holder;
import ceri.common.math.Maths;
import ceri.common.test.Assert;
import ceri.common.test.Captor;

public class IntProviderBehavior {
	private static final boolean msb = Bytes.IS_BIG_ENDIAN;
	private static final IntProvider ip = provider(0, -1, 2, -3, 4, -5, 6, -7, 8, -9);
	private static final String str = "abc\ud83c\udc39de";
	private static final int[] cp = str.codePoints().toArray();

	/* IntProvider tests */

	@Test
	public void testOf() {
		Assert.array(IntProvider.of(Integer.MAX_VALUE, Integer.MIN_VALUE), Integer.MAX_VALUE,
			Integer.MIN_VALUE);
	}

	@Test
	public void testCopyOf() {
		int[] ints = { Integer.MAX_VALUE, Integer.MIN_VALUE };
		var of = IntProvider.of(ints);
		var copyOf = IntProvider.copyOf(ints);
		Arrays.fill(ints, (byte) 0);
		Assert.array(of.copy(0), 0, 0);
		Assert.array(copyOf.copy(0), 0x7fffffff, 0x80000000);
	}

	@Test
	public void testToHex() {
		Assert.equal(IntProvider.toHex(ip),
			"[0x0,0xffffffff,0x2,0xfffffffd,0x4," + "0xfffffffb,0x6,...](10)");
	}

	@Test
	public void testToString() {
		Assert.equal(IntProvider.toString(ip), "[0,-1,2,-3,4,-5,6,...](10)");
		Assert.equal(IntProvider.toString(Maths::ubyte, ip), "[0,255,2,253,4,251,6,...](10)");
	}

	@Test
	public void shouldProvideAnEmptyInstance() {
		Assert.equal(IntProvider.empty().length(), 0);
		Assert.yes(IntProvider.empty().isEmpty());
		Assert.thrown(() -> IntProvider.empty().getInt(0));
	}

	@Test
	public void shouldIterateValues() {
		var captor = Captor.ofInt();
		for (int i : IntProvider.empty())
			captor.accept(i);
		captor.verifyInt();
		for (int i : provider(-1, 0, 1, Integer.MIN_VALUE, Integer.MAX_VALUE, 0xffffffff))
			captor.accept(i);
		captor.verifyInt(-1, 0, 1, Integer.MIN_VALUE, Integer.MAX_VALUE, 0xffffffff);
	}

	@Test
	public void shouldDetermineIfEmpty() {
		Assert.no(ip.isEmpty());
		Assert.yes(IntProvider.empty().isEmpty());
	}

	@Test
	public void shouldProvidePrimitiveValues() {
		Assert.no(ip.getBool(0));
		Assert.yes(ip.getBool(1));
		Assert.equal(ip.getInt(1), -1);
		Assert.equal(ip.getLong(1), msb ? 0xffffffff00000002L : 0x2ffffffffL);
		Assert.equal(ip.getFloat(2), Float.intBitsToFloat(2));
		Assert.equal(ip.getDouble(1),
			Double.longBitsToDouble(msb ? 0xffffffff00000002L : 0x2ffffffffL));
	}

	@Test
	public void shouldProvideUnsignedValues() {
		Assert.equal(ip.getUint(1), 0xffffffffL);
		Assert.equal(ip.getUint(2), 2L);
	}

	@Test
	public void shouldProvideIntAlignedValues() {
		Assert.equal(ip.getLong(1, true), 0xffffffff00000002L);
		Assert.equal(ip.getLong(1, false), 0x2ffffffffL);
		Assert.equal(ip.getDouble(1, true), Double.longBitsToDouble(0xffffffff00000002L));
		Assert.equal(ip.getDouble(1, false), Double.longBitsToDouble(0x2ffffffffL));
	}

	@Test
	public void shouldProvideDecodedStrings() {
		Assert.equal(provider(cp).getString(0), str);
	}

	@Test
	public void shouldSliceProvidedIntRange() {
		Assert.yes(ip.slice(10).isEmpty());
		Assert.array(ip.slice(5, 0).copy(0));
		Assert.equal(ip.slice(0), ip);
		Assert.equal(ip.slice(0, 10), ip);
		Assert.thrown(() -> ip.slice(1, 10));
		Assert.thrown(() -> ip.slice(0, 9));
	}

	@Test
	public void shouldProvideACopyOfInts() {
		Assert.array(ip.copy(5, 0));
		Assert.array(ip.copy(5, 3), -5, 6, -7);
	}

	@Test
	public void shouldCopyToIntArray() {
		int[] ints = new int[5];
		Assert.equal(ip.copyTo(1, ints), 6);
		Assert.array(ints, -1, 2, -3, 4, -5);
		Assert.thrown(() -> ip.copyTo(6, ints));
		Assert.thrown(() -> ip.copyTo(-1, ints));
		Assert.thrown(() -> ip.copyTo(1, ints, 0, 6));
	}

	@Test
	public void shouldCopyToReceiver() {
		var h = Holder.of(5);
		Assert.equal(ip.copyTo(1, h.receiver), 6);
		Assert.array(h.ints, -1, 2, -3, 4, -5);
		Assert.thrown(() -> ip.copyTo(6, h.receiver));
		Assert.thrown(() -> ip.copyTo(-1, h.receiver));
		Assert.thrown(() -> ip.copyTo(1, h.receiver, 0, 6));
	}

	@Test
	public void shouldGetEachInt() {
		provider().getEachInt((_, _) -> Assert.throwRuntime());
		int[] array = new int[ip.length()];
		ip.getEachInt((i, n) -> array[i] = -n);
		Assert.array(array, 0, 1, -2, 3, -4, 5, -6, 7, -8, 9);
	}

	@Test
	public void shouldStreamInts() {
		Assert.stream(ip.stream(0), 0, -1, 2, -3, 4, -5, 6, -7, 8, -9);
		Assert.thrown(() -> ip.stream(0, 11));
	}

	@Test
	public void shouldStreamUnsignedInts() {
		Assert.stream(ip.ustream(0), 0L, 0xffffffffL, 2L, 0xfffffffdL, 4L, 0xfffffffbL, 6L,
			0xfffffff9L, 8L, 0xfffffff7L);
		Assert.thrown(() -> ip.ustream(0, 11));
	}

	@Test
	public void shouldDetermineIfIntsAreEqual() {
		Assert.yes(ip.isEqualTo(5, -5, 6, -7, 8, -9));
		Assert.no(ip.isEqualTo(5, -5, 6, -7, 8, 9));
		int[] ints = Array.INT.of(0, -1, 2, -3, 4);
		Assert.yes(ip.isEqualTo(0, ints));
		Assert.no(ip.isEqualTo(0, ints, 0, 6));
		Assert.no(ip.isEqualTo(9, -9, 0));
	}

	@Test
	public void shouldDetermineIfProvidedIntsAreEqual() {
		Assert.yes(ip.isEqualTo(0, ip));
		Assert.yes(ip.isEqualTo(5, ip, 5));
		Assert.yes(ip.isEqualTo(5, ip, 5, 3));
		Assert.yes(ip.isEqualTo(1, provider(-1, 2, -3)));
		Assert.no(ip.isEqualTo(1, provider(1, 2, -3)));
		Assert.no(ip.isEqualTo(0, provider(1, 2, 3), 0, 4));
		Assert.no(ip.isEqualTo(9, provider(1, 2, 3)));
	}

	@Test
	public void shouldDetermineIfContains() {
		Assert.equal(ip.contains(-1, 2, -3), true);
		Assert.equal(ip.contains(-1, 2, 3), false);
	}

	@Test
	public void shouldDetermineIndexOfInts() {
		Assert.equal(ip.indexOf(0, -1, 2, -3), 1);
		Assert.equal(ip.indexOf(0, -1, 2, 3), -1);
		Assert.equal(ip.indexOf(8, -1, 2, -3), -1);
		Assert.equal(ip.indexOf(0, Array.INT.of(-1, 2, -3), 0, 4), -1);
	}

	@Test
	public void shouldDetermineIndexOfProvidedInts() {
		Assert.equal(ip.indexOf(0, provider(-1, 2, -3)), 1);
		Assert.equal(ip.indexOf(0, provider(-1, 2, 3)), -1);
		Assert.equal(ip.indexOf(8, provider(-1, 2, -3)), -1);
		Assert.equal(ip.indexOf(0, provider(-1, 2, -3), 0, 4), -1);
	}

	@Test
	public void shouldDetermineLastIndexOfBytes() {
		var ip = provider(0, -1, 2, -1, 0, 2, -1, 0);
		Assert.equal(ip.lastIndexOf(0, 2, -1), 5);
		Assert.equal(ip.lastIndexOf(0, 2, 1), -1);
		Assert.equal(ip.lastIndexOf(7, 0, -1), -1);
		Assert.equal(ip.lastIndexOf(0, Array.INT.of(2, -1, 0), 0, 4), -1);
	}

	@Test
	public void shouldDetermineLastIndexOfProviderBytes() {
		var ip = provider(0, -1, 2, -1, 0, 2, -1, 0);
		Assert.equal(ip.lastIndexOf(0, provider(2, -1)), 5);
		Assert.equal(ip.lastIndexOf(0, provider(2, 1)), -1);
		Assert.equal(ip.lastIndexOf(7, provider(0, -1)), -1);
		Assert.equal(ip.lastIndexOf(0, provider(2, -1, 0), 0, 4), -1);
	}

	@Test
	public void shouldProvideReaderAccessToInts() {
		Assert.array(ip.reader(5).readInts(), -5, 6, -7, 8, -9);
		Assert.array(ip.reader(5, 0).readInts());
		Assert.array(ip.reader(10, 0).readInts());
		Assert.thrown(() -> ip.reader(10, 1));
		Assert.thrown(() -> ip.reader(11, 0));
	}

	/* IntProvider.Reader tests */

	@Test
	public void shouldReadInt() {
		Assert.equal(ip.reader(1).readInt(), -1);
		Assert.thrown(() -> ip.reader(1, 0).readInt());
	}

	@Test
	public void shouldReadLong() {
		Assert.equal(ip.reader(6).readLong(true), 0x6fffffff9L);
		Assert.equal(ip.reader(6).readLong(false), 0xfffffff900000006L);
	}

	@Test
	public void shouldReadStrings() {
		Assert.equal(provider(cp).reader(2, 3).readString(), "c\ud83c\udc39d");
	}

	@Test
	public void shouldReadIntoIntArray() {
		int[] ints = new int[4];
		ip.reader(5).readInto(ints);
		Assert.array(ints, -5, 6, -7, 8);
	}

	@Test
	public void shouldReadIntoIntReceiver() {
		int[] ints = new int[4];
		var br = IntArray.Mutable.wrap(ints);
		ip.reader(5).readInto(br);
		Assert.array(ints, -5, 6, -7, 8);
	}

	@Test
	public void shouldStreamReaderInts() {
		Assert.stream(ip.reader(6).stream(), 6, -7, 8, -9);
		Assert.thrown(() -> ip.reader(0).ustream(11));
	}

	@Test
	public void shouldStreamReaderUnsignedInts() {
		Assert.stream(ip.reader(6).ustream(), 6L, 0xfffffff9L, 8L, 0xfffffff7L);
		Assert.thrown(() -> ip.reader(0).ustream(11));
	}

	@Test
	public void shouldReturnReaderIntProvider() {
		Assert.equal(ip.reader(0).provider(), ip);
		Assert.yes(ip.reader(5, 0).provider().isEmpty());
		Assert.thrown(() -> ip.reader(5).provider()); // slice() fails
	}

	@Test
	public void shouldSliceReader() {
		var r0 = ip.reader(6);
		var r1 = r0.slice();
		var r2 = r0.slice(3);
		Assert.thrown(() -> r0.slice(5));
		Assert.thrown(() -> r0.slice(-2));
		Assert.array(r0.readInts(), 6, -7, 8, -9);
		Assert.array(r1.readInts(), 6, -7, 8, -9);
		Assert.array(r2.readInts(), 6, -7, 8);
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
