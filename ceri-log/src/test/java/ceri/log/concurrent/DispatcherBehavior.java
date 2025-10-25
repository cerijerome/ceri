package ceri.log.concurrent;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.throwIt;
import static ceri.common.test.ErrorGen.RIX;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import ceri.common.concurrent.Concurrent;
import ceri.common.concurrent.ValueCondition;
import ceri.common.function.Functions;
import ceri.common.test.CallSync;
import ceri.log.test.LogModifier;

public class DispatcherBehavior {

	@Test
	public void shouldDispatchToListeners() throws InterruptedException {
		ValueCondition<String> sync = ValueCondition.of();
		try (var disp = Dispatcher.<String>direct(0)) {
			try (var _ = disp.listen(sync::signal)) {
				disp.dispatch("test");
				assertEquals(sync.await(), "test");
				Concurrent.delay(1); // to cover null queue poll
			}
			disp.dispatch("test");
			Concurrent.delay(1); // to cover dispatch with no listeners
		}
	}

	@Test
	public void shouldAdaptTypeForListeners() throws InterruptedException {
		ValueCondition<Integer> sync = ValueCondition.of();
		Functions.Function<String, Functions.Consumer<Functions.Consumer<Integer>>> adapter =
			s -> l -> l.accept(s.length());
		try (var disp = Dispatcher.of(0, adapter)) {
			try (var _ = disp.listen(sync::signal)) {
				disp.dispatch("test");
				assertEquals(sync.await(), 4);
			}
		}
	}

	@Test
	public void shouldFailToDispatchOnListenerError() throws InterruptedException {
		ValueCondition<String> sync = ValueCondition.of();
		var x = new RuntimeException("generated");
		LogModifier.run(() -> {
			try (var disp = Dispatcher.<String>direct(0)) {
				try (var _ = disp.listen(s -> {
					sync.signal(s); // signal, then generate error
					throwIt(x);
				})) {
					disp.dispatch("test");
					assertEquals(sync.await(), "test");
					disp.dispatch("test");
					assertEquals(sync.await(), "test");
				}
				try (var _ = disp.listen(sync::signal)) {
					disp.dispatch("test");
					assertEquals(sync.await(), "test");
				}
			}
		}, Level.ERROR, Dispatcher.class);
	}

	@Test
	public void shouldCloseOnListenerInterrupt() throws InterruptedException {
		CallSync.Consumer<String> sync = CallSync.consumer(null, true);
		sync.error.setFrom(RIX);
		try (var disp = Dispatcher.<String>direct(0)) {
			try (var _ = disp.listen(sync::accept)) {
				disp.dispatch("test");
				sync.assertAuto("test");
			}
			disp.waitUntilStopped();
		}
	}

}
