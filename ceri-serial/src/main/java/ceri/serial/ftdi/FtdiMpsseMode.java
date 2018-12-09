package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;

/**
 * Shifting commands IN MPSSE Mode
 */
public enum FtdiMpsseMode {
	MPSSE_WRITE_NEG(0x01),
	MPSSE_BITMODE(0x02),
	MPSSE_READ_NEG(0x04),
	MPSSE_LSB(0x08),
	MPSSE_DO_WRITE(0x10),
	MPSSE_DO_READ(0x20),
	MPSSE_WRITE_TMS(0x40);

	public static final TypeTranscoder.Single<FtdiMpsseMode> xcoder =
		TypeTranscoder.single(t -> t.value, FtdiMpsseMode.class);
	public final int value;

	private FtdiMpsseMode(int value) {
		this.value = value;
	}

}
