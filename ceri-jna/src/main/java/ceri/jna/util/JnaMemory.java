package ceri.jna.util;

import static ceri.common.data.ByteUtil.IS_BIG_ENDIAN;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.data.ByteAccessor;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReceiver;
import ceri.common.validation.ValidationUtil;
import ceri.jna.type.CLong;
import ceri.jna.type.CUlong;
import ceri.jna.type.IntType;

/**
 * Byte accessor wrapper for memory. Wrapped length must be within int range.
 */
public class JnaMemory implements ByteAccessor {
	public static final JnaMemory EMPTY = JnaMemory.of(null, 0, 0);
	private final Pointer p;
	private final long offset;
	private final int length;

	public static JnaMemory of(Memory m) {
		return of(m, 0);
	}

	public static JnaMemory of(Memory m, long offset) {
		return of(m, offset, Math.toIntExact(JnaUtil.size(m) - offset));
	}

	public static JnaMemory of(Pointer p, long offset, int length) {
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
		public CLong readCLong() {
			return new CLong(readEndian(CLong.SIZE, IS_BIG_ENDIAN));
		}

		/**
		 * Returns the value from big-endian bytes.
		 */
		public CLong readCLongMsb() {
			return new CLong(readEndian(CLong.SIZE, true));
		}

		/**
		 * Returns the value from little-endian bytes.
		 */
		public CLong readCLongLsb() {
			return new CLong(readEndian(CLong.SIZE, false));
		}

		/**
		 * Returns the unsigned value from native-order bytes.
		 */
		public CUlong readCUlong() {
			return new CUlong(readEndian(CUlong.SIZE, IS_BIG_ENDIAN));
		}

		/**
		 * Returns the unsigned value from big-endian bytes.
		 */
		public CUlong readCUlongMsb() {
			return new CUlong(readEndian(CUlong.SIZE, true));
		}

		/**
		 * Returns the unsigned value from little-endian bytes.
		 */
		public CUlong readCUlongLsb() {
			return new CUlong(readEndian(CUlong.SIZE, false));
		}

		/**
		 * Returns the value populated from native-order bytes.
		 */
		public <T extends IntType<T>> T readInto(T t) {
			return IntType.set(t, readEndian(t.size, IS_BIG_ENDIAN));
		}

		/**
		 * Returns the value populated from big-endian bytes.
		 */
		public <T extends IntType<T>> T readIntoMsb(T t) {
			return IntType.set(t, readEndian(t.size, true));
		}

		/**
		 * Returns the value populated from little-endian bytes.
		 */
		public <T extends IntType<T>> T readIntoLsb(T t) {
			return IntType.set(t, readEndian(t.size, false));
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
		public int readInto(Memory m, long offset) {
			return readInto(m, offset, Math.toIntExact(JnaUtil.size(m) - offset));
		}

		/**
		 * Reads bytes into the memory pointer. Returns the destination offset after reading.
		 * Default implementation reads one byte at a time; efficiency may be improved by
		 * overriding.
		 */
		public int readInto(Pointer p, long offset, int length) {
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
			ValidationUtil.validateSlice(length(), offset(), length);
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
		public Writer write(IntType<?> value) {
			return writeEndian(value.longValue(), value.size, IS_BIG_ENDIAN);
		}

		/**
		 * Writes big-endian bytes.
		 */
		public Writer writeMsb(IntType<?> value) {
			return writeEndian(value.longValue(), value.size, true);
		}

		/**
		 * Writes little-endian bytes.
		 */
		public Writer writeLsb(IntType<?> value) {
			return writeEndian(value.longValue(), value.size, false);
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
		public Writer writeFrom(Memory m, long offset) {
			return writeFrom(m, offset, Math.toIntExact(JnaUtil.size(m) - offset));
		}

		/**
		 * Writes bytes from the memory pointer. Default implementation writes one byte at a time;
		 * efficiency may be improved by overriding.
		 */
		public Writer writeFrom(Pointer p, long offset, int length) {
			return position(m.copyFrom(position(), p, offset, length));
		}

		@Override
		public Writer slice() {
			return slice(remaining());
		}

		@Override
		public Writer slice(int length) {
			ValidationUtil.validateSlice(length(), offset(), length);
			return new Writer(m, position(), length);
		}
	}

	private JnaMemory(Pointer p, long offset, int length) {
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
		ValidationUtil.validateIndex(length(), index);
		return p.getByte(offset(index));
	}

	/**
	 * Returns the value from native-order bytes at given index.
	 */
	public CLong getCLong(int index) {
		return getCLong(index, IS_BIG_ENDIAN);
	}

	/**
	 * Returns the value from big-endian bytes at given index.
	 */
	public CLong getCLongMsb(int index) {
		return getCLong(index, true);
	}

	/**
	 * Returns the value from little-endian bytes at given index.
	 */
	public CLong getCLongLsb(int index) {
		return getCLong(index, false);
	}

	/**
	 * Returns the unsigned value from native-order bytes at given index.
	 */
	public CUlong getCUlong(int index) {
		return getCUlong(index, IS_BIG_ENDIAN);
	}

	/**
	 * Returns the unsigned value from big-endian bytes at given index.
	 */
	public CUlong getCUlongMsb(int index) {
		return getCUlong(index, true);
	}

	/**
	 * Returns the unsigned value from little-endian bytes at given index.
	 */
	public CUlong getCUlongLsb(int index) {
		return getCUlong(index, false);
	}

	/**
	 * Returns the value populated from native-order bytes at given index.
	 */
	public <T extends IntType<T>> T getFrom(int index, T t) {
		return getIntType(index, t, IS_BIG_ENDIAN);
	}

	/**
	 * Returns the value populated from big-endian bytes at given index.
	 */
	public <T extends IntType<T>> T getFromMsb(int index, T t) {
		return getIntType(index, t, true);
	}

	/**
	 * Returns the value populated from little-endian bytes at given index.
	 */
	public <T extends IntType<T>> T getFromLsb(int index, T t) {
		return getIntType(index, t, false);
	}

	@Override
	public int copyTo(int index, byte[] array, int offset, int length) {
		return JnaUtil.read(p, offset(index), array, offset, length);
	}

	@Override
	public int copyTo(int index, ByteReceiver receiver, int offset, int length) {
		ValidationUtil.validateSlice(length(), index, length);
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
	 * Fails if the memory size to copy from is larger than int.
	 */
	public int copyTo(int index, Memory m, long offset) {
		return copyTo(index, m, offset, Math.toIntExact(JnaUtil.size(m) - offset));
	}

	/**
	 * Copies bytes from memory at index to the memory pointer. Returns the index after copying.
	 */
	public int copyTo(int index, Pointer p, long offset, int length) {
		ValidationUtil.validateSlice(length(), index, length);
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
		ValidationUtil.validateSlice(length(), index, length);
		return new Reader(this, index, length);
	}

	/* ByteReceiver overrides and additions */

	@Override
	public int setByte(int index, int b) {
		ValidationUtil.validateIndex(length(), index);
		p.setByte(offset(index), (byte) b);
		return index + 1;
	}

	/**
	 * Sets the value in native-order bytes at the index. Returns the index after the written bytes.
	 */
	public int set(int index, IntType<?> value) {
		return set(index, value, IS_BIG_ENDIAN);
	}

	/**
	 * Sets the value in big-endian bytes at the index. Returns the index after the written bytes.
	 */
	public int setMsb(int index, IntType<?> value) {
		return set(index, value, true);
	}

	/**
	 * Sets the value in little-endian bytes at the index. Returns the index after the written
	 * bytes.
	 */
	public int setLsb(int index, IntType<?> value) {
		return set(index, value, false);
	}

	@Override
	public int fill(int index, int length, int value) {
		ValidationUtil.validateSlice(length(), index, length);
		JnaUtil.fill(p, offset(index), length, value);
		return index + length;
	}

	@Override
	public int copyFrom(int index, byte[] array, int offset, int length) {
		ValidationUtil.validateSlice(length(), index, length);
		p.write(offset(index), array, offset, length);
		return index + length;
	}

	@Override
	public int copyFrom(int index, ByteProvider provider, int offset, int length) {
		ValidationUtil.validateSlice(length(), index, length);
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
	 * bytes. Fails if the memory size to copy from is larger than int.
	 */
	public int copyFrom(int index, Memory m, long offset) {
		return copyFrom(index, m, offset, Math.toIntExact(JnaUtil.size(m) - offset));
	}

	/**
	 * Copies bytes from the memory pointer to memory at index. Returns the index after the written
	 * bytes.
	 */
	public int copyFrom(int index, Pointer p, long offset, int length) {
		ValidationUtil.validateSlice(length(), index, length);
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
		ValidationUtil.validateSlice(length(), index, length);
		return new Writer(this, index, length);
	}

	/* Other methods */

	@Override
	public JnaMemory slice(int index) {
		return slice(index, length() - index);
	}

	@Override
	public JnaMemory slice(int index, int length) {
		ValidationUtil.validateSlice(length(), index, length);
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
		return (obj instanceof JnaMemory other) && (peer() == other.peer())
			&& (length == other.length);
	}

	@Override
	public String toString() {
		return String.format("%s@%x%s", getClass().getSimpleName(), PointerUtil.peer(p) + offset,
			ByteProvider.toHex(this));
	}

	/* Support methods */

	private CLong getCLong(int index, boolean msb) {
		return new CLong(getEndian(index, CLong.SIZE, msb));
	}

	private CUlong getCUlong(int index, boolean msb) {
		return new CUlong(getEndian(index, CUlong.SIZE, msb));
	}

	private <T extends IntType<T>> T getIntType(int index, T t, boolean msb) {
		t.setValue(getEndian(index, t.size, msb));
		return t;
	}

	private int set(int index, IntType<?> value, boolean msb) {
		return setEndian(index, value.size, value.longValue(), msb);
	}

	private long peer() {
		return PointerUtil.peer(p) + offset;
	}

	private long offset(int index) {
		return this.offset + index;
	}
}
