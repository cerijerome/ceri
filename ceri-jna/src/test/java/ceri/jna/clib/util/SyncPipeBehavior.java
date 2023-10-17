package ceri.jna.clib.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorGen.IOX;
import java.io.IOException;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.test.TestUtil;
import ceri.jna.clib.jna.CPoll;
import ceri.jna.clib.jna.CPoll.pollfd;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.clib.test.TestCLibNative;

public class SyncPipeBehavior {

	@Test
	public void shouldEncapsulatePollingWithFileDescriptors() throws IOException {
		int[] fds = CUnistd.pipe();
		try (var poll = SyncPipe.poll(2); var pipe = SyncPipe.of(); var m = new Memory(1)) {
			poll.get(0).init(fds[0], CPoll.POLLIN); // read fd
			assertEquals(poll.pollPeek(0), 0);
			CUnistd.write(fds[1], m, 1); // write fd
			assertEquals(poll.poll(0), 1);
		} finally {
			CUnistd.closeSilently(fds);
		}
	}

	@Test
	public void shouldEncapsulatePollingForAnEmptyFileDescriptorArray() throws IOException {
		try (var poll = SyncPipe.poll(0)) {
			assertEquals(poll.list.size(), 0);
			assertEquals(poll.count(), 0);
			assertThrown(() -> poll.get(0));
			assertEquals(poll.pollPeek(0), 0);
			poll.signal();
			assertEquals(poll.pollPeek(0), 0);
			assertEquals(poll.poll(0), 0);
		}
	}

	@Test
	public void shouldNotWaitForPollWhenClosed() throws IOException {
		try (var poll = SyncPipe.poll(0)) {
			poll.close();
			assertEquals(poll.poll(0), 0);
		}
	}

	@Test
	public void shouldInterruptPoll() throws IOException {
		try (var pipe = SyncPipe.of(); var x = TestUtil.threadRun(pipe::signal)) {
			pollfd[] pollfds = pollfd.array(1);
			pipe.init(pollfds[0]);
			assertEquals(CPoll.poll(pollfds, 10000), 1);
			assertEquals(pollfds[0].revents & CPoll.POLLIN, CPoll.POLLIN);
			pipe.clear();
			x.get();
		}
	}

	@Test
	public void shouldClosePipeOnCreationError() {
		TestCLibNative.exec(lib -> {
			lib.fcntl.error.setFrom(IOX);
			assertThrown(SyncPipe::of);
			lib.close.assertCalls(2);
		});
	}

	@Test
	public void shouldSignalOnceOnly() throws IOException {
		TestCLibNative.exec(lib -> {
			lib.write.autoResponses(1);
			try (var pipe = SyncPipe.of()) {
				pipe.signal();
				lib.write.assertCalls(1);
				pipe.signal();
				lib.write.assertCalls(1); // no additional calls
			}
		});
	}

	@Test
	public void shouldSignalAfterFailure() throws IOException {
		TestCLibNative.exec(lib -> {
			lib.write.error.setFrom(IOX, null);
			lib.write.autoResponses(1, 0, 1);
			try (var pipe = SyncPipe.of()) {
				assertThrown(pipe::signal);
				lib.write.assertCalls(1);
				assertFalse(pipe.signal());
				lib.write.assertCalls(2);
				assertTrue(pipe.signal());
				lib.write.assertCalls(2);
			}
		});
	}

	@Test
	public void shouldVerify() throws IOException {
		TestCLibNative.exec(lib -> {
			try (var pipe = SyncPipe.of()) {
				pollfd pollfd = new pollfd();
				assertThrown(() -> pipe.verify(pollfd)); // fd doesn't match
				pipe.init(pollfd);
				assertFalse(pipe.verify(pollfd));
				pollfd.revents = CPoll.POLLOUT;
				assertFalse(pipe.verify(pollfd));
				pollfd.revents |= CPoll.POLLIN;
				assertTrue(pipe.verify(pollfd));
			}
		});
	}

	@Test
	public void shouldClearOnlyIfSignalled() throws IOException {
		TestCLibNative.exec(lib -> {
			try (var pipe = SyncPipe.of()) {
				pollfd pollfd = new pollfd();
				assertThrown(() -> pipe.clear(pollfd)); // fd doesn't match
				pipe.init(pollfd);
				pipe.clear(pollfd);
				lib.read.assertCalls(0);
				pollfd.revents = CPoll.POLLIN;
				pipe.clear(pollfd);
				lib.read.assertCalls(1);
			}
		});
	}

	@Test
	public void shouldDoNothingAfterClosure() throws IOException {
		TestCLibNative.exec(lib -> {
			try (var pipe = SyncPipe.of()) {
				pipe.close();
				lib.write.assertCalls(1);
				assertFalse(pipe.signal());
				pipe.clear();
			}
			lib.write.assertCalls(1); // no change
			lib.read.assertCalls(0);
		});
	}

}
