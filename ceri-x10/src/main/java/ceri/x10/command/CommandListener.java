package ceri.x10.command;

import java.util.function.Consumer;

public interface CommandListener {

	default void allUnitsOff(@SuppressWarnings("unused") HouseCommand command) {}

	default void allLightsOff(@SuppressWarnings("unused") HouseCommand command) {}

	default void allLightsOn(@SuppressWarnings("unused") HouseCommand command) {}

	default void off(@SuppressWarnings("unused") UnitCommand command) {}

	default void on(@SuppressWarnings("unused") UnitCommand command) {}

	default void dim(@SuppressWarnings("unused") DimCommand command) {}

	default void bright(@SuppressWarnings("unused") DimCommand command) {}

	default void extended(@SuppressWarnings("unused") ExtCommand command) {}

	/**
	 * Returns a dispatch consumer that calls the matching CommandListener method for a command. 
	 */
	static Consumer<CommandListener> dispatcher(BaseCommand<?> command) {
		switch (command.type) {
		case allUnitsOff:
			HouseCommand allUnitsOff = (HouseCommand) command;
			return listener -> listener.allUnitsOff(allUnitsOff);
		case allLightsOff:
			HouseCommand allLightsOff = (HouseCommand) command;
			return listener -> listener.allLightsOff(allLightsOff);
		case allLightsOn:
			HouseCommand allLightsOn = (HouseCommand) command;
			return listener -> listener.allLightsOn(allLightsOn);
		case off:
			UnitCommand off = (UnitCommand) command;
			return listener -> listener.off(off);
		case on:
			UnitCommand on = (UnitCommand) command;
			return listener -> listener.on(on);
		case dim:
			DimCommand dim = (DimCommand) command;
			return listener -> listener.dim(dim);
		case bright:
			DimCommand bright = (DimCommand) command;
			return listener -> listener.bright(bright);
		case extended:
			ExtCommand extended = (ExtCommand) command;
			return listener -> listener.extended(extended);
		default:
			throw new UnsupportedOperationException("Function type not supported: " + command);
		}
	}

}
