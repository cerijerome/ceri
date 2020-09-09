package ceri.x10.util;

import ceri.common.util.Enclosed;
import ceri.x10.command.BaseCommand;
import ceri.x10.command.CommandListener;

/**
 * Interface for X10 controllers.
 */
public interface X10Controller {

	/**
	 * Puts a command on the processing queue.
	 */
	void command(BaseCommand<?> command);

	/**
	 * Listen to received/sent commands.
	 */
	Enclosed<CommandListener> listen(CommandListener listener);

}
