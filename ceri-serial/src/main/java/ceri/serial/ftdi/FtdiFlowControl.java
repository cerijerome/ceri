package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;
import ceri.serial.ftdi.jna.LibFtdi;

public enum FtdiFlowControl {
	disabled(LibFtdi.SIO_DISABLE_FLOW_CTRL),
	rtsCts(LibFtdi.SIO_RTS_CTS_HS),
	dtrDsr(LibFtdi.SIO_DTR_DSR_HS),
	xonXoff(LibFtdi.SIO_XON_XOFF_HS);

	private static final TypeTranscoder<FtdiFlowControl> xcoder =
		TypeTranscoder.of(t -> t.value, FtdiFlowControl.class);
	public final int value;

	public static FtdiFlowControl from(int value) {
		return xcoder.decode(value);
	}

	private FtdiFlowControl(int value) {
		this.value = value;
	}
}
