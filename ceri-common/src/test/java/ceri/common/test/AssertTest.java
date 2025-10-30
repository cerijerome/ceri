package ceri.common.test;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.collect.Sets;
import ceri.common.data.ByteProvider;
import ceri.common.data.Bytes;
import ceri.common.data.TypeValue;
import ceri.common.function.Functions;
import ceri.common.text.StringBuilders;
import ceri.common.util.Truth;

public class AssertTest {
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

	static class Uncreatable {
		private Uncreatable() {
			throw new RuntimeException();
		}
	}

	@After
	public void after() {
		helper = Testing.close(helper);
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
		Assert.throwable(ioe, t -> Assert.equal(t.getMessage().startsWith("test"), true));
		Assert.assertion(() -> Assert.throwable(ioe,
			t -> Assert.equal(t.getMessage().startsWith("Test"), true)));
		Assert.throwable(null, null, (Functions.Consumer<Throwable>) null);
	}

	// misc

	@Test
	public void testNo() {
		Assert.no(false);
		Assert.assertion(() -> Assert.no(true));
		Assert.assertion(() -> Assert.no(true, "False"));
	}

	@Test
	public void testYes() {
		Assert.yes(true);
		Assert.assertion(() -> Assert.yes(false));
		Assert.assertion(() -> Assert.yes(false, "True"));
	}

	@Test
	public void testInstance() {
		Assert.instance(1, Object.class);
		Assert.instance(1, Number.class);
		Assert.instance(1, Integer.class);
		Assert.assertion(() -> Assert.instance(1, String.class));
		Assert.assertion(() -> Assert.instance(1, Long.class));
	}

	@Test
	public void testPrivateConstructor() {
		Assert.privateConstructor(Testing.class);
		Assert.assertion(() -> Assert.privateConstructor(AssertTest.class));
		Assert.assertion(() -> Assert.privateConstructor(AssertTest.Uncreatable.class));
	}

	// object equality

