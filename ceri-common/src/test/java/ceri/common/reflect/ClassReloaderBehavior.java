package ceri.common.reflect;

import static ceri.common.test.AssertUtil.assertNotSame;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;

public class ClassReloaderBehavior {

	@Test
	public void shouldDelegateLoading() throws ClassNotFoundException {
		var cl = ClassReloader.of();
		cl.loadClass(String.class.getName());
	}

	@Test
	public void shouldFailToFindDelegatedClass() {
		var cl = ClassReloader.of();
		assertThrown(() -> cl.findClass(String.class.getName()));
	}

	@Test
	public void shouldFailToReloadInvalidClass() {
		var cl = ClassReloader.of(Nested.class);
		assertThrown(() -> cl.findClass("$"));
	}

	@Test
	public void shouldReloadClass() {
		var c1 = ClassReloader.reload(Nested.class);
		var c2 = ClassReloader.reload(Nested.class);
		assertNotSame(c1, c2);
	}

	@Test
	public void shouldReloadOnceOnly() {
		var cl = ClassReloader.of(Nested.class);
		var c1 = cl.load(Nested.class);
		var c2 = cl.load(Nested.class);
		assertSame(c1, c2);
	}

	private static class Nested {}
}
