package ceri.common.test;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertDir;
import static ceri.common.test.TestUtil.assertFile;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertMap;
import static ceri.common.test.TestUtil.assertNaN;
import static ceri.common.test.TestUtil.assertRange;
import static ceri.common.test.TestUtil.assertThrowable;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.init;
import static ceri.common.test.TestUtil.isList;
import static ceri.common.test.TestUtil.isObject;
import static ceri.common.test.TestUtil.matchesRegex;
import static ceri.common.test.TestUtil.resource;
import static ceri.common.test.TestUtil.testMap;
import static ceri.common.test.TestUtil.thrown;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.function.ExceptionRunnable;
import ceri.common.io.SystemIo;
import ceri.common.text.StringUtil;
import ceri.common.util.Align;

public class TestUtilTest {

	static class Uncreatable {
		private Uncreatable() {
			throw new RuntimeException();
		}
	}

	enum BadEnum {
		bad;

		BadEnum() {
			throw new RuntimeException();
		}
	}

	@Test
	public void testExerciseEnums() {
		TestUtil.exerciseEnum(Align.H.class);
		TestUtil.assertThrown(() -> TestUtil.exerciseEnum(BadEnum.class));
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
	public void testReadString() {
		try (SystemIo sys = SystemIo.of()) {
			sys.in(new ByteArrayInputStream("test".getBytes()));
			assertThat(TestUtil.readString(), is("test"));
			assertThat(TestUtil.readString(), is(""));
		}
	}

	@Test
	public void testReadStringWithBadInputStream() throws IOException {
		try (SystemIo sys = SystemIo.of()) {
			try (InputStream badIn = new InputStream() {
				@Override
				public int read() throws IOException {
					throw new IOException();
				}
			}) {
				sys.in(badIn);
				TestUtil.assertThrown(TestUtil::readString);
			}
		}
	}

	@Test
	public void testIsList() {
		List<Integer> list = Arrays.asList(1, 2, 3);
		assertThat(list, isList(1, 2, 3));
	}

	@Test
	public void testAssertDir() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().root("a") //
			.dir("a/0/d0").file("a/0/f0", "xxxxxx").file("a/0/f1", "") //
			.dir("a/1/d0").file("a/1/f0", "xxxxxx").file("a/1/f1", "") //
			.dir("a/2/d0").file("a/2/f", "xxxxxx").file("a/2/f1", "") //
			.dir("a/3/d0").file("a/3/f0", "").file("a/3/f1", "") //
			.dir("a/4/d0").file("a/4/f0", "xxxxxx").file("a/4/f1", "x") //
			.dir("a/5/d1").file("a/5/f0", "xxxxxx").file("a/5/f1", "") //
			.build()) {
			assertDir(helper.file("a/0"), helper.file("a/1"));
			assertAssertion(() -> assertDir(helper.file("a/0"), helper.file("a/2")));
			assertAssertion(() -> assertDir(helper.file("a/0"), helper.file("a/3")));
			assertAssertion(() -> assertDir(helper.file("a/0"), helper.file("a/4")));
			assertAssertion(() -> assertDir(helper.file("a/0"), helper.file("a/5")));
		}

	}

	@Test
	public void testAssertFile() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().dir("a").dir("b").file("c", "")
			.file("d", "D").file("e", "E").build()) {
			assertAssertion(() -> assertFile(helper.file("a"), helper.file("c")));
			assertAssertion(() -> assertFile(helper.file("c"), helper.file("a")));
			assertFile(helper.file("c"), helper.file("c"));
			assertAssertion(() -> assertFile(helper.file("c"), helper.file("d")));
			assertAssertion(() -> assertFile(helper.file("d"), helper.file("c")));
			assertAssertion(() -> assertFile(helper.file("d"), helper.file("e")));
			assertFile(helper.file("d"), helper.file("d"));
			assertAssertion(() -> assertFile(helper.file("d"), helper.file("e")));
		}
	}

	@Test
	public void testAssertPrivateConstructor() {
		TestUtil.assertPrivateConstructor(TestUtil.class);
		try {
			TestUtil.assertPrivateConstructor(TestUtilTest.class);
		} catch (AssertionError e) {
			// ignore
		}
		try {
			TestUtil.assertPrivateConstructor(TestUtilTest.Uncreatable.class);
		} catch (RuntimeException e) {
			// ignore
		}
	}

	public static class ExecTest {
		@Test
		public void shouldDoThis() {}

		@Test
		public void testThat() {}
	}

	@Test
	public void testExec() {
		try (SystemIo sys = SystemIo.of()) {
			StringBuilder b = new StringBuilder();
			sys.out(StringUtil.asPrintStream(b));
			TestUtil.exec(ExecTest.class);
			assertTrue(b.toString().contains("Exec should do this"));
			assertTrue(b.toString().contains("Exec test that"));
		}
	}

	@Test
	public void testException() {
		Throwable t = thrown(() -> {
			throw new IOException("test");
		});
		assertThrowable(t, IOException.class, "test");
		assertNull(thrown(() -> {}));
	}

	@Test
	public void testInit() {
		boolean throwIt = false;
		assertThat(init(() -> {
			if (throwIt) throw new IOException();
			return "test";
		}), is("test"));
		assertThrown(RuntimeException.class, () -> init(() -> {
			throw new IOException();
		}));
	}

	@Test
	public void testTestMap() {
		Map<Integer, String> map = testMap(1, "1", 2, "2", 3);
		assertThat(map.size(), is(3));
		assertThat(map.get(1), is("1"));
		assertThat(map.get(2), is("2"));
		assertNull(map.get(3));
	}

	@Test
	public void testResource() {
		assertThat(resource("resource.txt"), is("test"));
		assertThrown(RuntimeException.class, () -> resource("not-found.txt"));
	}

	@Test
	public void testAssertNan() {
		assertNaN(Double.NaN);
		assertNaN("test", Double.NaN);
		assertAssertion(() -> assertNaN(Double.MAX_VALUE));
		assertAssertion(() -> assertNaN("test", 0.0));
	}

	@Test
	public void testAssertThrowable() {
		IOException e = new FileNotFoundException("test");
		assertThrowable(e, IOException.class);
		assertThrowable(e, FileNotFoundException.class);
		assertThrowable(e, "test");
		assertThrowable(e, m -> m.startsWith("test"));
		assertThrowable(null, null, (Predicate<String>) null);
	}

	@Test
	public void testAssertThrown() {
		assertAssertion(() -> TestUtil.assertThrown(() -> {}));
		assertAssertion(() -> TestUtil.assertThrown(IllegalArgumentException.class, () -> {
			throw new RuntimeException();
		}));
		TestUtil.assertThrown(IllegalArgumentException.class, () -> {
			throw new IllegalArgumentException();
		});
		assertAssertion(() -> TestUtil.assertThrown(IllegalArgumentException.class, () -> {
			throw new RuntimeException();
		}));
		TestUtil.assertThrown("test", () -> {
			throw new IOException("test");
		});
		TestUtil.assertThrown(m -> m.startsWith("test"), () -> {
			throw new IOException("test");
		});
		assertAssertion(() -> TestUtil.assertThrown(IOException.class, "test", () -> {
			throw new IOException("test");
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
		assertArray(new short[] { 1, 2 }, 1, 2);

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
	public void testAssertMap() {
		assertMap(Map.of());
		assertMap(Map.of(1, "A"), 1, "A");
		assertMap(Map.of(1, "A", 2, "B"), 1, "A", 2, "B");
	}

	@Test
	public void testToReadableString() {
		byte[] bytes = { 0, 'a', '.', Byte.MAX_VALUE, Byte.MIN_VALUE, '~', '!', -1 };
		assertThat(TestUtil.toReadableString(bytes), is("?a.??~!?"));
		assertThrown(IllegalArgumentException.class,
			() -> TestUtil.toReadableString(bytes, 3, 2, "test", '?'));
		assertThat(TestUtil.toReadableString(new byte[0], 0, 0, null, '.'), is(""));
		assertThat(TestUtil.toReadableString(new byte[0], 0, 0, "", '.'), is(""));
	}

	@Test
	public void testMatchesRegex() {
		Pattern p = Pattern.compile("[a-z]+");
		assertThat("abc", matchesRegex(p));
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
		assertThat(Integer.valueOf(1), isObject(1));
		assertThat(1, isObject(Integer.valueOf(1)));
		assertThat(Integer.valueOf(1), not(isObject(1L)));
	}

	@Test
	public void testLambdaName() {
		Function<?, ?> fn = i -> i;
		assertThat(TestUtil.lambdaName(fn), is("[lambda]"));
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
