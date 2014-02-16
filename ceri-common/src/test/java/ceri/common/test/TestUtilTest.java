package ceri.common.test;

import static ceri.common.test.TestUtil.assertElements;
import static ceri.common.test.TestUtil.assertRange;
import static ceri.common.test.TestUtil.isObject;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;
import ceri.common.util.StringUtil;

public class TestUtilTest {

	static class Uncreatable {
		private Uncreatable() {
			throw new RuntimeException();
		}
	}

	@Test
	public void testAssertPrivateConstructor() {
		TestUtil.assertPrivateConstructor(TestUtil.class);
		try {
			TestUtil.assertPrivateConstructor(TestUtilTest.class);
		} catch (AssertionError e) {}
		try {
			TestUtil.assertPrivateConstructor(TestUtilTest.Uncreatable.class);
		} catch (RuntimeException e) {}
	}

	public static class ExecTest {
		@Test
		public void shouldDoThis() {}
		@Test
		public void testThat() {}
	}

	@Test
	public void testExec() {
		StringBuilder b = new StringBuilder();
		TestUtil.exec(StringUtil.asPrintStream(b), ExecTest.class);
		assertTrue(b.toString().contains("Exec should do this"));
		assertTrue(b.toString().contains("Exec test that"));
	}

	@Test
	public void testAssertException() {
		TestUtil.assertException(new TestRunnable() {
			@Override
			public void run() {
				throw new IllegalArgumentException();
			}
		});
		try {
			TestUtil.assertException(new Runnable() {
				@Override
				public void run() {}
			});
		} catch (AssertionError e) {}
		try {
			TestUtil.assertException(RuntimeException.class, new TestRunnable() {
				@Override
				public void run() throws IOException {
					throw new IOException();
				}
			});
		} catch (AssertionError e) {}
	}

	@Test
	public void testAssertRange() {
		try {
			TestUtil.assertRange(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE - 1);
			fail();
		} catch (AssertionError e) {}
		try {
			TestUtil.assertRange(Long.MIN_VALUE, Long.MIN_VALUE + 1, Long.MAX_VALUE - 1);
			fail();
		} catch (AssertionError e) {}
	}

	@Test
	public void testAssertCollection() {
		List<Integer> list = new ArrayList<>();
		Collections.addAll(list, 5, 1, 4, 2, 3);
		TestUtil.assertCollection(list, 1, 2, 3, 4, 5);
		try {
			TestUtil.assertCollection(list, 1, 2, 4, 5);
			fail();
		} catch (AssertionError e) {}
		try {
			TestUtil.assertCollection(list, 1, 2, 3, 4, 5, 6);
			fail();
		} catch (AssertionError e) {}
	}

	@Test
	public void testToReadableString() {
		byte[] bytes = { 0, 'a', '.', Byte.MAX_VALUE, Byte.MIN_VALUE, '~', '!', -1 };
		assertThat(TestUtil.toReadableString(bytes), is("?a.??~!?"));
		try {
			TestUtil.toReadableString(bytes, 3, 2, "test", '?');
			fail();
		} catch (IllegalArgumentException e) {}
	}

	@Test
	public void testRandomString() {
		String r = TestUtil.randomString(100);
		assertThat(r.length(), is(100));
		for (int i = 0; i < r.length(); i++) {
			char c = r.charAt(i);
			assertRange(c, ' ', '~');
		}
	}

	@Test
	public void testIsObject() {
		assertThat(new Integer(1), isObject(1));
		assertThat(1, isObject(new Integer(1)));
		assertThat(new Integer(1), not(isObject(1L)));
	}

	@Test
	public void testToUnixFromFile() throws IOException {
		try (FileTestHelper helper =
			FileTestHelper.builder().dir("c").file("b/b.txt", "bb").file("a/b/c.txt", "ccc")
				.build()) {
			List<String> unixPaths = TestUtil.toUnixFromFile(helper.fileList("c", "a/b/c.txt"));
			assertTrue(unixPaths.get(0).endsWith("/c"));
			assertTrue(unixPaths.get(1).endsWith("/a/b/c.txt"));
		}
	}

	@Test
	public void testToUnixFromPath() throws IOException {
		try (FileTestHelper helper =
			FileTestHelper.builder().dir("c").file("b/b.txt", "bb").file("a/b/c.txt", "ccc")
				.build()) {
			List<String> paths =
				Arrays.asList(helper.file("c").getPath(), helper.file("a/b/c.txt").getPath());
			List<String> unixPaths = TestUtil.toUnixFromPath(paths);
			assertTrue(unixPaths.get(0).endsWith("/c"));
			assertTrue(unixPaths.get(1).endsWith("/a/b/c.txt"));
		}
	}

	@Test
	public void testAssertElements() {
		final Set<Integer> set = new TreeSet<>();
		assertElements(set);
		Collections.addAll(set, Integer.MAX_VALUE, Integer.MIN_VALUE, 0);
		assertElements(set, Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
	}

	@Test
	public void testAssertElementsForArrays() {
		Integer[] array = { Integer.MAX_VALUE, Integer.MIN_VALUE, 0 };
		assertElements(array, Integer.MAX_VALUE, Integer.MIN_VALUE, 0);
	}

}
