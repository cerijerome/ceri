package ceri.serial.clib.jna;

import com.sun.jna.NativeLong;
import ceri.serial.jna.Struct;
import ceri.serial.jna.Struct.Fields;

/**
 * Linux-specific C-library constants, types and methods.
 */
public class CLibLinux {

	private CLibLinux() {}

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
		return termios;
	}

	/**
	 * Set termios attributes.
	 */
	public static void tcsetattr(int fd, int actions, termios termios) throws CException {
		CLib.tcsetattr(fd, actions, termios.getPointer());
	}

}
