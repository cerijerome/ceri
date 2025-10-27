package ceri.common.test;

import java.io.IOException;
import org.junit.Test;

public class TestFixableBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideName() {
		Assert.match(TestFixable.of().name(), TestFixable.class.getSimpleName() + "@.*");
		Assert.match(new TestFixable("test").name(), "test");
	}

	@Test
	public void shouldProvideStringRepresentation() throws IOException {
		try (var fixable = TestFixable.of()) {
			Assert.find(fixable, "fixed,closed");
			fixable.open();
			Assert.find(fixable, "fixed,open");
			fixable.broken();
			Assert.find(fixable, "broken,closed");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldOpenOnCreation() throws IOException {
		TestFixable.ofOpen().open.assertAuto(true);
	}

}
