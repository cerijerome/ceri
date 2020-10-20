package ceri.log.rpc.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import java.io.IOException;
import org.junit.Test;
import ceri.common.concurrent.SimpleExecutor;

public class TestObserverBehavior {

	@Test
	public void shouldAwaitNext() throws InterruptedException {
		TestObserver<String> observer = TestObserver.of();
		try (var exec = SimpleExecutor.run(() -> observer.onNext("test"))) {
			assertEquals(observer.next.await(), "test");
		}
	}

	@Test
	public void shouldAwaitCompletion() throws InterruptedException {
		TestObserver<String> observer = TestObserver.of();
		try (var exec = SimpleExecutor.run(observer::onCompleted)) {
			observer.completed.await();
		}
	}

	@Test
	public void shouldAwaitError() throws InterruptedException {
		TestObserver<String> observer = TestObserver.of();
		IOException iox = new IOException("iox");
		try (var exec = SimpleExecutor.run(() -> observer.onError(iox))) {
			assertEquals(observer.error.await(), iox);
		}
	}

	@Test
	public void shouldClearConditions() {
		TestObserver<String> observer = TestObserver.of();
		observer.onNext("test");
		observer.onCompleted();
		observer.onError(new IOException("iox"));
		observer.clear();
		assertNull(observer.next.value());
		assertFalse(observer.completed.isSet());
		assertNull(observer.error.value());
	}

}
