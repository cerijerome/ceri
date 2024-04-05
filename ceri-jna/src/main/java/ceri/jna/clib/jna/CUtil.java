package ceri.jna.clib.jna;

import com.sun.jna.Structure;
import ceri.jna.util.Struct;

/**
 * Support methods not explicitly part of CLib.
 */
public class CUtil {

	private CUtil() {}

	public static <T extends Structure> T[] requireContiguous(T[] array) throws CException {
		if (Struct.isByVal(array)) return array;
		throw CException.full(CErrNo.EINVAL, "Array is not contiguous");
	}

}
