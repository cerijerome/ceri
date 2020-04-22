package ceri.serial.clib;

import java.io.IOException;
import java.io.InputStream;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteWriter;
import ceri.common.io.IoUtil;
import ceri.common.io.RuntimeIoException;
import ceri.common.util.ExceptionAdapter;
import ceri.serial.jna.JnaWriter;

public class FileWriter implements JnaWriter<FileWriter> {
	private static final int BUFFER_SIZE_DEF = 1024;
	private ExceptionAdapter<RuntimeIoException> ioAdapter = IoUtil.RUNTIME_IO_ADAPTER;
	private final FileDescriptor fd;
	private final int bufferSize;
	private Memory buffer = null;

	public static FileWriter of(FileDescriptor fd) {
		return of(fd, BUFFER_SIZE_DEF);
	}

	public static FileWriter of(FileDescriptor fd, int bufferSize) {
		return new FileWriter(fd, bufferSize);
	}

	private FileWriter(FileDescriptor fd, int bufferSize) {
		this.fd = fd;
		this.bufferSize = bufferSize;
	}

	@Override
	public FileWriter writeByte(int value) {
		ioAdapter.run(() -> {
			buffer().setByte(0, (byte) value);
			fd.write(buffer, 0, 1);
		});
		return this;
	}

	@Override
	public FileWriter skip(int length) {
		ioAdapter.run(() -> fd.lseek(length, Seek.SEEK_CUR));
		return this;
	}

	/**
	 * <pre>
	 * T writeFrom(ByteProvider provider, int offset, int length); [1-byte]
	 * </pre>
	 */
	@Override
	public FileWriter fill(int length, int value) {
		ioAdapter.run(() -> {
			buffer().setMemory(0, size(length), (byte) value);
			for (int count = 0; count < length;) {
				int n = size(length - count);
				fd.write(buffer, 0, n);
				count += n;
			}
		});
		return this;
	}

	@Override
	public FileWriter writeFrom(byte[] array, int offset, int length) {
		ioAdapter.run(() -> {
			for (int count = 0; count < length;) {
				int n = size(length - count);
				buffer().write(0, array, offset + count, n);
				fd.write(buffer, 0, n);
				count += n;
			}
		});
		return this;
	}

	@Override
	public FileWriter writeFrom(ByteProvider provider, int offset, int length) {
		return writeFrom(provider.copy(offset, length));
	}

	@Override
	public FileWriter writeFrom(Pointer p, int offset, int length) {
		ioAdapter.run(() -> fd.write(p, offset, length));
		return this;
	}

	@Override
	public int transferFrom(InputStream in, int length) throws IOException {
		return ByteWriter.transferBufferFrom(this, in, length);
	}

	private int size(int length) {
		return Math.min(length, bufferSize);
	}

	private Memory buffer() {
		if (buffer == null) buffer = new Memory(bufferSize);
		return buffer;
	}

}
