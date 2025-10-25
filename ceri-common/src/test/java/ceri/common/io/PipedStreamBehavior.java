package ceri.common.io;

import static ceri.common.test.Assert.assertArray;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertTrue;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.concurrent.SimpleExecutor;

public class PipedStreamBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldReadWrittenBytes() throws IOException {
		byte[] data = ArrayUtil.bytes.of(1, 2, 3, 4, 5);
		try (var ps = PipedStream.of()) {
			try (var exec = SimpleExecutor.run(() -> writeFlush(ps.out(), data))) {
				assertArray(ps.in().readNBytes(data.length), data);
				exec.get();
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWaitForReadToComplete() throws IOException {
		byte[] data = ArrayUtil.bytes.of(1, 2, 3, 4, 5);
		try (var ps = PipedStream.of()) {
			try (var exec = SimpleExecutor.run(() -> ps.in().readNBytes(data.length))) {
				writeFlush(ps.out(), data);
				ps.awaitRead(0);
				exec.get();
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWaitForReadToCompleteWithTimeout() throws IOException {
		byte[] data = ArrayUtil.bytes.of(1, 2, 3, 4, 5);
		try (var ps = PipedStream.of()) {
			ps.out().write(data);
			assertFalse(ps.awaitRead(0, 1));
			try (var exec = SimpleExecutor.run(() -> ps.in().readNBytes(data.length))) {
				assertTrue(ps.awaitRead(0, 10000));
				exec.get();
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldClearBytes() throws IOException {
		byte[] data = ArrayUtil.bytes.of(1, 2, 3, 4, 5);
		try (var ps = PipedStream.of()) {
			try (var exec = SimpleExecutor.run(() -> writeFlush(ps.out(), data))) {
				assertArray(ps.in().readNBytes(2), 1, 2);
				assertEquals(ps.in().available(), 3);
				ps.clear();
				assertEquals(ps.in().available(), 0);
				exec.get();
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFeedConnectorBytes() throws IOException {
		byte[] data = ArrayUtil.bytes.of(1, 2, 3, 4, 5);
		try (var con = PipedStream.connector()) {
			try (var exec = SimpleExecutor.run(() -> writeFlush(con.inFeed(), data))) {
				assertArray(con.in().readNBytes(2), 1, 2);
				assertEquals(con.in().available(), 3);
				con.clear();
				assertEquals(con.in().available(), 0);
				exec.get();
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSinkConnectorBytes() throws IOException {
		byte[] data = ArrayUtil.bytes.of(1, 2, 3, 4, 5);
		try (var con = PipedStream.connector()) {
			try (var exec = SimpleExecutor.run(() -> writeFlush(con.out(), data))) {
				assertArray(con.outSink().readNBytes(2), 1, 2);
				assertEquals(con.outSink().available(), 3);
				con.clear();
				assertEquals(con.outSink().available(), 0);
				exec.get();
			}
		}
	}

	private static void writeFlush(OutputStream out, byte[] bytes) throws IOException {
		out.write(bytes);
		out.flush(); // avoids PipedInputStream wait(1000)
	}
}
