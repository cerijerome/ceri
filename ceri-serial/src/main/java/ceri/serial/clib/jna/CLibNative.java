package ceri.serial.clib.jna;

import com.sun.jna.IntegerType;
import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

/**
 * A collection of common C library functions, grouped by linux header file.
 */
public interface CLibNative extends Library {

	/* unistd.h */

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

	// int close(int fd)
	int close(int fd) throws LastErrorException;

	// ssize_t read(int fd, void *buf, size_t count)
	ssize_t read(int fd, Pointer buffer, size_t len) throws LastErrorException;

	// ssize_t write(int fd, const void *buf, size_t count)
	ssize_t write(int fd, Pointer buffer, size_t len) throws LastErrorException;

	// off_t lseek(int fd, off_t offset, int whence)
	int lseek(int fd, int offset, int whence) throws LastErrorException;

	/* fcntl.h */

	// int open(const char *pathname, int flags)
	int open(String path, int flags) throws LastErrorException;

	// int open(const char *pathname, int flags, mode_t mode)
	int open(String path, int flags, int mode) throws LastErrorException;

	/* ioctl.h */

	// int ioctl(int d, int request, ...)
	int ioctl(int fd, int request, Object... objs) throws LastErrorException;

	/* termios.h */

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

}
