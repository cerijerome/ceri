package ceri.x10.command;

import static ceri.common.validation.ValidationUtil.*;
import ceri.common.util.HashCoder;
import ceri.x10.type.ExtFunction;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;
import ceri.x10.type.Unit;

public class ExtCommand extends BaseUnitCommand<ExtFunction> {
	public final int data;
	public final int command;

	public static ExtCommand of(House house, Unit unit, int data, int command) {
		validateNotNull(house);
		validateNotNull(house);
		validateUbyte(data);
		validateUbyte(command);
		return new ExtCommand(house, unit, data, command);
	}
	
	private ExtCommand(House house, Unit unit, int data, int command) {
		super(house, unit, FunctionType.extended);
		this.data = data;
		this.command = command;
	}

	@Override
	public ExtFunction function() {
		return ExtFunction.of(house, data, command);
	}
	
	@Override
	public int hashCode() {
		return HashCoder.hash(super.hashCode(), data, command);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ExtCommand)) return false;
		ExtCommand other = (ExtCommand) obj;
		if (data != other.data) return false;
		if (command != other.command) return false;
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return super.toString() + ":0x" + Integer.toHexString(data) + ":0x" +
			Integer.toHexString(command);
	}

}
