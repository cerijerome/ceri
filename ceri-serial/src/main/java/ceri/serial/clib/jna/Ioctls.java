package ceri.serial.clib.jna;

import static ceri.common.util.OsUtil.macInt;
import static ceri.serial.clib.jna.Ioctl._IO;
import static ceri.serial.clib.jna.Ioctl._IOW;
import static ceri.serial.clib.jna.Termios.Size.SPEED_T;

/**
 * Standard ioctl command code. From ttycom.h, ioss.h (Mac) and ioctls.h (Linux).
 */
public class Ioctls {
	/** Set break bit */
	public static final int TIOCSBRK = macInt(_IO('t', 123), _IO('T', 0x27)); // 0x2000747b, 0x5427
	/** Clear break bit */
	public static final int TIOCCBRK = macInt(_IO('t', 122), _IO('T', 0x28)); // 0x2000747a, 0x5428
	/** Sets input and output speeds to a non-traditional baud rate (Mac only) */
	public static final int IOSSIOSPEED = macInt(_IOW('T', 2, SPEED_T), -1);

	private Ioctls() {}
}
