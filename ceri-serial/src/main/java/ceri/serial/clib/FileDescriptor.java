package ceri.serial.clib;

import static ceri.common.validation.ValidationUtil.validateMin;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.io.IoStreamUtil;
import ceri.serial.clib.jna.CLibUtil;
import ceri.serial.jna.JnaUtil;

/**
 * Encapsulates a file descriptor as a closable resource.
 */
public interface FileDescriptor extends Closeable {
	static final FileDescriptor NULL = new Null();
	static final int BUFFER_SIZE = 1024;

	/**
	 * Provide access to the underlying descriptor.
	 */
	int fd() throws IOException;

	/**
	 * Reads from the file descriptor into a memory pointer. The number of bytes read may be less
	 * than the given length. Returns the number of bytes read, or -1 for EOF.
	 */
	default int read(Memory m) throws IOException {
		return read(m, 0);
	}

	/**
	 * Reads from the file descriptor into a memory pointer. The number of bytes read may be less
	 * than the given length. Returns the number of bytes read, or -1 for EOF.
	 */
	default int read(Memory m, int offset) throws IOException {
		return read(m, offset, JnaUtil.size(m) - offset);
	}

	/**
	 * Reads from the file descriptor into a memory pointer. The number of bytes read may be less
	 * than the given length. Returns the number of bytes read, or -1 for EOF.
	 */
	int read(Pointer p, int offset, int length) throws IOException;

	/**
	 * Writes to the file descriptor from a memory pointer. Throws CException if write is not able
	 * to complete.
	 */
	default void write(Memory m) throws IOException {
		write(m, 0);
	}

	/**
	 * Writes to the file descriptor from a memory pointer. Throws CException if write is not able
	 * to complete.
	 */
	default void write(Memory m, int offset) throws IOException {
		write(m, offset, JnaUtil.size(m) - offset);
	}

	/**
	 * Writes to the file descriptor from a memory pointer. Throws CException if write is not able
	 * to complete.
	 */
	void write(Pointer p, int offset, int length) throws IOException;

	/**
	 * Move position of file by offset in bytes from seek position type. Returns the position after
	 * the move. May not be supported by file type.
	 */
	int seek(int offset, Seek whence) throws IOException;

	/**
	 * Creates an InputStream using a buffer of default size.
	 */
	default InputStream in() {
		return in(BUFFER_SIZE);
	}

	/**
	 * Creates an InputStream using a buffer of given size.
	 */
	default InputStream in(int bufferSize) {
		Memory buffer = new Memory(bufferSize);
		return IoStreamUtil
			.in((array, offset, length) -> read(this, buffer, array, offset, length));
	}

	/**
	 * Creates an OutputStream using a buffer of default size.
	 */
	default OutputStream out() {
		return out(BUFFER_SIZE);
	}

	/**
	 * Creates an OutputStream using a buffer of given size.
	 */
	default OutputStream out(int bufferSize) {
		validateMin(bufferSize, 1, "Buffer size");
		Memory buffer = new Memory(bufferSize);
		return IoStreamUtil
			.out((array, offset, length) -> write(this, buffer, array, offset, length));
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	default int ioctl(int request, Object... objs) throws IOException {
		return ioctl((String) null, request, objs);
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	int ioctl(String name, int request, Object... objs) throws IOException;

	/**
	 * Read bytes into array using memory buffer.
	 */
	static int read(FileDescriptor fd, Memory buffer, byte[] array, int offset, int length)
		throws IOException {
		int bufSize = JnaUtil.size(buffer);
		for (int count = 0; count < length;) {
			int size = Math.min(bufSize, length - count);
			int n = fd.read(buffer, 0, size); // read into buffer
			if (n <= 0) return count == 0 ? n : count; // return if 0 or EOF
			buffer.read(0, array, offset + count, n); // copy buffer to array
			count += n;
			if (n < size) return count; // read is finished, no need to get more
		}
		return length;
	}

	/**
	 * Write bytes from array using memory buffer.
	 */
	static int write(FileDescriptor fd, Memory buffer, byte[] array, int offset, int length)
		throws IOException {
		int bufSize = JnaUtil.size(buffer);
		for (int count = 0; count < length;) {
			int n = Math.min(bufSize, length - count);
			buffer.write(0, array, offset + count, n);
			fd.write(buffer, 0, n); // throws exception for under-write
			count += n;
		}
		return length;
	}

	static class Null implements FileDescriptor {
		protected Null() {}
		
		@Override
		public int fd() throws IOException {
			return CLibUtil.INVALID_FD;
		}

		@Override
		public int read(Pointer p, int offset, int length) throws IOException {
			return length;
		}

		@Override
		public void write(Pointer p, int offset, int length) throws IOException {}

		@Override
		public int seek(int offset, Seek whence) throws IOException {
			return 0;
		}

		@Override
		public int ioctl(String name, int request, Object... objs) throws IOException {
			return 0;
		}
		
		@Override
		public void close() throws IOException {}
	}
}
