package ceri.serial.jna.clib;

import com.sun.jna.Library;

/**
 * For C functionality not available from purejavacomm JTermios.
 */
public interface CLibNative extends Library {

	// extern int ioctl (int __fd, unsigned long int __request, ...) __THROW;
	int ioctl(int fd, int request, Object... objs);

}
