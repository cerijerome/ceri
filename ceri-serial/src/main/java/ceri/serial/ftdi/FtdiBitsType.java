package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;

/** Number of bits for ftdi_set_line_property() */
public enum FtdiBitsType {
	BITS_7(7),
	BITS_8(8);

	public static final TypeTranscoder.Single<FtdiBitsType> xcoder =
		TypeTranscoder.single(t -> t.value, FtdiBitsType.class);
	public final int value;

	private FtdiBitsType(int value) {
		this.value = value;
	}
}