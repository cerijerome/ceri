package ceri.serial.clib.jna;

import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.common.validation.ValidationUtil.validateUbyte;
import java.util.function.Supplier;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import ceri.common.util.OsUtil;
import ceri.serial.clib.OpenFlag;
import ceri.serial.clib.jna.CLibNative.size_t;
import ceri.serial.jna.JnaCaller;
import ceri.serial.jna.JnaLibrary;

/**
 * Methods that call the native C-library, grouped by linux header file.
 */
public class CLib {
	static final JnaLibrary<CLibNative> library =
		JnaLibrary.of(Platform.C_LIBRARY_NAME, CLibNative.class);
	private static final JnaCaller<CException> caller = JnaCaller.of();
	public static final int EOF = -1;

	private CLib() {}

	/* unistd.h */

	/**
	 * Closes the file descriptor.
	 */
	public static void close(int fd) throws CException {
		caller.verify(() -> lib().close(fd), "close", fd);
	}

	/**
	 * Reads up to length bytes into buffer. Returns the number of bytes read, or -1 for EOF.
	 */
	public static int read(int fd, Pointer buffer, int length) throws CException {
		if (length == 0) return 0;
		int result = caller.verifyInt(() -> lib().read(fd, buffer, new size_t(length)).intValue(),
			"read", fd, buffer, length);
		return result == 0 ? EOF : result;
	}

	/**
	 * Writes up to length bytes from buffer. Returns the number of bytes written.
	 */
	public static int write(int fd, Pointer buffer, int length) throws CException {
		if (length == 0) return 0;
		return caller.verifyInt(() -> lib().write(fd, buffer, new size_t(length)).intValue(),
			"write", fd, buffer, length);
	}

	/**
	 * Moves the position of file descriptor. Returns the new position.
	 */
	public static int lseek(int fd, int offset, int whence) throws CException {
		return caller.verifyInt(() -> lib().lseek(fd, offset, whence), "lseek", fd, offset, whence);
	}

	/* fcntl.h */

	public static final int O_RDONLY = 0x0;
	public static final int O_WRONLY = 0x1;
	public static final int O_RDWR = 0x2;
	public static final int O_ACCMODE = 0x3;
	public static final int O_CREAT;
	public static final int O_EXCL;
	public static final int O_NOCTTY;
	public static final int O_TRUNC;
	public static final int O_APPEND;
	public static final int O_NONBLOCK;
	public static final int O_DSYNC;
	public static final int O_DIRECTORY;
	public static final int O_NOFOLLOW;
	public static final int O_CLOEXEC;

	/**
	 * Opens the path with flags, and returns a file descriptor.
	 */
	public static int open(String path, int flags) throws CException {
		return caller.verifyInt(() -> lib().open(path, flags),
			() -> caller.failMessage("open", path, OpenFlag.string(flags)));
	}

	/**
	 * Opens the path with flags and mode, and returns a file descriptor.
	 */
	public static int open(String path, int flags, int mode) throws CException {
		return caller.verifyInt(() -> lib().open(path, flags, (short) mode), () -> caller
			.failMessage("open", path, OpenFlag.string(flags), Integer.toOctalString(mode)));
	}

	/* ioctl.h */

	public static final int _IOC_SIZEBITS;
	private static final int _IOC_SIZEMASK;
	private static final int _IOC_VOID;
	private static final int _IOC_OUT = 0x40000000;
	private static final int _IOC_IN = 0x80000000;
	private static final int _IOC_INOUT = _IOC_IN | _IOC_OUT;

	/**
	 * <pre>
	 * |xxxxxxxx|xxxxxxxx|xxxxxxxx|xxxxxxxx| value
	 * |--------|--------|--------|--------|
	 * |xx000000|00000000|00000000|00000000| in/out (2)
	 * |  ?xxxxx|xxxxxxxx|        |        | size (14|13)
	 * |        |        |xxxxxxxx|        | group (8)
	 * |        |        |        |xxxxxxxx| num (8)
	 * </pre>
	 */
	public static int _IOC(int inOut, int group, int num, int size) {
		validateUbyte(group, "Group");
		validateUbyte(num, "Num");
		validateRange(size, 0, _IOC_SIZEMASK, "Size");
		return inOut | ((size & _IOC_SIZEMASK) << Short.SIZE) | (group << Byte.SIZE) | num;
	}

