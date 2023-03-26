package ceri.jna.clib.test;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.sun.jna.Pointer;
import ceri.common.data.ByteProvider;
import ceri.common.test.CallSync;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.Seek;
import ceri.jna.util.JnaUtil;

public class TestFileDescriptor implements FileDescriptor {
	public final CallSync.Get<Integer> fd = CallSync.supplier();
	public final CallSync.Apply<Integer, ByteProvider> read =
		CallSync.function(null, ByteProvider.empty());
	public final CallSync.Accept<ByteProvider> write = CallSync.consumer(null, true);
	// List<?> = int offset, Seek whence
	public final CallSync.Apply<List<?>, Integer> seek = CallSync.function(null, 0);
	// List<?> = int request, Object... objs
	public final CallSync.Apply<List<?>, Integer> ioctl = CallSync.function(null, 0);
	public final CallSync.Run close = CallSync.runnable(true);

	public static TestFileDescriptor of(int fd) {
		return new TestFileDescriptor(fd);
	}

	private TestFileDescriptor(int fd) {
		this.fd.autoResponses(fd);
	}

	@Override
	public int fd() throws IOException {
		return fd.get(IO_ADAPTER);
	}

	@Override
	public int read(Pointer p, int offset, int length) throws IOException {
		ByteProvider data = read.apply(length, IO_ADAPTER);
		if (data == null || data.length() == 0) return 0;
		int n = Math.min(length, data.length());
		JnaUtil.write(p, offset, data.copy(0, n));
		return n;
	}

	@Override
	public void write(Pointer p, int offset, int length) throws IOException {
		byte[] bytes = new byte[length];
		if (p != null) JnaUtil.read(p, offset, bytes);
		write.accept(ByteProvider.of(bytes), IO_ADAPTER);
	}

	@Override
	public int seek(int offset, Seek whence) throws IOException {
		return seek.apply(List.of(offset, whence), IO_ADAPTER);
	}

	@Override
	public int ioctl(String name, int request, Object... objs) throws IOException {
		List<Object> list = new ArrayList<>();
		Collections.addAll(list, request);
		Collections.addAll(list, objs);
		return ioctl.apply(list, IO_ADAPTER);
	}

	@Override
	public void close() throws IOException {
		close.run(IO_ADAPTER);
	}
}
