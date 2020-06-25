package ceri.common.data.old;

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
@Deprecated
class ImmutableByteArray { //implements ByteProvider {
//	public static final ImmutableByteArray EMPTY = new ImmutableByteArray(EMPTY_BYTE, 0, 0);
//	private final byte[] array;
//	private final int offset;
//	private final int length;
//
//	public static ImmutableByteArray copyOf(byte[] array) {
//		return copyOf(array, 0);
//	}
//
//	public static ImmutableByteArray copyOf(byte[] array, int offset) {
//		return copyOf(array, offset, array.length - offset);
//	}
//
//	public static ImmutableByteArray copyOf(byte[] array, int offset, int length) {
//		byte[] newArray = Arrays.copyOfRange(array, offset, offset + length);
//		return wrap(newArray);
//	}
//
//	public static ImmutableByteArray wrap(IntStream stream) {
//		return wrap(ByteUtil.toBytes(stream));
//	}
//
//	public static ImmutableByteArray wrap(int... array) {
//		return wrap(ArrayUtil.bytes(array));
//	}
//
//	public static ImmutableByteArray wrap(byte... array) {
//		return wrap(array, 0);
//	}
//
//	public static ImmutableByteArray wrap(byte[] array, int offset) {
//		return wrap(array, offset, array.length - offset);
//	}
//
//	public static ImmutableByteArray wrap(byte[] array, int offset, int length) {
//		ArrayUtil.validateSlice(array.length, offset, length);
//		if (length == 0) return EMPTY;
//		return new ImmutableByteArray(array, offset, length);
//	}
//
////	public static ImmutableByteArray from(ByteProvider provider) {
////		return from(provider, 0);
////	}
////
////	public static ImmutableByteArray from(ByteProvider provider, int offset) {
////		return from(provider, offset, provider.length() - offset);
////	}
////
////	public static ImmutableByteArray from(ByteProvider provider, int offset, int length) {
////		if (provider instanceof ImmutableByteArray)
////			return ((ImmutableByteArray) provider).slice(offset, length);
////		return wrap(provider.copy(offset, length));
////	}
//
//	private ImmutableByteArray(byte[] array, int offset, int length) {
//		this.array = array;
//		this.offset = offset;
//		this.length = length;
//	}
//
//	/* ByteProvider overrides */
//	
//	@Override
//	public int length() {
//		return length;
//	}
//
//	@Override
//	public byte getByte(int index) {
//		return array[offset(index)];
//	}
//
//	@Override
//	public long getEndian(int index, int size, boolean msb) {
//		validateSlice(index, size);
//		return msb ? ByteUtil.fromMsb(array, offset(index), size) :
//			ByteUtil.fromLsb(array, offset(index), size);
//	}
//
//	@Override
//	public String getString(int index, int length, Charset charset) {
//		validateSlice(index, length);
//		return new String(array, offset(index), length, charset);
//	}
//
//	@Override
//	public ImmutableByteArray slice(int index) {
//		return slice(index, length() - index);
//	}
//
//	@Override
//	public ImmutableByteArray slice(int index, int length) {
//		if (length == 0) return ImmutableByteArray.EMPTY;
//		if (length < 0) return slice(index + length, -length);
//		validateSlice(index, length);
//		if (index == 0 && length == length()) return this;
//		return wrap(array, offset(index), length);
//	}
//
//	@Override
//	public byte[] copy(int index, int length) {
//		if (length == 0) return ArrayUtil.EMPTY_BYTE;
//		validateSlice(index, length);
//		return Arrays.copyOfRange(array, offset(index), offset(index + length));
//	}
//
//	@Override
//	public int copyTo(int index, byte[] dest, int offset, int length) {
//		validateSlice(index, length);
//		ArrayUtil.validateSlice(dest.length, offset, length);
//		System.arraycopy(array, offset(index), dest, offset, length);
//		return offset + length;
//	}
//
//	@Override
//	public int copyTo(int index, ByteReceiver receiver, int offset, int length) {
//		validateSlice(index, length);
//		receiver.copyFrom(offset, array, offset(index), length);
//		return index + length;
//	}
//
//	@Override
//	public int writeTo(int index, OutputStream out, int length) throws IOException {
//		validateSlice(index, length);
//		if (length > 0) out.write(array, offset(index), length);
//		return index + length;
//	}
//
//	@Override
//	public boolean matches(int index, byte[] array, int offset, int length) {
//		if (!isValidSlice(index, length)) return false;
//		if (!ArrayUtil.isValidSlice(array.length, offset, length)) return false;
//		return Arrays.equals(this.array, offset(index), offset(index + length), array, offset,
//			offset + length);
//	}
//
//	@Override
//	public boolean matches(int index, ByteProvider provider, int offset, int length) {
//		if (isValidSlice(index, length)) return false;
//		return provider.matches(offset, array, offset(index), length);
//	}
//
//	/* New functionality */
//	
//	public ImmutableByteArray append(byte... array) {
//		return append(array, 0);
//	}
//
//	public ImmutableByteArray append(byte[] array, int offset) {
//		return append(array, offset, array.length - offset);
//	}
//
//	public ImmutableByteArray append(byte[] array, int offset, int length) {
//		return append(wrap(array, offset, length));
//	}
//
//	public ImmutableByteArray append(ByteProvider array) {
//		return append(array, 0);
//	}
//
//	public ImmutableByteArray append(ByteProvider array, int offset) {
//		return append(array, offset, array.length() - offset);
//	}
//
//	public ImmutableByteArray append(ByteProvider array, int offset, int length) {
//		ArrayUtil.validateSlice(array.length(), offset, length);
//		if (length == 0) return this;
//		byte[] buffer = new byte[length() + length];
//		copyTo(buffer);
//		array.copyTo(offset, buffer, length(), length);
//		return wrap(buffer);
//	}
//
//	/**
//	 * Resizes the array view. If any part is outside the range, a new array is created and the
//	 * overlap copied into it.
//	 */
//	public ImmutableByteArray resize(int length) {
//		return resize(0, length);
//	}
//
//	/**
//	 * Resizes the array view. Use a negative length to right-justify the view. If any part is
//	 * outside the range, a new array is created and the overlap copied into it.
//	 */
//	public ImmutableByteArray resize(int index, int length) {
//		if (length == 0) return ImmutableByteArray.EMPTY;
//		if (length < 0) return resize(index + length, -length);
//		if (isValidSlice(index, length)) return slice(index, length);
//		byte[] array = new byte[length];
//		if (index + length <= 0 || index >= length()) return wrap(array);
//		int copyFrom = Math.max(0, index);
//		int copyLen = Math.min(length(), index + length) - copyFrom;
//		int copyTo = copyFrom - index;
//		copyTo(copyFrom, array, copyTo, copyLen);
//		return wrap(array);
//	}
//
//	@Override
//	public int hashCode() {
//		HashCoder hashCoder = HashCoder.create();
//		for (int i = 0; i < length; i++)
//			hashCoder.add(array[offset + i]);
//		return hashCoder.hashCode();
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj) return true;
//		if (!(obj instanceof ImmutableByteArray)) return false;
//		ImmutableByteArray other = (ImmutableByteArray) obj;
//		if (length != other.length) return false;
//		return Arrays.equals(array, offset, offset + length, other.array, other.offset,
//			other.offset + length);
//	}
//
//	@Override
//	public String toString() {
//		return ToStringHelper.createByClass(this, length).toString();
//	}
//
//	private boolean isValidSlice(int offset, int length) {
//		return ArrayUtil.isValidSlice(this.length, offset, length);
//	}
//
//	private void validateSlice(int offset, int length) {
//		ArrayUtil.validateSlice(this.length, offset, length);
//	}
//
//	private int offset(int offset) {
//		return this.offset + offset;
//	}
}
