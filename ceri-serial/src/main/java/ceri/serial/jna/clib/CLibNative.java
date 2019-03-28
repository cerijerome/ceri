package ceri.serial.jna.clib;

import com.sun.jna.Library;

/**
 * For C functionality not available from purejavacomm JTermios.
 */
public interface CLibNative extends Library {

	int ioctl(int fd, int request, Object... args);

}
