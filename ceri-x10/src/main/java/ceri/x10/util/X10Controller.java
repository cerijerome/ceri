package ceri.x10.util;

import java.io.IOException;
import ceri.common.function.Enclosure;
import ceri.x10.command.Command;

/**
 * Interface for X10 controllers.
 */
public interface X10Controller {
	X10Controller NULL = new X10Controller() {};

	enum Type {
		cm11a,
		cm17a;
	}
	
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
	default Enclosure<Command.Listener> listen(Command.Listener listener) {
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
