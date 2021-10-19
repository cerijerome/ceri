package ceri.serial.clib.jna;

import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.common.validation.ValidationUtil.validateUbyte;
import java.util.function.Supplier;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.common.util.OsUtil;
import ceri.serial.clib.OpenFlag;
import ceri.serial.clib.jna.CLibNative.sighandler_t;
import ceri.serial.clib.jna.CLibNative.size_t;
import ceri.serial.jna.JnaLibrary;
import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.PointerUtil;
import ceri.serial.jna.Struct;
import ceri.serial.jna.Struct.Fields;

/**
 * Methods that call the native C-library, grouped by linux header file. Contains OS-specific
 * constants and calls, initialized statically, and through nested classes.
 */
public class CLib {
	public static final JnaLibrary<CLibNative> library =
		JnaLibrary.of(Platform.C_LIBRARY_NAME, CLibNative.class);
	private static final CCaller<CException> caller = CCaller.of();
	public static final int INVALID_FD = -1;

	private CLib() {}

	/* stdio.h */

	public static final int EOF = -1;
	public static final int SEEK_SET = 0;
	public static final int SEEK_CUR = 1;
	public static final int SEEK_END = 2;

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
	 * Reads up to length bytes from current position in file.
	 */
	public static byte[] read(int fd, int length) throws CException {
		Memory m = JnaUtil.malloc(length);
		int n = read(fd, m, length);
		if (n <= 0) return ArrayUtil.EMPTY_BYTE;
		return JnaUtil.bytes(m, 0, n);
	}

