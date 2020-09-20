package ceri.common.log;

public enum Level {
	ALL(6),
	ERROR(5),
	WARN(4),
	INFO(3),
	DEBUG(2),
	TRACE(1),
	NONE(0);

	public final int value;

	private Level(int value) {
		this.value = value;
	}

	public boolean valid(Level level) {
		if (this == NONE || level == NONE) return false;
		if (this == ALL || level == ALL) return true;
		return level != null && level.value >= value;
	}

}