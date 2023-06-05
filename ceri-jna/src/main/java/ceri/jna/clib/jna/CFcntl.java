package ceri.jna.clib.jna;

import static ceri.jna.clib.jna.CLib.caller;
import static ceri.jna.clib.jna.CLib.lib;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import ceri.common.util.OsUtil;
import ceri.jna.clib.OpenFlag;

/**
 * Types and functions from {@code <fcntl.h>}
 */
public class CFcntl {
	public static final int INVALID_FD = -1;
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
	public static final int O_ASYNC;
	public static final int O_DIRECTORY;
	public static final int O_NOFOLLOW;
	public static final int O_CLOEXEC;
	public static final int O_SYNC;
	static final int F_DUPFD = 0;
	static final int F_GETFD = 1;
	static final int F_SETFD = 2;
	static final int F_GETFL = 3;
	static final int F_SETFL = 4;

	private CFcntl() {}

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
		return caller.verifyInt(() -> lib().open(path, flags, mode), () -> caller
			.failMessage("open", path, OpenFlag.string(flags), Integer.toOctalString(mode)));
	}

	/**
	 * Checks a file descriptor validity
	 */
	public static boolean validFd(int fd) {
		return fd >= 0;
	}

	/**
	 * Validates a file descriptor
	 */
	public static int validateFd(int fd) throws CException {
		if (validFd(fd)) return fd;
		throw CException.of(fd, "Invalid file descriptor");
	}

	/**
	 * Performs a fcntl function. Arguments and return value depend on the function.
	 */
	public static int fcntl(int fd, int command, Object... objs) throws CException {
		return fcntl("", fd, command, objs);
	}

	/**
	 * Performs a fcntl function. Arguments and return value depend on the function.
	 */
	public static int fcntl(String name, int fd, int command, Object... objs) throws CException {
		return caller.verifyInt(() -> lib().fcntl(fd, command, objs), "fcntl:" + name, fd, command,
			objs);
	}

	/**
	 * Performs a fcntl function. Arguments and return value depend on the function.
	 */
	public static int fcntl(Supplier<String> errorMsg, int fd, int command, Object... objs)
		throws CException {
		return caller.verifyInt(() -> lib().fcntl(fd, command, objs), errorMsg);
	}

	/**
	 * Duplicate the file descriptor using the lowest-numbered available >= min.
	 */
	public static int dupFd(int fd, int min) throws CException {
		return fcntl("F_DUPFD", fd, F_DUPFD, min);
	}

	/**
	 * Get the file descriptor flags.
	 */
	public static int getFd(int fd) throws CException {
		return fcntl("F_GETFD", fd, F_GETFD);
	}

	/**
	 * Set the file descriptor flags.
	 */
	public static void setFd(int fd, int flags) throws CException {
		fcntl("F_SETFD", fd, F_SETFD, flags);
	}

	/**
	 * Get the file access mode and file status flags.
	 */
	public static int getFl(int fd) throws CException {
		return fcntl("F_GETFL", fd, F_GETFL);
	}

	/**
	 * Set the file status flags.
	 */
	public static void setFl(int fd, int flags) throws CException {
		fcntl("F_SETFL", fd, F_SETFL, flags);
	}

	/**
	 * Update file status flags using an int function. Returns the new flags value.
	 */
	public static int setFl(int fd, IntUnaryOperator flagFn) throws CException {
		int flags = flagFn.applyAsInt(getFl(fd));
		setFl(fd, flags);
		return flags;
	}

	/* os-specific initialization */

	static {
		if (OsUtil.os().mac) {
			O_CREAT = 0x200;
			O_EXCL = 0x800;
			O_NOCTTY = 0x20000;
			O_TRUNC = 0x400;
			O_APPEND = 0x8;
			O_NONBLOCK = 0x4;
			O_DSYNC = 0x400000;
			O_ASYNC = 0x40;
			O_DIRECTORY = 0x100000;
			O_NOFOLLOW = 0x100;
			O_CLOEXEC = 0x1000000;
			O_SYNC = 0x80;
		} else {
			O_CREAT = 0x40;
			O_EXCL = 0x80;
			O_NOCTTY = 0x100;
			O_TRUNC = 0x200;
			O_APPEND = 0x400;
			O_NONBLOCK = 0x800;
			O_DSYNC = 0x1000;
			O_ASYNC = 0x2000;
			O_DIRECTORY = 0x4000;
			O_NOFOLLOW = 0x8000;
			O_CLOEXEC = 0x80000;
			O_SYNC = 0x100000 | O_DSYNC;
		}
	}

}
