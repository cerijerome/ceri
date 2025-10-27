package ceri.log.concurrent;

import org.apache.logging.log4j.Level;
import org.junit.Test;
import ceri.common.concurrent.Concurrent;
import ceri.common.concurrent.ValueCondition;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.ErrorGen;
import ceri.log.test.LogModifier;

public class DispatcherBehavior {

	@Test
	public void shouldDispatchToListeners() throws InterruptedException {
		var sync = ValueCondition.<String>of();
		try (var disp = Dispatcher.<String>direct(0)) {
			try (var _ = disp.listen(sync::signal)) {
				disp.dispatch("test");
				Assert.equal(sync.await(), "test");
				Concurrent.delay(1); // to cover null queue poll
			}
			disp.dispatch("test");
			Concurrent.delay(1); // to cover dispatch with no listeners
		}
	}

	@Test
	public void shouldAdaptTypeForListeners() throws InterruptedException {
		var sync = ValueCondition.<Integer>of();
		Functions.Function<String, Functions.Consumer<Functions.Consumer<Integer>>> adapter =
			s -> l -> l.accept(s.length());
		try (var disp = Dispatcher.of(0, adapter)) {
			try (var _ = disp.listen(sync::signal)) {
				disp.dispatch("test");
				Assert.equal(sync.await(), 4);
			}
		}
	}

	@Test
	public void shouldFailToDispatchOnListenerError() throws InterruptedException {
		var sync = ValueCondition.<String>of();
		var x = new RuntimeException("generated");
		LogModifier.run(() -> {
			try (var disp = Dispatcher.<String>direct(0)) {
				try (var _ = disp.listen(s -> {
					sync.signal(s); // signal, then generate error
					Assert.throwIt(x);
				})) {
					disp.dispatch("test");
					Assert.equal(sync.await(), "test");
					disp.dispatch("test");
					Assert.equal(sync.await(), "test");
				}
				try (var _ = disp.listen(sync::signal)) {
					disp.dispatch("test");
					Assert.equal(sync.await(), "test");
				}
			}
		}, Level.ERROR, Dispatcher.class);
	}

	@Test
	public void shouldCloseOnListenerInterrupt() throws InterruptedException {
		var sync = CallSync.<String>consumer(null, true);
		sync.error.setFrom(ErrorGen.RIX);
		try (var disp = Dispatcher.<String>direct(0)) {
			try (var _ = disp.listen(sync::accept)) {
				disp.dispatch("test");
				sync.assertAuto("test");
			}
			disp.waitUntilStopped();
		}
	}
}
