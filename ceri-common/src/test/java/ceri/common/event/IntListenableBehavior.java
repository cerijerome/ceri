package ceri.common.event;

import static ceri.common.test.AssertUtil.assertTrue;
import java.util.HashSet;
import java.util.Set;
import java.util.function.IntConsumer;
import org.junit.Test;
import ceri.common.function.Enclosure;
import ceri.common.function.Functions;
import ceri.common.test.Captor;

public class IntListenableBehavior {

	@Test
	public void shouldProvideWrapperToUnlistenOnClose() {
		Captor.OfInt captor = Captor.ofInt();
		IntListeners listeners = IntListeners.of();
		try (var _ = listeners.enclose(captor)) {
			listeners.accept(0);
		}
		listeners.accept(1);
		captor.verifyInt(0);
	}

	@Test
	public void shouldUnlistenCloseWrappedListenerOnceOnly() {
		Captor.OfInt captor = Captor.ofInt();
		TestListenable listeners = new TestListenable();
		try (var _ = listeners.enclose(captor)) {
			listeners.accept(0);
			try (Enclosure<?> enclosed = listeners.enclose(captor)) {
				listeners.accept(1);
				assertTrue(enclosed.isNoOp());
			}
			listeners.accept(2);
		}
		listeners.accept(3);
		captor.verifyInt(0, 1, 2);
	}

	@Test
	public void shouldProvideIndirectAccess() {
		Captor.OfInt captor = Captor.ofInt();
		IntListeners listeners = IntListeners.of();
		listeners.indirect().listeners().listen(captor);
		listeners.accept(0);
		captor.verifyInt(0);
	}

	@Test
	public void shouldProvideNullListener() {
		Captor.OfInt captor = Captor.ofInt();
		IntListenable listeners = IntListenable.ofNull();
		listeners.listen(captor);
		listeners.unlisten(captor);
		IntListenable.Indirect indirect = IntListenable.ofNull();
		indirect.listeners().listen(captor);
		indirect.listeners().unlisten(captor);
		captor.verifyInt();
	}

	private static class TestListenable implements IntConsumer, IntListenable {
		private final Set<IntConsumer> listeners = new HashSet<>();

		@Override
		public boolean listen(Functions.IntConsumer listener) {
			return listeners.add(listener);
		}

		@Override
		public boolean unlisten(Functions.IntConsumer listener) {
			return listeners.remove(listener);
		}

		@Override
		public void accept(int i) {
			listeners.forEach(l -> l.accept(i));
		}
	}
}
