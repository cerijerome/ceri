package ceri.serial.comm.jna;

import static ceri.jna.clib.jna.CFcntl.O_NOCTTY;
import static ceri.jna.clib.jna.CFcntl.O_NONBLOCK;
import static ceri.jna.clib.jna.CFcntl.O_RDWR;
import static ceri.jna.clib.jna.CIoctl.Linux.ASYNC_SPD_CUST;
import static ceri.jna.clib.jna.CIoctl.Linux.ASYNC_SPD_MASK;
import static ceri.jna.clib.jna.CTermios.CLOCAL;
import static ceri.jna.clib.jna.CTermios.CMSPAR;
import static ceri.jna.clib.jna.CTermios.CREAD;
import static ceri.jna.clib.jna.CTermios.CRTSCTS;
import static ceri.jna.clib.jna.CTermios.CS5;
import static ceri.jna.clib.jna.CTermios.CS6;
import static ceri.jna.clib.jna.CTermios.CS7;
import static ceri.jna.clib.jna.CTermios.CS8;
import static ceri.jna.clib.jna.CTermios.CSIZE;
import static ceri.jna.clib.jna.CTermios.CSTOPB;
import static ceri.jna.clib.jna.CTermios.IXANY;
import static ceri.jna.clib.jna.CTermios.IXOFF;
import static ceri.jna.clib.jna.CTermios.IXON;
import static ceri.jna.clib.jna.CTermios.PARENB;
import static ceri.jna.clib.jna.CTermios.PARODD;
import static ceri.jna.clib.jna.CTermios.TCSANOW;
import static ceri.jna.clib.jna.CTermios.VMIN;
import static ceri.jna.clib.jna.CTermios.VSTART;
import static ceri.jna.clib.jna.CTermios.VSTOP;
import static ceri.jna.clib.jna.CTermios.VTIME;
import com.sun.jna.IntegerType;
import ceri.common.collection.Maps;
import ceri.common.exception.Exceptions;
import ceri.common.math.Maths;
import ceri.common.util.OsUtil;
import ceri.common.util.Validate;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CFcntl;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.clib.jna.CTermios.termios;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.type.Struct;

/**
 * Provides logic for opening and configuring serial ttys. For Macos and non-standard baud, changes
 * to attributes will mangle the input buffer, as the attributes must be saved with a temporary
 * standard baud. IOSSIOSPEED ioctl is then used to set the desired baud. This baud is present in
 * the termios struct in subsequent calls to get attributes.
 */
public class CSerial {
	private static final int BAUD_UNSUPPORTED = -1;
	private static final int DC1 = 0x11; // ASCII device control 1 (^Q)
	private static final int DC3 = 0x13; // ASCII device control 3 (^S)
	private static final Maps.Bi<Integer, Integer> baudMap = baudMap();
	private static final double BAUD_MATCH = 0.05;
	public static final int DATABITS_5 = 5;
	public static final int DATABITS_6 = 6;
	public static final int DATABITS_7 = 7;
	public static final int DATABITS_8 = 8;
	public static final int FLOWCONTROL_NONE = 0;
	public static final int FLOWCONTROL_RTSCTS_IN = 1;
	public static final int FLOWCONTROL_RTSCTS_OUT = 2;
	public static final int FLOWCONTROL_XONXOFF_IN = 4;
	public static final int FLOWCONTROL_XONXOFF_OUT = 8;
	public static final int PARITY_EVEN = 2;
	public static final int PARITY_MARK = 3;
	public static final int PARITY_NONE = 0;
	public static final int PARITY_ODD = 1;
	public static final int PARITY_SPACE = 4;
	public static final int STOPBITS_1 = 1;
	public static final int STOPBITS_1_5 = 3;
	public static final int STOPBITS_2 = 2;

	private CSerial() {}

