package ceri.common.test;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionRunnable;

/**
 * OutputStream controlled by consumer functions for write() and close(). Integer data can also be
 * supplied to write(). Each write advances the index into the data; a data value of -1 generates an
 * EOF, -2 throws an IOException, and -3 throws a RuntimeException. A ByteArrayOutputStream is
 * enabled by default to collect received bytes.
 */
public class TestOutputStream extends OutputStream {
	private static final int EOF_VALUE = -1;
	private static final int THROW_IO_VALUE = -2;
	private static final int THROW_RT_VALUE = -3;
	private final ExceptionIntConsumer<IOException> writeConsumer;
	private final ExceptionRunnable<IOException> closeRunnable;
	private final ByteArrayOutputStream collector;

	private static class Data {
		private int pos = 0;
		private final int[] data;

		public Data(int... data) {
			this.data = data;
		}

		public void write() throws IOException {
			if (pos >= data.length) throw new EOFException("End of data");
			int value = data[pos++];
			if (value == THROW_IO_VALUE) throw new IOException("Write = " + THROW_IO_VALUE);
			if (value == THROW_RT_VALUE) throw new RuntimeException("Write = " + THROW_RT_VALUE);
			if (value == EOF_VALUE) throw new EOFException("Write = " + EOF_VALUE);
		}
	}

	public static TestOutputStream of(int... bytes) {
		return builder().data(bytes).build();
	}

	public static class Builder {
		ExceptionIntConsumer<IOException> writeConsumer = i -> {};
		ExceptionRunnable<IOException> closeRunnable = () -> {};
		boolean collect = true;

		Builder() {}

		public Builder dataSize(int size) {
			return data(new int[size]);
		}

		public Builder data(int... values) {
			Data data = new Data(values);
			return write(b -> data.write());
		}

		public Builder write(ExceptionIntConsumer<IOException> writeConsumer) {
			this.writeConsumer = writeConsumer;
			return this;
		}

		public Builder collect(boolean collect) {
			this.collect = collect;
			return this;
		}

		public Builder close(ExceptionRunnable<IOException> closeRunnable) {
			this.closeRunnable = closeRunnable;
			return this;
		}

		public TestOutputStream build() {
			return new TestOutputStream(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	TestOutputStream(Builder builder) {
		writeConsumer = builder.writeConsumer;
		closeRunnable = builder.closeRunnable;
		collector = builder.collect ? new ByteArrayOutputStream() : null;
	}

	@Override
	public void write(int b) throws IOException {
		writeConsumer.accept(b);
		if (collector != null) collector.write(b);
	}

	@Override
	public void close() throws IOException {
		closeRunnable.run();
	}

	public byte[] written() {
		if (collector == null) return ArrayUtil.EMPTY_BYTE;
		return collector.toByteArray();
	}
}
