package ceri.jna.clib.util;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.throwRuntime;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.ErrorGen.RIX;
import static ceri.common.test.ErrorGen.RTX;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.io.StateChange;
import ceri.common.test.CallSync;
import ceri.common.test.TestUtil;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.jna.CError;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.test.TestFileDescriptor;
import ceri.log.io.SelfHealingDevice;
import ceri.log.test.LogModifier;

public class SelfHealingFdBehavior {
	private CallSync.Supplier<FileDescriptor> open;
	private TestFileDescriptor fd;
	private SelfHealingFd shf;

	@Before
	public void before() {
		fd = TestFileDescriptor.of(33);
		open = CallSync.supplier(fd);
		var config = SelfHealingFdConfig.builder(() -> open.get(IO_ADAPTER))
			.selfHealing(b -> b.recoveryDelayMs(1).fixRetryDelayMs(1)).build();
		shf = SelfHealingFd.of(config);
	}

	@After
	public void after() {
		shf.close();
	}

	@Test
	public void shouldFailToOpenWithError() {
		open.error.setFrom(IOX);
		LogModifier.run(() -> {
			assertThrown(() -> shf.open());
			shf.close();
		}, Level.OFF, SelfHealingDevice.class);
	}

	@Test
	public void shouldOpenSilently() {
		open.error.setFrom(IOX);
		LogModifier.run(() -> {
			assertFalse(shf.openSilently());
			shf.close();
		}, Level.OFF, SelfHealingDevice.class);
	}

	@Test
	public void shouldListenForStateChanges() {
		CallSync.Consumer<StateChange> listener = CallSync.consumer(null, false);
		shf.listeners().listen(listener::accept);
		try (var x = TestUtil.threadRun(shf::broken)) {
			listener.assertCall(StateChange.broken);
			listener.assertCall(StateChange.fixed);
		}
	}

	@Test
	public void shouldHandleBadListeners() {
		CallSync.Consumer<StateChange> listener = CallSync.consumer(null, false);
		listener.error.setFrom(RTX, RIX);
		shf.listeners().listen(listener::accept);
		LogModifier.run(() -> {
			try (var x = TestUtil.threadRun(shf::broken)) {
				listener.assertCall(StateChange.broken);
				listener.assertCall(StateChange.fixed);
				shf.close();
			}
		}, Level.OFF, SelfHealingDevice.class);
	}

	@Test
	public void shouldFailIfNotOpen() {
		assertThrown(() -> shf.in().read());
		assertThrown(() -> shf.out().write(0xff));
		assertThrown(() -> shf.accept(fd -> {}));
		assertThrown(() -> shf.apply(fd -> 0));
	}

	@Test
	public void shouldDelegateToFileDescriptor() throws IOException {
		shf.open();
		shf.accept(fd -> assertEquals(fd, this.fd.fd()));
		assertEquals(shf.apply(fd -> {
			assertEquals(fd, this.fd.fd());
			return 77;
		}), 77);
	}

	@Test
	public void shouldCheckCallIfBroken() throws IOException {
		shf.open();
		open.autoResponse(null); // disable auto response
		assertThrown(() -> shf.accept(fd -> throwCException(CError.ENOENT))); // now broken
		assertThrown(() -> shf.accept(fd -> throwCException(CError.ENOENT))); // still broken
		open.await(fd); // re-open
		assertThrown(() -> shf.accept(fd -> throwRuntime())); // not broken
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
		}, Level.OFF, SelfHealingDevice.class);
	}

	private void throwCException(CError error) throws CException {
		throw CException.full("test", error);
	}

}
