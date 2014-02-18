package ceri.x10.type;

import ceri.common.util.HashCoder;

public class ExtFunction extends BaseFunction {
	public final byte data;
	public final byte command;
	private final int hashCode;

	public ExtFunction(House house, byte data, byte command) {
		super(house, FunctionType.EXTENDED);
		this.data = data;
		this.command = command;
		hashCode = HashCoder.hash(super.hashCode(), data, command);
	}

	public static boolean isAllowed(FunctionType type) {
		return type == FunctionType.EXTENDED;
	}

	@Override
	public int hashCode() {
		return hashCode;
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
		return super.toString() + ":0x" + Integer.toHexString(data) + ":0x" +
			Integer.toHexString(command);
	}

}
