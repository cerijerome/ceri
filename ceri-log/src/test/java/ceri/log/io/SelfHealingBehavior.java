package ceri.log.io;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.throwIo;
import static ceri.common.test.AssertUtil.throwIt;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.TestUtil.baseProperties;
import static ceri.common.test.TestUtil.threadRun;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Test;
import ceri.common.test.TestFixable;
import ceri.common.util.CloseableUtil;
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
		CloseableUtil.close(device, fixable);
		device = null;
		fixable = null;
	}

	@Test
	public void shouldCopyConfigExceptForDefaultBrokenPredicate() {
		var conf = SelfHealing.Config.builder().fixRetryDelayMs(1).recoveryDelayMs(3)
			.brokenPredicate(e -> true).apply(SelfHealing.Config.DEFAULT).build();
		assertEquals(conf.fixRetryDelayMs, SelfHealing.Config.DEFAULT.fixRetryDelayMs);
		assertEquals(conf.recoveryDelayMs, SelfHealing.Config.DEFAULT.recoveryDelayMs);
		assertTrue(conf.hasBrokenPredicate());
		assertTrue(conf.broken(null));
	}

	@Test
	public void shouldCreateFromProperties() {
		var config = new SelfHealingProperties(baseProperties("self-healing"), "device").config();
		assertEquals(config.fixRetryDelayMs, 123);
		assertEquals(config.recoveryDelayMs, 456);
	}

	@Test
	public void shouldOnlyFailOnFirstOpen() throws IOException {
		init();
		LogModifier.run(() -> {
			fixable.open.autoResponse(false);
			fixable.open.error.setFrom(IOX);
			try (var exec = threadRun(fixable.open::await)) {
				assertThrown(device::open);
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
			fixable.open.error.setFrom(IOX, IOX, IOX, null);
			try (var exec = threadRun(() -> {
				fixable.open.await(); // IOX
				fixable.open.await(); // IOX
				fixable.open.await(); // IOX
				fixable.open.await(); // success
			})) {
				assertThrown(device::open);
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
		assertThrown(() -> device.device.acceptValid(f -> throwIo()));
		assertThrown(() -> device.device.acceptValid(f -> throwIt(BROKEN_EXCEPTION)));
		assertThrown(() -> device.device.acceptValid(f -> throwIt(BROKEN_EXCEPTION)));
		fixable.open.await();
	}

	private void init() {
		fixable = TestFixable.of();
		device = new TestSelfHealingDevice(CONF, fixable);
	}
}
