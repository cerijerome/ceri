package ceri.serial.clib.jna;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import ceri.serial.clib.jna.CLib.size_t;
import ceri.serial.clib.jna.CLib.ssize_t;
import ceri.serial.clib.jna.Termios.termios;

/**
 * A collection of C library functions. Definitions from https://linux.die.net/
 */
public interface CLibNative extends Library {

	// fcntl.h: int open(const char *pathname, int flags)
	int open(String path, int flags) throws LastErrorException;

	// fcntl.h: int open(const char *pathname, int flags, mode_t mode)
	int open(String path, int flags, int mode) throws LastErrorException;

	// unistd.h: int close(int fd)
	int close(int fd) throws LastErrorException;

	// unistd.h: ssize_t read(int fd, void *buf, size_t count)
	ssize_t read(int fd, Pointer buffer, size_t len) throws LastErrorException;

	// unistd.h: ssize_t write(int fd, const void *buf, size_t count)
	ssize_t write(int fd, Pointer buffer, size_t len) throws LastErrorException;

	// unistd.h: off_t lseek(int fd, off_t offset, int whence)
	int lseek(int fd, int offset, int whence) throws LastErrorException;

	// sys/ioctl.h: int ioctl(int d, int request, ...)
	int ioctl(int fd, int request, Object... objs) throws LastErrorException;

	/* termios */
	
	// termios.h: int tcgetattr(int fd, struct termios *termios_p)
	int tcgetattr(int fd, termios.ByReference termios) throws LastErrorException;

	// termios.h: int tcsetattr(int fd, int optional_actions, const struct termios *termios_p)
	int tcsetattr(int fd, int optional_actions, termios termios) throws LastErrorException;

	// termios.h: int tcsendbreak(int fd, int duration)
	int tcsendbreak(int fd, int duration) throws LastErrorException;

	// termios.h: int tcdrain(int fd)
	int tcdrain(int fd) throws LastErrorException;

	// termios.h: int tcflush(int fd, int queue_selector)
	int tcflush(int fd, int queue_selector) throws LastErrorException;

	// termios.h: int tcflow(int fd, int action)
	int tcflow(int fd, int action) throws LastErrorException;

	// termios.h: void cfmakeraw(struct termios *termios_p)
	void cfmakeraw(termios termios) throws LastErrorException;

	// termios.h: speed_t cfgetispeed(const struct termios *termios_p)
	NativeLong cfgetispeed(termios.ByReference termios) throws LastErrorException;

	// termios.h: speed_t cfgetospeed(const struct termios *termios_p)
	NativeLong cfgetospeed(termios.ByReference termios) throws LastErrorException;

	// termios.h: int cfsetispeed(struct termios *termios_p, speed_t speed)
	int cfsetispeed(termios termios, NativeLong speed) throws LastErrorException;

	// termios.h: int cfsetospeed(struct termios *termios_p, speed_t speed)
	int cfsetospeed(termios termios, NativeLong speed) throws LastErrorException;

	// termios.h: int cfsetspeed(struct termios *termios_p, speed_t speed)
	int cfsetspeed(termios termios, NativeLong speed) throws LastErrorException;

}
