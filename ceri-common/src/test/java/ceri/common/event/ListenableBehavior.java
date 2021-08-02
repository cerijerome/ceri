package ceri.common.event;

import static ceri.common.test.AssertUtil.assertTrue;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.Test;
import ceri.common.test.Captor;
import ceri.common.util.Enclosed;

public class ListenableBehavior {

	@Test
	public void shouldProvideWrapperToUnlistenOnClose() {
		Captor<String> captor = Captor.of();
		Listeners<String> listeners = Listeners.of();
		try (Enclosed<RuntimeException, ?> enclosed = listeners.enclose(captor)) {
			listeners.accept("test0");
		}
		listeners.accept("test1");
		captor.verify("test0");
	}

	@Test
	public void shouldUnlistenCloseWrappedListenerOnceOnly() {
		Captor<String> captor = Captor.of();
		TestListenable listeners = new TestListenable();
		try (Enclosed<RuntimeException, ?> enclosed0 = listeners.enclose(captor)) {
			listeners.accept("test0");
			try (Enclosed<RuntimeException, ?> enclosed1 = listeners.enclose(captor)) {
				listeners.accept("test1");
				assertTrue(enclosed1.isNoOp());
			}
			listeners.accept("test2");
		}
		listeners.accept("test3");
		captor.verify("test0", "test1", "test2");
	}

	@Test
	public void shouldProvideIndirectAccess() {
		Captor<String> captor = Captor.of();
		Listeners<String> listeners = Listeners.of();
		listeners.indirect().listeners().listen(captor);
		listeners.accept("test");
		captor.verify("test");
	}

	@Test
	public void shouldProvideNullListener() {
		Captor<String> captor = Captor.of();
		Listenable<String> listeners = Listenable.ofNull();
		listeners.listen(captor);
		listeners.unlisten(captor);
		Listenable.Indirect<String> indirect = Listenable.ofNull();
		indirect.listeners().listen(captor);
		indirect.listeners().unlisten(captor);
		captor.verify();
	}

	private static class TestListenable implements Consumer<String>, Listenable<String> {
		private final Set<Consumer<? super String>> listeners = new HashSet<>();

		@Override
		public boolean listen(Consumer<? super String> listener) {
			return listeners.add(listener);
		}

		@Override
		public boolean unlisten(Consumer<? super String> listener) {
			return listeners.remove(listener);
		}

		@Override
		public void accept(String s) {
			listeners.forEach(l -> l.accept(s));
		}
	}
}
