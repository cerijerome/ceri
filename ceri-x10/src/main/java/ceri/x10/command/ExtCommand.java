package ceri.x10.command;

import ceri.common.util.HashCoder;
import ceri.x10.type.ExtFunction;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;
import ceri.x10.type.Unit;

public class ExtCommand extends BaseUnitCommand<ExtFunction> {
	public final byte data;
	public final byte command;
	private final int hashCode;

	ExtCommand(House house, Unit unit, byte data, byte command) {
		super(house, unit, FunctionType.EXTENDED);
		this.data = data;
		this.command = command;
		hashCode = HashCoder.hash(super.hashCode(), data, command);
	}

	@Override
	public ExtFunction function() {
		return new ExtFunction(house, data, command);
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ExtCommand)) return false;
		ExtCommand other = (ExtCommand) obj;
		return data == other.data && command == other.command && super.equals(obj);
	}

	@Override
	public String toString() {
		return super.toString() + ":0x" + Integer.toHexString(data) + ":0x" +
			Integer.toHexString(command);
	}

}
