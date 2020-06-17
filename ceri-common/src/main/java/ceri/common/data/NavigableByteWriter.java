package ceri.common.data;

import static ceri.common.validation.ValidationUtil.validateRange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray.Mutable;

/**
 * {@link Navigable} and {@link ByteWriter} wrapper for a {@link ByteReceiver}. This provides
 * sequential writing of bytes, and relative/absolute positioning for the next write. The type T
 * allows typed access to the ByteReceiver.
 * <p/>
 * ByteWriter interface is complemented with methods that use remaining bytes instead of given
 * length. Except for {@link #offset(int)}, methods do not include an offset position. Clients must
 * first call {@link #offset(int)} if an absolute position is required.
 */
@Deprecated
public class NavigableByteWriter<T extends ByteReceiver>
	implements Navigable, ByteWriter<NavigableByteWriter<T>> {
	private final T receiver;
	private final int offset;
	private final int length;
	private int index = 0;
	private int mark = 0;

	public static NavigableByteWriter<Mutable> of(int size) {
		return of(Mutable.of(size));
	}

	public static NavigableByteWriter<Mutable> of(byte[] data) {
		return of(data, 0);
	}

	public static NavigableByteWriter<Mutable> of(byte[] data, int offset) {
		return of(data, offset, data.length - offset);
	}

	public static NavigableByteWriter<Mutable> of(byte[] data, int offset, int length) {
		return of(Mutable.wrap(data), offset, length);
	}

	public static <T extends ByteReceiver> NavigableByteWriter<T> of(T receiver) {
		return of(receiver, 0);
	}

	public static <T extends ByteReceiver> NavigableByteWriter<T> of(T receiver, int offset) {
		return of(receiver, offset, receiver.length() - offset);
	}

	public static <T extends ByteReceiver> NavigableByteWriter<T> of(T receiver, int offset,
		int length) {
		ArrayUtil.validateSlice(receiver.length(), offset, length);
		return new NavigableByteWriter<>(receiver, offset, length);
	}

	private NavigableByteWriter(T receiver, int offset, int length) {
		this.receiver = receiver;
		this.offset = offset;
		this.length = length;
	}

	/* Navigable overrides */

	@Override
	public int offset() {
		return index;
	}

	@Override
	public NavigableByteWriter<T> offset(int index) {
		validateRange(index, 0, length());
		this.index = index;
		return this;
	}

	@Override
	public int length() {
		return this.length;
	}

	@Override
	public NavigableByteWriter<T> mark() {
		mark = index;
		return this;
	}

	@Override
	public int marked() {
		return offset() - mark;
	}

	@Override
	public NavigableByteWriter<T> reset() {
		return skip(-marked());
	}

	@Override
	public NavigableByteWriter<T> skip(int length) {
		return offset(offset() + length);
	}

	/* ByteWriter overrides and additions */

	@Override
	public NavigableByteWriter<T> writeByte(int value) {
		return position(receiver.setByte(position(), value));
	}

	@Override
	public NavigableByteWriter<T> writeEndian(long value, int size, boolean msb) {
		return position(receiver.setEndian(position(), size, value, msb));
	}

	@Override
	public NavigableByteWriter<T> writeString(String s, Charset charset) {
		return position(receiver.setString(position(), s, charset));
	}

	/**
	 * Fill remaining bytes with same value.
	 */
	public NavigableByteWriter<T> fill(int value) {
		return fill(remaining(), value);
	}

	@Override
	public NavigableByteWriter<T> fill(int length, int value) {
		return position(receiver.fill(position(), length, value));
	}

	@Override
	public NavigableByteWriter<T> writeFrom(byte[] array, int offset, int length) {
		return position(receiver.copyFrom(position(), array, offset, length));
	}

	@Override
	public NavigableByteWriter<T> writeFrom(ByteProvider provider, int offset, int length) {
		return position(receiver.copyFrom(position(), provider, offset, length));
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
		position(receiver.readFrom(current, in, length));
		return position() - current;
	}

	/* Other methods */

	public T receiver() {
		return receiver;
	}

	/**
	 * Creates a new reader for remaining bytes without incrementing the offset.
	 */
	public NavigableByteWriter<T> slice() {
		return slice(remaining());
	}

	/**
	 * Creates a new reader for subsequent bytes without incrementing the offset. Use a negative
	 * length to look backwards, which may be useful for checksum calculations.
	 */
	public NavigableByteWriter<T> slice(int length) {
		int index = length < 0 ? offset() + length : offset();
		length = Math.abs(length);
		ArrayUtil.validateSlice(length(), index, length);
		return of(receiver, offset + index, length);
	}

	/**
	 * The actual position within the byte receiver.
	 */
	private int position() {
		return offset + index;
	}

	/**
	 * Set the index from receiver actual position.
	 */
	private NavigableByteWriter<T> position(int offset) {
		return offset(offset - this.offset);
	}

}
