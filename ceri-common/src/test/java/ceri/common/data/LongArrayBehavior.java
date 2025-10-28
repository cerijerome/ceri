package ceri.common.data;

import java.util.function.Consumer;
import java.util.function.IntSupplier;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.data.LongArray.Encodable;
import ceri.common.data.LongArray.Encoder;
import ceri.common.data.LongArray.Immutable;
import ceri.common.data.LongArray.Mutable;
import ceri.common.reflect.Reflect;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class LongArrayBehavior {

	@Test
	public void shouldProvideStringRepresentation() {
		Assert.equal(LongArray.Immutable.wrap().toString(), "[]");
		Assert.equal(LongArray.Immutable.wrap(1, 2, 3, 4, 5, 6, 7, 8, 9).toString(),
			"[1,2,3,4,5,6,7,...](9)");
		Assert.equal(LongArray.Immutable.wrap(1, 2, 3, 4, 5, 6, 7, 8).toString(),
			"[1,2,3,4,5,6,7,8]");
	}

	/* Immutable tests */

	@Test
	public void shouldNotBreachImmutableEqualsContract() {
		var t = Immutable.wrap(1, 2, 3);
		var eq0 = Immutable.wrap(1, 2, 3);
		var eq1 = Immutable.copyOf(ArrayUtil.longs.of(1, 2, 3));
		var eq2 = Immutable.copyOf(ArrayUtil.longs.of(0, 1, 2, 3, 4), 1, 3);
		var ne0 = Immutable.wrap(1, 2, 4);
		var ne1 = Immutable.wrap(1, 2, 3, 0);
		var ne2 = Immutable.wrap();
		Testing.exerciseEquals(t, eq0, eq1, eq2);
		Assert.notEqualAll(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldCreateImmutableCopy() {
		long[] longs = ArrayUtil.longs.of(1, 2, 3);
		var im = Immutable.copyOf(longs);
		longs[1] = 0;
		Assert.equal(im.getLong(1), 2L);
	}

	@Test
	public void shouldCreateImmutableLongWrapper() {
		Assert.yes(Immutable.wrap(ArrayUtil.longs.of(1, 2, 3)).isEqualTo(0, 1, 2, 3));
		Assert.yes(Immutable.wrap(ArrayUtil.longs.of(1, 2, 3), 3).isEmpty());
		long[] longs = ArrayUtil.longs.of(1, 2, 3);
		var im = Immutable.wrap(longs);
		longs[1] = 0;
		Assert.equal(im.getLong(1), 0L);
	}

	@Test
	public void shouldCreateImmutableSlice() {
		Assert.yes(Immutable.wrap(1, 2, 3, 4, 5).slice(5).isEmpty());
		Assert.yes(Immutable.wrap(1, 2, 3, 4, 5).slice(0, 2).isEqualTo(0, 1, 2));
		Assert.yes(Immutable.wrap(1, 2, 3, 4, 5).slice(5, -2).isEqualTo(0, 4, 5));
		var im = Immutable.wrap(1, 2, 3);
		Assert.yes(im.slice(0) == im);
	}

	/* Mutable tests */

	@Test
	public void shouldNotBreachMutableEqualsContract() {
		var t = Mutable.of(3);
		var eq0 = Mutable.of(3);
		var eq1 = Mutable.wrap(new long[3]);
		var eq2 = Mutable.wrap(new long[5], 1, 3);
		var eq3 = Mutable.wrap(0, 0, 0);
		var ne0 = Mutable.of(4);
		var ne1 = Mutable.wrap(0, 0, 1);
		var ne2 = Mutable.wrap();
		Testing.exerciseEquals(t, eq0, eq1, eq2, eq3);
		Assert.notEqualAll(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldProvideAnImmutableView() {
		var m = Mutable.wrap(1, 2, 3);
		Assert.array(m.asImmutable().copy(0), 1, 2, 3);
		Assert.isNull(Reflect.castOrNull(LongReceiver.class, m.asImmutable()));
		m.setLong(0, -1);
		Assert.array(m.asImmutable().copy(0), -1, 2, 3);
	}

	@Test
	public void shouldCreateMutableSlice() {
		var m = Mutable.wrap(1, 2, 3, 4, 5);
		Assert.yes(m.slice(5).isEmpty());
		Assert.yes(m.slice(0, 2).isEqualTo(0, 1, 2));
		Assert.yes(m.slice(5, -2).isEqualTo(0, 4, 5));
		Assert.yes(m.slice(0) == m);
	}

	@Test
	public void shouldSetLong() {
		long[] longs = ArrayUtil.longs.of(1, 2, -1L);
		var m = Mutable.wrap(longs);
		Assert.equal(m.setLong(1, 0xffffeeeeddddccccL), 2);
		Assert.array(longs, 1, 0xffffeeeeddddccccL, -1L);
	}

	@Test
	public void shouldFillLongs() {
		var m = Mutable.wrap(1, 2, 3, 4, 5);
		Assert.equal(m.fill(1, 2, 0xff), 3);
		Assert.yes(m.isEqualTo(0, 1, 0xff, 0xff, 4, 5));
		Assert.thrown(() -> m.fill(3, 3, 0));
	}

	@Test
	public void shouldCopyFromArray() {
		var m = Mutable.wrap(1, 2, 3, 4, 5);
		Assert.equal(m.setLongs(3, -4, -5), 5);
		Assert.yes(m.isEqualTo(0, 1, 2, 3, -4, -5));
		Assert.thrown(() -> m.copyFrom(3, ArrayUtil.longs.of(1, 2, 3), 0));
		Assert.thrown(() -> m.copyFrom(0, ArrayUtil.longs.of(1, 2, 3), 2, 2));
	}

	@Test
	public void shouldCopyFromLongProvider() {
		var m = Mutable.wrap(1, 2, 3, 4, 5);
		Assert.equal(m.copyFrom(3, Immutable.wrap(-4, -5)), 5);
		Assert.yes(m.isEqualTo(0, 1, 2, 3, -4, -5));
		Assert.thrown(() -> m.copyFrom(3, Immutable.wrap(1, 2, 3), 0));
		Assert.thrown(() -> m.copyFrom(0, Immutable.wrap(1, 2, 3), 2, 2));
	}

	/* LongArray base tests */

	@Test
	public void shouldCopyLongs() {
		Assert.array(LongArray.Immutable.wrap().copy(0));
		Assert.array(LongArray.Immutable.wrap(1, 2, 3).copy(0), 1, 2, 3);
	}

	@Test
	public void shouldCopyToLongReceiver() {
		var m = Mutable.of(3);
		Assert.equal(Immutable.wrap(1, 2, 3).copyTo(1, m, 1), 3);
		Assert.yes(m.isEqualTo(0, 0, 2, 3));
		Assert.thrown(() -> Immutable.wrap(0, 1, 2).copyTo(0, m, 4));
	}

	@Test
	public void shouldDetermineIfEqualToLongs() {
		Assert.yes(Immutable.wrap(1, 2, 3, 4, 5).isEqualTo(1, ArrayUtil.longs.of(2, 3, 4)));
		Assert.no(Immutable.wrap(1, 2, 3).isEqualTo(2, ArrayUtil.longs.of(1, 2)));
		Assert.no(Immutable.wrap(1, 2, 3).isEqualTo(0, ArrayUtil.longs.of(1, 2), 0, 3));
	}

	@Test
	public void shouldDetermineIfEqualToProviderLongs() {
		Assert.yes(Immutable.wrap(1, 2, 3, 4, 5).isEqualTo(1, Immutable.wrap(2, 3, 4)));
		Assert.no(Immutable.wrap(1, 2, 3).isEqualTo(2, Immutable.wrap(1, 2)));
		Assert.no(Immutable.wrap(1, 2, 3).isEqualTo(0, Immutable.wrap(1, 2), 0, 3));
	}

	/* Encoder tests */

	@Test
	public void shouldEncodeToMinimumSizedArray() {
		Assert.array(Encoder.of().longs());
		Assert.array(Encoder.of(5).longs(), 0, 0, 0, 0, 0);
	}

	@Test
	public void shouldEncodeToMutableArray() {
		var m = Encoder.of().fill(3, -1).mutable();
		m.setLong(1, 0);
		Assert.array(m, -1L, 0, -1L);
	}

	@Test
	public void shouldEncodeFixedSize() {
		var en = Encoder.fixed(3);
		en.fill(3, 1);
		Assert.thrown(() -> en.writeLong(1));
		Assert.array(en.longs(), 1, 1, 1);
	}

	@Test
	public void shouldEncodeToArray() {
		long[] array = new long[5];
		Assert.thrown(() -> LongArray.Encoder.of(array, 6));
		LongArray.Encoder.of(array).writeLongs(1, 2, 3);
		Assert.array(array, 1L, 2L, 3L, 0L, 0L);
	}

	@Test
	public void shouldEncodeAndReadLong() {
		Assert.equal(Encoder.of().writeLong(-1).skip(-1).readLong(), -1L);
	}

	@Test
	public void shouldEncodeAndReadLongs() {
		Assert.array(Encoder.of().writeLongs(0, -1).skip(-2).readLongs(2), 0L, -1L);
	}

	@Test
	public void shouldEncodeAndReadFromIntoLongArray() {
		long[] bin = ArrayUtil.longs.of(1, 2, 3, 4, 5);
		long[] bout = new long[3];
		Encoder.of().writeFrom(bin, 1, 3).skip(-3).readInto(bout, 1, 2);
		Assert.array(bout, 0, 2, 3);
	}

	@Test
	public void shouldEncodeToAndReadLongoLongAccessor() {
		var m = Mutable.wrap(1, 2, 3, 0, 0);
		Encoder.of().writeFrom(m, 0, 3).skip(-3).readInto(m, 2);
		Assert.array(m.copy(0), 1, 2, 1, 2, 3);
	}

	@Test
	public void shouldEncodeFillLongs() {
		Assert.array(Encoder.of().fill(3, -1).skip(2).longs(), -1, -1, -1, 0, 0);
	}

	@Test
	public void shouldEncodeToStream() {
		Assert.stream(Encoder.of().writeLongs(1, 2, 3).offset(0).stream(3), 1, 2, 3);
	}

	@Test
	public void shouldNotGrowEncoderIfReading() {
		var en = Encoder.of();
		en.writeLongs(1, 2, 3).skip(-2);
		Assert.thrown(() -> en.readLongs(3));
		Assert.thrown(() -> en.readLongs(Integer.MAX_VALUE));
	}

	@Test
	public void shouldFailToGrowEncoderAtomically() {
		var en = Encoder.of(0, 3).writeLongs(1);
		Assert.thrown(() -> en.writeLongs(1, 2, 3));
		Assert.thrown(() -> en.fill(3, 0xff));
		Assert.thrown(() -> en.skip(3));
		en.writeLongs(1, 2);
	}

	@Test
	public void shouldNotGrowEncoderPastMax() {
		var en0 = Encoder.of(0, 3).writeLongs(1);
		Assert.thrown(() -> en0.writeLongs(1, 2, 3));
		Assert.thrown(() -> en0.fill(Integer.MAX_VALUE, 0xff));
		var en1 = Encoder.of(3, 5);
		en1.writeLongs(1, 2, 3, 4);
	}

	@Test
	public void shouldNotDoubleEncoderSizePastMax() { // each growth x2 size, but <= max
		var en = Encoder.of(3, 5);
		Assert.array(en.writeLongs(1, 2, 3, 4).longs(), 1, 2, 3, 4);
	}

	@Test
	public void shouldNotGrowEncoderLessThanDefaultSize() { // grow to minimum of 32 (default size)
		var en = Encoder.of(20);
		en.fill(21, 0xff); // grow to 20x2 = 40
		en.fill(60, 1); // grow to 81 (> 40x2)
	}

	/* Encodable tests */

	@Test
	public void shouldReturnEmptyArrayForZeroSizeEncodable() {
		Assert.array(encodable(() -> 0, _ -> Assert.throwRuntime()).encode());
	}

	@Test
	public void shouldEncodeFixedSizeAsEncodable() {
		Assert.array(encodable(() -> 3, enc -> enc.writeLongs(1, 2, 3)).encode(), 1, 2, 3);
	}

	@Test
	public void shouldFailEncodingIfSizeDoesNotMatchBytesAsEncodable() {
		Assert.thrown(() -> encodable(() -> 2, enc -> enc.writeLongs(1, 2, 3)).encode());
		Assert.thrown(() -> encodable(() -> 4, enc -> enc.writeLongs(1, 2, 3)).encode());
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
