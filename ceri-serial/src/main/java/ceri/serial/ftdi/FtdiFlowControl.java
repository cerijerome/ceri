package ceri.serial.ftdi;

import ceri.common.data.Xcoder;
import ceri.serial.ftdi.jna.LibFtdi;

public enum FtdiFlowControl {
	disabled(LibFtdi.SIO_DISABLE_FLOW_CTRL),
	rtsCts(LibFtdi.SIO_RTS_CTS_HS),
	dtrDsr(LibFtdi.SIO_DTR_DSR_HS),
	xonXoff(LibFtdi.SIO_XON_XOFF_HS);

	public static final Xcoder.Types<FtdiFlowControl> xcoder = Xcoder.types(FtdiFlowControl.class);
	public final int value;

	private FtdiFlowControl(int value) {
		this.value = value;
	}
}
