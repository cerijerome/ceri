package ceri.common.data;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertStream;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.IntProvider.Reader;
import ceri.common.data.IntReceiverBehavior.Holder;

public class IntProviderBehavior {
	private static final boolean msb = ByteUtil.BIG_ENDIAN;
	private static final IntProvider ip = provider(0, -1, 2, -3, 4, -5, 6, -7, 8, -9);
	private static final String str = "abc\ud83c\udc39de";
	private static final int[] cp = str.codePoints().toArray();

	/* IntProvider tests */

	@Test
	public void shouldProvideAnEmptyInstance() {
		assertThat(IntProvider.empty().length(), is(0));
		assertThat(IntProvider.empty().isEmpty(), is(true));
		assertThrown(() -> IntProvider.empty().getInt(0));
	}

	@Test
	public void shouldDetermineIfEmpty() {
		assertThat(ip.isEmpty(), is(false));
		assertThat(IntProvider.empty().isEmpty(), is(true));
	}

	@Test
	public void shouldProvidePrimitiveValues() {
		assertThat(ip.getBool(0), is(false));
		assertThat(ip.getBool(1), is(true));
		assertThat(ip.getInt(1), is(-1));
		assertThat(ip.getLong(1), is(msb ? 0xffffffff00000002L : 0x2ffffffffL));
		assertThat(ip.getFloat(2), is(Float.intBitsToFloat(2)));
		assertThat(ip.getDouble(1),
			is(Double.longBitsToDouble(msb ? 0xffffffff00000002L : 0x2ffffffffL)));
	}

	@Test
	public void shouldProvideUnsignedValues() {
		assertThat(ip.getUint(1), is(0xffffffffL));
		assertThat(ip.getUint(2), is(2L));
	}

	@Test
	public void shouldProvideIntAlignedValues() {
		assertThat(ip.getLong(1, true), is(0xffffffff00000002L));
		assertThat(ip.getLong(1, false), is(0x2ffffffffL));
		assertThat(ip.getDouble(1, true), is(Double.longBitsToDouble(0xffffffff00000002L)));
		assertThat(ip.getDouble(1, false), is(Double.longBitsToDouble(0x2ffffffffL)));
	}

	@Test
	public void shouldProvideDecodedStrings() {
		assertThat(provider(cp).getString(0), is(str));
	}

	@Test
	public void shouldSliceProvidedIntRange() {
		assertThat(ip.slice(10).isEmpty(), is(true));
		assertArray(ip.slice(5, 0).copy(0));
		assertThat(ip.slice(0), is(ip));
		assertThat(ip.slice(0, 10), is(ip));
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
		assertThat(ip.copyTo(1, ints), is(6));
		assertArray(ints, -1, 2, -3, 4, -5);
		assertThrown(() -> ip.copyTo(6, ints));
		assertThrown(() -> ip.copyTo(-1, ints));
		assertThrown(() -> ip.copyTo(1, ints, 0, 6));
	}

	@Test
	public void shouldCopyToReceiver() {
		Holder h = Holder.of(5);
		assertThat(ip.copyTo(1, h.receiver), is(6));
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
		assertThat(ip.isEqualTo(5, -5, 6, -7, 8, -9), is(true));
		assertThat(ip.isEqualTo(5, -5, 6, -7, 8, 9), is(false));
		int[] ints = ArrayUtil.ints(0, -1, 2, -3, 4);
		assertThat(ip.isEqualTo(0, ints), is(true));
		assertThat(ip.isEqualTo(0, ints, 0, 6), is(false));
		assertThat(ip.isEqualTo(9, -9, 0), is(false));
	}

	@Test
	public void shouldDetermineIfProvidedIntsAreEqual() {
		assertThat(ip.isEqualTo(0, ip), is(true));
		assertThat(ip.isEqualTo(5, ip, 5), is(true));
		assertThat(ip.isEqualTo(5, ip, 5, 3), is(true));
		assertThat(ip.isEqualTo(1, provider(-1, 2, -3)), is(true));
		assertThat(ip.isEqualTo(1, provider(1, 2, -3)), is(false));
		assertThat(ip.isEqualTo(0, provider(1, 2, 3), 0, 4), is(false));
		assertThat(ip.isEqualTo(9, provider(1, 2, 3)), is(false));
	}

	@Test
	public void shouldDetermineIndexOfInts() {
		assertThat(ip.indexOf(0, -1, 2, -3), is(1));
		assertThat(ip.indexOf(0, -1, 2, 3), is(-1));
		assertThat(ip.indexOf(8, -1, 2, -3), is(-1));
		assertThat(ip.indexOf(0, ArrayUtil.ints(-1, 2, -3), 0, 4), is(-1));
	}

	@Test
	public void shouldDetermineIndexOfProvidedInts() {
		assertThat(ip.indexOf(0, provider(-1, 2, -3)), is(1));
		assertThat(ip.indexOf(0, provider(-1, 2, 3)), is(-1));
		assertThat(ip.indexOf(8, provider(-1, 2, -3)), is(-1));
		assertThat(ip.indexOf(0, provider(-1, 2, -3), 0, 4), is(-1));
	}

	@Test
	public void shouldDetermineLastIndexOfBytes() {
		IntProvider ip = provider(0, -1, 2, -1, 0, 2, -1, 0);
		assertThat(ip.lastIndexOf(0, 2, -1), is(5));
		assertThat(ip.lastIndexOf(0, 2, 1), is(-1));
		assertThat(ip.lastIndexOf(7, 0, -1), is(-1));
		assertThat(ip.lastIndexOf(0, ArrayUtil.ints(2, -1, 0), 0, 4), is(-1));
	}

	@Test
	public void shouldDetermineLastIndexOfProviderBytes() {
		IntProvider ip = provider(0, -1, 2, -1, 0, 2, -1, 0);
		assertThat(ip.lastIndexOf(0, provider(2, -1)), is(5));
		assertThat(ip.lastIndexOf(0, provider(2, 1)), is(-1));
		assertThat(ip.lastIndexOf(7, provider(0, -1)), is(-1));
		assertThat(ip.lastIndexOf(0, provider(2, -1, 0), 0, 4), is(-1));
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
		assertThat(ip.reader(1).readInt(), is(-1));
		assertThrown(() -> ip.reader(1, 0).readInt());
	}

	@Test
	public void shouldReadLong() {
		assertThat(ip.reader(6).readLong(true), is(0x6fffffff9L));
		assertThat(ip.reader(6).readLong(false), is(0xfffffff900000006L));
	}

	@Test
	public void shouldReadStrings() {
		assertThat(provider(cp).reader(2, 3).readString(), is("c\ud83c\udc39d"));
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
		assertThat(ip.reader(0).provider(), is(ip));
		assertThat(ip.reader(5, 0).provider().isEmpty(), is(true));
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
