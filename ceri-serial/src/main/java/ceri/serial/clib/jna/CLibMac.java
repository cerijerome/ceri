package ceri.serial.clib.jna;

import static ceri.serial.clib.jna.CLib._IOW;
import com.sun.jna.NativeLong;
import ceri.serial.jna.Struct;
import ceri.serial.jna.Struct.Fields;

/**
 * Mac-specific C-library constants, types and methods.
 */
public class CLibMac {

	private CLibMac() {}

	/* ioss.h */

	private static final int IOSSIOSPEED = _IOW('T', 2, NativeLong.SIZE);

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
