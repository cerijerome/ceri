package ceri.log.rpc.test;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;

public class TestObserverBehavior {

	@Test
	public void shouldSetNext() {
		var observer = TestStreamObserver.<String>of();
		observer.onNext("test");
		observer.next.assertAuto("test");
	}

	@Test
	public void shouldSetCompleted() {
		var observer = TestStreamObserver.<String>of();
		observer.onCompleted();
		observer.completed.awaitAuto();
	}

	@Test
	public void shouldSetError() {
		var observer = TestStreamObserver.<String>of();
		observer.onError(new IOException("test"));
		Assert.throwable(observer.error.awaitAuto(), IOException.class);
	}

	@Test
	public void shouldReset() {
		var observer = TestStreamObserver.<String>of();
		observer.onNext("test");
		observer.onCompleted();
		observer.onError(new IOException());
		observer.reset();
		observer.next.assertCalls(0);
		observer.completed.assertCalls(0);
		observer.error.assertCalls(0);
	}

	// @Test
	// public void shouldAwaitNext() throws InterruptedException {
	// TestObserver<String> observer = TestObserver.of();
	// try (var exec = SimpleExecutor.run(() -> observer.onNext("test"))) {
	// assertEquals(observer.next.await(), "test");
	// }
	// }
	//
	// @Test
	// public void shouldAwaitCompletion() throws InterruptedException {
	// TestObserver<String> observer = TestObserver.of();
	// try (var exec = SimpleExecutor.run(observer::onCompleted)) {
	// observer.completed.await();
	// }
	// }
	//
	// @Test
	// public void shouldAwaitError() throws InterruptedException {
	// TestObserver<String> observer = TestObserver.of();
	// IOException iox = new IOException("iox");
	// try (var exec = SimpleExecutor.run(() -> observer.onError(iox))) {
	// assertEquals(observer.error.await(), iox);
	// }
	// }
	//
	// @Test
	// public void shouldClearConditions() {
	// TestObserver<String> observer = TestObserver.of();
	// observer.onNext("test");
	// observer.onCompleted();
	// observer.onError(new IOException("iox"));
	// observer.clear();
	// assertNull(observer.next.value());
	// Assert.no(observer.completed.isSet());
	// assertNull(observer.error.value());
	// }
}
