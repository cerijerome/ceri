package ceri.common.data;

import static ceri.common.collection.ArrayUtil.EMPTY_INT;
import static ceri.common.validation.ValidationUtil.validateEqual;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.Fluent;
import ceri.common.math.MathUtil;
import ceri.common.validation.ValidationUtil;

/**
 * Base wrapper for an int array, implementing the IntProvider interface. The Immutable sub-class
 * provides a concrete type. The Mutable sub-class also implements IntReceiver, which provides
 * mutable access to the array.
 * <p/>
 * Internally, offset and length mark the permitted access window of the array. Ints outside this
 * window must not be accessed or copied.
 */
public abstract class IntArray implements IntProvider {
	private static final int MAX_LEN_FOR_STRING = 8;
	final int[] array;
	private final int offset;
	private final int length;

	/**
	 * Wrapper for an int array that does not allow modification. It allows slicing of views to
	 * promote re-use rather than copying of ints. Note that wrap() does not copy the original int
	 * array, and modifications of the original array will modify the wrapped array. If slicing
	 * large arrays, copying may be better for long-term objects, as the reference to the original
	 * array is no longer held.
	 */
	public static class Immutable extends IntArray implements Fluent<Immutable> {
		public static final Immutable EMPTY = new Immutable(EMPTY_INT, 0, 0);

		public static Immutable copyOf(int[] array) {
			return copyOf(array, 0);
		}

		public static Immutable copyOf(int[] array, int offset) {
			return copyOf(array, offset, array.length - offset);
		}

		public static Immutable copyOf(int[] array, int offset, int length) {
			int[] newArray = Arrays.copyOfRange(array, offset, offset + length);
			return wrap(newArray);
		}

		public static Immutable wrap(int... array) {
			return wrap(array, 0);
		}

		public static Immutable wrap(int[] array, int offset) {
			return wrap(array, offset, array.length - offset);
		}

		public static Immutable wrap(int[] array, int offset, int length) {
			ValidationUtil.validateSlice(array.length, offset, length);
			if (length == 0) return EMPTY;
			return new Immutable(array, offset, length);
		}

		private Immutable(int[] array, int offset, int length) {
			super(array, offset, length);
		}

		/* IntProvider overrides */

		@Override
		public Immutable slice(int index) {
			return slice(index, length() - index);
		}

