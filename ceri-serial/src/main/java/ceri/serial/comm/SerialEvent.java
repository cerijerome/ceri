package ceri.serial.comm;

import ceri.common.data.TypeTranscoder;

public enum SerialPortEvent {
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

	private static final TypeTranscoder<SerialPortEvent> xcoder =
		TypeTranscoder.of(t -> t.value, SerialPortEvent.class);
	public final int value;

	public static SerialPortEvent from(int value) {
		return xcoder.decode(value);
	}

	private SerialPortEvent(int value) {
		this.value = value;
	}
}
