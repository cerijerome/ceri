package ceri.common.util;

/**
 * Simple counters that prevent overflow.
 */
public class Counter {
	private Counter() {}

	/**
	 * Returns an int counter.
	 */
	public static Counter.OfInt of(int value) {
		var counter = new Counter.OfInt();
		counter.set(value);
		return counter;
	}

	/**
	 * Returns a long counter.
	 */
	public static Counter.OfLong of(long value) {
		var counter = new Counter.OfLong();
		counter.set(value);
		return counter;
	}

	/**
	 * Counter that prevents int overflow.
	 */
	public static class OfInt {
		private int count = 0;

		/**
		 * Returns the current count.
		 */
		public int get() {
			return count;
		}

		/**
		 * Set the current count and return the previous count.
		 */
		public int set(int value) {
			var old = count;
			count = value;
			return old;
		}

		/**
		 * Increment then return the current count; value can be negative.
		 */
		public int inc(int value) {
			count = Math.addExact(count, value);
			return count;
		}

		/**
		 * Increment the current count and return the previous count; value can be negative.
		 */
		public int preInc(int value) {
			return set(Math.addExact(count, value));
		}

		@Override
		public String toString() {
			return String.valueOf(count);
		}
	}

	/**
	 * Counter that prevents int overflow.
	 */
	public static class OfLong {
		private long count = 0;

		/**
		 * Returns the current count.
		 */
		public long get() {
			return count;
		}

		/**
		 * Set the current count.
		 */
		public long set(long value) {
			var old = count;
			count = value;
			return old;
		}

		/**
		 * Increment then return the current count; value can be negative.
		 */
		public long inc(long value) {
			count = Math.addExact(count, value);
			return count;
		}

		/**
		 * Increment the current count and return the previous count; value can be negative.
		 */
		public long preInc(long value) {
			return set(Math.addExact(count, value));
		}

		@Override
		public String toString() {
			return String.valueOf(count);
		}
	}
}
