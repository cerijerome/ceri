package ceri.jna.clib.jna;

import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.common.validation.ValidationUtil.validateUbyte;
import static ceri.jna.clib.jna.CLib.caller;
import static ceri.jna.clib.jna.CLib.lib;
import java.util.function.Supplier;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import ceri.common.util.OsUtil;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.Struct;
import ceri.jna.util.Struct.Fields;

/**
 * Types and functions from {@code <sys/ioctl.h>}
 */
public class CIoctl {
	public static final int _IOC_SIZEBITS;
	private static final int _IOC_SIZEMASK;
	private static final int IOC_VOID;
	private static final int IOC_OUT;
	private static final int IOC_IN;
	static final int TIOCSBRK;
	static final int TIOCCBRK;
	static final int FIONREAD;
	static final int TIOCEXCL;
	static final int TIOCMGET;
	static final int TIOCMSET;
	public static final int TIOCM_LE = 0x0001; // line enable
	public static final int TIOCM_DTR = 0x0002; // data terminal ready
	public static final int TIOCM_RTS = 0x0004; // request to send
	public static final int TIOCM_CTS = 0x0020; // clear to send
	public static final int TIOCM_CD = 0x0040; // carrier detect
	public static final int TIOCM_RI = 0x0080; // ring
	public static final int TIOCM_DSR = 0x0100; // data set ready

	private CIoctl() {}

	/**
	 * <pre>
	 * |xxxxxxxx|xxxxxxxx|xxxxxxxx|xxxxxxxx| value
	 * |--------|--------|--------|--------|
	 * |xx000000|00000000|00000000|00000000| in/out (2)
	 * |  ?xxxxx|xxxxxxxx|        |        | size (14|13)
	 * |        |        |xxxxxxxx|        | group (8)
	 * |        |        |        |xxxxxxxx| num (8)
	 * </pre>
	 */
	public static int _IOC(int inOut, int group, int num, int size) {
		validateUbyte(group, "Group");
		validateUbyte(num, "Num");
		validateRange(size, 0, _IOC_SIZEMASK, "Size");
		return inOut | ((size & _IOC_SIZEMASK) << Short.SIZE) | (group << Byte.SIZE) | num;
	}

	public static int _IO(int group, int num) {
		return _IOC(IOC_VOID, group, num, 0);
	}

	public static int _IOR(int group, int num, int size) {
		return _IOC(IOC_OUT, group, num, size); // sizeof(t)
	}

	public static int _IOW(int group, int num, int size) {
		return _IOC(IOC_IN, group, num, size); // sizeof(t)
	}

