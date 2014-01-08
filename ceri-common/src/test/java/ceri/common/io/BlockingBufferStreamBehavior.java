package ceri.common.io;

import static ceri.common.test.TestUtil.assertArray;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import ceri.common.test.TestState;
import ceri.common.test.TestThread;

public class BlockingBufferStreamBehavior {
	byte[] buffer;
	
	@Before
	public void init() {
		buffer = new byte[256];
		for (int i = 0; i < buffer.length; i++) buffer[i] = (byte)i;
	}
	
	@Test
	public void shouldWritePartialDataIfNotAtMaximum() throws Exception {
		try (final BlockingBufferStream stream = new BlockingBufferStream(10, 10)) {
			TestThread readThread = new TestThread() {
				@Override
				protected void run() throws Exception {
					byte[] b = new byte[20];
					assertThat(stream.asInputStream().read(b), is(10));
					assertThat(stream.asInputStream().read(b), is(10));
				}
			};
			readThread.start();
			stream.write(buffer, 0, 25);
			assertThat(stream.asInputStream().available(), is(5));
			readThread.stop();
		}
	}
	
	@Test
	public void shouldBlockOnReadIfNoDataInBuffer() throws Exception {
		final TestState<Integer> state = new TestState<>(0);
		try (final BlockingBufferStream stream = new BlockingBufferStream(10, 100)) {
			TestThread readThread = new TestThread() {
				@Override
				protected void run() throws Exception {
					state.set(1);
					byte[] b = new byte[10];
					int count = stream.asInputStream().read(b);
					assertTrue(count > 0);
					state.set(2);
					count += stream.asInputStream().read(b, count, b.length - count);
					assertThat(count, is(10));
					assertArray(b, 0, buffer, 0, 10);
					state.set(3);
				}
			};
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
	public void shouldBlockOnWriteIfBufferIsNotBigEnough() throws Exception {
		final TestState<Integer> state = new TestState<>(0);
		try (final BlockingBufferStream stream = new BlockingBufferStream(10, 100)) {
			TestThread writeThread = new TestThread() {
				@Override
				protected void run() {
					stream.write(buffer, 0, 50);
					stream.write(buffer, 50, 50);
					state.set(1);
					stream.write(buffer, 100, 50);
					state.set(2);
				}
			};
			writeThread.start();
			assertThat(state.waitFor(1), is(1));
			assertThat(state.waitFor(2, 100), is(1));
			stream.asInputStream().read(new byte[50]);
			assertThat(state.waitFor(2), is(2));
			writeThread.stop();
		}
	}
	
}
