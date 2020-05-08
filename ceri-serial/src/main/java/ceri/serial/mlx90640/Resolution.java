package ceri.serial.mlx90640;

import ceri.common.data.TypeTranscoder;

public enum Resolution {
	_16bit(0),
	_17bit(1),
	_18bit(2),
	_19bit(3);

	private static final TypeTranscoder<Resolution> xcoder =
		TypeTranscoder.of(t -> t.id, Resolution.class);
	private static final int MASK = 0x3;
	private final int id;

	public static Resolution decode(int value) {
		return xcoder.decode(value & MASK);
	}

	private Resolution(int id) {
		this.id = id;
	}
	
	public int encode() {
		return id;
	}
	
	@Override
	public String toString() {
		return name().substring(1);
	}
}
