package ceri.common.data;

import java.util.Arrays;
import ceri.common.array.Array;
import ceri.common.function.Fluent;
import ceri.common.math.Maths;
import ceri.common.stream.LongStream;
import ceri.common.util.Validate;

/**
 * Base wrapper for an long array, implementing the LongProvider interface. The Immutable sub-class
 * provides a concrete type. The Mutable sub-class also implements LongReceiver, which provides
 * mutable access to the array.
 * <p/>
 * Internally, offset and length mark the permitted access window of the array. Longs outside this
 * window must not be accessed or copied.
 */
public abstract class LongArray implements LongProvider {
	final long[] array;
	private final int offset;
	private final int length;

	/**
	 * Wrapper for an long array that does not allow modification. It allows slicing of views to
	 * promote re-use rather than copying of longs. Note that wrap() does not copy the original long
	 * array, and modifications of the original array will modify the wrapped array. If slicing
	 * large arrays, copying may be better for long-term objects, as the reference to the original
	 * array is no longer held.
	 */
	public static class Immutable extends LongArray implements Fluent<Immutable> {
		public static final Immutable EMPTY = new Immutable(Array.longs.empty, 0, 0);

		public static Immutable copyOf(long[] array) {
			return copyOf(array, 0);
		}

		public static Immutable copyOf(long[] array, int offset) {
			return copyOf(array, offset, array.length - offset);
		}

		public static Immutable copyOf(long[] array, int offset, int length) {
			long[] newArray = Arrays.copyOfRange(array, offset, offset + length);
			return wrap(newArray);
		}

		public static Immutable wrap(long... array) {
			return wrap(array, 0);
		}

		public static Immutable wrap(long[] array, int offset) {
			return wrap(array, offset, array.length - offset);
		}

		public static Immutable wrap(long[] array, int offset, int length) {
			Validate.slice(array.length, offset, length);
			if (length == 0) return EMPTY;
			return new Immutable(array, offset, length);
		}

		private Immutable(long[] array, int offset, int length) {
			super(array, offset, length);
		}

		/* LongProvider overrides */

		@Override
		public Immutable slice(int index) {
			return slice(index, length() - index);
		}

		@Override
		public Immutable slice(int index, int length) {
			if (length == 0) return EMPTY;
			if (length < 0) return slice(index + length, -length);
			Validate.slice(length(), index, length);
			if (index == 0 && length == length()) return this;
			return wrap(array, offset(index), length);
		}

		/* Object overrides */

		@Override
		public int hashCode() {
			return hash();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			return (obj instanceof Immutable other) && isEqual(other);
		}
	}

	/**
	 * Wrapper for an long array that allows modification. It allows slicing of views to promote
	 * re-use rather than copying of longs. Note that wrap() does not copy the original long array,
	 * and modifications of the original array will modify the wrapped array.
	 */
	public static class Mutable extends LongArray implements LongAccessor, Fluent<Mutable> {
		public static final Mutable EMPTY = new Mutable(Array.longs.empty, 0, 0);

		public static Mutable of(int length) {
			return wrap(new long[length]);
		}

		public static Mutable wrap(long... array) {
			return wrap(array, 0);
		}

		public static Mutable wrap(long[] array, int offset) {
			return wrap(array, offset, array.length - offset);
		}

		public static Mutable wrap(long[] array, int offset, int length) {
			Validate.slice(array.length, offset, length);
			if (length == 0) return EMPTY;
			return new Mutable(array, offset, length);
		}

		private Mutable(long[] array, int offset, int length) {
			super(array, offset, length);
		}

		/**
		 * Returns an immutable view. Any changes to this class will be seen in new view.
		 */
		public Immutable asImmutable() {
			return Immutable.wrap(array, offset(0), length());
		}

		/* LongProvider overrides */

		@Override
		public boolean isEmpty() {
			return super.isEmpty();
		}

		@Override
		public Mutable slice(int index) {
			return slice(index, length() - index);
		}

		@Override
		public Mutable slice(int index, int length) {
			if (length == 0) return Mutable.EMPTY;
			if (length < 0) return slice(index + length, -length);
			Validate.slice(length(), index, length);
			if (index == 0 && length == length()) return this;
			return Mutable.wrap(array, offset(index), length);
		}

		/* LongReceiver overrides */

		@Override
		public int setLong(int index, long l) {
			array[offset(index++)] = l;
			return index;
		}

