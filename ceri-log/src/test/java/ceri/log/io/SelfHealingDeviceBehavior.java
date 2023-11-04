package ceri.log.io;

import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.throwIo;
import static ceri.common.test.AssertUtil.throwIt;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.TestUtil.threadRun;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.test.TestFixable;
import ceri.log.test.LogModifier;

public class SelfHealingDeviceBehavior {
	private static final IOException BROKEN_EXCEPTION = new IOException("broken");
	private static final SelfHealingConfig CONF =
		SelfHealingConfig.of(0, 0, e -> e == BROKEN_EXCEPTION);
	private TestFixable fixable;
	private TestSelfHealingDevice device;

	private static class TestSelfHealingDevice extends SelfHealingDevice<TestFixable> {
		private final TestFixable testFixable;

		private TestSelfHealingDevice(SelfHealingConfig config, TestFixable testFixable) {
			super(config);
			this.testFixable = testFixable;
		}

		@Override
		protected TestFixable openDevice() throws IOException {
			testFixable.open();
			return testFixable;
		}
	}

	@Before
	public void before() {
		fixable = TestFixable.of();
		device = new TestSelfHealingDevice(CONF, fixable);
	}

	@After
	public void after() throws IOException {
		device.close();
		fixable.close();
	}

	@Test
	public void shouldOnlyFailOnFirstOpen() throws IOException {
		LogModifier.run(() -> {
			fixable.open.autoResponse(false);
			fixable.open.error.setFrom(IOX);
			try (var exec = threadRun(fixable.open::await)) {
				assertThrown(device::open);
				device.open();
				exec.get();
			}
			device.close();
		}, Level.OFF, SelfHealingDevice.class);
	}

	@Test
	public void shouldTryToFixUntilSuccessful() {
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
		}, Level.OFF, SelfHealingDevice.class);
	}

	@Test
	public void shouldOnlySetBrokenOnce() {
		fixable.open.autoResponse(false);
		device.broken(); // signal set
		device.broken(); // signal already set
	}

	@Test
	public void shouldCheckIfBroken() throws IOException {
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

}
