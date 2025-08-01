package ceri.x10.util;

import java.io.IOException;
import ceri.common.util.Enclosure;
import ceri.x10.command.Command;
import ceri.x10.command.CommandListener;

/**
 * Interface for X10 controllers.
 */
public interface X10Controller {
	X10Controller NULL = new X10Controller() {};

	/**
	 * Determines if the controller supports the command.
	 */
	default boolean supports(@SuppressWarnings("unused") Command command) {
		// Support no commands by default
		return false;
	}

	/**
	 * Processes a command.
	 */
	@SuppressWarnings("unused")
	default void command(Command command) throws IOException {
		verifySupported(this, command);
	}

	/**
	 * Listen to received/sent commands.
	 */
	default Enclosure<CommandListener> listen(CommandListener listener) {
		// Do nothing by default
		return Enclosure.noOp(listener);
	}

	/**
	 * Throws an exception if the controller does not support the command.
	 */
	static Command verifySupported(X10Controller x10, Command command) {
		if (x10 != null && x10.supports(command)) return command;
		throw new UnsupportedOperationException("Command not supported: " + command);
	}
}