	public static int _IOWR(int group, int num, int size) {
		return _IOC(IOC_IN | IOC_OUT, group, num, size); // sizeof(t)
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public static int ioctl(int fd, int request, Object... objs) throws CException {
		return ioctl("", fd, request, objs);
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public static int ioctl(String name, int fd, int request, Object... objs) throws CException {
		var n = JnaUtil.unlong(request);
		return caller.verifyInt(() -> lib().ioctl(fd, n, objs), "ioctl:" + name, fd, request, objs);
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public static int ioctl(Supplier<String> errorMsg, int fd, int request, Object... objs)
		throws CException {
		var n = JnaUtil.unlong(request);
		return caller.verifyInt(() -> lib().ioctl(fd, n, objs), errorMsg);
	}

	/**
	 * Set break bit.
	 */
	public static void tiocsbrk(int fd) throws CException {
		ioctl("TIOCSBRK", fd, TIOCSBRK);
	}

	/**
	 * Clear break bit.
	 */
	public static void tioccbrk(int fd) throws CException {
		ioctl("TIOCCBRK", fd, TIOCCBRK);
	}

	/**
	 * Get the number of bytes in the input buffer.
	 */
	public static int fionread(int fd) throws CException {
		var ref = JnaUtil.intRef(0);
		ioctl("FIONREAD", fd, FIONREAD, ref);
		return ref.getValue();
	}

	/**
	 * Put the terminal into exclusive mode; no further open() is permitted.
	 */
	public static void tiocexcl(int fd) throws CException {
		ioctl("TIOCEXCL", fd, TIOCEXCL);
	}

	/**
	 * Get the status of modem bits TIOCM_*.
	 */
	public static int tiocmget(int fd) throws CException {
		var ref = JnaUtil.intRef(0);
		ioctl("TIOCMGET", fd, TIOCMGET, ref);
		return ref.getValue();
	}

	/**
	 * Set the status of modem bits TIOCM_*.
	 */
	public static void tiocmset(int fd, int bits) throws CException {
		var ref = JnaUtil.intRef(bits);
		ioctl("TIOCMSET", fd, TIOCMSET, ref);
	}

	/**
	 * Types and calls specific to Mac.
	 */
	public static final class Mac {
		private Mac() {}

		/* <IOKit/serial/ioss.h> */

		static final int IOSSIOSPEED = _IOW('T', 2, NativeLong.SIZE); // 0x80085402

		/**
		 * Sets input and output speeds to a non-traditional baud rate. Value is not represented in
		 * struct termios.
		 */
		public static void iossiospeed(int fd, int speed) throws CException {
			var p = JnaUtil.unlongRefPtr(speed);
			ioctl("IOSSIOSPEED", fd, IOSSIOSPEED, p);
		}
	}

	/**
	 * Types and calls specific to Linux.
	 */
	public static final class Linux {
		private Linux() {}

		/* <linux/serial.h> */

		public static final int ASYNC_SPD_HI = 1 << 4; // 0x0010; use 56000bps
		public static final int ASYNC_SPD_VHI = 1 << 5; // 0x0020; use 115200bps
		public static final int ASYNC_SPD_SHI = 1 << 12; // 0x1000; use 230400bps
		public static final int ASYNC_SPD_CUST = ASYNC_SPD_HI | ASYNC_SPD_VHI;
		public static final int ASYNC_SPD_MASK = ASYNC_SPD_HI | ASYNC_SPD_VHI | ASYNC_SPD_SHI;

		@Fields({ "type", "line", "port", "irq", "flags", "xmit_fifo_size", "custom_divisor",
			"baud_base", "close_delay", "io_type", "reserved_char", "hub6", "closing_wait",
			"closing_wait2", "iomem_base", "iomem_reg_shift", "port_high", "iomap_base" })
		public static class serial_struct extends Struct {
			public int type;
			public int line;
			public int port;
			public int irq;
			public int flags;
			public int xmit_fifo_size;
			public int custom_divisor;
			public int baud_base;
			public short close_delay;
			public byte io_type;
			public byte reserved_char;
			public int hub6;
			public short closing_wait;
			public short closing_wait2;
			public Pointer iomem_base;
			public short iomem_reg_shift;
			public int port_high;
			public NativeLong iomap_base;
		}

		/* <sys/ioctl.h> */

		static final int TIOCGSERIAL = _IO('T', 0x1e); // 0x541e;
		static final int TIOCSSERIAL = _IO('T', 0x1f); // 0x541f;

		public static serial_struct tiocgserial(int fd) throws CException {
			var serial = new serial_struct();
			ioctl(fd, TIOCGSERIAL, serial); // needs Struct.read(serial) ?
			return serial;
		}

		public static void tiocsserial(int fd, serial_struct serial) throws CException {
			ioctl(fd, TIOCSSERIAL, serial); // needs Struct.write(serial) ?
		}
	}

	/* os-specific initialization */

	static {
		if (OsUtil.os().mac) {
			_IOC_SIZEBITS = 13; // IOCPARM_MASK = 0x1fff;
			_IOC_SIZEMASK = (1 << _IOC_SIZEBITS) - 1;
			IOC_VOID = 0x20000000;
			IOC_OUT = 0x40000000;
			IOC_IN = 0x80000000;
			TIOCSBRK = _IO('t', 123); // 0x2000747b
			TIOCCBRK = _IO('t', 122); // 0x2000747a
			FIONREAD = _IOR('f', 127, Integer.BYTES); // 0x4004667f
			TIOCEXCL = _IO('t', 13); // 0x2000740d
			TIOCMGET = _IOR('t', 106, Integer.BYTES); // 0x4004746a
			TIOCMSET = _IOW('t', 109, Integer.BYTES); // 0x8004746d
		} else {
			_IOC_SIZEBITS = 14;
			_IOC_SIZEMASK = (1 << _IOC_SIZEBITS) - 1;
			IOC_VOID = 0;
			IOC_OUT = 0x80000000;
			IOC_IN = 0x40000000;
			TIOCSBRK = 0x5427;
			TIOCCBRK = 0x5428;
			FIONREAD = 0x541b;
			TIOCEXCL = 0x540c;
			TIOCMGET = 0x5415;
			TIOCMSET = 0x5418;
		}
	}

}
