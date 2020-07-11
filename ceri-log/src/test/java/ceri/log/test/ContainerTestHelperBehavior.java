package ceri.log.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;
import org.junit.Test;

public class ContainerTestHelperBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldCreateContainerOnce() throws IOException {
		try (var helper = new TestContainerHelper()) {
			assertThat(helper.container(1).value, is("def"));
			assertThat(helper.container(0).value, is("abc"));
			assertThat(helper.container(1).value, is("def"));
			assertThat(helper.container(0).value, is("abc"));
			assertThat(TestContainer.instances, is(2));
		}
	}

	private static class TestContainer implements Closeable {
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
