package ceri.log.io;

import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.common.test.ErrorGen;
import ceri.common.test.TestFixable;
import ceri.common.test.Testing;
import ceri.log.test.LogModifier;

public class SelfHealingBehavior {
	private static final IOException BROKEN_EXCEPTION = new IOException("broken");
	private static final SelfHealing.Config CONF =
		SelfHealing.Config.of(0, 0, e -> e == BROKEN_EXCEPTION);
	private TestFixable fixable = null;
	private TestSelfHealingDevice device = null;

	private static class TestSelfHealingDevice extends SelfHealing<TestFixable> {
		private final TestFixable testFixable;

		private TestSelfHealingDevice(Config config, TestFixable testFixable) {
			super(config);
			this.testFixable = testFixable;
		}

		@Override
		protected TestFixable openDevice() throws IOException {
			testFixable.open();
			return testFixable;
		}
	}

	@After
	public void after() {
		Closeables.close(device, fixable);
		device = null;
		fixable = null;
	}

	@Test
	public void shouldCopyConfigExceptForDefaultBrokenPredicate() {
		var conf = SelfHealing.Config.builder().fixRetryDelayMs(1).recoveryDelayMs(3)
			.brokenPredicate(_ -> true).apply(SelfHealing.Config.DEFAULT).build();
		Assert.equal(conf.fixRetryDelayMs, SelfHealing.Config.DEFAULT.fixRetryDelayMs);
		Assert.equal(conf.recoveryDelayMs, SelfHealing.Config.DEFAULT.recoveryDelayMs);
		Assert.yes(conf.hasBrokenPredicate());
		Assert.yes(conf.broken(null));
	}

	@Test
	public void shouldCreateFromProperties() {
		var config =
			new SelfHealing.Properties(Testing.properties("self-healing"), "device").config();
		Assert.equal(config.fixRetryDelayMs, 123);
		Assert.equal(config.recoveryDelayMs, 456);
	}

	@Test
	public void shouldOnlyFailOnFirstOpen() throws IOException {
		init();
		LogModifier.run(() -> {
			fixable.open.autoResponse(false);
			fixable.open.error.setFrom(ErrorGen.IOX);
			try (var exec = Testing.threadRun(fixable.open::await)) {
				Assert.thrown(device::open);
				device.open();
				exec.get();
			}
			device.close();
		}, Level.OFF, SelfHealing.class);
	}

	@Test
	public void shouldTryToFixUntilSuccessful() {
		init();
		LogModifier.run(() -> {
			fixable.open.autoResponse(false);
			fixable.open.error.setFrom(ErrorGen.IOX, ErrorGen.IOX, ErrorGen.IOX, null);
			try (var exec = Testing.threadRun(() -> {
				fixable.open.await(); // IOX
				fixable.open.await(); // IOX
				fixable.open.await(); // IOX
				fixable.open.await(); // success
			})) {
				Assert.thrown(device::open);
				exec.get();
			}
		}, Level.OFF, SelfHealing.class);
	}

	@Test
	public void shouldOnlySetBrokenOnce() {
		init();
		fixable.open.autoResponse(false);
		device.broken(); // signal set
		device.broken(); // signal already set
	}

	@Test
	public void shouldCheckIfBroken() throws IOException {
		init();
		device.open();
		fixable.open.await();
		// device listens for errors from delegate
		// 1) exception -> not broken
		// 2) exception -> broken, set signal
		// 3) exception -> broken, signal already set
		Assert.thrown(() -> device.device.acceptValid(_ -> Assert.throwIo()));
		Assert.thrown(() -> device.device.acceptValid(_ -> Assert.throwIt(BROKEN_EXCEPTION)));
		Assert.thrown(() -> device.device.acceptValid(_ -> Assert.throwIt(BROKEN_EXCEPTION)));
		fixable.open.await();
	}

	private void init() {
		fixable = TestFixable.of();
		device = new TestSelfHealingDevice(CONF, fixable);
	}
}
