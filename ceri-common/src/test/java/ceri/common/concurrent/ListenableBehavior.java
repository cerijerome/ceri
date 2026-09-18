package ceri.common.concurrent;

import java.util.Set;
import org.junit.Test;
import ceri.common.collect.Sets;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.common.test.Captor;

public class ListenableBehavior {

	@Test
	public void shouldProvideWrapperToUnlistenOnClose() {
		var captor = Captor.of();
		var listeners = new SetListeners<>();
		try (var _ = listeners.enclose(captor)) {
			listeners.signal("test0");
			try (var _ = listeners.enclose(captor)) {
				listeners.signal("test1");
			}
			listeners.signal("test2");
		}
		listeners.signal("test3");
		captor.verify("test0", "test1", "test2");
	}

	@Test
	public void shouldProvideIndirectAccess() {
		var captor = Captor.of();
		var listeners = Listeners.of();
		listeners.indirect().listeners().listen(captor);
		listeners.accept("test");
		captor.verify("test");
	}

	@Test
	public void shouldFilterEvents() {
		var captor = Captor.<String>of();
		var filterCaptor = Captor.<String>of();
		var listeners = Listeners.<String>of();
		var filtered = Listenable.filter(listeners, s -> s.length() <= 3);
		listeners.listen(captor);
		filtered.listen(filterCaptor);
		listeners.acceptAll("a", "bbbb", "ccc");
		captor.verify("a", "bbbb", "ccc");
		filterCaptor.verify("a", "ccc");
	}

	@Test
	public void shouldUnlistenFilteredEvents() {
		var captor = Captor.<String>of();
		var filterCaptor = Captor.<String>of();
		var listeners = Listeners.<String>of();
		var filtered = Listenable.filter(listeners, s -> s.length() <= 3);
		listeners.listen(captor);
		filtered.listen(filterCaptor);
		filtered.unlisten(captor); // does nothing
		filtered.unlisten(filterCaptor);
		listeners.acceptAll("a", "bbbb", "ccc");
		captor.verify("a", "bbbb", "ccc");
		filterCaptor.verify();
	}

	@Test
	public void shouldProvideNullListener() {
		var captor = Captor.of();
		var listeners = Listenable.ofNull();
		listeners.listen(captor);
		listeners.unlisten(captor);
		var indirect = Listenable.ofNull();
		indirect.listeners().listen(captor);
		indirect.listeners().unlisten(captor);
		captor.verify();
	}

	@Test
	public void shouldProvideSafeAccess() {
		var listen = Listeners.<String>of();
		Functions.Consumer<String> consumer = _ -> {};
		listen.listen(consumer);
		Listenable.safe((Listenable<String>) null).unlisten(consumer);
		Listenable.safe((Listenable.Indirect<String>) null).listeners().unlisten(consumer);
		Assert.equal(Listenable.safe(listen).unlisten(consumer), true);
	}

	@Test
	public void shouldProvideWrapperToUnlistenIntOnClose() {
		var captor = Captor.ofInt();
		var listeners = new SetIntListeners();
		try (var _ = listeners.enclose(captor)) {
			listeners.signal(0);
			try (var _ = listeners.enclose(captor)) {
				listeners.signal(1);
			}
			listeners.signal(2);
		}
		listeners.signal(3);
		captor.verify(0, 1, 2);
	}

	@Test
	public void shouldProvideIndirectIntAccess() {
		var captor = Captor.ofInt();
		var listeners = Listeners.ofInt();
		listeners.indirect().listeners().listen(captor);
		listeners.accept(0);
		captor.verifyInt(0);
	}

	@Test
	public void shouldProvideNullIntListener() {
		var captor = Captor.ofInt();
		var listeners = Listenable.OfInt.NULL;
		listeners.listen(captor);
		listeners.unlisten(captor);
		var indirect = Listenable.OfInt.NULL;
		indirect.listeners().listen(captor);
		indirect.listeners().unlisten(captor);
		captor.verifyInt();
	}

	private static class SetListeners<T> implements Listenable<T> {
		private final Set<Functions.Consumer<? super T>> listeners = Sets.of();

		@Override
		public boolean listen(Functions.Consumer<? super T> listener) {
			return listeners.add(listener);
		}

		@Override
		public boolean unlisten(Functions.Consumer<? super T> listener) {
			return listeners.remove(listener);
		}

		public void signal(T event) {
			listeners.forEach(l -> l.accept(event));
		}
	}

	private static class SetIntListeners implements Listenable.OfInt {
		private final Set<Functions.IntConsumer> listeners = Sets.of();

		@Override
		public boolean listen(Functions.IntConsumer listener) {
			return listeners.add(listener);
		}

		@Override
		public boolean unlisten(Functions.IntConsumer listener) {
			return listeners.remove(listener);
		}

		public void signal(int event) {
			listeners.forEach(l -> l.accept(event));
		}
	}
}
