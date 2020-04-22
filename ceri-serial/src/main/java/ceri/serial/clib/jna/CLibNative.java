package ceri.serial.clib.jna;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

/**
 * For C functionality not available from purejavacomm JTermios.
 */
public interface CLibNative extends Library {

	int open(String path, int flags) throws LastErrorException;

	int open(String path, int flags, int mode) throws LastErrorException; // int mode for linux?

	int close(int fd) throws LastErrorException;

	int read(int fd, Pointer buffer, int len) throws LastErrorException;

	int write(int fd, Pointer buffer, int len) throws LastErrorException;

	// extern int ioctl (int __fd, unsigned long int __request, ...) __THROW;
	int ioctl(int fd, int request, Object... objs) throws LastErrorException;

	// extern __off_t lseek (int __fd, __off_t __offset, int __whence) __THROW;
	int lseek(int fd, int offset, int whence) throws LastErrorException;
	
}
