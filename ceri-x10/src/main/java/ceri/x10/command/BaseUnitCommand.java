package ceri.x10.command;

import ceri.common.util.HashCoder;
import ceri.x10.type.Address;
import ceri.x10.type.BaseFunction;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;
import ceri.x10.type.Unit;

public abstract class BaseUnitCommand<T extends BaseFunction> extends BaseCommand<T> {
	public final Unit unit;
	private final int hashCode;
	
	protected BaseUnitCommand(House house, Unit unit, FunctionType function) {
		super(house, function);
		this.unit = unit;
		hashCode = HashCoder.hash(super.hashCode(), unit); 
	}
	
	public Address address() {
		return new Address(house, unit);
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof BaseUnitCommand)) return false;
		BaseUnitCommand<?> other = (BaseUnitCommand<?>)obj;
		return unit == other.unit && super.equals(obj);
	}
	
	@Override
	public String toString() {
		return house.name() + unit.index + ":" + type.name();
	}
	
}