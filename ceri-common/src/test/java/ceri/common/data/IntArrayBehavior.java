package ceri.common.data;

import java.util.function.Consumer;
import java.util.function.IntSupplier;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.data.IntArray.Encodable;
import ceri.common.data.IntArray.Encoder;
import ceri.common.data.IntArray.Immutable;
import ceri.common.data.IntArray.Mutable;
import ceri.common.reflect.Reflect;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class IntArrayBehavior {
	private static final long LMIN = Long.MIN_VALUE;

	@Test
	public void shouldProvideStringRepresentation() {
		Assert.equal(IntArray.Immutable.wrap().toString(), "[]");
		Assert.equal(IntArray.Immutable.wrap(1, 2, 3, 4, 5, 6, 7, 8, 9).toString(),
			"[1,2,3,4,5,6,7,...](9)");
		Assert.equal(IntArray.Immutable.wrap(1, 2, 3, 4, 5, 6, 7, 8).toString(),
			"[1,2,3,4,5,6,7,8]");
	}

	// Immutable

	@Test
	public void shouldNotBreachImmutableEqualsContract() {
		var t = Immutable.wrap(1, 2, 3);
		var eq0 = Immutable.wrap(1, 2, 3);
		var eq1 = Immutable.copyOf(ArrayUtil.ints.of(1, 2, 3));
		var eq2 = Immutable.copyOf(ArrayUtil.ints.of(0, 1, 2, 3, 4), 1, 3);
		var ne0 = Immutable.wrap(1, 2, 4);
		var ne1 = Immutable.wrap(1, 2, 3, 0);
		var ne2 = Immutable.wrap();
		Testing.exerciseEquals(t, eq0, eq1, eq2);
		Assert.notEqualAll(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldCreateImmutableCopy() {
		int[] ints = ArrayUtil.ints.of(1, 2, 3);
		var im = Immutable.copyOf(ints);
		ints[1] = 0;
		Assert.equal(im.getInt(1), 2);
	}

	@Test
	public void shouldCreateImmutableIntWrapper() {
		Assert.yes(Immutable.wrap(ArrayUtil.ints.of(1, 2, 3)).isEqualTo(0, 1, 2, 3));
		Assert.yes(Immutable.wrap(ArrayUtil.ints.of(1, 2, 3), 3).isEmpty());
		int[] ints = ArrayUtil.ints.of(1, 2, 3);
		var im = Immutable.wrap(ints);
		ints[1] = 0;
		Assert.equal(im.getInt(1), 0);
	}

	@Test
	public void shouldCreateImmutableSlice() {
		Assert.yes(Immutable.wrap(1, 2, 3, 4, 5).slice(5).isEmpty());
		Assert.yes(Immutable.wrap(1, 2, 3, 4, 5).slice(0, 2).isEqualTo(0, 1, 2));
		Assert.yes(Immutable.wrap(1, 2, 3, 4, 5).slice(5, -2).isEqualTo(0, 4, 5));
		var im = Immutable.wrap(1, 2, 3);
		Assert.yes(im.slice(0) == im);
	}

	// Mutable

	@Test
	public void shouldNotBreachMutableEqualsContract() {
		var t = Mutable.of(3);
		var eq0 = Mutable.of(3);
		var eq1 = Mutable.wrap(new int[3]);
		var eq2 = Mutable.wrap(new int[5], 1, 3);
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
		Assert.isNull(Reflect.castOrNull(IntReceiver.class, m.asImmutable()));
		m.setInt(0, -1);
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
	public void shouldSetInt() {
		var m = Mutable.of(3);
		Assert.equal(m.setInt(1, 0xff), 2);
		Assert.yes(m.isEqualTo(0, 0, 0xff, 0));
	}

	@Test
	public void shouldSetLong() {
		int[] ints = ArrayUtil.ints.of(1, 2, 3);
		var m = Mutable.wrap(ints);
		Assert.equal(m.setLong(1, 0xffffeeeeddddccccL, true), 3);
		Assert.array(ints, 1, 0xffffeeee, 0xddddcccc);
		Assert.equal(m.setLong(1, 0xffffeeeeddddccccL, false), 3);
		Assert.array(ints, 1, 0xddddcccc, 0xffffeeee);
	}

	@Test
	public void shouldFillInts() {
		var m = Mutable.wrap(1, 2, 3, 4, 5);
		Assert.equal(m.fill(1, 2, 0xff), 3);
		Assert.yes(m.isEqualTo(0, 1, 0xff, 0xff, 4, 5));
		Assert.thrown(() -> m.fill(3, 3, 0));
	}

	@Test
	public void shouldCopyFromArray() {
		var m = Mutable.wrap(1, 2, 3, 4, 5);
		Assert.equal(m.setInts(3, -4, -5), 5);
		Assert.yes(m.isEqualTo(0, 1, 2, 3, -4, -5));
		Assert.thrown(() -> m.copyFrom(3, ArrayUtil.ints.of(1, 2, 3), 0));
		Assert.thrown(() -> m.copyFrom(0, ArrayUtil.ints.of(1, 2, 3), 2, 2));
	}

	@Test
	public void shouldCopyFromIntProvider() {
		var m = Mutable.wrap(1, 2, 3, 4, 5);
		Assert.equal(m.copyFrom(3, Immutable.wrap(-4, -5)), 5);
		Assert.yes(m.isEqualTo(0, 1, 2, 3, -4, -5));
		Assert.thrown(() -> m.copyFrom(3, Immutable.wrap(1, 2, 3), 0));
		Assert.thrown(() -> m.copyFrom(0, Immutable.wrap(1, 2, 3), 2, 2));
	}

	// IntArray

	@Test
	public void shouldGetLongFromInts() {
		Assert.equal(Immutable.wrap(0, 0x7fffffff, 0x80000000, 0).getLong(1, true),
			0x7fffffff80000000L);
		Assert.equal(Immutable.wrap(0, 0x7fffffff, 0x80000000, 0).getLong(1, false),
			0x800000007fffffffL);
		Assert.thrown(() -> Immutable.wrap(0, 0x7fffffff).getLong(1, true));
		Assert.thrown(() -> Immutable.wrap(0, 0x7fffffff).getLong(1, false));
	}

	@Test
	public void shouldGetString() {
		Assert.equal(Immutable.wrap("abc\ud83c\udc39de".codePoints().toArray()).getString(0),
			"abc\ud83c\udc39de");
		Assert.thrown(() -> Immutable.wrap("abcde".codePoints().toArray()).getString(3, 3));
	}

	@Test
	public void shouldCopyInts() {
		Assert.array(IntArray.Immutable.wrap().copy(0));
		Assert.array(IntArray.Immutable.wrap(1, 2, 3).copy(0), 1, 2, 3);
	}

	@Test
	public void shouldCopyToIntReceiver() {
		var m = Mutable.of(3);
		Assert.equal(Immutable.wrap(1, 2, 3).copyTo(1, m, 1), 3);
		Assert.yes(m.isEqualTo(0, 0, 2, 3));
		Assert.thrown(() -> Immutable.wrap(0, 1, 2).copyTo(0, m, 4));
	}

	@Test
	public void shouldDetermineIfEqualToInts() {
		Assert.yes(Immutable.wrap(1, 2, 3, 4, 5).isEqualTo(1, ArrayUtil.ints.of(2, 3, 4)));
		Assert.no(Immutable.wrap(1, 2, 3).isEqualTo(2, ArrayUtil.ints.of(1, 2)));
		Assert.no(Immutable.wrap(1, 2, 3).isEqualTo(0, ArrayUtil.ints.of(1, 2), 0, 3));
	}

	@Test
	public void shouldDetermineIfEqualToProviderInts() {
		Assert.yes(Immutable.wrap(1, 2, 3, 4, 5).isEqualTo(1, Immutable.wrap(2, 3, 4)));
		Assert.no(Immutable.wrap(1, 2, 3).isEqualTo(2, Immutable.wrap(1, 2)));
		Assert.no(Immutable.wrap(1, 2, 3).isEqualTo(0, Immutable.wrap(1, 2), 0, 3));
	}

	// Encoder

	@Test
	public void shouldEncodeToMinimumSizedArray() {
		Assert.array(Encoder.of().ints());
		Assert.array(Encoder.of(5).ints(), 0, 0, 0, 0, 0);
	}

	@Test
	public void shouldEncodeFixedSize() {
		var en = Encoder.fixed(3);
		en.fill(3, 1);
		Assert.thrown(() -> en.writeInt(1));
		Assert.array(en.ints(), 1, 1, 1);
	}

	@Test
	public void shouldEncodeToArray() {
		int[] array = new int[5];
		Assert.thrown(() -> IntArray.Encoder.of(array, 6));
		IntArray.Encoder.of(array).writeInts(1, 2, 3);
		Assert.array(array, 1, 2, 3, 0, 0);
	}

	@Test
	public void shouldEncodeAsIntArrayWrappers() {
		Assert.array(Encoder.of().writeString("abc").ints(), 'a', 'b', 'c');
		Assert.array(Encoder.of().writeString("abc").mutable().copy(0), 'a', 'b', 'c');
		Assert.array(Encoder.of().writeString("abc").immutable().copy(0), 'a', 'b', 'c');
	}

	@Test
	public void shouldEncodeAndReadInt() {
		Assert.equal(Encoder.of().writeInt(-1).skip(-1).readInt(), -1);
	}

	@Test
	public void shouldEncodeAndReadLongs() {
		Assert.equal(Encoder.of().writeLong(LMIN, true).skip(-2).readLong(true), LMIN);
		Assert.equal(Encoder.of().writeLong(LMIN, false).skip(-2).readLong(false), LMIN);
	}

	@Test
	public void shouldEncodeAndReadString() {
		var s = "abc\ud83c\udc39de";
		int[] ints = s.codePoints().toArray();
		int n = ints.length;
		Assert.equal(Encoder.of().writeString(s).skip(-n).readString(n), s);
		Assert.array(Encoder.of().writeString(s).ints(), 'a', 'b', 'c', 0x1f039, 'd', 'e');
	}

	@Test
	public void shouldEncodeAndReadInts() {
		Assert.array(Encoder.of().writeInts(1, 2, 3).skip(-3).readInts(3), 1, 2, 3);
	}

	@Test
	public void shouldEncodeAndReadFromIntoIntArray() {
		int[] bin = ArrayUtil.ints.of(1, 2, 3, 4, 5);
		int[] bout = new int[3];
		Encoder.of().writeFrom(bin, 1, 3).skip(-3).readInto(bout, 1, 2);
		Assert.array(bout, 0, 2, 3);
	}

	@Test
	public void shouldEncodeToAndReadIntoIntAccessor() {
		var m = Mutable.wrap(1, 2, 3, 0, 0);
		Encoder.of().writeFrom(m, 0, 3).skip(-3).readInto(m, 2);
		Assert.array(m.copy(0), 1, 2, 1, 2, 3);
	}

	@Test
	public void shouldEncodeFillInts() {
		Assert.array(Encoder.of().fill(3, -1).skip(2).ints(), -1, -1, -1, 0, 0);
	}

	@Test
	public void shouldEncodeToStream() {
		Assert.stream(Encoder.of().writeInts(1, 2, 3).offset(0).stream(3), 1, 2, 3);
	}

	@Test
	public void shouldEncodeToUstream() {
		Assert.stream(Encoder.of().writeInts(-1, -2, -3).offset(0).ustream(3), 0xffffffffL,
			0xfffffffeL, 0xfffffffdL);
	}

	@Test
	public void shouldNotGrowEncoderIfReading() {
		var en = Encoder.of();
		en.writeInts(1, 2, 3).skip(-2);
		Assert.thrown(() -> en.readInts(3));
		Assert.thrown(() -> en.readInts(Integer.MAX_VALUE));
	}

	@Test
	public void shouldFailToGrowEncoderAtomically() {
		var en = Encoder.of(0, 3).writeInts(1);
		Assert.thrown(() -> en.writeInts(1, 2, 3));
		Assert.thrown(() -> en.fill(3, 0xff));
		Assert.thrown(() -> en.skip(3));
		en.writeInts(1, 2);
	}

	@Test
	public void shouldNotGrowEncoderPastMax() {
		var en0 = Encoder.of(0, 3).writeInts(1);
		Assert.thrown(() -> en0.writeInts(1, 2, 3));
		Assert.thrown(() -> en0.fill(Integer.MAX_VALUE, 0xff));
		var en1 = Encoder.of(3, 5);
		en1.writeInts(1, 2, 3, 4);
	}

	@Test
	public void shouldNotDoubleEncoderSizePastMax() { // each growth x2 size, but <= max
		var en = Encoder.of(3, 5);
		Assert.array(en.writeInts(1, 2, 3, 4).ints(), 1, 2, 3, 4);
	}

	@Test
	public void shouldNotGrowEncoderLessThanDefaultSize() { // grow to minimum of 32 (default size)
		var en = Encoder.of(20);
		en.fill(21, 0xff); // grow to 20x2 = 40
		en.fill(60, 1); // grow to 81 (> 40x2)
	}

	// Encodable

	@Test
	public void shouldReturnEmptyArrayForZeroSizeEncodable() {
		Assert.array(encodable(() -> 0, _ -> Assert.throwRuntime()).encode());
	}

	@Test
	public void shouldEncodeFixedSizeAsEncodable() {
		Assert.array(encodable(() -> 3, enc -> enc.writeInts(1, 2, 3)).encode(), 1, 2, 3);
	}

	@Test
	public void shouldFailEncodingIfSizeDoesNotMatchBytesAsEncodable() {
		Assert.thrown(() -> encodable(() -> 2, enc -> enc.writeInts(1, 2, 3)).encode());
		Assert.thrown(() -> encodable(() -> 4, enc -> enc.writeInts(1, 2, 3)).encode());
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
