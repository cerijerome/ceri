package ceri.ffm.clib.ffm;

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
		CLib.caller.verifyInt(lib -> lib.setenv(name, value, overwriteValue), -1,
			"setenv", name, value, overwriteValue);
	}

	/**
	 * Get an environment variable.
	 */
	public static String getenv(String name) throws CException {
		return CLib.caller.callType(c -> c.lib().getenv(name), "getenv", name);
	}
}
