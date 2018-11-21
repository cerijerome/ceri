package ceri.serial.jna;

import static ceri.serial.jna.JnaUtil.verify;
import static ceri.serial.jna.sys.Ioss.IOSSIOSPEED;
import static ceri.serial.jna.sys.TtyCom.TIOCCBRK;
import static ceri.serial.jna.sys.TtyCom.TIOCSBRK;
import static jtermios.JTermios.TCSANOW;
import java.io.IOException;
import jtermios.JTermios;
import jtermios.Termios;

public class TermiosUtil {

	private TermiosUtil() {}

	public static void setIossSpeed(int fd, int baud) throws IOException {
		verify(JTermios.ioctl(fd, IOSSIOSPEED, baud), "ioctl:IOSSIOSPEED");
	}

	public static void setBreakBit(int fd) throws IOException {
		verify(JTermios.ioctl(fd, TIOCSBRK), "ioctl:TIOCSBRK");
	}

	public static void clearBreakBit(int fd) throws IOException {
		verify(JTermios.ioctl(fd, TIOCCBRK), "ioctl:TIOCCBRK");
	}

	public static Termios getTermios(int fd) throws IOException {
		Termios termios = new Termios();
		verify(JTermios.tcgetattr(fd, termios), "tcgetattr");
		return termios;
	}

	public static void setTermios(int fd, Termios termios) throws IOException {
		verify(JTermios.tcsetattr(fd, TCSANOW, termios), "tcsetattr:TCSANOW");
	}

}
