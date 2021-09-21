package ceri.serial.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Callback;
import ceri.common.test.Captor;

public class CallbackRegistryBehavior {

	public static interface TestCallback extends Callback {
		public static final CallbackRegistry<TestCallback> registry = CallbackRegistry.of();

		void invoke(String s, int i);

		public static TestCallback register(int n, TestCallback cb) {
			return registry.register(n, id -> (s, i) -> {
				registry.remove(id);
				cb.invoke(s, i);
			});
		}
	}

	@After
	public void after() {
		TestCallback.registry.clear();
	}

	@Test
	public void shouldUnregisterOnCallbackCompletion() {
		Captor<List<?>> captor = Captor.of();
		assertEquals(TestCallback.registry.size(), 0);
		var cb = TestCallback.register(1, (s, i) -> captor.accept(List.of(s, i)));
		assertEquals(TestCallback.registry.size(), 1);
		call(cb, "test1", 100);
		assertEquals(TestCallback.registry.size(), 0);
		call(cb, "test2", 200);
		assertEquals(TestCallback.registry.size(), 0);
		captor.verify(List.of("test1", 100), List.of("test2", 200));
	}

	@Test
	public void shouldUnregisterOnZeroRefs() {
		Captor<List<?>> captor = Captor.of();
		var cb = TestCallback.register(3, (s, i) -> captor.accept(List.of(s, i)));
		call(cb, "test1", 100);
		call(cb, "test2", 200);
		assertEquals(TestCallback.registry.size(), 1);
		call(cb, "test2", 200);
		assertEquals(TestCallback.registry.size(), 0);
	}

	@Test
	public void shouldUnregisterOnDemand() {
		Captor<List<?>> captor = Captor.of();
		assertEquals(TestCallback.registry.size(), 0);
		var cb = TestCallback.registry.register(id -> (s, i) -> {
			if (i == 0) TestCallback.registry.remove(id);
			captor.accept(List.of(s, i));
		});
		assertEquals(TestCallback.registry.size(), 1);
		call(cb, "test1", 100);
		assertEquals(TestCallback.registry.size(), 1);
		call(cb, "test2", 0);
		assertEquals(TestCallback.registry.size(), 0);
		captor.verify(List.of("test1", 100), List.of("test2", 0));
	}

	private static void call(TestCallback cb, String s, int i) {
		cb.invoke(s, i);
	}

}
