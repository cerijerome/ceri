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

	default void ext(@SuppressWarnings("unused") Command.Ext command) {}

	default Consumer<Command> asConsumer() {
		return command -> dispatcher(command).accept(this);
	}

	/**
	 * Returns a dispatch consumer that calls the matching CommandListener method for a command.
	 */
	static Consumer<CommandListener> dispatcher(Command command) {
		return switch (command.type()) {
			case allUnitsOff -> listener -> listener.allUnitsOff(command);
			case allLightsOff -> listener -> listener.allLightsOff(command);
			case allLightsOn -> listener -> listener.allLightsOn(command);
			case off -> listener -> listener.off(command);
			case on -> listener -> listener.on(command);
			case dim -> listener -> listener.dim((Command.Dim) command);
			case bright -> listener -> listener.bright((Command.Dim) command);
			case ext -> listener -> listener.ext((Command.Ext) command);
			default -> throw new UnsupportedOperationException(
				"Function type not supported: " + command);
		};
	}

}
