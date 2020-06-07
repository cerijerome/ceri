package ceri.serial.mlx90640.register;

import ceri.common.data.TypeTranscoder;

public enum SubPage {
	_0(0),
	_1(1);

	public static final TypeTranscoder<SubPage> xcoder =
		TypeTranscoder.of(t -> t.value, SubPage.class);
	public final int value;

	public static SubPage from(int value) {
		return xcoder.decode(value);
	}
	
	private SubPage(int value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "subpage" + value;
	}
}
