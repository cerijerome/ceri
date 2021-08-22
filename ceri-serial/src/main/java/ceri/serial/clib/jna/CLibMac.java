package ceri.serial.clib.jna;

import static ceri.serial.clib.jna.Ioctls._IO;
import static ceri.serial.clib.jna.Ioctls._IOW;
import static jtermios.JTermios.TCSANOW;
import java.util.List;
import com.sun.jna.NativeLong;
import ceri.serial.jna.Struct;

/**
 * Mac-specific C-library constants, types and methods.
 */
public class CLibMac {

	private CLibMac() {}

	/* ioccom.h */

	public static final int _IOC_SIZEBITS = 13; // IOCPARM_MASK = 0x1fff;
	public static final int _IOC_VOID = 0x20000000;

	/* ttycom.h */

	public static final int TIOCSBRK = _IO('t', 123); // 0x2000747b
	public static final int TIOCCBRK = _IO('t', 122); // 0x2000747a

	/* ioss.h */

	private static final int IOSSIOSPEED = _IOW('T', 2, NativeLong.SIZE);

	/**
	 * Sets input and output speeds to a non-traditional baud rate.
	 */
	public static void iossiospeed(int fd, int baud) throws CException {
		// CUtil.verify(JTermios.ioctl(fd, IOSSIOSPEED, baud), "ioctl:IOSSIOSPEED");
		CLib.ioctl("IOSSIOSPEED", fd, IOSSIOSPEED, baud);
	}

	/* termios.h */

	/**
	 * General terminal interface to control asynchronous communications ports.
	 */
	public static class termios extends Struct {
		private static final List<String> FIELDS = List.of( //
			"c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_line", "c_cc", "c_ispeed", "c_ospeed");
		private static final int NCCS = 20;

		public NativeLong c_iflag; // input flags
		public NativeLong c_oflag; // output flags
		public NativeLong c_cflag; // control flags
		public NativeLong c_lflag; // local flags
		public byte[] c_cc = new byte[NCCS]; // control chars
		public NativeLong c_ispeed; // input speed
		public NativeLong c_ospeed; // output speed

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	/**
	 * Get termios attributes.
	 */
	public static termios tcgetattr(int fd) throws CException {
		termios termios = new termios();
		CLib.tcgetattr(fd, termios.getPointer());
		return termios;
	}

	/**
	 * Set termios attributes.
	 */
	public static void tcsetattr(int fd, int actions, termios termios) throws CException {
		CLib.tcsetattr(fd, actions, termios.getPointer());
	}

	/**
	 * Set termios attributes now.
	 */
	public static void tcsetattr(int fd, termios termios) throws CException {
		tcsetattr(fd, TCSANOW, termios);
	}

}
