package ceri.common.io;

import ceri.common.data.BinaryState;

/**
 * Digital signal level.
 */
public enum Level {
	unknown(-1), // or none
	high(1),
	low(0);

	public final int value;

	public static Level from(int value) {
		if (value == high.value) return high;
		if (value == low.value) return low;
		return unknown;
	}

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

	/**
	 * Inverts the level.
	 */
	public static Level invert(Level level) {
		return level == null ? Level.unknown : level.invert();
	}

	/**
	 * Determines state from the given signal active level.
	 */
	public static BinaryState activeState(Level level, Level active) {
		return level == null ? BinaryState.unknown : level.activeState(active);
	}

	/**
	 * Determines state from the level.
	 */
	public static BinaryState state(Level level) {
		return level == null ? BinaryState.unknown : level.state();
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
