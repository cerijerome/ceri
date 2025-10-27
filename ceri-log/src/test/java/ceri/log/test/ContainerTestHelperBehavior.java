package ceri.log.test;

import java.io.IOException;
import org.junit.Test;
import ceri.common.function.Functions;
import ceri.common.property.TypedProperties;
import ceri.common.test.Assert;

public class ContainerTestHelperBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldCreateContainerOnce() throws IOException {
		try (var helper = new TestContainerHelper()) {
			Assert.equal(helper.container(1).value, "def");
			Assert.equal(helper.container(0).value, "abc");
			Assert.equal(helper.container(1).value, "def");
			Assert.equal(helper.container(0).value, "abc");
			Assert.equal(TestContainer.instances, 2);
		}
	}

	private static class TestContainer implements Functions.Closeable {
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

		public TestContainer container(int id) {
			return get(id, p -> new TestContainer(p));
		}
	}

}
