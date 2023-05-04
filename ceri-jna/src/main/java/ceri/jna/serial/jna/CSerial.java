package ceri.jna.serial.jna;

import static ceri.common.exception.ExceptionUtil.exceptionf;
import static ceri.jna.clib.jna.CFcntl.O_NOCTTY;
import static ceri.jna.clib.jna.CFcntl.O_NONBLOCK;
import static ceri.jna.clib.jna.CFcntl.O_RDWR;
import static ceri.jna.clib.jna.CIoctl.Linux.ASYNC_SPD_CUST;
import static ceri.jna.clib.jna.CIoctl.Linux.ASYNC_SPD_MASK;
import static ceri.jna.clib.jna.CTermios.B0;
import static ceri.jna.clib.jna.CTermios.B1000000;
import static ceri.jna.clib.jna.CTermios.B110;
import static ceri.jna.clib.jna.CTermios.B115200;
import static ceri.jna.clib.jna.CTermios.B1152000;
import static ceri.jna.clib.jna.CTermios.B1200;
import static ceri.jna.clib.jna.CTermios.B134;
import static ceri.jna.clib.jna.CTermios.B150;
import static ceri.jna.clib.jna.CTermios.B1500000;
import static ceri.jna.clib.jna.CTermios.B1800;
import static ceri.jna.clib.jna.CTermios.B19200;
import static ceri.jna.clib.jna.CTermios.B200;
import static ceri.jna.clib.jna.CTermios.B2000000;
import static ceri.jna.clib.jna.CTermios.B230400;
import static ceri.jna.clib.jna.CTermios.B2400;
import static ceri.jna.clib.jna.CTermios.B2500000;
import static ceri.jna.clib.jna.CTermios.B300;
import static ceri.jna.clib.jna.CTermios.B3000000;
import static ceri.jna.clib.jna.CTermios.B3500000;
import static ceri.jna.clib.jna.CTermios.B38400;
import static ceri.jna.clib.jna.CTermios.B4000000;
import static ceri.jna.clib.jna.CTermios.B460800;
import static ceri.jna.clib.jna.CTermios.B4800;
import static ceri.jna.clib.jna.CTermios.B50;
import static ceri.jna.clib.jna.CTermios.B500000;
import static ceri.jna.clib.jna.CTermios.B57600;
import static ceri.jna.clib.jna.CTermios.B576000;
import static ceri.jna.clib.jna.CTermios.B600;
import static ceri.jna.clib.jna.CTermios.B75;
import static ceri.jna.clib.jna.CTermios.B921600;
import static ceri.jna.clib.jna.CTermios.B9600;
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
import com.sun.jna.NativeLong;
import ceri.common.collection.BiMap;
import ceri.common.util.OsUtil;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CFcntl;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.clib.jna.CLib;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.util.Struct;

public class CSerial {
	private static final int BAUD_UNSUPPORTED = -1;
	private static final int DC1 = 0x11; // ^Q device control 1
	private static final int DC3 = 0x13; // ^S device control 3
	private static final BiMap<Integer, Integer> baudMap = baudMap();
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

	public static int open(String port) throws CException {
		CLib.validateOs();
		int fd = CFcntl.open(port, O_RDWR | O_NOCTTY | O_NONBLOCK);
		try {
			clearNonBlock(fd);
			if (OsUtil.os().mac) Mac.initPort(fd);
			else Linux.initPort(fd);
			return fd;
		} catch (CException | RuntimeException e) {
			CUnistd.close(fd);
			throw e;
		}
	}

	public static void setParams(int fd, int baud, int dataBits, int stopBits, int parity)
		throws CException {
		CLib.validateOs();
		if (OsUtil.os().mac) Mac.setParams(fd, baud, dataBits, stopBits, parity);
		else Linux.setParams(fd, baud, dataBits, stopBits, parity);
	}

	public static void setFlowControl(int fd, int mode) throws CException {
		CLib.validateOs();
		if (OsUtil.os().mac) Mac.setFlowControl(fd, mode);
		else Linux.setFlowControl(fd, mode);
	}

	private static void clearNonBlock(int fd) throws CException {
		int flags = CFcntl.getFl(fd);
		CFcntl.setFl(fd, flags & ~O_NONBLOCK);
	}

	private static void initTermios(NativeLong iflag, NativeLong cflag, byte[] cc) {
		iflag.setValue(iflag.longValue() & ~(IXANY | IXOFF | IXON));
		cflag.setValue((cflag.longValue() & ~(CSIZE | CSTOPB | PARENB | CMSPAR | PARODD | CRTSCTS))
			| CLOCAL | CREAD | CS8);
		cc[VSTART] = DC1;
		cc[VSTOP] = DC3;
		cc[VMIN] = 0;
		cc[VTIME] = 0;
	}

	private static void setParams(NativeLong cflag, int dataBits, int stopBits, int parity) {
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
		throw exceptionf("Unsupported parity: 0x%02x", parity);
	}

