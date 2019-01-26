package ceri.serial.terminal.command;

import static ceri.common.validation.ValidationUtil.validateEqual;
import ceri.common.data.DataDecoder;
import ceri.common.data.DataEncoder;
import ceri.common.data.DataUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.serial.javax.FlowControl;

public class SetFlowControlCommand extends Command {
	private static final CommandType TYPE = CommandType.setFlowControl;
	static final int SIZE = CommandType.BYTES + 1;
	public final FlowControl flowControl;

	public static SetFlowControlCommand decode(DataDecoder decoder) {
		Commands.validateType(decoder, TYPE);
		return decodeBody(decoder);
	}

	static SetFlowControlCommand decodeBody(DataDecoder decoder) {
		validateEqual(decoder.total(), SIZE);
		FlowControl flowControl = DataUtil.validate(FlowControl::from, decoder.decodeByte());
		return of(flowControl);
	}

	public static SetFlowControlCommand of(FlowControl flowControl) {
		return new SetFlowControlCommand(flowControl);
	}

	private SetFlowControlCommand(FlowControl flowControl) {
		super(TYPE);
		this.flowControl = flowControl;
	}

	@Override
	public void encode(DataEncoder encoder) {
		super.encode(encoder);
		encoder.encodeByte(flowControl.value);
	}

	@Override
	public int size() {
		return SIZE;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(type, flowControl);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SetFlowControlCommand)) return false;
		SetFlowControlCommand other = (SetFlowControlCommand) obj;
		if (!EqualsUtil.equals(flowControl, other.flowControl)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, flowControl).toString();
	}

}
