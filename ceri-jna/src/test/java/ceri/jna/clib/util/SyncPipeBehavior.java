package ceri.jna.clib.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.IOX;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.TestUtil;
import ceri.jna.clib.jna.CPoll;
import ceri.jna.clib.jna.CPoll.pollfd;
import ceri.jna.clib.test.TestCLibNative;

public class SyncPipeBehavior {

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
			lib.write.autoResponses(0, 1);
			try (var pipe = SyncPipe.of()) {
				assertThrown(pipe::signal);
				lib.write.assertCalls(1);
				pipe.signal();
				lib.write.assertCalls(2);
			}
		});
	}
}
