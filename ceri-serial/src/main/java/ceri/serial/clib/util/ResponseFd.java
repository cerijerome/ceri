package ceri.serial.clib.util;

import java.io.IOException;
import java.util.Arrays;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.serial.clib.FileDescriptor;
import ceri.serial.clib.Seek;
import ceri.serial.clib.jna.CLib;
import ceri.serial.jna.JnaUtil;

public class ResponseFd implements FileDescriptor {
	private final int fd;
	protected byte[] bytes;
	protected int position = 0;

	public static ResponseFd of(int fd, int... bytes) {
		return of(fd, ArrayUtil.bytes(bytes));
	}

	public static ResponseFd of(int fd, byte[] bytes) {
		return new ResponseFd(fd, bytes);
	}

	protected ResponseFd(int fd, byte[] bytes) {
		this.fd = fd;
		this.bytes = bytes;
	}

	@Override
	public int fd() throws IOException {
		return fd;
	}

	public byte[] bytes() {
		return ArrayUtil.copyOf(bytes, 0);
	}

	public int position() {
		return position;
	}

	@Override
	public int read(Pointer p, int offset, int length) throws IOException {
		byte[] data = read(position, length);
		if (length > 0 && position >= bytes.length) return CLib.EOF;
		JnaUtil.write(p, offset, data);
		position += data.length;
		return data.length;
	}

	@Override
	public void write(Pointer p, int offset, int length) throws IOException {
		byte[] data = JnaUtil.byteArray(p, offset, length);
		write(data, position);
		position += data.length;
	}

	@Override
	public int seek(int offset, Seek whence) throws IOException {
		if (whence == Seek.SEEK_SET) position = offset;
		else if (whence == Seek.SEEK_END) position = bytes.length - offset;
		else if (whence == Seek.SEEK_HOLE) position = bytes.length;
		else position += offset;
		if (position < 0) position = 0;
		return position;
	}

	@Override
	public int ioctl(String name, int request, Object... objs) throws IOException {
		throw new IOException("Not supported");
	}

	@Override
	public void close() {}

	protected byte[] read(int position, int length) {
		if (position >= bytes.length) return ArrayUtil.EMPTY_BYTE;
		length = Math.min(length, bytes.length - position);
		return ArrayUtil.copyOf(bytes, position, length);
	}

	protected void write(byte[] data, int position) {
		resize(position + data.length);
		ArrayUtil.copy(data, 0, bytes, position, data.length);
	}

	protected void resize(int length) {
		if (length > bytes.length) bytes = Arrays.copyOf(bytes, length);
	}

}
