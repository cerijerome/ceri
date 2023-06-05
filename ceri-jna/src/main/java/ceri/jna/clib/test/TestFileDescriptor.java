package ceri.jna.clib.test;

import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionIntUnaryOperator;
import ceri.common.test.CallSync;
import ceri.common.test.TestInputStream;
import ceri.common.test.TestOutputStream;
import ceri.jna.clib.FileDescriptor;

public class TestFileDescriptor implements FileDescriptor {
	public final CallSync.Get<Integer> fd = CallSync.supplier();
	public final TestInputStream in = TestInputStream.of();
	public final TestOutputStream out = TestOutputStream.of();
	public final CallSync.Run close = CallSync.runnable(true);

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
	public <E extends Exception> void accept(ExceptionIntConsumer<E> consumer) throws E {
		consumer.accept(fd());
	}

	@Override
	public <E extends Exception> int applyAsInt(ExceptionIntUnaryOperator<E> operator) throws E {
		return operator.applyAsInt(fd());
	}

	@Override
	public void close() {
		close.run();
	}

	public int fd() {
		return fd.get();
	}
}