	public static int open(String port) throws CException {
		int fd = CFcntl.open(port, O_RDWR | O_NOCTTY | O_NONBLOCK);
		try {
			CFcntl.setFl(fd, flags -> flags & ~O_NONBLOCK);
			initPort(fd, CTermios.tcgetattr(fd));
			return fd;
		} catch (CException | RuntimeException e) {
			CUnistd.closeSilently(fd);
			throw e;
		}
	}

	/**
	 * Sets baud, data bits, stop bits, and parity.
	 */
	public static void setParams(int fd, int baud, int dataBits, int stopBits, int parity)
		throws CException {
		var tty = CTermios.tcgetattr(fd);
		setParamFlags(tty.c_cflag, dataBits, stopBits, parity);
		if (OsUtil.os().mac) Mac.setBaud(fd, tty, baud);
		else Linux.setBaud(fd, tty, baud);
	}

	/**
	 * Sets flow control.
	 */
	public static void setFlowControl(int fd, int mode) throws CException {
		var tty = CTermios.tcgetattr(fd);
		setFlowControlFlags(tty.c_iflag, tty.c_cflag, mode);
		setAttr(fd, tty);
	}

	/**
	 * Sets termios VMIN and VTIME control chars. VMIN is the minimum number of bytes required on a
	 * read; VTIME is the read timeout in .1 seconds.
	 * 
	 * <pre>
	 * VMIN = 0, VTIME = 0 (polling read) returns immediately
	 * VMIN > 0, VTIME = 0 (blocking read) blocks until VMIN bytes available
	 * VMIN = 0, VTIME > 0 (read timeout) blocks until 1+ bytes or timeout
	 * VMIN > 0, VTIME > 0 (byte timeout) blocks until VMIN bytes, timeout per byte 1+
	 * </pre>
	 */
	public static void setReadParams(int fd, int vmin, int vtime) throws CException {
		Validate.validateRange(vmin, 0, 0xff);
		Validate.validateRange(vtime, 0, 0xff);
		var tty = CTermios.tcgetattr(fd);
		tty.c_cc[VMIN] = (byte) vmin;
		tty.c_cc[VTIME] = (byte) vtime;
		setAttr(fd, tty);
	}

	public static void brk(int fd, boolean enable) throws CException {
		if (enable) CIoctl.tiocsbrk(fd);
		else CIoctl.tioccbrk(fd);
	}

	public static boolean cd(int fd) throws CException {
		return CIoctl.tiocmbit(fd, CIoctl.TIOCM_CD);
	}

	public static boolean cts(int fd) throws CException {
		return CIoctl.tiocmbit(fd, CIoctl.TIOCM_CTS);
	}

	public static boolean dsr(int fd) throws CException {
		return CIoctl.tiocmbit(fd, CIoctl.TIOCM_DSR);
	}

	public static boolean dtr(int fd) throws CException {
		return CIoctl.tiocmbit(fd, CIoctl.TIOCM_DTR);
	}

	public static void dtr(int fd, boolean enable) throws CException {
		CIoctl.tiocmbit(fd, CIoctl.TIOCM_DTR, enable);
	}

	public static boolean ri(int fd) throws CException {
		return CIoctl.tiocmbit(fd, CIoctl.TIOCM_RI);
	}

	public static boolean rts(int fd) throws CException {
		return CIoctl.tiocmbit(fd, CIoctl.TIOCM_RTS);
	}

	public static void rts(int fd, boolean enable) throws CException {
		CIoctl.tiocmbit(fd, CIoctl.TIOCM_RTS, enable);
	}

	private static void initPort(int fd, termios tty) throws CException {
		CTermios.cfmakeraw(tty);
		initTermios(tty.c_iflag, tty.c_cflag, tty.c_cc);
		setBaudCode(tty, CTermios.B9600);
		CTermios.tcsetattr(fd, TCSANOW, tty);
	}

