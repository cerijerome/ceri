package ceri.common.io;

import static ceri.common.test.TestUtil.assertArray;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.SimpleExecutor;

public class PipedStreamBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldReadWrittenBytes() throws IOException {
		byte[] data = ArrayUtil.bytes(1, 2, 3, 4, 5);
		try (var ps = PipedStream.of()) {
			try (var exec = SimpleExecutor.run(() -> ps.out().write(data))) {
				ConcurrentUtil.delay(5); // avoids PipedInputStream wait(1000)
				assertArray(ps.in().readNBytes(data.length), data);
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldClearBytes() throws IOException {
		byte[] data = ArrayUtil.bytes(1, 2, 3, 4, 5);
		try (var ps = PipedStream.of()) {
			try (var exec = SimpleExecutor.run(() -> ps.out().write(data))) {
				ConcurrentUtil.delay(5); // avoids PipedInputStream wait(1000)
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
			try (var exec = SimpleExecutor.run(() -> con.inFeed().write(data))) {
				ConcurrentUtil.delay(50); // avoids PipedInputStream wait(1000)
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
			try (var exec = SimpleExecutor.run(() -> con.out().write(data))) {
				ConcurrentUtil.delay(50); // avoids PipedInputStream wait(1000)
				assertArray(con.outSink().readNBytes(2), 1, 2);
				assertThat(con.outSink().available(), is(3));
				con.clear();
				assertThat(con.outSink().available(), is(0));
			}
		}
	}
	
}
