package ceri.common.io;

import static ceri.common.test.TestUtil.assertArrayObject;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import ceri.common.test.TestState;
import ceri.common.test.TestThread;
import ceri.common.test.TestUtil;

public class BlockingBufferStreamBehavior {
	byte[] buffer;

	@Before
	public void init() {
		buffer = new byte[256];
		for (int i = 0; i < buffer.length; i++)
			buffer[i] = (byte) i;
	}

	@Test
	public void shouldNotWriteToClosedStream() {
		try (BlockingBufferStream stream = new BlockingBufferStream()) {
			stream.close();
			stream.write(0);
			stream.write(new byte[1], 0, 1);
			assertThat(stream.available(), is(0));
		}
	}

	@Test
	public void shouldFailForInvalidReadParameters() {
		byte[] b = { 0 };
		try (BlockingBufferStream bbs = new BlockingBufferStream()) {
			TestUtil.assertThrown(() -> bbs.read(null, 0, 0));
			TestUtil.assertThrown(() -> bbs.read(b, -1, 0));
			TestUtil.assertThrown(() -> bbs.read(b, 0, -1));
			TestUtil.assertThrown(() -> bbs.read(b, 0, 2));
			assertThat(bbs.read(new byte[0], 0, 0), is(0));
		}
	}

	@Test
	public void shouldFailForInvalidWriteParameters() {
		byte[] b = { 0 };
		try (BlockingBufferStream bbs = new BlockingBufferStream()) {
			TestUtil.assertThrown(() -> bbs.write(null, 0, 0));
			TestUtil.assertThrown(() -> bbs.write(b, -1, 0));
			TestUtil.assertThrown(() -> bbs.write(b, 0, -1));
			TestUtil.assertThrown(() -> bbs.write(b, 0, 2));
			bbs.write(new byte[0], 0, 0);
		}
	}

	@Test
	public void shouldReadInvalidCodeForClosedStream() {
		try (BlockingBufferStream bbs = new BlockingBufferStream()) {
			bbs.close();
			assertThat(bbs.read(), is(-1));
			assertThat(bbs.read(new byte[1], 0, 1), is(-1));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailForInvalidConstructorArguments() {
		try (BlockingBufferStream stream = new BlockingBufferStream(2, 1)) {}
	}

	@Test
	public void shouldWriteAndReadSynchronously() throws Throwable {
		try (final BlockingBufferStream stream = new BlockingBufferStream()) {
			TestThread<?> thread = TestThread.create(() -> {
				assertThat(stream.read(), is(0xff));
				assertThat(stream.read(), is(0));
			});
			thread.start();
			stream.write(0xff);
			stream.write(0);
			thread.join();
		}
	}

	@Test
	public void shouldWritePartialDataIfNotAtMaximum() throws Throwable {
		try (final BlockingBufferStream stream = new BlockingBufferStream(10, 10)) {
			TestThread<?> readThread = TestThread.create(() -> {
				byte[] b = new byte[20];
				assertThat(stream.asInputStream().read(b), is(10));
				assertThat(stream.asInputStream().read(b), is(10));
			});
			readThread.start();
			stream.write(buffer, 0, 25);
			assertThat(stream.asInputStream().available(), is(5));
			readThread.stop();
		}
	}

	@Test
	public void shouldBlockOnReadIfNoDataInBuffer() throws Throwable {
		final TestState<Integer> state = new TestState<>(0);
		try (final BlockingBufferStream stream = new BlockingBufferStream(10, 100)) {
			TestThread<?> readThread = TestThread.create(() -> {
				state.set(1);
				byte[] b = new byte[10];
				int count = stream.asInputStream().read(b);
				assertTrue(count > 0);
				state.set(2);
				count += stream.asInputStream().read(b, count, b.length - count);
				assertThat(count, is(10));
				assertArrayObject(b, 0, buffer, 0, 10);
				state.set(3);
			});
			readThread.start();
			assertThat(state.waitFor(1), is(1));
			assertThat(state.waitFor(2, 100), is(1));
			stream.write(buffer, 0, 9);
			assertThat(state.waitFor(2), is(2));
			assertThat(state.waitFor(3, 100), is(2));
			stream.write(buffer, 9, 1);
			readThread.stop();
		}
	}

	@Test
	public void shouldBlockOnWriteIfBufferIsNotBigEnough() throws Throwable {
		final TestState<Integer> state = new TestState<>(0);
		try (final BlockingBufferStream stream = new BlockingBufferStream(10, 100)) {
			TestThread<?> writeThread = TestThread.create(() -> {
				stream.write(buffer, 0, 50);
				stream.write(buffer, 50, 50);
				state.set(1);
				stream.write(buffer, 100, 50);
				state.set(2);
			});
			writeThread.start();
			assertThat(state.waitFor(1), is(1));
			assertThat(state.waitFor(2, 100), is(1));
			stream.asInputStream().read(new byte[50]);
			assertThat(state.waitFor(2), is(2));
			writeThread.stop();
		}
	}

}
