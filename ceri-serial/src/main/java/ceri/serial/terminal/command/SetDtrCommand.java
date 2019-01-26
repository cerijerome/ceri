package ceri.serial.terminal.command;

import static ceri.common.validation.ValidationUtil.validateEqual;
import ceri.common.data.DataDecoder;
import ceri.common.data.DataEncoder;
import ceri.common.text.ToStringHelper;
import ceri.common.util.HashCoder;

public class SetDtrCommand extends Command {
	private static final CommandType TYPE = CommandType.setDtr;
	static final int SIZE = CommandType.BYTES + 1;
	public final boolean on;

	public static SetDtrCommand decode(DataDecoder decoder) {
		Commands.validateType(decoder, TYPE);
		return decodeBody(decoder);
	}

	static SetDtrCommand decodeBody(DataDecoder decoder) {
		validateEqual(decoder.total(), SIZE);
		boolean on = decoder.decodeByte() != 0;
		return of(on);
	}

	public static SetDtrCommand of(boolean on) {
		return new SetDtrCommand(on);
	}

	private SetDtrCommand(boolean on) {
		super(TYPE);
		this.on = on;
	}

	@Override
	public void encode(DataEncoder encoder) {
		super.encode(encoder);
		encoder.encodeByte(on ? 1 : 0);
	}

	@Override
	public int size() {
		return SIZE;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(type, on);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SetDtrCommand)) return false;
		SetDtrCommand other = (SetDtrCommand) obj;
		if (on != other.on) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, on).toString();
	}

}
