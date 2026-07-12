package ceri.ffm.clib.ffm;

import ceri.ffm.reflect.CAnnotations.CInclude;

/**
 * Types and functions from {@code <string.h>}
 */
@CInclude("string.h")
public class CString {

	private CString() {}

	/**
	 * Returns a string that describes the error code.
	 */
	public static String strerror(int errnum) throws CException {
		return CLib.caller.callType(c -> c.lib().strerror(errnum), "strerror", errnum);
	}
}
