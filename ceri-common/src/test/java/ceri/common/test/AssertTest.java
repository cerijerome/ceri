package ceri.common.test;

import static ceri.common.test.Assert.assertApproxArray;
import static ceri.common.test.Assert.assertArray;
import static ceri.common.test.Assert.assertAscii;
import static ceri.common.test.Assert.assertBuffer;
import static ceri.common.test.Assert.assertByte;
import static ceri.common.test.Assert.assertConsume;
import static ceri.common.test.Assert.assertContains;
import static ceri.common.test.Assert.assertDir;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertExists;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertFile;
import static ceri.common.test.Assert.assertFind;
import static ceri.common.test.Assert.assertImmutable;
import static ceri.common.test.Assert.assertList;
import static ceri.common.test.Assert.assertMap;
import static ceri.common.test.Assert.assertMask;
import static ceri.common.test.Assert.assertMatch;
import static ceri.common.test.Assert.assertNoMatch;
import static ceri.common.test.Assert.assertNotContains;
import static ceri.common.test.Assert.assertNotEquals;
import static ceri.common.test.Assert.assertNotFound;
import static ceri.common.test.Assert.assertOrdered;
import static ceri.common.test.Assert.assertPrivateConstructor;
import static ceri.common.test.Assert.assertRange;
import static ceri.common.test.Assert.assertRawArray;
import static ceri.common.test.Assert.assertRead;
import static ceri.common.test.Assert.assertShort;
import static ceri.common.test.Assert.assertString;
import static ceri.common.test.Assert.assertTrue;
import static ceri.common.test.Assert.assertUnordered;
import static ceri.common.test.Assert.assertValue;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.collect.Sets;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.function.Functions;

public class AssertTest {
	private static final byte BMIN = Byte.MIN_VALUE;
	private static final byte BMAX = Byte.MAX_VALUE;
	private static final short SMIN = Short.MIN_VALUE;
	private static final short SMAX = Short.MAX_VALUE;
	private static final int IMIN = Integer.MIN_VALUE;
	private static final int IMAX = Integer.MAX_VALUE;
	private static final long LMIN = Long.MIN_VALUE;
	private static final long LMAX = Long.MAX_VALUE;
	private static final double DMIN = Double.MIN_VALUE;
	private static final double DMAX = Double.MAX_VALUE;
	private static final double DNINF = Double.NEGATIVE_INFINITY;
	private static final double DPINF = Double.POSITIVE_INFINITY;
	private static final double DNaN = Double.NaN;
	private static final double DN0 = -0.0;
	private static final double DP0 = 0.0;
	private final IOException ioe = new IOException("test");
	private FileTestHelper helper;

	@After
	public void after() {
		helper = TestUtil.close(helper);
	}

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

	// exceptions
	
	@Test
	public void testThrowing() {
		Assert.thrown(() -> Assert.throwRuntime());
		Assert.thrown(() -> Assert.throwIo());
		Assert.thrown(() -> Assert.throwInterrupted());
		Assert.io(() -> Assert.throwIt(ioe));
	}

	@Test
	public void testFailure() {
		Assert.assertion(() -> Assert.fail());
		Assert.assertion(() -> Assert.fail("Test"));
		Assert.assertion(() -> Assert.fail(ioe));
		Assert.assertion(() -> Assert.fail(ioe, "Test"));
	}

	@Test
	public void testAssertion() {
		Assert.assertion(() -> Assert.fail());
		Assert.assertion(() -> Assert.assertion(() -> {}));
		Assert.assertion(() -> Assert.assertion(() -> Assert.throwIo()));
	}

