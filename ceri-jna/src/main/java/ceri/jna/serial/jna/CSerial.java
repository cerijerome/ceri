package ceri.jna.serial.jna;

import static ceri.common.exception.ExceptionUtil.exceptionf;
import static ceri.jna.clib.jna.CLib.B0;
import static ceri.jna.clib.jna.CLib.B1000000;
import static ceri.jna.clib.jna.CLib.B110;
import static ceri.jna.clib.jna.CLib.B115200;
import static ceri.jna.clib.jna.CLib.B1152000;
import static ceri.jna.clib.jna.CLib.B1200;
import static ceri.jna.clib.jna.CLib.B134;
import static ceri.jna.clib.jna.CLib.B150;
import static ceri.jna.clib.jna.CLib.B1500000;
import static ceri.jna.clib.jna.CLib.B1800;
import static ceri.jna.clib.jna.CLib.B19200;
import static ceri.jna.clib.jna.CLib.B200;
import static ceri.jna.clib.jna.CLib.B2000000;
import static ceri.jna.clib.jna.CLib.B230400;
import static ceri.jna.clib.jna.CLib.B2400;
import static ceri.jna.clib.jna.CLib.B2500000;
import static ceri.jna.clib.jna.CLib.B300;
import static ceri.jna.clib.jna.CLib.B3000000;
import static ceri.jna.clib.jna.CLib.B3500000;
import static ceri.jna.clib.jna.CLib.B38400;
import static ceri.jna.clib.jna.CLib.B4000000;
import static ceri.jna.clib.jna.CLib.B460800;
import static ceri.jna.clib.jna.CLib.B4800;
import static ceri.jna.clib.jna.CLib.B50;
import static ceri.jna.clib.jna.CLib.B500000;
import static ceri.jna.clib.jna.CLib.B57600;
import static ceri.jna.clib.jna.CLib.B576000;
import static ceri.jna.clib.jna.CLib.B600;
import static ceri.jna.clib.jna.CLib.B75;
import static ceri.jna.clib.jna.CLib.B921600;
import static ceri.jna.clib.jna.CLib.B9600;
import static ceri.jna.clib.jna.CLib.CLOCAL;
import static ceri.jna.clib.jna.CLib.CMSPAR;
import static ceri.jna.clib.jna.CLib.CREAD;
import static ceri.jna.clib.jna.CLib.CRTSCTS;
import static ceri.jna.clib.jna.CLib.CSIZE;
import static ceri.jna.clib.jna.CLib.CSTOPB;
import static ceri.jna.clib.jna.CLib.F_GETFL;
import static ceri.jna.clib.jna.CLib.F_SETFL;
import static ceri.jna.clib.jna.CLib.IXANY;
import static ceri.jna.clib.jna.CLib.IXOFF;
import static ceri.jna.clib.jna.CLib.IXON;
import static ceri.jna.clib.jna.CLib.O_NOCTTY;
import static ceri.jna.clib.jna.CLib.O_NONBLOCK;
import static ceri.jna.clib.jna.CLib.O_RDWR;
import static ceri.jna.clib.jna.CLib.PARENB;
import static ceri.jna.clib.jna.CLib.PARODD;
import static ceri.jna.clib.jna.CLib.TCSANOW;
import static ceri.jna.clib.jna.CLib.VMIN;
import static ceri.jna.clib.jna.CLib.VSTART;
import static ceri.jna.clib.jna.CLib.VSTOP;
import static ceri.jna.clib.jna.CLib.VTIME;
import static ceri.jna.clib.jna.CLib.Linux.ASYNC_SPD_CUST;
import static ceri.jna.clib.jna.CLib.Linux.ASYNC_SPD_MASK;
import com.sun.jna.NativeLong;
import ceri.common.collection.BiMap;
import ceri.common.data.ByteUtil;
import ceri.common.test.BinaryPrinter;
import ceri.common.util.OsUtil;
import ceri.jna.clib.OpenFlag;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CLib;
import ceri.jna.util.Struct;

