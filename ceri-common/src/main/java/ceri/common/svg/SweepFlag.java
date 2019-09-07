package ceri.common.svg;

public enum SweepFlag {
	negative(0),
	positive(1);

	public final int value;

	SweepFlag(int value) {
		this.value = value;
	}

	public SweepFlag reverse() {
		return this == negative ? positive : negative;
	}

}
