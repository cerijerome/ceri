package ceri.common.collection;

import java.util.Arrays;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;

/**
 * Wrapper for byte array that does not allow modification. It allows slicing of views, to promote
 * re-use rather than copying of bytes. Note that wrap() does not copy the original byte array, and
 * modifications of the original array will modify the wrapped array. If slicing large arrays,
 * copying may be better for long-term objects, as the reference to the original array is kept.
 */
public class ImmutableByteArray {
	public static final ImmutableByteArray EMPTY = new ImmutableByteArray(ArrayUtil.EMPTY_BYTE, 0,
		0);
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

	public static ImmutableByteArray wrap(byte... array) {
		return wrap(array, 0);
	}

	public static ImmutableByteArray wrap(byte[] array, int offset) {
		return wrap(array, offset, array.length - offset);
	}

	public static ImmutableByteArray wrap(byte[] array, int offset, int length) {
		validate(array.length, offset, length);
		if (length == 0) return EMPTY;
		return new ImmutableByteArray(array, offset, length);
	}

	private ImmutableByteArray(byte[] array, int offset, int length) {
		this.array = array;
		this.offset = offset;
		this.length = length;
	}

	public ImmutableByteArray slice(int offset) {
		return slice(offset, length - offset);
	}

	public ImmutableByteArray slice(int offset, int length) {
		validate(this.length, offset, length);
		if (length == this.length) return this;
		return wrap(array, this.offset + offset, length);
	}

	public byte at(int index) {
		return array[offset + index];
	}

	public byte[] copy() {
		return copy(0);
	}

	public byte[] copy(int offset) {
		return copy(offset, length - offset);
	}

	public byte[] copy(int offset, int length) {
		validate(this.length, offset, length);
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
		validate(this.length, srcOffset, length);
		if (length == 0) return destOffset;
		System.arraycopy(array, offset + srcOffset, dest, destOffset, length);
		return destOffset + length;
	}

	@Override
	public int hashCode() {
		HashCoder hashCoder = HashCoder.create();
		for (int i = 0; i < length; i++)
			hashCoder.add(array[offset + i]);
		return hashCoder.hashCode();
	}

	public boolean equals(byte[] array) {
		return equals(array, 0);
	}

	public boolean equals(byte[] array, int offset) {
		return equals(0, array, offset);
	}

	public boolean equals(int srcOffset, byte[] array, int offset) {
		return equals(srcOffset, array, offset, length);
	}

	public boolean equals(int srcOffset, byte[] array, int offset, int length) {
		validate(this.length, srcOffset, length);
		for (int i = 0; i < length; i++)
			if (at(srcOffset + i) != array[offset + i]) return false;
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

	private static void validate(int sliceLen, int offset, int length) {
		if (offset < 0 || offset > sliceLen) throw new IndexOutOfBoundsException(
			"Offset must be 0-" + sliceLen + ": " + offset);
		if (length < 0 || offset + length > sliceLen) throw new IndexOutOfBoundsException(
			"Length must be 0-" + (sliceLen - offset) + ": " + length);
	}

}
