package ceri.serial.clib.jna;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

/**
 * For C functionality not available from purejavacomm JTermios.
 */
public interface CLibNative extends Library {

	// extern int ioctl (int __fd, unsigned long int __request, ...) __THROW;
	int ioctl(int fd, int request, Object... objs);

	int open(String path, int flags);

	int close(int fd);

	int read(int fd, Pointer buffer, int len);

	int write(int fd, Pointer buffer, int len);

}
