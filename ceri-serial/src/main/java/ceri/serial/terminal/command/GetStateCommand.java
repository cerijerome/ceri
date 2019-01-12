package ceri.serial.terminal.command;

import ceri.common.data.DataDecoder;
import ceri.common.util.HashCoder;

public class GetStateCommand extends Command {
	public static final GetStateCommand INSTANCE = new GetStateCommand();

	public static GetStateCommand decode(DataDecoder decoder) {
		Commands.validateType(decoder, CommandType.getState);
		return INSTANCE;
	}

	private GetStateCommand() {
		super(CommandType.getState);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof GetStateCommand)) return false;
		return true;
	}

}
