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

	public static int value(Level level) {
		return level == null ? unknown.value : level.value;
	}

	public static BinaryState state(Level level) {
		return level == null ? BinaryState.unknown : level.state();
	}

	private Level(int value) {
		this.value = value;
	}

	public BinaryState state() {
		return switch (this) {
			case high -> BinaryState.on;
			case low -> BinaryState.off;
			default -> BinaryState.unknown;
		};
	}
}
