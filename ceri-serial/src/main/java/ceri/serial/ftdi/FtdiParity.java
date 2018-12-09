package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;

/** Parity mode for ftdi_set_line_property() */
public enum FtdiParity {
	NONE(0),
	ODD(1),
	EVEN(2),
	MARK(3),
	SPACE(4);

	public static final TypeTranscoder.Single<FtdiParity> xcoder =
		TypeTranscoder.single(t -> t.value, FtdiParity.class);
	public final int value;

	private FtdiParity(int value) {
		this.value = value;
	}
}