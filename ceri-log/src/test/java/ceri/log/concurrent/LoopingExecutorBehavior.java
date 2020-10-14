package ceri.log.concurrent;

import static ceri.common.concurrent.ConcurrentUtil.delayMicros;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import ceri.common.concurrent.ValueCondition;
import ceri.common.function.ExceptionIntConsumer;
import ceri.log.test.LogModifier;

public class LoopingExecutorBehavior {

	@Test
	public void shouldLoop() throws InterruptedException {
		ValueCondition<Integer> sync = ValueCondition.of();
		try (TestLoop loop = new TestLoop(sync::signal)) {
			assertTrue(sync.await(i -> i > 3) > 3);
		}
	}

	@Test
	public void shouldStopOnException() throws InterruptedException {
		LogModifier.run(() -> {
			try (TestLoop loop = new TestLoop(i -> throwIoException())) {
				loop.waitUntilStopped();
				loop.waitUntilStopped(1);
				assertThat(loop.stopped(), is(true));
			}
		}, Level.OFF, LoopingExecutor.class);
	}

	@Test
	public void shouldUseLogName() {
		try (TestLoop loop = new TestLoop("testloop", i -> delayMicros(10))) {}
	}

	private static void throwIoException() throws IOException {
		throw new IOException("test");
	}

	private static class TestLoop extends LoopingExecutor {
		private final ExceptionIntConsumer<?> looper;
		private int count = 0;

		TestLoop(String logName, ExceptionIntConsumer<?> looper) {
			super(logName);
			this.looper = looper;
			start();
		}

		TestLoop(ExceptionIntConsumer<?> looper) {
			this.looper = looper;
			start();
		}

		@Override
		protected void loop() throws Exception {
			looper.accept(count++);
		}
	}
}
