package ceri.serial.clib.jna;

import static ceri.serial.clib.jna.Ioctl._IO;
import static ceri.serial.clib.jna.Ioctl._IOW;
import com.sun.jna.Platform;

/**
 * From ttycom.h, ioss.h (Mac) and ioctls.h (Linux)
 */
public class Ioctls {

	static {
		if (Platform.isMac()) {
			TIOCSBRK = _IO('t', 123); // 0x2000747b
			TIOCCBRK = _IO('t', 122); // 0x2000747a
		} else {
			TIOCSBRK = _IO('T', 0x27); // 0x5427;
			TIOCCBRK = _IO('T', 0x28); // 0x5428;
		}
	}

	/*
	 * Control of serial break bit
	 */
	public static final int TIOCSBRK; /* set break bit */
	public static final int TIOCCBRK; /* clear break bit */

	/*
	 * Sets the input speed and output speed to a non-traditional baud rate (Mac)
	 */
	public static final int IOSSIOSPEED = _IOW('T', 2, SizeOf.SPEED_T);

	private Ioctls() {}

}
