package ceri.common.svg;

public enum SweepFlag {
	negative(0),
	positive(1);

	public final int value;

	private SweepFlag(int value) {
		this.value = value;
	}

}
