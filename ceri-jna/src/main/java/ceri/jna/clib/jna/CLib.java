package ceri.jna.clib.jna;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import ceri.common.util.OsUtil;
import ceri.jna.clib.jna.CSignal.sighandler_t;
import ceri.jna.clib.jna.CUnistd.size_t;
import ceri.jna.clib.jna.CUnistd.ssize_t;
import ceri.jna.util.Caller;
import ceri.jna.util.JnaLibrary;

/**
 * Provides JNA access to native c-library functions, grouped by include header file. Separate
 * classes named after include header files provide specific constants and functions. Currently only
 * Linux and MacOSX are supported.
 */
public class CLib {
	public static final JnaLibrary<CLib.Native> library =
		JnaLibrary.of(Platform.C_LIBRARY_NAME, CLib.Native.class);
	static final Caller<CException> caller = Caller.of(CException::full);

	private CLib() {}

	/**
	 * A collection of common C library functions, grouped by header file.
	 */
	public static interface Native extends Library {

		/* <unistd.h> */

		// int close(int fd)
		int close(int fd) throws LastErrorException;

		// ssize_t read(int fd, void *buf, size_t count)
		ssize_t read(int fd, Pointer buffer, size_t len) throws LastErrorException;

		// ssize_t write(int fd, const void *buf, size_t count)
		ssize_t write(int fd, Pointer buffer, size_t len) throws LastErrorException;

		// off_t lseek(int fd, off_t offset, int whence)
		int lseek(int fd, int offset, int whence) throws LastErrorException;

		/* <signal.h> */

		// sighandler_t signal(int signum, sighandler_t handler)
		Pointer signal(int signum, sighandler_t handler) throws LastErrorException;

		// sighandler_t signal(int signum, sighandler_t handler)
		Pointer signal(int signum, Pointer handler) throws LastErrorException;

		// int raise(int sig)
		int raise(int sig) throws LastErrorException;

		/* <poll.h> */

		// extern int poll(struct pollfd *__fds, nfds_t __nfds, int __timeout);
		int poll(Pointer fds, int nfds, int timeout) throws LastErrorException;

		/* <fcntl.h> */

		// int open(const char *pathname, int flags, ...)
		int open(String path, int flags, Object... args) throws LastErrorException;

		// int fcntl(int fd, int cmd, ...);
		int fcntl(int fd, int cmd, Object... args) throws LastErrorException;

		/* <sys/ioctl.h> */

		// int ioctl(int d, unsigned long request, ...)
		int ioctl(int fd, NativeLong request, Object... objs) throws LastErrorException;

		/* <termios.h> */

		// int tcgetattr(int fd, struct termios *termios_p)
		int tcgetattr(int fd, Pointer termios) throws LastErrorException;

		// int tcsetattr(int fd, int optional_actions, const struct termios *termios_p)
		int tcsetattr(int fd, int optional_actions, Pointer termios) throws LastErrorException;

		// int tcsendbreak(int fd, int duration)
		int tcsendbreak(int fd, int duration) throws LastErrorException;

		// int tcdrain(int fd)
		int tcdrain(int fd) throws LastErrorException;

		// int tcflush(int fd, int queue_selector)
		int tcflush(int fd, int queue_selector) throws LastErrorException;

		// int tcflow(int fd, int action)
		int tcflow(int fd, int action) throws LastErrorException;

		// void cfmakeraw(struct termios *termios_p)
		void cfmakeraw(Pointer termios) throws LastErrorException;

		// speed_t cfgetispeed(const struct termios *termios_p)
		NativeLong cfgetispeed(Pointer termios) throws LastErrorException;

		// speed_t cfgetospeed(const struct termios *termios_p)
		NativeLong cfgetospeed(Pointer termios) throws LastErrorException;

		// int cfsetispeed(struct termios *termios_p, speed_t speed)
		int cfsetispeed(Pointer termios, NativeLong speed) throws LastErrorException;

		// int cfsetospeed(struct termios *termios_p, speed_t speed)
		int cfsetospeed(Pointer termios, NativeLong speed) throws LastErrorException;

		// int cfsetspeed(struct termios *termios_p, speed_t speed)
		int cfsetspeed(Pointer termios, NativeLong speed) throws LastErrorException;

		/* <stdlib.h> */

		// int setenv(const char *name, const char *value, int overwrite)
		int setenv(String name, String value, int overwrite) throws LastErrorException;

		// char *getenv(const char *name)
		String getenv(String name);
	}

	/**
	 * Throws an exception if this C-lib implementation does not support the current OS.
	 */
	public static void validateOs() {
		if (!OsUtil.IS_MAC && !OsUtil.IS_LINUX) throw OsUtil.unsupportedOs();
	}

	static CLib.Native lib() {
		return library.get();
	}
}
