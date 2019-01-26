package ceri.serial.terminal.command;

import static ceri.common.validation.ValidationUtil.validateEqual;
import ceri.common.data.DataDecoder;
import ceri.common.data.DataEncoder;
import ceri.common.text.ToStringHelper;
import ceri.common.util.HashCoder;

public class SendBreakCommand extends Command {
	private static final CommandType TYPE = CommandType.sendBreak;
	static final int SIZE = CommandType.BYTES + Integer.BYTES;
	public final int breakMs;

	public static SendBreakCommand decode(DataDecoder decoder) {
		Commands.validateType(decoder, TYPE);
		return decodeBody(decoder);
	}

	static SendBreakCommand decodeBody(DataDecoder decoder) {
		validateEqual(decoder.total(), SIZE);
		int breakMs = decoder.decodeIntMsb();
		return of(breakMs);
	}

	public static SendBreakCommand of(int breakMs) {
		return new SendBreakCommand(breakMs);
	}

	private SendBreakCommand(int breakMs) {
		super(TYPE);
		this.breakMs = breakMs;
	}

	@Override
	public void encode(DataEncoder encoder) {
		super.encode(encoder);
		encoder.encodeIntMsb(breakMs);
	}

	@Override
	public int size() {
		return SIZE;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(type, breakMs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SendBreakCommand)) return false;
		SendBreakCommand other = (SendBreakCommand) obj;
		if (breakMs != other.breakMs) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, breakMs).toString();
	}

}
