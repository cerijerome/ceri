package ceri.x10.command;

/**
 * No-op implementation of command listener.
 */
public class CommandAdapter implements CommandListener {

	@Override
	public void allUnitsOff(HouseCommand command) {}

	@Override
	public void allLightsOff(HouseCommand command) {}

	@Override
	public void allLightsOn(HouseCommand command) {}

	@Override
	public void off(UnitCommand command) {}

	@Override
	public void on(UnitCommand command) {}

	@Override
	public void dim(DimCommand command) {}

	@Override
	public void bright(DimCommand command) {}

	@Override
	public void extended(ExtCommand command) {}

}
