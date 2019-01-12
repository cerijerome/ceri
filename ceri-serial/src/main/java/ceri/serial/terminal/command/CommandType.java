package ceri.serial.terminal.command;

import ceri.common.data.TypeTranscoder;

public enum CommandType {
	getState(1),
	sendBreak(2),
	setNotifications(3),
	setDtr(4),
	setRts(5),
	setFlowControl(6);
	
	public static final TypeTranscoder.Single<CommandType> xcoder =
		TypeTranscoder.single(t -> t.value, CommandType.class);
	public static final int BYTES = 2;
	public final int value;

	private CommandType(int value) {
		this.value = value;
	}
	
}
