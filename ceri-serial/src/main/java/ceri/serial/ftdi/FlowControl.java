package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;

public enum FlowControl {
	/** Reset the port */
	SIO_RESET(0),
	/** Set the modem control register */
	SIO_MODEM_CTRL(1),
	/** Set flow control register */
	SIO_SET_FLOW_CTRL(2),
	/** Set baud rate */
	SIO_SET_BAUD_RATE(3),
	/** Set the data characteristics of the port */
	SIO_SET_DATA(4);

	public static final TypeTranscoder.Single<FlowControl> xcoder =
		TypeTranscoder.single(t -> t.value, FlowControl.class);
	public final int value;

	private FlowControl(int value) {
		this.value = value;
	}

}
