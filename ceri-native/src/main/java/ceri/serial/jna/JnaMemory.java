package ceri.serial.jna;

import static ceri.common.data.ByteUtil.BIG_ENDIAN;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteAccessor;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReceiver;

/**
 * Byte accessor wrapper for memory.
 */
public class JnaMemory implements ByteAccessor {
	private static final int MAX_LEN_FOR_STRING = 8;
	public static final JnaMemory EMPTY = JnaMemory.of(null, 0, 0);
	private final Pointer p;
	private final int offset;
	private final int length;

	public static JnaMemory of(Memory m) {
		return of(m, 0);
	}

	public static JnaMemory of(Memory m, int offset) {
		return of(m, offset, JnaUtil.size(m) - offset);
	}

	public static JnaMemory of(Pointer p, int offset, int length) {
		return new JnaMemory(p, offset, length);
	}

	/**
	 * Extends ByteProvider.Reader<?> for JNA-specific sequential access to bytes.
	 */
	public static class Reader extends ByteProvider.Reader<Reader> {
		private final JnaMemory m;

		private Reader(JnaMemory m, int offset, int length) {
			super(m, offset, length);
			this.m = m;
		}

		/**
		 * Returns the value from native-order bytes.
		 */
		public NativeLong readNlong() {
			return new NativeLong(readEndian(NativeLong.SIZE, BIG_ENDIAN), false);
		}

		/**
		 * Returns the value from big-endian bytes.
		 */
		public NativeLong readNlongMsb() {
			return new NativeLong(readEndian(NativeLong.SIZE, true), false);
		}

		/**
		 * Returns the value from little-endian bytes.
		 */
		public NativeLong readNlongLsb() {
			return new NativeLong(readEndian(NativeLong.SIZE, false), false);
		}

		/**
		 * Returns the unsigned value from native-order bytes.
		 */
		public NativeLong readUnlong() {
			return new NativeLong(readEndian(NativeLong.SIZE, BIG_ENDIAN), true);
		}

		/**
		 * Returns the unsigned value from big-endian bytes.
		 */
		public NativeLong readUnlongMsb() {
			return new NativeLong(readEndian(NativeLong.SIZE, true), true);
		}

		/**
		 * Returns the unsigned value from little-endian bytes.
		 */
		public NativeLong readUnlongLsb() {
			return new NativeLong(readEndian(NativeLong.SIZE, false), true);
		}

		/**
		 * Reads bytes into the memory pointer. Returns the destination offset after reading.
		 */
		public int readInto(Memory m) {
			return readInto(m, 0);
		}

		/**
		 * Reads bytes into the memory pointer. Returns the destination offset after reading.
		 */
		public int readInto(Memory m, int offset) {
			return readInto(m, offset, JnaUtil.size(m) - offset);
		}

		/**
		 * Reads bytes into the memory pointer. Returns the destination offset after reading.
		 * Default implementation reads one byte at a time; efficiency may be improved by
		 * overriding.
		 */
		public int readInto(Pointer p, int offset, int length) {
			return m.copyTo(inc(length), p, offset, length);
		}

		/**
		 * Creates a new reader for remaining bytes without incrementing the offset.
		 */
		@Override
		public Reader slice() {
			return slice(remaining());
		}

		/**
		 * Creates a new reader for subsequent bytes without incrementing the offset.
		 */
		@Override
		public Reader slice(int length) {
			ArrayUtil.validateSlice(length(), offset(), length);
			return new Reader(m, position(), length);
		}
	}

	/**
	 * Extends ByteReceiver.Writer for JNA-specific sequential access to bytes.
	 */
	public static class Writer extends ByteReceiver.Writer<Writer> {
		private final JnaMemory m;

		private Writer(JnaMemory m, int offset, int length) {
			super(m, offset, length);
			this.m = m;
		}

		/**
		 * Writes native-order bytes.
		 */
		public Writer writeNlong(NativeLong value) {
			return writeEndian(value.longValue(), NativeLong.SIZE, BIG_ENDIAN);
		}

