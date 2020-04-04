package ceri.common.data;

import static ceri.common.validation.ValidationUtil.validateRange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.stream.IntStream;
import ceri.common.collection.ArrayUtil;

/**
 * {@link Navigable} and {@link ByteReader} wrapper for a {@link ByteProvider}. This provides
 * sequential access to bytes, and relative/absolute positioning for the next read.
 * <p/>
 * ByteReader interface is complemented with methods that use remaining bytes instead of given
 * length. Except for {@link #offset(int)}, methods do not include an offset position. Clients must
 * first call {@link #offset(int)} if an absolute position is required.
 */
public class NavigableByteReader implements ByteReader, Navigable {
	private final ByteProvider data;
	private int offset = 0;
	private int mark = 0;

	public static NavigableByteReader of(int... data) {
		return of(ArrayUtil.bytes(data));
	}

	public static NavigableByteReader of(byte[] data) {
		return of(data, 0);
	}

	public static NavigableByteReader of(byte[] data, int offset) {
		return of(data, offset, data.length - offset);
	}

	public static NavigableByteReader of(byte[] data, int offset, int length) {
		return of(ByteArray.Immutable.wrap(data, offset, length));
	}

	public static NavigableByteReader of(ByteProvider provider) {
		return new NavigableByteReader(provider);
	}

	public static NavigableByteReader of(ByteProvider provider, int offset) {
		return of(provider, offset, provider.length() - offset);
	}

	public static NavigableByteReader of(ByteProvider provider, int offset, int length) {
		return of(provider.slice(offset, length));
	}

	private NavigableByteReader(ByteProvider data) {
		this.data = data;
	}

	/* Navigable overrides */

	@Override
	public int offset() {
		return offset;
	}

	@Override
	public NavigableByteReader offset(int offset) {
		validateRange(offset, 0, length());
		this.offset = offset;
		return this;
	}

	@Override
	public int length() {
		return data.length();
	}

	@Override
	public NavigableByteReader mark() {
		mark = offset;
		return this;
	}

	@Override
	public int marked() {
		return offset() - mark;
	}

	@Override
	public NavigableByteReader reset() {
		return skip(-marked());
	}

	@Override
	public NavigableByteReader skip(int length) {
		return offset(offset() + length);
	}

	/* ByteReader overrides and additions */

	@Override
	public byte readByte() {
		return data.getByte(inc(1));
	}

	@Override
	public long readEndian(int size, boolean msb) {
		return data.getEndian(inc(size), size, msb);
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
		return data.getString(inc(length), length, charset);
	}

	/**
	 * Reads an array of the remaining bytes.
	 */
	public byte[] readBytes() {
		return readBytes(remaining());
	}

	@Override
	public byte[] readBytes(int length) {
		return data.copy(inc(length), length);
	}

	@Override
	public int readInto(byte[] dest, int offset, int length) {
		return data.copyTo(inc(length), dest, offset, length);
	}

	/**
	 * Writes bytes to the output stream, and returns the number of bytes transferred.
	 */
	public int transferTo(OutputStream out) throws IOException {
		return transferTo(out, remaining());
	}

	@Override
	public int transferTo(OutputStream out, int length) throws IOException {
		return data.writeTo(inc(length), out, length);
	}

	/**
	 * Provides unsigned bytes as a stream.
	 */
	public IntStream ustream() {
		return ustream(remaining());
	}

	@Override
	public IntStream ustream(int length) {
		return data.ustream(inc(length), length);
	}

	/* Other methods */

	/**
	 * Creates a new reader for remaining bytes without incrementing the offset.
	 */
	public NavigableByteReader slice() {
		return slice(remaining());
	}

	/**
	 * Creates a new reader for subsequent bytes without incrementing the offset. Use a negative
	 * length to look backwards, which may be useful for checksum calculations.
	 */
	public NavigableByteReader slice(int length) {
		if (length >= 0) return of(data.slice(offset(), length));
		return of(data.slice(offset() + length, -length));
	}

	/* Support methods */

	private int inc(int length) {
		int current = offset();
		offset(current + length);
		return current;
	}
}
