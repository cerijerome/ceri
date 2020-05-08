package ceri.serial.clib;

import static ceri.common.validation.ValidationUtil.*;
import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.io.IoStreamUtil;
import ceri.common.util.HashCoder;
import ceri.serial.clib.jna.CException;
import ceri.serial.clib.jna.CLib;
import ceri.serial.clib.jna.CUtil;
import ceri.serial.jna.JnaUtil;

/**
 * Encapsulates a file descriptor as a closable resource.
 */
public class FileDescriptor implements Closeable {
	private static final int BUFFER_SIZE_DEF = 1024;
	private final int fd;
	private volatile boolean closed = false;

	/**
	 * Opens a file.
	 */
	public static FileDescriptor open(String path, OpenFlag... flags) throws CException {
		return open(path, Arrays.asList(flags));
	}

	/**
	 * Opens a file.
	 */
	public static FileDescriptor open(String path, Collection<OpenFlag> flags) throws CException {
		return open(path, null, flags);
	}

	/**
	 * Opens a file.
	 */
	public static FileDescriptor open(String path, Mode mode, OpenFlag... flags) throws CException {
		return open(path, mode, Arrays.asList(flags));
	}

	/**
	 * Opens a file.
	 */
	public static FileDescriptor open(String path, Mode mode, Collection<OpenFlag> flags)
		throws CException {
		return new FileDescriptor(mode == null ? CLib.open(path, OpenFlag.encode(flags)) :
			CLib.open(path, OpenFlag.encode(flags), mode.value()));
	}

	/**
	 * Take ownership of an existing file descriptor.
	 */
	public static FileDescriptor of(int fd) {
		return new FileDescriptor(fd);
	}

	private FileDescriptor(int fd) {
		if (fd < 0) throw new IllegalArgumentException("Invalid file descriptor: " + fd);
		this.fd = fd;
	}

	public int fd() {
		if (closed) throw new IllegalStateException("File descriptor is closed: " + fd);
		return fd;
	}

	/**
	 * Reads from the file descriptor into a memory pointer. The number of bytes read may be less
	 * than the given length. Returns the number of bytes read, or -1 for EOF.
	 */
	public int read(Memory m) throws CException {
		return read(m, 0);
	}

	/**
	 * Reads from the file descriptor into a memory pointer. The number of bytes read may be less
	 * than the given length. Returns the number of bytes read, or -1 for EOF.
	 */
	public int read(Memory m, int offset) throws CException {
		return read(m, offset, JnaUtil.size(m) - offset);
	}

	/**
	 * Reads from the file descriptor into a memory pointer. The number of bytes read may be less
	 * than the given length. Returns the number of bytes read, or -1 for EOF.
	 */
	public int read(Pointer p, int offset, int length) throws CException {
		if (length == 0) return 0;
		return CLib.read(fd(), JnaUtil.offset(p, offset), length);
	}

	/**
	 * Writes to the file descriptor from a memory pointer. Throws CException if write is not able
	 * to complete.
	 */
	public void write(Memory m) throws CException {
		write(m, 0);
	}

	/**
	 * Writes to the file descriptor from a memory pointer. Throws CException if write is not able
	 * to complete.
	 */
	public void write(Memory m, int offset) throws CException {
		write(m, offset, JnaUtil.size(m) - offset);
	}

	/**
	 * Writes to the file descriptor from a memory pointer. Throws CException if write is not able
	 * to complete.
	 */
	public void write(Pointer p, int offset, int length) throws CException {
		// Loop until all bytes are written, as recommended in gnu-c docs.
		for (int count = 0; count < length;) {
			int n = CLib.write(fd(), JnaUtil.offset(p, offset + count), length - count);
			if (n == 0) throw CException.general("Incomplete write: %d/%d bytes", count, length);
			count += n;
		}
	}

	/**
	 * Move position of file by offset in bytes from seek position type. Returns the position after
	 * the move. May not be supported by file type.
	 */
	public int lseek(int offset, Seek whence) throws CException {
		return CLib.lseek(fd(), offset, whence.value);
	}

	/**
	 * Creates an InputStream using a buffer of default size.
	 */
	public InputStream in() {
		return in(BUFFER_SIZE_DEF);
	}

	/**
	 * Creates an InputStream using a buffer of given size.
	 */
	public InputStream in(int bufferSize) {
		Memory buffer = new Memory(bufferSize);
		return IoStreamUtil.in((array, offset, length) -> read(buffer, array, offset, length));
	}

	/**
	 * Creates an OutputStream using a buffer of default size.
	 */
	public OutputStream out() {
		return out(BUFFER_SIZE_DEF);
	}

	/**
	 * Creates an OutputStream using a buffer of given size.
	 */
	public OutputStream out(int bufferSize) {
		validateMinL(bufferSize, 1, "Buffer size");
		Memory buffer = new Memory(bufferSize);
		return IoStreamUtil.out((array, offset, length) -> write(buffer, array, offset, length));
	}

	/**
	 * Creates a reader for sequential reading of bytes.
	 */
	public FileReader reader() {
		return reader(BUFFER_SIZE_DEF);
	}

	/**
	 * Creates a reader for sequential reading of bytes.
	 */
	public FileReader reader(int bufferSize) {
		return FileReader.of(this, bufferSize);
	}

	/**
	 * Creates a writer for sequential writing of bytes.
	 */
	public FileWriter writer() {
		return writer(BUFFER_SIZE_DEF);
	}

	/**
	 * Creates a writer for sequential writing of bytes.
	 */
	public FileWriter writer(int bufferSize) {
		return FileWriter.of(this, bufferSize);
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public int ioctl(int request, Object... objs) throws CException {
		return ioctl((String) null, request, objs);
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public int ioctl(String name, int request, Object... objs) throws CException {
		return CLib.ioctl(name, fd(), request, objs);
	}

	/**
	 * Performs an ioctl function. Arguments and return value depend on the function.
	 */
	public int ioctl(Supplier<String> errorMsg, int request, Object... objs) throws CException {
		return CLib.ioctl(errorMsg, fd(), request, objs);
	}

	@Override
	public void close() throws CException {
		if (closed) return;
		closed = CUtil.close(fd);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(fd);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof FileDescriptor)) return false;
		FileDescriptor other = (FileDescriptor) obj;
		if (fd != other.fd) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("fd=%d/%x", fd, fd);
	}

	/**
	 * Read bytes into array using memory buffer.
	 */
	private int read(Memory buffer, byte[] array, int offset, int length) throws CException {
		int bufSize = JnaUtil.size(buffer);
		for (int count = 0; count < length;) {
			int size = Math.min(bufSize, length - count);
			int n = read(buffer, 0, size); // read into buffer
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
	private int write(Memory buffer, byte[] array, int offset, int length) throws CException {
		int bufSize = JnaUtil.size(buffer);
		for (int count = 0; count < length;) {
			int n = Math.min(bufSize, length - count);
			buffer.write(0, array, offset + count, n);
			write(buffer, 0, n); // throws exception for under-write
			count += n;
		}
		return length;
	}

}