	private static void setFlowControl(NativeLong iflag, NativeLong cflag, int mode) {
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

	private static <T extends CTermios.termios> void setBaudCode(T tty, int baudCode)
		throws CException {
		Struct.write(tty);
		CTermios.cfsetispeed(tty, baudCode);
		CTermios.cfsetospeed(tty, baudCode);
	}

	private static int baudCode(int baud) {
		return baudMap.values.getOrDefault(baud, BAUD_UNSUPPORTED);
	}

	private static BiMap<Integer, Integer> baudMap() {
		return BiMap.<Integer, Integer>builder().put(B0, 0).put(B50, 50).put(B75, 75).put(B110, 110)
			.put(B134, 134).put(B150, 150).put(B200, 200).put(B300, 300).put(B600, 600)
			.put(B1200, 1200).put(B1800, 1800).put(B2400, 2400).put(B4800, 4800).put(B9600, 9600)
			.put(B19200, 19200).put(B38400, 38400).put(B57600, 57600).put(B115200, 115200)
			.put(B230400, 230400).put(B460800, 460800).put(B500000, 500000).put(B576000, 576000)
			.put(B921600, 921600).put(B1000000, 1000000).put(B1152000, 1152000)
			.put(B1500000, 1500000).put(B2000000, 2000000).put(B2500000, 2500000)
			.put(B3000000, 3000000).put(B3500000, 3500000).put(B4000000, 4000000).build();
	}

	/* os-specific types and calls */

	public static final class Mac {
		private Mac() {}

		private static void initPort(int fd) throws CException {
			var tty = CTermios.Mac.tcgetattr(fd);
			CTermios.cfmakeraw(tty);
			CSerial.initTermios(tty.c_iflag, tty.c_cflag, tty.c_cc);
			CSerial.setBaudCode(tty, B9600);
			CTermios.tcsetattr(fd, TCSANOW, tty);
		}

		private static void setParams(int fd, int baud, int dataBits, int stopBits, int parity)
			throws CException {
			var tty = CTermios.Mac.tcgetattr(fd);
			CSerial.setParams(tty.c_cflag, dataBits, stopBits, parity);
			int baudCode = CSerial.baudCode(baud);
			CSerial.setBaudCode(tty, baudCode == BAUD_UNSUPPORTED ? B9600 : baudCode);
			CTermios.tcsetattr(fd, TCSANOW, tty);
			if (baudCode == BAUD_UNSUPPORTED) CIoctl.Mac.iossiospeed(fd, baud);
		}

		private static void setFlowControl(int fd, int mode) throws CException {
			var tty = CTermios.Mac.tcgetattr(fd);
			CSerial.setFlowControl(tty.c_iflag, tty.c_cflag, mode);
			CTermios.tcsetattr(fd, TCSANOW, tty);
		}
	}

	public static final class Linux {
		private Linux() {}

		private static void initPort(int fd) throws CException {
			var tty = CTermios.Linux.tcgetattr(fd);
			CTermios.cfmakeraw(tty);
			CSerial.initTermios(tty.c_iflag, tty.c_cflag, tty.c_cc);
			CSerial.setBaudCode(tty, B9600);
			CTermios.tcsetattr(fd, TCSANOW, tty);
		}

		private static void setParams(int fd, int baud, int dataBits, int stopBits, int parity)
			throws CException {
			var tty = CTermios.Linux.tcgetattr(fd);
			CSerial.setParams(tty.c_cflag, dataBits, stopBits, parity);
			setBaud(fd, tty, baud);
			CTermios.tcsetattr(fd, TCSANOW, tty);
		}

		private static void setFlowControl(int fd, int mode) throws CException {
			var tty = CTermios.Linux.tcgetattr(fd);
			CSerial.setFlowControl(tty.c_iflag, tty.c_cflag, mode);
			CTermios.tcsetattr(fd, TCSANOW, tty);
		}

		private static void setBaud(int fd, CTermios.Linux.termios tty, int baud)
			throws CException {
			int baudCode = CSerial.baudCode(baud);
			if (baudCode == BAUD_UNSUPPORTED) {
				enableCustomBaud(fd, baud);
				verifyCustomBaud(fd, baud);
				CSerial.setBaudCode(tty, B38400);
			} else {
				disableCustomBaud(fd);
				CSerial.setBaudCode(tty, baudCode);
			}
		}

		private static void disableCustomBaud(int fd) throws CException {
			var serial = CIoctl.Linux.tiocgserial(fd);
			serial.flags &= ~ASYNC_SPD_MASK;
			CIoctl.Linux.tiocsserial(fd, serial);
		}

		private static void enableCustomBaud(int fd, int baud) throws CException {
			var serial = CIoctl.Linux.tiocgserial(fd);
			serial.flags = (serial.flags & ~ASYNC_SPD_MASK) | ASYNC_SPD_CUST;
			serial.custom_divisor = Math.min((serial.baud_base + (baud / 2)) / baud, 1);
			CIoctl.Linux.tiocsserial(fd, serial);
		}

		private static void verifyCustomBaud(int fd, int baud) throws CException {
			var serial = CIoctl.Linux.tiocgserial(fd);
			if (serial.custom_divisor * baud == serial.baud_base) return;
			throw CException.general("Failed to set custom baud: %d / %d != %d", serial.baud_base,
				serial.custom_divisor, baud);
		}
	}
}
