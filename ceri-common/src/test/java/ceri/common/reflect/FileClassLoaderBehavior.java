package ceri.common.reflect;

import org.junit.Test;

public class FileClassLoaderBehavior {

	@Test
	public void shouldDelegateLoading() throws ClassNotFoundException {
		var cl = new FileClassLoader();
		cl.loadClass(String.class.getName());
	}

	@Test
	public void shouldOnlyLoadOnceFromFile() throws ClassNotFoundException {
		var cl = new FileClassLoader(Nested.class);
		cl.loadClass(Nested.class.getName());
		cl.loadClass(Nested.class.getName());
	}

	private static class Nested {}
}
