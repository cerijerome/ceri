package ceri.serial.terminal.command;

import static ceri.common.validation.ValidationUtil.validateEqual;
import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateRange;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.data.DataDecoder;
import ceri.common.data.DataEncoder;
import ceri.common.data.DataUtil;

public class Commands {
	private static final int MIN_SIZE = GetStateCommand.INSTANCE.size();
	private static final int MAX_SIZE = SetNotificationsCommand.SIZE;

	private Commands() {}

	public static Command decode(ImmutableByteArray data) {
		validateRange(data.length, MIN_SIZE, MAX_SIZE);
		return decode(DataDecoder.of(data));
	}
	
	public static Command decode(DataDecoder decoder) {
		validateMin(decoder.remaining(), MIN_SIZE);
		CommandType type = decodeType(decoder);
		switch (type) {
		case getState:
			return GetStateCommand.INSTANCE;
		case setNotifications:
			return SetNotificationsCommand.decodeBody(decoder);
		default:
			throw new UnsupportedOperationException("Unable to decode packet: " + type);
		}
	}

	public static CommandType decodeType(DataDecoder decoder) {
		return DataUtil.validate(CommandType.xcoder::decode, decoder.decodeShortMsb());
	}
	
	public static void validateType(DataDecoder decoder, CommandType expected) {
		CommandType actual = decodeType(decoder);
		validateEqual(actual,  expected);
	}
	
	public static void encode(DataEncoder encoder, CommandType type) {
		encoder.encodeShortMsb(type.value);
	}
	
}
