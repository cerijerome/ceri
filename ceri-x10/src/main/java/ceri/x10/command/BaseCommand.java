package ceri.x10.command;

import ceri.common.util.HashCoder;
import ceri.x10.type.BaseFunction;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;

public abstract class BaseCommand<T extends BaseFunction> {
	public final House house;
	public final FunctionType type;
	
	protected BaseCommand(House house, FunctionType type) {
		this.house = house;
		this.type = type;
	}
	
	public abstract T function();
	
	@Override
	public int hashCode() {
		return HashCoder.hash(house, type);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof BaseCommand)) return false;
		BaseCommand<?> other = (BaseCommand<?>)obj;
		return house == other.house && type == other.type;
	}
	
	@Override
	public String toString() {
		return house.name() + ":" + type.name();
	}
	
}