		@Override
		public int fill(int index, int length, long value) {
			Validate.slice(length(), index, length);
			Arrays.fill(array, offset(index), offset(index + length), value);
			return index + length;
		}

		@Override
		public int copyFrom(int index, long[] array, int offset, int length) {
			Validate.slice(length(), index, length);
			Validate.slice(array.length, offset, length);
			System.arraycopy(array, offset, this.array, offset(index), length);
			return index + length;
		}

		@Override
		public int copyFrom(int index, LongProvider provider, int offset, int length) {
			Validate.slice(length(), index, length);
			provider.copyTo(offset, array, offset(index), length);
			return index + length;
		}

		/* Object overrides */

		@Override
		public int hashCode() {
			return hash();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			return (obj instanceof Mutable other) && isEqual(other);
		}
	}

	/**
	 * Wrapper for an expandable long array, implementing LongWriter and LongReader. Reader methods
	 * will not increase the size of the array, and can throw IndexOutOfBoundsException if out of
	 * range. Writer methods will increase the size of the array as needed, up to MAX_SIZE.
	 * <p/>
	 * Internally, length is the current size, either the initial minimum size, or advanced by
	 * writing longs. The length determines the size of the array for longs(), mutable(),
	 * immutable() calls.
	 */
	public static class Encoder extends Navigator<Encoder>
		implements LongWriter<Encoder>, LongReader {
		public static final int MAX_DEF = Integer.MAX_VALUE - 8; // from ByteArrayOutputStream
		private static final int SIZE_DEF = 32;
		private final int max;
		private Mutable mutable;
		private long[] array;

		/**
		 * Create an encoder with zero length, and default buffer size.
		 */
		public static Encoder of() {
			return new Encoder(new long[SIZE_DEF], 0, MAX_DEF);
		}

		/**
		 * Create an encoder with minimum length, and matching buffer size. Useful to optimize
		 * delivery of the long array if the length is known ahead of time.
		 */
		public static Encoder of(int min) {
			return of(min, MAX_DEF);
		}

		/**
		 * Create an encoder with minimum length, and matching buffer size. Useful to optimize
		 * delivery of the long array if the length is known ahead of time. The max determines the
		 * maximum size the long array can grow to.
		 */
		public static Encoder of(int min, int max) {
			return new Encoder(new long[min], min, max);
		}

		/**
		 * Create an encoder that only writes to the array.
		 */
		public static Encoder of(long[] array) {
			return of(array, array.length);
		}

		/**
		 * Create an encoder that only writes to the array.
		 */
		public static Encoder of(long[] array, int max) {
			Validate.slice(array.length, 0, max);
			return new Encoder(array, 0, array.length);
		}

		/**
		 * Create an encoder with fixed length.
		 */
		public static Encoder fixed(int size) {
			return new Encoder(new long[size], size, size);
		}

		private Encoder(long[] array, int limit, int max) {
			super(limit);
			this.max = max;
			this.array = array;
			mutable = Mutable.wrap(array);
		}

		/**
		 * Returns the long array. Makes a sub-copy if the array size is not the current length.
		 */
		public long[] longs() {
			if (length() == array.length) return array;
			return Arrays.copyOf(array, length());
		}

		/**
		 * Returns a mutable array.
		 */
		public Mutable mutable() {
			return Mutable.wrap(array, 0, length());
		}

		/**
		 * Returns an immutable array.
		 */
		public Immutable immutable() {
			return Immutable.wrap(array, 0, length());
		}

		/* LongReader */

		@Override
		public long readLong() {
			return array[readInc(1)];
		}

		@Override
		public long[] readLongs(int length) {
			return mutable.copy(readInc(length), length);
		}

		@Override
		public int readInto(long[] dest, int offset, int length) {
			return mutable.copyTo(readInc(length), dest, offset, length);
		}

		@Override
		public int readInto(LongReceiver receiver, int offset, int length) {
			return mutable.copyTo(readInc(length), receiver, offset, length);
		}

		@Override
		public LongStream<RuntimeException> stream(int length) {
			return mutable.stream(readInc(length), length);
		}

		/* LongWriter */

		@Override
		public Encoder skip(int length) {
			writeInc(length);
			return this;
		}

		@Override
		public Encoder writeLong(long value) {
			int current = writeInc(1); // may modify mutable reference, don't combine
			mutable.setLong(current, value);
			return this;
		}

		@Override
		public Encoder fill(int length, long value) {
			int current = writeInc(length); // may modify mutable reference, don't combine
			mutable.fill(current, length, value);
			return this;
		}

		@Override
		public Encoder writeFrom(long[] array, int offset, int length) {
			int current = writeInc(length); // may modify mutable reference, don't combine
			mutable.copyFrom(current, array, offset, length);
			return this;
		}

		@Override
		public Encoder writeFrom(LongProvider provider, int offset, int length) {
			int current = writeInc(length); // may modify mutable reference, don't combine
			mutable.copyFrom(current, provider, offset, length);
			return this;
		}

		private int readInc(int length) {
			return inc(length, false);
		}

		private int writeInc(int length) {
			return inc(length, true);
		}

		/**
		 * Returns the current position and increments the offset by given length. If this exceeds
		 * the limit, and growth is permitted, the int array is expanded to contain the new length.
		 * If growth is required but not permitted, an exception is thrown.
		 */
		private int inc(int length, boolean grow) {
			int current = offset();
			int offset = current + length;
			if (!grow && (offset < 0 || offset > length())) // includes overflow
				throw new IndexOutOfBoundsException(offset);
			if (grow) grow(offset);
			length(Math.max(length(), offset));
			offset(offset);
			return current;
		}

		private void grow(int length) {
			verifyGrowLength(length);
			if (length <= array.length) return;
			int newLen = array.length << 1;
			if (Maths.uint(newLen) > max) newLen = max;
			if (newLen < SIZE_DEF) newLen = Math.min(SIZE_DEF, max);
			if (newLen < length) newLen = length;
			array = Arrays.copyOf(array, newLen);
			mutable = Mutable.wrap(array);
		}

		private void verifyGrowLength(int length) {
			if (Maths.uint(length) > max) throw new IllegalArgumentException(String
				.format("Max allocation size %1$d (0x%1$x) longs: %2$d (0x%2$x)", max, length));
		}
	}

