package ceri.serial.clib.jna;

import static ceri.serial.clib.jna.CLib._IO;
import ceri.common.util.OsUtil;

/**
 * Common ioctls.
 */
public class Ioctls {

	private Ioctls() {}

	/* sys/ioctl.h */
	
	private static final int TIOCSBRK;
	private static final int TIOCCBRK;

	/**
	 * Set break bit.
	 */
	public static void tiocsbrk(int fd) throws CException {
		CLib.ioctl("TIOCSBRK", fd, TIOCSBRK);
	}

	/**
	 * Clear break bit.
	 */
	public static void tioccbrk(int fd) throws CException {
		CLib.ioctl("TIOCCBRK", fd, TIOCCBRK);
	}

	/* os-specific initialization */

	static {
		if (OsUtil.IS_MAC) {
			/* sys/ttycom.h */
			TIOCSBRK = _IO('t', 123); // 0x2000747b
			TIOCCBRK = _IO('t', 122); // 0x2000747a
		} else {
			/* asm-generic/ioctls.h */
			TIOCSBRK = _IO('T', 0x27); // 0x5427
			TIOCCBRK = _IO('T', 0x28); // 0x5428
		}
	}
}
