package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.jna.clib.jna.CPoll.POLLHUP;
import static ceri.jna.clib.jna.CPoll.POLLIN;
import static ceri.jna.clib.jna.CPoll.POLLOUT;
import static ceri.jna.clib.jna.CPoll.POLLWRBAND;
import java.util.List;
import org.junit.Test;
import ceri.common.util.BasicUtil;
import ceri.jna.clib.jna.CPoll.pollfd;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.util.Struct;

public class CPollTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CPoll.class);
	}

	@Test
	public void testPoll() throws CException {
		TestCLibNative.exec(lib -> {
			var fds = pollfd.array(3);
			fds[0].fd = 1;
			fds[0].events = POLLIN | POLLOUT;
			fds[1].fd = 2;
			fds[1].events = POLLHUP;
			fds[2].fd = 3;
			fds[2].events = (short) POLLWRBAND;
			assertEquals(CPoll.poll(pollfd.array(0), 0), 0);
			assertThrown(() -> CPoll.poll(new pollfd[] { fds[0], fds[2] }, 100));
			assertEquals(CPoll.poll(fds, 100), 0);
			lib.poll.autoResponse(list -> {
				var fd = BasicUtil.<List<pollfd>>uncheckedCast(list.get(0)).get(0);
				fd.revents = POLLIN;
				Struct.write(fd);
				return 1;
			});
			assertEquals(CPoll.poll(fds, 100), 1);
			assertEquals(fds[0].revents, (short) POLLIN);
			lib.poll.assertAuto(List.of(List.of(fds), 100));
		});
	}

}
