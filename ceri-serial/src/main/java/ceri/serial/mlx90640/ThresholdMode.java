package ceri.serial.mlx90640;

import ceri.common.data.TypeTranscoder;

public enum ThresholdMode {
	normal(0, "normal"),
	_1_8v(1, "1.8V");

	public static final TypeTranscoder<ThresholdMode> xcoder =
		TypeTranscoder.of(t -> t.id, ThresholdMode.class);
	private final int id;
	private final String desc;

	private ThresholdMode(int id, String desc) {
		this.id = id;
		this.desc = desc;
	}

	@Override
	public String toString() {
		return desc;
	}
}