	@Test
	public void testThrown() {
		var iae = new IllegalArgumentException("test");
		Assert.assertion(() -> Assert.thrown(() -> {}));
		Assert.assertion(
			() -> Assert.thrown(IllegalArgumentException.class, () -> Assert.throwRuntime()));
		Assert.thrown(IllegalArgumentException.class, () -> Assert.throwIt(iae));
		Assert.assertion(
			() -> Assert.thrown(IllegalArgumentException.class, () -> Assert.throwRuntime()));
		Assert.thrown("test", () -> Assert.throwIt(ioe));
		Assert.thrown(e -> e.getMessage().startsWith("test"), () -> Assert.throwIt(ioe));
		Assert.thrown(IOException.class, "test", () -> Assert.throwIt(ioe));
	}

	@Test
	public void testThrowable() {
		var ioe = new FileNotFoundException("test");
		Assert.assertion(() -> Assert.throwable(null, null, _ -> {}));
		Assert.assertion(() -> Assert.throwable(null, Exception.class, _ -> {}));
		Assert.throwable(ioe, IOException.class);
		Assert.throwable(ioe, FileNotFoundException.class);
		Assert.throwable(ioe, "test");
		Assert.throwable(ioe,
			t -> Assert.assertEquals(t.getMessage().startsWith("test"), true));
		Assert.assertion(() -> Assert.throwable(ioe,
			t -> Assert.assertEquals(t.getMessage().startsWith("Test"), true)));
		Assert.throwable(null, null, (Functions.Consumer<Throwable>) null);
	}

	// equality
	
	@Test
	public void testInstance() {
		Assert.instance(1, Object.class);
		Assert.instance(1, Number.class);
		Assert.instance(1, Integer.class);
		Assert.assertion(() -> Assert.instance(1, String.class));
		Assert.assertion(() -> Assert.instance(1, Long.class));
	}

	@Test
	public void testSame() {
		var i0 = Integer.valueOf(12345678);
		var i1 = Integer.valueOf(12345678);
		Assert.same(i0, i0);
		Assert.same(i1, i1);
		Assert.assertion(() -> Assert.same(i0, i1));
	}

	@Test
	public void testNotSame() {
		var obj = new Object();
		Assert.notSame(obj, new Object());
		Assert.assertion(() -> Assert.notSame(obj, obj));
	}

	@Test
	public void testIsNull() {
		Assert.isNull(null);
		Assert.assertion(() -> Assert.isNull("test"));
		Assert.assertion(() -> Assert.isNull("test", "message"));
	}

	@Test
	public void testNotNull() {
		Assert.notNull("");
		Assert.assertion(() -> Assert.notNull(null));
		Assert.assertion(() -> Assert.notNull(null, "message"));
	}

	@Test
	public void testEqualDouble() {
		for (var d : ds(DN0, DP0, DMIN, DMAX, DNINF, DPINF, DNaN))
			Assert.equal(Assert.equal(d, d), d);
		Assert.assertion(() -> Assert.equal(-0.0, 0.0));
		Assert.assertion(() -> Assert.equal(0.0, -0.0));
	}

	@Test
	public void testApprox() {
		for (var d : ds(DN0, DP0, DMIN, DMAX, DNINF, DPINF, DNaN))
			Assert.equal(Assert.approx(d, d), d);
		Assert.equal(Assert.approx(-0.0, 0.0), -0.0);
		Assert.approx(-0.0, 0.0, 0);
		Assert.approx(0.000995, 0.00099499, 4);
		Assert.assertion(() -> Assert.approx(0.000995, 0.00099499, 5));
		Assert.assertion(() -> Assert.approx(Double.POSITIVE_INFINITY, Double.MAX_VALUE));
	}

	// support

	private static double[] ds(double... doubles) {
		return doubles;
	}

	// ======================================================================================
	// old
	// ======================================================================================

	@Test
	public void testAssertFalse() {
		assertFalse(false);
		Assert.assertion(() -> assertFalse(true));
		Assert.assertion(() -> assertFalse(true, "False"));
	}

	@Test
	public void testAssertTrue() {
		assertTrue(true);
		Assert.assertion(() -> assertTrue(false));
		Assert.assertion(() -> assertTrue(false, "True"));
	}

