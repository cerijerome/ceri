package ceri.common.data;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.throwRuntime;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.data.IntArray.Encodable;
import ceri.common.data.IntArray.Encoder;
import ceri.common.data.IntArray.Immutable;
import ceri.common.data.IntArray.Mutable;
import ceri.common.reflect.Reflect;
import ceri.common.test.TestUtil;

public class IntArrayBehavior {

	@Test
	public void shouldProvideStringRepresentation() {
		assertEquals(IntArray.Immutable.wrap().toString(), "[]");
		assertEquals(IntArray.Immutable.wrap(1, 2, 3, 4, 5, 6, 7, 8, 9).toString(),
			"[1,2,3,4,5,6,7,...](9)");
		assertEquals(IntArray.Immutable.wrap(1, 2, 3, 4, 5, 6, 7, 8).toString(),
			"[1,2,3,4,5,6,7,8]");
	}

	/* Immutable tests */

	@Test
	public void shouldNotBreachImmutableEqualsContract() {
		Immutable t = Immutable.wrap(1, 2, 3);
		Immutable eq0 = Immutable.wrap(1, 2, 3);
		Immutable eq1 = Immutable.copyOf(ArrayUtil.ints.of(1, 2, 3));
		Immutable eq2 = Immutable.copyOf(ArrayUtil.ints.of(0, 1, 2, 3, 4), 1, 3);
		Immutable ne0 = Immutable.wrap(1, 2, 4);
		Immutable ne1 = Immutable.wrap(1, 2, 3, 0);
		Immutable ne2 = Immutable.wrap();
		TestUtil.exerciseEquals(t, eq0, eq1, eq2);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldCreateImmutableCopy() {
		int[] ints = ArrayUtil.ints.of(1, 2, 3);
		Immutable im = Immutable.copyOf(ints);
		ints[1] = 0;
		assertEquals(im.getInt(1), 2);
	}

	@Test
	public void shouldCreateImmutableIntWrapper() {
		assertTrue(Immutable.wrap(ArrayUtil.ints.of(1, 2, 3)).isEqualTo(0, 1, 2, 3));
		assertTrue(Immutable.wrap(ArrayUtil.ints.of(1, 2, 3), 3).isEmpty());
		int[] ints = ArrayUtil.ints.of(1, 2, 3);
		Immutable im = Immutable.wrap(ints);
		ints[1] = 0;
		assertEquals(im.getInt(1), 0);
	}

	@Test
	public void shouldCreateImmutableSlice() {
		assertTrue(Immutable.wrap(1, 2, 3, 4, 5).slice(5).isEmpty());
		assertTrue(Immutable.wrap(1, 2, 3, 4, 5).slice(0, 2).isEqualTo(0, 1, 2));
		assertTrue(Immutable.wrap(1, 2, 3, 4, 5).slice(5, -2).isEqualTo(0, 4, 5));
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
		TestUtil.exerciseEquals(t, eq0, eq1, eq2, eq3);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldProvideAnImmutableView() {
		Mutable m = Mutable.wrap(1, 2, 3);
		assertArray(m.asImmutable().copy(0), 1, 2, 3);
		assertNull(Reflect.castOrNull(IntReceiver.class, m.asImmutable()));
		m.setInt(0, -1);
		assertArray(m.asImmutable().copy(0), -1, 2, 3);
	}

	@Test
	public void shouldCreateMutableSlice() {
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		assertTrue(m.slice(5).isEmpty());
		assertTrue(m.slice(0, 2).isEqualTo(0, 1, 2));
		assertTrue(m.slice(5, -2).isEqualTo(0, 4, 5));
		assertTrue(m.slice(0) == m);
	}

	@Test
	public void shouldSetInt() {
		Mutable m = Mutable.of(3);
		assertEquals(m.setInt(1, 0xff), 2);
		assertTrue(m.isEqualTo(0, 0, 0xff, 0));
	}

	@Test
	public void shouldSetLong() {
		int[] ints = ArrayUtil.ints.of(1, 2, 3);
		Mutable m = Mutable.wrap(ints);
		assertEquals(m.setLong(1, 0xffffeeeeddddccccL, true), 3);
		assertArray(ints, 1, 0xffffeeee, 0xddddcccc);
		assertEquals(m.setLong(1, 0xffffeeeeddddccccL, false), 3);
		assertArray(ints, 1, 0xddddcccc, 0xffffeeee);
	}

	@Test
	public void shouldFillInts() {
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		assertEquals(m.fill(1, 2, 0xff), 3);
		assertTrue(m.isEqualTo(0, 1, 0xff, 0xff, 4, 5));
		assertThrown(() -> m.fill(3, 3, 0));
	}

	@Test
	public void shouldCopyFromArray() {
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		assertEquals(m.setInts(3, -4, -5), 5);
		assertTrue(m.isEqualTo(0, 1, 2, 3, -4, -5));
		assertThrown(() -> m.copyFrom(3, ArrayUtil.ints.of(1, 2, 3), 0));
		assertThrown(() -> m.copyFrom(0, ArrayUtil.ints.of(1, 2, 3), 2, 2));
	}

	@Test
	public void shouldCopyFromIntProvider() {
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		assertEquals(m.copyFrom(3, Immutable.wrap(-4, -5)), 5);
		assertTrue(m.isEqualTo(0, 1, 2, 3, -4, -5));
		assertThrown(() -> m.copyFrom(3, Immutable.wrap(1, 2, 3), 0));
		assertThrown(() -> m.copyFrom(0, Immutable.wrap(1, 2, 3), 2, 2));
	}

	/* IntArray base tests */

	@Test
	public void shouldGetLongFromInts() {
		assertEquals(Immutable.wrap(0, 0x7fffffff, 0x80000000, 0).getLong(1, true),
			0x7fffffff80000000L);
		assertEquals(Immutable.wrap(0, 0x7fffffff, 0x80000000, 0).getLong(1, false),
			0x800000007fffffffL);
		assertThrown(() -> Immutable.wrap(0, 0x7fffffff).getLong(1, true));
		assertThrown(() -> Immutable.wrap(0, 0x7fffffff).getLong(1, false));
	}

	@Test
	public void shouldGetString() {
		assertEquals(Immutable.wrap("abc\ud83c\udc39de".codePoints().toArray()).getString(0),
			"abc\ud83c\udc39de");
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
		assertEquals(Immutable.wrap(1, 2, 3).copyTo(1, m, 1), 3);
		assertTrue(m.isEqualTo(0, 0, 2, 3));
		assertThrown(() -> Immutable.wrap(0, 1, 2).copyTo(0, m, 4));
	}

	@Test
	public void shouldDetermineIfEqualToInts() {
		assertTrue(Immutable.wrap(1, 2, 3, 4, 5).isEqualTo(1, ArrayUtil.ints.of(2, 3, 4)));
		assertFalse(Immutable.wrap(1, 2, 3).isEqualTo(2, ArrayUtil.ints.of(1, 2)));
		assertFalse(Immutable.wrap(1, 2, 3).isEqualTo(0, ArrayUtil.ints.of(1, 2), 0, 3));
	}

	@Test
	public void shouldDetermineIfEqualToProviderInts() {
		assertTrue(Immutable.wrap(1, 2, 3, 4, 5).isEqualTo(1, Immutable.wrap(2, 3, 4)));
		assertFalse(Immutable.wrap(1, 2, 3).isEqualTo(2, Immutable.wrap(1, 2)));
		assertFalse(Immutable.wrap(1, 2, 3).isEqualTo(0, Immutable.wrap(1, 2), 0, 3));
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
	public void shouldEncodeToArray() {
		int[] array = new int[5];
		assertThrown(() -> IntArray.Encoder.of(array, 6));
		IntArray.Encoder.of(array).writeInts(1, 2, 3);
		assertArray(array, 1, 2, 3, 0, 0);
	}

	@Test
	public void shouldEncodeAsIntArrayWrappers() {
		assertArray(Encoder.of().writeString("abc").ints(), 'a', 'b', 'c');
		assertArray(Encoder.of().writeString("abc").mutable().copy(0), 'a', 'b', 'c');
		assertArray(Encoder.of().writeString("abc").immutable().copy(0), 'a', 'b', 'c');
	}

	@Test
	public void shouldEncodeAndReadInt() {
		assertEquals(Encoder.of().writeInt(-1).skip(-1).readInt(), -1);
	}

	@Test
	public void shouldEncodeAndReadLongs() {
		assertEquals(Encoder.of().writeLong(Long.MIN_VALUE, true).skip(-2).readLong(true),
			Long.MIN_VALUE);
		assertEquals(Encoder.of().writeLong(Long.MIN_VALUE, false).skip(-2).readLong(false),
			Long.MIN_VALUE);
	}

	@Test
	public void shouldEncodeAndReadString() {
		String s = "abc\ud83c\udc39de";
		int[] ints = s.codePoints().toArray();
		int n = ints.length;
		assertEquals(Encoder.of().writeString(s).skip(-n).readString(n), s);
		assertArray(Encoder.of().writeString(s).ints(), 'a', 'b', 'c', 0x1f039, 'd', 'e');
	}

	@Test
	public void shouldEncodeAndReadInts() {
		assertArray(Encoder.of().writeInts(1, 2, 3).skip(-3).readInts(3), 1, 2, 3);
	}

	@Test
	public void shouldEncodeAndReadFromIntoIntArray() {
		int[] bin = ArrayUtil.ints.of(1, 2, 3, 4, 5);
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
		assertArray(encodable(() -> 0, _ -> throwRuntime()).encode());
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
