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

public class TestUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		TestUtil.assertPrivateConstructor(TestUtil.class);
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