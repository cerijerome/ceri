package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;

/** Number of stop bits for ftdi_set_line_property() */
public enum FtdiStopBitsType {
	STOP_BIT_1(0),
	STOP_BIT_15(1),
	STOP_BIT_2(2);

	public static final TypeTranscoder.Single<FtdiStopBitsType> xcoder =
		TypeTranscoder.single(t -> t.value, FtdiStopBitsType.class);
	public final int value;

	private FtdiStopBitsType(int value) {
		this.value = value;
	}
}