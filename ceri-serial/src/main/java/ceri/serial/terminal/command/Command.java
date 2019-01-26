package ceri.serial.terminal.command;

import static ceri.common.validation.ValidationUtil.validateEqual;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.data.DataEncoder;
import ceri.common.data.DataEncoder.Encodable;

public abstract class Command implements Encodable {
	public final CommandType type;

	protected Command(CommandType type) {
		this.type = type;
	}

	@Override
	public ImmutableByteArray encode() {
		byte[] data = new byte[size()];
		DataEncoder encoder = DataEncoder.of(data);
		encode(encoder);
		validateEqual(encoder.remaining(), 0, "Remaining bytes");
		return ImmutableByteArray.wrap(data);
	}

	@Override
	public void encode(DataEncoder encoder) {
		encoder.encodeShortMsb(type.value);
	}

	@Override
	public int size() {
		return CommandType.BYTES;
	}

}
