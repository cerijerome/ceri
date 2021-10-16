package ceri.log.test;

import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import java.util.Properties;
import org.junit.Test;
import ceri.common.function.RuntimeCloseable;

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

		public TestContainer(Properties properties, String id) {
			value = properties.getProperty(id + ".value" + instances++);
		}

		@Override
		public void close() {}
	}

	private static class TestContainerHelper extends ContainerTestHelper {
		public TestContainerHelper() throws IOException {
			super("test-container-helper");
		}

		public TestContainer container(int id) throws IOException {
			return get(idName(id), () -> new TestContainer(properties, idName(id)));
		}
	}

}
