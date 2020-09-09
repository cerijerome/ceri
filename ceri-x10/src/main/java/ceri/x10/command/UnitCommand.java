package ceri.x10.command;

import ceri.x10.type.Function;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;
import ceri.x10.type.Unit;

public class UnitCommand extends BaseUnitCommand<Function> {

	UnitCommand(House house, Unit unit, FunctionType function) {
		super(house, unit, function);
	}

	@Override
	public Function function() {
		return Function.of(house, type);
	}
	
}
