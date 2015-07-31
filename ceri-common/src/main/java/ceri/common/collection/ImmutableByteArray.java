package ceri.common.collection;

import java.util.Arrays;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;

public class ImmutableByteArray {
	public static final ImmutableByteArray EMPTY = wrap(ArrayUtil.EMPTY_BYTE);
	private final byte[] array;
	private final int offset;
	public final int length;

	public static ImmutableByteArray copyOf(byte[] array) {
		return wrap(array.clone());
	}

	public static ImmutableByteArray copyOf(byte[] array, int offset, int length) {
		byte[] newArray = Arrays.copyOfRange(array, offset, length);
		return wrap(newArray);
	}

	public static ImmutableByteArray wrap(byte... array) {
		return wrap(array, 0, array.length);
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

	public ImmutableByteArray slice(int offset, int length) {
		validate(this.length, offset, length);
		if (offset == 0 && length == this.length) return this;
		return wrap(array, this.offset + offset, length);
	}

	public byte[] copy() {
		if (length == 0) return ArrayUtil.EMPTY_BYTE;
		if (offset == 0 && length == array.length) return array.clone();
		return Arrays.copyOfRange(array, offset, length);
	}

	public int copyTo(byte[] dest, int destOffset) {
		return copyTo(0, dest, destOffset, length);
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
		if (offset > sliceLen) throw new IndexOutOfBoundsException("Offset cannot be larger than " +
			sliceLen + ": " + offset);
		if (offset + length > sliceLen) throw new IndexOutOfBoundsException(
			"Length cannot be larger than " + (sliceLen - offset) + ": " + length);
	}

}
