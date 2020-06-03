package ceri.serial.mlx90640;

import ceri.common.data.TypeTranscoder;

public enum Resolution {
	_16bit(0),
	_17bit(1),
	_18bit(2),
	_19bit(3);

	public static final TypeTranscoder<Resolution> xcoder =
		TypeTranscoder.of(t -> t.id, Resolution.class);
	public final int id;

	private Resolution(int id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return name().substring(1);
	}
}
