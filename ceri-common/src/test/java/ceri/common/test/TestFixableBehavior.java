package ceri.common.test;

import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertMatch;
import java.io.IOException;
import org.junit.Test;

public class TestFixableBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideName() {
		assertMatch(TestFixable.of().name(), TestFixable.class.getSimpleName() + "@.*");
		assertMatch(new TestFixable("test").name(), "test");
	}

	@Test
	public void shouldProvideStringRepresentation() throws IOException {
		try (var fixable = TestFixable.of()) {
			assertFind(fixable, "fixed,closed");
			fixable.open();
			assertFind(fixable, "fixed,open");
			fixable.broken();
			assertFind(fixable, "broken,closed");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldOpenOnCreation() throws IOException {
		TestFixable.ofOpen().open.assertAuto(true);
	}

}
