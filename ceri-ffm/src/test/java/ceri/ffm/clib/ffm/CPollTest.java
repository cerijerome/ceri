package ceri.ffm.clib.ffm;

import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.ffm.clib.ffm.CPoll.pollfd;
import ceri.ffm.clib.test.TestCLibNative;
import ceri.ffm.test.FfmAssert;
import ceri.ffm.test.FfmTesting;

public class CPollTest {
	private final FfmTesting.Lib<TestCLibNative> lib = TestCLibNative.lib();

	@After
	public void after() {
		Closeables.close(lib);
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(CPoll.class);
	}

	@Test
	public void testPollWithPipe() throws CException {
		int[] fds = CUnistd.pipe();
		try {
			pollfd pfd = new pollfd();
			pfd.fd = fds[0];
			pfd.events = CPoll.POLLIN;
			Assert.equal(CPoll.poll(0, pfd), 0);
			Assert.equal(CUnistd.write(fds[1], 33), 1);
			Assert.equal(CPoll.poll(10000, pfd), 1);
			Assert.equal(pfd.revents & CPoll.POLLIN, CPoll.POLLIN);
		} finally {
			CUnistd.closeSilently(fds);
		}
	}

	@Test
	public void testPollEmulated() throws CException {
		lib.init().poll.autoResponse(p -> {
			for (var pollFd : p.pollFds())
				pollFd.revents = pollFd.events;
		}, null);
		var pollFds = pollfd.$.initArray(3);
		pollFds[0].fd = 1;
		pollFds[0].events = CPoll.POLLIN | CPoll.POLLOUT;
		pollFds[1].fd = 2;
		pollFds[1].events = CPoll.POLLPRI;
		pollFds[2].fd = 3;
		pollFds[2].events = CPoll.POLLIN;
		Assert.equal(CPoll.poll(0), 0);
		Assert.equal(CPoll.poll(0, pollFds), 3);
		Assert.equals(pollFds[0].revents, CPoll.POLLIN | CPoll.POLLOUT);
		Assert.equals(pollFds[1].revents, CPoll.POLLPRI);
		Assert.equals(pollFds[2].revents, CPoll.POLLIN);
		Assert.equal(CPoll.poll(0, pollFds[0], pollFds[2]), 2);
		Assert.equals(pollFds[0].revents, CPoll.POLLIN | CPoll.POLLOUT);
		Assert.equals(pollFds[2].revents, CPoll.POLLIN);
	}

	@Test
	public void testPollEmulatedErrors() throws CException {
		lib.init().poll.autoResponses(null, CErrNo.EFAULT);
		var pollFd = new pollfd();
		var pointer = pollfd.$.pointerOf(pollFd);
		Assert.equal(CPoll.poll(pointer, 0), 0);
		FfmAssert.cexception(CErrNo.EFAULT, () -> CPoll.poll(pointer, 0));
	}
}
