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
import static ceri.common.test.AssertUtil.assertApproxArray;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertArrayObject;
import static ceri.common.test.AssertUtil.assertAscii;
import static ceri.common.test.AssertUtil.assertAssertion;
import static ceri.common.test.AssertUtil.assertBuffer;
import static ceri.common.test.AssertUtil.assertByte;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertDir;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertExists;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFile;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertInstance;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertList;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertMask;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertNaN;
import static ceri.common.test.AssertUtil.assertNoMatch;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertNotFound;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertNotSame;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertRange;
import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertShort;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.AssertUtil.assertThrowable;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.assertValue;
import static ceri.common.test.AssertUtil.fail;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
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
	public void testAssertFalse() {
		assertFalse(false);
		assertAssertion(() -> assertFalse(true));
		assertAssertion(() -> assertFalse(true, "False"));
	}

	@Test
	public void testAssertTrue() {
		assertTrue(true);
		assertAssertion(() -> assertTrue(false));
		assertAssertion(() -> assertTrue(false, "True"));
	}

	@Test
	public void testAssertSame() {
		Integer i0 = Integer.valueOf(12345678);
		Integer i1 = Integer.valueOf(12345678);
		assertSame(i0, i0);
		assertSame(i1, i1);
		assertAssertion(() -> assertSame(i0, i1));
	}

	@Test
	public void testAssertInstance() {
		assertInstance(1, Object.class);
		assertInstance(1, Number.class);
		assertInstance(1, Integer.class);
		assertAssertion(() -> assertInstance(1, String.class));
		assertAssertion(() -> assertInstance(1, Long.class));
	}

	@Test
	public void testFail() {
		assertAssertion(() -> fail());
		assertAssertion(() -> fail("Test"));
		assertAssertion(() -> fail(new IOException()));
		assertAssertion(() -> fail(new IOException(), "Test"));
	}

	@Test
	public void testAssertNotSame() {
		Object obj = new Object();
		assertNotSame(obj, new Object());
		assertAssertion(() -> assertNotSame(obj, obj));
	}

	@Test
	public void testAssertNull() {
		assertNull(null);
		assertAssertion(() -> assertNull("test"));
		assertAssertion(() -> assertNull("test", "message"));
	}

	@Test
	public void testAssertNotNull() {
		assertNotNull("");
		assertAssertion(() -> assertNotNull(null));
		assertAssertion(() -> assertNotNull(null, "message"));
	}

	@Test
	public void testAssertDoubleEquals() {
		assertEquals(0.001, 0.001, 0.001);
		assertEquals(0.001, 0.002, 0.001);
		assertAssertion(() -> assertEquals(0.001, 0.002, 0.0001));
		assertAssertion(() -> assertEquals(0.001, 0.002, 0.0001, "message"));
	}

	@Test
	public void testAssertNotEquals() {
		assertNotEquals("test0", "test1");
		assertAssertion(() -> assertNotEquals("test", "test"));
		assertAssertion(() -> assertNotEquals("test", "test", "message"));
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
		ByteProvider.Reader<?> r = ByteUtil.toAscii("tests").reader(0);
		assertAscii(r, "test");
		r.reset();
		assertAssertion(() -> assertAscii(r, "test0"));
	}

	@Test
	public void testAssertRead() throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(ArrayUtil.bytes(1, 2, 3));
		assertRead(in, 1, 2, 3);
		in.reset();
		assertRead(in, ByteProvider.of(1, 2));
		in.reset();
		assertAssertion(() -> assertRead(in, 1, 2, 3, 4));
		in.reset();
		assertAssertion(() -> assertRead(in, ByteProvider.of(1, 2, 4)));
	}

	@Test
	public void testAssertExists() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().root("a").file("b", "bbb").build()) {
			assertExists(helper.root, true);
			assertExists(helper.path("b"), true);
			assertExists(helper.path("c"), false);
			assertAssertion(() -> assertExists(helper.path("b"), false));
			assertAssertion(() -> assertExists(helper.path("c"), true));
			assertDir(helper.root, true);
			assertDir(helper.path("b"), false);
			assertDir(helper.path("c"), false);
			assertAssertion(() -> assertDir(helper.root, false));
			assertAssertion(() -> assertDir(helper.path("c"), true));
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
	public void testAssertToString() {
		assertString(100, "100");
		assertAssertion(() -> assertString(0100, "0100"));
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
		// Class<? extends Throwable> nullCls = null;
		assertAssertion(() -> assertThrowable(null, null, s -> {}));
		assertAssertion(() -> assertThrowable(null, Exception.class, s -> {}));
		assertThrowable(e, IOException.class);
		assertThrowable(e, FileNotFoundException.class);
		assertThrowable(e, "test");
		assertThrowable(e, t -> assertTrue(t.getMessage().startsWith("test")));
		assertAssertion(
			() -> assertThrowable(e, t -> assertTrue(t.getMessage().startsWith("Test"))));
		assertThrowable(null, null, (Consumer<Throwable>) null);
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
		assertThrown(e -> e.getMessage().startsWith("test"), () -> {
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
		assertByte((byte) -1, 0xff, "%s", 0);
		assertAssertion(() -> assertByte((byte) -1, 0xfe));
		assertAssertion(() -> assertByte((byte) -1, 0xfe, "%s", 0));
	}

	@Test
	public void testAssertShort() {
		assertShort((short) -1, 0xffff);
		assertShort((short) -1, 0xffff, "%s", 0);
		assertAssertion(() -> assertShort((short) -1, 0xfffe));
		assertAssertion(() -> assertShort((short) -1, 0xfffe, "%s", 0));
	}

	@Test
	public void testAssertMask() {
		assertMask(0x4321, 0x4100);
		assertMask(-1, -1);
		assertMask(0, 0);
		assertMask(1, 0);
		assertMask(0x432100000000L, 0x410000000000L);
		assertMask(-1L, -1L);
		assertMask(0L, 0L);
		assertMask(1L, 0L);
		assertAssertion(() -> assertMask(0, -1));
		assertAssertion(() -> assertMask(0x80000000, 0x90000000));
		assertAssertion(() -> assertMask(0L, -1L));
		assertAssertion(() -> assertMask(0x8000000000000000L, 0x9000000000000000L));
	}

	@Test
	public void testAssertIntRange() {
		assertRange(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
		assertRange(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		assertRange(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
		assertRange(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		assertAssertion(
			() -> assertRange(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE - 1));
		assertAssertion(
			() -> assertRange(Integer.MIN_VALUE, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1));
	}

	@Test
	public void testAssertLongRange() {
		assertRange(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
		assertRange(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
		assertRange(Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
		assertRange(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE);
		assertAssertion(() -> assertRange(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE - 1));
		assertAssertion(() -> assertRange(Long.MIN_VALUE, Long.MIN_VALUE + 1, Long.MAX_VALUE - 1));
	}

	@Test
	public void testAssertDoubleRange() {
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
		assertArrayObject(b0, 0, b1, 0, 2);
		assertAssertion(() -> assertArrayObject(b0, 0, b1, 0, 3));
		assertArray(new short[] { 1, 2 }, 1, 2);
	}

	@Test
	public void testAssertApproxArray() {
		assertApproxArray(doubles(0.1234, 0.1235), 0.1233, 0.1236);
		assertApproxArray(4, doubles(0.1234, 0.1235), 0.1234, 0.1235);
		assertAssertion(() -> assertApproxArray(doubles(0.1234, 0.1235), 0.1235, 0.1235));
	}

	@Test
	public void testAssertArrayWithinDiff() {
		assertArray(0.001, doubles(0.1234, 0.1235), 0.1233, 0.1236);
		assertArray(0.0001, doubles(0.1234, 0.1235), 0.1234, 0.1235);
		assertAssertion(() -> assertArray(0.0001, doubles(0.1234, 0.1235), 0.1235, 0.1235));
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
		assertAssertion(() -> assertCollection(list, 1, 2, 3, 4, 5, 6));
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
	public void testAssertBuffer() {
		assertBuffer(ByteBuffer.wrap(bytes()));
		assertBuffer(ByteBuffer.wrap(bytes(0x80, 0xff, 0x7f)), 0x80, 0xff, 0x7f);
	}

	@Test
	public void testThrowIt() {
		assertThrown(() -> AssertUtil.throwRuntime());
		assertThrown(() -> AssertUtil.throwIo());
		assertThrown(() -> AssertUtil.throwInterrupted());
		assertThrown(IOException.class, () -> AssertUtil.throwIt(new IOException("test")));
	}

	@Test
	public void testAssertString() {
		assertString("x123.1", "%s%d%.1f", "x", 12, 3.1);
		assertAssertion(() -> assertString("x123.1", "%s%d%f", "x", 12, 3.1));
	}

	@Test
	public void testAssertFind() {
		Pattern p = Pattern.compile("[a-z]+");
		assertFind("123abc456", p);
		assertFind("123test456", "%1$s..%1$s", "t");
		assertAssertion(() -> assertFind("test", "%1$s..%1$s", "T"));
		assertAssertion(() -> assertFind("123456", p, "message"));
	}

	@Test
	public void testAssertNoMatch() {
		Pattern p = Pattern.compile("[a-z]+");
		assertNoMatch("123", p);
		assertNoMatch("123", "%1$s..%1$s", "\\d");
		assertAssertion(() -> assertNoMatch("test", "%1$s..%1$s", "t"));
		assertAssertion(() -> assertNoMatch("test", p, "message"));
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
