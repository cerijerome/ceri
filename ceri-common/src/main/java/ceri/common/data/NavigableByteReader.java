package ceri.common.data;

import static ceri.common.validation.ValidationUtil.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.stream.IntStream;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray.Immutable;

/**
 * {@link Navigable} and {@link ByteReader} wrapper for a {@link ByteProvider}. This provides
 * sequential access to bytes, and relative/absolute positioning for the next read.
 * <p/>
 * ByteReader interface is complemented with methods that use remaining bytes instead of given
 * length. Except for {@link #offset(int)}, methods do not include an offset position. Clients must
 * first call {@link #offset(int)} if an absolute position is required.
 */
@Deprecated
public class NavigableByteReader<T extends ByteProvider> implements ByteReader, Navigable {
	private final T provider;
	private final int off;	
	private final int len;
	private int index = 0;
	private int mark = 0;

	public static NavigableByteReader<Immutable> of(int... data) {
		return of(ArrayUtil.bytes(data));
	}

	public static NavigableByteReader<Immutable> of(byte[] data) {
		return of(data, 0);
	}

	public static NavigableByteReader<Immutable> of(byte[] data, int offset) {
		return of(data, offset, data.length - offset);
	}

	public static NavigableByteReader<Immutable> of(byte[] data, int offset, int length) {
		return of(ByteArray.Immutable.wrap(data), offset, length);
	}

	public static <T extends ByteProvider> NavigableByteReader<T> of(T provider) {
		return of(provider, 0);
	}

	public static <T extends ByteProvider> NavigableByteReader<T> of(T provider, int offset) {
		return of(provider, offset, provider.length() - offset);
	}

	public static <T extends ByteProvider> NavigableByteReader<T> of(T provider, int offset, int length) {
		return new NavigableByteReader<>(provider, offset, length);
	}

	private NavigableByteReader(T provider, int offset, int length) {
		this.provider = provider;
		this.off = offset;
		this.len = length;
	}

	/* Navigable overrides */

	@Override
	public int offset() {
		return index;
	}

	@Override
	public NavigableByteReader<T> offset(int index) {
		validateRange(index, 0, length());
		this.index = index;
		return this;
	}

	@Override
	public int length() {
		return this.len;
	}

	@Override
	public NavigableByteReader<T> mark() {
		mark = index;
		return this;
	}

	@Override
	public int marked() {
		return offset() - mark;
	}

	@Override
	public NavigableByteReader<T> reset() {
		return skip(-marked());
	}

	@Override
	public NavigableByteReader<T> skip(int length) {
		return offset(offset() + length);
	}

	/* ByteReader overrides and additions */

	@Override
	public byte readByte() {
		return provider.getByte(inc(1));
	}

	@Override
	public long readEndian(int size, boolean msb) {
		return provider.getEndian(inc(size), size, msb);
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
		return provider.getString(inc(length), length, charset);
	}

	/**
	 * Reads an array of the remaining bytes.
	 */
	public byte[] readBytes() {
		return readBytes(remaining());
	}

	@Override
	public byte[] readBytes(int length) {
		return provider.copy(inc(length), length);
	}

	@Override
	public int readInto(byte[] dest, int offset, int length) {
		return provider.copyTo(inc(length), dest, offset, length);
	}

	@Override
	public int readInto(ByteReceiver receiver, int offset, int length) {
		return provider.copyTo(inc(length), receiver, offset, length);
	}

	/**
	 * Writes bytes to the output stream, and returns the number of bytes transferred.
	 */
	public int transferTo(OutputStream out) throws IOException {
		return transferTo(out, remaining());
	}

	@Override
	public int transferTo(OutputStream out, int length) throws IOException {
		return provider.writeTo(inc(length), out, length);
	}

	/**
	 * Provides unsigned bytes as a stream.
	 */
	public IntStream ustream() {
		return ustream(remaining());
	}

	@Override
	public IntStream ustream(int length) {
		return provider.ustream(inc(length), length);
	}

	/* Other methods */

	public T provider() {
		return provider;
	}
	
	/**
	 * Creates a new reader for remaining bytes without incrementing the offset.
	 */
	public NavigableByteReader<T> slice() {
		return slice(remaining());
	}

	/**
	 * Creates a new reader for subsequent bytes without incrementing the offset. Use a negative
	 * length to look backwards, which may be useful for checksum calculations.
	 */
	public NavigableByteReader<T> slice(int length) {
		int index = length < 0 ? offset() + length : offset();
		length = Math.abs(length);
		ArrayUtil.validateSlice(length(), index, length);
		return of(provider, off + index, length);
	}

	private int inc(int length) {
		int position = off + index;
		skip(length);
		return position;
	}
	
}
