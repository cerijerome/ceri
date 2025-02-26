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
import static ceri.common.test.TestUtil.exerciseEquals;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.LongArray.Encodable;
import ceri.common.data.LongArray.Encoder;
import ceri.common.data.LongArray.Immutable;
import ceri.common.data.LongArray.Mutable;
import ceri.common.reflect.ReflectUtil;

public class LongArrayBehavior {

	@Test
	public void shouldProvideStringRepresentation() {
		assertEquals(LongArray.Immutable.wrap().toString(), "[]");
		assertEquals(LongArray.Immutable.wrap(1, 2, 3, 4, 5, 6, 7, 8, 9).toString(),
			"[1,2,3,4,5,6,7,...](9)");
		assertEquals(LongArray.Immutable.wrap(1, 2, 3, 4, 5, 6, 7, 8).toString(),
			"[1,2,3,4,5,6,7,8]");
	}

	/* Immutable tests */

	@Test
	public void shouldNotBreachImmutableEqualsContract() {
		Immutable t = Immutable.wrap(1, 2, 3);
		Immutable eq0 = Immutable.wrap(1, 2, 3);
		Immutable eq1 = Immutable.copyOf(ArrayUtil.longs(1, 2, 3));
		Immutable eq2 = Immutable.copyOf(ArrayUtil.longs(0, 1, 2, 3, 4), 1, 3);
		Immutable ne0 = Immutable.wrap(1, 2, 4);
		Immutable ne1 = Immutable.wrap(1, 2, 3, 0);
		Immutable ne2 = Immutable.wrap();
		exerciseEquals(t, eq0, eq1, eq2);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldCreateImmutableCopy() {
		long[] longs = ArrayUtil.longs(1, 2, 3);
		Immutable im = Immutable.copyOf(longs);
		longs[1] = 0;
		assertEquals(im.getLong(1), 2L);
	}

	@Test
	public void shouldCreateImmutableLongWrapper() {
		assertTrue(Immutable.wrap(ArrayUtil.longs(1, 2, 3)).isEqualTo(0, 1, 2, 3));
		assertTrue(Immutable.wrap(ArrayUtil.longs(1, 2, 3), 3).isEmpty());
		long[] longs = ArrayUtil.longs(1, 2, 3);
		Immutable im = Immutable.wrap(longs);
		longs[1] = 0;
		assertEquals(im.getLong(1), 0L);
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
		Mutable eq1 = Mutable.wrap(new long[3]);
		Mutable eq2 = Mutable.wrap(new long[5], 1, 3);
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
		assertNull(ReflectUtil.castOrNull(LongReceiver.class, m.asImmutable()));
		m.setLong(0, -1);
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
	public void shouldSetLong() {
		long[] longs = ArrayUtil.longs(1, 2, -1L);
		Mutable m = Mutable.wrap(longs);
		assertEquals(m.setLong(1, 0xffffeeeeddddccccL), 2);
		assertArray(longs, 1, 0xffffeeeeddddccccL, -1L);
	}

	@Test
	public void shouldFillLongs() {
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		assertEquals(m.fill(1, 2, 0xff), 3);
		assertTrue(m.isEqualTo(0, 1, 0xff, 0xff, 4, 5));
		assertThrown(() -> m.fill(3, 3, 0));
	}

	@Test
	public void shouldCopyFromArray() {
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		assertEquals(m.setLongs(3, -4, -5), 5);
		assertTrue(m.isEqualTo(0, 1, 2, 3, -4, -5));
		assertThrown(() -> m.copyFrom(3, ArrayUtil.longs(1, 2, 3), 0));
		assertThrown(() -> m.copyFrom(0, ArrayUtil.longs(1, 2, 3), 2, 2));
	}

	@Test
	public void shouldCopyFromLongProvider() {
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		assertEquals(m.copyFrom(3, Immutable.wrap(-4, -5)), 5);
		assertTrue(m.isEqualTo(0, 1, 2, 3, -4, -5));
		assertThrown(() -> m.copyFrom(3, Immutable.wrap(1, 2, 3), 0));
		assertThrown(() -> m.copyFrom(0, Immutable.wrap(1, 2, 3), 2, 2));
	}

	/* LongArray base tests */

	@Test
	public void shouldCopyLongs() {
		assertArray(LongArray.Immutable.wrap().copy(0));
		assertArray(LongArray.Immutable.wrap(1, 2, 3).copy(0), 1, 2, 3);
	}

	@Test
	public void shouldCopyToLongReceiver() {
		Mutable m = Mutable.of(3);
		assertEquals(Immutable.wrap(1, 2, 3).copyTo(1, m, 1), 3);
		assertTrue(m.isEqualTo(0, 0, 2, 3));
		assertThrown(() -> Immutable.wrap(0, 1, 2).copyTo(0, m, 4));
	}

	@Test
	public void shouldDetermineIfEqualToLongs() {
		assertTrue(Immutable.wrap(1, 2, 3, 4, 5).isEqualTo(1, ArrayUtil.longs(2, 3, 4)));
		assertFalse(Immutable.wrap(1, 2, 3).isEqualTo(2, ArrayUtil.longs(1, 2)));
		assertFalse(Immutable.wrap(1, 2, 3).isEqualTo(0, ArrayUtil.longs(1, 2), 0, 3));
	}

	@Test
	public void shouldDetermineIfEqualToProviderLongs() {
		assertTrue(Immutable.wrap(1, 2, 3, 4, 5).isEqualTo(1, Immutable.wrap(2, 3, 4)));
		assertFalse(Immutable.wrap(1, 2, 3).isEqualTo(2, Immutable.wrap(1, 2)));
		assertFalse(Immutable.wrap(1, 2, 3).isEqualTo(0, Immutable.wrap(1, 2), 0, 3));
	}

	/* Encoder tests */

	@Test
	public void shouldEncodeToMinimumSizedArray() {
		assertArray(Encoder.of().longs());
		assertArray(Encoder.of(5).longs(), 0, 0, 0, 0, 0);
	}

	@Test
	public void shouldEncodeToMutableArray() {
		var m = Encoder.of().fill(3, -1).mutable();
		m.setLong(1, 0);
		assertArray(m, -1L, 0, -1L);
	}

	@Test
	public void shouldEncodeFixedSize() {
		Encoder en = Encoder.fixed(3);
		en.fill(3, 1);
		assertThrown(() -> en.writeLong(1));
		assertArray(en.longs(), 1, 1, 1);
	}

	@Test
	public void shouldEncodeToArray() {
		long[] array = new long[5];
		assertThrown(() -> LongArray.Encoder.of(array, 6));
		LongArray.Encoder.of(array).writeLongs(1, 2, 3);
		assertArray(array, 1L, 2L, 3L, 0L, 0L);
	}

	@Test
	public void shouldEncodeAndReadLong() {
		assertEquals(Encoder.of().writeLong(-1).skip(-1).readLong(), -1L);
	}

	@Test
	public void shouldEncodeAndReadLongs() {
		assertArray(Encoder.of().writeLongs(0, -1).skip(-2).readLongs(2), 0L, -1L);
	}

	@Test
	public void shouldEncodeAndReadFromIntoLongArray() {
		long[] bin = ArrayUtil.longs(1, 2, 3, 4, 5);
		long[] bout = new long[3];
		Encoder.of().writeFrom(bin, 1, 3).skip(-3).readInto(bout, 1, 2);
		assertArray(bout, 0, 2, 3);
	}

	@Test
	public void shouldEncodeToAndReadLongoLongAccessor() {
		Mutable m = Mutable.wrap(1, 2, 3, 0, 0);
		Encoder.of().writeFrom(m, 0, 3).skip(-3).readInto(m, 2);
		assertArray(m.copy(0), 1, 2, 1, 2, 3);
	}

	@Test
	public void shouldEncodeFillLongs() {
		assertArray(Encoder.of().fill(3, -1).skip(2).longs(), -1, -1, -1, 0, 0);
	}

	@Test
	public void shouldEncodeToStream() {
		assertStream(Encoder.of().writeLongs(1, 2, 3).offset(0).stream(3), 1, 2, 3);
	}

	@Test
	public void shouldNotGrowEncoderIfReading() {
		Encoder en = Encoder.of();
		en.writeLongs(1, 2, 3).skip(-2);
		assertThrown(() -> en.readLongs(3));
		assertThrown(() -> en.readLongs(Integer.MAX_VALUE));
	}

	@Test
	public void shouldFailToGrowEncoderAtomically() {
		Encoder en = Encoder.of(0, 3).writeLongs(1);
		assertThrown(() -> en.writeLongs(1, 2, 3));
		assertThrown(() -> en.fill(3, 0xff));
		assertThrown(() -> en.skip(3));
		en.writeLongs(1, 2);
	}

	@Test
	public void shouldNotGrowEncoderPastMax() {
		Encoder en0 = Encoder.of(0, 3).writeLongs(1);
		assertThrown(() -> en0.writeLongs(1, 2, 3));
		assertThrown(() -> en0.fill(Integer.MAX_VALUE, 0xff));
		Encoder en1 = Encoder.of(3, 5);
		en1.writeLongs(1, 2, 3, 4);
	}

	@Test
	public void shouldNotDoubleEncoderSizePastMax() { // each growth x2 size, but <= max
		Encoder en = Encoder.of(3, 5);
		assertArray(en.writeLongs(1, 2, 3, 4).longs(), 1, 2, 3, 4);
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
		assertArray(encodable(() -> 3, enc -> enc.writeLongs(1, 2, 3)).encode(), 1, 2, 3);
	}

	@Test
	public void shouldFailEncodingIfSizeDoesNotMatchBytesAsEncodable() {
		assertThrown(() -> encodable(() -> 2, enc -> enc.writeLongs(1, 2, 3)).encode());
		assertThrown(() -> encodable(() -> 4, enc -> enc.writeLongs(1, 2, 3)).encode());
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
