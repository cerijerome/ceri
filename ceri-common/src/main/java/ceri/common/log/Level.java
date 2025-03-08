package ceri.common.log;

import java.util.Comparator;
import ceri.common.data.TypeTranscoder;

public enum Level {
	ALL(6),
	ERROR(5),
	WARN(4),
	INFO(3),
	DEBUG(2),
	TRACE(1),
	NONE(0);

	public static final Comparator<Level> COMPARATOR = Comparator.comparingInt(l -> l.value);
	public static final TypeTranscoder<Level> xcoder = TypeTranscoder.of(t -> t.value, Level.class);
	public final int value;

	private Level(int value) {
		this.value = value;
	}

	/**
	 * Returns true if the given level should be logged, when this level is configured.
	 */
	public boolean valid(Level level) {
		if (this == NONE || level == NONE) return false;
		if (this == ALL || level == ALL) return true;
		return level != null && level.value >= value;
	}

	/**
	 * Returns true if this level is below the given level.
	 */
	public boolean isBelow(Level level) {
		return level != null && value < level.value;
	}
}