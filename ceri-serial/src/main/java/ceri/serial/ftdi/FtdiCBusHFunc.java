package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;

public enum FtdiCBusHFunc {
	CBUSH_TRISTATE(0),
	CBUSH_TXLED(1),
	CBUSH_RXLED(2),
	CBUSH_TXRXLED(3),
	CBUSH_PWREN(4),
	CBUSH_SLEEP(5),
	CBUSH_DRIVE_0(6),
	CBUSH_DRIVE1(7),
	CBUSH_IOMODE(8),
	CBUSH_TXDEN(9),
	CBUSH_CLK30(10),
	CBUSH_CLK15(11),
	CBUSH_CLK7_5(12);

	public static final TypeTranscoder.Single<FtdiCBusHFunc> xcoder =
		TypeTranscoder.single(t -> t.value, FtdiCBusHFunc.class);
	public final int value;

	private FtdiCBusHFunc(int value) {
		this.value = value;
	}
}