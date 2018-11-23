package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;

/** Break type for ftdi_set_line_property2() */
public enum FtdiBreakType {
	BREAK_OFF(0),
	BREAK_ON(1);

	public static final TypeTranscoder.Single<FtdiBreakType> xcoder =
		TypeTranscoder.single(t -> t.value, FtdiBreakType.class);
	public final int value;

	private FtdiBreakType(int value) {
		this.value = value;
	}
}