package ceri.serial.jna.sys;

import static ceri.serial.jna.sys.IocCom._IOW;

public class Ioss {
	private static final int SIZEOF_SPEED_T = SizeOf.INT;

	/* External clock baud rates, for use with cfsetospeed */
	public static final int BEXT1 = _MAKE_EXT(1);
	public static final int BEXT2 = _MAKE_EXT(2);
	public static final int BEXT4 = _MAKE_EXT(4);
	public static final int BEXT8 = _MAKE_EXT(8);
	public static final int BEXT16 = _MAKE_EXT(16);
	public static final int BEXT32 = _MAKE_EXT(32);
	public static final int BEXT64 = _MAKE_EXT(64);
	public static final int BEXT128 = _MAKE_EXT(128);
	public static final int BEXT256 = _MAKE_EXT(256);

	private static final int SIZEOF_USER_UL_T = SizeOf.UINT64;
	private static final int SIZEOF_USER_SPEED_T = SizeOf.UINT64;
	private static final int SIZEOF_USER_US_T = SizeOf.UINT32;
	private static final int SIZEOF_USER_SHSPEED_T = SizeOf.UINT32;

	/*
	 * Sets the receive latency (in microseconds) with the default value of 0 meaning a 256 / 3
	 * character delay latency.
	 */
	public static final int IOSSDATALAT = _IOW('T', 0, SizeOf.UNSIGNED_LONG);
	public static final int IOSSDATALAT_32 = _IOW('T', 0, SIZEOF_USER_US_T);
	public static final int IOSSDATALAT_64 = _IOW('T', 0, SIZEOF_USER_UL_T);

	/*
	 * Controls the pre-emptible status of IOSS based serial dial in devices (i.e. /dev/tty.*
	 * devices). If true an open tty.* device is pre-emptible by a dial out call. Once a dial in
	 * call is established then setting pre-empt to false will halt any further call outs on the cu
	 * device.
	 */
	public static final int IOSSPREEMPT = _IOW('T', 1, SizeOf.INT);

	/*
	 * Sets the input speed and output speed to a non-traditional baud rate
	 */
	public static final int IOSSIOSPEED = _IOW('T', 2, SIZEOF_SPEED_T);
	public static final int IOSSIOSPEED_32 = _IOW('T', 2, SIZEOF_USER_SHSPEED_T);
	public static final int IOSSIOSPEED_64 = _IOW('T', 2, SIZEOF_USER_SPEED_T);

	private Ioss() {}

	/**
	 * _MAKE_EXT(x)
	 */
	private static int _MAKE_EXT(int x) {
		return (x << 1) | 1;
	}

}
