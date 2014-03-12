package ceri.x10.command;

import ceri.x10.type.Address;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;
import ceri.x10.type.Unit;

public class CommandFactory {
	private CommandFactory() {}

	public static HouseCommand allUnitsOff(char house) {
		return allUnitsOff(House.fromChar(house));
	}

	public static HouseCommand allUnitsOff(House house) {
		return new HouseCommand(house, FunctionType.ALL_UNITS_OFF);
	}

	public static HouseCommand allLightsOff(char house) {
		return allLightsOff(House.fromChar(house));
	}

	public static HouseCommand allLightsOff(House house) {
		return new HouseCommand(house, FunctionType.ALL_LIGHTS_OFF);
	}

	public static HouseCommand allLightsOn(char house) {
		return allLightsOn(House.fromChar(house));
	}

	public static HouseCommand allLightsOn(House house) {
		return new HouseCommand(house, FunctionType.ALL_LIGHTS_ON);
	}

	public static UnitCommand off(String address) {
		Address addr = Address.fromString(address);
		return off(addr);
	}

	public static UnitCommand off(Address address) {
		return off(address.house, address.unit);
	}

	public static UnitCommand off(House house, Unit unit) {
		return new UnitCommand(house, unit, FunctionType.OFF);
	}

	public static UnitCommand on(String address) {
		Address addr = Address.fromString(address);
		return on(addr);
	}

	public static UnitCommand on(Address address) {
		return on(address.house, address.unit);
	}

	public static UnitCommand on(House house, Unit unit) {
		return new UnitCommand(house, unit, FunctionType.ON);
	}

	public static DimCommand dim(String address, int percent) {
		Address addr = Address.fromString(address);
		return dim(addr, percent);
	}

	public static DimCommand dim(Address address, int percent) {
		return dim(address.house, address.unit, percent);
	}

	public static DimCommand dim(House house, Unit unit, int percent) {
		return new DimCommand(house, unit, FunctionType.DIM, percent);
	}

	public static DimCommand bright(String address, int percent) {
		Address addr = Address.fromString(address);
		return bright(addr, percent);
	}

	public static DimCommand bright(Address address, int percent) {
		return bright(address.house, address.unit, percent);
	}

	public static DimCommand bright(House house, Unit unit, int percent) {
		return new DimCommand(house, unit, FunctionType.BRIGHT, percent);
	}

	public static ExtCommand extended(String address, int data, int command) {
		Address addr = Address.fromString(address);
		return extended(addr, data, command);
	}

	public static ExtCommand extended(Address address, int data, int command) {
		return extended(address.house, address.unit, data, command);
	}

	public static ExtCommand extended(House house, Unit unit, int data, int command) {
		return new ExtCommand(house, unit, (byte)data, (byte)command);
	}

}
