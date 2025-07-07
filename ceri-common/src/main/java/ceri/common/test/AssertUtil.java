package ceri.common.test;

import static ceri.common.reflect.ReflectUtil.hashId;
import static ceri.common.text.StringUtil.EOL;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.ImmutableUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReader;
import ceri.common.data.ByteUtil;
import ceri.common.data.IntProvider;
import ceri.common.data.LongProvider;
import ceri.common.function.Excepts;
import ceri.common.io.IoUtil;
import ceri.common.math.MathUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;

public class AssertUtil {
	public static final int APPROX_PRECISION_DEF = 3;

	private AssertUtil() {}

	private static interface ItemAssert {
		static ItemAssert DEEP_EQUALS = AssertUtil::assertDeepEquals;

		static ItemAssert doubleEquals(double diff) {
			return (lhs, rhs, format, args) -> assertEquals((Double) lhs, (Double) rhs, diff,
				format, args);
		}

		static ItemAssert doubleApprox(int precision) {
			return (lhs, rhs, format, args) -> assertApprox((Double) lhs, (Double) rhs, precision,
				format, args);
		}

		void assertItem(Object lhs, Object rhs, String format, Object... args);
	}

	/**
	 * Throws a runtime exception. Useful for creating a lambda without the need for a code block.
	 */
	public static <T> T throwRuntime() {
		throw new RuntimeException("throwRuntime");
	}

	/**
	 * Throws an i/o exception. Useful for creating a lambda without the need for a code block.
	 */
	public static <T> T throwIo() throws IOException {
		throw new IOException("throwIo");
	}

	/**
	 * Throws an i/o exception. Useful for creating a lambda without the need for a code block.
	 */
	public static <T> T throwInterrupted() throws InterruptedException {
		throw new InterruptedException("throwInterrupted");
	}

	/**
	 * Throws the given exception. Useful for creating a lambda without the need for a code block.
	 */
	public static <E extends Exception, T> T throwIt(E exception) throws E {
		throw exception;
	}

	public static void fail() {
		throw new AssertionError("Failed");
	}

	public static void fail(Throwable t) {
		throw new AssertionError("Failed", t);
	}

	public static void fail(String format, Object... args) {
		throw failure(format, args);
	}

	public static void fail(Throwable t, String format, Object... args) {
		throw failure(t, format, args);
	}

	public static AssertionError failure(String format, Object... args) {
		return failure(null, format, args);
	}

	public static AssertionError failure(Throwable t, String format, Object... args) {
		return new AssertionError(StringUtil.format(format, args), t);
	}

	public static void assertFalse(boolean condition) {
		assertFalse(condition, null);
	}

	public static void assertFalse(boolean condition, String format, Object... args) {
		if (!condition) return;
		String message = StringUtil.format(format, args);
		throw failure(message.isEmpty() ? "Expected false" : message);
	}

	public static void assertTrue(boolean condition) {
		assertTrue(condition, null);
	}

	public static void assertTrue(boolean condition, String format, Object... args) {
		if (condition) return;
		String message = StringUtil.format(format, args);
		throw failure(message.isEmpty() ? "Expected true" : message);
	}

	public static <T> T assertInstance(Object actual, Class<T> expected) {
		if (expected.isInstance(actual)) return BasicUtil.unchecked(actual);
		throw failure("Expected instance: %s\n           actual: %s", ReflectUtil.name(expected),
			ReflectUtil.className(actual));
	}

	public static <T> void assertSame(T actual, T expected) {
		assertSame(actual, expected, null);
	}

	public static <T> void assertSame(T actual, T expected, String format, Object... args) {
		if (expected == actual) return;
		throw failure("%sExpected same: %s (%s)\n       actual: %s (%s)", nl(format, args),
			expected, hashId(expected), actual, hashId(actual));
	}

	public static <T> void assertNotSame(T actual, T unexpected) {
		assertNotSame(actual, unexpected, null);
	}

	public static <T> void assertNotSame(T actual, T unexpected, String format, Object... args) {
		if (unexpected != actual) return;
		throw failure("%sValues are the same: %s (%s)", nl(format, args), actual, hashId(actual));
	}

