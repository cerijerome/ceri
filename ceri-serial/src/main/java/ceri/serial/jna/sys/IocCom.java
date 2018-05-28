package ceri.serial.jna.sys;

/**
 * From sys/ioccom.h
 * 
 * Macros for calculating constants.
 */
public class IocCom {
	private static final int IOCPARM_MASK = 0x1fff;
	public static final int IOCPARM_MAX = IOCPARM_MASK + 1;
	private static final int IOC_VOID = 0x20000000;
	private static final int IOC_OUT = 0x40000000;
	private static final int IOC_IN = 0x80000000;
	private static final int IOC_INOUT = IOC_IN | IOC_OUT;
	public static final int IOC_DIRMASK = 0xe0000000;

	private IocCom() {}

	public static int IOCPARM_LEN(int x) {
		return (x >> Short.SIZE) & IOCPARM_MASK;
	}

	public static int IOCBASECMD(int x) {
		return x & ~(IOCPARM_MASK << Short.SIZE);
	}

	public static int IOCGROUP(int x) {
		return (x >> Byte.SIZE) & 0xff;
	}

	public static int _IOC(int inOut, int group, int num, int len) {
		return inOut | ((len & IOCPARM_MASK) << Short.SIZE) | (group << Byte.SIZE) | num;
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
		return _IOC(IOC_INOUT, group, num, size); // sizeof(t)
	}

}
