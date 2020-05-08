package ceri.serial.mlx90640;

import ceri.common.data.TypeTranscoder;

public enum SubPage {
	_0(0),
	_1(1),
	reserved2(2),
	reserved3(3),
	reserved4(4),
	reserved5(5),
	reserved6(6),
	reserved7(7);

	private static final TypeTranscoder<SubPage> xcoder =
		TypeTranscoder.of(t -> t.id, SubPage.class);
	private static final int MASK = 0x7;
	private static final int INVALID_NUMBER = -1;
	public final int number;
	private final int id;

	public static SubPage decode(int value) {
		return xcoder.decode(value & MASK);
	}

	private SubPage(int id) {
		this.id = id;
		number = id <= 1 ? id : INVALID_NUMBER;
	}

	public boolean isValid() {
		return number != INVALID_NUMBER;
	}

	public int encode() {
		return id;
	}

	@Override
	public String toString() {
		return isValid() ? String.valueOf(number) : "reserved(" + id + ")";
	}
}
