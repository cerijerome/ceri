package ceri.common.io;

import ceri.common.data.BinaryState;

/**
 * Digital signal edge.
 */
public enum Edge {
	none,
	rising,
	falling,
	both;

	/**
	 * Converts a change in state to an edge.
	 */
	public static Edge from(BinaryState change) {
		if (change == BinaryState.off) return falling;
		if (change == BinaryState.on) return rising;
		return none;
	}

	/**
	 * Returns the level resulting from the waveform edge.
	 */
	public static Level level(Edge edge) {
		return edge == null ? Level.unknown : edge.level();
	}

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
		return (change != null) && (change != BinaryState.unknown) && contains(from(change));
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