	private static void initTermios(IntegerType iflag, IntegerType cflag, byte[] cc) {
		iflag.setValue(iflag.longValue() & ~(IXANY | IXOFF | IXON));
		cflag.setValue((cflag.longValue() & ~(CSIZE | CSTOPB | PARENB | CMSPAR | PARODD | CRTSCTS))
			| CLOCAL | CREAD | CS8);
		cc[VSTART] = DC1;
		cc[VSTOP] = DC3;
		cc[VMIN] = 0; // no min bytes for read
		cc[VTIME] = 0; // no timeout for read
	}

	private static void setParamFlags(IntegerType cflag, int dataBits, int stopBits, int parity) {
		cflag.setValue((cflag.longValue() & ~(CSIZE | CSTOPB | PARENB | CMSPAR | PARODD))
			| dataBitFlag(dataBits) | stopBitFlag(stopBits) | parityFlags(parity));
	}

	private static long dataBitFlag(int dataBits) {
		return switch (dataBits) {
			case DATABITS_5 -> CS5;
			case DATABITS_6 -> CS6;
			case DATABITS_7 -> CS7;
			case DATABITS_8 -> CS8;
			default -> throw new IllegalArgumentException("Unsupported data bits: " + dataBits);
		};
	}

	private static long stopBitFlag(int stopBits) {
		return switch (stopBits) {
			case STOPBITS_1 -> 0;
			case STOPBITS_1_5 -> CSTOPB;
			case STOPBITS_2 -> CSTOPB;
			default -> throw new IllegalArgumentException("Unsupported stop bits: " + stopBits);
		};
	}

	private static long parityFlags(int parity) {
		if (parity == PARITY_NONE) return 0;
		if (parity == PARITY_EVEN) return PARENB;
		if (CMSPAR != 0) { // if CMSPAR is supported
			if (parity == PARITY_MARK) return PARENB | CMSPAR | PARODD;
			if (parity == PARITY_SPACE) return PARENB | CMSPAR;
		}
		throw Exceptions.illegalArg("Unsupported parity: 0x%02x", parity);
	}

	private static void setFlowControlFlags(IntegerType iflag, IntegerType cflag, int mode) {
		iflag.setValue((iflag.longValue() & ~(IXANY | IXOFF | IXON)) | flowControlSw(mode));
		cflag.setValue((cflag.longValue() & ~CRTSCTS) | flowControlHw(mode));
	}

	/**
	 * Hardware flow control flags (RTS, CTS)
	 */
	private static long flowControlHw(int mode) {
		return (mode & (FLOWCONTROL_RTSCTS_IN | FLOWCONTROL_RTSCTS_OUT)) == 0 ? 0 : CRTSCTS;
	}

	/**
	 * Software flow control flags (XON, XOFF)
	 */
	private static long flowControlSw(int mode) {
		return ((mode & FLOWCONTROL_XONXOFF_IN) == 0 ? 0 : IXOFF)
			| ((mode & FLOWCONTROL_XONXOFF_OUT) == 0 ? 0 : IXON);
	}

	private static void setBaudCode(termios tty, int baudCode) throws CException {
		Struct.write(tty);
		CTermios.cfsetispeed(tty, baudCode);
		CTermios.cfsetospeed(tty, baudCode);
	}

	private static int baudCode(int baud) {
		return baudMap.values.getOrDefault(baud, BAUD_UNSUPPORTED);
	}

