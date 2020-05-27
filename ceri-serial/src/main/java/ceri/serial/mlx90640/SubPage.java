package ceri.serial.mlx90640;

import ceri.common.data.TypeTranscoder;

public enum SubPage {
	_0(0),
	_1(1);

	public static final TypeTranscoder<SubPage> xcoder =
		TypeTranscoder.of(t -> t.id, SubPage.class);
	private final int id;

	private SubPage(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "subpage" + id;
	}
}
