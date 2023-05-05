package ceri.jna.clib.util;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.ErrorGen.RIX;
import static ceri.common.test.ErrorGen.RTX;
import static ceri.jna.test.JnaTestUtil.assertMemory;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.data.ByteProvider;
import ceri.common.io.StateChange;
import ceri.common.test.CallSync;
import ceri.common.test.TestUtil;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.Seek;
import ceri.jna.clib.jna.CError;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.test.TestFileDescriptor;
import ceri.jna.util.JnaUtil;
import ceri.log.test.LogModifier;

public class SelfHealingFdBehavior {
	private CallSync.Get<FileDescriptor> open;
	private TestFileDescriptor fd;
	private SelfHealingFd shf;

	@Before
	public void before() {
		fd = TestFileDescriptor.of(33);
		open = CallSync.supplier(fd);
		var config = SelfHealingFdConfig.builder(() -> open.get(IO_ADAPTER)).recoveryDelayMs(1)
			.fixRetryDelayMs(1).build();
		shf = SelfHealingFd.of(config);
	}

	@After
	public void after() {
		shf.close();
	}

	@Test
	public void shouldOpen() {
		open.error.setFrom(IOX);
		LogModifier.run(() -> {
			assertThrown(() -> shf.open());
			shf.close();
		}, Level.OFF, SelfHealingFd.class);
	}

	@Test
	public void shouldOpenQuietly() throws IOException {
		assertTrue(shf.openQuietly());
		assertEquals(shf.fd(), 33);
		open.error.setFrom(IOX);
		LogModifier.run(() -> {
			assertFalse(shf.openQuietly());
			shf.close();
		}, Level.OFF, SelfHealingFd.class);
	}

	@Test
	public void shouldRead() throws IOException {
		try (Memory m = JnaUtil.calloc(3)) {
			fd.read.autoResponses(ByteProvider.of(1, 2, 3));
			shf.open();
			assertEquals(shf.read(m), 3);
			assertMemory(m, 0, 1, 2, 3);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWrite() throws IOException {
		shf.open();
		shf.write(JnaUtil.mallocBytes(1, 2, 3));
		fd.write.assertAuto(ByteProvider.of(1, 2, 3));
	}

	@Test
	public void shouldSeek() throws IOException {
		shf.open();
		fd.seek.autoResponses(2);
		assertEquals(shf.seek(5, Seek.SEEK_SET), 2);
		fd.seek.assertAuto(List.of(5, Seek.SEEK_SET));
	}

	@Test
	public void shouldCallIoctl() throws IOException {
		shf.open();
		fd.ioctl.autoResponses(3);
		assertEquals(shf.ioctl(1, "test", -1), 3);
		fd.ioctl.assertAuto(List.of(1, "test", -1));
	}

	@Test
	public void shouldCallFcntl() throws IOException {
		shf.open();
		fd.fcntl.autoResponses(3);
		assertEquals(shf.fcntl(1, "test", -1), 3);
		fd.fcntl.assertAuto(List.of(1, "test", -1));
	}

	@Test
	public void shouldFailIfNotOpen() {
		assertThrown(() -> shf.fd());
		assertThrown(() -> shf.read(new Memory(3)));
	}

	@Test
	public void shouldListenForStateChanges() {
		CallSync.Accept<StateChange> listener = CallSync.consumer(null, false);
		shf.listeners().listen(listener::accept);
		try (var x = TestUtil.threadRun(shf::broken)) {
			listener.assertCall(StateChange.broken);
			listener.assertCall(StateChange.fixed);
		}
	}

	@Test
	public void shouldHandleBadListeners() {
		CallSync.Accept<StateChange> listener = CallSync.consumer(null, false);
		listener.error.setFrom(RTX, RIX);
		shf.listeners().listen(listener::accept);
		LogModifier.run(() -> {
			try (var x = TestUtil.threadRun(shf::broken)) {
				listener.assertCall(StateChange.broken);
				listener.assertCall(StateChange.fixed);
				shf.close();
			}
		}, Level.OFF, SelfHealingFd.class);
	}

	@Test
	public void shouldCheckIfBroken() throws IOException {
		shf.open();
		open.autoResponse(null); // disable auto response
		fd.seek.error.set(CException.of(CError.ENOENT, "test"));
		assertThrown(() -> shf.seek(0, Seek.SEEK_CUR));
		assertThrown(() -> shf.seek(0, Seek.SEEK_CUR));
		open.await(fd);
	}

	@Test
	public void shouldFixIfBroken() throws IOException {
		shf.open();
		open.autoResponse(null); // disable auto response
		open.error.setFrom(IOX, IOX, null);
		LogModifier.run(() -> {
			shf.broken();
			open.await(fd); // throws IOException
			open.await(fd); // throws IOException
			open.await(fd); // success
			shf.close();
		}, Level.OFF, SelfHealingFd.class);
	}

}
