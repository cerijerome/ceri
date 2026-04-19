package ceri.common.util;

import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * JVM-related functionality.
 */
public class Jvm {

	private Jvm() {}

	/**
	 * A memory usage snapshot.
	 */
	public record Memory(long total, long available, long max) {
		public static final Memory NONE = new Memory(0L, 0L, 0L);

		/**
		 * Tracks memory usage.
		 */
		public static class Tracker {
			private volatile Memory memory = Memory.of();
			private volatile Memory diff = Memory.NONE;

			/**
			 * Returns the memory snapshot calculated in the last update.
			 */
			public Memory memory() {
				return memory;
			}

			/**
			 * Returns the memory changes calculated in the last update.
			 */
			public Memory diff() {
				return diff;
			}

			/**
			 * Saves the current memory snapshot, and calculates the changes.
			 */
			public Tracker update() {
				var memory = Memory.of();
				this.diff = memory.diff(memory());
				this.memory = memory;
				return this;
			}

			/**
			 * Returns a text report of the memory snapshot.
			 */
			public String report() {
				return String.format("used=%d%s free=%d%s", memory().used(), diffVal(diff().used()),
					memory().free(), diffVal(diff().free()));
			}
		}

		private static Memory of() {
			var rt = Runtime.getRuntime();
			return new Memory(rt.totalMemory(), rt.freeMemory(), rt.maxMemory());
		}

		/**
		 * The amount of memory used as the difference between total and free.
		 */
		public long used() {
			return total() - available();
		}

		/**
		 * The amount of memory free as the difference between max and used.
		 */
		public long free() {
			return max() - used();
		}

		/**
		 * Returns a memory snapshot as the difference between this and the given snapshot.
		 */
		public Memory diff(Memory other) {
			if (other == null) return this;
			return new Memory(total() - other.total(), available() - other.available(),
				max() - other.max());
		}
	}

	/**
	 * Returns a new memory usage snapshot.
	 */
	public static Memory memory() {
		return Memory.of();
	}

	/**
	 * Returns a memory usage tracker.
	 */
	public static Memory.Tracker trackMemory() {
		return new Memory.Tracker();
	}

	/**
	 * Returns the current list of JVM arguments.
	 */
	public static List<String> args() {
		return ManagementFactory.getRuntimeMXBean().getInputArguments();
	}

	// support

	private static String diffVal(long value) {
		if (value == 0L) return "";
		return value < 0 ? String.valueOf(value) : "+" + value;
	}
}
