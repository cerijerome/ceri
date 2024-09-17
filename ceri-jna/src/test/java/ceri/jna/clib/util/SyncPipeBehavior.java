package ceri.jna.clib.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.ptr.IntByReference;
import ceri.common.test.TestUtil;
import ceri.common.util.CloseableUtil;
import ceri.common.util.Enclosed;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.clib.ErrNo;
import ceri.jna.clib.Poll;
import ceri.jna.clib.Poll.Event;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.clib.test.TestCLibNative;

public class SyncPipeBehavior {
	private TestCLibNative lib;
	private Enclosed<RuntimeException, TestCLibNative> enc;
	private Poll poll;

	@After
	public void after() {
		CloseableUtil.close(enc);
		enc = null;
		lib = null;
		poll = null;
	}

	@Test
	public void shouldSignalOnce() throws IOException {
		initLib(1);
		try (var pipe = SyncPipe.of(poll.fd(0))) {
			assertTrue(pipe.signal());
			assertFalse(pipe.signal());
		}
	}

	@Test
	public void shouldSignalOnlyIfReadBytesUnavailable() throws IOException {
		initLib(1);
		try (var pipe = SyncPipe.of(poll.fd(0))) {
			lib.ioctl.autoResponse(args -> { // available() always returns 1
				if (args.request() == CIoctl.FIONREAD) args.<IntByReference>arg(0).setValue(1);
			}, 0);
			assertFalse(pipe.signal());
			assertFalse(pipe.signal());
		}
	}

	@Test
	public void shouldAllowSignalAfterWriteError() throws IOException {
		initLib(1);
		lib.write.error.setFrom(ErrNo.EBADFD::lastError, null);
		try (var pipe = SyncPipe.of(poll.fd(0))) {
			assertThrown(IOException.class, pipe::signal);
			assertTrue(pipe.signal());
		}
	}

	@Test
	public void shouldInterruptPoll() throws IOException {
		var poll = Poll.of(1);
		try (var pipe = SyncPipe.of(poll.fd(0)); var x = TestUtil.threadRun(pipe::signal)) {
			assertEquals(poll.poll(), 1);
			assertTrue(pipe.verifyPoll());
			pipe.clear();
			x.get();
		}
	}

	@Test
	public void shouldDoNothingAfterClosure() throws IOException {
		initLib(1);
		try (var pipe = SyncPipe.of(poll.fd(0))) {
			pipe.close();
			lib.write.assertCalls(1);
			assertFalse(pipe.signal());
			pipe.clear();
		}
		lib.write.assertCalls(1); // no change
		lib.read.assertCalls(0);
	}

	@Test
	public void shouldSyncSingleFd() throws IOException {
		initLib(0);
		try (var fd = CFileDescriptor.open("test"); var sync = SyncPipe.fd(fd, Event.POLLIN)) {
			assertFalse(sync.poll());
			lib.pollAuto(args -> args.pollfd(0).revents = (short) Event.POLLIN.value);
			assertTrue(sync.poll());
		}
	}

	@Test
	public void shouldSignalSingleFd() throws IOException {
		initLib(0);
		try (var fd = CFileDescriptor.open("test"); var sync = SyncPipe.fd(fd, Event.POLLIN)) {
			assertTrue(sync.signal());
			assertFalse(sync.signal());
			lib.write.assertCalls(1);
			lib.read.assertCalls(0);
		}
	}

	@Test
	public void shouldNotPollFdAfterClosure() throws IOException {
		initLib(0);
		try (var fd = CFileDescriptor.open("test"); var sync = SyncPipe.fd(fd, Event.POLLIN)) {
			sync.poll();
			sync.close();
			sync.poll();
			lib.poll.assertCalls(1);
		}
	}

	private void initLib(int pollFds) {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
		poll = Poll.of(pollFds);
		lib.write.autoResponses(1);
	}
}
