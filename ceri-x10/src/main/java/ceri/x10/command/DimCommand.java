package ceri.x10.command;

import ceri.common.util.HashCoder;
import ceri.x10.type.DimFunction;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;
import ceri.x10.type.Unit;

public class DimCommand extends BaseUnitCommand<DimFunction> {
	public final int percent;
	private final int hashCode;

	DimCommand(House house, Unit unit, FunctionType function, int percent) {
		super(house, unit, function);
		if (percent < 0 || percent > 100) throw new IllegalArgumentException(
			"Dim level must be 0-100%: " + percent);
		this.percent = percent;
		hashCode = HashCoder.hash(super.hashCode(), percent);
	}

	@Override
	public DimFunction function() {
		return new DimFunction(house, type, percent);
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof DimCommand)) return false;
		DimCommand other = (DimCommand) obj;
		return percent == other.percent && super.equals(obj);
	}

	@Override
	public String toString() {
		return super.toString() + "(" + percent + "%)";
	}

}
