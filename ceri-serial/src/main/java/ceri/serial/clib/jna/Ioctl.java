package ceri.serial.clib.jna;

import com.sun.jna.Platform;

/**
 * From ioccom.h (Mac) and ioctl.h (Linux)
 * 
 * Macros for calculating constants.
 */
public class Ioctl {

	static {
		if (Platform.isMac()) {
			IOC_VOID = 0x20000000; // why?
		} else {
			IOC_VOID = 0;
		}
	}

	public static final int _IOC_SIZEBITS = 14;
	private static final int IOCPARM_MASK = 0x3fff;
	private static final int IOC_VOID;
	private static final int IOC_OUT = 0x40000000;
	private static final int IOC_IN = 0x80000000;
	private static final int IOC_INOUT = IOC_IN | IOC_OUT;

	/**
	 * <pre>
	 * |xxxxxxxx|xxxxxxxx|xxxxxxxx|xxxxxxxx| value
	 * |--------|--------|--------|--------|
	 * |xxx00000|00000000|00000000|00000000| inOut (3/32)
	 * |   xxxxx|xxxxxxxx|        |        | len (13)      |
	 * |        |        |xxxxxxxx|        | group (8)
	 * |        |        |        |xxxxxxxx| num (8)
	 * </pre>
	 */
	public static int _IOC(int inOut, int group, int num, int len) {
		return inOut | ((len & IOCPARM_MASK) << Short.SIZE) | (group << Byte.SIZE) | num;
	}

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

	private Ioctl() {}

}
