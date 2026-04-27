package ceri.common.util;

import java.util.Arrays;

/**
 * Simple hashCoder, keeping result state.
 */
public class Hasher {
	private static final int HASH_INIT = 1;
	private static final int HASH_MULTIPLIER = 31;
	private static final int TRUE_HASH = 1231;
	private static final int FALSE_HASH = 1237;
	private int hash;

	/**
	 * Varargs method for Arrays.deepHashCode().
	 */
	public static int deep(Object... objs) {
		return Arrays.deepHashCode(objs);
	}

	public static Hasher of() {
		return of(HASH_INIT);
	}

	public static Hasher of(int init) {
		return new Hasher(init);
	}

	private Hasher(int init) {
		this.hash = init;
	}

	public Hasher add(int value) {
		hash = HASH_MULTIPLIER * hash + value;
		return this;
	}

	public Hasher add(boolean value) {
		return add(value ? TRUE_HASH : FALSE_HASH);
	}

	public Hasher add(long value) {
		return add((int) (value ^ (value >>> Integer.SIZE)));
	}

	public Hasher add(float value) {
		return add(Float.floatToIntBits(value));
	}

	public Hasher add(double value) {
		return add(Double.doubleToLongBits(value));
	}

	public Hasher add(Object value) {
		return add(value == null ? 0 : value.hashCode());
	}

	public Hasher deepAdd(Object value) {
		return add(value == null ? 0 : deep(value));
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return (obj instanceof Hasher other) && hash == other.hash;
	}

	@Override
	public String toString() {
		return String.format("0x%08x", hash);
	}
}
