package ceri.common.data;

import static ceri.common.data.ByteUtil.BIG_ENDIAN;
import ceri.common.collection.ArrayUtil;

/**
 * Interface for receiving ints into an array. For bulk efficiency, consider overriding the
 * following methods that process one int at a time, or copy arrays.
 * 
 * <pre>
 * int fill(int index, int length, int value); [1-int]
 * int copyFrom(int index, int[] array, int offset, int length); [1-int]
 * int copyFrom(int index, IntProvider provider, int offset, int length); [1-int]
 * int readFrom(int index, InputStream in, int length) throws IOException; [1-int]
 * </pre>
 * 
 * @see ceri.common.data.MutableIntArray
 * @see ceri.common.concurrent.VolatileIntArray
 * @see ceri.dmx.spi.device.SpiPulseDevice
 * @see ceri.serial.spi.pulse.PulseBuffer
 * @see ceri.serial.spi.pulse.SpiPulseTransmitter
 */
public interface IntReceiver {

	static IntReceiver empty() {
		return IntArray.Mutable.EMPTY;
	}

	/**
	 * {@link Navigator} and {@link IntWriter} wrapper for a {@link IntReceiver}. This provides
	 * sequential writing of ints, and relative/absolute positioning for the next write. The type T
	 * allows typed access to the IntReceiver.
	 * <p/>
	 * IntWriter interface is complemented with methods that use remaining ints instead of given
	 * length. Except for {@link #offset(int)}, methods do not include an offset position. Clients
	 * must first call {@link #offset(int)} if an absolute position is required.
	 */
	static class Writer extends Navigator<Writer> implements IntWriter<Writer> {
		private final IntReceiver receiver;
		private final int start;

		private Writer(IntReceiver receiver, int offset, int length) {
			super(length);
			this.receiver = receiver;
			this.start = offset;
		}

		/* IntWriter overrides and additions */

		@Override
		public Writer writeInt(int value) {
			return position(receiver.setInt(position(), value));
		}

		/**
		 * Fill remaining ints with same value.
		 */
		public Writer fill(int value) {
			return fill(remaining(), value);
		}

		@Override
		public Writer fill(int length, int value) {
			return position(receiver.fill(position(), length, value));
		}

		@Override
		public Writer writeFrom(int[] array, int offset, int length) {
			return position(receiver.copyFrom(position(), array, offset, length));
		}

		@Override
		public Writer writeFrom(IntProvider provider, int offset, int length) {
			return position(receiver.copyFrom(position(), provider, offset, length));
		}

		/* Other methods */

		@Override
		public Writer skip(int length) {
			return super.skip(length);
		}

		public IntReceiver receiver() {
			return receiver(remaining());
		}

		public IntReceiver receiver(int length) {
			IntReceiver receiver = this.receiver.slice(position(), length);
			position(position() + length);
			return receiver;
		}

		/**
		 * Creates a new reader for remaining ints without incrementing the offset.
		 */
		public Writer slice() {
			return slice(remaining());
		}

		/**
		 * Creates a new reader for subsequent ints without incrementing the offset. Use a negative
		 * length to look backwards, which may be useful for checksum calculations.
		 */
		public Writer slice(int length) {
			int offset = length < 0 ? offset() + length : offset();
			length = Math.abs(length);
			ArrayUtil.validateSlice(length(), offset, length);
			return new Writer(receiver, start + offset, length);
		}

		/**
		 * The actual position within the int receiver.
		 */
		private int position() {
			return start + offset();
		}

		/**
		 * Set the offset from receiver actual position.
		 */
		private Writer position(int position) {
			return offset(position - start);
		}
	}

	/**
	 * Length of the space to receive ints.
	 */
	int length();

	/**
	 * Determines if the length is 0.
	 */
	default boolean isEmpty() {
		return length() == 0;
	}

	/**
	 * Sets the int value at given index, returns index + 1.
	 */
	int setInt(int index, int value);

	/**
	 * Sets ints at given index. Returns the index after the written ints.
	 */
	default int setInts(int index, int... array) {
		return copyFrom(index, array);
	}

	/**
	 * Sets int value 1 or 0 at the index. Returns the index after the written ints.
	 */
	default int setBool(int index, boolean value) {
		return setInt(index, value ? 1 : 0);
	}

