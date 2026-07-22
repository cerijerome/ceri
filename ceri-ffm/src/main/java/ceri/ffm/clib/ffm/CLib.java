package ceri.ffm.clib.ffm;

import ceri.common.util.Os;
import ceri.ffm.core.Caller;
import ceri.ffm.core.Library;
import ceri.ffm.reflect.Refine.LastError;
import ceri.ffm.reflect.Refine.Out;
import ceri.ffm.type.IntType.CUlong;
import ceri.ffm.type.IntType.size_t;
import ceri.ffm.type.IntType.ssize_t;
import ceri.ffm.type.Pointer;

/**
 * Provides FFM access to native c-library functions, grouped by include header file. Separate
 * classes named after include header files provide specific constants and functions. Currently only
 * Linux and MacOSX are supported.
 */
public class CLib {
	public static final Library<CLib.Native> library = Library.of(CLib.Native.class);
	public static final Caller<CException, CLib.Native> caller =
		Caller.config(CException::full).caller(library);

	private CLib() {}

	/**
	 * A collection of common C library functions, grouped by header file.
	 */
	public static interface Native {

		// <unistd.h>

		// int close(int fd)
		int close(int fd);

		// int isatty(int fd)
		int isatty(int fd); // errno is set on 0

		// int pipe(int pipefd[2]);
		int pipe(@Out int[] pipefd);

		// ssize_t read(int fd, void *buf, size_t count)
		ssize_t read(int fd, Pointer<?> buffer, size_t len);

		// ssize_t write(int fd, const void *buf, size_t count)
		ssize_t write(int fd, Pointer<?> buffer, size_t len);

		// off_t lseek(int fd, off_t offset, int whence)
		int lseek(int fd, int offset, int whence);

		// int getpagesize(void)
		@LastError(false) // errno not set
		int getpagesize();

		// <signal.h>

		// sighandler_t signal(int signum, sighandler_t handler)
		// Pointer signal(int signum, sighandler_t handler);

		// sighandler_t signal(int signum, sighandler_t handler)
		// Pointer signal(int signum, Pointer handler);

		// int raise(int sig)
		// int raise(int sig); // ignore errno

		// int sigemptyset(sigset_t *set);
		// int sigemptyset(Pointer set);

		// int sigaddset(sigset_t *set, int signum);
		// int sigaddset(Pointer set, int signum);

		// int sigdelset(sigset_t *set, int signum);
		// int sigdelset(Pointer set, int signum);

		// int sigismember(const sigset_t *set, int signum);
		// int sigismember(Pointer set, int signum);

		// <poll.h>

		// int poll(struct pollfd *__fds, nfds_t __nfds, int __timeout);
		// int poll(Pointer fds, int nfds, int timeout);

		// int ppoll(struct pollfd *fds, nfds_t nfds, const struct timespec * tmo_p,
		// const sigset_t * sigmask);
		// int ppoll(Pointer fds, int nfds, Pointer tmo_p, Pointer sigmask);

		// <fcntl.h>

		// int open(const char *pathname, int flags, ...)
		int open(String path, int flags, Object... args);

		// int fcntl(int fd, int cmd, ...);
		int fcntl(int fd, int cmd, Object... args);

		// <sys/ioctl.h>

		// int ioctl(int d, unsigned long request, ...)
		int ioctl(int fd, CUlong request, Object... objs);

		// <termios.h>

		// int tcgetattr(int fd, struct termios *termios_p)
		// int tcgetattr(int fd, Pointer termios);

		// int tcsetattr(int fd, int optional_actions, const struct termios *termios_p)
		// int tcsetattr(int fd, int optional_actions, Pointer termios);

		// int tcsendbreak(int fd, int duration)
		// int tcsendbreak(int fd, int duration);

		// int tcdrain(int fd)
		// int tcdrain(int fd);

		// int tcflush(int fd, int queue_selector)
		// int tcflush(int fd, int queue_selector);

		// int tcflow(int fd, int action)
		// int tcflow(int fd, int action);

		// void cfmakeraw(struct termios *termios_p)
		// void cfmakeraw(Pointer termios);

		// speed_t cfgetispeed(const struct termios *termios_p)
		// speed_t cfgetispeed(Pointer termios);

		// speed_t cfgetospeed(const struct termios *termios_p)
		// speed_t cfgetospeed(Pointer termios);

		// int cfsetispeed(struct termios *termios_p, speed_t speed)
		// int cfsetispeed(Pointer termios, speed_t speed);

		// int cfsetospeed(struct termios *termios_p, speed_t speed)
		// int cfsetospeed(Pointer termios, speed_t speed);

		// <sys/mman.h>

		// void *mmap(void *addr, size_t len, int prot, int flags, int fd, off_t offset)
		// Pointer mmap(Pointer addr, size_t len, int prot, int flags, int fd, int offset)
		// ;

		// int munmap (void *addr, size_t len)
		// int munmap(Pointer addr, size_t len);

		// <stdlib.h>

		// int setenv(const char *name, const char *value, int overwrite)
		int setenv(String name, String value, int overwrite);

		// char *getenv(const char *name)
		@LastError(false) // errno not set
		String getenv(String name);

		// <string.h>

		@LastError(false) // don't capture errno
		String strerror(int errnum);
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
