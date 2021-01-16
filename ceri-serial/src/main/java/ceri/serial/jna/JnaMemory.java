package ceri.serial.jna;

import static ceri.common.data.ByteUtil.BIG_ENDIAN;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReceiver;
import ceri.common.util.BasicUtil;
import ceri.serial.clib.jna.CUtil;

/**
 * Fixed-size byte array with volatile values.
 */
public class JnaMemory implements ByteProvider, ByteReceiver {
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
	public boolean isEmpty() {
		return length() == 0;
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
	public JnaMemory slice(int index) {
		return slice(index, length() - index);
	}

	@Override
	public JnaMemory slice(int index, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		return JnaMemory.of(p, offset(index), length);
	}

	@Override
	public int copyTo(int index, byte[] array, int offset, int length) {
		return JnaUtil.read(p, offset(index), array, offset, length);
	}

	@Override
	public int copyTo(int index, ByteReceiver receiver, int offset, int length) {
		JnaMemory other = BasicUtil.castOrNull(JnaMemory.class, receiver);
		if (other != null) return other.copyFrom(offset, p, offset(index), length);
		byte[] bytes = copy(index, length);
		return receiver.copyFrom(offset, bytes);
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
		CUtil.memcpy(p, offset, this.p, offset(index), length);
		return offset + length;
	}

	@Override
	public int writeTo(int index, OutputStream out, int length) throws IOException {
		return ByteProvider.writeBufferTo(this, index, out, length);
	}

	/* ByteReceiver overrides and additions */

	@Override
	public int setByte(int index, int b) {
		p.setByte(offset(index++), (byte) b);
		return index;
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
		p.setMemory(offset(index), length, (byte) value);
		return index + length;
	}

	@Override
	public int copyFrom(int index, byte[] array, int offset, int length) {
		p.write(offset(index), array, offset, length);
		return index + length;
	}

	@Override
	public int copyFrom(int index, ByteProvider provider, int offset, int length) {
		JnaMemory other = BasicUtil.castOrNull(JnaMemory.class, provider);
		if (other != null) return other.copyTo(offset, p, offset(index), length);
		byte[] bytes = provider.copy(offset, length);
		return copyFrom(index, bytes);
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
		CUtil.memcpy(p, offset, this.p, offset(index), length);
		return index + length;
	}

	@Override
	public int readFrom(int index, InputStream in, int length) throws IOException {
		return ByteReceiver.readBufferFrom(this, index, in, length);
	}

	/* Other methods */

	/**
	 * Provides sequential access to memory.
	 */
	public JnaAccessor accessor(int index) {
		return accessor(index, length() - index);
	}

	/**
	 * Provides sequential access to memory.
	 */
	public JnaAccessor accessor(int index, int length) {
		return new JnaAccessor(this, index, length);
	}

	/* Object overrides */

	@Override
	public String toString() {
		return String.format("%s@0x%s+%d%s", getClass().getSimpleName(),
			Long.toHexString(Pointer.nativeValue(p) + offset), length, ByteProvider.toHex(this));
	}

	/* Support methods */

	private NativeLong getNlong(int index, boolean msb, boolean unsigned) {
		return new NativeLong(getEndian(index, NativeLong.SIZE, msb), unsigned);
	}

	private int setNlong(int index, NativeLong value, boolean msb) {
		return setEndian(index, NativeLong.SIZE, value.longValue(), msb);
	}

	private int offset(int index) {
		return this.offset + index;
	}

}
