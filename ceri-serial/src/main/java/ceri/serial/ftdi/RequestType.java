package ceri.serial.ftdi;

import static ceri.serial.ftdi.FlowControl.SIO_MODEM_CTRL;
import static ceri.serial.ftdi.FlowControl.SIO_RESET;
import static ceri.serial.ftdi.FlowControl.SIO_SET_BAUD_RATE;
import static ceri.serial.ftdi.FlowControl.SIO_SET_DATA;
import static ceri.serial.ftdi.FlowControl.SIO_SET_FLOW_CTRL;
import ceri.common.data.TypeTranscoder;

public enum RequestType {
	SIO_RESET_REQUEST(SIO_RESET.value),
	SIO_SET_BAUDRATE_REQUEST(SIO_SET_BAUD_RATE.value),
	SIO_SET_DATA_REQUEST(SIO_SET_DATA.value),
	SIO_SET_FLOW_CTRL_REQUEST(SIO_SET_FLOW_CTRL.value),
	SIO_SET_MODEM_CTRL_REQUEST(SIO_MODEM_CTRL.value),
	SIO_POLL_MODEM_STATUS_REQUEST(0x05),
	SIO_SET_EVENT_CHAR_REQUEST(0x06),
	SIO_SET_ERROR_CHAR_REQUEST(0x07),
	SIO_SET_LATENCY_TIMER_REQUEST(0x09),
	SIO_GET_LATENCY_TIMER_REQUEST(0x0A),
	SIO_SET_BITMODE_REQUEST(0x0B),
	SIO_READ_PINS_REQUEST(0x0C),
	SIO_READ_EEPROM_REQUEST(0x90),
	SIO_WRITE_EEPROM_REQUEST(0x91),
	SIO_ERASE_EEPROM_REQUEST(0x92);

	public static final TypeTranscoder.Single<RequestType> xcoder =
		TypeTranscoder.single(t -> t.value, RequestType.class);
	public final int value;

	private RequestType(int value) {
		this.value = value;
	}

}
