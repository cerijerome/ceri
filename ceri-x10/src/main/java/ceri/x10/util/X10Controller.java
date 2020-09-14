package ceri.x10.util;

import java.io.IOException;
import ceri.common.util.Enclosed;
import ceri.x10.command.Command;
import ceri.x10.command.CommandListener;

/**
 * Interface for X10 controllers.
 */
public interface X10Controller {

	/**
	 * Determines if the controller supports the command.
	 */
	default boolean supports(@SuppressWarnings("unused") Command command) {
		return false;
	}

	/**
	 * Puts a command on the processing queue.
	 */
	void command(Command command) throws IOException;

	/**
	 * Listen to received/sent commands.
	 */
	Enclosed<CommandListener> listen(CommandListener listener);

}
