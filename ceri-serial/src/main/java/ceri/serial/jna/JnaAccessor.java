package ceri.serial.jna;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.stream.IntStream;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReader;
import ceri.common.data.ByteReceiver;
import ceri.common.data.Navigator;

/**
 * {@link Navigator} and {@link ByteReader} wrapper for a {@link ByteProvider}. This provides
 * sequential access to bytes, and relative/absolute positioning for the next read.
 * <p/>
 * ByteReader interface is complemented with methods that use remaining bytes instead of given
 * length. Except for {@link #offset(int)}, methods do not include an offset position. Clients must
 * first call {@link #offset(int)} if an absolute position is required.
 */
public class JnaAccessor extends Navigator<JnaAccessor>
	implements JnaReader, JnaWriter<JnaAccessor> {
	private final JnaMemory memory;
	private final int start;

	JnaAccessor(JnaMemory memory, int offset, int length) {
		super(length);
		this.memory = memory;
		this.start = offset;
	}

	/* ByteReader overrides and additions */

	@Override
	public byte readByte() {
		return memory.getByte(inc(1));
	}

	@Override
	public long readEndian(int size, boolean msb) {
		return memory.getEndian(inc(size), size, msb);
	}

	/**
	 * Returns the string from ISO-Latin-1 bytes.
	 */
	public String readAscii() {
		return readAscii(remaining());
	}

	/**
	 * Returns the string from UTF-8 bytes.
	 */
	public String readUtf8() {
		return readUtf8(remaining());
	}

	/**
	 * Returns the string from default character-set bytes.
	 */
	public String readString() {
		return readString(remaining());
	}

	/**
	 * Returns the string from character-set encoded bytes.
	 */
	public String readString(Charset charset) {
		return readString(remaining(), charset);
	}

	@Override
	public String readString(int length, Charset charset) {
		return memory.getString(inc(length), length, charset);
	}

	/**
	 * Reads an array of the remaining bytes.
	 */
	public byte[] readBytes() {
		return readBytes(remaining());
	}

	@Override
	public int readInto(byte[] dest, int offset, int length) {
		return memory.copyTo(inc(length), dest, offset, length);
	}

	@Override
	public int readInto(ByteReceiver receiver, int offset, int length) {
		return memory.copyTo(inc(length), receiver, offset, length);
	}

	@Override
	public int readInto(Pointer p, int offset, int length) {
		return memory.copyTo(inc(length), p, offset, length);
	}

	/**
	 * Writes bytes to the output stream, and returns the number of bytes transferred.
	 */
	public int transferTo(OutputStream out) throws IOException {
		return transferTo(out, remaining());
	}

	@Override
	public int transferTo(OutputStream out, int length) throws IOException {
		return memory.writeTo(inc(length), out, length);
	}

	/**
	 * Provides unsigned bytes as a stream.
	 */
	public IntStream ustream() {
		return ustream(remaining());
	}

	@Override
	public IntStream ustream(int length) {
		return memory.ustream(inc(length), length);
	}

	/* ByteWriter overrides and additions */

	@Override
	public JnaAccessor writeByte(int value) {
		return position(memory.setByte(position(), value));
	}

	@Override
	public JnaAccessor writeEndian(long value, int size, boolean msb) {
		return position(memory.setEndian(position(), size, value, msb));
	}

	@Override
	public JnaAccessor writeString(String s, Charset charset) {
		return position(memory.setString(position(), s, charset));
	}

	/**
	 * Fill remaining bytes with same value.
	 */
	public JnaAccessor fill(int value) {
		return fill(remaining(), value);
	}

	@Override
	public JnaAccessor fill(int length, int value) {
		return position(memory.fill(position(), length, value));
	}

	@Override
	public JnaAccessor writeFrom(byte[] array, int offset, int length) {
		return position(memory.copyFrom(position(), array, offset, length));
	}

	@Override
	public JnaAccessor writeFrom(ByteProvider provider, int offset, int length) {
		return position(memory.copyFrom(position(), provider, offset, length));
	}

	@Override
	public JnaAccessor writeFrom(Pointer p, int offset, int length) {
		return position(memory.copyFrom(position(), p, offset, length));
	}

	/**
	 * Writes bytes from the input stream to remaining space, and returns the number of bytes
	 * transferred.
	 */
	public int transferFrom(InputStream in) throws IOException {
		return transferFrom(in, remaining());
	}

	@Override
	public int transferFrom(InputStream in, int length) throws IOException {
		int current = position();
		position(memory.readFrom(current, in, length));
		return position() - current;
	}

	/* Other methods */

	/**
	 * Creates a new reader for remaining bytes without incrementing the offset.
	 */
	public JnaAccessor slice() {
		return slice(remaining());
	}

	/**
	 * Creates a new reader for subsequent bytes without incrementing the offset. Use a negative
	 * length to look backwards, which may be useful for checksum calculations.
	 */
	public JnaAccessor slice(int length) {
		int offset = length < 0 ? offset() + length : offset();
		length = Math.abs(length);
		ArrayUtil.validateSlice(length(), offset, length);
		return new JnaAccessor(memory, start + offset, length);
	}

	/* Support methods */

	/**
	 * Returns the current position and increments the offset by length.
	 */
	private int inc(int length) {
		int position = position();
		skip(length);
		return position;
	}

	/**
	 * The actual position within the byte receiver.
	 */
	private int position() {
		return start + offset();
	}

	/**
	 * Set the offset from receiver actual position.
	 */
	private JnaAccessor position(int position) {
		return offset(position - start);
	}

}
