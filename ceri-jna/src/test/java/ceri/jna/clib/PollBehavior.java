package ceri.jna.clib;

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
		Assert.equal(poll.poll(1000), 1);
		Assert.yes(poll.fd(0).has(POLLIN));
	}

	@Test
	public void shouldProvideAllEvents() throws IOException {
		initPipe();
		Assert.no(poll.fd(0).has(POLLIN));
		Assert.no(poll.fd(1).has(POLLOUT));
		writeToPipe(0);
		Assert.equal(poll.poll(), 2);
		Assert.unordered(poll.responses(), POLLOUT, POLLIN);
		Assert.yes(poll.fd(0).has(POLLIN));
		Assert.yes(poll.fd(1).has(POLLOUT));
		Assert.unordered(poll.fd(0).responses(), POLLIN);
		Assert.unordered(poll.fd(1).responses(), POLLOUT);
	}

	@Test
	public void shouldProvideAllErrors() throws IOException {
		initPipe();
		Assert.no(poll.fd(0).hasErrors());
		Assert.no(poll.fd(0).has(POLLNVAL));
		Assert.no(poll.fd(1).hasErrors());
		Assert.no(poll.fd(1).has(POLLNVAL));
		writeToPipe(0);
		pipe.close();
		pipe = null;
		Assert.equal(poll.poll(), 2);
		Assert.unordered(poll.responses());
		Assert.unordered(poll.errors(), POLLNVAL);
		Assert.yes(poll.fd(0).hasErrors());
		Assert.yes(poll.fd(0).has(POLLNVAL));
		Assert.yes(poll.fd(1).hasErrors());
		Assert.yes(poll.fd(1).has(POLLNVAL));
		Assert.unordered(poll.fd(0).errors(), POLLNVAL);
		Assert.unordered(poll.fd(1).errors(), POLLNVAL);
	}

	@Test
	public void shouldValidateForErrors() throws IOException {
		initPipe();
		Assert.equal(poll.poll(), 1);
		poll.validate();
		pipe.close();
		pipe = null;
		Assert.equal(poll.poll(), 2);
		Assert.thrown(IOException.class, ".*POLLNVAL.*", () -> poll.validate());
	}

	@Test
	public void shouldNotPollWithSigsetOnMac() throws IOException {
		initPipe();
		JnaOs.mac.accept(_ -> Assert.unsupportedOp(() -> poll.poll(SigSet.of(Signal.SIGINT))));
	}

	@Test
	public void shouldPollWithSigsetOnLinux() throws IOException {
		initPipe();
		JnaOs.linux.accept(_ -> {
			try (var enc = TestCLibNative.register()) {
				enc.ref.poll.autoResponses(1, 2);
				Assert.equal(poll.poll(SigSet.of(Signal.SIGINT)), 1);
				Assert.equal(poll.poll(TimeSpec.ZERO, SigSet.of(Signal.SIGINT)), 2);
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
		Assert.equal(poll.size(), 2);
		poll.fd(0).fd(pipe.read).request(POLLIN);
		poll.fd(1).fd(pipe.write).request(POLLOUT);
	}
}
