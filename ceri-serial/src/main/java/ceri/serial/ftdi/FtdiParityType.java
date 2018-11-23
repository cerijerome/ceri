package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;

/** Parity mode for ftdi_set_line_property() */
public enum FtdiParityType {
	NONE(0),
	ODD(1),
	EVEN(2),
	MARK(3),
	SPACE(4);

	public static final TypeTranscoder.Single<FtdiParityType> xcoder =
		TypeTranscoder.single(t -> t.value, FtdiParityType.class);
	public final int value;

	private FtdiParityType(int value) {
		this.value = value;
	}
}