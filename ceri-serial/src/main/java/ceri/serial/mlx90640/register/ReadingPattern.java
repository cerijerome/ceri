package ceri.serial.mlx90640.register;

import ceri.common.data.TypeTranscoder;

public enum ReadingPattern {
	interleaved(0),
	chess(1);

	public static final TypeTranscoder<ReadingPattern> xcoder =
		TypeTranscoder.of(t -> t.id, ReadingPattern.class);
	private final int id;

	private ReadingPattern(int id) {
		this.id = id;
	}
}
