package ceri.common.io;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import ceri.common.except.ExceptionAdapter;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;

public class NioTest {
	private TestSelector selector;
	private TestKey key0;
	private TestKey key1;
	private TestKey key2;

	@Before
	public void before() {
		selector = new TestSelector(null, Set.of());
		key0 = new TestKey(selector, null).interestOps(1);
		key1 = new TestKey(selector, null).interestOps(2);
		key2 = new TestKey(selector, null).interestOps(4);
	}

	@Test
	public void testSelectKeys() throws IOException, InterruptedException {
		var keys = new TestKeySet(key0, key1, key2);
		selector.selectedKeys.autoResponses(keys);
		var consumer = CallSync.<SelectionKey>consumer(null, true);
		Nio.selectKeys(selector, k -> consumer.accept(k, ExceptionAdapter.io));
		consumer.assertValues(key0, key1, key2);
		Assert.equal(keys.isEmpty(), true);
	}

	@Test
	public void testClearKeys() {
		var keys = new TestKeySet(key0, key1, key2);
		Nio.clearKeys(keys);
	}

	@SuppressWarnings("serial")
	public static class TestKeySet extends LinkedHashSet<SelectionKey> {

		@SafeVarargs
		public TestKeySet(SelectionKey... keys) {
			for (var key : keys)
				add(key);
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}
	}

	public static class TestKey extends SelectionKey {
		private final Selector selector;
		private final SelectableChannel channel;
		public final CallSync.Supplier<Integer> readyOps = CallSync.supplier(0);
		private int interestOps = 0;

		public TestKey(Selector selector, SelectableChannel channel) {
			this.selector = selector;
			this.channel = channel;
		}

		@Override
		public void cancel() {}

		@Override
		public SelectableChannel channel() {
			return channel;
		}

		@Override
		public int interestOps() {
			return interestOps;
		}

		@Override
		public TestKey interestOps(int ops) {
			this.interestOps = ops;
			return this;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public int readyOps() {
			return readyOps.get();
		}

		@Override
		public Selector selector() {
			return selector;
		}
	}

	public static class TestSelector extends Selector {
		private final SelectorProvider provider;
		private final Set<SelectionKey> keys;
		public final CallSync.Runnable wakeUp = CallSync.runnable(true);
		public final CallSync.Function<Long, Integer> select = CallSync.function(0L, 0);
		public final CallSync.Supplier<Integer> selectNow = CallSync.supplier(0);
		public final CallSync.Supplier<Set<SelectionKey>> selectedKeys =
			CallSync.supplier(Set.of());
		public boolean open = true;

		public TestSelector(SelectorProvider provider, Set<SelectionKey> keys) {
			this.provider = provider;
			this.keys = keys;
		}

		@Override
		public int select() throws IOException {
			return select.apply(0L, ExceptionAdapter.io);
		}

		@Override
		public int select(long timeout) throws IOException {
			return select.apply(timeout, ExceptionAdapter.io);
		}

		@Override
		public int selectNow() throws IOException {
			return selectNow.get(ExceptionAdapter.io);
		}

		@Override
		public Set<SelectionKey> selectedKeys() {
			return selectedKeys.get();
		}

		@Override
		public Set<SelectionKey> keys() {
			return keys;
		}

		@Override
		public SelectorProvider provider() {
			return provider;
		}

		@Override
		public TestSelector wakeup() {
			wakeUp.run();
			return this;
		}

		@Override
		public boolean isOpen() {
			return open;
		}

		@Override
		public void close() throws IOException {
			open = false;
		}
	}
}
