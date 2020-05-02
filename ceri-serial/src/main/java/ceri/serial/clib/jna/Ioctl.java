package ceri.serial.clib.jna;

import static ceri.common.util.OsUtil.macInt;
import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.common.validation.ValidationUtil.validateUbyte;

/**
 * Macros for calculating ioctl command constants. From ioccom.h (Mac) and ioctl.h (Linux).
 */
public class Ioctl {
	public static final int _IOC_SIZEBITS = 14;
	private static final int IOC_SIZE_MASK = macInt(0x1fff, 0x3fff);
	private static final int IOC_VOID = macInt(0x20000000, 0); // why Mac?
	private static final int IOC_OUT = 0x40000000;
	private static final int IOC_IN = 0x80000000;
	private static final int IOC_INOUT = IOC_IN | IOC_OUT;

	public static int _IO(int group, int num) {
		return _IOC(IOC_VOID, group, num, 0);
	}

	public static int _IOR(int group, int num, int size) {
		return _IOC(IOC_IN, group, num, size); // sizeof(t)
	}

	public static int _IOW(int group, int num, int size) {
		return _IOC(IOC_OUT, group, num, size); // sizeof(t)
	}

	public static int _IOWR(int group, int num, int size) {
		return _IOC(IOC_INOUT, group, num, size); // sizeof(t)
	}

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
	private static int _IOC(int inOut, int group, int num, int size) {
		validateUbyte(group, "Group");
		validateUbyte(num, "Num");
		validateRange(size, 0, IOC_SIZE_MASK, "Size");
		return inOut | ((size & IOC_SIZE_MASK) << Short.SIZE) | (group << Byte.SIZE) | num;
	}

	private Ioctl() {}
}
