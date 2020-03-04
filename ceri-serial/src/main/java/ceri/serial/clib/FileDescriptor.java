package ceri.serial.clib;

import java.io.Closeable;
import com.sun.jna.Pointer;
import ceri.common.util.HashCoder;
import ceri.serial.clib.jna.CException;
import ceri.serial.clib.jna.CLib;

/**
 * Encapsulates a file descriptor as a closeable resource.
 */
public class FileDescriptor implements ByteWriter, ByteReader, Closeable {
	public final int fd;

	/**
	 * Open a file.
	 */
	public static FileDescriptor open(String path, OpenFlag flag) throws CException {
		return new FileDescriptor(CLib.open(path, flag.value));
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

	@Override
	public int readInto(Pointer p, int len) throws CException {
		return CLib.read(fd, p, len);
	}

	@Override
	public int writeFrom(Pointer p, int len) throws CException {
		return CLib.write(fd, p, len);
	}

	@Override
	public void close() throws CException {
		CLib.close(fd);
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
}
