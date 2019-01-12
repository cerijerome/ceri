package ceri.serial.terminal.command;

import ceri.common.data.TypeTranscoder;

public enum NotificationType {
	clearToSend(0x1),
	dataSetReady(0x2),
	carrierDetect(0x4),
	ringIndicator(0x8),
	breakInterrupt(0x10),
	dataAvailable(0x20),
	outputEmpty(0x40),
	framingError(0x100),
	overrunError(0x200),
	parityError(0x400);
	
	public static final TypeTranscoder.Flag<NotificationType> xcoder =
		TypeTranscoder.flag(t -> t.value, NotificationType.class);
	public final int value;

	private NotificationType(int value) {
		this.value = value;
	}
}
