package ceri.serial.terminal.command;

import ceri.common.data.TypeTranscoder;

public enum CommandType {
	getStatus(1),
	sendBreak(2),
	setBreakBit(3),
	setNotifications(4),
	setDtr(5),
	setRts(6),
	setFlowControl(7),
	statusResponse(101);
	
	public static final TypeTranscoder.Single<CommandType> xcoder =
		TypeTranscoder.single(t -> t.value, CommandType.class);
	public static final int BYTES = Short.BYTES;
	public final int value;

	private CommandType(int value) {
		this.value = value;
	}
	
}