	@Test
	public void testAssertDoubleEquals() {
		assertEquals(0.001, 0.001, 0.001);
		assertEquals(0.001, 0.002, 0.001);
		Assert.assertion(() -> assertEquals(0.001, 0.002, 0.0001));
		Assert.assertion(() -> assertEquals(0.001, 0.002, 0.0001, "message"));
	}

	@Test
	public void testAssertNotEquals() {
		assertNotEquals("test0", "test1");
		Assert.assertion(() -> assertNotEquals("test", "test"));
		Assert.assertion(() -> assertNotEquals("test", "test", "message"));
	}

	@Test
	public void testAssertNotFound() {
		assertNotFound("hello", "l{3}");
		Assert.assertion(() -> assertNotFound("hello", "l{2}"));
	}

	@Test
	public void testAssertAscii() {
		var r = ByteUtil.toAscii("tests").reader(0);
		assertAscii(r, "test");
		r.reset();
		Assert.assertion(() -> assertAscii(r, "test0"));
	}

	@Test
	public void testAssertRead() throws IOException {
		var in = new ByteArrayInputStream(ArrayUtil.bytes.of(1, 2, 3));
		assertRead(in, 1, 2, 3);
		in.reset();
		assertRead(in, ByteProvider.of(1, 2));
		in.reset();
		Assert.assertion(() -> assertRead(in, 1, 2, 3, 4));
		in.reset();
		Assert.assertion(() -> assertRead(in, ByteProvider.of(1, 2, 4)));
	}

	@Test
	public void testAssertExists() throws IOException {
		helper = FileTestHelper.builder().root("a").file("b", "bbb").build();
		assertExists(helper.root, true);
		assertExists(helper.path("b"), true);
		assertExists(helper.path("c"), false);
		Assert.assertion(() -> assertExists(helper.path("b"), false));
		Assert.assertion(() -> assertExists(helper.path("c"), true));
		assertDir(helper.root, true);
		assertDir(helper.path("b"), false);
		assertDir(helper.path("c"), false);
		Assert.assertion(() -> assertDir(helper.root, false));
		Assert.assertion(() -> assertDir(helper.path("c"), true));
	}

	@Test
	public void testAssertDir() throws IOException {
		helper = FileTestHelper.builder().root("a").dir("a/0/d0") //
			.file("a/0/f0", "xxxxxx") //
			.file("a/0/f1", "") //
			.dir("a/1/d0").file("a/1/f0", "xxxxxx").file("a/1/f1", "") //
			.dir("a/2/d0").file("a/2/f", "xxxxxx").file("a/2/f1", "") //
			.dir("a/3/d0").file("a/3/f0", "").file("a/3/f1", "") //
			.dir("a/4/d0").file("a/4/f0", "xxxxxx").file("a/4/f1", "x") //
			.dir("a/5/d1").file("a/5/f0", "xxxxxx").file("a/5/f1", "") //
			.build();
		assertDir(helper.path("a/0"), helper.path("a/1"));
		Assert.assertion(() -> assertDir(helper.path("a/0"), helper.path("a/2")));
		Assert.assertion(() -> assertDir(helper.path("a/0"), helper.path("a/3")));
		Assert.assertion(() -> assertDir(helper.path("a/0"), helper.path("a/4")));
		Assert.assertion(() -> assertDir(helper.path("a/0"), helper.path("a/5")));
	}

	@Test
	public void testAssertFile() throws IOException {
		try (var helper = FileTestHelper.builder().dir("a").dir("b").file("c", "").file("d", "D")
			.file("e", "E").build()) {
			Assert.assertion(() -> assertFile(helper.path("a"), helper.path("c")));
			Assert.assertion(() -> assertFile(helper.path("c"), helper.path("a")));
			assertFile(helper.path("c"), helper.path("c"));
			Assert.assertion(() -> assertFile(helper.path("c"), helper.path("d")));
			Assert.assertion(() -> assertFile(helper.path("d"), helper.path("c")));
			Assert.assertion(() -> assertFile(helper.path("d"), helper.path("e")));
			assertFile(helper.path("d"), helper.path("d"));
			Assert.assertion(() -> assertFile(helper.path("d"), helper.path("e")));
		}
	}

