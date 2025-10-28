package ceri.common.event;

import org.junit.After;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.Testing;

public class EventThreadBehavior {
	private EventThread<Object> et;

	@After
	public void after() {
		et = Testing.close(et);
	}

	@Test
	public void shouldPropagateEvent() {
		var consumer = CallSync.consumer(null, false);
		et = EventThread.of();
		et.listeners().listen(consumer);
		et.accept("test1");
		et.accept("test2");
		et.accept("test3");
		consumer.assertCall("test1");
		consumer.assertCall("test2");
		consumer.assertCall("test3");
	}

	@Test
	public void shouldNotifyOnException() {
		var exceptionConsumer = CallSync.<Exception>consumer(null, false);
		var exception = new RuntimeException("test");
		et = EventThread.of(exceptionConsumer);
		et.listeners().listen(_ -> Assert.throwIt(exception));
		et.accept("test");
		exceptionConsumer.assertCall(exception);
	}

	@Test
	public void shouldOptionallyIgnoreExceptions() {
		var consumer = CallSync.consumer(null, false);
		var exception = new RuntimeException("test");
		consumer.error.set(exception);
		et = EventThread.of();
		et.listeners().listen(consumer);
		et.accept("test");
		consumer.assertCall("test");
	}
}
