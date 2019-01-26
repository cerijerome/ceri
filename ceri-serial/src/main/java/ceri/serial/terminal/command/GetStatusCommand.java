package ceri.serial.terminal.command;

import ceri.common.data.DataDecoder;
import ceri.common.text.ToStringHelper;
import ceri.common.util.HashCoder;

public class GetStatusCommand extends Command {
	public static final GetStatusCommand INSTANCE = new GetStatusCommand();

	public static GetStatusCommand decode(DataDecoder decoder) {
		Commands.validateType(decoder, CommandType.getStatus);
		return INSTANCE;
	}

	private GetStatusCommand() {
		super(CommandType.getStatus);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof GetStatusCommand)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this).toString();
	}
	
}
