package ceri.common.data;

import static ceri.common.validation.ValidationUtil.validateRange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * ByteReader and Navigator wrapper for a ByteProvider. This provides sequential access to bytes,
 * and relative/absolute positioning for the next read.
 * <p/>
 * ByteReader interface is complemented with methods that use remaining bytes instead of given
 * length. Methods must not include an offset position, apart from offset(int). If an absolute
 * position is required, offset(int) should be called first.
 */
public class NavigableByteWriter implements Navigable, ByteWriter<NavigableByteWriter> {
	private final ByteReceiver data;
	private int offset = 0;
	private int mark = 0;

	public static NavigableByteWriter wrap(byte[] data) {
		return wrap(data, 0);
	}

	public static NavigableByteWriter wrap(byte[] data, int offset) {
		return wrap(data, offset, data.length - offset);
	}

	public static NavigableByteWriter wrap(byte[] data, int offset, int length) {
		return wrap(ByteArray.Mutable.wrap(data, offset, length));
	}

	public static NavigableByteWriter wrap(ByteReceiver receiver) {
		return new NavigableByteWriter(receiver);
	}

	public static NavigableByteWriter wrap(ByteReceiver receiver, int offset) {
		return wrap(receiver, offset, receiver.length() - offset);
	}

	public static NavigableByteWriter wrap(ByteReceiver receiver, int offset, int length) {
		return wrap(receiver.slice(offset, length));
	}

	private NavigableByteWriter(ByteReceiver data) {
		this.data = data;
	}

	/* Navigable overrides */

	@Override
	public int offset() {
		return offset;
	}

	@Override
	public NavigableByteWriter offset(int offset) {
		validateRange(offset, 0, length());
		this.offset = offset;
		return this;
	}

	@Override
	public int length() {
		return data.length();
	}

	@Override
	public NavigableByteWriter mark() {
		mark = offset;
		return this;
	}

	@Override
	public int marked() {
		return offset() - mark;
	}

	@Override
	public NavigableByteWriter reset() {
		return skip(-marked());
	}

	@Override
	public NavigableByteWriter skip(int length) {
		return offset(offset() + length);
	}

	/* ByteReader overrides and additions */

	@Override
	public NavigableByteWriter writeByte(int value) {
		return offset(data.setByte(offset(), value));
	}

	@Override
	public NavigableByteWriter writeEndian(long value, int size, boolean msb) {
		return offset(data.setEndian(offset(), size, value, msb));
	}

	@Override
	public NavigableByteWriter writeString(String s, Charset charset) {
		return offset(data.setString(offset(), s, charset));
	}

	/**
	 * Fill remaining bytes with same value.
	 */
	public NavigableByteWriter fill(int value) {
		return fill(remaining(), value);
	}

	@Override
	public NavigableByteWriter fill(int length, int value) {
		return offset(data.fill(offset(), length, value));
	}

	@Override
	public NavigableByteWriter writeFrom(byte[] array, int offset, int length) {
		return offset(data.copyFrom(offset(), array, offset, length));
	}

	@Override
	public NavigableByteWriter writeFrom(ByteProvider provider, int offset, int length) {
		return offset(data.copyFrom(offset(), provider, offset, length));
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
		int current = offset();
		offset(data.readFrom(current, in, length));
		return offset() - current;
	}

	/* Other methods */

	/**
	 * Creates a new reader for remaining bytes without incrementing the offset.
	 */
	public NavigableByteWriter slice() {
		return slice(remaining());
	}

	/**
	 * Creates a new reader for subsequent bytes without incrementing the offset. Use a negative
	 * length to look backwards, which may be useful for checksum calculations.
	 */
	public NavigableByteWriter slice(int length) {
		if (length >= 0) return wrap(data.slice(offset(), length));
		return wrap(data.slice(offset() + length, -length));
	}

}
