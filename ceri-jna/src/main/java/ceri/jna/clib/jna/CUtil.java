package ceri.jna.clib.jna;

import static ceri.jna.clib.jna.CUnistd.STDIN_FILENO;
import com.sun.jna.Structure;
import ceri.jna.util.Struct;

/**
 * Support methods not explicitly part of CLib.
 */
public class CUtil {

	private CUtil() {}

	/**
	 * Throws an exception if the array is not contiguous.
	 */
	public static <T extends Structure> T[] requireContiguous(T[] array) throws CException {
		if (Struct.isByVal(array)) return array;
		throw CException.full(CErrNo.EINVAL, "Array is not contiguous");
	}

	/**
	 * Returns true if stdin is a tty, returning false on error.
	 */
	public static boolean tty() {
		try {
			return CUnistd.isatty(STDIN_FILENO);
		} catch (CException _) {
			return false;
		}
	}
	
}
