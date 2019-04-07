package ceri.serial.jna.clib;

public class Fcntl {
	public static final int O_RDONLY = 0x0000;
	public static final int O_WRONLY = 0x0001;
	public static final int O_RDWR = 0x0002;

	private Fcntl() {}

	public static boolean isWrite(int flags) {
		return (flags & (O_WRONLY | O_RDWR)) != 0;
	}
	
	public static boolean isRead(int flags) {
		return (flags & O_WRONLY) == 0;
	}
	
	public static boolean isReadWrite(int flags) {
		return (flags & O_RDWR) != 0;
	}
	
}