	@Test
	public void testAssertFileBytes() throws IOException {
		try (var helper = FileTestHelper.builder().file("test", "abc").build()) {
			assertFile(helper.path("test"), "abc".getBytes());
			Assert.assertion(() -> assertFile(helper.path("test"), "abd".getBytes()));
		}
	}

	@Test
	public void testAssertToString() {
		assertString(100, "100");
		Assert.assertion(() -> assertString(0100, "0100"));
	}

	@Test
	public void testAssertPrivateConstructor() {
		assertPrivateConstructor(TestUtil.class);
		Assert.assertion(() -> assertPrivateConstructor(AssertTest.class));
		Assert.assertion(() -> assertPrivateConstructor(AssertTest.Uncreatable.class));
	}

	@Test
	public void testAssertList() {
		assertList(List.of(1), 0, List.of(1), 0, 1);
		Assert.assertion(() -> assertList(List.of(), 0, List.of(), 0, 1));
		Assert.assertion(() -> assertList(List.of(), 0, List.of(1), 0, 1));
		Assert.assertion(() -> assertList(List.of(1), 0, List.of(), 0, 1));
		Assert.assertion(() -> assertList(List.of(1), 0, List.of(2), 0, 1));
		Assert.assertion(() -> assertList(List.of(""), 0, List.of(" "), 0, 1));
	}

	@Test
	public void testAssertValue() {
		assertValue("", String::isEmpty);
		Assert.assertion(() -> assertValue("test", String::isEmpty));
	}

	@Test
	public void testAssertByte() {
		assertByte((byte) -1, 0xff);
		assertByte((byte) -1, 0xff, "%s", 0);
		Assert.assertion(() -> assertByte((byte) -1, 0xfe));
		Assert.assertion(() -> assertByte((byte) -1, 0xfe, "%s", 0));
	}

	@Test
	public void testAssertShort() {
		assertShort((short) -1, 0xffff);
		assertShort((short) -1, 0xffff, "%s", 0);
		Assert.assertion(() -> assertShort((short) -1, 0xfffe));
		Assert.assertion(() -> assertShort((short) -1, 0xfffe, "%s", 0));
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
		Assert.assertion(() -> assertMask(0, -1));
		Assert.assertion(() -> assertMask(0x80000000, 0x90000000));
		Assert.assertion(() -> assertMask(0L, -1L));
		Assert.assertion(() -> assertMask(0x8000000000000000L, 0x9000000000000000L));
	}

