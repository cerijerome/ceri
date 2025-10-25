package ceri.log.rpc.test;

import static ceri.common.test.Assert.throwable;
import java.io.IOException;
import org.junit.Test;

public class TestObserverBehavior {

	@Test
	public void shouldSetNext() {
		TestStreamObserver<String> observer = TestStreamObserver.of();
		observer.onNext("test");
		observer.next.assertAuto("test");
	}

	@Test
	public void shouldSetCompleted() {
		TestStreamObserver<String> observer = TestStreamObserver.of();
		observer.onCompleted();
		observer.completed.awaitAuto();
	}

	@Test
	public void shouldSetError() {
		TestStreamObserver<String> observer = TestStreamObserver.of();
		observer.onError(new IOException("test"));
		throwable(observer.error.awaitAuto(), IOException.class);
	}

	@Test
	public void shouldReset() {
		TestStreamObserver<String> observer = TestStreamObserver.of();
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
	// assertFalse(observer.completed.isSet());
	// assertNull(observer.error.value());
	// }

}
