package ceri.jna.clib.jna;

import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.common.validation.ValidationUtil.validateUbyte;
import java.util.function.Supplier;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.common.util.OsUtil;
import ceri.jna.clib.OpenFlag;
import ceri.jna.clib.jna.CLibNative.sighandler_t;
import ceri.jna.clib.jna.CLibNative.size_t;
import ceri.jna.util.JnaLibrary;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.PointerUtil;
import ceri.jna.util.Struct;
import ceri.jna.util.Struct.Fields;

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

	public static void validateOs() {
		if (!OsUtil.IS_MAC && !OsUtil.IS_LINUX) throw OsUtil.unsupportedOs();
	}

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
		try (Memory m = JnaUtil.malloc(length)) {
			int n = read(fd, m, length);
			if (n <= 0) return ArrayUtil.EMPTY_BYTE;
			return JnaUtil.bytes(m, 0, n);
		}
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
		try (Memory m = JnaUtil.mallocBytes(bytes, offset, length)) {
			write(fd, m, length);
		}
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
		return caller.verifyInt(() -> lib().open(path, flags, mode), () -> caller
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

	public static final int F_DUPFD = 0;
	public static final int F_GETFD = 1;
	public static final int F_SETFD = 2;
	public static final int F_GETFL = 3;
	public static final int F_SETFL = 4;

	/**
	 * Performs a fcntl function. Arguments and return value depend on the function.
	 */
	public static int fcntl(int fd, int request, Object... objs) throws CException {
		return fcntl("", fd, request, objs);
	}

	/**
	 * Performs a fcntl function. Arguments and return value depend on the function.
	 */
	public static int fcntl(String name, int fd, int request, Object... objs) throws CException {
		return caller.verifyInt(() -> lib().fcntl(fd, request, objs), "fcntl:" + name, fd, request,
			objs);
	}

	/**
	 * Performs a fcntl function. Arguments and return value depend on the function.
	 */
	public static int fcntl(Supplier<String> errorMsg, int fd, int request, Object... objs)
		throws CException {
		return caller.verifyInt(() -> lib().fcntl(fd, request, objs), errorMsg);
	}

	/* sys/ioctl.h */

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
		var n = JnaUtil.unlong(request);
		return caller.verifyInt(() -> lib().ioctl(fd, n, objs), "ioctl:" + name, fd, request, objs);
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public static int ioctl(Supplier<String> errorMsg, int fd, int request, Object... objs)
		throws CException {
		var n = JnaUtil.unlong(request);
		return caller.verifyInt(() -> lib().ioctl(fd, n, objs), errorMsg);
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

	/* termios.h */

	public static final int IXON;
	public static final int IXOFF;
	public static final int IXANY;
	public static final int OPOST = 0x0001;
	public static final int CSIZE;
	public static final int CS5 = 0x0000;
	public static final int CS6;
	public static final int CS7;
	public static final int CS8;
	public static final int CSTOPB;
	public static final int CREAD;
	public static final int PARENB;
	public static final int CMSPAR;
	public static final int PARODD;
	public static final int CLOCAL;
	public static final int CRTSCTS;
	public static final int ISIG;
	public static final int ICANON;
	public static final int ECHO = 0x0008;
	public static final int ECHOE;
	public static final int VEOF;
	public static final int VSTART;
	public static final int VSTOP;
	public static final int VMIN;
	public static final int VTIME;
	public static final int TCSANOW = 0;
	public static final int TCSADRAIN = 1;
	public static final int TCSAFLUSH = 2;
	public static final int B0;
	public static final int B50;
	public static final int B75;
	public static final int B110;
	public static final int B134;
	public static final int B150;
	public static final int B200;
	public static final int B300;
	public static final int B600;
	public static final int B1200;
	public static final int B1800;
	public static final int B2400;
	public static final int B4800;
	public static final int B9600;
	public static final int B19200;
	public static final int B38400;
	public static final int B57600;
	public static final int B115200;
	public static final int B230400;
	public static final int B460800;
	public static final int B500000;
	public static final int B576000;
	public static final int B921600;
	public static final int B1000000;
	public static final int B1152000;
	public static final int B1500000;
	public static final int B2000000;
	public static final int B2500000;
	public static final int B3000000;
	public static final int B3500000;
	public static final int B4000000;

	public static abstract class termios extends Struct {}

	/**
	 * Populates termios attributes; the general terminal interface to control asynchronous
	 * communications ports.
	 */
	public static <T extends termios> T tcgetattr(int fd, T termios) throws CException {
		Pointer p = termios.getPointer();
		caller.verify(() -> lib().tcgetattr(fd, p), "tcgetattr", fd, p);
		return Struct.read(termios);
	}

	/**
	 * Set termios attributes on the terminal.
	 */
	public static void tcsetattr(int fd, int actions, termios termios) throws CException {
		Pointer p = Struct.write(termios).getPointer();
		caller.verify(() -> lib().tcsetattr(fd, actions, p), "tcsetattr", fd, actions, p);
	}

	/**
	 * Configures raw mode; input is available char by char. Call <code>Struct.write(termios)</code>
	 * first if memory is out of date.
	 */
	public static void cfmakeraw(termios termios) throws CException {
		Pointer p = termios.getPointer();
		caller.call(() -> lib().cfmakeraw(p), "cfmakeraw", p);
		Struct.read(termios);
	}

	/**
	 * Returns the input baud rate stored in the termios structure. Call
	 * <code>Struct.write(termios)</code> first if memory is out of date.
	 */
	public static int cfgetispeed(termios termios) throws CException {
		Pointer p = termios.getPointer();
		return caller.callType(() -> lib().cfgetispeed(p), "cfgetispeed", p).intValue();
	}

	/**
	 * Sets the input baud rate in the termios structure. Call <code>Struct.write(termios)</code>
	 * first if memory is out of date.
	 */
	public static void cfsetispeed(termios termios, int speed) throws CException {
		Pointer p = termios.getPointer();
		NativeLong n = JnaUtil.unlong(speed);
		caller.verify(() -> lib().cfsetispeed(p, n), "cfsetispeed", p, n);
		Struct.read(termios);
	}

	/**
	 * Returns the output baud rate stored in the termios structure. Call
	 * <code>Struct.write(termios)</code> first if memory is out of date.
	 */
	public static long cfgetospeed(termios termios) throws CException {
		Pointer p = termios.getPointer();
		return caller.callType(() -> lib().cfgetospeed(p), "cfgetospeed", p).longValue();
	}

	/**
	 * Sets the output baud rate in the termios structure. Call <code>Struct.write(termios)</code>
	 * first if memory is out of date.
	 */
	public static void cfsetospeed(termios termios, int speed) throws CException {
		Pointer p = termios.getPointer();
		NativeLong n = JnaUtil.unlong(speed);
		caller.verify(() -> lib().cfsetospeed(p, n), "cfsetospeed", p, n);
		Struct.read(termios);
	}

	/* stdlib.h */

	/**
	 * Change or add an environment variable.
	 */
	public static void setenv(String name, String value, boolean overwrite) throws CException {
		int overwriteValue = overwrite ? 1 : 0;
		caller.verify(() -> lib().setenv(name, value, overwriteValue), name, value, overwriteValue);
	}

	/**
	 * Get an environment variable.
	 */
	public static String getenv(String name) throws CException {
		return caller.callType(() -> lib().getenv(name), "getenv", name);
	}

	/* os-specific types and calls */

	/**
	 * Types and calls specific to Mac.
	 */
	public static final class Mac {
		private Mac() {}

		/* termios.h */

		/**
		 * General terminal interface to control asynchronous communications ports.
		 */
		@Fields({ "c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_cc", "c_ispeed", "c_ospeed" })
		public static class termios extends CLib.termios {
			private static final int NCCS = 20;
			public NativeLong c_iflag; // input flags
			public NativeLong c_oflag; // output flags
			public NativeLong c_cflag; // control flags
			public NativeLong c_lflag; // local flags
			public byte[] c_cc = new byte[NCCS]; // control chars
			public NativeLong c_ispeed; // input speed
			public NativeLong c_ospeed; // output speed
		}

		/* IOKit/serial/ioss.h */

		static final int IOSSIOSPEED = CLib._IOW('T', 2, NativeLong.SIZE); // 0x80085402

		/**
		 * Sets input and output speeds to a non-traditional baud rate. Value is not represented in
		 * struct termios.
		 */
		public static void iossiospeed(int fd, int speed) throws CException {
			var p = JnaUtil.unlongRefPtr(speed);
			CLib.ioctl("IOSSIOSPEED", fd, IOSSIOSPEED, p);
		}
	}

	/**
	 * Types and calls specific to Linux.
	 */
	public static final class Linux {
		private Linux() {}

		/* asm/ioctls.h */

		static final int TIOCGSERIAL = CLib._IO('T', 0x1e); // 0x541E;
		static final int TIOCSSERIAL = CLib._IO('T', 0x1f); // 0x541F;

		/* linux/serial.h */

		public static final int ASYNC_SPD_HI = 1 << 4; // 0x0010; use 56000bps
		public static final int ASYNC_SPD_VHI = 1 << 5; // 0x0020; use 115200bps
		public static final int ASYNC_SPD_SHI = 1 << 12; // 0x1000; use 230400bps
		public static final int ASYNC_SPD_CUST = ASYNC_SPD_HI | ASYNC_SPD_VHI;
		public static final int ASYNC_SPD_MASK = ASYNC_SPD_HI | ASYNC_SPD_VHI | ASYNC_SPD_SHI;

		@Fields({ "type", "line", "port", "irq", "flags", "xmit_fifo_size", "custom_divisor",
			"baud_base", "close_delay", "io_type", "reserved_char", "hub6", "closing_wait",
			"closing_wait2", "iomem_base", "iomem_reg_shift", "port_high", "iomap_base" })
		public static class serial_struct extends Struct {
			public int type;
			public int line;
			public int port;
			public int irq;
			public int flags;
			public int xmit_fifo_size;
			public int custom_divisor;
			public int baud_base;
			public short close_delay;
			public byte io_type;
			public byte reserved_char;
			public int hub6;
			public short closing_wait;
			public short closing_wait2;
			public Pointer iomem_base;
			public short iomem_reg_shift;
			public int port_high;
			public NativeLong iomap_base;
		}

		public static serial_struct tiocgserial(int fd) throws CException {
			var serial = new serial_struct();
			CLib.ioctl(fd, TIOCGSERIAL, serial); // Struct.read(serial) ?
			return serial;
		}

		public static void tiocsserial(int fd, serial_struct serial) throws CException {
			CLib.ioctl(fd, TIOCSSERIAL, serial); // Struct.write(serial) ?
		}

		/* termios.h */

		/**
		 * General terminal interface to control asynchronous communications ports.
		 */
		@Fields({ "c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_line", "c_cc", "c_ispeed",
			"c_ospeed" })
		public static class termios extends CLib.termios {
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
			TIOCSBRK = CLib._IO('t', 123); // 0x2000747b
			TIOCCBRK = CLib._IO('t', 122); // 0x2000747a
			/* termios.h */
			IXON = 0x0200;
			IXOFF = 0x0400;
			IXANY = 0x0800;
			CSIZE = 0x0300;
			CS6 = 0x0100;
			CS7 = 0x0200;
			CS8 = 0x0300;
			CSTOPB = 0x0400;
			CREAD = 0x0800;
			PARENB = 0x1000;
			CMSPAR = 0; // not supported
			PARODD = 0x2000;
			CLOCAL = 0x8000;
			CRTSCTS = 0x30000;
			ISIG = 0x0080;
			ICANON = 0x0100;
			ECHOE = 0x0002;
			VEOF = 0;
			VSTART = 12;
			VSTOP = 13;
			VMIN = 16;
			VTIME = 17;
			B0 = 0;
			B50 = 50;
			B75 = 75;
			B110 = 110;
			B134 = 134;
			B150 = 150;
			B200 = 200;
			B300 = 300;
			B600 = 600;
			B1200 = 1200;
			B1800 = 1800;
			B2400 = 2400;
			B4800 = 4800;
			B9600 = 9600;
			B19200 = 19200;
			B38400 = 38400;
			B57600 = 57600;
			B115200 = 115200;
			B230400 = 230400;
			B460800 = 460800;
			B500000 = 500000;
			B576000 = 576000;
			B921600 = 921600;
			B1000000 = 1000000;
			B1152000 = 1152000;
			B1500000 = 1500000;
			B2000000 = 2000000;
			B2500000 = 2500000;
			B3000000 = 3000000;
			B3500000 = 3500000;
			B4000000 = 4000000;
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
			/* sys/ioctl.h */
			_IOC_SIZEBITS = 14;
			_IOC_SIZEMASK = (1 << _IOC_SIZEBITS) - 1;
			IOC_VOID = 0;
			IOC_OUT = 0x80000000;
			IOC_IN = 0x40000000;
			/* asm-generic/ioctls.h */
			TIOCSBRK = CLib._IO('T', 0x27); // 0x5427
			TIOCCBRK = CLib._IO('T', 0x28); // 0x5428
			/* termios.h */
			IXON = 0x0400;
			IXOFF = 0x1000;
			IXANY = 0x0800;
			CSIZE = 0x0030;
			CS6 = 0x0010;
			CS7 = 0x0020;
			CS8 = 0x0030;
			CSTOPB = 0x0040;
			CREAD = 0x0080;
			PARENB = 0x0100;
			CMSPAR = 0x40000000;
			PARODD = 0x0200;
			CLOCAL = 0x0800;
			CRTSCTS = 0x80000000;
			ISIG = 0x0001;
			ICANON = 0x0002;
			ECHOE = 0x0010;
			VEOF = 4;
			VTIME = 5;
			VMIN = 6;
			VSTART = 8;
			VSTOP = 9;
			B0 = 0x0000;
			B50 = 0x0001;
			B75 = 0x0002;
			B110 = 0x0003;
			B134 = 0x0004;
			B150 = 0x0005;
			B200 = 0x0006;
			B300 = 0x0007;
			B600 = 0x0008;
			B1200 = 0x0009;
			B1800 = 0x000a;
			B2400 = 0x000b;
			B4800 = 0x000c;
			B9600 = 0x000d;
			B19200 = 0x000e;
			B38400 = 0x000f;
			B57600 = 0x1001;
			B115200 = 0x1002;
			B230400 = 0x1003;
			B460800 = 0x1004;
			B500000 = 0x1005;
			B576000 = 0x1006;
			B921600 = 0x1007;
			B1000000 = 0x1008;
			B1152000 = 0x1009;
			B1500000 = 0x100a;
			B2000000 = 0x100b;
			B2500000 = 0x100c;
			B3000000 = 0x100d;
			B3500000 = 0x100e;
			B4000000 = 0x100f;
		}
	}
}
