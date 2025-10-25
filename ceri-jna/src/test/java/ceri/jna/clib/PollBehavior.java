package ceri.jna.clib;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertTrue;
import static ceri.common.test.Assert.assertUnordered;
import static ceri.common.test.Assert.unsupportedOp;
import static ceri.jna.clib.Poll.Error.POLLNVAL;
import static ceri.jna.clib.Poll.Event.POLLIN;
import static ceri.jna.clib.Poll.Event.POLLOUT;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.test.Assert;
import ceri.common.time.TimeSpec;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.util.JnaOs;
import ceri.log.util.LogUtil;

public class PollBehavior {
	private Pipe pipe;
	private Poll poll;

	@After
	public void after() {
		LogUtil.close(pipe);
		pipe = null;
		poll = null;
	}

	@Test
	public void shouldCreateForSingleFd() throws IOException {
		pipe = Pipe.of();
		var poll = Poll.of(pipe.read, Poll.Event.POLLIN);
		writeToPipe(0);
		assertEquals(poll.poll(1000), 1);
		assertTrue(poll.fd(0).has(POLLIN));
	}

	@Test
	public void shouldProvideAllEvents() throws IOException {
		initPipe();
		assertFalse(poll.fd(0).has(POLLIN));
		assertFalse(poll.fd(1).has(POLLOUT));
		writeToPipe(0);
		assertEquals(poll.poll(), 2);
		assertUnordered(poll.responses(), POLLOUT, POLLIN);
		assertTrue(poll.fd(0).has(POLLIN));
		assertTrue(poll.fd(1).has(POLLOUT));
		assertUnordered(poll.fd(0).responses(), POLLIN);
		assertUnordered(poll.fd(1).responses(), POLLOUT);
	}

	@Test
	public void shouldProvideAllErrors() throws IOException {
		initPipe();
		assertFalse(poll.fd(0).hasErrors());
		assertFalse(poll.fd(0).has(POLLNVAL));
		assertFalse(poll.fd(1).hasErrors());
		assertFalse(poll.fd(1).has(POLLNVAL));
		writeToPipe(0);
		pipe.close();
		pipe = null;
		assertEquals(poll.poll(), 2);
		assertUnordered(poll.responses());
		assertUnordered(poll.errors(), POLLNVAL);
		assertTrue(poll.fd(0).hasErrors());
		assertTrue(poll.fd(0).has(POLLNVAL));
		assertTrue(poll.fd(1).hasErrors());
		assertTrue(poll.fd(1).has(POLLNVAL));
		assertUnordered(poll.fd(0).errors(), POLLNVAL);
		assertUnordered(poll.fd(1).errors(), POLLNVAL);
	}

	@Test
	public void shouldValidateForErrors() throws IOException {
		initPipe();
		assertEquals(poll.poll(), 1);
		poll.validate();
		pipe.close();
		pipe = null;
		assertEquals(poll.poll(), 2);
		Assert.thrown(IOException.class, ".*POLLNVAL.*", () -> poll.validate());
	}

	@Test
	public void shouldNotPollWithSigsetOnMac() throws IOException {
		initPipe();
		JnaOs.mac.accept(_ -> unsupportedOp(() -> poll.poll(SigSet.of(Signal.SIGINT))));
	}

	@Test
	public void shouldPollWithSigsetOnLinux() throws IOException {
		initPipe();
		JnaOs.linux.accept(_ -> {
			try (var enc = TestCLibNative.register()) {
				enc.ref.poll.autoResponses(1, 2);
				assertEquals(poll.poll(SigSet.of(Signal.SIGINT)), 1);
				assertEquals(poll.poll(TimeSpec.ZERO, SigSet.of(Signal.SIGINT)), 2);
			}
		});
	}

	@SuppressWarnings("resource")
	private void writeToPipe(int... bytes) throws IOException {
		pipe.out().write(ArrayUtil.bytes.of(bytes));
	}

	private void initPipe() throws IOException {
		pipe = Pipe.of();
		pipe.blocking(false);
		poll = Poll.of(2);
		assertEquals(poll.size(), 2);
		poll.fd(0).fd(pipe.read).request(POLLIN);
		poll.fd(1).fd(pipe.write).request(POLLOUT);
	}
}
