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
	public void shouldReloadOnceOnly() throws ClassNotFoundException {
		var cl = ClassReloader.of(Nested.class);
		var c0 = Nested.class;
		var c1 = cl.loadClass(Nested.class.getName());
		var c2 = cl.loadClass(Nested.class.getName());
		assertNotSame(c0, c1);
		assertSame(c1, c2);
	}

	@Test
	public void shouldOnlyReloadSpecifiedClasses() {
		var cl = ClassReloader.of(Nested.class);
		var c0 = Nested.Nested2.class;
		var c1 = cl.forName(Nested.Nested2.class, false);
		var c2 = cl.forName(Nested.Nested2.class, true);
		assertSame(c0, c1);
		assertSame(c1, c2);
	}

	@Test
	public void shouldReloadNested() {
		var cl = ClassReloader.ofNested(Nested.class);
		var c0 = Nested.Nested2.class;
		var c1 = cl.forName(Nested.Nested2.class, false);
		var c2 = cl.forName(Nested.Nested2.class, true);
		assertNotSame(c0, c1);
		assertSame(c1, c2);
	}

	private static class Nested {
		private static class Nested2 {}
	}
}
