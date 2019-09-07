package ceri.common.svg;

public enum LargeArcFlag {
	small(0),
	large(1);

	public final int value;

	LargeArcFlag(int value) {
		this.value = value;
	}

	public LargeArcFlag reverse() {
		return this == small ? large : small;
	}

}
