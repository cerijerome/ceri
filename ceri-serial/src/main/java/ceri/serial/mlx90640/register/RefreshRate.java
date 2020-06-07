package ceri.serial.mlx90640.register;

import java.util.concurrent.TimeUnit;
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

	public static final TypeTranscoder<RefreshRate> xcoder =
		TypeTranscoder.of(t -> t.id, RefreshRate.class);
	public static final RefreshRate DEFAULT = _2Hz;
	private final int id;
	public final double hz;

	private RefreshRate(int id, double hz) {
		this.id = id;
		this.hz = hz;
	}

	/**
	 * Time for each sub-page refresh in milliseconds.
	 */
	public int timeMillis() {
		return (int) Math.ceil(TimeUnit.SECONDS.toMillis(1) / hz);	
	}
	
	/**
	 * Time for each sub-page refresh in microseconds.
	 */
	public int timeMicros() {
		return (int) Math.ceil(TimeUnit.SECONDS.toMicros(1) / hz);	
	}
	
	@Override
	public String toString() {
		return this == _0_5Hz ? "0.5Hz" : name().substring(1);
	}
}