	private static Maps.Bi<Integer, Integer> baudMap() {
		var b = Maps.build(CTermios.B0, 0).put(CTermios.B50, 50).put(CTermios.B75, 75)
			.put(CTermios.B110, 110).put(CTermios.B134, 134).put(CTermios.B150, 150)
			.put(CTermios.B200, 200).put(CTermios.B300, 300).put(CTermios.B600, 600)
			.put(CTermios.B1200, 1200).put(CTermios.B1800, 1800).put(CTermios.B2400, 2400)
			.put(CTermios.B4800, 4800).put(CTermios.B9600, 9600).put(CTermios.B19200, 19200)
			.put(CTermios.B38400, 38400).put(CTermios.B57600, 57600).put(CTermios.B115200, 115200)
			.put(CTermios.B230400, 230400);
		if (OsUtil.os().linux) b.put(CTermios.B460800, 460800).put(CTermios.B500000, 500000)
			.put(CTermios.B576000, 576000).put(CTermios.B921600, 921600)
			.put(CTermios.B1000000, 1000000).put(CTermios.B1152000, 1152000)
			.put(CTermios.B1500000, 1500000).put(CTermios.B2000000, 2000000)
			.put(CTermios.B2500000, 2500000).put(CTermios.B3000000, 3000000)
			.put(CTermios.B3500000, 3500000).put(CTermios.B4000000, 4000000);
		return Maps.Bi.of(b.get());
	}

	private static void setAttr(int fd, termios tty) throws CException {
		if (OsUtil.os().mac) Mac.setAttr(fd, tty); // handle non-standard baud
		else CTermios.tcsetattr(fd, TCSANOW, tty);
	}

	/* os-specific types and calls */

	public static final class Mac {
		private Mac() {}

		private static void setBaud(int fd, termios tty, int baud) throws CException {
			int baudCode = CSerial.baudCode(baud);
			if (baudCode == BAUD_UNSUPPORTED) enableCustomBaud(fd, tty, baud);
			else {
				CSerial.setBaudCode(tty, baudCode);
				CTermios.tcsetattr(fd, TCSANOW, tty);
			}
		}

		private static void setAttr(int fd, termios tty) throws CException {
			int baud = tty.c_ospeed.intValue(); // must re-apply if non-standard
			if (CSerial.baudCode(baud) == BAUD_UNSUPPORTED) enableCustomBaud(fd, tty, baud);
			else CTermios.tcsetattr(fd, TCSANOW, tty);
		}

		private static void enableCustomBaud(int fd, termios tty, int baud) throws CException {
			CSerial.setBaudCode(tty, CTermios.B9600);
			CTermios.tcsetattr(fd, TCSANOW, tty);
			CIoctl.Mac.iossiospeed(fd, baud);
		}
	}

	public static final class Linux {
		private Linux() {}

		private static void setBaud(int fd, termios tty, int baud) throws CException {
			int baudCode = CSerial.baudCode(baud);
			if (baudCode == BAUD_UNSUPPORTED) {
				enableCustomBaud(fd, baud);
				verifyCustomBaud(fd, baud, BAUD_MATCH);
				baudCode = CTermios.B38400;
			} else disableCustomBaud(fd);
			CSerial.setBaudCode(tty, baudCode);
			CTermios.tcsetattr(fd, TCSANOW, tty);
		}

		private static void disableCustomBaud(int fd) throws CException {
			var serial = CIoctl.Linux.tiocgserial(fd);
			serial.flags &= ~ASYNC_SPD_MASK;
			CIoctl.Linux.tiocsserial(fd, serial);
		}

		private static void enableCustomBaud(int fd, int baud) throws CException {
			var serial = CIoctl.Linux.tiocgserial(fd);
			serial.flags = (serial.flags & ~ASYNC_SPD_MASK) | ASYNC_SPD_CUST;
			serial.custom_divisor = Math.max((serial.baud_base + (baud / 2)) / baud, 1);
			CIoctl.Linux.tiocsserial(fd, serial);
		}

		private static void verifyCustomBaud(int fd, int baud, double maxDiff) throws CException {
			var serial = CIoctl.Linux.tiocgserial(fd);
			int actual = serial.baud_base / serial.custom_divisor;
			double diff = Math.abs(((double) (actual - baud)) / baud);
			if (diff > maxDiff) throw CException.general(
				"Failed to set custom baud: %d (closest %d, error %.01f%%)", baud, actual,
				Maths.toPercent(diff));
		}
	}
}
