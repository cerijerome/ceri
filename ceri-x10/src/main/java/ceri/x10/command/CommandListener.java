package ceri.x10.command;

public interface CommandListener {

	void allUnitsOff(HouseCommand command);

	void allLightsOff(HouseCommand command);

	void allLightsOn(HouseCommand command);

	void off(UnitCommand command);

	void on(UnitCommand command);

	void dim(DimCommand command);

	void bright(DimCommand command);

	void extended(ExtCommand command);

}
