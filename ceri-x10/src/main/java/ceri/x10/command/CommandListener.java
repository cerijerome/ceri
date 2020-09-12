package ceri.x10.command;

import java.util.function.Consumer;

public interface CommandListener {

	default void allUnitsOff(@SuppressWarnings("unused") Command command) {}

	default void allLightsOff(@SuppressWarnings("unused") Command command) {}

	default void allLightsOn(@SuppressWarnings("unused") Command command) {}

	default void off(@SuppressWarnings("unused") Command command) {}

	default void on(@SuppressWarnings("unused") Command command) {}

	default void dim(@SuppressWarnings("unused") Command.Dim command) {}

	default void bright(@SuppressWarnings("unused") Command.Dim command) {}

	default void extended(@SuppressWarnings("unused") Command.Ext command) {}

	default Consumer<Command> asConsumer() {
		return command -> dispatcher(command).accept(this);
	}
	
	/**
	 * Returns a dispatch consumer that calls the matching CommandListener method for a command.
	 */
	static Consumer<CommandListener> dispatcher(Command command) {
		switch (command.type()) {
		case allUnitsOff:
			return listener -> listener.allUnitsOff(command);
		case allLightsOff:
			return listener -> listener.allLightsOff(command);
		case allLightsOn:
			return listener -> listener.allLightsOn(command);
		case off:
			return listener -> listener.off(command);
		case on:
			return listener -> listener.on(command);
		case dim:
			return listener -> listener.dim((Command.Dim) command);
		case bright:
			return listener -> listener.bright((Command.Dim) command);
		case ext:
			return listener -> listener.extended((Command.Ext) command);
		default:
			throw new UnsupportedOperationException("Function type not supported: " + command);
		}
	}

}
