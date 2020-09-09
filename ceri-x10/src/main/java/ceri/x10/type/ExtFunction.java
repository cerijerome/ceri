package ceri.x10.type;

import static ceri.common.validation.ValidationUtil.*;
import static java.lang.Integer.toHexString;
import ceri.common.util.HashCoder;

public class ExtFunction extends BaseFunction {
	public final int data;
	public final int command;

	public static ExtFunction of(House house, int data, int command) {
		validateNotNull(house);
		validateUbyte(data);
		validateUbyte(command);
		return new ExtFunction(house, data, command);
	}

	private ExtFunction(House house, int data, int command) {
		super(house, FunctionType.extended);
		this.data = data;
		this.command = command;
	}

	public static boolean isAllowed(FunctionType type) {
		return type == FunctionType.extended;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(super.hashCode(), data, command);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ExtFunction)) return false;
		ExtFunction other = (ExtFunction) obj;
		return data == other.data && command == other.command && super.equals(obj);
	}

	@Override
	public String toString() {
		return super.toString() + ":0x" + toHexString(data) + ":0x" + toHexString(command);
	}

}