	public static int _IO(int group, int num) {
		return _IOC(_IOC_VOID, group, num, 0);
	}

	public static int _IOR(int group, int num, int size) {
		return _IOC(_IOC_IN, group, num, size); // sizeof(t)
	}

	public static int _IOW(int group, int num, int size) {
		return _IOC(_IOC_OUT, group, num, size); // sizeof(t)
	}

	public static int _IOWR(int group, int num, int size) {
		return _IOC(_IOC_INOUT, group, num, size); // sizeof(t)
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public static int ioctl(int fd, int request, Object... objs) throws CException {
		return ioctl("", fd, request, objs);
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public static int ioctl(String name, int fd, int request, Object... objs) throws CException {
		return caller.verifyInt(() -> lib().ioctl(fd, request, objs), "ioctl:" + name, fd, request,
			objs);
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public static int ioctl(Supplier<String> errorMsg, int fd, int request, Object... objs)
		throws CException {
		return caller.verifyInt(() -> lib().ioctl(fd, request, objs), errorMsg);
	}

	/* termios.h */

	public static int TCSANOW = 0;

	/**
	 * Retrieves termios attributes; the general terminal interface to control asynchronous
	 * communications ports.
	 */
	public static void tcgetattr(int fd, Pointer termios) throws CException {
		caller.verify(() -> lib().tcgetattr(fd, termios), "tcgetattr", fd, termios);
	}

	/**
	 * Set termios attributes.
	 */
	public static void tcsetattr(int fd, int actions, Pointer termios) throws CException {
		caller.verify(() -> lib().tcsetattr(fd, actions, termios), "tcsetattr", fd, actions,
			termios);
	}

	/* sys/ioctl.h */

	private static final int TIOCSBRK;
	private static final int TIOCCBRK;

	/**
	 * Set break bit.
	 */
	public static void tiocsbrk(int fd) throws CException {
		CLib.ioctl("TIOCSBRK", fd, TIOCSBRK);
	}

	/**
	 * Clear break bit.
	 */
	public static void tioccbrk(int fd) throws CException {
		CLib.ioctl("TIOCCBRK", fd, TIOCCBRK);
	}

	/* private */

	private static CLibNative lib() {
		return library.get();
	}

	/* os-specific initialization */

	static {
		if (OsUtil.IS_MAC) {
			/* fcntl.h */
			O_CREAT = 0x200;
			O_EXCL = 0x800;
			O_NOCTTY = 0x20000;
			O_TRUNC = 0x400;
			O_APPEND = 0x8;
			O_NONBLOCK = 0x4;
			O_DSYNC = 0x400000;
			O_DIRECTORY = 0x100000;
			O_NOFOLLOW = 0x100;
			O_CLOEXEC = 0x1000000;
			/* ioccom.h */
			_IOC_SIZEBITS = 13; // IOCPARM_MASK = 0x1fff;
			_IOC_SIZEMASK = (1 << _IOC_SIZEBITS) - 1;
			_IOC_VOID = 0x20000000;
			/* sys/ttycom.h */
			TIOCSBRK = _IO('t', 123); // 0x2000747b
			TIOCCBRK = _IO('t', 122); // 0x2000747a
		} else {
			/* fcntl.h */
			O_CREAT = 0x40;
			O_EXCL = 0x80;
			O_NOCTTY = 0x100;
			O_TRUNC = 0x200;
			O_APPEND = 0x400;
			O_NONBLOCK = 0x800;
			O_DSYNC = 0x1000;
			O_DIRECTORY = 0x4000;
			O_NOFOLLOW = 0x8000;
			O_CLOEXEC = 0x80000;
			/* ioctl.h */
			_IOC_SIZEBITS = 14;
			_IOC_SIZEMASK = (1 << _IOC_SIZEBITS) - 1;
			_IOC_VOID = 0;
			/* asm-generic/ioctls.h */
			TIOCSBRK = _IO('T', 0x27); // 0x5427
			TIOCCBRK = _IO('T', 0x28); // 0x5428
		}
	}
}
