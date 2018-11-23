package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;

public enum FtdiCBusXFunc {
	CBUSX_TRISTATE(0),
	CBUSX_TXLED(1),
	CBUSX_RXLED(2),
	CBUSX_TXRXLED(3),
	CBUSX_PWREN(4),
	CBUSX_SLEEP(5),
	CBUSX_DRIVE_0(6),
	CBUSX_DRIVE1(7),
	CBUSX_IOMODE(8),
	CBUSX_TXDEN(9),
	CBUSX_CLK24(10),
	CBUSX_CLK12(11),
	CBUSX_CLK6(12),
	CBUSX_BAT_DETECT(13),
	CBUSX_BAT_DETECT_NEG(14),
	CBUSX_I2C_TXE(15),
	CBUSX_I2C_RXF(16),
	CBUSX_VBUS_SENSE(17),
	CBUSX_BB_WR(18),
	CBUSX_BB_RD(19),
	CBUSX_TIME_STAMP(20),
	CBUSX_AWAKE(21);

	public static final TypeTranscoder.Single<FtdiCBusXFunc> xcoder =
		TypeTranscoder.single(t -> t.value, FtdiCBusXFunc.class);
	public final int value;

	private FtdiCBusXFunc(int value) {
		this.value = value;
	}
}