package ceri.serial.clib.jna;

import static ceri.common.util.OsUtil.macInt;
import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.common.validation.ValidationUtil.validateUbyte;

/**
 * Common ioctl constants, macros and functions. Default settings for Linux, overrides for Mac.
 */
public class Ioctls {

	private Ioctls() {}

	/* ioccom.h (mac), ioctl.h (linux) */

	public static final int _IOC_SIZEBITS = macInt(CLibMac._IOC_SIZEBITS, CLibLinux._IOC_SIZEBITS);
	private static final int _IOC_SIZEMASK = (1 << _IOC_SIZEBITS) - 1;
	private static final int _IOC_VOID = macInt(CLibMac._IOC_VOID, CLibLinux._IOC_VOID);
	private static final int _IOC_OUT = 0x40000000;
	private static final int _IOC_IN = 0x80000000;
	private static final int _IOC_INOUT = _IOC_IN | _IOC_OUT;

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
		return _IOC(_IOC_VOID, group, num, 0);
	}

	public static int _IOR(int group, int num, int size) {
		return _IOC(_IOC_IN, group, num, size); // sizeof(t)
	}

	public static int _IOW(int group, int num, int size) {
		return _IOC(_IOC_OUT, group, num, size); // sizeof(t)
	}

	public static int _IOWR(int group, int num, int size) {
		return _IOC(_IOC_INOUT, group, num, size); // sizeof(t)
	}

	/* ttycom.h (mac), ioctls.h (linux) */

	private static final int TIOCSBRK = macInt(CLibMac.TIOCSBRK, CLibLinux.TIOCSBRK);
	private static final int TIOCCBRK = macInt(CLibMac.TIOCCBRK, CLibLinux.TIOCCBRK);

	/**
	 * Set break bit.
	 */
	public static void tiocsbrk(int fd) throws CException {
		// CUtil.verify(JTermios.ioctl(fd, TIOCSBRK), "ioctl:TIOCSBRK");
		CLib.ioctl("TIOCSBRK", fd, TIOCSBRK);
	}

	/**
	 * Clear break bit.
	 */
	public static void tioccbrk(int fd) throws CException {
		// CUtil.verify(JTermios.ioctl(fd, TIOCCBRK), "ioctl:TIOCCBRK");
		CLib.ioctl("TIOCCBRK", fd, TIOCCBRK);
	}

}