	public static <T> void assertNull(T actual) {
		assertNull(actual, null);
	}

	public static <T> void assertNull(T actual, String format, Object... args) {
		if (actual != null) throw failure("%sExpected null: %s", nl(format, args), actual);
	}

	public static <T> void assertNotNull(T actual) {
		assertNotNull(actual, null);
	}

	public static <T> void assertNotNull(T actual, String format, Object... args) {
		if (actual != null) return;
		String message = StringUtil.format(format, args);
		throw failure(message.isEmpty() ? "Value is null" : message);
	}

	public static <T> void assertEquals(T actual, T expected) {
		assertEquals(actual, expected, null);
	}

	public static <T> void assertEquals(T actual, T expected, String format, Object... args) {
		if (Objects.equals(expected, actual)) return;
		throw failure("%sExpected: %s\n  actual: %s", nl(format, args), str(expected), str(actual));
	}

	public static void assertEquals(double actual, double expected, double diff) {
		assertEquals(actual, expected, diff, null);
	}

	public static void assertEquals(double actual, double expected, double diff, String format,
		Object... args) {
		if (Double.compare(expected, actual) == 0) return;
		if ((Math.abs(expected - actual) <= diff)) return;
		throw failure("%sExpected: %s (±%s)\n  actual: %s", nl(format, args), expected, diff,
			actual);
	}

	public static <T> void assertNotEquals(T actual, T unexpected) {
		assertNotEquals(actual, unexpected, null);
	}

	public static <T> void assertNotEquals(T actual, T unexpected, String format, Object... args) {
		if (!Objects.equals(unexpected, actual)) return;
		throw failure("%sUnexpected: %s", nl(format, args), str(actual));
	}