public class CSerial {
	private static final int BAUD_UNSUPPORTED = -1;
	private static final int DC1 = 0x11;
	private static final int DC3 = 0x13;
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
		int fd = CLib.open(port, O_RDWR | O_NOCTTY | O_NONBLOCK);
		try {
			clearNonBlock(fd);
			if (OsUtil.IS_MAC) Mac.initPort(fd);
			else Linux.initPort(fd);
			return fd;
		} catch (CException | RuntimeException e) {
			CLib.close(fd);
			throw e;
		}
	}

	public static void setParams(int fd, int baud, int dataBits, int stopBits, int parity)
		throws CException {
		CLib.validateOs();
		if (OsUtil.IS_MAC) Mac.setParams(fd, baud, dataBits, stopBits, parity);
		else Linux.setParams(fd, baud, dataBits, stopBits, parity);
	}

	public static void setFlowControl(int fd, int mode) throws CException {
		CLib.validateOs();
		if (OsUtil.IS_MAC) Mac.setFlowControl(fd, mode);
		else Linux.setFlowControl(fd, mode);
	}

	private static void clearNonBlock(int fd) throws CException {
		int flags = CLib.fcntl(fd, F_GETFL) & ~O_NONBLOCK;
		CLib.fcntl(fd, F_SETFL, flags);
	}

	private static void initTermios(NativeLong iflag, NativeLong cflag, byte[] cc) {
		long cf = cflag.longValue() | CLOCAL | CREAD;
		cf = configureParamCflag(cf, DATABITS_8, STOPBITS_1, PARITY_NONE);
		cf = configureFlowControlCflag(cf, FLOWCONTROL_NONE);
		cflag.setValue(cf);
		iflag.setValue(configureFlowControlIflag(iflag.longValue(), FLOWCONTROL_NONE));
		cc[VSTART] = DC1;
		cc[VSTOP] = DC3;
		cc[VMIN] = 0;
		cc[VTIME] = 0;
	}

	private static void setParamCflag(NativeLong cflag, int dataBits, int stopBits, int parity) {
		cflag.setValue(configureParamCflag(cflag.longValue(), dataBits, stopBits, parity));
	}

	private static long configureParamCflag(long cflag, int dataBits, int stopBits, int parity) {
		cflag &= ~(CSIZE | CSTOPB | PARENB | CMSPAR | PARODD);
		cflag |= dataBitFlag(dataBits) | stopBitFlag(stopBits) | parityFlags(parity);
		return cflag;
	}

	private static long dataBitFlag(int dataBits) {
		return switch (dataBits) {
		case DATABITS_5 -> CLib.CS5;
		case DATABITS_6 -> CLib.CS6;
		case DATABITS_7 -> CLib.CS7;
		case DATABITS_8 -> CLib.CS8;
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

	private static long configureFlowControlCflag(long cflag, int mode) {
		cflag &= ~CRTSCTS;
		if ((mode & (FLOWCONTROL_RTSCTS_IN | FLOWCONTROL_RTSCTS_OUT)) != 0) cflag |= CRTSCTS;
		return cflag;
	}

	private static long configureFlowControlIflag(long iflag, int mode) {
		iflag &= ~(IXANY | IXOFF | IXON);
		if ((mode & FLOWCONTROL_XONXOFF_IN) != 0) iflag |= IXOFF;
		if ((mode & FLOWCONTROL_XONXOFF_OUT) != 0) iflag |= IXON;
		return iflag;
	}

	private static <T extends CLib.termios> void configureBaudCode(T tty, int baudCode)
		throws CException {
		Struct.write(tty);
		CLib.cfsetispeed(tty, baudCode);
		CLib.cfsetospeed(tty, baudCode);
	}

	private static int printFlags(int flags) {
		BinaryPrinter.STD.print(ByteUtil.toMsb(flags));
		System.out.println(OpenFlag.decode(flags));
		return flags;
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
			var tty = CLib.tcgetattr(fd, new CLib.Mac.termios());
			CLib.cfmakeraw(tty);
			initTermios(tty.c_iflag, tty.c_cflag, tty.c_cc);
			configureBaudCode(tty, B9600);
			CLib.tcsetattr(fd, TCSANOW, tty);
		}

		private static void setParams(int fd, int baud, int dataBits, int stopBits, int parity)
			throws CException {
			var tty = CLib.tcgetattr(fd, new CLib.Mac.termios());
			setParamCflag(tty.c_cflag, dataBits, stopBits, parity);
			int baudCode = baudCode(baud);
			configureBaudCode(tty, baudCode == BAUD_UNSUPPORTED ? B9600 : baudCode);
			CLib.tcsetattr(fd, TCSANOW, tty);
			if (baudCode == BAUD_UNSUPPORTED) CLib.Mac.iossiospeed(fd, baud);
		}

		private static void setFlowControl(int fd, int mode) throws CException {
			var tty = CLib.tcgetattr(fd, new CLib.Mac.termios());
			tty.c_iflag.setValue(configureFlowControlIflag(tty.c_iflag.longValue(), mode));
			tty.c_cflag.setValue(configureFlowControlCflag(tty.c_cflag.longValue(), mode));
			CLib.tcsetattr(fd, TCSANOW, tty);
		}
	}

	public static final class Linux {
		private Linux() {}

		private static void initPort(int fd) throws CException {
			var tty = CLib.tcgetattr(fd, new CLib.Linux.termios());
			CLib.cfmakeraw(tty);
			initTermios(tty.c_iflag, tty.c_cflag, tty.c_cc);
			configureBaudCode(tty, B9600);
			CLib.tcsetattr(fd, TCSANOW, tty);
		}

		private static void setParams(int fd, int baud, int dataBits, int stopBits, int parity)
			throws CException {
			var tty = CLib.tcgetattr(fd, new CLib.Linux.termios());
			setParamCflag(tty.c_cflag, dataBits, stopBits, parity);
			configureBaud(fd, tty, baud);
			CLib.tcsetattr(fd, TCSANOW, tty);
		}

		private static void setFlowControl(int fd, int mode) throws CException {
			var tty = CLib.tcgetattr(fd, new CLib.Linux.termios());
			tty.c_iflag.setValue(configureFlowControlIflag(tty.c_iflag.longValue(), mode));
			tty.c_cflag.setValue(configureFlowControlCflag(tty.c_cflag.longValue(), mode));
			CLib.tcsetattr(fd, TCSANOW, tty);
		}
		
		private static void configureBaud(int fd, CLib.Linux.termios tty, int baud)
			throws CException {
			int baudCode = CSerial.baudCode(baud);
			if (baudCode == BAUD_UNSUPPORTED) {
				enableCustomBaud(fd, baud);
				verifyCustomBaud(fd, baud);
				configureBaudCode(tty, B38400);
			} else {
				disableCustomBaud(fd);
				configureBaudCode(tty, baudCode);
			}
		}

		private static void disableCustomBaud(int fd) throws CException {
			var serial = CLib.Linux.tiocgserial(fd);
			serial.flags &= ~ASYNC_SPD_MASK;
			CLib.Linux.tiocsserial(fd, serial);
		}

		private static void enableCustomBaud(int fd, int baud) throws CException {
			var serial = CLib.Linux.tiocgserial(fd);
			serial.flags = (serial.flags & ~ASYNC_SPD_MASK) | ASYNC_SPD_CUST;
			serial.custom_divisor = Math.min((serial.baud_base + (baud / 2)) / baud, 1);
			CLib.Linux.tiocsserial(fd, serial);
		}

		private static void verifyCustomBaud(int fd, int baud) throws CException {
			var serial = CLib.Linux.tiocgserial(fd);
			if (serial.custom_divisor * baud == serial.baud_base) return;
			throw CException.general("Failed to set custom baud: %d / %d != %d", serial.baud_base,
				serial.custom_divisor, baud);
		}
	}
}
