package ceri.x10.type;

import ceri.common.util.HashCoder;

/**
 * Base type for all functions. Keeps track of house and type.
 */
public abstract class BaseFunction {
	public final House house;
	public final FunctionType type;
	private final int hashCode;
	
	protected BaseFunction(House house, FunctionType type) {
		this.house = house;
		this.type = type;
		hashCode = HashCoder.hash(house, type);
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof BaseFunction)) return false;
		BaseFunction other = (BaseFunction)obj;
		return house == other.house && type == other.type;
	}
	
	@Override
	public String toString() {
		return house.name() + ":" + type.name();
	}
	
}
