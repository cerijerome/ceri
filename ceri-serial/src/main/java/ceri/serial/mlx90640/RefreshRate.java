package ceri.serial.mlx90640;

import ceri.common.data.TypeTranscoder;

public enum RefreshRate {
	_0_5Hz(0, 0.5),
	_1Hz(1, 1),
	_2Hz(2, 2),
	_4Hz(3, 4),
	_8Hz(4, 8),
	_16Hz(5, 16),
	_32Hz(6, 32),
	_64Hz(7, 64);

	private static final TypeTranscoder<RefreshRate> xcoder =
		TypeTranscoder.of(t -> t.id, RefreshRate.class);
	private static final int MASK = 0x7;
	private final int id;
	public final double hz;

	public static RefreshRate decode(int value) {
		return xcoder.decode(value & MASK);
	}

	private RefreshRate(int id, double hz) {
		this.id = id;
		this.hz = hz;
	}

	public int encode() {
		return id;
	}

	@Override
	public String toString() {
		return this == _0_5Hz ? "0.5Hz" : name().substring(1);
	}
}
