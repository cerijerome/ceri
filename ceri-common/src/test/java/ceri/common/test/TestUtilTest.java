package ceri.common.test;

import static ceri.common.collection.ArrayUtil.booleans;
import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.collection.ArrayUtil.chars;
import static ceri.common.collection.ArrayUtil.doubles;
import static ceri.common.collection.ArrayUtil.floats;
import static ceri.common.collection.ArrayUtil.ints;
import static ceri.common.collection.ArrayUtil.longs;
import static ceri.common.collection.ArrayUtil.shorts;
import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertAssertion;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertDir;
import static ceri.common.test.TestUtil.assertExists;
import static ceri.common.test.TestUtil.assertFile;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertMap;
import static ceri.common.test.TestUtil.assertNaN;
import static ceri.common.test.TestUtil.assertRange;
import static ceri.common.test.TestUtil.assertRegex;
import static ceri.common.test.TestUtil.assertThrowable;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.assertValue;
import static ceri.common.test.TestUtil.init;
import static ceri.common.test.TestUtil.isArray;
import static ceri.common.test.TestUtil.isList;
import static ceri.common.test.TestUtil.isObject;
import static ceri.common.test.TestUtil.matchesRegex;
import static ceri.common.test.TestUtil.resource;
import static ceri.common.test.TestUtil.testMap;
import static ceri.common.test.TestUtil.thrown;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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
import ceri.common.collection.ArrayUtil;
import ceri.common.io.SystemIo;
import ceri.common.property.BaseProperties;
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
		TestUtil.assertApprox(dd, 1.0015, -0.00502, 0);
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
	public void testIsArray() {
		Integer[] array = { 1, 2, 3 };
		assertThat(array, isArray(1, 2, 3));
	}

	@Test
	public void testIsList() {
		List<Integer> list = Arrays.asList(1, 2, 3);
		assertThat(list, isList(1, 2, 3));
	}

	@Test
	public void testAssertAssertion() {
		assertAssertion(() -> assertAssertion(() -> {}));
		assertAssertion(() -> assertAssertion(() -> {
			throw new IOException();
		}));
	}

	@Test
	public void testAssertRegex() {
		assertRegex("test", "%1$s..%1$s", "t");
		assertAssertion(() -> assertRegex("test", "%1$s..%1$s", "T"));
	}

	@Test
	public void testAssertExists() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().root("a").file("b", "bbb").build()) {
			assertExists(helper.root, true);
			assertExists(helper.path("b"), true);
			assertExists(helper.path("c"), false);
			assertDir(helper.root, true);
			assertDir(helper.path("b"), false);
			assertDir(helper.path("c"), false);
		}
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
			assertDir(helper.path("a/0"), helper.path("a/1"));
			assertAssertion(() -> assertDir(helper.path("a/0"), helper.path("a/2")));
			assertAssertion(() -> assertDir(helper.path("a/0"), helper.path("a/3")));
			assertAssertion(() -> assertDir(helper.path("a/0"), helper.path("a/4")));
			assertAssertion(() -> assertDir(helper.path("a/0"), helper.path("a/5")));
		}
	}

	@Test
	public void testAssertFile() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().dir("a").dir("b").file("c", "")
			.file("d", "D").file("e", "E").build()) {
			assertAssertion(() -> assertFile(helper.path("a"), helper.path("c")));
			assertAssertion(() -> assertFile(helper.path("c"), helper.path("a")));
			assertFile(helper.path("c"), helper.path("c"));
			assertAssertion(() -> assertFile(helper.path("c"), helper.path("d")));
			assertAssertion(() -> assertFile(helper.path("d"), helper.path("c")));
			assertAssertion(() -> assertFile(helper.path("d"), helper.path("e")));
			assertFile(helper.path("d"), helper.path("d"));
			assertAssertion(() -> assertFile(helper.path("d"), helper.path("e")));
		}
	}

	@Test
	public void testAssertFileBytes() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().file("test", "abc").build()) {
			assertFile(helper.path("test"), "abc".getBytes());
			assertAssertion(() -> assertFile(helper.path("test"), "abd".getBytes()));
		}
	}

	@Test
	public void testAssertPrivateConstructor() {
		TestUtil.assertPrivateConstructor(TestUtil.class);
		assertAssertion(() -> TestUtil.assertPrivateConstructor(TestUtilTest.class));
		assertAssertion(() -> TestUtil.assertPrivateConstructor(TestUtilTest.Uncreatable.class));
	}

	public static class ExecTest {
		@Test
		public void shouldDoThis() {}

		@Test
		public void testThat() {}
	}

	@SuppressWarnings("resource")
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
	public void testFirstSystemProperty() {
		assertNotNull(TestUtil.firstSystemProperty());
	}

	@Test
	public void testFirstEnvironmentVariable() {
		assertNotNull(TestUtil.firstEnvironmentVariable());
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
	public void testNullErr() {
		try (SystemIo sys = TestUtil.nullErr()) {
			System.err.print("This text should not appear");
		}
	}

	@Test
	public void testAssertList() {
		TestUtil.assertList(List.of(1), 0, List.of(1), 0, 1);
		assertAssertion(() -> TestUtil.assertList(List.of(), 0, List.of(), 0, 1));
		assertAssertion(() -> TestUtil.assertList(List.of(), 0, List.of(1), 0, 1));
		assertAssertion(() -> TestUtil.assertList(List.of(1), 0, List.of(), 0, 1));
		assertAssertion(() -> TestUtil.assertList(List.of(1), 0, List.of(2), 0, 1));
		assertAssertion(() -> TestUtil.assertList(List.of(""), 0, List.of(" "), 0, 1));
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
	public void testProperties() throws IOException {
		BaseProperties properties = TestUtil.properties(getClass(), "test.properties");
		var p = new BaseProperties(properties, "a") {
			@Override
			public String toString() {
				return value("b");
			}
		};
		assertThat(p.toString(), is("123"));
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
		Class<? extends Throwable> nullCls = null;
		Predicate<String> nullPredicate = null;
		assertThrowable(null, nullCls, nullPredicate);
		assertAssertion(() -> assertThrowable(null, IOException.class, nullPredicate));
		assertAssertion(() -> assertThrowable(null, nullCls, s -> true));
		assertThrowable(e, IOException.class);
		assertThrowable(e, FileNotFoundException.class);
		assertThrowable(e, "test");
		assertThrowable(e, m -> m.startsWith("test"));
		assertAssertion(() -> assertThrowable(e, m -> m.startsWith("Test")));
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
		TestUtil.assertThrown(IOException.class, "test", () -> {
			throw new IOException("test");
		});
	}

	@Test
	public void testAssertValue() {
		assertValue("", String::isEmpty);
		assertAssertion(() -> assertValue("test", String::isEmpty));
	}

	@Test
	public void testAssertByte() {
		TestUtil.assertByte((byte) -1, 0xff);
		assertAssertion(() -> TestUtil.assertByte((byte) -1, 0xfe));
	}

	@Test
	public void testAssertShort() {
		TestUtil.assertShort((short) -1, 0xffff);
		assertAssertion(() -> TestUtil.assertShort((short) -1, 0xfffe));
	}

	@Test
	public void testAssertRange() {
		assertAssertion(
			() -> TestUtil.assertRange(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE - 1));
		assertAssertion(
			() -> TestUtil.assertRange(Long.MIN_VALUE, Long.MIN_VALUE + 1, Long.MAX_VALUE - 1));
		TestUtil.assertRange(0.0, 0.0, 1.0);
		TestUtil.assertRange(0.999, 0.0, 1.0);
		assertAssertion(() -> TestUtil.assertRange(1.0, 0.0, 1.0));
		assertAssertion(() -> TestUtil.assertRange(1.0, 1.0, 1.0));
		assertAssertion(() -> TestUtil.assertRange(-1.0, 0.0, 1.0));
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
		List<Integer> list = ArrayUtil.intList(5, 1, 4, 2, 3);
		TestUtil.assertCollection(list, 1, 2, 3, 4, 5);
		assertAssertion(() -> TestUtil.assertCollection(list, 1, 2, 4, 5));
		TestUtil.assertCollection(booleans(true, false), false, true);
		assertAssertion(() -> TestUtil.assertCollection(booleans(true, false), false, false));
		TestUtil.assertCollection(chars('a', 'b'), 'b', 'a');
		assertAssertion(() -> TestUtil.assertCollection(chars('a', 'b'), 'b', 'b'));
		TestUtil.assertCollection(chars('a', 'b'), 0x62, 0x61);
		assertAssertion(() -> TestUtil.assertCollection(chars('a', 'b'), 0x62, 0x62));
		TestUtil.assertCollection(bytes(-1, 1), 1, -1);
		assertAssertion(() -> TestUtil.assertCollection(bytes(-1, 1), 1, 1));
		TestUtil.assertCollection(shorts(-1, 1), 1, -1);
		assertAssertion(() -> TestUtil.assertCollection(shorts(-1, 1), 1, 1));
		TestUtil.assertCollection(ints(-1, 1), 1, -1);
		assertAssertion(() -> TestUtil.assertCollection(ints(-1, 1), 1, 1));
		TestUtil.assertCollection(longs(-1, 1), 1, -1);
		assertAssertion(() -> TestUtil.assertCollection(longs(-1, 1), 1, 1));
		TestUtil.assertCollection(floats(-1.0, 1.0), 1.0, -1.0);
		assertAssertion(() -> TestUtil.assertCollection(floats(-1.0, 1.0), 1.0, 1.0));
		TestUtil.assertCollection(doubles(-1, 1), 1, -1);
		assertAssertion(() -> TestUtil.assertCollection(doubles(-1, 1), 1, 1));
	}

	@Test
	public void testAssertMap() {
		assertMap(Map.of());
		assertMap(Map.of(1, "A"), 1, "A");
		assertMap(Map.of(1, "A", 2, "B"), 1, "A", 2, "B");
		assertMap(Map.of(1, "A", 2, "B", 3, "C"), 1, "A", 2, "B", 3, "C");
		assertMap(Map.of(1, "A", 2, "B", 3, "C", 4, "D"), 1, "A", 2, "B", 3, "C", 4, "D");
		assertMap(Map.of(1, "A", 2, "B", 3, "C", 4, "D", 5, "E"), 1, "A", 2, "B", 3, "C", 4, "D", 5,
			"E");
	}

	@Test
	public void testThrowIt() {
		assertThrown(IOException.class, () -> TestUtil.throwIt(new IOException("test")));
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
		assertThat(TestUtil.lambdaName(this), is(not("[lambda]")));
	}

	@Test
	public void testPathsToUnix() {
		List<Path> paths = List.of(Path.of("a", "b", "c.txt"), Path.of("a", "a.txt"));
		assertCollection(TestUtil.pathsToUnix(paths), "a/b/c.txt", "a/a.txt");
	}

	@Test
	public void testToUnixFromPath() {
		List<String> paths =
			List.of(Path.of("a", "b", "c.txt").toString(), Path.of("a", "a.txt").toString());
		assertCollection(TestUtil.pathNamesToUnix(paths), "a/b/c.txt", "a/a.txt");
	}

	@Test
	public void testAssertIterable() {
		final Set<Integer> set = new TreeSet<>();
		assertIterable(set);
		Collections.addAll(set, Integer.MAX_VALUE, Integer.MIN_VALUE, 0);
		assertIterable(set, Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
	}

	@Test
	public void testReader() {
		assertArray(TestUtil.reader(1, 2, 3).readBytes(), 1, 2, 3);
		assertArray(TestUtil.reader("abc").readBytes(), 'a', 'b', 'c');
	}

	@Test
	public void testInputStreamBytes() throws IOException {
		try (var in = TestUtil.inputStream(ArrayUtil.bytes(1, 2, 3, -1, -2, -3, 4))) {
			assertArray(in.readAllBytes(), 1, 2, 3, -1, -2, -3, 4);
		}
	}

}
