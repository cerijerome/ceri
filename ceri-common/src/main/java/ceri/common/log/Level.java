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
		return this != NONE && level != NONE && level != null && level.value >= value;
	}
	
}