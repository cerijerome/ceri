package ceri.serial.comm;

import ceri.common.data.Xcoder;

/**
 * Serial events from JavaComm API; currently unused.
 */
public enum SerialEvent {
	dataAvailable(1),
	outputBufferEmpty(2),
	clearToSend(3), // CTS
	dataSetReady(4), // DSR
	ringIndicator(5), // RI
	carrierDetect(6), // CD
	overrunError(7), // OE
	parityError(8), // PE
	framingError(9), // FE
	breakInterrupt(10); // BI

	private static final Xcoder.Type<SerialEvent> xcoder = Xcoder.type(SerialEvent.class);
	public final int value;

	public static SerialEvent from(int value) {
		return xcoder.decode(value);
	}

	private SerialEvent(int value) {
		this.value = value;
	}
}
