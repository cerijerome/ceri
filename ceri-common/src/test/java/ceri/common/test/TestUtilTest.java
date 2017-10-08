package ceri.common.test;

import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertFile;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertRange;
import static ceri.common.test.TestUtil.isList;
import static ceri.common.test.TestUtil.isObject;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;
import ceri.common.concurrent.ExceptionRunnable;
import ceri.common.text.StringUtil;
import ceri.common.util.HAlign;

public class TestUtilTest {

	static class Uncreatable {
		private Uncreatable() {
			throw new RuntimeException();
		}
	}

	static enum BadEnum {
		bad;

		BadEnum() {
			throw new RuntimeException();
		}
	}

	@Test
	public void testExerciseEnums() {
		TestUtil.exerciseEnum(HAlign.class);
		assertException(() -> TestUtil.exerciseEnum(BadEnum.class));
	}

	@Test
	public void testApprox() {
		assertAssertion(() -> TestUtil.assertApprox(Double.POSITIVE_INFINITY, Double.MAX_VALUE));
		TestUtil.assertApprox(0.000995, 0.00099499, 4);
		assertAssertion(() -> TestUtil.assertApprox(0.000995, 0.00099499, 5));
		double[] dd = { 1.0015, -0.00501, 0 };
		assertAssertion(() -> TestUtil.assertApprox(dd, 1.0015, -0.00502, 0));
	}

	@Test
	public void testReadString() throws IOException {
		InputStream stdin = System.in;
		try (InputStream in = new ByteArrayInputStream("test".getBytes())) {
			System.setIn(in);
			assertThat(TestUtil.readString(), is("test"));
			assertThat(TestUtil.readString(), is(""));
		} finally {
			System.setIn(stdin);
		}
	}

	@Test
	public void testIsList() {
		List<Integer> list = Arrays.asList(1, 2, 3);
		assertThat(list, isList(1, 2, 3));
	}

	@Test
	public void testAssertFile() throws IOException {
		try (FileTestHelper helper =
			FileTestHelper.builder().dir("a").dir("b").file("c", "").file("d", "D").build()) {
			assertAssertion(() -> assertFile(helper.file("a"), helper.file("c")));
			assertAssertion(() -> assertFile(helper.file("c"), helper.file("a")));
			assertFile(helper.file("c"), helper.file("c"));
			assertAssertion(() -> assertFile(helper.file("c"), helper.file("d")));
			assertAssertion(() -> assertFile(helper.file("d"), helper.file("c")));
			assertFile(helper.file("d"), helper.file("d"));
			assertException(() -> assertFile(helper.file("e"), helper.file("e")));
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
		PrintStream sysOut = System.out;
		StringBuilder b = new StringBuilder();
		System.setOut(StringUtil.asPrintStream(b));
		TestUtil.exec(ExecTest.class);
		assertTrue(b.toString().contains("Exec should do this"));
		assertTrue(b.toString().contains("Exec test that"));
		System.setOut(sysOut);
	}

	@Test
	public void testAssertException() {
		assertAssertion(() -> TestUtil.assertException(() -> {}));
		assertAssertion(() -> TestUtil.assertException(IllegalArgumentException.class, () -> {
			throw new RuntimeException();
		}));
		TestUtil.assertException(IllegalArgumentException.class, () -> {
			throw new IllegalArgumentException();
		});
		assertAssertion(() -> TestUtil.assertException(IllegalArgumentException.class, () -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void testAssertRange() {
		assertAssertion(
			() -> TestUtil.assertRange(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE - 1));
		assertAssertion(
			() -> TestUtil.assertRange(Long.MIN_VALUE, Long.MIN_VALUE + 1, Long.MAX_VALUE - 1));
	}

	@Test
	public void testAssertArray() {
		boolean[] b0 = { false, false };
		boolean[] b1 = { false, false };
		assertAssertion(() -> TestUtil.assertArrayObject(b0, 0, b1, 0, 3));
	}

	@Test
	public void testAssertCollection() {
		List<Integer> list = new ArrayList<>();
		Collections.addAll(list, 5, 1, 4, 2, 3);
		TestUtil.assertCollection(list, 1, 2, 3, 4, 5);
		assertAssertion(() -> TestUtil.assertCollection(list, 1, 2, 4, 5));
		assertAssertion(() -> TestUtil.assertCollection(list, 1, 2, 3, 4, 5, 6));
		assertAssertion(
			() -> TestUtil.assertCollection(new boolean[] { true, false }, true, false));
		assertAssertion(() -> TestUtil.assertCollection(new byte[] { -1, 1 }, -1, 1));
		assertAssertion(() -> TestUtil.assertCollection(new char[] { 'a', 'b' }, 'a', 'b'));
		assertAssertion(
			() -> TestUtil.assertCollection(new short[] { -1, 1 }, (short) -1, (short) 1));
		assertAssertion(() -> TestUtil.assertCollection(new int[] { -1, 1 }, -1, 1));
		assertAssertion(() -> TestUtil.assertCollection(new long[] { -1, 1 }, -1, 1));
		assertAssertion(() -> TestUtil.assertCollection(new float[] { -1, 1 }, -1, 1));
	}

	@Test
	public void testToReadableString() {
		byte[] bytes = { 0, 'a', '.', Byte.MAX_VALUE, Byte.MIN_VALUE, '~', '!', -1 };
		assertThat(TestUtil.toReadableString(bytes), is("?a.??~!?"));
		assertException(IllegalArgumentException.class,
			() -> TestUtil.toReadableString(bytes, 3, 2, "test", '?'));
		assertThat(TestUtil.toReadableString(new byte[0], 0, 0, null, '.'), is(""));
		assertThat(TestUtil.toReadableString(new byte[0], 0, 0, "", '.'), is(""));
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
		try (FileTestHelper helper = FileTestHelper.builder().dir("c").file("b/b.txt", "bb")
			.file("a/b/c.txt", "ccc").build()) {
			List<String> unixPaths = TestUtil.toUnixFromFile(helper.fileList("c", "a/b/c.txt"));
			assertTrue(unixPaths.get(0).endsWith("/c"));
			assertTrue(unixPaths.get(1).endsWith("/a/b/c.txt"));
		}
	}

	@Test
	public void testToUnixFromPath() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().dir("c").file("b/b.txt", "bb")
			.file("a/b/c.txt", "ccc").build()) {
			List<String> paths =
				Arrays.asList(helper.file("c").getPath(), helper.file("a/b/c.txt").getPath());
			List<String> unixPaths = TestUtil.toUnixFromPath(paths);
			assertTrue(unixPaths.get(0).endsWith("/c"));
			assertTrue(unixPaths.get(1).endsWith("/a/b/c.txt"));
		}
	}

	@Test
	public void testAssertIterable() {
		final Set<Integer> set = new TreeSet<>();
		assertIterable(set);
		Collections.addAll(set, Integer.MAX_VALUE, Integer.MIN_VALUE, 0);
		assertIterable(set, Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
	}

	private void assertAssertion(ExceptionRunnable<Exception> runnable) {
		try {
			runnable.run();
			fail();
		} catch (Exception e) {
			fail();
		} catch (AssertionError e) {
			// Success
		}
	}

}
