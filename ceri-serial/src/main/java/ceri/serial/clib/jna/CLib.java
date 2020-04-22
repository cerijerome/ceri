package ceri.serial.clib.jna;

import static ceri.serial.jna.JnaUtil.print;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import ceri.common.text.StringUtil;
import ceri.serial.clib.OpenFlag;
import ceri.serial.jna.JnaUtil;

/**
 * Methods that call the native C-library.
 */
public class CLib {
	public static final int EOF = -1;
	private static final Logger logger = LogManager.getLogger();
	private static final CLibNative CLIB = loadLibrary(Platform.C_LIBRARY_NAME);

	/**
	 * Opens the path with flags, and returns a file descriptor.
	 */
	public static int open(String path, int flags) throws CException {
		return CUtil.verifyErrno(() -> CLIB.open(path, flags),
			() -> String.format("open(\"%s\", %s)", path, OpenFlag.string(flags)));
	}

	/**
	 * Opens the path with flags and mode, and returns a file descriptor.
	 */
	public static int open(String path, int flags, int mode) throws CException {
		return CUtil.verifyErrno(() -> CLIB.open(path, flags, (short) mode),
			() -> String.format("open(\"%s\", %s, 0%s)", path, OpenFlag.string(flags),
				Integer.toOctalString(mode)));
	}

	/**
	 * Closes the file descriptor.
	 */
	public static void close(int fd) throws CException {
		CUtil.verifyErrno(() -> CLIB.close(fd), "close(%d)", fd);
	}

	/**
	 * Reads up to length bytes into buffer. Returns the number of bytes read, or -1 for EOF.
	 */
	public static int read(int fd, Pointer buffer, int length) throws CException {
		if (length == 0) return 0;
		int result = CUtil.verifyErrno(() -> CLIB.read(fd, buffer, length),
			() -> String.format("read(%d, %s, %d)", fd, print(buffer), length));
		return result == 0 ? EOF : result;
	}

	/**
	 * Writes up to length bytes from buffer. Returns the number of bytes written.
	 */
	public static int write(int fd, Pointer buffer, int length) throws CException {
		if (length == 0) return 0;
		return CUtil.verifyErrno(() -> CLIB.write(fd, buffer, length),
			() -> String.format("write(%d, %s, %d)", fd, print(buffer), length));
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public static int ioctl(int fd, int request, Object... objs) throws CException {
		return ioctl((String) null, fd, request, objs);
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public static int ioctl(String name, int fd, int request, Object... objs) throws CException {
		return ioctl(() -> formatIoctl(name, fd, request, objs), fd, request, objs);
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public static int ioctl(Supplier<String> errorMsg, int fd, int request, Object... objs)
		throws CException {
		return CUtil.verifyErrno(() -> CLIB.ioctl(fd, request, objs), errorMsg);
	}

	/**
	 * Moves the position of file descriptor. Returns the new position.
	 */
	public static int lseek(int fd, int offset, int whence) throws CException {
		return CUtil.verifyErrno(() -> CLIB.lseek(fd, offset, whence),
			() -> String.format("lseek(%d, %d, %d)", fd, offset, whence));
	}

	private static String formatIoctl(String name, int fd, int request, Object... objs) {
		name = name == null ? "ioctl" : "ioctl:" + name;
		String args = objs.length == 0 ? "" : StringUtil.join(", ", ", ", "", objs);
		return String.format("%s(%d, 0x%x%s)", name, fd, request, args);
	}

	private static CLibNative loadLibrary(String name) {
		logger.info("Loading {} started", name);
		// logger.info("Protected: {}", JnaUtil.setProtected()); // only use for debug
		CLibNative lib = JnaUtil.loadLibrary(null, CLibNative.class);
		logger.info("Loading {} complete", name);
		return lib;
	}
}
