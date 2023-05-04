package ceri.jna.clib.jna;

import static ceri.jna.clib.jna.CLib.caller;
import static ceri.jna.clib.jna.CLib.lib;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import ceri.common.util.OsUtil;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.Struct;
import ceri.jna.util.Struct.Fields;

/**
 * Types and functions from {@code <termios.h>}
 */
public class CTermios {
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

	private CTermios() {}

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
	 * Transmits a continuous stream of zero-valued bits.
	 */
	public static void tcsendbreak(int fd, int duration) throws CException {
		caller.verify(() -> lib().tcsendbreak(fd, duration), "tcsendbreak", fd, duration);
	}

	/**
	 * Wait for transmission of output.
	 */
	public static void tcdrain(int fd) throws CException {
		caller.verify(() -> lib().tcdrain(fd), "tcdrain", fd);
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

	/* os-specific types and calls */

	/**
	 * Types and calls specific to Mac.
	 */
	public static final class Mac {
		private Mac() {}

		/**
		 * General terminal interface to control asynchronous communications ports.
		 */
		@Fields({ "c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_cc", "c_ispeed", "c_ospeed" })
		public static class termios extends CTermios.termios {
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
		 * Populates mac termios attributes.
		 */
		public static termios tcgetattr(int fd) throws CException {
			return CTermios.tcgetattr(fd, new termios());
		}
	}

	/**
	 * Types and calls specific to Linux.
	 */
	public static final class Linux {
		private Linux() {}

		/**
		 * General terminal interface to control asynchronous communications ports.
		 */
		@Fields({ "c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_line", "c_cc", "c_ispeed",
			"c_ospeed" })
		public static class termios extends CTermios.termios {
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
		 * Populates linux termios attributes..
		 */
		public static termios tcgetattr(int fd) throws CException {
			return CTermios.tcgetattr(fd, new termios());
		}
	}

	/* os-specific initialization */

	static {
		if (OsUtil.os().mac) {
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
