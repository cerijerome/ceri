package ceri.jna.clib.jna;

import static ceri.jna.clib.jna.CLib.caller;
import static ceri.jna.clib.jna.CLib.lib;
import java.util.function.Supplier;
import ceri.common.math.MathUtil;
import ceri.common.util.OsUtil;
import ceri.jna.type.IntType;
import ceri.jna.util.JnaSize;
import ceri.jna.util.Struct;
import ceri.jna.util.Struct.Fields;

/**
 * Types and functions from {@code <termios.h>}
 */
public class CTermios {
	// Sizes
	private static final int TCFLAG_T_SIZE;
	private static final int SPEED_T_SIZE;
	private static final int NCCS; // number of termios control chars
	// Input flags
	public static final int IGNBRK = 0x0001;
	public static final int BRKINT = 0x0002;
	public static final int IGNPAR = 0x0004;
	public static final int PARMRK = 0x0008;
	public static final int INPCK = 0x0010;
	public static final int ISTRIP = 0x0020;
	public static final int INLCR = 0x0040;
	public static final int IGNCR = 0x0080;
	public static final int ICRNL = 0x0100;
	public static final int IXON;
	public static final int IXANY = 0x800;
	public static final int IXOFF;
	public static final int IMAXBEL = 0x2000;
	public static final int IUTF8 = 0x4000;
	// Output flags
	public static final int OPOST = 0x0001;
	public static final int ONLCR;
	public static final int OCRNL;
	public static final int ONOCR;
	public static final int ONLRET;
	public static final int OFILL;
	public static final int OFDEL;
	// Control flags
	public static final int CSIZE;
	public static final int CSTOPB;
	public static final int CREAD;
	public static final int PARENB;
	public static final int PARODD;
	public static final int HUPCL;
	public static final int CLOCAL;
	public static final int CMSPAR;
	public static final int CRTSCTS;
	// CSIZE values
	public static final int CS5 = 0x0000;
	public static final int CS6;
	public static final int CS7;
	public static final int CS8;
	// Local flags
	public static final int ISIG;
	public static final int ICANON;
	public static final int ECHO = 0x0008;
	public static final int ECHOE;
	public static final int ECHOK;
	public static final int ECHONL;
	public static final int ECHOCTL;
	public static final int ECHOPRT;
	public static final int ECHOKE;
	public static final int NOFLSH;
	public static final int TOSTOP;
	public static final int IEXTEN;
	// Special chars
	public static final int VEOF;
	public static final int VEOL;
	public static final int VEOL2;
	public static final int VERASE;
	public static final int VINTR;
	public static final int VKILL;
	public static final int VLNEXT;
	public static final int VMIN;
	public static final int VQUIT;
	public static final int VREPRINT;
	public static final int VSTART;
	public static final int VSTOP;
	public static final int VSUSP = 10;
	public static final int VTIME;
	public static final int VWERASE;
	// tcsetattr() optional actions
	public static final int TCSANOW = 0;
	public static final int TCSADRAIN = 1;
	public static final int TCSAFLUSH = 2;
	// tcflush() queue selector
	public static final int TCIFLUSH;
	public static final int TCOFLUSH;
	public static final int TCIOFLUSH;
	// tcflow() action
	public static final int TCOOFF;
	public static final int TCOON;
	public static final int TCIOFF;
	public static final int TCION;
	// cfsetospeed() constants
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

	@SuppressWarnings("serial")
	public static class tcflag_t extends IntType<tcflag_t> {
		public static final int SIZE = TCFLAG_T_SIZE;

		public tcflag_t() {
			this(0L);
		}

		public tcflag_t(long value) {
			super(TCFLAG_T_SIZE, value, true);
		}
	}

	@SuppressWarnings("serial")
	public static class speed_t extends IntType<speed_t> {
		private static final Supplier<speed_t> CONSTRUCTOR = speed_t::new;
		public static final int SIZE = SPEED_T_SIZE;

		public static class ByRef extends IntType.ByRef<speed_t> {
			public ByRef() {
				this(new speed_t());
			}

