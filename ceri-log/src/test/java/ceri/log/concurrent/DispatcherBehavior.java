package ceri.log.concurrent;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.throwIt;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.concurrent.ValueCondition;
import ceri.log.test.LogModifier;

public class DispatcherBehavior {

	@Test
	public void shouldDispatchToListeners() throws InterruptedException {
		ValueCondition<String> sync = ValueCondition.of();
		try (var disp = Dispatcher.<String>direct(0)) {
			try (var enc = disp.listen(sync::signal)) {
				disp.dispatch("test");
				assertEquals(sync.await(), "test");
				ConcurrentUtil.delay(1); // to cover null queue poll
			}
			disp.dispatch("test");
			ConcurrentUtil.delay(1); // to cover dispatch with no listeners
		}
	}

	@Test
	public void shouldAdaptTypeForListeners() throws InterruptedException {
		ValueCondition<Integer> sync = ValueCondition.of();
		Function<String, Consumer<Consumer<Integer>>> adapter = s -> l -> l.accept(s.length());
		try (var disp = Dispatcher.of(0, adapter)) {
			try (var enc = disp.listen(sync::signal)) {
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
				try (var enc = disp.listen(s -> {
					sync.signal(s); // signal, then generate error
					throwIt(x);
				})) {
					disp.dispatch("test");
					assertEquals(sync.await(), "test");
					disp.dispatch("test");
					assertEquals(sync.await(), "test");
				}
				try (var enc = disp.listen(sync::signal)) {
					disp.dispatch("test");
					assertEquals(sync.await(), "test");
				}
			}
		}, Level.ERROR, Dispatcher.class);
	}

	@Test
	public void shouldCloseOnListenerInterrupt() {
		var x = new RuntimeInterruptedException("generated");
		try (var disp = Dispatcher.<String>direct(0)) {
			try (var enc = disp.listen(s -> throwIt(x))) {
				disp.dispatch("test");
			}
		}
	}

}
