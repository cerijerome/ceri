package ceri.ffm.clib.ffm;

import static ceri.ffm.clib.ffm.CLib.caller;
import static ceri.ffm.clib.ffm.CLib.lib;
import ceri.ffm.reflect.CAnnotations.CInclude;

/**
 * Types and functions from {@code <stdlib.h>}
 */
@CInclude("stdlib.h")
public class CStdLib {

	private CStdLib() {}

	/**
	 * Change or add an environment variable.
	 */
	public static void setenv(String name, String value, boolean overwrite) throws CException {
		int overwriteValue = overwrite ? 1 : 0;
		caller.verify(() -> lib().setenv(name, value, overwriteValue), "setenv", name, value,
			overwriteValue);
	}

	/**
	 * Get an environment variable.
	 */
	public static String getenv(String name) throws CException {
		return caller.callType(() -> lib().getenv(name), "getenv", name);
	}
}
