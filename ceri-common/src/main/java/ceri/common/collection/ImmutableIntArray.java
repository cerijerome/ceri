package ceri.common.collection;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import ceri.common.text.ToStringHelper;
import ceri.common.util.HashCoder;

/**
 * Wrapper for int array that does not allow modification. It allows slicing of views to promote
 * re-use rather than copying of ints. Note that wrap() does not copy the original int array, and
 * modifications of the original array will modify the wrapped array. If slicing large arrays,
 * copying may be better for long-term objects, as the reference to the original array is no longer
 * held.
 * 
 * TODO: create IntProvider/IntReceiver and refactor this class.
 */

public class ImmutableIntArray {
	public static final ImmutableIntArray EMPTY = new ImmutableIntArray(ArrayUtil.EMPTY_INT, 0, 0);
	private final int[] array;
	private final int offset;
	public final int length;

	public static ImmutableIntArray copyOf(int[] array) {
		return copyOf(array, 0);
	}

	public static ImmutableIntArray copyOf(int[] array, int offset) {
		return copyOf(array, offset, array.length - offset);
	}

	public static ImmutableIntArray copyOf(int[] array, int offset, int length) {
		int[] newArray = Arrays.copyOfRange(array, offset, offset + length);
		return wrap(newArray);
	}

	public static ImmutableIntArray wrap(int... array) {
		return wrap(array, 0);
	}

	public static ImmutableIntArray wrap(int[] array, int offset) {
		return wrap(array, offset, array.length - offset);
	}

	public static ImmutableIntArray wrap(int[] array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		if (length == 0) return EMPTY;
		return new ImmutableIntArray(array, offset, length);
	}

	private ImmutableIntArray(int[] array, int offset, int length) {
		this.array = array;
		this.offset = offset;
		this.length = length;
	}

	public ImmutableIntArray append(ImmutableIntArray array) {
		return append(array, 0);
	}

	public ImmutableIntArray append(ImmutableIntArray array, int offset) {
		return append(array, offset, array.length - offset);
	}

	public ImmutableIntArray append(ImmutableIntArray array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		if (length == 0) return this;
		int[] buffer = new int[this.length + length];
		copyTo(buffer);
		array.copyTo(offset, buffer, this.length, length);
		return wrap(buffer);
	}

	public ImmutableIntArray append(int... array) {
		return append(array, 0);
	}

	public ImmutableIntArray append(int[] array, int offset) {
		return append(array, offset, array.length - offset);
	}

	public ImmutableIntArray append(int[] array, int offset, int length) {
		return append(wrap(array, offset, length));
	}

	public ImmutableIntArray slice(int offset) {
		return slice(offset, length - offset);
	}

	public ImmutableIntArray slice(int offset, int length) {
		ArrayUtil.validateSlice(this.length, offset, length);
		if (length == this.length) return this;
		return wrap(array, this.offset + offset, length);
	}

	public int at(int index) {
		return array[offset + index];
	}

	public IntStream stream() {
		return IntStream.range(0, array.length).map(i -> array[i]);
	}

	public void forEach(IntConsumer action) {
		Objects.requireNonNull(action);
		for (int i = 0; i < array.length; i++)
			action.accept(at(i));
	}

	public int[] copy() {
		return copy(0);
	}

	public int[] copy(int offset) {
		return copy(offset, length - offset);
	}

	public int[] copy(int offset, int length) {
		ArrayUtil.validateSlice(this.length, offset, length);
		if (length == 0) return ArrayUtil.EMPTY_INT;
		return Arrays.copyOfRange(array, this.offset + offset, this.offset + offset + length);
	}

	public int copyTo(int[] dest) {
		return copyTo(dest, 0);
	}

	public int copyTo(int[] dest, int destOffset) {
		return copyTo(0, dest, destOffset, length);
	}

	public int copyTo(int srcOffset, int[] dest, int destOffset) {
		return copyTo(srcOffset, dest, destOffset, length - srcOffset);
	}

	public int copyTo(int srcOffset, int[] dest, int destOffset, int length) {
		ArrayUtil.validateSlice(this.length, srcOffset, length);
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

	public boolean equals(int...array) {
		return equals(array, 0);
	}

	public boolean equals(int[] array, int offset) {
		return equals(0, array, offset);
	}

	public boolean equals(int[] array, int offset, int length) {
		return equals(0, array, offset, length);
	}

	public boolean equals(int srcOffset, int[] array, int offset) {
		return equals(srcOffset, array, offset, length - srcOffset);
	}

	public boolean equals(int srcOffset, int[] array, int offset, int length) {
		ArrayUtil.validateSlice(this.length, srcOffset, length);
		for (int i = 0; i < length; i++)
			if (at(srcOffset + i) != array[offset + i]) return false;
		return true;
	}

	public boolean equals(ImmutableIntArray array, int offset) {
		return equals(array, offset, length);
	}

	public boolean equals(ImmutableIntArray array, int offset, int length) {
		return equals(0, array, offset, length);
	}

	public boolean equals(int srcOffset, ImmutableIntArray array, int offset) {
		return equals(srcOffset, array, offset, length - srcOffset);
	}

	public boolean equals(int srcOffset, ImmutableIntArray array, int offset, int length) {
		ArrayUtil.validateSlice(this.length, srcOffset, length);
		for (int i = 0; i < length; i++)
			if (at(srcOffset + i) != array.at(offset + i)) return false;
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ImmutableIntArray)) return false;
		ImmutableIntArray other = (ImmutableIntArray) obj;
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
