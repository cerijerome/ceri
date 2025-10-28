package ceri.jna.clib.jna;

import static ceri.jna.clib.jna.CUnistd.STDIN_FILENO;
import java.util.Optional;
import com.sun.jna.Structure;
import ceri.common.function.Excepts;
import ceri.jna.type.Struct;

/**
 * Support methods not explicitly part of CLib.
 */
public class CUtil {

	private CUtil() {}

	/**
	 * Used with ioctl enums.
	 */
	public interface Ioctl {
		/**
		 * Call ioctl using enum name and value: {@code CIoctl.ioctl(name(), fd, value, objs)}
		 */
		int ioctl(int fd, Object... objs) throws CException;
	}

	/**
	 * Common ioctl read paradigm. Call ioctl with struct pointer, read struct values, and return
	 * the struct. If a given error occurs, return null. Any other error will throw an exception.
	 */
	public static <T extends Struct> T ioctlRead(int fd, Ioctl ioctl, T t, int... errNos)
		throws CException {
		try {
			ioctl.ioctl(fd, t.getPointer());
			return Struct.read(t);
		} catch (CException e) {
			for (int errNo : errNos)
				if (errNo == e.code) return null;
			throw e;
		}
	}

	/**
	 * Execute supplier and return the result as optional. If a given error occurs, return empty.
	 * Any other error will throw an exception.
	 */
	public static <T> Optional<T> optionalGet(Excepts.Supplier<CException, T> supplier,
		int... errNos) throws CException {
		return Optional.ofNullable(get(supplier, errNos));
	}

	/**
	 * Execute supplier and return the result. If a given error occurs, return null. Any other error
	 * will throw an exception.
	 */
	public static <T> T get(Excepts.Supplier<CException, T> supplier, int... errNos)
		throws CException {
		try {
			return supplier.get();
		} catch (CException e) {
			for (int errNo : errNos)
				if (errNo == e.code) return null;
			throw e;
		}
	}

	/**
	 * Execute runnable and return true. If a given error occurs, return false. Any other error will
	 * throw an exception.
	 */
	public static boolean run(Excepts.Runnable<CException> runnable, int... errNos)
		throws CException {
		try {
			runnable.run();
			return true;
		} catch (CException e) {
			for (int errNo : errNos)
				if (errNo == e.code) return false;
			throw e;
		}
	}

	/**
	 * Throws a CLib exception if the array is not contiguous.
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
