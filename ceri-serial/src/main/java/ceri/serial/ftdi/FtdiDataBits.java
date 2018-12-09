package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;

/** Number of bits for ftdi_set_line_property() */
public enum FtdiDataBits {
	BITS_7(7),
	BITS_8(8);

	public static final TypeTranscoder.Single<FtdiDataBits> xcoder =
		TypeTranscoder.single(t -> t.value, FtdiDataBits.class);
	public final int value;

	private FtdiDataBits(int value) {
		this.value = value;
	}
}