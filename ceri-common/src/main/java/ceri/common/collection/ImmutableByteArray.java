package ceri.common.collection;

import static ceri.common.collection.ArrayUtil.EMPTY_BYTE;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;
import ceri.common.data.ByteUtil;
import ceri.common.function.ByteConsumer;
import ceri.common.text.ToStringHelper;
import ceri.common.util.HashCoder;

/**
 * Wrapper for byte array that does not allow modification. It allows slicing of views to promote
 * re-use rather than copying of bytes. Note that wrap() does not copy the original byte array, and
 * modifications of the original array will modify the wrapped array. If slicing large arrays,
 * copying may be better for long-term objects, as the reference to the original array is no longer
 * held.
 */
public class ImmutableByteArray {
	public static final ImmutableByteArray EMPTY = new ImmutableByteArray(EMPTY_BYTE, 0, 0);
	private final byte[] array;
	private final int offset;
	public final int length;

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
		return wrap(ByteUtil.bytes(array));
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

	public ImmutableByteArray append(ImmutableByteArray array) {
		return append(array, 0);
	}

	public ImmutableByteArray append(ImmutableByteArray array, int offset) {
		return append(array, offset, array.length - offset);
	}

	public ImmutableByteArray append(ImmutableByteArray array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		if (length == 0) return this;
		byte[] buffer = new byte[this.length + length];
		copyTo(buffer);
		array.copyTo(offset, buffer, this.length, length);
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
		if (offset == 0 && length == this.length) return this;
		byte[] buffer = new byte[length];
		slice(offset, Math.min(this.length - offset, length)).copyTo(buffer);
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

	public byte at(int index) {
		return array[offset + index];
	}

	public int indexOf(int... array) {
		return indexOf(ByteUtil.bytes(array));
	}

	public int indexOf(byte... array) {
		return indexOf(wrap(array));
	}

	public int indexOf(ImmutableByteArray array) {
		for (int i = 0; i <= length - array.length; i++)
			if (array.equals(this.array, offset + i)) return i;
		return -1;
	}

	public IntStream stream() {
		return ByteUtil.streamOf(array, offset, length);
	}

	public void forEach(ByteConsumer action) {
		Objects.requireNonNull(action);
		for (int i = 0; i < array.length; i++)
			action.accept(at(i));
	}

	public byte[] copy() {
		return copy(0);
	}

	public byte[] copy(int offset) {
		return copy(offset, length - offset);
	}

	public byte[] copy(int offset, int length) {
		ArrayUtil.validateSlice(this.length, offset, length);
		if (length == 0) return ArrayUtil.EMPTY_BYTE;
		return Arrays.copyOfRange(array, this.offset + offset, this.offset + offset + length);
	}

	public int copyTo(byte[] dest) {
		return copyTo(dest, 0);
	}

	public int copyTo(byte[] dest, int destOffset) {
		return copyTo(0, dest, destOffset, length);
	}

	public int copyTo(int srcOffset, byte[] dest, int destOffset) {
		return copyTo(srcOffset, dest, destOffset, length - srcOffset);
	}

	public int copyTo(int srcOffset, byte[] dest, int destOffset, int length) {
		ArrayUtil.validateSlice(this.length, srcOffset, length);
		if (length == 0) return destOffset;
		System.arraycopy(array, offset + srcOffset, dest, destOffset, length);
		return destOffset + length;
	}

	public void writeTo(OutputStream out) throws IOException {
		writeTo(out, 0);
	}

	public void writeTo(OutputStream out, int offset) throws IOException {
		writeTo(out, offset, length - offset);
	}

	public void writeTo(OutputStream out, int offset, int length) throws IOException {
		ArrayUtil.validateSlice(this.length, offset, length);
		if (length == 0) return;
		out.write(array, this.offset + offset, length);
	}

	@Override
	public int hashCode() {
		HashCoder hashCoder = HashCoder.create();
		for (int i = 0; i < length; i++)
			hashCoder.add(array[offset + i]);
		return hashCoder.hashCode();
	}

	public boolean equals(byte... array) {
		return equals(array, 0);
	}

	public boolean equals(byte[] array, int offset) {
		return equals(0, array, offset);
	}

	public boolean equals(byte[] array, int offset, int length) {
		return equals(0, array, offset, length);
	}

	public boolean equals(int srcOffset, byte[] array, int offset) {
		return equals(srcOffset, array, offset, length - srcOffset);
	}

	public boolean equals(int srcOffset, byte[] array, int offset, int length) {
		ArrayUtil.validateSlice(this.length, srcOffset, length);
		for (int i = 0; i < length; i++)
			if (at(srcOffset + i) != array[offset + i]) return false;
		return true;
	}

	public boolean equals(ImmutableByteArray array, int offset) {
		return equals(array, offset, length);
	}

	public boolean equals(ImmutableByteArray array, int offset, int length) {
		return equals(0, array, offset, length);
	}

	public boolean equals(int srcOffset, ImmutableByteArray array) {
		return equals(srcOffset, array, 0);
	}

	public boolean equals(int srcOffset, ImmutableByteArray array, int offset) {
		return equals(srcOffset, array, offset, length - srcOffset);
	}

	public boolean equals(int srcOffset, ImmutableByteArray array, int offset, int length) {
		ArrayUtil.validateSlice(this.length, srcOffset, length);
		for (int i = 0; i < length; i++)
			if (at(srcOffset + i) != array.at(offset + i)) return false;
		return true;
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
