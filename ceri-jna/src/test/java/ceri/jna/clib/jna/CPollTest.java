package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.jna.clib.jna.CPoll.POLLIN;
import static ceri.jna.clib.jna.CPoll.POLLOUT;
import static ceri.jna.clib.jna.CPoll.POLLPRI;
import java.util.List;
import org.junit.Test;
import ceri.jna.clib.jna.CPoll.pollfd;
import ceri.jna.clib.test.TestCLibNative;

public class CPollTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CPoll.class);
	}

	@Test
	public void testPollfdVerify() throws CException {
		var pollfd = new pollfd();
		pollfd.revents = POLLOUT;
		pollfd.verify();
		pollfd.revents |= CPoll.POLLNVAL;
		assertThrown(() -> pollfd.verify());
		pollfd.revents |= CPoll.POLLHUP;
		assertThrown(() -> pollfd.verify());
		pollfd.revents |= CPoll.POLLERR;
		assertThrown(() -> pollfd.verify());
	}

	@Test
	public void testSinglePoll() throws CException {
		TestCLibNative.exec(lib -> {
			var pollfd = new pollfd().init(1, POLLIN | POLLOUT);
			lib.pollAutoResponse((pollfds, t) -> pollfds[0].revents = POLLIN);
			assertTrue(CPoll.poll(pollfd, 100));
			assertTrue(pollfd.hasEvent(POLLIN));
			lib.pollAutoResponse((pollfds, t) -> {});
			pollfd.init(1, POLLOUT);
			assertFalse(CPoll.poll(pollfd, 99));
			assertFalse(pollfd.hasEvent(POLLIN));
			lib.poll.assertValues(List.of(List.of(pollfd), 100), List.of(List.of(pollfd), 99));
		});
	}

	@Test
	public void testPoll() throws CException {
		TestCLibNative.exec(lib -> {
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
			lib.pollAutoResponse((pollfds, t) -> pollfds[0].revents = POLLIN);
			assertEquals(CPoll.poll(fds, 100), 1);
			assertEquals(fds[0].revents, (short) POLLIN);
			lib.poll.assertAuto(List.of(List.of(fds), 100));
		});
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

}
