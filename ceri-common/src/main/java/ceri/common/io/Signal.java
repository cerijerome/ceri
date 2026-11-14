package ceri.common.io;

import ceri.common.data.BinaryState;

/**
 * Digital signal support.
 */
public class Signal {

	private Signal() {}

	/**
	 * Digital signal edge.
	 */
	public enum Edge {
		none,
		rising,
		falling,
		both;

		/**
		 * Returns true for any rising/falling edge.
		 */
		public static boolean hasAny(Edge edge) {
			return edge != null && edge != none;
		}

		/**
		 * Returns true if this edge contains the given edge.
		 */
		public boolean contains(Edge edge) {
			return (edge != null) && ((this == edge) || (this == both && edge != none));
		}

		/**
		 * Returns true if this edge contains the given state change.
		 */
		public boolean contains(BinaryState change) {
			return (change != null) && (change != BinaryState.unknown) && contains(edge(change));
		}

		/**
		 * Returns the level resulting from the waveform edge.
		 */
		public Level level() {
			return switch (this) {
				case rising -> Level.high;
				case falling -> Level.low;
				default -> Level.unknown;
			};
		}
	}

	/**
	 * Digital signal level.
	 */
	public enum Level {
		unknown(-1), // or none
		high(1),
		low(0);

		public final int value;

		/**
		 * Determines the level value.
		 */
		public static int value(Level level) {
			return level == null ? unknown.value : level.value;
		}

		/**
		 * Determines if the level is known.
		 */
		public static boolean known(Level level) {
			return (level != null) && level.known();
		}

		private Level(int value) {
			this.value = value;
		}

		/**
		 * Determines if the level is known.
		 */
		public boolean known() {
			return this != unknown;
		}

		/**
		 * Inverts the level.
		 */
		public Level invert() {
			return switch (this) {
				case high -> low;
				case low -> high;
				default -> unknown;
			};
		}

		/**
		 * Determines state from the given signal active level.
		 */
		public BinaryState activeState(Level active) {
			return active != Level.low ? state() : state().invert();
		}

		/**
		 * Determines state from the level.
		 */
		public BinaryState state() {
			return switch (this) {
				case high -> BinaryState.on;
				case low -> BinaryState.off;
				default -> BinaryState.unknown;
			};
		}
	}

	/**
	 * Converts a change in state to an edge.
	 */
	public static Edge edge(BinaryState change) {
		if (change == BinaryState.off) return Edge.falling;
		if (change == BinaryState.on) return Edge.rising;
		return Edge.none;
	}

	/**
	 * Returns the level resulting from the waveform edge.
	 */
	public static Level level(Edge edge) {
		return edge == null ? Level.unknown : edge.level();
	}

	/**
	 * Returns the level type for an int value.
	 */
	public static Level level(int value) {
		if (value < 0) return Level.unknown;
		return value > 0 ? Level.high : Level.low;
	}
	
	/**
	 * Inverts the level.
	 */
	public static Level invert(Level level) {
		return level == null ? Level.unknown : level.invert();
	}

	/**
	 * Determines state from the level.
	 */
	public static BinaryState state(Level level) {
		return level == null ? BinaryState.unknown : level.state();
	}
	
	/**
	 * Determines state from the given signal active level.
	 */
	public static BinaryState activeState(Level level, Level active) {
		return level == null ? BinaryState.unknown : level.activeState(active);
	}
}
