package ceri.log.test;

import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.Test;
import ceri.common.function.RuntimeCloseable;
import ceri.common.property.TypedProperties;

public class ContainerTestHelperBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldCreateContainerOnce() throws IOException {
		try (var helper = new TestContainerHelper()) {
			assertEquals(helper.container(1).value, "def");
			assertEquals(helper.container(0).value, "abc");
			assertEquals(helper.container(1).value, "def");
			assertEquals(helper.container(0).value, "abc");
			assertEquals(TestContainer.instances, 2);
		}
	}

	private static class TestContainer implements RuntimeCloseable {
		public static int instances = 0;
		public final String value;

		public TestContainer(TypedProperties properties) {
			value = properties.parse("value" + instances++).get();
		}

		@Override
		public void close() {}
	}

	private static class TestContainerHelper extends ContainerTestHelper {
		public TestContainerHelper() throws IOException {
			super("test-container-helper");
		}

		public TestContainer container(int id) throws IOException {
			return get(id, p -> new TestContainer(p));
		}
	}

}