		/**
		 * Writes big-endian bytes.
		 */
		public Writer writeNlongMsb(NativeLong value) {
			return writeEndian(value.longValue(), NativeLong.SIZE, true);
		}

		/**
		 * Writes little-endian bytes.
		 */
		public Writer writeNlongLsb(NativeLong value) {
			return writeEndian(value.longValue(), NativeLong.SIZE, false);
		}

		/**
		 * Writes bytes from the memory pointer.
		 */
		public Writer writeFrom(Memory m) {
			return writeFrom(m, 0);
		}

		/**
		 * Writes bytes from the memory pointer.
		 */
		public Writer writeFrom(Memory m, int offset) {
			return writeFrom(m, offset, JnaUtil.size(m) - offset);
		}

		/**
		 * Writes bytes from the memory pointer. Default implementation writes one byte at a time;
		 * efficiency may be improved by overriding.
		 */
		public Writer writeFrom(Pointer p, int offset, int length) {
			return position(m.copyFrom(position(), p, offset, length));
		}

		@Override
		public Writer slice() {
			return slice(remaining());
		}

		@Override
		public Writer slice(int length) {
			ArrayUtil.validateSlice(length(), offset(), length);
			return new Writer(m, position(), length);
		}
	}

	private JnaMemory(Pointer p, int offset, int length) {
		this.p = p;
		this.offset = offset;
		this.length = length;
	}

	/* ByteProvider overrides and additions */

	@Override
	public int length() {
		return length;
	}

	@Override
	public byte getByte(int index) {
		ArrayUtil.validateIndex(length(), index);
		return p.getByte(offset(index));
	}

	/**
	 * Returns the value from native-order bytes at given index.
	 */
	public NativeLong getNlong(int index) {
		return getNlong(index, BIG_ENDIAN, false);
	}

	/**
	 * Returns the value from big-endian bytes at given index.
	 */
	public NativeLong getNlongMsb(int index) {
		return getNlong(index, true, false);
	}

	/**
	 * Returns the value from little-endian bytes at given index.
	 */
	public NativeLong getNlongLsb(int index) {
		return getNlong(index, false, false);
	}

	/**
	 * Returns the unsigned value from native-order bytes at given index.
	 */
	public NativeLong getUnlong(int index) {
		return getNlong(index, BIG_ENDIAN, true);
	}

	/**
	 * Returns the unsigned value from big-endian bytes at given index.
	 */
	public NativeLong getUnlongMsb(int index) {
		return getNlong(index, true, true);
	}

	/**
	 * Returns the unsigned value from little-endian bytes at given index.
	 */
	public NativeLong getUnlongLsb(int index) {
		return getNlong(index, false, true);
	}

	@Override
	public int copyTo(int index, byte[] array, int offset, int length) {
		return JnaUtil.read(p, offset(index), array, offset, length);
	}

	@Override
	public int copyTo(int index, ByteReceiver receiver, int offset, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		if (receiver instanceof JnaMemory m) m.copyFrom(offset, p, offset(index), length);
		else receiver.copyFrom(offset, copy(index, length));
		return index + length;
	}

	/**
	 * Copies bytes from memory at index to the memory pointer. Returns the index after copying.
	 */
	public int copyTo(int index, Memory m) {
		return copyTo(index, m, 0);
	}

	/**
	 * Copies bytes from memory at index to the memory pointer. Returns the index after copying.
	 */
	public int copyTo(int index, Memory m, int offset) {
		return copyTo(index, m, offset, JnaUtil.size(m) - offset);
	}

	/**
	 * Copies bytes from memory at index to the memory pointer. Returns the index after copying.
	 */
	public int copyTo(int index, Pointer p, int offset, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		JnaUtil.memcpy(p, offset, this.p, offset(index), length);
		return index + length;
	}