	@Test
	public void testAssertIntRange() {
		assertRange(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
		assertRange(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		assertRange(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
		assertRange(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		Assert.assertion(
			() -> assertRange(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE - 1));
		Assert.assertion(
			() -> assertRange(Integer.MIN_VALUE, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1));
	}

	@Test
	public void testAssertLongRange() {
		assertRange(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
		assertRange(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
		assertRange(Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
		assertRange(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE);
		Assert.assertion(() -> assertRange(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE - 1));
		Assert.assertion(() -> assertRange(Long.MIN_VALUE, Long.MIN_VALUE + 1, Long.MAX_VALUE - 1));
	}

	@Test
	public void testAssertDoubleRange() {
		assertRange(0.0, 0.0, 1.0);
		assertRange(0.999, 0.0, 1.0);
		Assert.assertion(() -> assertRange(1.0, 0.0, 1.0));
		Assert.assertion(() -> assertRange(1.0, 1.0, 1.0));
		Assert.assertion(() -> assertRange(-1.0, 0.0, 1.0));
	}

	@Test
	public void testAssertArray() {
		boolean[] b0 = { false, false };
		boolean[] b1 = { false, false };
		assertRawArray(b0, 0, b1, 0, 2);
		Assert.assertion(() -> assertRawArray(b0, 0, b1, 0, 3));
		assertArray(new short[] { 1, 2 }, 1, 2);
	}

	@Test
	public void testAssertNullArray() {
		assertArray((int[]) null, null);
	}

	@Test
	public void testAssertApproxArray() {
		assertApproxArray(ArrayUtil.doubles.of(0.1234, 0.1235), 0.1233, 0.1236);
		assertApproxArray(4, ArrayUtil.doubles.of(0.1234, 0.1235), 0.1234, 0.1235);
		Assert.assertion(
			() -> assertApproxArray(ArrayUtil.doubles.of(0.1234, 0.1235), 0.1235, 0.1235));
	}

	@Test
	public void testAssertArrayWithinDiff() {
		assertArray(0.001, ArrayUtil.doubles.of(0.1234, 0.1235), 0.1233, 0.1236);
		assertArray(0.0001, ArrayUtil.doubles.of(0.1234, 0.1235), 0.1234, 0.1235);
		Assert.assertion(
			() -> assertArray(0.0001, ArrayUtil.doubles.of(0.1234, 0.1235), 0.1235, 0.1235));
	}

	@Test
	public void testAssertArrayForByteProvider() {
		assertArray(TestUtil.provider(1, 2, 3), 1, 2, 3);
		Assert.assertion(() -> assertArray(TestUtil.provider(1, 2, 3), 1, 2));
	}

	@Test
	public void testAssertUnordered() {
		var list = ArrayUtil.ints.list(5, 1, 4, 2, 3);
		assertUnordered(list, 1, 2, 3, 4, 5);
		Assert.assertion(() -> assertUnordered(list, 1, 2, 4, 5));
		Assert.assertion(() -> assertUnordered(list, 1, 2, 3, 4, 5, 6));
		assertUnordered(ArrayUtil.bools.of(true, false), false, true);
		Assert.assertion(() -> assertUnordered(ArrayUtil.bools.of(true, false), false, false));
		assertUnordered(ArrayUtil.chars.of('a', 'b'), 'b', 'a');
		Assert.assertion(() -> assertUnordered(ArrayUtil.chars.of('a', 'b'), 'b', 'b'));
		assertUnordered(ArrayUtil.chars.of('a', 'b'), 0x62, 0x61);
		Assert.assertion(() -> assertUnordered(ArrayUtil.chars.of('a', 'b'), 0x62, 0x62));
		assertUnordered(ArrayUtil.bytes.of(-1, 1), 1, -1);
		Assert.assertion(() -> assertUnordered(ArrayUtil.bytes.of(-1, 1), 1, 1));
		assertUnordered(ArrayUtil.shorts.of(-1, 1), 1, -1);
		Assert.assertion(() -> assertUnordered(ArrayUtil.shorts.of(-1, 1), 1, 1));
		assertUnordered(ArrayUtil.ints.of(-1, 1), 1, -1);
		Assert.assertion(() -> assertUnordered(ArrayUtil.ints.of(-1, 1), 1, 1));
		assertUnordered(ArrayUtil.longs.of(-1, 1), 1, -1);
		Assert.assertion(() -> assertUnordered(ArrayUtil.longs.of(-1, 1), 1, 1));
		assertUnordered(ArrayUtil.floats.of(-1.0, 1.0), 1.0, -1.0);
		Assert.assertion(() -> assertUnordered(ArrayUtil.floats.of(-1.0, 1.0), 1.0, 1.0));
		assertUnordered(ArrayUtil.doubles.of(-1, 1), 1, -1);
		Assert.assertion(() -> assertUnordered(ArrayUtil.doubles.of(-1, 1), 1, 1));
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
	public void testAssertImmutables() {
		assertImmutable(Map.of());
		assertImmutable(Immutable.wrap(Maps.of()));
		assertImmutable(Map.of(1, "A"));
		assertImmutable(Immutable.wrap(Maps.copy(Map.of(1, "A"))));
		assertImmutable(List.of());
		assertImmutable(Immutable.wrap(Lists.of()));
		assertImmutable(List.of(1));
		assertImmutable(Immutable.wrap(Lists.of(Set.of(1))));
		assertImmutable(Set.of());
		assertImmutable(Immutable.wrap(Sets.of()));
		assertImmutable(Set.of(1));
		assertImmutable(Immutable.wrap(Sets.of(Set.of(1))));
		Assert.assertion(() -> assertImmutable(Maps.of()));
		Assert.assertion(() -> assertImmutable(Lists.of()));
		Assert.assertion(() -> assertImmutable(Sets.of()));
	}

	@Test
	public void testAssertBuffer() {
		assertBuffer(ByteBuffer.wrap(ArrayUtil.bytes.of()));
		assertBuffer(ByteBuffer.wrap(ArrayUtil.bytes.of(0x80, 0xff, 0x7f)), 0x80, 0xff, 0x7f);
	}

	@Test
	public void testAssertString() {
		assertString("x123.1", "%s%d%.1f", "x", 12, 3.1);
		Assert.assertion(() -> assertString("x123.1", "%s%d%f", "x", 12, 3.1));
	}

	@Test
	public void testAssertContains() {
		assertContains("aBcDe", "cD");
		Assert.assertion(() -> assertContains("aBcDe", "cd"));
	}

	@Test
	public void testAssertNotContains() {
		assertNotContains("aBcDe", "cd");
		Assert.assertion(() -> assertNotContains("aBcDe", "cD"));
	}

	@Test
	public void testAssertFind() {
		var p = Pattern.compile("[a-z]+");
		assertFind("123abc456", p);
		assertFind("123test456", "%1$s..%1$s", "t");
		Assert.assertion(() -> assertFind("test", "%1$s..%1$s", "T"));
		Assert.assertion(() -> assertFind("123456", p, "message"));
	}

	@Test
	public void testAssertNoMatch() {
		var p = Pattern.compile("[a-z]+");
		assertNoMatch("123", p);
		assertNoMatch("123", "%1$s..%1$s", "\\d");
		Assert.assertion(() -> assertNoMatch("test", "%1$s..%1$s", "t"));
		Assert.assertion(() -> assertNoMatch("test", p, "message"));
	}

	@Test
	public void testAssertMatch() {
		var p = Pattern.compile("[a-z]+");
		assertMatch("abc", p);
		assertMatch("test", "%1$s..%1$s", "t");
		Assert.assertion(() -> assertMatch("test", "%1$s..%1$s", "T"));
	}

	@Test
	public void testAssertIterable() {
		var set = Sets.<Integer>tree();
		assertOrdered(set);
		Collections.addAll(set, Integer.MAX_VALUE, Integer.MIN_VALUE, 0);
		assertOrdered(set, Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
	}

	@Test
	public void testAssertConsume() {
		assertConsume(List.of());
		assertConsume(List.of(0), t -> assertEquals(t, 0));
		assertConsume(List.of(Integer.MAX_VALUE, Integer.MIN_VALUE, 0),
			t -> assertEquals(t, Integer.MAX_VALUE), t -> assertEquals(t, Integer.MIN_VALUE),
			t -> assertEquals(t, 0));
		assertConsume(List.of(Integer.MAX_VALUE, Integer.MIN_VALUE, 0), _ -> {}, _ -> {}, _ -> {});
	}

	@Test
	public void testAssertConsumeFailure() {
		Assert.assertion(() -> assertConsume(List.of(0), t -> assertEquals(t, 1)));
		Assert.assertion(() -> assertConsume(List.of(Integer.MAX_VALUE, Integer.MIN_VALUE, 0),
			_ -> {}, _ -> {}));
		Assert.assertion(() -> assertConsume(List.of(Integer.MAX_VALUE, Integer.MIN_VALUE, 0),
			_ -> {}, _ -> {}, _ -> {}, _ -> {}));
	}
}
