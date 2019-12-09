package ceri.common.util;

import ceri.common.text.ToStringHelper;

/**
 * Simple counter.
 */
public class Counter {
	private long count;

	public static Counter of() {
		return of(0);
	}

	public static Counter of(long count) {
		return new Counter(count);
	}

	private Counter(long count) {
		this.count = count;
	}

	public long count() {
		return count;
	}

	public int intCount() {
		return (int) count; // no overflow check
	}

	public long set(long value) {
		long old = count;
		count = value;
		return old;
	}

	public long inc() {
		return inc(1);
	}

	public long inc(long value) {
		count = Math.addExact(count, value);
		return count;
	}

	public int intInc() {
		return intInc(1);
	}

	public int intInc(long value) {
		return (int) inc(value);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(count);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Counter)) return false;
		Counter other = (Counter) obj;
		if (count != other.count) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, count).toString();
	}

}
