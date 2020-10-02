package ceri.common.io;

import static ceri.common.test.TestUtil.assertArray;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.concurrent.SimpleExecutor;

public class PipedStreamBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldReadWrittenBytes() throws IOException {
		byte[] data = ArrayUtil.bytes(1, 2, 3, 4, 5);
		try (var ps = PipedStream.of()) {
			try (var exec = SimpleExecutor.run(() -> writeFlush(ps.out(), data))) {
				assertArray(ps.in().readNBytes(data.length), data);
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWaitForReadToComplete() throws IOException {
		byte[] data = ArrayUtil.bytes(1, 2, 3, 4, 5);
		try (var ps = PipedStream.of()) {
			try (var exec = SimpleExecutor.run(() -> ps.in().readNBytes(data.length))) {
				writeFlush(ps.out(), data);
				ps.awaitRead(0);
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWaitForReadToCompleteWithTimeout() throws IOException {
		byte[] data = ArrayUtil.bytes(1, 2, 3, 4, 5);
		try (var ps = PipedStream.of()) {
			ps.out().write(data);
			assertThat(ps.awaitRead(0, 1), is(false));
			try (var exec = SimpleExecutor.run(() -> ps.in().readNBytes(data.length))) {
				assertThat(ps.awaitRead(0, 10000), is(true));
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldClearBytes() throws IOException {
		byte[] data = ArrayUtil.bytes(1, 2, 3, 4, 5);
		try (var ps = PipedStream.of()) {
			try (var exec = SimpleExecutor.run(() -> writeFlush(ps.out(), data))) {
				assertArray(ps.in().readNBytes(2), 1, 2);
				assertThat(ps.in().available(), is(3));
				ps.clear();
				assertThat(ps.in().available(), is(0));
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFeedConnectorBytes() throws IOException {
		byte[] data = ArrayUtil.bytes(1, 2, 3, 4, 5);
		try (var con = PipedStream.connector()) {
			try (var exec = SimpleExecutor.run(() -> writeFlush(con.inFeed(), data))) {
				assertArray(con.in().readNBytes(2), 1, 2);
				assertThat(con.in().available(), is(3));
				con.clear();
				assertThat(con.in().available(), is(0));
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSinkConnectorBytes() throws IOException {
		byte[] data = ArrayUtil.bytes(1, 2, 3, 4, 5);
		try (var con = PipedStream.connector()) {
			try (var exec = SimpleExecutor.run(() -> writeFlush(con.out(), data))) {
				assertArray(con.outSink().readNBytes(2), 1, 2);
				assertThat(con.outSink().available(), is(3));
				con.clear();
				assertThat(con.outSink().available(), is(0));
			}
		}
	}

	private static void writeFlush(OutputStream out, byte[] bytes) throws IOException {
		out.write(bytes);
		out.flush(); // avoids PipedInputStream wait(1000)
	}
}