		@Override
		public Immutable slice(int index, int length) {
			if (length == 0) return EMPTY;
			if (length < 0) return slice(index + length, -length);
			validateSlice(index, length);
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
	 * Wrapper for an int array that allows modification. It allows slicing of views to promote
	 * re-use rather than copying of ints. Note that wrap() does not copy the original int array,
	 * and modifications of the original array will modify the wrapped array.
	 */
	public static class Mutable extends IntArray implements IntAccessor, Fluent<Mutable> {
		public static final Mutable EMPTY = new Mutable(EMPTY_INT, 0, 0);

		public static Mutable of(int length) {
			return wrap(new int[length]);
		}

		public static Mutable wrap(int... array) {
			return wrap(array, 0);
		}

		public static Mutable wrap(int[] array, int offset) {
			return wrap(array, offset, array.length - offset);
		}

		public static Mutable wrap(int[] array, int offset, int length) {
			ValidationUtil.validateSlice(array.length, offset, length);
			if (length == 0) return EMPTY;
			return new Mutable(array, offset, length);
		}

		private Mutable(int[] array, int offset, int length) {
			super(array, offset, length);
		}

		/**
		 * Returns an immutable view. Any changes to this class will be seen in new view.
		 */
		public Immutable asImmutable() {
			return Immutable.wrap(array, offset(0), length());
		}

		/* IntProvider overrides */

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
			validateSlice(index, length);
			if (index == 0 && length == length()) return this;
			return Mutable.wrap(array, offset(index), length);
		}

		/* IntReceiver overrides */

		@Override
		public int setInt(int index, int i) {
			array[offset(index++)] = i;
			return index;
		}

		@Override
		public int fill(int index, int length, int value) {
			validateSlice(index, length);
			Arrays.fill(array, offset(index), offset(index + length), value);
			return index + length;
		}

		@Override
		public int copyFrom(int index, int[] array, int offset, int length) {
			validateSlice(index, length);
			ValidationUtil.validateSlice(array.length, offset, length);
			System.arraycopy(array, offset, this.array, offset(index), length);
			return index + length;
		}

		@Override
		public int copyFrom(int index, IntProvider provider, int offset, int length) {
			validateSlice(index, length);
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
	 * Wrapper for an expandable int array, implementing IntWriter and IntReader. Reader methods
	 * will not increase the size of the array, and can throw IndexOutOfBoundsException if out of
	 * range. Writer methods will increase the size of the array as needed, up to MAX_SIZE.
	 * <p/>
	 * Internally, length is the current size, either the initial minimum size, or advanced by
	 * writing ints. The length determines the size of the array for ints(), mutable(), immutable()
	 * calls.
	 */
	public static class Encoder extends Navigator<Encoder>
		implements IntWriter<Encoder>, IntReader {
		public static final int MAX_DEF = Integer.MAX_VALUE - 8; // from ByteArrayOutputStream
		private static final int SIZE_DEF = 32;
		private final int max;
		private Mutable mutable;
		private int[] array;

		/**
		 * Create an encoder with zero length, and default buffer size.
		 */
		public static Encoder of() {
			return new Encoder(new int[SIZE_DEF], 0, MAX_DEF);
		}

		/**
		 * Create an encoder with minimum length, and matching buffer size. Useful to optimize
		 * delivery of the int array if the length is known ahead of time.
		 */
		public static Encoder of(int min) {
			return of(min, MAX_DEF);
		}

		/**
		 * Create an encoder with minimum length, and matching buffer size. Useful to optimize
		 * delivery of the int array if the length is known ahead of time. The max determines the
		 * maximum size the int array can grow to.
		 */
		public static Encoder of(int min, int max) {
			return new Encoder(new int[min], min, max);
		}

		/**
		 * Create an encoder that only writes to the array.
		 */
		public static Encoder of(int[] array) {
			return of(array, array.length);
		}

		/**
		 * Create an encoder that only writes to the array.
		 */
		public static Encoder of(int[] array, int max) {
			ValidationUtil.validateSubRange(array.length, 0, max);
			return new Encoder(array, 0, array.length);
		}

		/**
		 * Create an encoder with fixed length.
		 */
		public static Encoder fixed(int size) {
			return new Encoder(new int[size], size, size);
		}

		private Encoder(int[] array, int limit, int max) {
			super(limit);
			this.max = max;
			this.array = array;
			mutable = Mutable.wrap(array);
		}

		/**
		 * Returns the int array. Makes a sub-copy if the array size is not the current length.
		 */
		public int[] ints() {
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

		/* IntReader */

		@Override
		public int readInt() {
			return array[readInc(1)];
		}

		@Override
		public int[] readInts(int length) {
			return mutable.copy(readInc(length), length);
		}

		@Override
		public int readInto(int[] dest, int offset, int length) {
			return mutable.copyTo(readInc(length), dest, offset, length);
		}

		@Override
		public int readInto(IntReceiver receiver, int offset, int length) {
			return mutable.copyTo(readInc(length), receiver, offset, length);
		}

		@Override
		public IntStream stream(int length) {
			return mutable.stream(readInc(length), length);
		}

		@Override
		public LongStream ustream(int length) {
			return mutable.ustream(readInc(length), length);
		}

		/* IntWriter */

		@Override
		public Encoder skip(int length) {
			writeInc(length);
			return this;
		}

		@Override
		public Encoder writeInt(int value) {
			int current = writeInc(1); // may modify mutable reference, don't combine
			mutable.setInt(current, value);
			return this;
		}

		@Override
		public Encoder fill(int length, int value) {
			int current = writeInc(length); // may modify mutable reference, don't combine
			mutable.fill(current, length, value);
			return this;
		}

		@Override
		public Encoder writeFrom(int[] array, int offset, int length) {
			int current = writeInc(length); // may modify mutable reference, don't combine
			mutable.copyFrom(current, array, offset, length);
			return this;
		}

		@Override
		public Encoder writeFrom(IntProvider provider, int offset, int length) {
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
			if (MathUtil.uint(newLen) > max) newLen = max;
			if (newLen < SIZE_DEF) newLen = Math.min(SIZE_DEF, max);
			if (newLen < length) newLen = length;
			array = Arrays.copyOf(array, newLen);
			mutable = Mutable.wrap(array);
		}

		private void verifyGrowLength(int length) {
			if (MathUtil.uint(length) > max) throw new IllegalArgumentException(String
				.format("Max allocation size %1$d (0x%1$x) ints: %2$d (0x%2$x)", max, length));
		}
	}

	/**
	 * Interface for fixed-size encoding. This optimizes the encoded int array size.
	 */
	public static interface Encodable {
		int size();

		void encode(Encoder encoder);

		default IntProvider encode() {
			int size = size();
			if (size == 0) return Immutable.EMPTY;
			Encoder encoder = Encoder.fixed(size).apply(this::encode);
			validateEqual(encoder.remaining(), 0, "Remaining ints");
			return encoder.immutable();
		}
	}

	private IntArray(int[] array, int offset, int length) {
		this.array = array;
		this.offset = offset;
		this.length = length;
	}

	/* IntProvider overrides */

	@Override
	public int length() {
		return length;
	}

	@Override
	public int getInt(int index) {
		return array[offset(index)];
	}

	@Override
	public String getString(int index, int length) {
		validateSlice(index, length);
		return new String(array, offset(index), length);
	}

	@Override
	public int[] copy(int index, int length) {
		if (length == 0) return ArrayUtil.EMPTY_INT;
		validateSlice(index, length);
		return Arrays.copyOfRange(array, offset(index), offset(index + length));
	}

	@Override
	public int copyTo(int index, int[] dest, int offset, int length) {
		validateSlice(index, length);
		ValidationUtil.validateSlice(dest.length, offset, length);
		System.arraycopy(array, offset(index), dest, offset, length);
		return offset + length;
	}

	@Override
	public int copyTo(int index, IntReceiver receiver, int offset, int length) {
		validateSlice(index, length);
		receiver.copyFrom(offset, array, offset(index), length);
		return index + length;
	}

	@Override
	public boolean isEqualTo(int index, int[] array, int offset, int length) {
		if (!isValidSlice(index, length)) return false;
		if (!ArrayUtil.isValidSlice(array.length, offset, length)) return false;
		return ArrayUtil.equals(this.array, offset(index), array, offset, length);
	}

	@Override
	public boolean isEqualTo(int index, IntProvider provider, int offset, int length) {
		if (!isValidSlice(index, length)) return false;
		return provider.isEqualTo(offset, array, offset(index), length);
	}

	/* Object overrides */

	@Override
	public String toString() {
		return getClass().getSimpleName() + IntProvider.toString(this, MAX_LEN_FOR_STRING);
	}

	/* Support methods */

	boolean isEqual(IntArray other) {
		if (length != other.length) return false;
		return ArrayUtil.equals(array, offset(0), other.array, other.offset(0), length);
	}

	int hash() {
		return ArrayUtil.hash(array, offset, length);
	}

	boolean isValidSlice(int index, int length) {
		return ArrayUtil.isValidSlice(this.length, index, length);
	}

	void validateSlice(int index, int length) {
		ValidationUtil.validateSlice(this.length, index, length);
	}

	int offset(int index) {
		return this.offset + index;
	}
}
