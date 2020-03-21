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
 * Wrapper for a byte array that does not allow modification. It allows slicing of views to promote
 * re-use rather than copying of bytes. Note that wrap() does not copy the original byte array, and
 * modifications of the original array will modify the wrapped array. If slicing large arrays,
 * copying may be better for long-term objects, as the reference to the original array is no longer
 * held.
 * <p>
 * Internally, offset and length mark the permitted access window of the array. Bytes outside this
 * window must not be accessed or copied.
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
		return wrap(ByteUtil.toBytes(stream));
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

	public static ImmutableByteArray from(ByteProvider provider) {
		return from(provider, 0);
	}
	
	public static ImmutableByteArray from(ByteProvider provider, int offset) {
		return from(provider, offset, provider.length() - offset);
	}
	
	public static ImmutableByteArray from(ByteProvider provider, int offset, int length) {
		if (provider instanceof ImmutableByteArray)
			return ((ImmutableByteArray)provider).slice(offset, length);
		return wrap(provider.copy(offset, length));
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
	public byte getByte(int index) {
		return array[offset(index)];
	}

	@Override
	public long getEndian(int index, int size, boolean msb) {
		return msb ? ByteUtil.fromMsb(array, offset(index), size) :
			ByteUtil.fromLsb(array, offset(index), size);
	}

	@Override
	public String getString(int offset, int length, Charset charset) {
		validateSlice(offset, length);
		return new String(array, offset(offset), length, charset);
	}

	@Override
	public int copyTo(int srcOffset, byte[] dest, int destOffset, int length) {
		validateSlice(srcOffset, length);
		ArrayUtil.validateSlice(dest.length, destOffset, length);
		System.arraycopy(array, offset(srcOffset), dest, destOffset, length);
		return destOffset + length;
	}

	@Override
	public int copyTo(int srcOffset, ByteReceiver dest, int destOffset, int length) {
		return dest.copyFrom(destOffset, array, offset(srcOffset), length);
	}

	@Override
	public int writeTo(int offset, OutputStream out, int length) throws IOException {
		validateSlice(offset, length);
		if (length > 0) out.write(array, offset(offset), length);
		return offset + length;
	}

	@Override
	public boolean matches(int srcOffset, byte[] array, int offset, int length) {
		validateSlice(srcOffset, length);
		ArrayUtil.validateSlice(array.length, offset, length);
		return Arrays.equals(this.array, offset(srcOffset), offset(srcOffset + length), array,
			offset, offset + length);
	}

	@Override
	public boolean matches(int srcOffset, ByteProvider provider, int offset, int length) {
		validateSlice(srcOffset, length);
		return provider.matches(offset, array, offset(srcOffset), length);
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

	/**
	 * Resizes the array view. If any part is outside the range, a new array is created and the
	 * overlap copied into it.
	 */
	public ImmutableByteArray resize(int length) {
		return resize(0, length);
	}

	/**
	 * Resizes the array view. Use a negative length to right-justify the view. If any part is
	 * outside the range, a new array is created and the overlap copied into it.
	 */
	public ImmutableByteArray resize(int offset, int length) {
		if (length == 0) return ImmutableByteArray.EMPTY;
		if (length < 0) return resize(offset + length, -length);
		if (isValidSlice(offset, length)) return wrap(array, offset(offset), length);
		byte[] array = new byte[length];
		if (offset + length <= 0 || offset >= length()) return wrap(array);
		int copyFrom = Math.max(0, offset);
		int copyLen = Math.min(length(), offset + length) - copyFrom;
		int copyTo = copyFrom - offset;
		copyTo(copyFrom, array, copyTo, copyLen);
		return wrap(array);
	}

	/**
	 * Create a new view of the array. Use a negative length to right-justify the view.
	 */
	@Override
	public ImmutableByteArray slice(int offset) {
		return slice(offset, length() - offset);
	}
	
	/**
	 * Create a new view of the array. Use a negative length to right-justify the view.
	 */
	@Override
	public ImmutableByteArray slice(int offset, int length) {
		if (length == 0) return ImmutableByteArray.EMPTY;
		if (length < 0) return slice(offset + length, -length);
		validateSlice(offset, length);
		if (offset == 0 && length == length()) return this;
		return wrap(array, offset(offset), length);
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
		return Arrays.equals(array, offset, offset + length, other.array, other.offset,
			other.offset + length);
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, length).toString();
	}

	private boolean isValidSlice(int offset, int length) {
		return ArrayUtil.isValidSlice(this.length, offset, length);
	}

	private void validateSlice(int offset, int length) {
		ArrayUtil.validateSlice(this.length, offset, length);
	}

	private int offset(int offset) {
		return this.offset + offset;
	}
}