			public ByRef(speed_t value) {
				super(CONSTRUCTOR, value);
			}
		}

		public speed_t() {
			this(0L);
		}

		public speed_t(int value) {
			this(MathUtil.uint(value));
		}
		
		public speed_t(long value) {
			super(SPEED_T_SIZE, value, true);
		}
	}

	/**
	 * Common base for os-specific termios struct.
	 */
	public static abstract class termios extends Struct {
		public tcflag_t c_iflag; // input flags
		public tcflag_t c_oflag; // output flags
		public tcflag_t c_cflag; // control flags
		public tcflag_t c_lflag; // local flags
		public byte[] c_cc = new byte[NCCS]; // control chars
		public speed_t c_ispeed; // input speed
		public speed_t c_ospeed; // output speed
	}

	/**
	 * Populates termios attributes; the general terminal interface to control asynchronous
	 * communications ports.
	 */
	public static <T extends termios> T tcgetattr(int fd, T termios) throws CException {
		var p = termios.getPointer();
		caller.verify(() -> lib().tcgetattr(fd, p), "tcgetattr", fd, p);
		return Struct.read(termios);
	}

	/**
	 * Convenience method to get populated os-specific termios attributes.
	 */
	public static termios tcgetattr(int fd) throws CException {
		return CLib.validateOs().mac ? Mac.tcgetattr(fd) : Linux.tcgetattr(fd);
	}

	/**
	 * Set termios attributes on the terminal.
	 */
	public static void tcsetattr(int fd, int actions, termios termios) throws CException {
		var p = Struct.write(termios).getPointer();
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
	 * Discards data written but not transmitted, or data received but not read, depending on the
	 * queue selector: TCIFLUSH, TCOFLUSH, TCIOFLUSH.
	 */
	public static void tcflush(int fd, int queueSelector) throws CException {
		caller.verify(() -> lib().tcflush(fd, queueSelector), "tcflush", fd);
	}

	/**
	 * Suspends transmission or reception of data, depending on the action: TCOOFF, TCOON, TCIOFF,
	 * TCION
	 */
	public static void tcflow(int fd, int action) throws CException {
		caller.verify(() -> lib().tcflow(fd, action), "tcflow", fd);
	}

	/**
	 * Configures raw mode; input is available char by char. Call <code>Struct.write(termios)</code>
	 * first if memory is out of date.
	 */
	public static void cfmakeraw(termios termios) throws CException {
		var p = termios.getPointer();
		caller.call(() -> lib().cfmakeraw(p), "cfmakeraw", p);
		Struct.read(termios);
	}

	/**
	 * Returns the input baud rate stored in the termios structure. Call
	 * <code>Struct.write(termios)</code> first if memory is out of date.
	 */
	public static int cfgetispeed(termios termios) throws CException {
		var p = termios.getPointer();
		return caller.callType(() -> lib().cfgetispeed(p), "cfgetispeed", p).intValue();
	}

	/**
	 * Sets the input baud rate in the termios structure. Call <code>Struct.write(termios)</code>
	 * first if memory is out of date.
	 */
	public static void cfsetispeed(termios termios, int speed) throws CException {
		var p = termios.getPointer();
		var n = new speed_t(speed);
		caller.verify(() -> lib().cfsetispeed(p, n), "cfsetispeed", p, n);
		Struct.read(termios);
	}

	/**
	 * Returns the output baud rate stored in the termios structure. Call
	 * <code>Struct.write(termios)</code> first if memory is out of date.
	 */
	public static int cfgetospeed(termios termios) throws CException {
		var p = termios.getPointer();
		return caller.callType(() -> lib().cfgetospeed(p), "cfgetospeed", p).intValue();
	}

	/**
	 * Sets the output baud rate in the termios structure. Call <code>Struct.write(termios)</code>
	 * first if memory is out of date.
	 */
	public static void cfsetospeed(termios termios, int speed) throws CException {
		var p = termios.getPointer();
		var n = new speed_t(speed);
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
		public static class termios extends CTermios.termios {}

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
			public byte c_line; // line discipline
		}

		/**
		 * Populates linux termios attributes.
		 */
		public static termios tcgetattr(int fd) throws CException {
			return CTermios.tcgetattr(fd, new termios());
		}
	}

