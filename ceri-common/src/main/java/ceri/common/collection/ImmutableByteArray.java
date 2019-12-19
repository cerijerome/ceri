package ceri.common.collection;

import static ceri.common.collection.ArrayUtil.EMPTY_BYTE;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.IntStream;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReceiver;
import ceri.common.data.ByteUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.HashCoder;

/**
 * Wrapper for byte array that does not allow modification. It allows slicing of views to promote
 * re-use rather than copying of bytes. Note that wrap() does not copy the original byte array, and
 * modifications of the original array will modify the wrapped array. If slicing large arrays,
 * copying may be better for long-term objects, as the reference to the original array is no longer
 * held.
 */
public class ImmutableByteArray implements ByteProvider {
	public static final ImmutableByteArray EMPTY = new ImmutableByteArray(EMPTY_BYTE, 0, 0);
	private final byte[] array;
	private final int offset;
	private final int length;

	public static ImmutableByteArray copyOf(byte[] array) {
		return copyOf(array, 0);
	}

	public static ImmutableByteArray copyOf(byte[] array, int offset) {
		return copyOf(array, offset, array.length - offset);
	}

	public static ImmutableByteArray copyOf(byte[] array, int offset, int length) {
		byte[] newArray = Arrays.copyOfRange(array, offset, offset + length);
		return wrap(newArray);
	}

	public static ImmutableByteArray wrap(IntStream stream) {
		return wrap(ByteUtil.toByteArray(stream));
	}

	public static ImmutableByteArray wrap(int... array) {
		return wrap(ArrayUtil.bytes(array));
	}

	public static ImmutableByteArray wrap(byte... array) {
		return wrap(array, 0);
	}

	public static ImmutableByteArray wrap(byte[] array, int offset) {
		return wrap(array, offset, array.length - offset);
	}

	public static ImmutableByteArray wrap(byte[] array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		if (length == 0) return EMPTY;
		return new ImmutableByteArray(array, offset, length);
	}

	private ImmutableByteArray(byte[] array, int offset, int length) {
		this.array = array;
		this.offset = offset;
		this.length = length;
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public byte get(int index) {
		return array[offset + index];
	}

	@Override
	public int copyTo(int srcOffset, ByteReceiver dest, int destOffset, int length) {
		ArrayUtil.validateSlice(length(), srcOffset, length);
		ArrayUtil.validateSlice(dest.length(), destOffset, length);
		dest.copyFrom(destOffset, array, this.offset + srcOffset, length);
		return destOffset + length;
	}

	@Override
	public void writeTo(OutputStream out, int offset, int length) throws IOException {
		ArrayUtil.validateSlice(length(), offset, length);
		if (length > 0) out.write(array, this.offset + offset, length);
	}

	public ImmutableByteArray append(ByteProvider array) {
		return append(array, 0);
	}

	public ImmutableByteArray append(ByteProvider array, int offset) {
		return append(array, offset, array.length() - offset);
	}

	public ImmutableByteArray append(ByteProvider array, int offset, int length) {
		ArrayUtil.validateSlice(array.length(), offset, length);
		if (length == 0) return this;
		byte[] buffer = new byte[length() + length];
		copyTo(buffer);
		array.copyTo(offset, buffer, length(), length);
		return wrap(buffer);
	}

	public ImmutableByteArray append(byte... array) {
		return append(array, 0);
	}

	public ImmutableByteArray append(byte[] array, int offset) {
		return append(array, offset, array.length - offset);
	}

	public ImmutableByteArray append(byte[] array, int offset, int length) {
		return append(wrap(array, offset, length));
	}

	public ImmutableByteArray resize(int length) {
		return resize(0, length);
	}

	public ImmutableByteArray resize(int offset, int length) {
		if (length == 0) return ImmutableByteArray.EMPTY;
		if (offset == 0 && length == length()) return this;
		byte[] buffer = new byte[length];
		slice(offset, Math.min(length() - offset, length)).copyTo(buffer);
		return wrap(buffer);
	}

	public ImmutableByteArray slice(int offset) {
		return slice(offset, length - offset);
	}

	public ImmutableByteArray slice(int offset, int length) {
		ArrayUtil.validateSlice(this.length, offset, length);
		if (length == this.length) return this;
		return wrap(array, this.offset + offset, length);
	}

	public String asString() {
		return asString(Charset.defaultCharset());
	}

	public String asString(Charset charset) {
		return new String(array, offset, length, charset);
	}

	@Override
	public int hashCode() {
		HashCoder hashCoder = HashCoder.create();
		for (int i = 0; i < length; i++)
			hashCoder.add(array[offset + i]);
		return hashCoder.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ImmutableByteArray)) return false;
		ImmutableByteArray other = (ImmutableByteArray) obj;
		if (length != other.length) return false;
		for (int i = 0; i < length; i++)
			if (array[offset + i] != other.array[other.offset + i]) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, length).toString();
	}

}
