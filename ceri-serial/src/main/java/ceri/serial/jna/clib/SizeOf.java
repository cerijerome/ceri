package ceri.serial.jna.clib;

import com.sun.jna.NativeLong;

public class SizeOf {
	public static final int CHAR = Byte.BYTES;
	public static final int SHORT = Short.BYTES;
	public static final int INT = Integer.BYTES;
	public static final int LONG = NativeLong.SIZE;
	public static final int UNSIGNED_LONG = LONG;
	public static final int UINT32 = Integer.BYTES;
	public static final int UINT64 = Long.BYTES;
	
	public static final int SPEED_T = INT;

	private SizeOf() {}

}