	/* os-specific initialization */

	static {
		if (OsUtil.os().mac) {
			TCFLAG_T_SIZE = JnaSize.LONG.get();
			SPEED_T_SIZE = JnaSize.LONG.get();
			NCCS = 20;
			IXON = 0x0200;
			IXOFF = 0x0400;
			ONLCR = 0x0002;
			OCRNL = 0x0010;
			ONOCR = 0x0020;
			ONLRET = 0x0040;
			OFILL = 0x0080;
			OFDEL = 0x00020000;
			CSIZE = 0x0300;
			CSTOPB = 0x0400;
			CREAD = 0x0800;
			PARENB = 0x1000;
			PARODD = 0x2000;
			HUPCL = 0x4000;
			CLOCAL = 0x8000;
			CMSPAR = 0; // not supported
			CRTSCTS = 0x00030000;
			CS6 = 0x0100;
			CS7 = 0x0200;
			CS8 = 0x0300;
			ISIG = 0x0080;
			ICANON = 0x0100;
			ECHOE = 0x0002;
			ECHOK = 0x0004;
			ECHONL = 0x0010;
			ECHOCTL = 0x0040;
			ECHOPRT = 0x0020;
			ECHOKE = 0x0001;
			NOFLSH = 0x80000000;
			TOSTOP = 0x00400000;
			IEXTEN = 0x0400;
			VEOF = 0;
			VEOL = 1;
			VEOL2 = 2;
			VERASE = 3;
			VINTR = 8;
			VKILL = 5;
			VLNEXT = 14;
			VMIN = 16;
			VQUIT = 9;
			VREPRINT = 6;
			VSTART = 12;
			VSTOP = 13;
			VTIME = 17;
			VWERASE = 4;
			TCIFLUSH = 1;
			TCOFLUSH = 2;
			TCIOFLUSH = 3;
			TCOOFF = 1;
			TCOON = 2;
			TCIOFF = 3;
			TCION = 4;
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
			TCFLAG_T_SIZE = Integer.BYTES;
			SPEED_T_SIZE = Integer.BYTES;
			NCCS = 32;
			IXON = 0x0400;
			IXOFF = 0x1000;
			ONLCR = 0x0004;
			OCRNL = 0x0008;
			ONOCR = 0x0010;
			ONLRET = 0x0020;
			OFILL = 0x0040;
			OFDEL = 0x0080;
			CSIZE = 0x0030;
			CSTOPB = 0x0040;
			CREAD = 0x0080;
			PARENB = 0x0100;
			PARODD = 0x0200;
			HUPCL = 0x0400;
			CLOCAL = 0x0800;
			CMSPAR = 0x40000000;
			CRTSCTS = 0x80000000;
			CS6 = 0x0010;
			CS7 = 0x0020;
			CS8 = 0x0030;
			ISIG = 0x0001;
			ICANON = 0x0002;
			ECHOE = 0x0010;
			ECHOK = 0x0020;
			ECHONL = 0x0040;
			ECHOCTL = 0x0200;
			ECHOPRT = 0x0400;
			ECHOKE = 0x0800;
			NOFLSH = 0x0080;
			TOSTOP = 0x0100;
			IEXTEN = 0x8000;
			VEOF = 4;
			VEOL = 11;
			VEOL2 = 16;
			VERASE = 2;
			VINTR = 0;
			VKILL = 3;
			VLNEXT = 15;
			VMIN = 6;
			VQUIT = 1;
			VREPRINT = 12;
			VSTART = 8;
			VSTOP = 9;
			VTIME = 5;
			VWERASE = 14;
			TCIFLUSH = 0;
			TCOFLUSH = 1;
			TCIOFLUSH = 2;
			TCOOFF = 0;
			TCOON = 1;
			TCIOFF = 2;
			TCION = 3;
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
