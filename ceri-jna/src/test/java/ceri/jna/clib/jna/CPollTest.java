package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.jna.clib.jna.CPoll.POLLIN;
import static ceri.jna.clib.jna.CPoll.POLLOUT;
import static ceri.jna.clib.jna.CPoll.POLLPRI;
import org.junit.After;
import org.junit.Test;
import ceri.common.time.TimeSpec;
import ceri.common.util.Enclosed;
import ceri.jna.clib.jna.CPoll.pollfd;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.PollArgs;
import ceri.log.util.LogUtil;

public class CPollTest {
	private Enclosed<?, TestCLibNative> enc;
	private TestCLibNative lib;

	@After
	public void after() {
		LogUtil.close(enc);
		enc = null;
		lib = null;
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CPoll.class);
	}

	@Test
	public void testPoll() throws CException {
		initLib();
		var fds = pollfd.array(3);
		fds[0].fd = 1;
		fds[0].events = POLLIN | POLLOUT;
		fds[1].fd = 2;
		fds[1].events = POLLPRI;
		fds[2].fd = 3;
		fds[2].events = (short) POLLIN;
		assertEquals(CPoll.poll(pollfd.array(0), 0), 0);
		assertThrown(() -> CPoll.poll(new pollfd[] { fds[0], fds[2] }, 100));
		assertEquals(CPoll.poll(fds, 100), 0);
		lib.pollAuto(args -> args.pollfd(0).revents = POLLIN);
		assertEquals(CPoll.poll(fds, 100), 1);
		assertEquals(fds[0].revents, (short) POLLIN);
		lib.poll.assertAuto(PollArgs.of(fds, 100));
	}

	@Test
	public void testPollWithPipe() throws CException {
		int[] fds = CUnistd.pipe();
		try {
			assertEquals(CUnistd.write(fds[1], 33), 1);
			pollfd[] pfds = pollfd.array(1);
			pfds[0].fd = fds[0];
			pfds[0].events = CPoll.POLLIN;
			assertEquals(CPoll.poll(pfds, 10000), 1);
			assertEquals(pfds[0].revents & CPoll.POLLIN, CPoll.POLLIN);
		} finally {
			CUnistd.closeSilently(fds);
		}
	}

	@Test
	public void testPpoll() throws CException {
		initLib();
		var fds = pollfd.array(3);
		fds[0].fd = 1;
		fds[0].events = POLLIN | POLLOUT;
		fds[1].fd = 2;
		fds[1].events = POLLPRI;
		fds[2].fd = 3;
		fds[2].events = (short) POLLIN;
		var sigset = new CSignal.sigset_t();
		CSignal.sigaddset(sigset, CSignal.SIGINT);
		var tmo = new CTime.timespec().time(TimeSpec.ofMillis(0, 1));
		assertEquals(CPoll.Linux.ppoll(fds, tmo, sigset), 0);
		assertEquals(CPoll.Linux.ppoll(fds, tmo, null), 0);
		assertEquals(CPoll.Linux.ppoll(fds, null, null), 0);
		lib.pollAuto(args -> args.pollfd(0).revents = POLLIN);
		assertEquals(CPoll.Linux.ppoll(fds, tmo, sigset), 1);
		assertEquals(fds[0].revents, (short) POLLIN);
		lib.poll.assertAuto(PollArgs.of(fds, 1000000, CSignal.SIGINT));
	}

	private void initLib() {
		enc = TestCLibNative.register();
		lib = enc.ref;
	}
}
