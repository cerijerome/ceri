package ceri.common.reflect;

import org.junit.Test;
import ceri.common.test.Assert;

public class ClassReloaderBehavior {

	@Test
	public void shouldDelegateLoading() throws ClassNotFoundException {
		var cl = ClassReloader.of();
		cl.loadClass(String.class.getName());
	}

	@Test
	public void shouldFailToFindDelegatedClass() {
		var cl = ClassReloader.of();
		Assert.thrown(() -> cl.findClass(String.class.getName()));
	}

	@Test
	public void shouldFailToReloadInvalidClass() {
		var cl = ClassReloader.of(Nested.class);
		Assert.thrown(() -> cl.findClass("$"));
	}

	@Test
	public void shouldReloadClass() {
		var c1 = ClassReloader.reload(Nested.class);
		var c2 = ClassReloader.reload(Nested.class);
		Assert.notSame(c1, c2);
	}

	@Test
	public void shouldReloadOnceOnly() throws ClassNotFoundException {
		var cl = ClassReloader.of(Nested.class);
		var c0 = Nested.class;
		var c1 = cl.loadClass(Nested.class.getName());
		var c2 = cl.loadClass(Nested.class.getName());
		Assert.notSame(c0, c1);
		Assert.same(c1, c2);
	}

	@Test
	public void shouldOnlyReloadSpecifiedClasses() {
		var cl = ClassReloader.of(Nested.class);
		var c0 = Nested.Nested2.class;
		var c1 = cl.forName(Nested.Nested2.class, false);
		var c2 = cl.forName(Nested.Nested2.class, true);
		Assert.same(c0, c1);
		Assert.same(c1, c2);
	}

	@Test
	public void shouldReloadNested() {
		var cl = ClassReloader.ofNested(Nested.class);
		var c0 = Nested.Nested2.class;
		var c1 = cl.forName(Nested.Nested2.class, false);
		var c2 = cl.forName(Nested.Nested2.class, true);
		Assert.notSame(c0, c1);
		Assert.same(c1, c2);
	}

	private static class Nested {
		private static class Nested2 {}
	}
}
