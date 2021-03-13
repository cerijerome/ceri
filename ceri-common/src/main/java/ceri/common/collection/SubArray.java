package ceri.common.collection;

import java.util.Arrays;
import ceri.common.util.BasicUtil;

/**
 * Encapsulates a sub-array as a single object.
 */
public abstract class SubArray<T> {
	public final T array;
	public final int offset;
	public final int length;

	public static <T> Types<T> of(T[] array, int offset, int length) {
		return new Types<>(array, offset, length);
	}

	public static Bytes of(byte[] array, int offset, int length) {
		return new Bytes(array, offset, length);
	}

	public static Ints of(int[] array, int offset, int length) {
		return new Ints(array, offset, length);
	}

	public static class Types<T> extends SubArray<T[]> {
		private Types(T[] array, int offset, int length) {
			super(array, offset, length);
		}

		@Override
		public int hashCode() {
			return ArrayUtil.hash(array, offset, length);
		}

		@Override
		public boolean equals(Object obj) {
			return isEqual(this, obj, Arrays::equals);
		}

		@Override
		public String toString() {
			return ArrayUtil.toString(array, offset, length);
		}
	}

	public static class Bytes extends SubArray<byte[]> {
		private Bytes(byte[] array, int offset, int length) {
			super(array, offset, length);
		}

		@Override
		public int hashCode() {
			return ArrayUtil.hash(array, offset, length);
		}

		@Override
		public boolean equals(Object obj) {
			return isEqual(this, obj, Arrays::equals);
		}

		@Override
		public String toString() {
			return ArrayUtil.toString(array, offset, length);
		}
	}

	public static class Ints extends SubArray<int[]> {
		private Ints(int[] array, int offset, int length) {
			super(array, offset, length);
		}

		@Override
		public int hashCode() {
			return ArrayUtil.hash(array, offset, length);
		}

		@Override
		public boolean equals(Object obj) {
			return isEqual(this, obj, Arrays::equals);
		}

		@Override
		public String toString() {
			return ArrayUtil.toString(array, offset, length);
		}
	}

	public static interface Apply<E extends Exception, T, R> {
		R apply(T array, int offset, int length) throws E;
	}

	public static interface ApplyAsInt<E extends Exception, T> {
		int applyAsInt(T array, int offset, int length) throws E;
	}

	public static interface Accept<E extends Exception, T> {
		void accept(T array, int offset, int length) throws E;
	}

	private SubArray(T array, int offset, int length) {
		this.array = array;
		this.offset = offset;
		this.length = length;
	}

	public <E extends Exception, R> R apply(Apply<E, T, R> applier) throws E {
		return applier.apply(array, offset, length);
	}

	public <E extends Exception> int applyAsInt(ApplyAsInt<E, T> applier) throws E {
		return applier.applyAsInt(array, offset, length);
	}

	public <E extends Exception> void accept(Accept<E, T> accepter) throws E {
		accepter.accept(array, offset, length);
	}

	public int to() {
		return offset + length;
	}

	private static interface Equals<T> {
		boolean isEqual(T lhs, int lhsOff, int lhsLen, T rhs, int rhsOff, int rhsLen);
	}

	private static <T> boolean isEqual(SubArray<T> t, Object obj, Equals<T> equalsFn) {
		if (t == obj) return true;
		if (obj == null) return false;
		if (!t.getClass().isInstance(obj)) return false;
		SubArray<T> other = BasicUtil.uncheckedCast(obj);
		if (t.length != other.length) return false;
		if (t.array == other.array && t.offset == other.offset) return true;
		return equalsFn.isEqual(t.array, t.offset, t.offset + t.length, other.array, other.offset,
			other.offset + other.length);
	}

}
