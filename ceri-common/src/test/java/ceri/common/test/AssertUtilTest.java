package ceri.common.test;

import static ceri.common.collection.ArrayUtil.booleans;
import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.collection.ArrayUtil.chars;
import static ceri.common.collection.ArrayUtil.doubles;
import static ceri.common.collection.ArrayUtil.floats;
import static ceri.common.collection.ArrayUtil.ints;
import static ceri.common.collection.ArrayUtil.longs;
import static ceri.common.collection.ArrayUtil.shorts;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertArrayObject;
import static ceri.common.test.AssertUtil.assertAscii;
import static ceri.common.test.AssertUtil.assertAssertion;
import static ceri.common.test.AssertUtil.assertByte;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertDir;
import static ceri.common.test.AssertUtil.assertExists;
import static ceri.common.test.AssertUtil.assertFile;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertList;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertNaN;
import static ceri.common.test.AssertUtil.assertNotFound;
import static ceri.common.test.AssertUtil.assertNotSame;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertRange;
import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.AssertUtil.assertShort;
import static ceri.common.test.AssertUtil.assertThrowable;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertValue;
import static ceri.common.test.AssertUtil.fail;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;

public class AssertUtilTest {

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
	public void testFail() {
		assertAssertion(() -> fail());
		assertAssertion(() -> fail("Test"));
	}

	@Test
	public void testAssertNotSame() {
		Object obj = new Object();
		assertNotSame(obj, new Object());
		assertAssertion(() -> assertNotSame(obj, obj));
	}

	@Test
	public void testAssertNotFound() {
		assertNotFound("hello", "l{3}");
		assertAssertion(() -> assertNotFound("hello", "l{2}"));
	}

	@Test
	public void testAssertApprox() {
		assertAssertion(() -> assertApprox(Double.POSITIVE_INFINITY, Double.MAX_VALUE));
		assertApprox(0.000995, 0.00099499, 4);
		assertAssertion(() -> assertApprox(0.000995, 0.00099499, 5));
	}

	@Test
	public void testAssertAssertion() {
		assertAssertion(() -> assertAssertion(() -> {}));
		assertAssertion(() -> assertAssertion(() -> {
			throw new IOException();
		}));
	}

	@Test
	public void testAssertAscii() {
		ByteProvider.Reader r = ByteUtil.toAscii("tests").reader(0);
		assertAscii(r, "test");
		r.reset();
		assertAssertion(() -> assertAscii(r, "test0"));
	}

