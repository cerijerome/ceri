package ceri.serial.terminal.command;

import ceri.common.data.TypeTranscoder;

public enum NotificationType {
	clearToSend(0),
	dataSetReady(1),
	carrierDetect(2),
	ringIndicator(3),
	breakInterrupt(8),
	dataAvailable(9),
	outputEmpty(10),
	framingError(16),
	overrunError(17),
	parityError(18);
	
	public static final TypeTranscoder.Flag<NotificationType> xcoder =
		TypeTranscoder.flag(t -> t.mask, NotificationType.class);
	public static final int BYTES = Integer.BYTES;
	public final int mask;

	private NotificationType(int bit) {
		this.mask = 1 << bit;
	}
}
