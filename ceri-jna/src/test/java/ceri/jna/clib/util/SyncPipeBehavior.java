package ceri.jna.clib.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.ptr.IntByReference;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.test.TestUtil;
import ceri.common.util.CloseableUtil;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.clib.ErrNo;
import ceri.jna.clib.Poll;
import ceri.jna.clib.Poll.Event;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.util.JnaLibrary;

public class SyncPipeBehavior {
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();
	private Poll poll;
	private SyncPipe.Fixed pipe;
	private SyncPipe.Fd sync;
	private CFileDescriptor fd;
	private SimpleExecutor<RuntimeException, ?> thread;

	@After
	public void after() {
		CloseableUtil.close(thread, sync, pipe, fd, ref);
		poll = null;
		pipe = null;
		sync = null;
		fd = null;
		thread = null;
	}

	@Test
	public void shouldSignalOnce() throws IOException {
		initLib(1);
		pipe = SyncPipe.of(poll.fd(0));
		assertTrue(pipe.signal());
		assertFalse(pipe.signal());
	}

	@Test
	public void shouldSignalOnlyIfReadBytesUnavailable() throws IOException {
		var lib = initLib(1);
		pipe = SyncPipe.of(poll.fd(0));
		lib.ioctl.autoResponse(args -> { // available() always returns 1
			if (args.request() == CIoctl.FIONREAD) args.<IntByReference>arg(0).setValue(1);
		}, 0);
		assertFalse(pipe.signal());
		assertFalse(pipe.signal());
	}

	@Test
	public void shouldAllowSignalAfterWriteError() throws IOException {
		var lib = initLib(1);
		lib.write.error.setFrom(ErrNo.EBADFD::lastError, null);
		pipe = SyncPipe.of(poll.fd(0));
		assertThrown(IOException.class, pipe::signal);
		assertTrue(pipe.signal());
	}

	@Test
	public void shouldInterruptPoll() throws IOException {
		var poll = Poll.of(1);
		pipe = SyncPipe.of(poll.fd(0));
		thread = TestUtil.threadRun(pipe::signal);
		assertEquals(poll.poll(), 1);
		assertTrue(pipe.verifyPoll());
		pipe.clear();
		thread.get();
	}

	@Test
	public void shouldDoNothingAfterClosure() throws IOException {
		var lib = initLib(1);
		pipe = SyncPipe.of(poll.fd(0));
		pipe.close();
		lib.write.assertCalls(1);
		assertFalse(pipe.signal());
		pipe.clear();
		pipe.close();
		lib.write.assertCalls(1); // no change
		lib.read.assertCalls(0);
	}

	@Test
	public void shouldSyncSingleFd() throws IOException {
		var lib = initLib(0);
		fd = CFileDescriptor.open("test");
		sync = SyncPipe.fd(fd, Event.POLLIN);
		assertFalse(sync.poll());
		lib.pollAuto(args -> args.pollfd(0).revents = (short) Event.POLLIN.value);
		assertTrue(sync.poll());
	}

	@Test
	public void shouldSignalSingleFd() throws IOException {
		var lib = initLib(0);
		fd = CFileDescriptor.open("test");
		sync = SyncPipe.fd(fd, Event.POLLIN);
		assertTrue(sync.signal());
		assertFalse(sync.signal());
		lib.write.assertCalls(1);
		lib.read.assertCalls(0);
	}

	@Test
	public void shouldNotPollFdAfterClosure() throws IOException {
		var lib = initLib(0);
		fd = CFileDescriptor.open("test");
		sync = SyncPipe.fd(fd, Event.POLLIN);
		lib.poll.autoResponse(_ -> {
			fd.close();
			sync.close();
			return 0;
		});
		assertEquals(sync.poll(), false);
		assertEquals(sync.poll(), false);
		lib.poll.assertCalls(1);
	}

	private TestCLibNative initLib(int pollFds) {
		var lib = ref.init();
		poll = Poll.of(pollFds);
		lib.write.autoResponses(1);
		return lib;
	}
}
