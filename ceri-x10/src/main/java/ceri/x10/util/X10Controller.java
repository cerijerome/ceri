package ceri.x10.util;

import java.io.Closeable;
import ceri.x10.command.BaseCommand;

/**
 * Interface for X10 controllers.
 */
public interface X10Controller extends Closeable {

	/**
	 * Puts a command on the processing queue.
	 */
	void command(BaseCommand<?> command);
	
}
