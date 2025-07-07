package ceri.jna.clib.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.function.Excepts.IntConsumer;
import ceri.common.function.Excepts.IntFunction;
import ceri.common.test.CallSync;
import ceri.common.test.TestInputStream;
import ceri.common.test.TestOutputStream;
import ceri.jna.clib.FileDescriptor;

public class TestFileDescriptor implements FileDescriptor {
	public final CallSync.Supplier<Integer> fd = CallSync.supplier();
	public final CallSync.Consumer<Integer> flags = CallSync.consumer(0, true);
	public final TestInputStream in = TestInputStream.of();
	public final TestOutputStream out = TestOutputStream.of();
	public final CallSync.Runnable close = CallSync.runnable(true);

	public static TestFileDescriptor of(int fd) {
		return new TestFileDescriptor(fd);
	}

	private TestFileDescriptor(int fd) {
		this.fd.autoResponses(fd);
	}

	@Override
	public InputStream in() {
		return in;
	}

	@Override
	public OutputStream out() {
		return out;
	}

	@Override
	public void accept(IntConsumer<IOException> consumer) throws IOException {
		consumer.accept(fd());
	}

	@Override
	public <T> T apply(IntFunction<IOException, T> function) throws IOException {
		return function.apply(fd());
	}

	@Override
	public int flags() throws IOException {
		return flags.lastValue(ExceptionAdapter.io);
	}

	@Override
	public void flags(int flags) throws IOException {
		this.flags.accept(flags, ExceptionAdapter.io);
	}

	@Override
	public void close() {
		close.run();
	}

	public int fd() throws IOException {
		return fd.get(ExceptionAdapter.io);
	}
}
