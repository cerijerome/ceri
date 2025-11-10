package ceri.ffm.clib.ffm;

import ceri.common.util.Os;
import ceri.ffm.core.LastErrorException;
import ceri.ffm.core.Library;
import ceri.ffm.util.Caller;

/**
 * Provides FFM access to native c-library functions, grouped by include header file. Separate
 * classes named after include header files provide specific constants and functions. Currently only
 * Linux and MacOSX are supported.
 */
public class CLib {
	// public static final JnaLibrary<CLib.Native> library =
	// JnaLibrary.of(Platform.C_LIBRARY_NAME, CLib.Native.class);
	public static final Library<CLib.Native> library = Library.of(CLib.Native.class);
	static final Caller<CException> caller = Caller.of(CException::full);

	private CLib() {}

	/**
	 * A collection of common C library functions, grouped by header file.
	 */
	public static interface Native {

		// <unistd.h>

		// int close(int fd)
		int close(int fd) throws LastErrorException;

		// int isatty(int fd)
		int isatty(int fd); // no ErrNoException; errno is set for 0 case

		// int pipe(int pipefd[2]);
		// int pipe(int[] pipefd) throws LastErrorException;

		// ssize_t read(int fd, void *buf, size_t count)
		// ssize_t read(int fd, Pointer buffer, size_t len) throws LastErrorException;

		// ssize_t write(int fd, const void *buf, size_t count)
		// ssize_t write(int fd, Pointer buffer, size_t len) throws LastErrorException;

		// off_t lseek(int fd, off_t offset, int whence)
		// int lseek(int fd, int offset, int whence) throws LastErrorException;

		// int getpagesize(void)
		// int getpagesize() throws LastErrorException;

		// <signal.h>

		// sighandler_t signal(int signum, sighandler_t handler)
		// Pointer signal(int signum, sighandler_t handler) throws LastErrorException;

		// sighandler_t signal(int signum, sighandler_t handler)
		// Pointer signal(int signum, Pointer handler) throws LastErrorException;

		// int raise(int sig)
		// int raise(int sig); // ignore LastErrorException

		// int sigemptyset(sigset_t *set);
		// int sigemptyset(Pointer set) throws LastErrorException;

		// int sigaddset(sigset_t *set, int signum);
		// int sigaddset(Pointer set, int signum) throws LastErrorException;

		// int sigdelset(sigset_t *set, int signum);
		// int sigdelset(Pointer set, int signum) throws LastErrorException;

		// int sigismember(const sigset_t *set, int signum);
		// int sigismember(Pointer set, int signum) throws LastErrorException;

		// <poll.h>

		// int poll(struct pollfd *__fds, nfds_t __nfds, int __timeout);
		// int poll(Pointer fds, int nfds, int timeout) throws LastErrorException;

		// int ppoll(struct pollfd *fds, nfds_t nfds, const struct timespec * tmo_p,
		// const sigset_t * sigmask);
		// int ppoll(Pointer fds, int nfds, Pointer tmo_p, Pointer sigmask);

		// <fcntl.h>

		// int open(const char *pathname, int flags, ...)
		int open(String path, int flags, Object... args) throws LastErrorException;

		// int fcntl(int fd, int cmd, ...);
		int fcntl(int fd, int cmd, Object... args) throws LastErrorException;

		// <sys/ioctl.h>

		// int ioctl(int d, unsigned long request, ...)
		// int ioctl(int fd, CUlong request, Object... objs) throws LastErrorException;

		// <termios.h>

		// int tcgetattr(int fd, struct termios *termios_p)
		// int tcgetattr(int fd, Pointer termios) throws LastErrorException;

		// int tcsetattr(int fd, int optional_actions, const struct termios *termios_p)
		// int tcsetattr(int fd, int optional_actions, Pointer termios) throws LastErrorException;

		// int tcsendbreak(int fd, int duration)
		// int tcsendbreak(int fd, int duration) throws LastErrorException;

		// int tcdrain(int fd)
		// int tcdrain(int fd) throws LastErrorException;

		// int tcflush(int fd, int queue_selector)
		// int tcflush(int fd, int queue_selector) throws LastErrorException;

		// int tcflow(int fd, int action)
		// int tcflow(int fd, int action) throws LastErrorException;

		// void cfmakeraw(struct termios *termios_p)
		// void cfmakeraw(Pointer termios) throws LastErrorException;

		// speed_t cfgetispeed(const struct termios *termios_p)
		// speed_t cfgetispeed(Pointer termios) throws LastErrorException;

		// speed_t cfgetospeed(const struct termios *termios_p)
		// speed_t cfgetospeed(Pointer termios) throws LastErrorException;

		// int cfsetispeed(struct termios *termios_p, speed_t speed)
		// int cfsetispeed(Pointer termios, speed_t speed) throws LastErrorException;

		// int cfsetospeed(struct termios *termios_p, speed_t speed)
		// int cfsetospeed(Pointer termios, speed_t speed) throws LastErrorException;

		// <sys/mman.h>

		// void *mmap(void *addr, size_t len, int prot, int flags, int fd, off_t offset)
		// Pointer mmap(Pointer addr, size_t len, int prot, int flags, int fd, int offset)
		// throws LastErrorException;

		// int munmap (void *addr, size_t len)
		// int munmap(Pointer addr, size_t len) throws LastErrorException;

		// <stdlib.h>

		// int setenv(const char *name, const char *value, int overwrite)
		int setenv(String name, String value, int overwrite) throws LastErrorException;

		// char *getenv(const char *name)
		String getenv(String name);

		// <string.h>

		// String strerror(int errNo);
	}

	/**
	 * Throws an exception if this C-lib implementation does not support the current OS.
	 */
	public static Os.Info validateOs() {
		var os = Os.info();
		if (os.mac || os.linux) return os;
		throw new UnsupportedOperationException("Not supported: " + os);
	}

	protected static CLib.Native lib() {
		return library.get();
	}
}
