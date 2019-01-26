package ceri.serial.terminal.command;

import static ceri.common.validation.ValidationUtil.validateEqual;
import ceri.common.data.DataDecoder;
import ceri.common.data.DataEncoder;
import ceri.common.text.ToStringHelper;
import ceri.common.util.HashCoder;

public class SetRtsCommand extends Command {
	private static final CommandType TYPE = CommandType.setRts;
	static final int SIZE = CommandType.BYTES + 1;
	public final boolean on;

	public static SetRtsCommand decode(DataDecoder decoder) {
		Commands.validateType(decoder, TYPE);
		return decodeBody(decoder);
	}

	static SetRtsCommand decodeBody(DataDecoder decoder) {
		validateEqual(decoder.total(), SIZE);
		boolean on = decoder.decodeByte() != 0;
		return of(on);
	}

	public static SetRtsCommand of(boolean on) {
		return new SetRtsCommand(on);
	}

	private SetRtsCommand(boolean on) {
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
		if (!(obj instanceof SetRtsCommand)) return false;
		SetRtsCommand other = (SetRtsCommand) obj;
		if (on != other.on) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, on).toString();
	}

}
