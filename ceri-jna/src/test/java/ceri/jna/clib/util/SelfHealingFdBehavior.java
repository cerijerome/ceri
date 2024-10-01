package ceri.jna.clib.util;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.throwIt;
import static ceri.common.test.AssertUtil.throwRuntime;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.ErrorGen.RIX;
import static ceri.common.test.ErrorGen.RTX;
import static ceri.common.test.TestUtil.typedProperties;
import static ceri.jna.clib.jna.CFcntl.O_APPEND;
import static ceri.jna.clib.jna.CFcntl.O_RDWR;
import java.io.IOException;
import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Test;
import ceri.common.io.StateChange;
import ceri.common.test.CallSync;
import ceri.common.test.TestUtil;
import ceri.common.util.CloseableUtil;
import ceri.jna.clib.ErrNo;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.FileDescriptor.Open;
import ceri.jna.clib.Mode;
import ceri.jna.clib.jna.CFcntl;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.OpenArgs;
import ceri.jna.clib.test.TestFileDescriptor;
import ceri.log.io.SelfHealing;
import ceri.log.test.LogModifier;

public class SelfHealingFdBehavior {
	private CallSync.Supplier<FileDescriptor> open;
	private TestFileDescriptor fd;
	private SelfHealingFd shf;

	@After
	public void after() {
		CloseableUtil.close(shf, fd);
		shf = null;
		fd = null;
		open = null;
	}

	@Test
	public void shouldCreateFromProperties() throws IOException {
		try (var enc = TestCLibNative.register()) {
			var lib = enc.ref;
			var config =
				new SelfHealingFdProperties(typedProperties("self-healing-fd"), "fd").config();
			try (var fd = config.open()) {
				lib.open.assertAuto(new OpenArgs("test", O_RDWR + O_APPEND, 0666));
				assertEquals(config.selfHealing.fixRetryDelayMs, 123);
				assertEquals(config.selfHealing.recoveryDelayMs, 456);
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCreateFromOpenFunction() throws IOException {
		TestFileDescriptor fd = TestFileDescriptor.of(33);
		CallSync.Supplier<FileDescriptor> sync = CallSync.supplier(fd);
		var config = SelfHealingFd.Config.of(() -> sync.get(IO_ADAPTER));
		assertEquals(config.open(), fd);
		sync.awaitAuto();
	}

	@Test
	public void shouldSpecifyBrokenPredicate() {
		var config = SelfHealingFd.Config.builder("test", Mode.NONE)
			.selfHealing(b -> b.brokenPredicate(Objects::nonNull)).build();
		assertEquals(config.selfHealing.brokenPredicate.test(null), false);
		assertEquals(config.selfHealing.brokenPredicate.test(new IOException()), true);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		var config = SelfHealingFd.Config.of(() -> TestFileDescriptor.of(33));
		assertFind(config, "\\(2000,1000,%s\\)", config.selfHealing.brokenPredicate);
	}

	@Test
	public void shouldFailToOpenWithError() {
		init();
		open.error.setFrom(IOX);
		LogModifier.run(() -> {
			assertThrown(() -> shf.open());
			shf.close();
		}, Level.OFF, SelfHealing.class);
	}

	@Test
	public void shouldOpenSilently() {
		init();
		open.error.setFrom(IOX);
		LogModifier.run(() -> {
			assertFalse(shf.openSilently());
			shf.close();
		}, Level.OFF, SelfHealing.class);
	}

	@Test
	public void shouldListenForStateChanges() {
		init();
		CallSync.Consumer<StateChange> listener = CallSync.consumer(null, false);
		shf.listeners().listen(listener::accept);
		try (var x = TestUtil.threadRun(shf::broken)) {
			listener.assertCall(StateChange.broken);
			listener.assertCall(StateChange.fixed);
		}
	}

	@Test
	public void shouldHandleBadListeners() {
		init();
		CallSync.Consumer<StateChange> listener = CallSync.consumer(null, false);
		listener.error.setFrom(RTX, RIX);
		shf.listeners().listen(listener::accept);
		LogModifier.run(() -> {
			try (var x = TestUtil.threadRun(shf::broken)) {
				listener.assertCall(StateChange.broken);
				listener.assertCall(StateChange.fixed);
				shf.close();
			}
		}, Level.OFF, SelfHealing.class);
	}

	@Test
	public void shouldFailIfNotOpen() {
		init();
		assertThrown(() -> shf.in().read());
		assertThrown(() -> shf.out().write(0xff));
		assertThrown(() -> shf.accept(fd -> {}));
		assertThrown(() -> shf.apply(fd -> 0));
	}

	@Test
	public void shouldDelegateToFileDescriptor() throws IOException {
		init();
		shf.open();
		shf.accept(fd -> assertEquals(fd, this.fd.fd()));
		assertEquals(shf.apply(fd -> {
			assertEquals(fd, this.fd.fd());
			return 77;
		}), 77);
	}

	@Test
	public void shouldGetDelegateFlags() throws IOException {
		try (var fd0 = TestFileDescriptor.of(33); var fd1 = TestFileDescriptor.of(33)) {
			fd0.flags.accept(0x11);
			fd1.flags.accept(0x22);
			open = CallSync.supplier(fd0, fd1);
			var config = SelfHealingFd.Config.builder(() -> open.get(IO_ADAPTER))
				.selfHealing(b -> b.recoveryDelayMs(1).fixRetryDelayMs(1)).build();
			shf = SelfHealingFd.of(config);
			var flags = shf.flags();
			shf.open();
			open.awaitAuto(); // fd0
			assertEquals(flags.field().getInt(), 0x11);
			shf.broken();
			open.awaitAuto(); // fd1
			assertEquals(flags.field().getInt(), 0x22);
		}
	}

	@Test
	public void shouldSetDelegateFlags() throws IOException {
		init();
		shf.open();
		shf.flags().set(Open.RDWR, Open.APPEND);
		assertEquals(fd.flags.lastValue(), CFcntl.O_RDWR | CFcntl.O_APPEND);
	}

	@Test
	public void shouldCheckCallIfBroken() throws IOException {
		init();
		shf.open();
		open.autoResponse(null); // disable auto response
		assertThrown(() -> shf.accept(fd -> throwIt(ErrNo.ENOENT.error("test")))); // now broken
		assertThrown(() -> shf.accept(fd -> throwIt(ErrNo.ENOENT.error("test")))); // still broken
		open.await(fd); // re-open
		assertThrown(() -> shf.accept(fd -> throwRuntime())); // not broken
	}

	@Test
	public void shouldFixIfBroken() throws IOException {
		init();
		shf.open();
		open.autoResponse(null); // disable auto response
		open.error.setFrom(IOX, IOX, null);
		LogModifier.run(() -> {
			shf.broken();
			open.await(fd); // throws IOException
			open.await(fd); // throws IOException
			open.await(fd); // success
			shf.close();
		}, Level.OFF, SelfHealing.class);
	}

	private void init() {
		fd = TestFileDescriptor.of(33);
		open = CallSync.supplier(fd);
		var config = SelfHealingFd.Config.builder(() -> open.get(IO_ADAPTER))
			.selfHealing(b -> b.recoveryDelayMs(1).fixRetryDelayMs(1)).build();
		shf = SelfHealingFd.of(config);
	}
}
