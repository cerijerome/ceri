package ceri.serial.clib.jna;

import static ceri.serial.clib.jna.CUtil.failMessage;
import java.util.function.Supplier;
import com.sun.jna.IntegerType;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import ceri.serial.clib.OpenFlag;
import ceri.serial.jna.JnaLibrary;
import ceri.serial.jna.JnaUtil;

/**
 * Methods that call the native C-library.
 */
public class CLib {
	static final JnaLibrary<CLibNative> library =
		JnaLibrary.of(Platform.C_LIBRARY_NAME, CLibNative.class);
	public static final int EOF = -1;

	private CLib() {}

	@SuppressWarnings("serial")
	public static class size_t extends IntegerType {
		public size_t() {
			this(0);
		}

		public size_t(long value) {
			super(Native.SIZE_T_SIZE, value, true);
		}
	}

	@SuppressWarnings("serial")
	public static class ssize_t extends IntegerType {
		public ssize_t() {
			this(0);
		}

		public ssize_t(long value) {
			super(Native.SIZE_T_SIZE, value);
		}
	}

	/**
	 * Opens the path with flags, and returns a file descriptor.
	 */
	public static int open(String path, int flags) throws CException {
		return CUtil.verify(() -> lib().open(path, flags),
			() -> String.format("open(\"%s\", %s)", path, OpenFlag.string(flags)));
	}

	/**
	 * Opens the path with flags and mode, and returns a file descriptor.
	 */
	public static int open(String path, int flags, int mode) throws CException {
		return CUtil.verify(() -> lib().open(path, flags, (short) mode),
			() -> String.format("open(\"%s\", %s, 0%s)", path, OpenFlag.string(flags),
				Integer.toOctalString(mode)));
	}

	/**
	 * Closes the file descriptor.
	 */
	public static void close(int fd) throws CException {
		CUtil.verify(() -> lib().close(fd), "close", fd);
	}

	/**
	 * Reads up to length bytes into buffer. Returns the number of bytes read, or -1 for EOF.
	 */
	public static int read(int fd, Pointer buffer, int length) throws CException {
		if (length == 0) return 0;
		int result = CUtil.verify(() -> lib().read(fd, buffer, new size_t(length)).intValue(),
			() -> failMessage("read", fd, JnaUtil.print(buffer), length));
		return result == 0 ? EOF : result;
	}

	/**
	 * Writes up to length bytes from buffer. Returns the number of bytes written.
	 */
	public static int write(int fd, Pointer buffer, int length) throws CException {
		if (length == 0) return 0;
		return CUtil.verify(() -> lib().write(fd, buffer, new size_t(length)).intValue(),
			() -> failMessage("write", fd, JnaUtil.print(buffer), length));
	}

	/**
	 * Moves the position of file descriptor. Returns the new position.
	 */
	public static int lseek(int fd, int offset, int whence) throws CException {
		return CUtil.verify(() -> lib().lseek(fd, offset, whence), "lseek", fd, offset, whence);
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public static int ioctl(int fd, int request, Object... objs) throws CException {
		return CUtil.verify(() -> lib().ioctl(fd, request, objs),
			() -> ioctlFailMessage(fd, request, objs));
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public static int ioctl(String name, int fd, int request, Object... objs) throws CException {
		return CUtil.verify(() -> lib().ioctl(fd, request, objs),
			() -> ioctlFailMessage(name, fd, request, objs));
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public static int ioctl(Supplier<String> errorMsg, int fd, int request, Object... objs)
		throws CException {
		return CUtil.verify(() -> lib().ioctl(fd, request, objs), errorMsg);
	}

	/* termios.h */

	public static int TCSANOW = 0;

	/**
	 * Retrieves termios attributes; the general terminal interface to control asynchronous
	 * communications ports.
	 */
	public static void tcgetattr(int fd, Pointer termios) throws CException {
		CUtil.verify(() -> lib().tcgetattr(fd, termios),
			() -> failMessage("tcgetattr", fd, JnaUtil.print(termios)));
	}

	/**
	 * Set termios attributes.
	 */
	public static void tcsetattr(int fd, int actions, Pointer termios) throws CException {
		CUtil.verify(() -> lib().tcsetattr(fd, actions, termios),
			() -> failMessage("tcsetattr", fd, JnaUtil.print(termios)));
	}

	/* private */

	private static String ioctlFailMessage(String name, int fd, int request, Object... objs) {
		return failMessage(String.format("%d:ioctl:%s:0x%x", fd, name, request), objs);
	}

	private static String ioctlFailMessage(int fd, int request, Object... objs) {
		return failMessage(String.format("%d:ioctl:0x%x", fd, request), objs);
	}

	private static CLibNative lib() {
		return library.get();
	}

}
