package ceri.jna.clib.test;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.data.FieldTranscoder;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionIntFunction;
import ceri.common.test.CallSync;
import ceri.common.test.TestInputStream;
import ceri.common.test.TestOutputStream;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.OpenFlag;

public class TestFileDescriptor implements FileDescriptor {
	public final CallSync.Supplier<Integer> fd = CallSync.supplier();
	public final CallSync.Consumer<Integer> flags = CallSync.consumer(0, true);
	public final TestInputStream in = TestInputStream.of();
	public final TestOutputStream out = TestOutputStream.of();
	public final CallSync.Runnable close = CallSync.runnable(true);
	private final FieldTranscoder<IOException, OpenFlag> flagField;

	public static TestFileDescriptor of(int fd) {
		return new TestFileDescriptor(fd);
	}

	private TestFileDescriptor(int fd) {
		this.fd.autoResponses(fd);
		flagField = FileDescriptor.flagField(() -> flags.lastValue(IO_ADAPTER),
			value -> flags.accept(value, IO_ADAPTER));
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
	public void accept(ExceptionIntConsumer<IOException> consumer) throws IOException {
		consumer.accept(fd());
	}

	@Override
	public <T> T apply(ExceptionIntFunction<IOException, T> function) throws IOException {
		return function.apply(fd());
	}

	@Override
	public FieldTranscoder<IOException, OpenFlag> flags() {
		return flagField;
	}

	@Override
	public void close() {
		close.run();
	}

	public int fd() throws IOException {
		return fd.get(IO_ADAPTER);
	}
}