	/**
	 * Calls private constructor. Useful for code coverage of utility classes.
	 */
	public static void assertPrivateConstructor(Class<?> cls) {
		try {
			Constructor<?> constructor = cls.getDeclaredConstructor();
			assertTrue(Modifier.isPrivate(constructor.getModifiers()),
				"Constructor is not private: %s()", cls.getSimpleName());
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
	public static void assertAssertion(Excepts.Runnable<Exception> runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			// Failure
			throw new AssertionError("Expected to assert", e);
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
	public static <E extends Exception, T> void assertValue(T t, Excepts.Predicate<E, T> test)
		throws E {
		if (!test.test(t)) throw failure("No match: %s", String.valueOf(t).trim());
	}

	/**
	 * Checks an optional value equals the given value.
	 */
	public static <T> void assertOptional(Optional<T> actual, T expected) {
		assertOptional(actual, expected, null);
	}

	/**
	 * Checks an optional value equals the given value.
	 */
	public static <T> void assertOptional(Optional<T> actual, T expected, String format,
		Object... args) {
		assertEquals(actual, Optional.ofNullable(expected), format, args);
	}

	/**
	 * Checks a double value is NaN.
	 */
	public static void assertNaN(double value) {
		assertTrue(Double.isNaN(value), "Expected NaN: %s", value);
	}

	/**
	 * Checks a double value is correct to 3 decimal places.
	 */
	public static void assertApprox(double actual, double expected) {
		assertApprox(actual, expected, null);
	}

	/**
	 * Checks a double value is correct to 3 decimal places.
	 */
	public static void assertApprox(double actual, double expected, String format, Object... args) {
		assertApprox(actual, expected, APPROX_PRECISION_DEF, format, args);
	}

	/**
	 * Checks a double value is correct to given number of digits after the decimal separator.
	 */
	public static void assertApprox(double actual, double expected, int precision) {
		assertApprox(actual, expected, precision, null);
	}

	/**
	 * Checks a double value is correct to given number of digits after the decimal separator.
	 */
	public static void assertApprox(double actual, double expected, int precision, String format,
		Object... args) {
		if (!Double.isFinite(expected) || !Double.isFinite(actual)) {
			assertEquals(actual, expected, format, args);
		} else {
			double approxValue = MathUtil.round(precision, actual);
			double approxExpected = MathUtil.round(precision, expected);
			assertEquals(approxValue, approxExpected, format, args);
		}
	}

	/**
	 * Convenience method to check byte value.
	 */
	public static void assertByte(byte value, int expected) {
		assertEquals(value, (byte) expected);
	}

	/**
	 * Convenience method to check byte value.
	 */
	public static void assertByte(byte value, int expected, String format, Object... args) {
		assertEquals(value, (byte) expected, format, args);
	}

	/**
	 * Convenience method to check short value.
	 */
	public static void assertShort(short value, int expected) {
		assertEquals(value, (short) expected);
	}

	/**
	 * Convenience method to check short value.
	 */
	public static void assertShort(short value, int expected, String format, Object... args) {
		assertEquals(value, (short) expected, format, args);
	}

	/**
	 * Convenience methods to check presence of a mask.
	 */
	public static void assertMask(int value, int mask) {
		assertEquals(value & mask, mask, "Mask not present 0x%x", mask);
	}

	/**
	 * Convenience methods to check presence of a mask.
	 */
	public static void assertMask(long value, long mask) {
		assertEquals(value & mask, mask, "Mask not present 0x%x", mask);
	}

	/**
	 * Checks a value is within given range, with detailed failure information if not.
	 */
	public static void assertRange(int value, int minInclusive, int maxInclusive) {
		assertRange(value, minInclusive, maxInclusive, null);
	}

	/**
	 * Checks a value is within given range, with detailed failure information if not.
	 */
	public static void assertRange(int value, int minInclusive, int maxInclusive, String format,
		Object... args) {
		if (value < minInclusive || value > maxInclusive)
			throw failure("%sExpected: %s <= value <= %s\n  actual:    %s", nl(format, args),
				str(minInclusive), str(maxInclusive), str(value));
	}

	/**
	 * Checks a value is within given range, with detailed failure information if not.
	 */
	public static void assertRange(long value, long minInclusive, long maxInclusive) {
		assertRange(value, minInclusive, maxInclusive, null);
	}

	/**
	 * Checks a value is within given range, with detailed failure information if not.
	 */
	public static void assertRange(long value, long minInclusive, long maxInclusive, String format,
		Object... args) {
		if (value < minInclusive || value > maxInclusive)
			throw failure("%sExpected: %s <= value <= %s\n  actual:    %s", nl(format, args),
				str(minInclusive), str(maxInclusive), str(value));
	}

	/**
	 * Checks a value is within given range, with detailed failure information if not.
	 */
	public static void assertRange(double value, double minInclusive, double maxExclusive) {
		assertRange(value, minInclusive, maxExclusive, null);
	}

	/**
	 * Checks a value is within given range, with detailed failure information if not.
	 */
	public static void assertRange(double value, double minInclusive, double maxExclusive,
		String format, Object... args) {
		if (value < minInclusive || value >= maxExclusive)
			throw failure("%sExpected: %s <= value < %s\n  actual:    %s", nl(format, args),
				str(minInclusive), str(maxExclusive), str(value));
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArrayObject(Object lhs, int lhsOffset, Object rhs, int rhsOffset,
		int len) {
		assertArrayObject(lhs, lhsOffset, rhs, rhsOffset, len, ItemAssert.DEEP_EQUALS);
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	private static void assertArrayObject(Object lhs, int lhsOffset, Object rhs, int rhsOffset,
		int len, ItemAssert itemAssert) {
		assertIsArray(lhs);
		assertIsArray(rhs);
		int lhsLen = Array.getLength(lhs);
		int rhsLen = Array.getLength(rhs);
		for (int i = 0; i < len; i++) {
			boolean hasLhs = lhsOffset + i < lhsLen;
			Object lhsVal = hasLhs ? Array.get(lhs, lhsOffset + i) : null;
			boolean hasRhs = rhsOffset + i < rhsLen;
			Object rhsVal = hasRhs ? Array.get(rhs, rhsOffset + i) : null;
			assertIndex(lhsOffset + i, lhsVal, hasLhs, rhsVal, hasRhs, itemAssert);
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
	 * Checks two arrays are equal with diff, with specific failure information if not.
	 */
	public static void assertArray(double diff, double[] array, double... expected) {
		assertArrayObject(array, expected, ItemAssert.doubleEquals(diff));
	}

	/**
	 * Checks two arrays are equal within precision, with specific failure information if not.
	 */
	public static void assertApproxArray(double[] array, double... expected) {
		assertApproxArray(APPROX_PRECISION_DEF, array, expected);
	}

	/**
	 * Checks two arrays are equal within precision, with specific failure information if not.
	 */
	public static void assertApproxArray(int precision, double[] array, double... expected) {
		assertArrayObject(array, expected, ItemAssert.doubleApprox(precision));
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
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(LongProvider array, long... values) {
		assertArray(array.copy(0), values);
	}

	/**
	 * Checks two lists are equal, with specific failure information if not.
	 */
	public static <T> void assertList(List<? extends T> lhs, List<? extends T> rhs) {
		assertList(lhs, 0, rhs, 0, lhs.size());
		assertEquals(lhs.size(), rhs.size(), "List size");
	}

	/**
	 * Checks two lists are equal, with specific failure information if not.
	 */
	public static <T> void assertList(List<? extends T> lhs, int lhsOffset, List<? extends T> rhs,
		int rhsOffset, int len) {
		assertList(lhs, lhsOffset, rhs, rhsOffset, len, ItemAssert.DEEP_EQUALS);
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
		assertCollection(ArrayUtil.boolList(lhs), ArrayUtil.boolList(expected));
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
			if (!rhs.contains(t)) throw failure("Unexpected element at position %d: %s", i, str(t));
			i++;
		}
		for (T t : rhs)
			if (!lhs.contains(t)) throw failure("Missing element: %s", str(t));
		assertEquals(lhs.size(), rhs.size(), "Invalid collection size");
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

	/**
	 * Checks iterable types against consumers for consecutive values. The consumers are expected to
	 * be assertions. Fails if the number of consumers does not match the number of items.
	 */
	@SafeVarargs
	public static <E extends Exception, T> void assertConsume(Iterable<T> iterable,
		Excepts.Consumer<E, T>... consumers) throws E {
		int i = 0;
		var iter = iterable.iterator();
		while (iter.hasNext()) {
			if (i >= consumers.length) throw failure("No consumers from index %d", i);
			consumers[i++].accept(iter.next());
		}
		if (i < consumers.length) throw failure("Expected %d items: %d", consumers.length, i);
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

	public static void assertStream(DoubleStream stream, double... ds) {
		assertArray(stream.toArray(), ds);
	}

	public static void assertBuffer(ByteBuffer buffer, int... bytes) {
		assertArray(ByteUtil.bytes(buffer), bytes);
	}

	/**
	 * Verifies throwable super class.
	 */
	public static void assertThrowable(Throwable t, Class<? extends Throwable> superCls) {
		assertThrowable(t, superCls, (Consumer<Throwable>) null);
	}

	/**
	 * Verifies throwable message.
	 */
	public static void assertThrowable(Throwable t, String regex, Object... args) {
		assertThrowable(t, Throwable.class, regex, args);
	}

	/**
	 * Verifies throwable message.
	 */
	public static void assertThrowable(Throwable t, Consumer<Throwable> messageTest) {
		assertThrowable(t, null, messageTest);
	}

	/**
	 * Verifies throwable super class and message.
	 */
	public static void assertThrowable(Throwable t, Class<? extends Throwable> superCls,
		String regex, Object... args) {
		assertThrowable(t, superCls, e -> assertMatch(e.getMessage(), regex, args));
	}

	/**
	 * Verifies throwable type.
	 */
	@SuppressWarnings("null")
	public static <E extends Throwable> void assertThrowable(Throwable t, Class<E> superCls,
		Consumer<? super E> test) {
		if (t == null && superCls == null && test == null) return;
		assertNotNull(t);
		if (superCls != null && !superCls.isAssignableFrom(t.getClass()))
			throw failure("Expected %s: %s", superCls.getName(), t.getClass().getName());
		if (test != null) test.accept(BasicUtil.unchecked(t));
	}

	/**
	 * Use this for more flexibility than adding @Test(expected=...)
	 */
	public static void assertThrown(Excepts.Runnable<Exception> runnable) {
		assertThrown(Exception.class, runnable);
	}

	/**
	 * Use this for more flexibility than adding @Test(expected=...)
	 */
	public static void assertThrown(Class<? extends Throwable> exceptionCls,
		Excepts.Runnable<?> runnable) {
		assertThrown(exceptionCls, (Consumer<Throwable>) null, runnable);
	}

	/**
	 * Use this for more flexibility than adding @Test(expected=...)
	 */
	public static void assertThrown(String regex, Excepts.Runnable<Exception> runnable) {
		assertThrown(Throwable.class, regex, runnable);
	}

	/**
	 * Tests if an exception is thrown with given message.
	 */
	public static void assertThrown(Class<? extends Throwable> superCls, String regex,
		Excepts.Runnable<?> runnable) {
		assertThrown(superCls, t -> assertMatch(t.getMessage(), regex), runnable);
	}

	/**
	 * Tests if an exception is thrown with given message.
	 */
	public static void assertThrown(Consumer<? super Throwable> test,
		Excepts.Runnable<?> runnable) {
		assertThrown(Throwable.class, test, runnable);
	}

	/**
	 * Tests if an exception is thrown with given message.
	 */
	public static <E extends Throwable> void assertThrown(Class<E> superCls,
		Consumer<? super E> test, Excepts.Runnable<?> runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			assertThrowable(t, superCls, test);
			return;
		}
		throw failure("Nothing thrown, expected: %s", superCls.getName());
	}

	/**
	 * Assert an InterruptedException is thrown.
	 */
	public static void assertInterrupted(Excepts.Runnable<Exception> runnable) {
		assertThrown(InterruptedException.class, runnable);
	}

	/**
	 * Assert a RuntimeInterruptedException is thrown.
	 */
	public static void assertRtInterrupted(Excepts.Runnable<Exception> runnable) {
		assertThrown(RuntimeInterruptedException.class, runnable);
	}

	/**
	 * Assert an UnsupportedOperationException is thrown.
	 */
	public static void assertUnsupported(Excepts.Runnable<Exception> runnable) {
		assertThrown(UnsupportedOperationException.class, runnable);
	}

	/**
	 * Assert a NullPointerException is thrown.
	 */
	public static void assertNpe(Excepts.Runnable<Exception> runnable) {
		assertThrown(NullPointerException.class, runnable);
	}

	/**
	 * Assert a NullPointerException is thrown.
	 */
	public static void assertRte(Excepts.Runnable<Exception> runnable) {
		assertThrown(RuntimeException.class, runnable);
	}

	/**
	 * Assert an IOException is thrown.
	 */
	public static void assertIoe(Excepts.Runnable<Exception> runnable) {
		assertThrown(IOException.class, runnable);
	}

	/**
	 * Assert an IllegalArgumentException is thrown.
	 */
	public static void assertIllegalArg(Excepts.Runnable<Exception> runnable) {
		assertThrown(IllegalArgumentException.class, runnable);
	}

	/**
	 * Assert an IllegalArgumentException is thrown.
	 */
	public static void assertIllegalState(Excepts.Runnable<Exception> runnable) {
		assertThrown(IllegalStateException.class, runnable);
	}

	/**
	 * Assert an IndexOutOfBounds exception is thrown with an overflow message.
	 */
	public static void assertIndexOob(Excepts.Runnable<Exception> runnable) {
		assertThrown(IndexOutOfBoundsException.class, runnable);
	}

	/**
	 * Assert an ArithmeticException is thrown with an overflow message.
	 */
	public static void assertOverflow(Excepts.Runnable<Exception> runnable) {
		assertThrown(ArithmeticException.class, "(?i).*\\boverflow\\b.*", runnable);
	}

	/**
	 * Checks regex not found against the string.
	 */
	public static void assertNotFound(Object actual, String pattern, Object... objs) {
		assertNotFound(actual, RegexUtil.compile(pattern, objs));
	}

	/**
	 * Checks regex not found against the string.
	 */
	public static void assertNotFound(Object actual, Pattern pattern) {
		assertNotFound(actual, pattern, null);
	}

	/**
	 * Checks regex not found against the string.
	 */
	public static void assertNotFound(Object actual, Pattern pattern, String format,
		Object... args) {
		String s = String.valueOf(actual);
		Matcher m = pattern.matcher(s);
		if (!m.find()) return;
		throw failure("%sFound by regex \"%s\"\nString: %s (%s)", nl(format, args),
			pattern.pattern(), s, m.group());
	}

	/**
	 * Checks string representation is equal to given formatted string.
	 */
	public static void assertString(Object actual, String format, Object... objs) {
		assertEquals(String.valueOf(actual), StringUtil.format(format, objs));
	}

	/**
	 * Checks string representation contains the formatted string.
	 */
	public static void assertContains(Object actual, String format, Object... objs) {
		var s = String.valueOf(actual);
		var text = StringUtil.format(format, objs);
		if (s.contains(text)) return;
		throw failure("%sNot contained in string\nString: %s", nl(text), s);
	}

	/**
	 * Checks string representation contains the formatted string.
	 */
	public static void assertNotContains(Object actual, String format, Object... objs) {
		var s = String.valueOf(actual);
		var text = StringUtil.format(format, objs);
		if (!s.contains(text)) return;
		throw failure("%sContained in string\nString: %s", nl(text), s);
	}

	/**
	 * Checks string representation is made up of lines with native separators.
	 */
	public static void assertLines(Object actual, String... lines) {
		assertString(actual, String.join(EOL, lines));
	}

	/**
	 * Checks regex find against the string.
	 */
	public static void assertFind(Object actual, String pattern, Object... objs) {
		assertFind(actual, RegexUtil.compile(pattern, objs));
	}

	/**
	 * Checks regex find against the string.
	 */
	public static void assertFind(Object actual, Pattern pattern) {
		assertFind(actual, pattern, null);
	}

	/**
	 * Checks regex find against the string.
	 */
	public static void assertFind(Object actual, Pattern pattern, String format, Object... args) {
		if (pattern.matcher(String.valueOf(actual)).find()) return;
		throw failure("%sNot found by regex \"%s\"\nString: %s", nl(format, args),
			pattern.pattern(), actual);
	}

	/**
	 * Checks regex does not match against the string.
	 */
	public static void assertNoMatch(Object actual, String pattern, Object... objs) {
		assertNoMatch(actual, RegexUtil.compile(pattern, objs));
	}

	/**
	 * Checks regex does not match against the string.
	 */
	public static void assertNoMatch(Object actual, Pattern pattern) {
		assertNoMatch(actual, pattern, null);
	}

	/**
	 * Checks regex does not match against the string.
	 */
	public static void assertNoMatch(Object actual, Pattern pattern, String format,
		Object... args) {
		String s = String.valueOf(actual);
		Matcher m = pattern.matcher(s);
		if (!m.matches()) return;
		throw failure("%sRegex match \"%s\"\nString: %s", nl(format, args), pattern.pattern(), s);
	}

	/**
	 * Checks regex match against the string.
	 */
	public static void assertMatch(Object actual, String pattern, Object... objs) {
		assertMatch(actual, RegexUtil.compile(pattern, objs));
	}

	/**
	 * Checks regex match against the string.
	 */
	public static void assertMatch(Object actual, Pattern pattern) {
		assertMatch(actual, pattern, null);
	}

	/**
	 * Checks regex match against the string.
	 */
	public static void assertMatch(Object actual, Pattern pattern, String format, Object... args) {
		if (pattern.matcher(String.valueOf(actual)).matches()) return;
		throw failure("%sDoes not match regex \"%s\"\nString: %s", nl(format, args),
			pattern.pattern(), actual);
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
		if (Files.exists(path) == exists) return;
		throw failure(exists ? "Path does not exist: %s" : "Path exists: %s", path);
	}

	/**
	 * Check if file exists.
	 */
	public static void assertDir(Path path, boolean isDir) {
		if (Files.isDirectory(path) == isDir) return;
		throw failure(isDir ? "Path is not a directory: %s" : "Path is a directory: %s", path);
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
			boolean rhsIsDir = Files.isDirectory(rhsFile);
			assertDir(lhsFile, rhsIsDir);
			if (!rhsIsDir) assertFile(lhsFile, rhsFile);
		}
	}

	/**
	 * Checks contents of two files are equal, with specific failure information if not.
	 */
	public static void assertFile(Path actual, Path expected) throws IOException {
		assertEquals(Files.size(actual), Files.size(expected), "Invalid file size");
		long pos = Files.mismatch(actual, expected);
		if (pos >= 0) throw failure("File byte mismatch at index %d", pos);
	}

	/**
	 * Checks contents of the files matches bytes, with specific failure information if not.
	 */
	public static void assertFile(Path actual, int... bytes) throws IOException {
		assertFile(actual, ByteProvider.of(bytes));
	}

	/**
	 * Checks contents of the files matches bytes, with specific failure information if not.
	 */
	public static void assertFile(Path actual, byte[] bytes) throws IOException {
		assertFile(actual, ByteProvider.of(bytes));
	}

	/**
	 * Checks contents of the files matches bytes, with specific failure information if not.
	 */
	public static void assertFile(Path actual, ByteProvider byteProvider) throws IOException {
		assertEquals(Files.size(actual), (long) byteProvider.length(), "Invalid file size");
		byte[] actualBytes = Files.readAllBytes(actual);
		for (int i = 0; i < actualBytes.length; i++)
			if (actualBytes[i] != byteProvider.getByte(i))
				throw failure("File byte mismatch at index %d", i);
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
			assertEquals(paths.length, 0, "Invalid path count");
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

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	private static void assertArrayObject(Object lhs, Object rhs) {
		if (rhs == null) assertNull(lhs);
		else {
			assertNotNull(lhs);
			assertArrayObject(lhs, rhs, ItemAssert.DEEP_EQUALS);
		}
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	private static void assertArrayObject(Object lhs, Object rhs, ItemAssert itemAssert) {
		assertIsArray(lhs);
		assertIsArray(rhs);
		assertEquals(Array.getLength(lhs), Array.getLength(rhs), "Invalid array size");
		assertArrayObject(lhs, 0, rhs, 0, Array.getLength(lhs), itemAssert);
	}

	private static void assertIsArray(Object array) {
		assertTrue(array.getClass().isArray(), "Expected an array: %s", array.getClass());
	}

	private static <T> void assertList(List<? extends T> lhs, int lhsOffset, List<? extends T> rhs,
		int rhsOffset, int len, ItemAssert indexAssert) {
		int lhsLen = lhs.size();
		int rhsLen = rhs.size();
		for (int i = 0; i < len; i++) {
			boolean hasLhs = lhsOffset + i < lhsLen;
			T lhsVal = hasLhs ? lhs.get(lhsOffset + i) : null;
			boolean hasRhs = rhsOffset + i < rhsLen;
			T rhsVal = hasRhs ? rhs.get(rhsOffset + i) : null;
			assertIndex(lhsOffset + i, lhsVal, hasLhs, rhsVal, hasRhs, indexAssert);
		}
	}

	private static <T> void assertIndex(int i, T lhs, boolean hasLhs, T rhs, boolean hasRhs,
		ItemAssert itemAssert) {
		if (!hasLhs && !hasRhs) throw failure("No value at index %d", i);
		if (!hasLhs) throw failure("No value at index %d, expected: %s", i, str(rhs));
		if (!hasRhs) throw failure("Unexpected value at index %d: %s", i, str(lhs));
		itemAssert.assertItem(lhs, rhs, "Index %d", i);
	}

	private static void assertDeepEquals(Object lhs, Object rhs, String format, Object... args) {
		if (Objects.deepEquals(lhs, rhs)) return;
		throw failure("%sExpected: %s\n  actual: %s", nl(format, args), str(rhs), str(lhs));
	}

	private static String nl(String format, Object... args) {
		String s = StringUtil.format(format, args);
		return s.isEmpty() ? "" : s + '\n';
	}

	private static String str(Object obj) {
		if (obj instanceof Byte) return String.format("%1$d (0x%1$02x)", obj);
		if (obj instanceof Short) return String.format("%1$d (0x%1$04x)", obj);
		if (obj instanceof Integer) return String.format("%1$d (0x%1$08x)", obj);
		if (obj instanceof Long) return String.format("%1$dL (0x%1$016x)", obj);
		if (obj instanceof Float) return String.format("%sf", obj);
		return String.valueOf(obj);
	}

}
