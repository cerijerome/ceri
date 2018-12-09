package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;

public enum FtdiFlowControl {
	SIO_DISABLE_FLOW_CTRL(0x0000),
	SIO_RTS_CTS_HS(0x0100),
	SIO_DTR_DSR_HS(0x0200),
	SIO_XON_XOFF_HS(0x0400);

	public static final TypeTranscoder.Single<FtdiFlowControl> xcoder =
		TypeTranscoder.single(t -> t.value, FtdiFlowControl.class);
	public final int value;

	private FtdiFlowControl(int value) {
		this.value = value;
	}

}
