package ceri.serial.jna.sys;

import com.sun.jna.NativeLong;

public class SizeOf {
	public static final int CHAR = 1;
	public static final int SHORT = Short.BYTES;
	public static final int INT = Integer.BYTES;
	public static final int LONG = NativeLong.SIZE;
	public static final int UNSIGNED_LONG = LONG;
	public static final int UINT32 = 32 / Byte.SIZE;
	public static final int UINT64 = 64 / Byte.SIZE;
	
	private SizeOf() {}

}
