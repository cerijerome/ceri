package ceri.serial.jna.clib;

import static ceri.serial.jna.JnaUtil.validateFileDescriptor;
import static ceri.serial.jna.JnaUtil.verify;
import static ceri.serial.jna.clib.Ioctls.IOSSIOSPEED;
import static ceri.serial.jna.clib.Ioctls.TIOCCBRK;
import static ceri.serial.jna.clib.Ioctls.TIOCSBRK;
import static jtermios.JTermios.TCSANOW;
import jtermios.JTermios;
import jtermios.Termios;

public class TermiosUtil {

	private TermiosUtil() {}

	public static int open(String path, int flags) throws CException {
		return validateFileDescriptor(JTermios.open(path, flags));
	}

	public static int close(int fd) throws CException {
		return verify(JTermios.close(fd), "close");
	}

	public static void setIossSpeed(int fd, int baud) throws CException {
		verify(JTermios.ioctl(fd, IOSSIOSPEED, baud), "ioctl:IOSSIOSPEED");
	}

	public static void setBreakBit(int fd) throws CException {
		verify(JTermios.ioctl(fd, TIOCSBRK), "ioctl:TIOCSBRK");
	}

	public static void clearBreakBit(int fd) throws CException {
		verify(JTermios.ioctl(fd, TIOCCBRK), "ioctl:TIOCCBRK");
	}

	public static Termios getTermios(int fd) throws CException {
		Termios termios = new Termios();
		verify(JTermios.tcgetattr(fd, termios), "tcgetattr");
		return termios;
	}

	public static void setTermios(int fd, Termios termios) throws CException {
		verify(JTermios.tcsetattr(fd, TCSANOW, termios), "tcsetattr:TCSANOW");
	}

	public static int write(int fd, byte[] data) throws CException {
		return verify(JTermios.write(fd, data, data.length), "write");
	}

}
