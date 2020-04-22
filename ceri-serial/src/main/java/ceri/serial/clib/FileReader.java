package ceri.serial.clib;

import java.io.IOException;
import java.io.OutputStream;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.data.ByteReader;
import ceri.common.data.ByteReceiver;
import ceri.common.io.IoUtil;
import ceri.common.io.RuntimeIoException;
import ceri.common.util.ExceptionAdapter;
import ceri.serial.clib.jna.CException;
import ceri.serial.jna.JnaReader;

public class FileReader implements JnaReader {
	private static final int BUFFER_SIZE_DEF = 1024;
	private ExceptionAdapter<RuntimeIoException> ioAdapter = IoUtil.RUNTIME_IO_ADAPTER;
	private final FileDescriptor fd;
	private final int bufferSize;
	private Memory buffer = null;

	public static FileReader of(FileDescriptor fd) {
		return of(fd, BUFFER_SIZE_DEF);
	}
	
	public static FileReader of(FileDescriptor fd, int bufferSize) {
		return new FileReader(fd, bufferSize);
	}
	
	private FileReader(FileDescriptor fd, int bufferSize) {
		this.fd = fd;
		this.bufferSize = bufferSize;
	}

	@Override
	public byte readByte() {
		ioAdapter.run(() -> {
			if (fd.read(buffer(), 0, 1) != 1) throw readException(0, 1);
		});
		return buffer.getByte(0);
	}

	@Override
	public FileReader skip(int length) {
		ioAdapter.run(() -> fd.lseek(length, Seek.SEEK_CUR));
		return this;
	}

	@Override
	public int readInto(byte[] array, int offset, int length) {
		ioAdapter.run(() -> {
			for (int count = 0; count < length;) {
				int n = fd.read(buffer(), 0, size(length - count));
				if (n <= 0) throw readException(count, length);
				buffer.read(0, array, offset + count, n);
				count += n;
			}
		});
		return offset + length;
	}

	@Override
	public int readInto(ByteReceiver receiver, int offset, int length) {
		byte[] bytes = new byte[length];
		readInto(bytes);
		return receiver.copyFrom(offset, bytes, 0, length);
	}

	@Override
	public int readInto(Pointer p, int offset, int length) {
		ioAdapter.run(() -> {
			for (int count = 0; count < length;) {
				int n = fd.read(p, offset + count, length - count);
				if (n <= 0) throw readException(count, length);
				count += n;
			}
		});
		return offset + length;
	}

	@Override
	public int transferTo(OutputStream out, int length) throws IOException {
		return ByteReader.transferBufferTo(this, out, length);
	}

	private int size(int length) {
		return Math.min(length, bufferSize);
	}

	private CException readException(int actual, int expected) {
		return CException.general("Incomplete read: %d/%d bytes", actual, expected);
	}

	private Memory buffer() {
		if (buffer == null) buffer = new Memory(bufferSize);
		return buffer;
	}

}
