package ceri.ffm.clib.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import ceri.common.util.Os;
import ceri.common.util.Validate;
import ceri.ffm.clib.ffm.CTermios.speed_t;
import ceri.ffm.reflect.CAnnotations.CInclude;
import ceri.ffm.reflect.CAnnotations.CType;
import ceri.ffm.type.Group.Fields;
import ceri.ffm.type.IntType.CUlong;
import ceri.ffm.type.Memory;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Struct;
import ceri.ffm.util.FfmOs;

/**
 * Types and functions from {@code <sys/ioctl.h>}
 */
@CInclude("sys/ioctl.h")
public class CIoctl {
	@CType(os = FfmOs.linux)
	public static final int _IOC_SIZEBITS;
	@CType(os = FfmOs.linux)
	public static final int _IOC_SIZEMASK;
	@CType(os = FfmOs.mac, name = "IOC_VOID")
	@CType(os = FfmOs.linux)
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
	 * Generates an ioctl request code.
	 * 
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
		Validate.ubyte(group, "Group");
		Validate.ubyte(num, "Num");
		Validate.range(size, 0, _IOC_SIZEMASK, "Size");
		return inOut | ((size & _IOC_SIZEMASK) << Short.SIZE) | (group << Byte.SIZE) | num;
	}

	/**
	 * Generates an ioctl request code without read or write parameters.
	 */
	public static int _IO(int group, int num) {
		return _IOC(_IOC_NONE, group, num, 0);
	}

	/**
	 * Generates an ioctl request code with read parameters.
	 */
	public static int _IOR(int group, int num, int size) {
		return _IOC(IOC_OUT, group, num, size); // sizeof(t)
	}

	/**
	 * Generates an ioctl request code with write parameters.
	 */
	public static int _IOW(int group, int num, int size) {
		return _IOC(IOC_IN, group, num, size); // sizeof(t)
	}

	/**
	 * Generates an ioctl request code with read and write parameters.
	 */
	public static int _IOWR(int group, int num, int size) {
		return _IOC(IOC_IN | IOC_OUT, group, num, size); // sizeof(t)
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public static int ioctl(int fd, String name, int request, Object... objs) throws CException {
		return CLib.caller.verifyInt(lib -> lib.ioctl(fd, new CUlong(request), objs), -1,
			m -> m.accept("ioctl", fd, m.fmt("%s:0x%x", name, request), objs));
	}

	/**
	 * Turn break on; start sending zero bits.
	 */
	public static void tiocsbrk(int fd) throws CException {
		ioctl(fd, "TIOCSBRK", TIOCSBRK);
	}

	/**
	 * Turn break off; stop sending zero bits.
	 */
	public static void tioccbrk(int fd) throws CException {
		ioctl(fd, "TIOCCBRK", TIOCCBRK);
	}

	/**
	 * Get the number of bytes in the input buffer.
	 */
	public static int fionread(int fd) throws CException {
		try (var arena = Arena.ofConfined()) {
			var argp = Pointer.ofInt(arena, 0);
			ioctl(fd, "FIONREAD", FIONREAD, argp);
			return argp.get();
		}
	}

	/**
	 * Get the number of bytes in the output buffer.
	 */
	public static int tiocoutq(int fd) throws CException {
		try (var arena = Arena.ofConfined()) {
			var argp = Pointer.ofInt(arena, 0);
			ioctl(fd, "TIOCOUTQ", TIOCOUTQ, argp);
			return argp.get();
		}
	}

	/**
	 * Put the terminal into exclusive mode; no further open() is permitted.
	 */
	public static void tiocexcl(int fd) throws CException {
		ioctl(fd, "TIOCEXCL", TIOCEXCL);
	}

	/**
	 * Get the status of modem bits TIOCM_*.
	 */
	public static int tiocmget(int fd) throws CException {
		try (var arena = Arena.ofConfined()) {
			var argp = Pointer.ofInt(arena, 0);
			ioctl(fd, "TIOCMGET", TIOCMGET, argp);
			return argp.get();
		}
	}

	/**
	 * Set the modem status bit.
	 */
	public static void tiocmbis(int fd, int bit) throws CException {
		try (var arena = Arena.ofConfined()) {
			var argp = Pointer.ofInt(arena, bit);
			ioctl(fd, "TIOCMBIS", TIOCMBIS, argp);
		}
	}

	/**
	 * Clear the modem status bit.
	 */
	public static void tiocmbic(int fd, int bit) throws CException {
		try (var arena = Arena.ofConfined()) {
			var argp = Pointer.ofInt(arena, bit);
			ioctl(fd, "TIOCMBIC", TIOCMBIC, argp);
		}
	}

	/**
	 * Set the status of modem bits TIOCM_*.
	 */
	public static void tiocmset(int fd, int bits) throws CException {
		try (var arena = Arena.ofConfined()) {
			var argp = Pointer.ofInt(arena, bits);
			ioctl(fd, "TIOCMSET", TIOCMSET, argp);
		}
	}

	/**
	 * Set or clear the modem status bit. Not an ioctl type.
	 */
	public static void tiocmbit(int fd, int bit, boolean enable) throws CException {
		if (enable) tiocmbis(fd, bit);
		else tiocmbic(fd, bit);
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
	@CType(os = FfmOs.mac)
	@CInclude("IOKit/serial/ioss.h")
	public static final class Mac {
		private Mac() {}

		// <IOKit/serial/ioss.h>

		public static final int IOSSIOSPEED = _IOW('T', 2, speed_t.$.sizeInt(1)); // 0x80085402

		/**
		 * Sets input and output speeds to a non-traditional baud rate. Value is not represented in
		 * struct termios.
		 */
		public static void iossiospeed(int fd, int speed) throws CException {
			try (var arena = Arena.ofConfined()) {
				var argp = speed_t.$.pointerOf(arena, speed);
				ioctl(fd, "IOSSIOSPEED", IOSSIOSPEED, argp);
			}
		}
	}

	/**
	 * Types and calls specific to Linux.
	 */
	@CType(os = FfmOs.linux)
	@CInclude("linux/serial.h")
	public static final class Linux {
		private Linux() {}

		// <linux/serial.h>

		public static final int ASYNC_SPD_HI = 1 << 4; // 0x0010; use 56000bps
		public static final int ASYNC_SPD_VHI = 1 << 5; // 0x0020; use 115200bps
		public static final int ASYNC_SPD_SHI = 1 << 12; // 0x1000; use 230400bps
		public static final int ASYNC_SPD_CUST = ASYNC_SPD_HI | ASYNC_SPD_VHI;
		public static final int ASYNC_SPD_MASK = ASYNC_SPD_HI | ASYNC_SPD_VHI | ASYNC_SPD_SHI;

		/**
		 * Serial port settings.
		 */
		@Fields({ "type", "line", "port", "irq", "flags", "xmit_fifo_size", "custom_divisor",
			"baud_base", "close_delay", "io_type", "reserved_char", "hub6", "closing_wait",
			"closing_wait2", "iomem_base", "iomem_reg_shift", "port_high", "iomap_base" })
		public static class serial_struct extends Struct<serial_struct> {
			public static final Supporter<serial_struct> $ = support(serial_struct.class);
			public int type;
			public int line;
			public int port; // unsigned
			public int irq;
			public int flags;
			public int xmit_fifo_size;
			public int custom_divisor;
			public int baud_base;
			public short close_delay; // unsigned
			public byte io_type;
			public byte reserved_char; // char[1]
			public int hub6;
			public short closing_wait; // unsigned
			public short closing_wait2; // unsigned
			public MemorySegment iomem_base;
			public short iomem_reg_shift; // unsigned
			public int port_high; // unsigned
			public CUlong iomap_base;
		}

		// <sys/ioctl.h>

		public static final int TIOCGSERIAL = _IO('T', 0x1e); // 0x541e;
		public static final int TIOCSSERIAL = _IO('T', 0x1f); // 0x541f;

		/**
		 * Reads serial port settings.
		 */
		public static Pointer<serial_struct> tiocgserial(int fd) throws CException {
			return tiocgserial(Memory.auto(), fd);
		}

		/**
		 * Reads serial port settings.
		 */
		public static Pointer<serial_struct> tiocgserial(SegmentAllocator allocator, int fd)
			throws CException {
			var serial = serial_struct.$.pointer(allocator);
			ioctl(fd, "TIOCGSERIAL", TIOCGSERIAL, serial);
			return serial;
		}

		/**
		 * Writes serial port settings.
		 */
		public static void tiocsserial(int fd, Pointer<serial_struct> serial) throws CException {
			ioctl(fd, "TIOCSSERIAL", TIOCSSERIAL, serial);
		}
	}

	// os-specific initialization

	static {
		if (Os.info().mac) {
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
