package ceri.x10.command;

import ceri.x10.type.Function;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;

public class HouseCommand extends BaseCommand<Function> {
	
	HouseCommand(House house, FunctionType function) {
		super(house, function);
	}
	
	@Override
	public Function function() {
		return Function.of(house, type);
	}
	
}