	@Test
	public void testAssertRead() throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(ArrayUtil.bytes(1, 2, 3));
		assertRead(in, 1, 2, 3);
		in.reset();
		assertRead(in, ByteArray.Immutable.wrap(1, 2));
		in.reset();
		assertAssertion(() -> assertRead(in, 1, 2, 3, 4));
		in.reset();
		assertAssertion(() -> assertRead(in, ByteArray.Immutable.wrap(1, 2, 4)));
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
		assertPrivateConstructor(TestUtil.class);
		assertAssertion(() -> assertPrivateConstructor(AssertUtilTest.class));
		assertAssertion(() -> assertPrivateConstructor(AssertUtilTest.Uncreatable.class));
	}

	@Test
	public void testAssertList() {
		assertList(List.of(1), 0, List.of(1), 0, 1);
		assertAssertion(() -> assertList(List.of(), 0, List.of(), 0, 1));
		assertAssertion(() -> assertList(List.of(), 0, List.of(1), 0, 1));
		assertAssertion(() -> assertList(List.of(1), 0, List.of(), 0, 1));
		assertAssertion(() -> assertList(List.of(1), 0, List.of(2), 0, 1));
		assertAssertion(() -> assertList(List.of(""), 0, List.of(" "), 0, 1));
	}

	@Test
	public void testAssertNan() {
		assertNaN(Double.NaN);
		assertAssertion(() -> assertNaN(Double.MAX_VALUE));
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
		assertAssertion(() -> assertThrown(() -> {}));
		assertAssertion(() -> assertThrown(IllegalArgumentException.class, () -> {
			throw new RuntimeException();
		}));
		assertThrown(IllegalArgumentException.class, () -> {
			throw new IllegalArgumentException();
		});
		assertAssertion(() -> assertThrown(IllegalArgumentException.class, () -> {
			throw new RuntimeException();
		}));
		assertThrown("test", () -> {
			throw new IOException("test");
		});
		assertThrown(m -> m.startsWith("test"), () -> {
			throw new IOException("test");
		});
		assertThrown(IOException.class, "test", () -> {
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
		assertByte((byte) -1, 0xff);
		assertAssertion(() -> assertByte((byte) -1, 0xfe));
	}

	@Test
	public void testAssertShort() {
		assertShort((short) -1, 0xffff);
		assertAssertion(() -> assertShort((short) -1, 0xfffe));
	}

	@Test
	public void testAssertRange() {
		assertAssertion(() -> assertRange(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE - 1));
		assertAssertion(() -> assertRange(Long.MIN_VALUE, Long.MIN_VALUE + 1, Long.MAX_VALUE - 1));
		assertRange(0.0, 0.0, 1.0);
		assertRange(0.999, 0.0, 1.0);
		assertAssertion(() -> assertRange(1.0, 0.0, 1.0));
		assertAssertion(() -> assertRange(1.0, 1.0, 1.0));
		assertAssertion(() -> assertRange(-1.0, 0.0, 1.0));
	}

	@Test
	public void testAssertArray() {
		boolean[] b0 = { false, false };
		boolean[] b1 = { false, false };
		assertAssertion(() -> assertArrayObject(b0, 0, b1, 0, 3));
		assertArray(new short[] { 1, 2 }, 1, 2);
	}

	@Test
	public void testAssertArrayForByteProvider() {
		assertArray(TestUtil.provider(1, 2, 3), 1, 2, 3);
		assertAssertion(() -> assertArray(TestUtil.provider(1, 2, 3), 1, 2));
	}

	@Test
	public void testAssertCollection() {
		List<Integer> list = ArrayUtil.intList(5, 1, 4, 2, 3);
		assertCollection(list, 1, 2, 3, 4, 5);
		assertAssertion(() -> assertCollection(list, 1, 2, 4, 5));
		assertCollection(booleans(true, false), false, true);
		assertAssertion(() -> assertCollection(booleans(true, false), false, false));
		assertCollection(chars('a', 'b'), 'b', 'a');
		assertAssertion(() -> assertCollection(chars('a', 'b'), 'b', 'b'));
		assertCollection(chars('a', 'b'), 0x62, 0x61);
		assertAssertion(() -> assertCollection(chars('a', 'b'), 0x62, 0x62));
		assertCollection(bytes(-1, 1), 1, -1);
		assertAssertion(() -> assertCollection(bytes(-1, 1), 1, 1));
		assertCollection(shorts(-1, 1), 1, -1);
		assertAssertion(() -> assertCollection(shorts(-1, 1), 1, 1));
		assertCollection(ints(-1, 1), 1, -1);
		assertAssertion(() -> assertCollection(ints(-1, 1), 1, 1));
		assertCollection(longs(-1, 1), 1, -1);
		assertAssertion(() -> assertCollection(longs(-1, 1), 1, 1));
		assertCollection(floats(-1.0, 1.0), 1.0, -1.0);
		assertAssertion(() -> assertCollection(floats(-1.0, 1.0), 1.0, 1.0));
		assertCollection(doubles(-1, 1), 1, -1);
		assertAssertion(() -> assertCollection(doubles(-1, 1), 1, 1));
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
		assertThrown(() -> AssertUtil.throwIt());
		assertThrown(IOException.class, () -> AssertUtil.throwIt(new IOException("test")));
	}

	@Test
	public void testAssertMatch() {
		Pattern p = Pattern.compile("[a-z]+");
		assertMatch("abc", p);
		assertMatch("test", "%1$s..%1$s", "t");
		assertAssertion(() -> assertMatch("test", "%1$s..%1$s", "T"));
	}

	@Test
	public void testAssertIterable() {
		final Set<Integer> set = new TreeSet<>();
		assertIterable(set);
		Collections.addAll(set, Integer.MAX_VALUE, Integer.MIN_VALUE, 0);
		assertIterable(set, Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
	}

}