	/**
	 * Reads all bytes from current position in file.
	 */
	public static byte[] readAll(int fd) throws CException {
		return read(fd, size(fd));
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
	 * Writes bytes to file.
	 */
	public static void write(int fd, int... bytes) throws CException {
		write(fd, ArrayUtil.bytes(bytes));
	}

	/**
	 * Writes bytes to file.
	 */
	public static void write(int fd, byte[] bytes) throws CException {
		write(fd, bytes, 0);
	}

	/**
	 * Writes bytes to file.
	 */
	public static void write(int fd, byte[] bytes, int offset) throws CException {
		write(fd, bytes, offset, bytes.length - offset);
	}

	/**
	 * Writes bytes to file.
	 */
	public static void write(int fd, byte[] bytes, int offset, int length) throws CException {
		Memory m = JnaUtil.mallocBytes(bytes, offset, length);
		write(fd, m, length);
	}

	/**
	 * Moves the position of file descriptor. Returns the new position.
	 */
	public static int lseek(int fd, int offset, int whence) throws CException {
		return caller.verifyInt(() -> lib().lseek(fd, offset, whence), "lseek", fd, offset, whence);
	}

	/**
	 * Returns the current position in the file.
	 */
	public static int position(int fd) throws CException {
		return lseek(fd, 0, SEEK_CUR);
	}

	/**
	 * Sets the current position in the file.
	 */
	public static void position(int fd, int position) throws CException {
		int n = lseek(fd, position, SEEK_SET);
		if (n != position)
			throw CException.general("Unable to set position on %d: %d", fd, position);
	}

	/**
	 * Returns the file size by moving to end of file then back to original position.
	 */
	public static int size(int fd) throws CException {
		int pos = position(fd);
		int size = lseek(fd, 0, SEEK_END);
		position(fd, pos);
		return size;
	}

	/* signal.h */

	public static final int SIGHUP = 1;
	public static final int SIGINT = 2;
	public static final int SIGQUIT = 3;
	public static final int SIGILL = 4;
	public static final int SIGTRAP = 5;
	public static final int SIGABRT = 6;
	public static final int SIGIOT = 6;
	public static final int SIGBUS;
	public static final int SIGFPE = 8;
	public static final int SIGKILL = 9;
	public static final int SIGUSR1;
	public static final int SIGSEGV = 11;
	public static final int SIGUSR2;
	public static final int SIGPIPE = 13;
	public static final int SIGALRM = 14;
	public static final int SIGTERM = 15;

	public static final int SIG_DFL = 0;
	public static final int SIG_IGN = 1;
	public static final int SIG_ERR = -1;

	/**
	 * Sets a signal handler. Returns true if the result is not SIG_ERR.
	 */
	public static boolean signal(int signum, sighandler_t handler) throws CException {
		Pointer p = caller.callType(() -> lib().signal(signum, handler), "signal", signum, handler);
		return PointerUtil.peer(p) != SIG_ERR;
	}

	/**
	 * Sets a standard signal handler SIG_DFL or SIG_IGN. Returns true if the result is not SIG_ERR.
	 */
	public static boolean signal(int signum, int handler) throws CException {
		if (handler < SIG_DFL || handler > SIG_IGN)
			throw CException.of(CError.EINVAL, "Only SIG_DFL or SIG_IGN allowed: %d", handler);
		Pointer p = caller.callType(() -> lib().signal(signum, new Pointer(handler)), "signal",
			signum, handler);
		return PointerUtil.peer(p) != SIG_ERR;
	}

	public static void raise(int sig) throws CException {
		caller.verify(() -> lib().raise(sig), "raise", sig);
	}

	/* poll.h */

	public static final int POLLIN = 0x0001;
	public static final int POLLPRI = 0x0002;
	public static final int POLLOUT = 0x0004;
	public static final int POLLERR = 0x0008;
	public static final int POLLHUP = 0x0010;
	public static final int POLLNVAL = 0x0020;
	public static final int POLLRDNORM = 0x0040;
	public static final int POLLRDBAND = 0x0080;
	public static final int POLLWRNORM;
	public static final int POLLWRBAND;

	/**
	 * Data structure for a polling request.
	 */
	@Fields({ "fd", "events", "revents" })
	public static class pollfd extends Struct {
		public int fd; /* File descriptor to poll. */
		public short events; /* Types of events poller cares about. */
		public short revents; /* Types of events that actually occurred. */

		public static pollfd[] array(int size) {
			return Struct.arrayByVal(() -> new pollfd(), pollfd[]::new, size);
		}

		public pollfd() {}

		public pollfd(Pointer p) {
			super(p);
		}
	}

	/**
	 * Examines a set of file descriptors to see if some of them are ready for I/O or if certain
	 * events have occurred on them. A timeoutMs of -1 blocks until an event occurs. Returns the
	 * number of descriptors with returned events.
	 */
	public static int poll(pollfd[] fds, int timeout) throws CException {
		if (fds.length > 0 && !Struct.isByVal(fds))
			throw CException.full("Array is not contiguous", CError.EINVAL);
		Pointer p = Struct.pointer(fds);
		int n = caller.verifyInt(() -> lib().poll(p, fds.length, timeout), "poll", p, fds.length,
			timeout);
		if (n > 0) Struct.read(fds, "revents");
		return n;
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

	/**
	 * Checks a file descriptor validity
	 */
	public static boolean validFd(int fd) {
		return fd != INVALID_FD;
	}

	/**
	 * Validates a file descriptor
	 */
	public static int validateFd(int fd) throws CException {
		if (validFd(fd)) return fd;
		throw CException.of(fd, "Invalid file descriptor");
	}

	/* ioctl.h */

	public static final int _IOC_SIZEBITS;
	private static final int _IOC_SIZEMASK;
	private static final int IOC_VOID;
	private static final int IOC_OUT;
	private static final int IOC_IN;

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
		return _IOC(IOC_VOID, group, num, 0);
	}

	public static int _IOR(int group, int num, int size) {
		return _IOC(IOC_OUT, group, num, size); // sizeof(t)
	}

	public static int _IOW(int group, int num, int size) {
		return _IOC(IOC_IN, group, num, size); // sizeof(t)
	}

	public static int _IOWR(int group, int num, int size) {
		return _IOC(IOC_IN | IOC_OUT, group, num, size); // sizeof(t)
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

	static final int TIOCSBRK;
	static final int TIOCCBRK;

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

	/* os-specific types and calls */

	/**
	 * Types and calls specific to Mac.
	 */
	public static class Mac {
		private Mac() {}

		/* ioss.h */

		static final int IOSSIOSPEED = _IOW('T', 2, NativeLong.SIZE); // 0x80085402

		/**
		 * Sets input and output speeds to a non-traditional baud rate.
		 */
		public static void iossiospeed(int fd, int baud) throws CException {
			CLib.ioctl("IOSSIOSPEED", fd, IOSSIOSPEED, baud);
		}

		/* termios.h */

		/**
		 * General terminal interface to control asynchronous communications ports.
		 */
		@Fields({ "c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_cc", "c_ispeed", "c_ospeed" })
		public static class termios extends Struct {
			private static final int NCCS = 20;
			public NativeLong c_iflag; // input flags
			public NativeLong c_oflag; // output flags
			public NativeLong c_cflag; // control flags
			public NativeLong c_lflag; // local flags
			public byte[] c_cc = new byte[NCCS]; // control chars
			public NativeLong c_ispeed; // input speed
			public NativeLong c_ospeed; // output speed
		}

		/**
		 * Get termios attributes.
		 */
		public static termios tcgetattr(int fd) throws CException {
			termios termios = new termios();
			CLib.tcgetattr(fd, termios.getPointer());
			return Struct.read(termios);
		}

		/**
		 * Set termios attributes.
		 */
		public static void tcsetattr(int fd, int actions, termios termios) throws CException {
			CLib.tcsetattr(fd, actions, Struct.write(termios).getPointer());
		}
	}

	/**
	 * Types and calls specific to Linux.
	 */
	public static class Linux {
		private Linux() {}

		/* termios.h */

		/**
		 * General terminal interface to control asynchronous communications ports.
		 */
		@Fields({ "c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_line", "c_cc", "c_ispeed",
			"c_ospeed" })
		public static class termios extends Struct {
			private static final int NCCS = 32;
			public NativeLong c_iflag; // input mode flags
			public NativeLong c_oflag; // output mode flags
			public NativeLong c_cflag; // control mode flags
			public NativeLong c_lflag; // local mode flags
			public NativeLong c_line; // line discipline
			public byte[] c_cc = new byte[NCCS]; // control characters
			public NativeLong c_ispeed; // input speed
			public NativeLong c_ospeed; // output speed
		}

		/**
		 * Populate termios attributes.
		 */
		public static termios tcgetattr(int fd) throws CException {
			termios termios = new termios();
			CLib.tcgetattr(fd, termios.getPointer());
			return Struct.read(termios);
		}

		/**
		 * Set termios attributes.
		 */
		public static void tcsetattr(int fd, int actions, termios termios) throws CException {
			CLib.tcsetattr(fd, actions, Struct.write(termios).getPointer());
		}
	}

	/* private */

	private static CLibNative lib() {
		return library.get();
	}

	/* os-specific initialization */

	static {
		if (OsUtil.IS_MAC) {
			/* signal.h */
			SIGBUS = 10;
			SIGUSR1 = 30;
			SIGUSR2 = 31;
			/* poll.h */
			POLLWRNORM = 0x4;
			POLLWRBAND = 0x100;
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
			IOC_VOID = 0x20000000;
			IOC_OUT = 0x40000000;
			IOC_IN = 0x80000000;
			/* sys/ttycom.h */
			TIOCSBRK = _IO('t', 123); // 0x2000747b
			TIOCCBRK = _IO('t', 122); // 0x2000747a
		} else {
			/* signal.h */
			SIGBUS = 7;
			SIGUSR1 = 10;
			SIGUSR2 = 12;
			/* poll.h */
			POLLWRNORM = 0x100;
			POLLWRBAND = 0x200;
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
			IOC_VOID = 0;
			IOC_OUT = 0x80000000;
			IOC_IN = 0x40000000;
			/* asm-generic/ioctls.h */
			TIOCSBRK = _IO('T', 0x27); // 0x5427
			TIOCCBRK = _IO('T', 0x28); // 0x5428
		}
	}
}
