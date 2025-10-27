package ceri.jna.clib.util;

import java.io.IOException;
import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Test;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Lambdas;
import ceri.common.io.StateChange;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.ErrorGen;
import ceri.common.test.TestUtil;
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
		shf = TestUtil.close(shf);
		fd = TestUtil.close(fd);
		open = null;
	}

	@Test
	public void shouldCreateFromProperties() throws IOException {
		try (var enc = TestCLibNative.register()) {
			var config =
				new SelfHealingFd.Properties(TestUtil.typedProperties("self-healing-fd"), "fd")
					.config();
			try (var _ = config.open()) {
				enc.ref.open
					.assertAuto(new OpenArgs("test", CFcntl.O_RDWR + CFcntl.O_APPEND, 0666));
				Assert.equal(config.selfHealing.fixRetryDelayMs, 123);
				Assert.equal(config.selfHealing.recoveryDelayMs, 456);
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCreateFromOpenFunction() throws IOException {
		var fd = TestFileDescriptor.of(33);
		var sync = CallSync.supplier(fd);
		var config = SelfHealingFd.Config.of(() -> sync.get(ExceptionAdapter.io));
		Assert.equal(config.open(), fd);
		sync.awaitAuto();
	}

	@Test
	public void shouldSpecifyBrokenPredicate() {
		var config = SelfHealingFd.Config.builder("test", Mode.NONE)
			.selfHealing(b -> b.brokenPredicate(Objects::nonNull)).build();
		Assert.equal(config.selfHealing.brokenPredicate.test(null), false);
		Assert.equal(config.selfHealing.brokenPredicate.test(new IOException()), true);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		var config = SelfHealingFd.Config.of(() -> TestFileDescriptor.of(33));
		Assert.find(config, "\\(2000,1000,%s\\)", Lambdas.name(config.selfHealing.brokenPredicate));
	}

	@Test
	public void shouldFailToOpenWithError() {
		init();
		open.error.setFrom(ErrorGen.IOX);
		LogModifier.run(() -> {
			Assert.thrown(() -> shf.open());
			shf.close();
		}, Level.OFF, SelfHealing.class);
	}

	@Test
	public void shouldOpenSilently() {
		init();
		open.error.setFrom(ErrorGen.IOX);
		LogModifier.run(() -> {
			Assert.no(shf.openSilently());
			shf.close();
		}, Level.OFF, SelfHealing.class);
	}

	@Test
	public void shouldListenForStateChanges() {
		init();
		CallSync.Consumer<StateChange> listener = CallSync.consumer(null, false);
		shf.listeners().listen(listener::accept);
		try (var _ = TestUtil.threadRun(shf::broken)) {
			listener.assertCall(StateChange.broken);
			listener.assertCall(StateChange.fixed);
		}
	}

	@Test
	public void shouldHandleBadListeners() {
		init();
		CallSync.Consumer<StateChange> listener = CallSync.consumer(null, false);
		listener.error.setFrom(ErrorGen.RTX, ErrorGen.RIX);
		shf.listeners().listen(listener::accept);
		LogModifier.run(() -> {
			try (var _ = TestUtil.threadRun(shf::broken)) {
				listener.assertCall(StateChange.broken);
				listener.assertCall(StateChange.fixed);
				shf.close();
			}
		}, Level.OFF, SelfHealing.class);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFailIfNotOpen() {
		init();
		Assert.thrown(() -> shf.in().read());
		Assert.thrown(() -> shf.out().write(0xff));
		Assert.thrown(() -> shf.accept(_ -> {}));
		Assert.thrown(() -> shf.apply(_ -> 0));
	}

	@Test
	public void shouldDelegateToFileDescriptor() throws IOException {
		init();
		shf.open();
		shf.accept(fd -> Assert.equal(fd, this.fd.fd()));
		Assert.equal(shf.apply(fd -> {
			Assert.equal(fd, this.fd.fd());
			return 77;
		}), 77);
	}

	@Test
	public void shouldGetDelegateFlags() throws IOException {
		try (var fd0 = TestFileDescriptor.of(33); var fd1 = TestFileDescriptor.of(33)) {
			fd0.flags.accept(0x11);
			fd1.flags.accept(0x22);
			open = CallSync.supplier(fd0, fd1);
			var config = SelfHealingFd.Config.builder(() -> open.get(ExceptionAdapter.io))
				.selfHealing(b -> b.recoveryDelayMs(1).fixRetryDelayMs(1)).build();
			shf = SelfHealingFd.of(config);
			shf.open();
			open.awaitAuto(); // fd0
			Assert.equal(shf.flags(), 0x11);
			shf.broken();
			open.awaitAuto(); // fd1
			Assert.equal(shf.flags(), 0x22);
		}
	}

	@Test
	public void shouldSetDelegateFlags() throws IOException {
		init();
		shf.open();
		FileDescriptor.FLAGS.set(shf, Open.RDWR, Open.APPEND);
		Assert.equal(fd.flags.lastValue(), CFcntl.O_RDWR | CFcntl.O_APPEND);
	}

	@Test
	public void shouldCheckCallIfBroken() throws IOException {
		init();
		shf.open();
		open.autoResponse(null); // disable auto response
		// now broken:
		Assert.thrown(() -> shf.accept(_ -> Assert.throwIt(ErrNo.ENOENT.error("test"))));
		// still broken:
		Assert.thrown(() -> shf.accept(_ -> Assert.throwIt(ErrNo.ENOENT.error("test"))));
		open.await(fd); // re-open
		Assert.thrown(() -> shf.accept(_ -> Assert.throwRuntime())); // not broken
	}

	@Test
	public void shouldFixIfBroken() throws IOException {
		init();
		shf.open();
		open.autoResponse(null); // disable auto response
		open.error.setFrom(ErrorGen.IOX, ErrorGen.IOX, null);
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
		var config = SelfHealingFd.Config.builder(() -> open.get(ExceptionAdapter.io))
			.selfHealing(b -> b.recoveryDelayMs(1).fixRetryDelayMs(1)).build();
		shf = SelfHealingFd.of(config);
	}
}
