package ceri.common.data;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertStream;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.IntArray.Encodable;
import ceri.common.data.IntArray.Encoder;
import ceri.common.data.IntArray.Immutable;
import ceri.common.data.IntArray.Mutable;
import ceri.common.test.TestUtil;
import ceri.common.util.BasicUtil;

public class IntArrayBehavior {

	/* Immutable tests */

	@Test
	public void shouldNotBreachImmutableEqualsContract() {
		Immutable t = Immutable.wrap(1, 2, 3);
		Immutable eq0 = Immutable.wrap(1, 2, 3);
		Immutable eq1 = Immutable.copyOf(ArrayUtil.ints(1, 2, 3));
		Immutable eq2 = Immutable.copyOf(ArrayUtil.ints(0, 1, 2, 3, 4), 1, 3);
		Immutable ne0 = Immutable.wrap(1, 2, 4);
		Immutable ne1 = Immutable.wrap(1, 2, 3, 0);
		Immutable ne2 = Immutable.wrap();
		exerciseEquals(t, eq0, eq1, eq2);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldCreateImmutableCopy() {
		int[] ints = ArrayUtil.ints(1, 2, 3);
		Immutable im = Immutable.copyOf(ints);
		ints[1] = 0;
		assertThat(im.getInt(1), is(2));
	}

	@Test
	public void shouldCreateImmutableIntWrapper() {
		assertThat(Immutable.wrap(ArrayUtil.ints(1, 2, 3)).isEqualTo(0, 1, 2, 3), is(true));
		assertThat(Immutable.wrap(ArrayUtil.ints(1, 2, 3), 3).isEmpty(), is(true));
		int[] ints = ArrayUtil.ints(1, 2, 3);
		Immutable im = Immutable.wrap(ints);
		ints[1] = 0;
		assertThat(im.getInt(1), is(0));
	}

	@Test
	public void shouldCreateImmutableSlice() {
		assertThat(Immutable.wrap(1, 2, 3, 4, 5).slice(5).isEmpty(), is(true));
		assertThat(Immutable.wrap(1, 2, 3, 4, 5).slice(0, 2).isEqualTo(0, 1, 2), is(true));
		assertThat(Immutable.wrap(1, 2, 3, 4, 5).slice(5, -2).isEqualTo(0, 4, 5), is(true));
		Immutable im = Immutable.wrap(1, 2, 3);
		assertTrue(im.slice(0) == im);
	}

	/* Mutable tests */

	@Test
	public void shouldNotBreachMutableEqualsContract() {
		Mutable t = Mutable.of(3);
		Mutable eq0 = Mutable.of(3);
		Mutable eq1 = Mutable.wrap(new int[3]);
		Mutable eq2 = Mutable.wrap(new int[5], 1, 3);
		Mutable eq3 = Mutable.wrap(0, 0, 0);
		Mutable ne0 = Mutable.of(4);
		Mutable ne1 = Mutable.wrap(0, 0, 1);
		Mutable ne2 = Mutable.wrap();
		exerciseEquals(t, eq0, eq1, eq2, eq3);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldProvideAnImmutableView() {
		Mutable m = Mutable.wrap(1, 2, 3);
		assertArray(m.asImmutable().copy(0), 1, 2, 3);
		assertNull(BasicUtil.castOrNull(IntReceiver.class, m.asImmutable()));
		m.setInt(0, -1);
		assertArray(m.asImmutable().copy(0), -1, 2, 3);
	}

	@Test
	public void shouldCreateMutableSlice() {
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		assertThat(m.slice(5).isEmpty(), is(true));
		assertThat(m.slice(0, 2).isEqualTo(0, 1, 2), is(true));
		assertThat(m.slice(5, -2).isEqualTo(0, 4, 5), is(true));
		assertTrue(m.slice(0) == m);
	}

	@Test
	public void shouldSetInt() {
		Mutable m = Mutable.of(3);
		assertThat(m.setInt(1, 0xff), is(2));
		assertThat(m.isEqualTo(0, 0, 0xff, 0), is(true));
	}

	@Test
	public void shouldSetLong() {
		int[] ints = ArrayUtil.ints(1, 2, 3);
		Mutable m = Mutable.wrap(ints);
		assertThat(m.setLong(1, 0xffffeeeeddddccccL, true), is(3));
		assertArray(ints, 1, 0xffffeeee, 0xddddcccc);
		assertThat(m.setLong(1, 0xffffeeeeddddccccL, false), is(3));
		assertArray(ints, 1, 0xddddcccc, 0xffffeeee);
	}

	@Test
	public void shouldFillInts() {
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		assertThat(m.fill(1, 2, 0xff), is(3));
		assertThat(m.isEqualTo(0, 1, 0xff, 0xff, 4, 5), is(true));
		assertThrown(() -> m.fill(3, 3, 0));
	}

	@Test
	public void shouldCopyFromArray() {
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		assertThat(m.setInts(3, -4, -5), is(5));
		assertThat(m.isEqualTo(0, 1, 2, 3, -4, -5), is(true));
		assertThrown(() -> m.copyFrom(3, ArrayUtil.ints(1, 2, 3), 0));
		assertThrown(() -> m.copyFrom(0, ArrayUtil.ints(1, 2, 3), 2, 2));
	}

	@Test
	public void shouldCopyFromIntProvider() {
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		assertThat(m.copyFrom(3, Immutable.wrap(-4, -5)), is(5));
		assertThat(m.isEqualTo(0, 1, 2, 3, -4, -5), is(true));
		assertThrown(() -> m.copyFrom(3, Immutable.wrap(1, 2, 3), 0));
		assertThrown(() -> m.copyFrom(0, Immutable.wrap(1, 2, 3), 2, 2));
	}

	/* IntArray base tests */

	@Test
	public void shouldGetLongFromInts() {
		assertThat(Immutable.wrap(0, 0x7fffffff, 0x80000000, 0).getLong(1, true),
			is(0x7fffffff80000000L));
		assertThat(Immutable.wrap(0, 0x7fffffff, 0x80000000, 0).getLong(1, false),
			is(0x800000007fffffffL));
		assertThrown(() -> Immutable.wrap(0, 0x7fffffff).getLong(1, true));
		assertThrown(() -> Immutable.wrap(0, 0x7fffffff).getLong(1, false));
	}

	@Test
	public void shouldGetString() {
		assertThat(Immutable.wrap("abc\ud83c\udc39de".codePoints().toArray()).getString(0),
			is("abc\ud83c\udc39de"));
		assertThrown(() -> Immutable.wrap("abcde".codePoints().toArray()).getString(3, 3));
	}

	@Test
	public void shouldCopyInts() {
		assertArray(IntArray.Immutable.wrap().copy(0));
		assertArray(IntArray.Immutable.wrap(1, 2, 3).copy(0), 1, 2, 3);
	}

	@Test
	public void shouldCopyToIntReceiver() {
		Mutable m = Mutable.of(3);
		assertThat(Immutable.wrap(1, 2, 3).copyTo(1, m, 1), is(3));
		assertThat(m.isEqualTo(0, 0, 2, 3), is(true));
		assertThrown(() -> Immutable.wrap(0, 1, 2).copyTo(0, m, 4));
	}

	@Test
	public void shouldDetermineIfEqualToInts() {
		assertThat(Immutable.wrap(1, 2, 3, 4, 5).isEqualTo(1, ArrayUtil.ints(2, 3, 4)), is(true));
		assertThat(Immutable.wrap(1, 2, 3).isEqualTo(2, ArrayUtil.ints(1, 2)), is(false));
		assertThat(Immutable.wrap(1, 2, 3).isEqualTo(0, ArrayUtil.ints(1, 2), 0, 3), is(false));
	}

	@Test
	public void shouldDetermineIfEqualToProviderInts() {
		assertThat(Immutable.wrap(1, 2, 3, 4, 5).isEqualTo(1, Immutable.wrap(2, 3, 4)), is(true));
		assertThat(Immutable.wrap(1, 2, 3).isEqualTo(2, Immutable.wrap(1, 2)), is(false));
		assertThat(Immutable.wrap(1, 2, 3).isEqualTo(0, Immutable.wrap(1, 2), 0, 3), is(false));
	}

	/* Encoder tests */

	@Test
	public void shouldEncodeToMinimumSizedArray() {
		assertArray(Encoder.of().ints());
		assertArray(Encoder.of(5).ints(), 0, 0, 0, 0, 0);
	}

	@Test
	public void shouldEncodeFixedSize() {
		Encoder en = Encoder.fixed(3);
		en.fill(3, 1);
		assertThrown(() -> en.writeInt(1));
		assertArray(en.ints(), 1, 1, 1);
	}

	@Test
	public void shouldEncodeAsIntArrayWrappers() {
		assertArray(Encoder.of().writeString("abc").ints(), 'a', 'b', 'c');
		assertArray(Encoder.of().writeString("abc").mutable().copy(0), 'a', 'b', 'c');
		assertArray(Encoder.of().writeString("abc").immutable().copy(0), 'a', 'b', 'c');
	}

	@Test
	public void shouldEncodeAndReadInt() {
		assertThat(Encoder.of().writeInt(-1).skip(-1).readInt(), is(-1));
	}

	@Test
	public void shouldEncodeAndReadLongs() {
		assertThat(Encoder.of().writeLong(Long.MIN_VALUE, true).skip(-2).readLong(true),
			is(Long.MIN_VALUE));
		assertThat(Encoder.of().writeLong(Long.MIN_VALUE, false).skip(-2).readLong(false),
			is(Long.MIN_VALUE));
	}

	@Test
	public void shouldEncodeAndReadString() {
		String s = "abc\ud83c\udc39de";
		int[] ints = s.codePoints().toArray();
		int n = ints.length;
		assertThat(Encoder.of().writeString(s).skip(-n).readString(n), is(s));
		assertArray(Encoder.of().writeString(s).ints(), 'a', 'b', 'c', 0x1f039, 'd', 'e');
	}

	@Test
	public void shouldEncodeAndReadInts() {
		assertArray(Encoder.of().writeInts(1, 2, 3).skip(-3).readInts(3), 1, 2, 3);
	}

	@Test
	public void shouldEncodeAndReadFromIntoIntArray() {
		int[] bin = ArrayUtil.ints(1, 2, 3, 4, 5);
		int[] bout = new int[3];
		Encoder.of().writeFrom(bin, 1, 3).skip(-3).readInto(bout, 1, 2);
		assertArray(bout, 0, 2, 3);
	}

	@Test
	public void shouldEncodeToAndReadIntoIntAccessor() {
		Mutable m = Mutable.wrap(1, 2, 3, 0, 0);
		Encoder.of().writeFrom(m, 0, 3).skip(-3).readInto(m, 2);
		assertArray(m.copy(0), 1, 2, 1, 2, 3);
	}

	@Test
	public void shouldEncodeFillInts() {
		assertArray(Encoder.of().fill(3, -1).skip(2).ints(), -1, -1, -1, 0, 0);
	}

	@Test
	public void shouldEncodeToStream() {
		assertStream(Encoder.of().writeInts(1, 2, 3).offset(0).stream(3), 1, 2, 3);
	}

	@Test
	public void shouldEncodeToUstream() {
		assertStream(Encoder.of().writeInts(-1, -2, -3).offset(0).ustream(3), 0xffffffffL,
			0xfffffffeL, 0xfffffffdL);
	}

	@Test
	public void shouldNotGrowEncoderIfReading() {
		Encoder en = Encoder.of();
		en.writeInts(1, 2, 3).skip(-2);
		assertThrown(() -> en.readInts(3));
		assertThrown(() -> en.readInts(Integer.MAX_VALUE));
	}

	@Test
	public void shouldFailToGrowEncoderAtomically() {
		Encoder en = Encoder.of(0, 3).writeInts(1);
		assertThrown(() -> en.writeInts(1, 2, 3));
		assertThrown(() -> en.fill(3, 0xff));
		assertThrown(() -> en.skip(3));
		en.writeInts(1, 2);
	}

	@Test
	public void shouldNotGrowEncoderPastMax() {
		Encoder en0 = Encoder.of(0, 3).writeInts(1);
		assertThrown(() -> en0.writeInts(1, 2, 3));
		assertThrown(() -> en0.fill(Integer.MAX_VALUE, 0xff));
		Encoder en1 = Encoder.of(3, 5);
		en1.writeInts(1, 2, 3, 4);
	}

	@Test
	public void shouldNotDoubleEncoderSizePastMax() { // each growth x2 size, but <= max
		Encoder en = Encoder.of(3, 5);
		assertArray(en.writeInts(1, 2, 3, 4).ints(), 1, 2, 3, 4);
	}

	@Test
	public void shouldNotGrowEncoderLessThanDefaultSize() { // grow to minimum of 32 (default size)
		Encoder en = Encoder.of(20);
		en.fill(21, 0xff); // grow to 20x2 = 40
		en.fill(60, 1); // grow to 81 (> 40x2)
	}

	/* Encodable tests */

	@Test
	public void shouldReturnEmptyArrayForZeroSizeEncodable() {
		assertArray(encodable(() -> 0, enc -> TestUtil.throwIt()).encode());
	}

	@Test
	public void shouldEncodeFixedSizeAsEncodable() {
		assertArray(encodable(() -> 3, enc -> enc.writeInts(1, 2, 3)).encode(), 1, 2, 3);
	}

	@Test
	public void shouldFailEncodingIfSizeDoesNotMatchBytesAsEncodable() {
		assertThrown(() -> encodable(() -> 2, enc -> enc.writeInts(1, 2, 3)).encode());
		assertThrown(() -> encodable(() -> 4, enc -> enc.writeInts(1, 2, 3)).encode());
	}

	private static Encodable encodable(IntSupplier sizeFn, Consumer<Encoder> encodeFn) {
		return new Encodable() {
			@Override
			public int size() {
				return sizeFn.getAsInt();
			}

			@Override
			public void encode(Encoder encoder) {
				encodeFn.accept(encoder);
			}
		};
	}

}
