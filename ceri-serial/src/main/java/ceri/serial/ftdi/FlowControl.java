package ceri.serial.ftdi;

import ceri.serial.ftdi.jna.LibFtdi.ftdi_flow_control;

public enum FlowControl {
	disabled(ftdi_flow_control.SIO_DISABLE_FLOW_CTRL),
	rtsCts(ftdi_flow_control.SIO_RTS_CTS_HS),
	dtrDsr(ftdi_flow_control.SIO_DTR_DSR_HS),
	xonXoff(ftdi_flow_control.SIO_XON_XOFF_HS);

	public final ftdi_flow_control value;

	private FlowControl(ftdi_flow_control value) {
		this.value = value;
	}

}