	/**
	 * Interface for fixed-size encoding. This optimizes the encoded int array size.
	 */
	public static interface Encodable {
		int size();

		void encode(Encoder encoder);

		default LongProvider encode() {
			int size = size();
			if (size == 0) return Immutable.EMPTY;
			var encoder = Encoder.fixed(size).apply(this::encode);
			Validate.equal(encoder.remaining(), 0, "Remaining longs");
			return encoder.immutable();
		}
	}

	private LongArray(long[] array, int offset, int length) {
		this.array = array;
		this.offset = offset;
		this.length = length;
	}

	/* LongProvider overrides */

	@Override
	public int length() {
		return length;
	}

	@Override
	public long getLong(int index) {
		return array[offset(index)];
	}

	@Override
	public long[] copy(int index, int length) {
		if (length == 0) return Array.longs.empty;
		Validate.slice(length(), index, length);
		return Arrays.copyOfRange(array, offset(index), offset(index + length));
	}

	@Override
	public int copyTo(int index, long[] dest, int offset, int length) {
		Validate.slice(length(), index, length);
		Validate.slice(dest.length, offset, length);
		System.arraycopy(array, offset(index), dest, offset, length);
		return offset + length;
	}

	@Override
	public int copyTo(int index, LongReceiver receiver, int offset, int length) {
		Validate.slice(length(), index, length);
		receiver.copyFrom(offset, array, offset(index), length);
		return index + length;
	}

	@Override
	public boolean isEqualTo(int index, long[] array, int offset, int length) {
		if (!Array.isValidSlice(length(), index, length)) return false;
		if (!Array.isValidSlice(array.length, offset, length)) return false;
		return Array.longs.equals(this.array, offset(index), array, offset, length);
	}

	@Override
	public boolean isEqualTo(int index, LongProvider provider, int offset, int length) {
		if (!Array.isValidSlice(length(), index, length)) return false;
		return provider.isEqualTo(offset, array, offset(index), length);
	}

	/* Object overrides */

	@Override
	public String toString() {
		return LongProvider.toString(this);
	}

	/* Support methods */

	boolean isEqual(LongArray other) {
		if (length != other.length) return false;
		return Array.longs.equals(array, offset(0), other.array, other.offset(0), length);
	}

	int hash() {
		return Array.longs.hash(array, offset, length);
	}

	int offset(int index) {
		return this.offset + index;
	}
}
