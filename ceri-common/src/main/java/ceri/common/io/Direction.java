package ceri.common.io;

import ceri.common.data.TypeTranscoder;
import ceri.common.util.BasicUtil;

/**
 * I/O direction.
 */
public enum Direction {
	none(0),
	in(1),
	out(2),
	duplex(3);

	public static final TypeTranscoder<Direction> xcoder =
		TypeTranscoder.of(t -> t.value, Direction.class);
	public final int value;

	public static Direction from(Boolean isOut) {
		return BasicUtil.conditional(isOut, out, in, none);
	}

	public static boolean in(Direction direction) {
		return direction != null && direction.in();
	}

	public static boolean out(Direction direction) {
		return direction != null && direction.out();
	}

	private Direction(int value) {
		this.value = value;
	}

	public boolean in() {
		return this == in || this == duplex;
	}

	public boolean out() {
		return this == out || this == duplex;
	}
}
