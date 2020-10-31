package ceri.common.io;

import static ceri.common.test.ErrorProducer.IOX;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Predicate;
import org.junit.Test;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.test.TestInputStream;

public class KeyCheckerBehavior {

	@Test
	public void shouldShutdownOnClose() {
		try (KeyChecker k = KeyChecker.of()) {}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldIgnoreNoInput() throws InterruptedException {
		BooleanCondition sync = BooleanCondition.of();
		try (KeyChecker k = keyChecker(TestInputStream.of(), sync, s -> true)) {
			sync.await();
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldExecuteActionOnIoError() throws InterruptedException {
		BooleanCondition sync = BooleanCondition.of();
		TestInputStream in = TestInputStream.of();
		in.available.error.setFrom(IOX);
		try (KeyChecker k = keyChecker(in, sync, s -> true)) {
			sync.await();
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldExecuteActionOnMatch() throws IOException, InterruptedException {
		BooleanCondition sync = BooleanCondition.of();
		TestInputStream in = TestInputStream.of();
		try (KeyChecker k = keyChecker(in, sync, s -> s.contains("test"))) {
			in.to.writeAscii("123");
			in.awaitFeed();
			in.to.writeAscii("test");
			in.awaitFeed();
			sync.await();
		}
	}

	private KeyChecker keyChecker(InputStream in, BooleanCondition sync,
		Predicate<String> checkFn) {
		return KeyChecker.builder().shutdownTimeoutMs(1000).pollMs(1).in(in).action(sync::signal)
			.checkFunction(checkFn).build();
	}

}