	@Override
	public int writeTo(int index, OutputStream out, int length) throws IOException {
		return ByteProvider.writeBufferTo(this, index, out, length);
	}

	@Override
	public Reader reader(int index) {
		return reader(index, length() - index);
	}

	@Override
	public Reader reader(int index, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		return new Reader(this, index, length);
	}

	/* ByteReceiver overrides and additions */

	@Override
	public int setByte(int index, int b) {
		ArrayUtil.validateIndex(length(), index);
		p.setByte(offset(index), (byte) b);
		return index + 1;
	}

	/**
	 * Sets the value in native-order bytes at the index. Returns the index after the written bytes.
	 */
	public int setNlong(int index, NativeLong value) {
		return setNlong(index, value, BIG_ENDIAN);
	}

	/**
	 * Sets the value in big-endian bytes at the index. Returns the index after the written bytes.
	 */
	public int setNlongMsb(int index, NativeLong value) {
		return setNlong(index, value, true);
	}

	/**
	 * Sets the value in little-endian bytes at the index. Returns the index after the written
	 * bytes.
	 */
	public int setNlongLsb(int index, NativeLong value) {
		return setNlong(index, value, false);
	}

	@Override
	public int fill(int index, int length, int value) {
		ArrayUtil.validateSlice(length(), index, length);
		JnaUtil.fill(p, offset(index), length, value);
		return index + length;
	}

	@Override
	public int copyFrom(int index, byte[] array, int offset, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		p.write(offset(index), array, offset, length);
		return index + length;
	}

	@Override
	public int copyFrom(int index, ByteProvider provider, int offset, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		if (provider instanceof JnaMemory m) m.copyTo(offset, p, offset(index), length);
		else copyFrom(index, provider.copy(offset, length));
		return index + length;
	}

	/**
	 * Copies bytes from the memory pointer to memory at index. Returns the index after the written
	 * bytes.
	 */
	public int copyFrom(int index, Memory m) {
		return copyFrom(index, m, 0);
	}

	/**
	 * Copies bytes from the memory pointer to memory at index. Returns the index after the written
	 * bytes.
	 */
	public int copyFrom(int index, Memory m, int offset) {
		return copyFrom(index, m, offset, JnaUtil.size(m) - offset);
	}

	/**
	 * Copies bytes from the memory pointer to memory at index. Returns the index after the written
	 * bytes.
	 */
	public int copyFrom(int index, Pointer p, int offset, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		JnaUtil.memcpy(this.p, offset(index), p, offset, length);
		return index + length;
	}

	@Override
	public int readFrom(int index, InputStream in, int length) throws IOException {
		return ByteReceiver.readBufferFrom(this, index, in, length);
	}

	@Override
	public Writer writer(int index) {
		return writer(index, length() - index);
	}

	@Override
	public Writer writer(int index, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		return new Writer(this, index, length);
	}

	/* Other methods */

	@Override
	public JnaMemory slice(int index) {
		return slice(index, length() - index);
	}

	@Override
	public JnaMemory slice(int index, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		return JnaMemory.of(p, offset(index), length);
	}

	public Pointer pointer() {
		return p == null ? null : p.share(offset);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(peer(), length);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof JnaMemory other)) return false;
		if (peer() != other.peer()) return false;
		if (length != other.length) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s@%x%s", getClass().getSimpleName(), PointerUtil.peer(p) + offset,
			ByteProvider.toHex(this, MAX_LEN_FOR_STRING));
	}

	/* Support methods */

	private NativeLong getNlong(int index, boolean msb, boolean unsigned) {
		return new NativeLong(getEndian(index, NativeLong.SIZE, msb), unsigned);
	}

	private int setNlong(int index, NativeLong value, boolean msb) {
		return setEndian(index, NativeLong.SIZE, value.longValue(), msb);
	}

	private long peer() {
		return PointerUtil.peer(p) + offset;
	}
	
	private int offset(int index) {
		return this.offset + index;
	}
}
