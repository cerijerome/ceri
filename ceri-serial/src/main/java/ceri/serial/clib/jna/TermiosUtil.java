package ceri.serial.clib.jna;

import static ceri.serial.clib.jna.Ioctls.IOSSIOSPEED;
import static ceri.serial.clib.jna.Ioctls.TIOCCBRK;
import static ceri.serial.clib.jna.Ioctls.TIOCSBRK;
import static jtermios.JTermios.TCSANOW;
import jtermios.JTermios;
import jtermios.Termios;

/**
 * Utility functions using purejavacomm JTermios.
 */
public class TermiosUtil {

	private TermiosUtil() {}

//	public static int open(String path, int flags) throws CException {
//		return CUtil.validateFd(JTermios.open(path, flags));
//	}
//
//	public static int close(int fd) throws CException {
//		if (fd <= 0) return 0;
//		return CUtil.verify(JTermios.close(fd), "close");
//	}
//
//	public static int write(int fd, byte[] data) throws CException {
//		return CUtil.verify(JTermios.write(fd, data, data.length), "write");
//	}
//
//	public static int ioctl(int fd, int cmd, int... values) throws CException {
//		return CUtil.verify(JTermios.ioctl(fd, cmd, values),
//			() -> String.format("ioctl:0x%08x", cmd));
//	}

	public static void setIossSpeed(int fd, int baud) throws CException {
		CUtil.verify(JTermios.ioctl(fd, IOSSIOSPEED, baud), "ioctl:IOSSIOSPEED");
	}

	public static void setBreakBit(int fd) throws CException {
		CUtil.verify(JTermios.ioctl(fd, TIOCSBRK), "ioctl:TIOCSBRK");
	}

	public static void clearBreakBit(int fd) throws CException {
		CUtil.verify(JTermios.ioctl(fd, TIOCCBRK), "ioctl:TIOCCBRK");
	}

	public static Termios getTermios(int fd) throws CException {
		Termios termios = new Termios();
		CUtil.verify(JTermios.tcgetattr(fd, termios), "tcgetattr");
		return termios;
	}

	public static void setTermios(int fd, Termios termios) throws CException {
		CUtil.verify(JTermios.tcsetattr(fd, TCSANOW, termios), "tcsetattr:TCSANOW");
	}

}