	/**
	 * Sets the value in native-order ints at the index. Returns the index after the written ints.
	 */
	default int setLong(int index, long value) {
		return setLong(index, value, BIG_ENDIAN);
	}

	/**
	 * Sets the value in endian ints at the index. Returns the index after the written ints.
	 */
	default int setLong(int index, long value, boolean msb) {
		int[] ints = msb ? IntUtil.longToMsb(value) : IntUtil.longToLsb(value);
		return setInts(index, ints);
	}

	/**
	 * Sets the value in native-order ints at the index. Returns the index after the written ints.
	 */
	default int setFloat(int index, float value) {
		return setInt(index, Float.floatToIntBits(value));
	}

	/**
	 * Sets the value in native-order ints at the index. Returns the index after the written ints.
	 */
	default int setDouble(int index, double value) {
		return setLong(index, Double.doubleToLongBits(value));
	}

	/**
	 * Sets the value in endian ints at the index. Returns the index after the written ints.
	 */
	default int setDouble(int index, double value, boolean msb) {
		return setLong(index, Double.doubleToLongBits(value), msb);
	}

	/**
	 * Sets string code points. Returns the index after the written ints.
	 */
	default int setString(int index, String s) {
		int[] ints = s.codePoints().toArray();
		return copyFrom(index, ints);
	}

	/**
	 * Creates an int receiver view from index.
	 */
	default IntReceiver slice(int index) {
		return slice(index, length() - index);
	}

	/**
	 * Creates an int receiver sub-view. A negative length will right-justify the view. Returns the
	 * current receiver for zero index and same length.
	 */
	default IntReceiver slice(int index, int length) {
		if (length == 0) return empty();
		if (index == 0 && length == length()) return this;
		throw new UnsupportedOperationException(
			String.format("slice(%d, %d) is not supported", index, length));
	}

	/**
	 * Fills ints from the index with the same value. Returns the index after the written ints.
	 */
	default int fill(int index, int value) {
		return fill(index, length() - index, value);
	}

	/**
	 * Fills ints from the index with the same value. Returns the index after the written ints.
	 * Default implementation copies one int at a time; efficiency may be improved by overriding
	 * this method.
	 */
	default int fill(int index, int length, int value) {
		ArrayUtil.validateSlice(length(), index, length);
		while (length-- > 0)
			setInt(index++, value);
		return index;
	}

	/**
	 * Copies ints from the array to the index. Returns the index after the written ints.
	 */
	default int copyFrom(int index, int[] array) {
		return copyFrom(index, array, 0);
	}

	/**
	 * Copies ints from the array to the index. Returns the index after the written ints.
	 */
	default int copyFrom(int index, int[] array, int offset) {
		return copyFrom(index, array, offset, array.length - offset);
	}

	/**
	 * Copies ints from the array to the index. Returns the index after the written ints. Default
	 * implementation copies one int at a time; efficiency may be improved by overriding this
	 * method.
	 */
	default int copyFrom(int index, int[] array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		ArrayUtil.validateSlice(length(), index, length);
		while (length-- > 0)
			setInt(index++, array[offset++]);
		return index;
	}

	/**
	 * Copies ints from the provider to the index. Returns the index after the written ints.
	 */
	default int copyFrom(int index, IntProvider array) {
		return copyFrom(index, array, 0);
	}

	/**
	 * Copies ints from the provider to the index. Returns the index after the written ints.
	 */
	default int copyFrom(int index, IntProvider array, int offset) {
		return copyFrom(index, array, offset, array.length() - offset);
	}

	/**
	 * Copies ints from the provider to the index. Returns the index after the written ints. Default
	 * implementation copies one int at a time; efficiency may be improved by overriding this
	 * method.
	 */
	default int copyFrom(int index, IntProvider provider, int offset, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		ArrayUtil.validateSlice(provider.length(), offset, length);
		while (length-- > 0)
			setInt(index++, provider.getInt(offset++));
		return index;
	}

	/**
	 * Provides sequential int access.
	 */
	default Writer writer(int index) {
		return writer(index, length() - index);
	}

	/**
	 * Provides sequential int access.
	 */
	default Writer writer(int index, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		return new Writer(this, index, length);
	}

}
