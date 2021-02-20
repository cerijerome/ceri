package ceri.common.util;

/**
 * Simple hashCoder, keeping result state.
 */
public class Hasher {
	private static final int HASH_INIT = 1;
	private static final int HASH_MULTIPLIER = 31;
	private static final int TRUE_HASH = 1231;
	private static final int FALSE_HASH = 1237;
	private int hash = HASH_INIT;

	public static Hasher of() {
		return new Hasher();
	}

	private Hasher() {}

	public int code() {
		return hash;
	}

	public Hasher hash(int value) {
		hash = HASH_MULTIPLIER * hash + value;
		return this;
	}

	public Hasher hash(boolean value) {
		return hash(value ? TRUE_HASH : FALSE_HASH);
	}

	public Hasher hash(long value) {
		return hash((int) (value ^ (value >>> Integer.SIZE)));
	}

	public Hasher hash(float value) {
		return hash(Float.floatToIntBits(value));
	}

	public Hasher hash(double value) {
		return hash(Double.doubleToLongBits(value));
	}

	public Hasher hash(Object value) {
		return hash(value == null ? 0 : value.hashCode());
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Hasher)) return false;
		Hasher other = (Hasher) obj;
		if (hash != other.hash) return false;
		return true;
	}

}
