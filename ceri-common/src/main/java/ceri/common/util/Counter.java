package ceri.common.util;

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
		return Math.toIntExact(count);
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
		inc(value);
		return intCount();
	}

	@Override
	public String toString() {
		return String.valueOf(count);
	}

}
