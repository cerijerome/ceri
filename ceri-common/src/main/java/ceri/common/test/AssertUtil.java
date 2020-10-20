package ceri.common.test;

import static ceri.common.test.TestUtil.lambdaName;
import static org.hamcrest.CoreMatchers.not;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.ImmutableUtil;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReader;
import ceri.common.data.IntProvider;
import ceri.common.function.ExceptionPredicate;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.FunctionUtil;
import ceri.common.io.IoUtil;
import ceri.common.math.MathUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;

public class AssertUtil {
	public static final int APPROX_PRECISION_DEF = 3;

	private AssertUtil() {}

	/**
	 * Throws a runtime exception. Useful for creating a lambda without the need for a code block.
	 */
	public static <T> T throwIt() {
		throw new RuntimeException("throwIt");
	}

	/**
	 * Throws an i/o exception. Useful for creating a lambda without the need for a code block.
	 */
	public static <T> T throwIo() throws IOException {
		throw new IOException("throwIo");
	}

	/**
	 * Throws the given exception. Useful for creating a lambda without the need for a code block.
	 */
	public static <E extends Exception, T> T throwIt(E exception) throws E {
		throw exception;
	}

	public static void fail() {
		throw new AssertionError();
	}

	public static void fail(String format, Object... args) {
		throw failure(format, args);
	}

	public static AssertionError failure(String format, Object... args) {
		return new AssertionError(String.format(format, args));
	}

	public static void assertFalse(boolean condition) {
		assertFalse(condition, null);
	}

	public static void assertFalse(boolean condition, String message) {
		Assert.assertFalse(message, condition);
	}

	public static void assertTrue(boolean condition) {
		assertTrue(condition, null);
	}

	public static void assertTrue(boolean condition, String message) {
		Assert.assertTrue(message, condition);
	}

	public static <T> void assertSame(T actual, T expected) {
		Assert.assertSame(expected, actual);
	}

	public static <T> void assertNotSame(T actual, T unexpected) {
		Assert.assertNotSame(unexpected, actual);
	}

	public static <T> void assertNull(T actual) {
		Assert.assertNull(actual);
	}

	public static <T> void assertNotNull(T actual) {
		Assert.assertNotNull(actual);
	}

	public static <T> void assertThat(T actual, Matcher<? super T> matcher) {
		assertThat(actual, matcher, "");
	}

	public static <T> void assertThat(T actual, Matcher<? super T> matcher, String reason) {
		MatcherAssert.assertThat(reason, actual, matcher);
	}

	public static <T> void assertEquals(T actual, T expected) {
		assertEquals(actual, expected, null);
	}

	public static <T> void assertEquals(T actual, T expected, String message) {
		Assert.assertEquals(message, expected, actual);
	}

	public static void assertEquals(double actual, double expected, double diff) {
		Assert.assertEquals(expected, actual, diff);
	}

	public static <T> void assertNotEquals(T actual, T expected) {
		assertNotEquals(actual, expected, null);
	}

	public static <T> void assertNotEquals(T actual, T unexpected, String message) {
		Assert.assertNotEquals(message, unexpected, actual);
	}