	@Test
	public void testNotEqual() {
		Assert.notEqual("test0", "test1");
		Assert.assertion(() -> Assert.notEqual(null, null));
		Assert.assertion(() -> Assert.notEqual("test", "test"));
		Assert.assertion(() -> Assert.notEqual("test", "test", "message"));
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

	// primitives

	@Test
	public void testEqualsByte() {
		Assert.equals((byte) -1, 0xff);
		Assert.equals((byte) -1, 0xff, "%s", 0);
		Assert.assertion(() -> Assert.equals((byte) -1, 0xfe));
		Assert.assertion(() -> Assert.equals((byte) -1, 0xfe, "%s", 0));
	}

	@Test
	public void testEqualsShort() {
		Assert.equals((short) -1, 0xffff);
		Assert.equals((short) -1, 0xffff, "%s", 0);
		Assert.assertion(() -> Assert.equals((short) -1, 0xfffe));
		Assert.assertion(() -> Assert.equals((short) -1, 0xfffe, "%s", 0));
	}

	@Test
	public void testEqualsDouble() {
		for (var d : ds(DN0, DP0, DMIN, DMAX, DNINF, DPINF, DNaN))
			Assert.equals(Assert.equals(d, d), d);
		Assert.assertion(() -> Assert.equals(-0.0, 0.0));
		Assert.assertion(() -> Assert.equals(0.0, -0.0));
	}

	@Test
	public void testMask() {
		Assert.mask(0x4321, 0x4100);
		Assert.mask(-1, -1);
		Assert.mask(0, 0);
		Assert.mask(1, 0);
		Assert.mask(0x432100000000L, 0x410000000000L);
		Assert.mask(-1L, -1L);
		Assert.mask(0L, 0L);
		Assert.mask(1L, 0L);
		Assert.assertion(() -> Assert.mask(0, -1));
		Assert.assertion(() -> Assert.mask(0x80000000, 0x90000000));
		Assert.assertion(() -> Assert.mask(0L, -1L));
		Assert.assertion(() -> Assert.mask(0x8000000000000000L, 0x9000000000000000L));
	}

	@Test
	public void testIntRange() {
		Assert.range(IMAX, IMIN, IMAX);
		Assert.range(IMAX, IMAX, IMAX);
		Assert.range(IMIN, IMIN, IMAX);
		Assert.range(IMIN, IMIN, IMIN);
		Assert.assertion(() -> Assert.range(IMAX, IMIN, IMAX - 1));
		Assert.assertion(() -> Assert.range(IMIN, IMIN + 1, IMAX - 1));
	}

	@Test
	public void testLongRange() {
		Assert.range(LMAX, LMIN, LMAX);
		Assert.range(LMAX, LMAX, LMAX);
		Assert.range(LMIN, LMIN, LMAX);
		Assert.range(LMIN, LMIN, LMIN);
		Assert.assertion(() -> Assert.range(LMAX, LMIN, LMAX - 1));
		Assert.assertion(() -> Assert.range(LMIN, LMIN + 1, LMAX - 1));
	}

	@Test
	public void testDoubleRange() {
		Assert.range(0.0, 0.0, 1.0);
		Assert.range(0.5, 0.0, 1.0);
		Assert.range(-1.0, -1.0, 0.0);
		Assert.assertion(() -> Assert.range(1.0, 0.0, 0.999));
		Assert.assertion(() -> Assert.range(1.0, 0.999, 0.999));
		Assert.assertion(() -> Assert.range(-1.0, 0.0, 1.0));
	}

	@Test
	public void testApprox() {
		for (var d : ds(DN0, DP0, DMIN, DMAX, DNINF, DPINF, DNaN))
			Assert.equal(Assert.approx(d, d), d);
		Assert.equal(Assert.approx(-0.0, 0.0), -0.0);
		Assert.approx(-0.0, 0.0, 0);
		Assert.approx(0.000995, 0.00099499, 4);
		Assert.assertion(() -> Assert.approx(0.000995, 0.00099499, 5));
		Assert.assertion(() -> Assert.approx(DPINF, DMAX));
	}

	@Test
	public void testApproxWithDiff() {
		for (var d : ds(DN0, DP0, DMIN, DMAX, DNINF, DPINF, DNaN))
			Assert.equals(Assert.approx(d, d, 0.0), d);
		Assert.approx(0.001, 0.001, 0.001);
		Assert.approx(0.001, 0.002, 0.001);
		Assert.assertion(() -> Assert.approx(0.001, 0.002, 0.0001));
		Assert.assertion(() -> Assert.approx(0.001, 0.002, 0.0001, "test"));
	}

	// arrays

	@Test
	public void testNullArray() {
		Assert.array((int[]) null, null);
	}

	@Test
	public void testArray() {
		Assert.array(new short[] { 1, 2 }, 1, 2);
	}

	@Test
	public void testArrayForByteProvider() {
		Assert.array(ByteProvider.of(1, 2, 3), 1, 2, 3);
		Assert.assertion(() -> Assert.array(ByteProvider.of(1, 2, 3), 1, 2));
	}

	@Test
	public void testApproxArray() {
		Assert.approxArray(Array.doubles.of(0.1234, 0.1235), 0.1233, 0.1236);
		Assert.approxArray(4, Array.doubles.of(0.1234, 0.1235), 0.1234, 0.1235);
		Assert.assertion(
			() -> Assert.approxArray(Array.doubles.of(0.1234, 0.1235), 0.1235, 0.1235));
	}

	@Test
	public void testApproxArrayWithDiff() {
		Assert.approxArray(0.001, Array.doubles.of(0.1234, 0.1235), 0.1233, 0.1236);
		Assert.approxArray(0.0001, Array.doubles.of(0.1234, 0.1235), 0.1234, 0.1235);
		Assert.assertion(
			() -> Assert.approxArray(0.0001, Array.doubles.of(0.1234, 0.1235), 0.1235, 0.1235));
	}

	// collections

	@Test
	public void testList() {
		Assert.list(null, null);
		Assert.list(List.of(1), 0, List.of(1), 0, 1);
		Assert.assertion(() -> Assert.list(List.of(), null));
		Assert.assertion(() -> Assert.list(List.of(), 0, List.of(), 0, 1));
		Assert.assertion(() -> Assert.list(List.of(), 0, List.of(1), 0, 1));
		Assert.assertion(() -> Assert.list(List.of(1), 0, List.of(), 0, 1));
		Assert.assertion(() -> Assert.list(List.of(1), 0, List.of(2), 0, 1));
		Assert.assertion(() -> Assert.list(List.of(""), 0, List.of(" "), 0, 1));
	}

	@Test
	public void testMap() {
		Assert.map(Map.of());
		Assert.map(Map.of(1, "A"), 1, "A");
		Assert.map(Map.of(1, "A", 2, "B"), 1, "A", 2, "B");
		Assert.map(Map.of(1, "A", 2, "B", 3, "C"), 1, "A", 2, "B", 3, "C");
		Assert.map(Map.of(1, "A", 2, "B", 3, "C", 4, "D"), 1, "A", 2, "B", 3, "C", 4, "D");
		Assert.map(Map.of(1, "A", 2, "B", 3, "C", 4, "D", 5, "E"), 1, "A", 2, "B", 3, "C", 4, "D",
			5, "E");
	}

	@Test
	public void testImmutableIterator() {
		Assert.immutable(List.of(1).iterator());
		Assert.assertion(() -> Assert.immutable(Lists.ofAll(1).iterator()));
	}

	@Test
	public void testImmutableCollection() {
		Assert.immutable(Immutable.setOf(1, null));
		Assert.immutable(Immutable.listOf(1, null));
		Assert.immutable(Lists.wrap());
		Assert.assertion(() -> Assert.immutable(Sets.of()));
		Assert.assertion(() -> Assert.immutable(Lists.of()));
		Assert.assertion(() -> Assert.immutable(Lists.wrap(1)));
		for (int i = 0; i <= 8; i++) { // increase when adding more mocked calls
			int call = i;
			Assert.assertion(() -> Assert.immutable(immutableListUntil(call)));
		}
	}

	@Test
	public void testImmutableMap() {
		Assert.immutable(Map.of());
		Assert.immutable(Immutable.wrap(Maps.of()));
		Assert.immutable(Map.of(1, "A"));
		Assert.immutable(Immutable.wrap(Maps.copy(Map.of(1, "A"))));
		Assert.assertion(() -> Assert.immutable(Maps.of()));
		for (int i = 0; i <= 1; i++) { // increase when adding more mocked calls
			int call = i;
			Assert.assertion(() -> Assert.immutable(immutableMapUntil(call)));
		}
	}

	// streams

	// unordered

	@Test
	public void testUnordered() {
		Assert.unordered(Array.of(1, -1, null), null, 1, -1);
		var list = Array.ints.list(5, 1, 4, 2, 3);
		Assert.unordered(list, 1, 2, 3, 4, 5);
		Assert.assertion(() -> Assert.unordered(list, 1, 2, 4, 5));
		Assert.assertion(() -> Assert.unordered(list, 1, 2, 3, 4, 5, 6));
		Assert.unordered(Array.bools.of(true, false), false, true);
		Assert.assertion(() -> Assert.unordered(Array.bools.of(true, false), false, false));
		Assert.unordered(Array.chars.of('a', 'b'), 'b', 'a');
		Assert.assertion(() -> Assert.unordered(Array.chars.of('a', 'b'), 'b', 'b'));
		Assert.unordered(Array.chars.of('a', 'b'), 0x62, 0x61);
		Assert.assertion(() -> Assert.unordered(Array.chars.of('a', 'b'), 0x62, 0x62));
		Assert.unordered(Array.bytes.of(-1, 1), 1, -1);
		Assert.assertion(() -> Assert.unordered(Array.bytes.of(-1, 1), 1, 1));
		Assert.unordered(Array.shorts.of(-1, 1), 1, -1);
		Assert.assertion(() -> Assert.unordered(Array.shorts.of(-1, 1), 1, 1));
		Assert.unordered(Array.ints.of(-1, 1), 1, -1);
		Assert.assertion(() -> Assert.unordered(Array.ints.of(-1, 1), 1, 1));
		Assert.unordered(Array.longs.of(-1, 1), 1, -1);
		Assert.assertion(() -> Assert.unordered(Array.longs.of(-1, 1), 1, 1));
		Assert.unordered(Array.floats.of(-1.0, 1.0), 1.0, -1.0);
		Assert.assertion(() -> Assert.unordered(Array.floats.of(-1.0, 1.0), 1.0, 1.0));
		Assert.unordered(Array.doubles.of(-1, 1), 1, -1);
		Assert.assertion(() -> Assert.unordered(Array.doubles.of(-1, 1), 1, 1));
	}

	// ordered

	@Test
	public void testOrdered() {
		var set = Sets.<Integer>tree();
		Assert.ordered(set);
		Collections.addAll(set, IMAX, IMIN, 0);
		Assert.ordered(set, IMIN, 0, IMAX);
	}

	@Test
	public void testOrderedMap() {
		Assert.ordered(Maps.put(Maps.link(), 1, "A"), 1, "A");
		Assert.ordered(Maps.put(Maps.link(), 1, "A", 2, "B"), 1, "A", 2, "B");
		Assert.ordered(Maps.put(Maps.link(), 1, "A", 2, "B", 3, "C"), 1, "A", 2, "B", 3, "C");
		Assert.ordered(Maps.put(Maps.link(), 1, "A", 2, "B", 3, "C", 4, "D"), 1, "A", 2, "B", 3,
			"C", 4, "D");
		Assert.ordered(Maps.put(Maps.link(), 1, "A", 2, "B", 3, "C", 4, "D", 5, "E"), 1, "A", 2,
			"B", 3, "C", 4, "D", 5, "E");
		Assert.assertion(() -> Assert.ordered(Maps.put(Maps.link(), //
			1, "A", 2, "B"), 2, "B", 1, "A"));
		Assert.assertion(() -> Assert.ordered(Maps.put(Maps.link(), //
			1, "A", 2, "B", 3, "C"), 2, "B", 3, "C", 1, "A"));
		Assert.assertion(() -> Assert.ordered(Maps.put(Maps.link(), //
			1, "A", 2, "B", 3, "C", 4, "D"), 2, "B", 3, "C", 4, "D", 1, "A"));
		Assert.assertion(() -> Assert.ordered(Maps.put(Maps.link(), //
			1, "A", 2, "B", 3, "C", 4, "D", 5, "E"), 2, "B", 3, "C", 4, "D", 5, "E", 1, "A"));
	}

	// strings

	@Test
	public void testString() {
		Assert.string(100, "100");
		Assert.string("x123.1", "%s%d%.1f", "x", 12, 3.1);
		Assert.assertion(() -> Assert.string(null, ""));
		Assert.assertion(() -> Assert.string("", "\0"));
		Assert.assertion(() -> Assert.string(0100, "0100"));
		Assert.assertion(() -> Assert.string("x123.1", "%s%d%f", "x", 12, 3.1));
		Assert.assertion(() -> Assert.string("abcdef", "abcde"));
	}

	@Test
	public void testLines() {
		Assert.lines(lines(3), "line000", "line001", "line002");
		Assert.lines(lines(3), "line000", "line001", "line002", "");
		Assert.assertion(() -> Assert.lines(lines(3), "line000", "line001"));
		Assert.assertion(() -> Assert.lines(lines(3), "line000", "line001", "line003"));
		Assert.assertion(() -> Assert.lines(lines(3), "line000", "line001", "line002", " "));
	}

	@Test
	public void testText() {
		Assert.text("", "");
		Assert.text("", "\n");
		Assert.text(lines(3), lines(3));
		Assert.assertion(() -> Assert.text("", " "));
		Assert.assertion(() -> Assert.text(lines(3), lines(2)));
	}

	@Test
	public void testContains() {
		Assert.contains("aBcDe", "cD");
		Assert.assertion(() -> Assert.contains("aBcDe", "cd"));
	}

	@Test
	public void testMatch() {
		var p = Pattern.compile("[a-z]+");
		Assert.match("abc", p);
		Assert.match("test", "%1$s..%1$s", "t");
		Assert.assertion(() -> Assert.match("test", "%1$s..%1$s", "T"));
	}

	@Test
	public void testNoMatch() {
		var p = Pattern.compile("[a-z]+");
		Assert.noMatch("123", p);
		Assert.noMatch("123", "%1$s..%1$s", "\\d");
		Assert.assertion(() -> Assert.noMatch("test", "%1$s..%1$s", "t"));
		Assert.assertion(() -> Assert.noMatch("test", p, "message"));
	}

	@Test
	public void testFind() {
		var p = Pattern.compile("[a-z]+");
		Assert.find("123abc456", p);
		Assert.find("123test456", "%1$s..%1$s", "t");
		Assert.assertion(() -> Assert.find("test", "%1$s..%1$s", "T"));
		Assert.assertion(() -> Assert.find("123456", p, "message"));
	}

	@Test
	public void testNotFound() {
		Assert.notFound("hello", "l{3}");
		Assert.assertion(() -> Assert.notFound("hello", "l{2}"));
	}

	@Test
	public void testAscii() {
		var r = Bytes.toAscii("tests").reader(0);
		Assert.ascii(r, "test");
		r.reset();
		Assert.assertion(() -> Assert.ascii(r, "test0"));
	}

	// other types
	
	@Test
	public void testTypeValue() {
		Assert.typeValue(TypeValue.of(-1, null, null), null, -1);
		Assert.typeValue(TypeValue.of(-1, null, null), null, -1, null);
		Assert.typeValue(TypeValue.of(-1, null, "no"), null, -1);
		Assert.typeValue(TypeValue.of(-1, null, "no"), null, -1, "no");
		Assert.typeValue(TypeValue.of(-1, Truth.maybe, "maybe"), Truth.maybe, -1);
		Assert.assertion(() -> Assert.typeValue(
			TypeValue.of(-1, Truth.maybe, "maybe"), Truth.maybe, -1, null));
	}
	
	// I/O

	@Test
	public void testBuffer() {
		Assert.buffer(ByteBuffer.wrap(Array.bytes.of()));
		Assert.buffer(ByteBuffer.wrap(Array.bytes.of(0x80, 0xff, 0x7f)), 0x80, 0xff, 0x7f);
	}

	@Test
	public void testRead() throws IOException {
		var in = new ByteArrayInputStream(Array.bytes.of(1, 2, 3));
		Assert.read(in, 1, 2, 3);
		in.reset();
		Assert.read(in, ByteProvider.of(1, 2));
		in.reset();
		Assert.assertion(() -> Assert.read(in, 1, 2, 3, 4));
		in.reset();
		Assert.assertion(() -> Assert.read(in, ByteProvider.of(1, 2, 4)));
	}

	@Test
	public void testExists() throws IOException {
		helper = FileTestHelper.builder().root("a").file("b", "bbb").build();
		Assert.exists(helper.root, true);
		Assert.exists(helper.path("b"), true);
		Assert.exists(helper.path("c"), false);
		Assert.assertion(() -> Assert.exists(helper.path("b"), false));
		Assert.assertion(() -> Assert.exists(helper.path("c"), true));
		Assert.dir(helper.root, true);
		Assert.dir(helper.path("b"), false);
		Assert.dir(helper.path("c"), false);
		Assert.assertion(() -> Assert.dir(helper.root, false));
		Assert.assertion(() -> Assert.dir(helper.path("c"), true));
	}

	@Test
	public void testDir() throws IOException {
		helper = FileTestHelper.builder().root("a").dir("a/0/d0") //
			.file("a/0/f0", "xxxxxx") //
			.file("a/0/f1", "") //
			.dir("a/1/d0").file("a/1/f0", "xxxxxx").file("a/1/f1", "") //
			.dir("a/2/d0").file("a/2/f", "xxxxxx").file("a/2/f1", "") //
			.dir("a/3/d0").file("a/3/f0", "").file("a/3/f1", "") //
			.dir("a/4/d0").file("a/4/f0", "xxxxxx").file("a/4/f1", "x") //
			.dir("a/5/d1").file("a/5/f0", "xxxxxx").file("a/5/f1", "") //
			.build();
		Assert.dir(helper.path("a/0"), helper.path("a/1"));
		Assert.assertion(() -> Assert.dir(helper.path("a/0"), helper.path("a/2")));
		Assert.assertion(() -> Assert.dir(helper.path("a/0"), helper.path("a/3")));
		Assert.assertion(() -> Assert.dir(helper.path("a/0"), helper.path("a/4")));
		Assert.assertion(() -> Assert.dir(helper.path("a/0"), helper.path("a/5")));
	}

	@Test
	public void testFile() throws IOException {
		helper = FileTestHelper.builder().dir("a").dir("b").file("c", "").file("d", "D")
			.file("e", "E").build();
		Assert.assertion(() -> Assert.file(helper.path("a"), helper.path("c")));
		Assert.assertion(() -> Assert.file(helper.path("c"), helper.path("a")));
		Assert.file(helper.path("c"), helper.path("c"));
		Assert.assertion(() -> Assert.file(helper.path("c"), helper.path("d")));
		Assert.assertion(() -> Assert.file(helper.path("d"), helper.path("c")));
		Assert.assertion(() -> Assert.file(helper.path("d"), helper.path("e")));
		Assert.file(helper.path("d"), helper.path("d"));
		Assert.assertion(() -> Assert.file(helper.path("d"), helper.path("e")));
	}

	@Test
	public void testFileBytes() throws IOException {
		helper = FileTestHelper.builder().file("test", "abc").build();
		Assert.file(helper.path("test"), "abc".getBytes());
		Assert.assertion(() -> Assert.file(helper.path("test"), "abd".getBytes()));
	}

	// support

	private static double[] ds(double... doubles) {
		return doubles;
	}

	private static String lines(int count) {
		var b = new StringBuilder();
		for (int i = 0; i < count; i++)
			StringBuilders.format(b, "line%03d%n", i);
		return b.toString();
	}

	private static List<Object> immutableListUntil(int n) {
		return new AbstractList<>() {
			private int i = 0;

			@Override
			public Object get(int index) {
				return null;
			}

			@Override
			public boolean add(Object element) {
				return check(true);
			}

			@Override
			public void add(int index, Object element) {
				check(null);
			}

			@Override
			public boolean addAll(Collection<? extends Object> c) {
				return check(true);
			}

			@Override
			public boolean addAll(int index, Collection<? extends Object> c) {
				return check(true);
			}

			@Override
			public boolean remove(Object o) {
				return check(true);
			}

			@Override
			public Object remove(int index) {
				return check(true);
			}

			@Override
			public boolean removeAll(Collection<?> c) {
				return check(true);
			}

			@Override
			public boolean retainAll(Collection<?> c) {
				return check(true);
			}

			@Override
			public int size() {
				return 1;
			}

			private <T> T check(T response) {
				if (i++ > n) return response;
				throw new UnsupportedOperationException("call " + i);
			}
		};
	}

	private static Map<Object, Object> immutableMapUntil(int n) {
		return new AbstractMap<>() {
			private int i = 0;

			@Override
			public Object put(Object key, Object value) {
				return check(value);
			}

			@Override
			public void putAll(Map<? extends Object, ? extends Object> m) {
				check(null);
			}

			@Override
			public Object remove(Object key) {
				return check(null);
			}

			@Override
			public Set<Entry<Object, Object>> entrySet() {
				return Set.of(new AbstractMap.SimpleEntry<>(null, null));
			}

			private <T> T check(T response) {
				if (i++ > n) return response;
				throw new UnsupportedOperationException("call " + i);
			}
		};
	}
}