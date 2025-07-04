package ceri.jna.clib.jna;

import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.common.validation.ValidationUtil.validateUbyte;
import static ceri.jna.clib.jna.CLib.caller;
import static ceri.jna.clib.jna.CLib.lib;
import static ceri.jna.util.JnaOs.linux;
import static ceri.jna.util.JnaOs.mac;
import java.util.function.Supplier;
import com.sun.jna.Pointer;
import ceri.common.util.OsUtil;
import ceri.jna.clib.jna.CTermios.speed_t;
import ceri.jna.reflect.CAnnotations.CInclude;
import ceri.jna.reflect.CAnnotations.CType;
import ceri.jna.type.CUlong;
import ceri.jna.type.Struct;
import ceri.jna.type.Struct.Fields;
import ceri.jna.util.JnaOs;
import ceri.jna.util.JnaUtil;

/**
 * Types and functions from {@code <sys/ioctl.h>}
 */
@CInclude("sys/ioctl.h")
public class CIoctl {
	@CType(os = linux)
	public static final int _IOC_SIZEBITS;
	@CType(os = linux)
	public static final int _IOC_SIZEMASK;
	@CType(os = mac, name = "IOC_VOID")
	@CType(os = linux)
	public static final int _IOC_NONE;
	public static final int IOC_OUT;
	public static final int IOC_IN;
	public static final int TIOCSBRK;
	public static final int TIOCCBRK;
	public static final int FIONREAD;
	public static final int TIOCEXCL;
	public static final int TIOCOUTQ;
	public static final int TIOCMGET;
	public static final int TIOCMBIC;
	public static final int TIOCMBIS;
	public static final int TIOCMSET;
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
		return _IOC(_IOC_NONE, group, num, 0);
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
	public static int ioctl(String name, int fd, int request, Object... objs) throws CException {
		var n = new CUlong(request);
		return caller.verifyInt(() -> lib().ioctl(fd, n, objs), "ioctl:" + name, fd, request, objs);
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public static int ioctl(Supplier<String> errorMsg, int fd, int request, Object... objs)
		throws CException {
		var n = new CUlong(request);
		return caller.verifyInt(() -> lib().ioctl(fd, n, objs), errorMsg);
	}

	/**
	 * Turn break on; start sending zero bits.
	 */
	public static void tiocsbrk(int fd) throws CException {
		ioctl("TIOCSBRK", fd, TIOCSBRK);
	}

	/**
	 * Turn break off; stop sending zero bits.
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
	 * Get the number of bytes in the output buffer.
	 */
	public static int tiocoutq(int fd) throws CException {
		var ref = JnaUtil.intRef(0);
		ioctl("TIOCOUTQ", fd, TIOCOUTQ, ref);
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
	 * Set the modem status bit.
	 */
	public static int tiocmbis(int fd, int bit) throws CException {
		var ref = JnaUtil.intRef(bit);
		ioctl("TIOCMBIS", fd, TIOCMBIS, ref);
		return ref.getValue();
	}

	/**
	 * Clear the modem status bit.
	 */
	public static int tiocmbic(int fd, int bit) throws CException {
		var ref = JnaUtil.intRef(bit);
		ioctl("TIOCMBIC", fd, TIOCMBIC, ref);
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
	 * Set or clear the modem status bit. Not an ioctl type.
	 */
	public static int tiocmbit(int fd, int bit, boolean enable) throws CException {
		return enable ? tiocmbis(fd, bit) : tiocmbic(fd, bit);
	}

	/**
	 * return modem status bit enabled state. Not an ioctl type.
	 */
	public static boolean tiocmbit(int fd, int bit) throws CException {
		return (tiocmget(fd) & bit) != 0;
	}

	/**
	 * Types and calls specific to Mac.
	 */
	@CType(os = JnaOs.mac)
	@CInclude("IOKit/serial/ioss.h")
	public static final class Mac {
		private Mac() {}

		/* <IOKit/serial/ioss.h> */

		public static final int IOSSIOSPEED = _IOW('T', 2, speed_t.SIZE); // 0x80085402

		/**
		 * Sets input and output speeds to a non-traditional baud rate. Value is not represented in
		 * struct termios.
		 */
		public static void iossiospeed(int fd, int speed) throws CException {
			var ref = new speed_t.ByRef(new speed_t(speed));
			ioctl("IOSSIOSPEED", fd, IOSSIOSPEED, ref.getPointer());
		}
	}

	/**
	 * Types and calls specific to Linux.
	 */
	@CType(os = JnaOs.linux)
	@CInclude("linux/serial.h")
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
			public CUlong iomap_base;
		}

		/* <sys/ioctl.h> */

		public static final int TIOCGSERIAL = _IO('T', 0x1e); // 0x541e;
		public static final int TIOCSSERIAL = _IO('T', 0x1f); // 0x541f;

		public static serial_struct tiocgserial(int fd) throws CException {
			var serial = new serial_struct();
			ioctl("TIOCGSERIAL", fd, TIOCGSERIAL, serial);
			return serial;
		}

		public static void tiocsserial(int fd, serial_struct serial) throws CException {
			ioctl("TIOCGSERIAL", fd, TIOCSSERIAL, serial);
		}
	}

	/* os-specific initialization */

	static {
		if (OsUtil.os().mac) {
			_IOC_SIZEBITS = 13; // IOCPARM_MASK = 0x1fff;
			_IOC_SIZEMASK = (1 << _IOC_SIZEBITS) - 1;
			_IOC_NONE = 0x20000000;
			IOC_OUT = 0x40000000;
			IOC_IN = 0x80000000;
			TIOCSBRK = _IO('t', 123); // 0x2000747b
			TIOCCBRK = _IO('t', 122); // 0x2000747a
			FIONREAD = _IOR('f', 127, Integer.BYTES); // 0x4004667f
			TIOCEXCL = _IO('t', 13); // 0x2000740d
			TIOCOUTQ = _IOR('t', 115, Integer.BYTES); // 0x40047473
			TIOCMGET = _IOR('t', 106, Integer.BYTES); // 0x4004746a
			TIOCMBIS = _IOW('t', 108, Integer.BYTES); // 0x8004746c
			TIOCMBIC = _IOW('t', 107, Integer.BYTES); // 0x8004746b
			TIOCMSET = _IOW('t', 109, Integer.BYTES); // 0x8004746d
		} else {
			_IOC_SIZEBITS = 14;
			_IOC_SIZEMASK = (1 << _IOC_SIZEBITS) - 1;
			_IOC_NONE = 0; // _IOC_NONE
			IOC_OUT = 0x80000000;
			IOC_IN = 0x40000000;
			TIOCSBRK = 0x5427;
			TIOCCBRK = 0x5428;
			FIONREAD = 0x541b;
			TIOCEXCL = 0x540c;
			TIOCOUTQ = 0x5411;
			TIOCMGET = 0x5415;
			TIOCMBIS = 0x5416;
			TIOCMBIC = 0x5417;
			TIOCMSET = 0x5418;
		}
	}

}
