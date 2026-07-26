package ceri.ffm.clib.ffm;

import ceri.ffm.reflect.CAnnotations.CInclude;

/**
 * Types and functions from {@code <string.h>}
 */
@CInclude("string.h")
public class CString {

	private CString() {}

	/**
	 * Returns a string that describes the error code. Returns empty string if the call fails.
	 */
	public static String strerror(int errnum) {
		// Using caller here can cause a failure loop, due to its own use of this call
		// return CLib.caller.callType(c -> c.lib().strerror(errnum), "strerror", errnum);
		return CLib.lib().strerror(errnum);
	}
}
