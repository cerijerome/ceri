package ceri.jna.clib.jna;

import static ceri.jna.clib.jna.CLib.caller;
import static ceri.jna.clib.jna.CLib.lib;

/**
 * Types and functions from {@code <stdlib.h>}
 */
public class CStdlib {
	
	private CStdlib() {}
	
	/**
	 * Change or add an environment variable.
	 */
	public static void setenv(String name, String value, boolean overwrite) throws CException {
		int overwriteValue = overwrite ? 1 : 0;
		caller.verify(() -> lib().setenv(name, value, overwriteValue), name, value, overwriteValue);
	}

	/**
	 * Get an environment variable.
	 */
	public static String getenv(String name) throws CException {
		return caller.callType(() -> lib().getenv(name), "getenv", name);
	}

}