	/**
	 * Calls private constructor. Useful for code coverage of utility classes.
	 */
	public static void assertPrivateConstructor(Class<?> cls) {
		try {
			Constructor<?> constructor = cls.getDeclaredConstructor();
			assertEquals(Modifier.isPrivate(constructor.getModifiers()), true,
				"Constructor is not private");
			constructor.setAccessible(true);
			constructor.newInstance();
			constructor.setAccessible(false);
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException
			| InstantiationException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * Check an assertion error is thrown. Useful for checking assertXxx methods.
	 */
	public static void assertAssertion(ExceptionRunnable<Exception> runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			// Failure
		} catch (AssertionError e) {
			// Success
			return;
		}
		throw new AssertionError("Expected to assert");
	}

	/**
	 * Checks all objects are not equal to the first given object.
	 */
	@SafeVarargs
	public static <T> void assertAllNotEqual(T t0, T... ts) {
		for (T t : ts)
			assertNotEquals(t0, t);
	}

	/**
	 * Checks a value, with failure message.
	 */
	public static <E extends Exception, T> void assertValue(T t, ExceptionPredicate<E, T> test)
		throws E {
		if (!test.test(t)) throw failure("No match: %s", String.valueOf(t).trim());
	}

	/**
	 * Checks a double value is NaN.
	 */
	public static void assertNaN(double value) {
		assertEquals(Double.isNaN(value), true, "Expected NaN: " + value);
	}

	/**
	 * Checks a double value is correct to 3 decimal places.
	 */
	public static void assertApprox(double actual, double expected) {
		assertApprox(actual, expected, APPROX_PRECISION_DEF);
	}

	/**
	 * Checks a double value is correct to given number of digits after the decimal separator.
	 */
	public static void assertApprox(double actual, double expected, int precision) {
		if (!Double.isFinite(expected) || !Double.isFinite(actual)) {
			assertEquals(actual, expected);
		} else {
			double approxValue = MathUtil.round(precision, actual);
			double approxExpected = MathUtil.round(precision, expected);
			assertEquals(approxValue, approxExpected);
		}
	}

	/**
	 * Convenience method to check byte value.
	 */
	public static void assertByte(byte value, int expected) {
		assertEquals(value, (byte) expected);
	}

	/**
	 * Convenience method to check short value.
	 */
	public static void assertShort(short value, int expected) {
		assertEquals(value, (short) expected);
	}

	/**
	 * Checks a value is within given range, with detailed failure information if not.
	 */
	public static void assertRange(long value, long minInclusive, long maxInclusive) {
		assertEquals(value >= minInclusive, true,
			"Expected >= " + minInclusive + " but was " + value);
		assertEquals(value <= maxInclusive, true,
			"Expected <= " + maxInclusive + " but was " + value);
	}

	/**
	 * Checks a value is within given range, with detailed failure information if not.
	 */
	public static void assertRange(double value, double minInclusive, double maxExclusive) {
		assertEquals(value >= minInclusive, true,
			"Expected >= " + minInclusive + " but was " + value);
		assertEquals(value < maxExclusive, true,
			"Expected < " + maxExclusive + " but was " + value);
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	private static void assertArrayObject(Object lhs, Object rhs) {
		assertIsArray(lhs);
		assertIsArray(rhs);
		assertSize("Array size", Array.getLength(lhs), Array.getLength(rhs));
		assertArrayObject(lhs, 0, rhs, 0, Array.getLength(lhs));
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArrayObject(Object lhs, int lhsOffset, Object rhs, int rhsOffset,
		int len) {
		assertIsArray(lhs);
		assertIsArray(rhs);
		int lhsLen = Array.getLength(lhs);
		int rhsLen = Array.getLength(rhs);
		for (int i = 0; i < len; i++) {
			boolean hasLhs = lhsOffset + i < lhsLen;
			Object lhsVal = hasLhs ? Array.get(lhs, lhsOffset + i) : null;
			boolean hasRhs = rhsOffset + i < rhsLen;
			Object rhsVal = hasRhs ? Array.get(rhs, rhsOffset + i) : null;
			assertIndex(lhsOffset + i, lhsVal, hasLhs, rhsVal, hasRhs);
		}
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	@SafeVarargs
	public static <T> void assertArray(T[] array, T... expected) {
		assertArrayObject(array, expected);
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(boolean[] array, boolean... expected) {
		assertArrayObject(array, expected);
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(char[] array, char... expected) {
		assertArrayObject(array, expected);
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(byte[] array, byte[] expected) {
		assertArrayObject(array, expected);
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(byte[] array, int... values) {
		assertArray(array, ArrayUtil.bytes(values));
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(short[] array, short[] expected) {
		assertArrayObject(array, expected);
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(short[] array, int... values) {
		assertArray(array, ArrayUtil.shorts(values));
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(int[] array, int... expected) {
		assertArrayObject(array, expected);
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(long[] array, long... expected) {
		assertArrayObject(array, expected);
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(float[] array, float... expected) {
		assertArrayObject(array, expected);
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(double[] array, double... expected) {
		assertArrayObject(array, expected);
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(ByteProvider array, byte[] expected) {
		assertArray(array.copy(0), expected);
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(ByteProvider array, int... values) {
		assertArray(array, ArrayUtil.bytes(values));
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(IntProvider array, int... values) {
		assertArray(array.copy(0), values);
	}

	/**
	 * Checks two lists are equal, with specific failure information if not.
	 */
	public static <T> void assertList(List<? extends T> lhs, List<? extends T> rhs) {
		assertList(lhs, 0, rhs, 0, lhs.size());
		assertSize("List size", lhs.size(), rhs.size());
	}

	/**
	 * Checks two lists are equal, with specific failure information if not.
	 */
	public static <T> void assertList(List<? extends T> lhs, int lhsOffset, List<? extends T> rhs,
		int rhsOffset, int len) {
		int lhsLen = lhs.size();
		int rhsLen = rhs.size();
		for (int i = 0; i < len; i++) {
			boolean hasLhs = lhsOffset + i < lhsLen;
			T lhsVal = hasLhs ? lhs.get(lhsOffset + i) : null;
			boolean hasRhs = rhsOffset + i < rhsLen;
			T rhsVal = hasRhs ? rhs.get(rhsOffset + i) : null;
			assertIndex(lhsOffset + i, lhsVal, hasLhs, rhsVal, hasRhs);
		}
	}

	private static <T> void assertIndex(int i, T lhs, boolean hasLhs, T rhs, boolean hasRhs) {
		if (!hasLhs && !hasRhs) throw failure("Nothing at index %d", i);
		if (!hasLhs) throw failure("Nothing at index %d, expected %s", i, toString(rhs));
		if (!hasRhs) throw failure("Unexpected item at index %d: %s", i, toString(lhs));
		if (!Objects.deepEquals(lhs, rhs)) throw failExpected(lhs, rhs, "Index %d", i);
	}

	private static <T> AssertionError failExpected(T actual, T expected, String format,
		Object... args) {
		String msg = StringUtil.format(format, args) + '\n';
		return failure("%sExpected: %s\n  actual: %s", msg, toString(expected), toString(actual));
	}

	private static <T> String toString(T t) {
		if (ReflectUtil.instanceOfAny(t, Byte.class, Short.class, Integer.class, Long.class))
			return String.format("%1$d (0x%1$02x)", t);
		return String.valueOf(t);
	}

	/**
	 * Checks collection contains exactly given elements in any order, with specific failure
	 * information if not.
	 */
	@SafeVarargs
	public static <T> void assertCollection(T[] lhs, T... ts) {
		assertCollection(Arrays.asList(lhs), ts);
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(boolean[] lhs, boolean... expected) {
		assertCollection(ArrayUtil.booleanList(lhs), ArrayUtil.booleanList(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(byte[] lhs, byte... expected) {
		assertCollection(ArrayUtil.byteList(lhs), ArrayUtil.byteList(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(byte[] lhs, int... values) {
		assertCollection(lhs, ArrayUtil.bytes(values));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(char[] lhs, char... expected) {
		assertCollection(ArrayUtil.charList(lhs), ArrayUtil.charList(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(char[] lhs, int... expected) {
		assertCollection(lhs, ArrayUtil.chars(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(short[] lhs, short... expected) {
		assertCollection(ArrayUtil.shortList(lhs), ArrayUtil.shortList(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(short[] lhs, int... expected) {
		assertCollection(lhs, ArrayUtil.shorts(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(int[] lhs, int... expected) {
		assertCollection(ArrayUtil.intList(lhs), ArrayUtil.intList(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(long[] lhs, long... expected) {
		assertCollection(ArrayUtil.longList(lhs), ArrayUtil.longList(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(float[] lhs, float... expected) {
		assertCollection(ArrayUtil.floatList(lhs), ArrayUtil.floatList(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(float[] lhs, double... expected) {
		assertCollection(lhs, ArrayUtil.floats(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(double[] lhs, double... expected) {
		assertCollection(ArrayUtil.doubleList(lhs), ArrayUtil.doubleList(expected));
	}

	/**
	 * Checks exact items in any order, with specific failure information if not.
	 */
	@SafeVarargs
	public static <T> void assertCollection(Collection<T> lhs, T... ts) {
		assertCollection(lhs, Arrays.asList(ts));
	}

	/**
	 * Checks two collections have equal elements, with specific failure information if not.
	 */
	public static <T> void assertCollection(Collection<? extends T> lhs,
		Collection<? extends T> rhs) {
		int i = 0;
		for (T t : lhs) {
			assertEquals(rhs.contains(t), true, "Unexpected element at position " + i + ": " + t);
			i++;
		}
		for (T t : rhs)
			assertEquals(lhs.contains(t), true, "Missing element: " + t);
		assertEquals(lhs.size(), rhs.size(), "Collection size");
	}

	/**
	 * Checks iterable type against given list of items, with specific failure information if not.
	 */
	@SafeVarargs
	public static <T> void assertIterable(Iterable<T> lhs, T... ts) {
		assertIterable(lhs, Arrays.asList(ts));
	}

	/**
	 * Checks two iterable types have equal elements, with specific failure information if not.
	 * Useful for testing Collections.unmodifiableXXX as they don't implement equals().
	 */
	public static <T> void assertIterable(Iterable<T> lhs, Iterable<T> rhs) {
		List<T> lhsC = new ArrayList<>();
		for (T t : lhs)
			lhsC.add(t);
		List<T> rhsC = new ArrayList<>();
		for (T t : rhs)
			rhsC.add(t);
		assertList(lhsC, rhsC);
	}

	private static void assertSize(String message, long lhsSize, long rhsSize) {
		assertEquals(lhsSize, rhsSize, message);
	}

	private static void assertIsArray(Object array) {
		assertEquals(array.getClass().isArray(), true,
			"Expected an array but was " + array.getClass());
	}

	public static <K, V> void assertMap(Map<K, V> subject) {
		assertEquals(subject, Map.of());
	}

	public static <K, V> void assertMap(Map<K, V> subject, K k, V v) {
		assertEquals(subject, ImmutableUtil.asMap(k, v));
	}

	public static <K, V> void assertMap(Map<K, V> subject, K k0, V v0, K k1, V v1) {
		assertEquals(subject, ImmutableUtil.asMap(k0, v0, k1, v1));
	}

	public static <K, V> void assertMap(Map<K, V> subject, K k0, V v0, K k1, V v1, K k2, V v2) {
		assertEquals(subject, ImmutableUtil.asMap(k0, v0, k1, v1, k2, v2));
	}

	public static <K, V> void assertMap(Map<K, V> subject, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
		V v3) {
		assertEquals(subject, ImmutableUtil.asMap(k0, v0, k1, v1, k2, v2, k3, v3));
	}

	public static <K, V> void assertMap(Map<K, V> subject, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
		V v3, K k4, V v4) {
		assertEquals(subject, ImmutableUtil.asMap(k0, v0, k1, v1, k2, v2, k3, v3, k4, v4));
	}

	@SafeVarargs
	public static <T> void assertStream(Stream<T> stream, T... ts) {
		assertArray(stream.toArray(), ts);
	}

	public static void assertStream(IntStream stream, int... is) {
		assertArray(stream.toArray(), is);
	}

	public static void assertStream(LongStream stream, long... ls) {
		assertArray(stream.toArray(), ls);
	}

	/**
	 * Verifies throwable super class.
	 */
	public static void assertThrowable(Throwable t, Class<? extends Throwable> superCls) {
		assertThrowable(t, superCls, (Predicate<String>) null);
	}

	/**
	 * Verifies throwable message.
	 */
	public static void assertThrowable(Throwable t, String message) {
		assertThrowable(t, null, message);
	}

	/**
	 * Verifies throwable message.
	 */
	public static void assertThrowable(Throwable t, Predicate<String> messageTest) {
		assertThrowable(t, null, messageTest);
	}

	/**
	 * Verifies throwable super class and message.
	 */
	public static void assertThrowable(Throwable t, Class<? extends Throwable> superCls,
		String msg) {
		assertThrowable(t, superCls, equalsPredicate(msg));
	}

	/**
	 * Verifies throwable super class and message.
	 */
	@SuppressWarnings("null")
	public static void assertThrowable(Throwable t, Class<? extends Throwable> superCls,
		Predicate<String> messageTest) {
		if (t == null && superCls == null && messageTest == null) return;
		assertNotNull(t);
		if (superCls != null && !superCls.isAssignableFrom(t.getClass()))
			throw failure("Expected %s: %s", superCls.getName(), t.getClass().getName());
		if (messageTest == null) return;
		if (!messageTest.test(t.getMessage()))
			throw failure("Unmatched message %s: %s", lambdaName(messageTest), t.getMessage());
	}

	/**
	 * Use this for more flexibility than adding @Test(expected=...)
	 */
	public static void assertThrown(ExceptionRunnable<Exception> runnable) {
		assertThrown(Exception.class, runnable);
	}

	/**
	 * Use this for more flexibility than adding @Test(expected=...)
	 */
	public static void assertThrown(Class<? extends Throwable> exceptionCls,
		ExceptionRunnable<?> runnable) {
		assertThrown(exceptionCls, (Predicate<String>) null, runnable);
	}

	/**
	 * Use this for more flexibility than adding @Test(expected=...)
	 */
	public static void assertThrown(String message, ExceptionRunnable<Exception> runnable) {
		assertThrown(Exception.class, message, runnable);
	}

	/**
	 * Use this for more flexibility than adding @Test(expected=...)
	 */
	public static void assertThrown(Predicate<String> messageTest,
		ExceptionRunnable<Exception> runnable) {
		assertThrown(Exception.class, messageTest, runnable);
	}

	/**
	 * Tests if an exception is thrown with given message.
	 */
	public static void assertThrown(Class<? extends Throwable> superCls, String message,
		ExceptionRunnable<?> runnable) {
		assertThrown(superCls, equalsPredicate(message), runnable);
	}

	/**
	 * Tests if an exception is thrown with given message.
	 */
	public static void assertThrown(Class<? extends Throwable> superCls, Predicate<String> msgTest,
		ExceptionRunnable<?> runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			assertThrowable(t, superCls, msgTest);
			return;
		}
		throw failure("Nothing thrown, expected: %s", superCls.getName());
	}

	/**
	 * Checks regex not found against the string.
	 */
	public static void assertNotFound(CharSequence actual, String pattern, Object... objs) {
		assertNotFound(actual, RegexUtil.compile(pattern, objs));
	}

	/**
	 * Checks regex not found against the string.
	 */
	public static void assertNotFound(CharSequence actual, Pattern pattern) {
		assertThat(String.valueOf(actual), not(RegexMatcher.find(pattern)));
	}

	/**
	 * Checks regex find against the string.
	 */
	public static void assertFind(CharSequence actual, String pattern, Object... objs) {
		assertFind(actual, RegexUtil.compile(pattern, objs));
	}

	/**
	 * Checks regex find against the string.
	 */
	public static void assertFind(CharSequence actual, Pattern pattern) {
		assertThat(String.valueOf(actual), RegexMatcher.find(pattern));
	}

	/**
	 * Checks regex match against the string.
	 */
	public static void assertMatch(CharSequence actual, String pattern, Object... objs) {
		assertMatch(actual, RegexUtil.compile(pattern, objs));
	}

	/**
	 * Checks regex match against the string.
	 */
	public static void assertMatch(CharSequence actual, Pattern pattern) {
		assertThat(String.valueOf(actual), RegexMatcher.match(pattern));
	}

	/**
	 * Checks regex does not match against the string.
	 */
	public static void assertNoMatch(CharSequence actual, String pattern, Object... objs) {
		assertNoMatch(actual, RegexUtil.compile(pattern, objs));
	}

	/**
	 * Checks regex does not match against the string.
	 */
	public static void assertNoMatch(CharSequence actual, Pattern pattern) {
		assertThat(String.valueOf(actual), not(RegexMatcher.match(pattern)));
	}

	/**
	 * Check ascii read from byte reader.
	 */
	public static void assertAscii(ByteReader reader, String s) {
		var actual = reader.readAscii(s.length());
		assertEquals(actual, s);
	}

	/**
	 * Check bytes read from input stream.
	 */
	public static void assertRead(InputStream in, ByteProvider bytes) throws IOException {
		assertRead(in, bytes.copy(0));
	}

	/**
	 * Check bytes read from input stream.
	 */
	public static void assertRead(InputStream in, int... bytes) throws IOException {
		assertRead(in, ArrayUtil.bytes(bytes));
	}

	/**
	 * Check bytes read from input stream.
	 */
	public static void assertRead(InputStream in, byte[] bytes) throws IOException {
		assertArray(in.readNBytes(bytes.length), bytes);
	}

	/**
	 * Check if file exists.
	 */
	public static void assertExists(Path path, boolean exists) {
		assertEquals(Files.exists(path), exists, "Path exists: " + path);
	}

	/**
	 * Check if file exists.
	 */
	public static void assertDir(Path path, boolean isDir) {
		assertEquals(Files.isDirectory(path), isDir, "Path is a directory: " + path);
	}

	/**
	 * Checks contents of two directories are equal, with specific failure information if not.
	 */
	public static void assertDir(Path lhsDir, Path rhsDir) throws IOException {
		List<Path> lhsPathsRelative = IoUtil.pathsRelative(lhsDir);
		List<Path> rhsPathsRelative = IoUtil.pathsRelative(rhsDir);
		assertCollection(lhsPathsRelative, rhsPathsRelative);
		for (Path path : lhsPathsRelative) {
			Path lhsFile = lhsDir.resolve(path);
			Path rhsFile = rhsDir.resolve(path);
			boolean lhsIsDir = Files.isDirectory(lhsFile);
			boolean rhsIsDir = Files.isDirectory(rhsFile);
			assertEquals(lhsIsDir, rhsIsDir, lhsFile + " is directory");
			if (!rhsIsDir) assertFile(lhsFile, rhsFile);
		}
	}

	/**
	 * Checks contents of two files are equal, with specific failure information if not.
	 */
	public static void assertFile(Path actual, Path expected) throws IOException {
		assertEquals(Files.size(actual), Files.size(expected), "File size");
		long pos = Files.mismatch(actual, expected);
		if (pos >= 0) throw failure("Byte mismatch at index %d", pos);
	}

	/**
	 * Checks contents of the files matches bytes, with specific failure information if not.
	 */
	public static void assertFile(Path actual, int... bytes) throws IOException {
		assertFile(actual, ByteArray.Immutable.wrap(bytes));
	}

	/**
	 * Checks contents of the files matches bytes, with specific failure information if not.
	 */
	public static void assertFile(Path actual, byte[] bytes) throws IOException {
		assertFile(actual, ByteArray.Immutable.wrap(bytes));
	}

	/**
	 * Checks contents of the files matches bytes, with specific failure information if not.
	 */
	public static void assertFile(Path actual, ByteProvider byteProvider) throws IOException {
		assertEquals(Files.size(actual), (long) byteProvider.length(), "File size");
		byte[] actualBytes = Files.readAllBytes(actual);
		for (int i = 0; i < actualBytes.length; i++)
			if (actualBytes[i] != byteProvider.getByte(i))
				throw failure("Byte mismatch at index %d", i);
	}

	/**
	 * Checks the paths are the same.
	 */
	public static void assertPath(Path actual, String expected, String... more) {
		Path expectedPath = IoUtil.newPath(actual, expected, more);
		assertEquals(actual, expectedPath);
	}

	/**
	 * Assert a collection of paths in unspecific order, using the first path's file system.
	 */
	public static void assertPaths(Collection<Path> actual, String... paths) {
		if (actual.isEmpty()) {
			assertEquals(paths.length, 0, "Path count");
			return;
		}
		@SuppressWarnings("resource")
		FileSystem fs = actual.iterator().next().getFileSystem();
		List<Path> expected = Stream.of(paths).map(fs::getPath).collect(Collectors.toList());
		assertCollection(actual, expected);
	}

	/**
	 * Assert paths relative to file helper.
	 */
	public static void assertHelperPaths(Collection<Path> actual, FileTestHelper helper,
		String... paths) {
		List<Path> expected = Stream.of(paths).map(helper::path).collect(Collectors.toList());
		assertCollection(actual, expected);
	}

	private static Predicate<String> equalsPredicate(String s) {
		return FunctionUtil.named(Predicate.isEqual(s), "\"" + s + "\"");
	}

}
