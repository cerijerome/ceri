package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;

public enum FtdiCBusFunc {
	CBUS_TXDEN(0),
	CBUS_PWREN(1),
	CBUS_RXLED(2),
	CBUS_TXLED(3),
	CBUS_TXRXLED(4),
	CBUS_SLEEP(5),
	CBUS_CLK48(6),
	CBUS_CLK24(7),
	CBUS_CLK12(8),
	CBUS_CLK6(9),
	CBUS_IOMODE(0xa),
	CBUS_BB_WR(0xb),
	CBUS_BB_RD(0xc);

	public static final TypeTranscoder.Single<FtdiCBusFunc> xcoder =
		TypeTranscoder.single(t -> t.value, FtdiCBusFunc.class);
	public final int value;

	private FtdiCBusFunc(int value) {
		this.value = value;
	}
}