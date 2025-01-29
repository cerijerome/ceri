package ceri.common.event;

import static ceri.common.test.AssertUtil.throwIt;
import org.junit.Test;
import ceri.common.test.CallSync;

public class EventThreadBehavior {

	@Test
	public void shouldPropagateEvent() {
		var consumer = CallSync.consumer(null, false);
		try (var et = EventThread.of()) {
			et.listeners().listen(consumer);
			et.accept("test1");
			et.accept("test2");
			et.accept("test3");
			consumer.assertCall("test1");
			consumer.assertCall("test2");
			consumer.assertCall("test3");
		}
	}

	@Test
	public void shouldNotifyOnException() {
		var exceptionConsumer = CallSync.<Exception>consumer(null, false);
		var exception = new RuntimeException("test");
		try (var et = EventThread.of(exceptionConsumer)) {
			et.listeners().listen(_ -> throwIt(exception));
			et.accept("test");
			exceptionConsumer.assertCall(exception);
		}
	}

	@Test
	public void shouldOptionallyIgnoreExceptions() {
		var consumer = CallSync.consumer(null, false);
		var exception = new RuntimeException("test");
		consumer.error.set(exception);
		try (var et = EventThread.of()) {
			et.listeners().listen(consumer);
			et.accept("test");
			consumer.assertCall("test");
		}
	}

}
