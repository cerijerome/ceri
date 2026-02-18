package ceri.common.test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import ceri.common.array.Array;
import ceri.common.array.RawArray;
import ceri.common.collect.Immutable;
import ceri.common.collect.Iterables;
import ceri.common.collect.Lists;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReader;
import ceri.common.data.IntProvider;
import ceri.common.data.LongProvider;
import ceri.common.data.TypeValue;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.io.Buffers;
import ceri.common.io.PathList;
import ceri.common.io.Paths;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
import ceri.common.stream.DoubleStream;
import ceri.common.stream.IntStream;
import ceri.common.stream.LongStream;
import ceri.common.stream.Stream;
import ceri.common.stream.Streams;
import ceri.common.text.Chars;
import ceri.common.text.Regex;
import ceri.common.text.Strings;
import ceri.common.text.Text;

/**
 * Assertions.
 */
public class Assert {
	private static final Item DEEP_EQUALS_ITEM = Assert::deepEquals;
	private static final int LINE_COUNT = 10;
	private static final int PRECISION_DEF = 3;
	public static final int APPROX_PRECISION_DEF = 3;

	private Assert() {}

	/**
	 * Assertion for an item in a group.
	 */
	private interface Item {
		void item(Object lhs, Object rhs, String format, Object... args);
	}

	// failures

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

	/**
	 * Fail by throwing an assertion error.
	 */
	public static <T> T fail() {
		throw failure("Failed");
	}

	/**
	 * Fail by throwing an assertion error.
	 */
	public static <T> T fail(Throwable t) {
		throw failure(t, "Failed");
	}

	/**
	 * Fail by throwing an assertion error.
	 */
	public static <T> T fail(String format, Object... args) {
		throw failure((Throwable) null, format, args);
	}

	/**
	 * Fail by throwing an assertion error.
	 */
	public static <T> T fail(Throwable t, String format, Object... args) {
		throw failure(t, format, args);
	}

	/**
	 * Returns a failure assertion error to throw.
	 */
	public static AssertionError failure(String format, Object... args) {
		return failure((Throwable) null, format, args);
	}

	/**
	 * Returns a failure assertion error to throw.
	 */
	public static AssertionError failure(Throwable t, String format, Object... args) {
		return new AssertionError(Strings.format(format, args), t);
	}

	// exceptions

	/**
	 * Fails unless an assertion error is thrown; primarily used to check assertion methods.
	 */
	public static void assertion(Excepts.Runnable<Exception> runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			throw failure(e, "Expected to assert"); // Exception not allowed
		} catch (AssertionError e) {
			return; // Success
		}
		throw failure("Expected to assert");
	}

	/**
	 * Fails unless a RuntimeException is thrown.
	 */
	public static void runtime(Excepts.Runnable<Exception> runnable) {
		thrown(RuntimeException.class, runnable);
	}

	/**
	 * Fails unless a NullPointerException is thrown.
	 */
	public static void nullPointer(Excepts.Runnable<Exception> runnable) {
		thrown(NullPointerException.class, runnable);
	}

	/**
	 * Fails unless an IllegalArgumentException is thrown.
	 */
	public static void illegalArg(Excepts.Runnable<? extends Exception> runnable) {
		thrown(IllegalArgumentException.class, runnable);
	}

	/**
	 * Fails unless an IllegalArgumentException is thrown.
	 */
	public static void illegalState(Excepts.Runnable<Exception> runnable) {
		thrown(IllegalStateException.class, runnable);
	}

	/**
	 * Fails unless an ArithmeticException is thrown with an overflow message.
	 */
	public static void overflow(Excepts.Runnable<Exception> runnable) {
		thrown(ArithmeticException.class, "(?i).*\\boverflow\\b.*", runnable);
	}

	/**
	 * Fails unless a NoSuchElementException is thrown.
	 */
	public static void noSuchElement(Excepts.Runnable<Exception> runnable) {
		thrown(NoSuchElementException.class, runnable);
	}

	/**
	 * Fails unless an UnsupportedOperationException is thrown.
	 */
	public static void unsupportedOp(Excepts.Runnable<Exception> runnable) {
		thrown(UnsupportedOperationException.class, runnable);
	}

	/**
	 * Fails unless an IOException is thrown.
	 */
	public static void io(Excepts.Runnable<Exception> runnable) {
		thrown(IOException.class, runnable);
	}

	/**
	 * Tests that an exception was thrown while executing the runnable.
	 */
	public static void thrown(Excepts.Runnable<Exception> runnable) {
		thrown(Exception.class, runnable);
	}

	/**
	 * Tests that a specific exception type was thrown while executing the runnable.
	 */
	public static void thrown(Class<? extends Throwable> exceptionCls,
		Excepts.Runnable<?> runnable) {
		thrown(exceptionCls, (Functions.Consumer<Throwable>) null, runnable);
	}

	/**
	 * Tests that an exception was thrown while executing the runnable, with message matching the
	 * regex.
	 */
	public static void thrown(String regex, Excepts.Runnable<Exception> runnable) {
		thrown(Throwable.class, regex, runnable);
	}

	/**
	 * Tests that a specific exception type was thrown while executing the runnable, with message
	 * matching the regex.
	 */
	public static void thrown(Class<? extends Throwable> superCls, String regex,
		Excepts.Runnable<?> runnable) {
		thrown(superCls, t -> match(t.getMessage(), regex), runnable);
	}

	/**
	 * Tests if an exception is thrown with given message.
	 */
	public static void thrown(Functions.Consumer<? super Throwable> test,
		Excepts.Runnable<?> runnable) {
		thrown(Throwable.class, test, runnable);
	}

	/**
	 * Tests if an exception is thrown with given message.
	 */
	public static <E extends Throwable> void thrown(Class<E> superCls,
		Functions.Consumer<? super E> test, Excepts.Runnable<?> runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			throwable(t, superCls, test);
			return;
		}
		throw failure("Nothing thrown, expected: %s", superCls.getName());
	}

	/**
	 * Verifies throwable super class.
	 */
	public static void throwable(Throwable t, Class<? extends Throwable> superCls) {
		throwable(t, superCls, (Functions.Consumer<Throwable>) null);
	}

	/**
	 * Verifies throwable message.
	 */
	public static void throwable(Throwable t, String regex, Object... args) {
		throwable(t, Throwable.class, regex, args);
	}

	/**
	 * Verifies throwable message.
	 */
	public static void throwable(Throwable t, Functions.Consumer<Throwable> messageTest) {
		throwable(t, null, messageTest);
	}

	/**
	 * Verifies throwable super class and message.
	 */
	public static void throwable(Throwable t, Class<? extends Throwable> superCls, String regex,
		Object... args) {
		throwable(t, superCls, e -> match(e.getMessage(), regex, args));
	}

	/**
	 * Verifies throwable type.
	 */
	@SuppressWarnings("null")
	public static <T extends Throwable> void throwable(Throwable t, Class<T> superCls,
		Functions.Consumer<? super T> test) {
		if (t == null && superCls == null && test == null) return;
		notNull(t, "Throwable");
		if (superCls != null && !superCls.isAssignableFrom(t.getClass()))
			throw failure("Expected %s: %s", superCls.getName(), t.getClass().getName());
		if (test != null) test.accept(Reflect.unchecked(t));
	}

	// misc

	/**
	 * Fails if the condition is true.
	 */
	public static void no(boolean condition) {
		no(condition, "");
	}

	/**
	 * Fails if the condition is true.
	 */
	public static void no(boolean condition, String format, Object... args) {
		if (!condition) return;
		var message = Strings.format(format, args);
		throw failure(message.isEmpty() ? "Expected false" : message);
	}

	/**
	 * Fails if the condition is false.
	 */
	public static void yes(boolean condition) {
		yes(condition, "");
	}

	/**
	 * Fails if the condition is false.
	 */
	public static void yes(boolean condition, String format, Object... args) {
		if (condition) return;
		var message = Strings.format(format, args);
		throw failure(message.isEmpty() ? "Expected true" : message);
	}

	/**
	 * Fail if the object is not an instance of the class.
	 */
	public static <T> T instance(Object actual, Class<T> expected) {
		if (expected.isInstance(actual)) return Reflect.unchecked(actual);
		throw failure("Expected instance: %s\n           actual: %s", Reflect.name(expected),
			Reflect.className(actual));
	}

	/**
	 * Fails if the class constructors are not private; used to test utility classes.
	 */
	public static void privateConstructor(Class<?>... classes) {
		for (var cls : classes)
			constructorIsPrivate(cls);
	}

	// object equality

	/**
	 * Fails if the objects are not equal.
	 */
	public static <T> T equal(T actual, T expected) {
		return equal(actual, expected, "");
	}

	/**
	 * Fails if the objects are not equal.
	 */
	public static <T> T equal(T actual, T expected, String format, Object... args) {
		if (Objects.equals(expected, actual)) return actual;
		throw unexpected(actual, expected, format, args);
	}

	/**
	 * Fails if the objects are equal.
	 */
	public static <T> void notEqual(T actual, T unexpected) {
		notEqual(actual, unexpected, "");
	}

	/**
	 * Fails if the objects are equal.
	 */
	public static <T> void notEqual(T actual, T unexpected, String format, Object... args) {
		if (!Objects.equals(unexpected, actual)) return;
		throw failure("%sUnexpected: %s", nl(format, args), str(actual));
	}

	/**
	 * Fails if the first object equals any of the following objects.
	 */
	@SafeVarargs
	public static <T> void notEqualAll(T t0, T... ts) {
		for (T t : ts)
			notEqual(t0, t);
	}

	/**
	 * Fail if the objects are not the same reference.
	 */
	public static <T> void same(T actual, T expected) {
		same(actual, expected, "");
	}

	/**
	 * Fail if the objects are not the same reference.
	 */
	public static <T> void same(T actual, T expected, String format, Object... args) {
		if (expected == actual) return;
		throw failure("%sExpected same: %s (%s)\n       actual: %s (%s)", nl(format, args),
			expected, Reflect.hashId(expected), actual, Reflect.hashId(actual));
	}

	/**
	 * Fail if the objects are the same reference.
	 */
	public static <T> void notSame(T actual, T unexpected) {
		notSame(actual, unexpected, "");
	}

	/**
	 * Fail if the objects are not the same reference.
	 */
	public static <T> void notSame(T actual, T unexpected, String format, Object... args) {
		if (unexpected != actual) return;
		throw failure("%sValues are the same: %s (%s)", nl(format, args), actual,
			Reflect.hashId(actual));
	}

	/**
	 * Fail if the object is not null.
	 */
	public static <T> void isNull(T actual) {
		isNull(actual, "");
	}

	/**
	 * Fail if the object is not null.
	 */
	public static <T> void isNull(T actual, String format, Object... args) {
		if (actual != null) throw failure("%sExpected null: %s", nl(format, args), actual);
	}

	/**
	 * Fail if the object is null.
	 */
	public static <T> void notNull(T actual) {
		notNull(actual, "");
	}

	/**
	 * Fail if the object is null.
	 */
	public static <T> void notNull(T actual, String format, Object... args) {
		if (actual != null) return;
		String message = Strings.format(format, args);
		throw failure(message.isEmpty() ? "Value is null" : message);
	}

	/**
	 * Fails if the optional value is not equal.
	 */
	public static <T> void optional(Optional<T> actual, T expected) {
		optional(actual, expected, "");
	}

	/**
	 * Fails if the optional value is not equal.
	 */
	public static <T> void optional(Optional<T> actual, T expected, String format, Object... args) {
		equal(actual, Optional.ofNullable(expected), format, args);
	}

	// primitives

	/**
	 * Fails if the value does not equal the expected value.
	 */
	public static byte equals(byte actual, int expected) {
		return equals(actual, expected, "");
	}

	/**
	 * Fails if the value does not equal the expected value.
	 */
	public static byte equals(byte actual, int expected, String format, Object... args) {
		equal(actual, (byte) expected, format, args);
		return actual;
	}

	/**
	 * Fails if the value does not equal the expected value.
	 */
	public static short equals(short actual, int expected) {
		return equals(actual, expected, "");
	}

	/**
	 * Fails if the value does not equal the expected value.
	 */
	public static short equals(short actual, int expected, String format, Object... args) {
		equal(actual, (short) expected, format, args);
		return actual;
	}

	/**
	 * Fails if the value does not equal the expected value. Equality includes infinity and NaN.
	 */
	public static double equals(double actual, double expected) {
		return equals(actual, expected, "");
	}

	/**
	 * Fails if the value does not equal the expected value. Equality includes infinity and NaN.
	 */
	public static double equals(double actual, double expected, String format, Object... args) {
		if (doubleEqual(actual, expected)) return actual;
		throw failure("%sExpected: %s\n  actual: %s", nl(format, args), expected, actual);
	}

	/**
	 * Fails if any of the masked bits are not set.
	 */
	public static long mask(int actual, int mask) {
		equal(actual & mask, mask, "Mask not present 0x%x", mask);
		return actual;
	}

	/**
	 * Fails if any of the masked bits are not set.
	 */
	public static long mask(long actual, long mask) {
		equal(actual & mask, mask, "Mask not present 0x%x", mask);
		return actual;
	}

	/**
	 * Fails if the value is outside the given inclusive range.
	 */
	public static int range(int value, int minInclusive, int maxInclusive) {
		return range(value, minInclusive, maxInclusive, "");
	}

	/**
	 * Fails if the value is outside the given inclusive range.
	 */
	public static int range(int actual, int minInclusive, int maxInclusive, String format,
		Object... args) {
		if (actual >= minInclusive && actual <= maxInclusive) return actual;
		throw failure("%sExpected: %s <= value <= %s\n  actual:    %s", nl(format, args),
			str(minInclusive), str(maxInclusive), str(actual));
	}

	/**
	 * Fails if the value is outside the given inclusive range.
	 */
	public static long range(long value, long minInclusive, long maxInclusive) {
		return range(value, minInclusive, maxInclusive, "");
	}

	/**
	 * Fails if the value is outside the given inclusive range.
	 */
	public static long range(long actual, long minInclusive, long maxInclusive, String format,
		Object... args) {
		if (actual >= minInclusive && actual <= maxInclusive) return actual;
		throw failure("%sExpected: %s <= value <= %s\n  actual:    %s", nl(format, args),
			str(minInclusive), str(maxInclusive), str(actual));
	}

	/**
	 * Fails if the value is outside the given inclusive range.
	 */
	public static double range(double actual, double minInclusive, double maxExclusive) {
		return range(actual, minInclusive, maxExclusive, null);
	}

	/**
	 * Fails if the value is outside the given inclusive range.
	 */
	public static double range(double actual, double minInclusive, double maxExclusive,
		String format, Object... args) {
		if (actual >= minInclusive && actual <= maxExclusive) return actual;
		throw failure("%sExpected: %s <= value < %s\n  actual:    %s", nl(format, args),
			str(minInclusive), str(maxExclusive), str(actual));
	}

	/**
	 * Fails if the value does not equal the expected value within precision decimal places.
	 */
	public static double approx(double actual, double expected) {
		return approx(actual, expected, "");
	}

	/**
	 * Fails if the value does not equal the expected value within precision decimal places.
	 */
	public static double approx(double actual, double expected, String format, Object... args) {
		return approx(actual, expected, PRECISION_DEF, format, args);
	}

	/**
	 * Fails if the value does not equal the expected value within precision decimal places.
	 */
	public static double approx(double actual, double expected, int precision) {
		return approx(actual, expected, precision, "");
	}

	/**
	 * Fails if the value does not equal the expected value within precision decimal places.
	 */
	public static double approx(double actual, double expected, int precision, String format,
		Object... args) {
		if (!Double.isFinite(expected)) return equals(actual, expected, format, args);
		equal(Maths.round(precision, actual), Maths.round(precision, expected), format, args);
		return actual;
	}

	/**
	 * Fails if the value does not equal the expected value within the difference.
	 */
	public static double approx(double actual, double expected, double diff) {
		return approx(actual, expected, diff, "");
	}

	/**
	 * Fails if the value does not equal the expected value within the difference.
	 */
	public static double approx(double actual, double expected, double diff, String format,
		Object... args) {
		if (!Double.isFinite(expected)) return equals(actual, expected, format, args);
		if ((Math.abs(expected - actual) <= diff)) return actual;
		throw failure("%sExpected: %s (±%s)\n  actual: %s", nl(format, args), expected, diff,
			actual);
	}

	// arrays

	/**
	 * Fails if the array does not equal the given value array.
	 */
	@SafeVarargs
	public static <T> void array(T[] array, T... expected) {
		rawArray(array, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(boolean[] array, boolean... expected) {
		rawArray(array, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(char[] array, char... expected) {
		rawArray(array, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(byte[] array, byte[] expected) {
		rawArray(array, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(byte[] array, int... values) {
		array(array, Array.BYTE.of(values));
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(short[] array, short[] expected) {
		rawArray(array, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(short[] array, int... expected) {
		array(array, Array.SHORT.of(expected));
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(int[] array, int... expected) {
		rawArray(array, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(long[] array, long... expected) {
		rawArray(array, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(float[] array, float... expected) {
		rawArray(array, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(float[] array, double... expected) {
		array(array, Array.FLOAT.of(expected));
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(double[] array, double... expected) {
		rawArray(array, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(ByteProvider array, byte[] expected) {
		array(array.copy(0), expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(ByteProvider array, int... values) {
		array(array, Array.BYTE.of(values));
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(IntProvider array, int... values) {
		array(array.copy(0), values);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(LongProvider array, long... values) {
		array(array.copy(0), values);
	}

	/**
	 * Fails if the array does not equal the given value array, with default value precision.
	 */
	public static void approxArray(double[] array, double... expected) {
		approxArray(PRECISION_DEF, array, expected);
	}

	/**
	 * Fails if the array does not equal the given value array, with specified value precision.
	 */
	public static void approxArray(int precision, double[] array, double... expected) {
		rawArray(array, expected, (lhs, rhs, format, args) -> approx((Double) lhs, (Double) rhs,
			precision, format, args));
	}

	/**
	 * Fails if the array does not equal the given value array, within specified value difference.
	 */
	public static void approxArray(double diff, double[] array, double... expected) {
		rawArray(array, expected,
			(lhs, rhs, format, args) -> approx((Double) lhs, (Double) rhs, diff, format, args));
	}

	// collections

	/**
	 * Fails if the iterator does not have equal elements in order.
	 */
	@SafeVarargs
	public static <T> void iterator(Iterator<T> lhs, T... ts) {
		iterator(lhs, Lists.wrap(ts));
	}

	/**
	 * Fails if the iterator does not have equal elements in order.
	 */
	public static <T> void iterator(Iterator<T> lhs, Iterable<T> rhs) {
		var lhsList = Immutable.list(Iterables.of(lhs));
		var rhsList = Immutable.list(rhs);
		list(lhsList, rhsList);
		no(lhs.hasNext(), "Has more elements");
		noSuchElement(lhs::next);
	}

	/**
	 * Fails if the list does not have equal elements in order.
	 */
	public static <T> void list(List<? extends T> lhs, List<? extends T> rhs) {
		if (rhs == null) isNull(lhs);
		else {
			list(lhs, 0, rhs, 0, lhs.size());
			equal(lhs.size(), rhs.size(), "List size");
		}
	}

	/**
	 * Fails if the list does not have equal elements in order.
	 */
	public static <T> void list(List<? extends T> lhs, int lhsOffset, List<? extends T> rhs,
		int rhsOffset, int len) {
		list(lhs, lhsOffset, rhs, rhsOffset, len, DEEP_EQUALS_ITEM);
	}

	/**
	 * Fails if the map is null or has elements.
	 */
	public static <K, V> void map(Map<K, V> subject) {
		equal(subject, Map.of());
	}

	/**
	 * Fails if the map does not contain exactly the given elements.
	 */
	public static <K, V> void map(Map<K, V> subject, K k, V v) {
		equal(subject, Immutable.mapOf(k, v));
	}

	/**
	 * Fails if the map does not contain exactly the given elements.
	 */
	public static <K, V> void map(Map<K, V> subject, K k0, V v0, K k1, V v1) {
		unordered(subject, Immutable.listOf(k0, k1), Immutable.listOf(v0, v1));
	}

	/**
	 * Fails if the map does not contain exactly the given elements.
	 */
	public static <K, V> void map(Map<K, V> subject, K k0, V v0, K k1, V v1, K k2, V v2) {
		unordered(subject, Immutable.listOf(k0, k1, k2), Immutable.listOf(v0, v1, v2));
	}

	/**
	 * Fails if the map does not contain exactly the given elements.
	 */
	public static <K, V> void map(Map<K, V> subject, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
		V v3) {
		unordered(subject, Immutable.listOf(k0, k1, k2, k3), Immutable.listOf(v0, v1, v2, v3));
	}

	/**
	 * Fails if the map does not contain exactly the given elements.
	 */
	public static <K, V> void map(Map<K, V> subject, K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3,
		K k4, V v4) {
		unordered(subject, Immutable.listOf(k0, k1, k2, k3, k4),
			Immutable.listOf(v0, v1, v2, v3, v4));
	}

	/**
	 * Fails if the map does not contain the key/value entry.
	 */
	public static <K, V> void entry(Map<K, V> subject, K key, V value) {
		equal(subject.get(key), value, "Unexpected value for key %s", key);
	}

	/**
	 * Fails if the iterator allows mutation.
	 */
	public static <T> void immutable(Iterator<T> iterator) {
		notNull(iterator);
		if (!iterator.hasNext()) return;
		iterator.next();
		unsupportedOp(() -> iterator.remove());
	}

	/**
	 * Fails if the collection allows mutation.
	 */
	public static <T, C extends Collection<T>> C immutable(C collection) {
		notNull(collection);
		unsupportedOp(() -> collection.add(null));
		unsupportedOp(() -> collection.addAll(Immutable.setOf((T) null)));
		if (collection instanceof List<?> list) immutableList(list);
		if (collection.isEmpty()) return collection;
		var t = collection.iterator().next();
		unsupportedOp(() -> collection.remove(t));
		unsupportedOp(() -> collection.removeAll(Immutable.setOf(t)));
		unsupportedOp(() -> collection.retainAll(Set.of()));
		unsupportedOp(collection::clear);
		immutable(collection.iterator());
		return collection;
	}

	private static <T> void immutableList(List<T> list) {
		unsupportedOp(() -> list.set(0, null));
		unsupportedOp(() -> list.add(0, null));
		unsupportedOp(() -> list.addAll(0, Immutable.setOf((T) null)));
		unsupportedOp(() -> list.remove(0));
		immutable(list.listIterator());
		immutable(list.listIterator(0));
	}

	/**
	 * Fails if the map allows mutation.
	 */
	public static <K, V, M extends Map<K, V>> M immutable(M map) {
		notNull(map);
		unsupportedOp(() -> map.put(null, null));
		unsupportedOp(() -> map.putAll(Immutable.mapOf(null, null)));
		if (map.isEmpty()) return map;
		var k = map.keySet().iterator().next();
		unsupportedOp(() -> map.remove(k));
		unsupportedOp(map::clear);
		immutable(map.entrySet());
		immutable(map.keySet());
		immutable(map.values());
		return map;
	}

	// streams

	/**
	 * Fails if the stream does does not contain exactly the values in order.
	 */
	@SafeVarargs
	public static <E extends Exception, T> void stream(Stream<E, T> stream, T... ts) throws E {
		ordered(stream.toList(), ts);
	}

	/**
	 * Fails if the stream does does not contain exactly the values in order.
	 */
	public static <E extends Exception> void stream(IntStream<E> stream, int... is) throws E {
		array(stream.toArray(), is);
	}

	/**
	 * Fails if the stream does does not contain exactly the values in order.
	 */
	public static <E extends Exception> void stream(LongStream<E> stream, long... ls) throws E {
		array(stream.toArray(), ls);
	}

	/**
	 * Fails if the stream does does not contain exactly the values in order.
	 */
	public static <E extends Exception> void stream(DoubleStream<E> stream, double... ds) throws E {
		array(stream.toArray(), ds);
	}

	// unordered

	/**
	 * Checks collection contains exactly given elements in any order, with specific failure
	 * information if not.
	 */
	@SafeVarargs
	public static <T> void unordered(T[] lhs, T... ts) {
		unordered(Lists.wrap(lhs), ts);
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(boolean[] lhs, boolean... expected) {
		unordered(Array.BOOL.list(lhs), Array.BOOL.list(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(byte[] lhs, byte... expected) {
		unordered(Array.BYTE.list(lhs), Array.BYTE.list(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(byte[] lhs, int... values) {
		unordered(lhs, Array.BYTE.of(values));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(char[] lhs, char... expected) {
		unordered(Array.CHAR.list(lhs), Array.CHAR.list(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(char[] lhs, int... expected) {
		unordered(lhs, Array.CHAR.of(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(short[] lhs, short... expected) {
		unordered(Array.SHORT.list(lhs), Array.SHORT.list(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(short[] lhs, int... expected) {
		unordered(lhs, Array.SHORT.of(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(int[] lhs, int... expected) {
		unordered(Array.INT.list(lhs), Array.INT.list(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(long[] lhs, long... expected) {
		unordered(Array.LONG.list(lhs), Array.LONG.list(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(float[] lhs, float... expected) {
		unordered(Array.FLOAT.list(lhs), Array.FLOAT.list(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(float[] lhs, double... expected) {
		unordered(lhs, Array.FLOAT.of(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(double[] lhs, double... expected) {
		unordered(Array.DOUBLE.list(lhs), Array.DOUBLE.list(expected));
	}

	/**
	 * Fails if the collection does not contain exactly the elements in any order.
	 */
	@SafeVarargs
	public static <T> void unordered(Collection<T> lhs, T... ts) {
		unordered(lhs, Arrays.asList(ts));
	}

	/**
	 * Fails if the collection does not contain exactly the elements in any order.
	 */
	public static <T> void unordered(Collection<? extends T> lhs, Collection<? extends T> rhs) {
		int i = 0;
		for (T t : lhs) {
			if (!rhs.contains(t)) throw failure("Unexpected element at position %d: %s", i, str(t));
			i++;
		}
		for (T t : rhs)
			if (!lhs.contains(t)) throw failure("Missing element: %s", str(t));
		equal(lhs.size(), rhs.size(), "Unexpected collection size");
	}

	// ordered

	/**
	 * Fails if the iterated values do not equal the elements in order.
	 */
	@SafeVarargs
	public static <T> void ordered(Iterable<T> lhs, T... ts) {
		ordered(lhs, Arrays.asList(ts));
	}

	/**
	 * Fails if the iterated values do not equal the elements in order.
	 */
	public static <T> void ordered(Iterable<T> lhs, Iterable<T> rhs) {
		iterator(lhs.iterator(), rhs);
	}

	/**
	 * Fails if the map does not contain exactly the keys and values in order.
	 */
	public static <K, V> void ordered(Map<K, V> subject, K k, V v) {
		map(subject, k, v);
	}

	/**
	 * Fails if the map does not contain exactly the keys and values in order.
	 */
	public static <K, V> void ordered(Map<K, V> subject, K k0, V v0, K k1, V v1) {
		ordered(subject, Immutable.listOf(k0, k1), Immutable.listOf(v0, v1));
	}

	/**
	 * Fails if the map does not contain exactly the keys and values in order.
	 */
	public static <K, V> void ordered(Map<K, V> subject, K k0, V v0, K k1, V v1, K k2, V v2) {
		ordered(subject, Immutable.listOf(k0, k1, k2), Immutable.listOf(v0, v1, v2));
	}

	/**
	 * Fails if the map does not contain exactly the keys and values in order.
	 */
	public static <K, V> void ordered(Map<K, V> subject, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
		V v3) {
		ordered(subject, Immutable.listOf(k0, k1, k2, k3), Immutable.listOf(v0, v1, v2, v3));
	}

	/**
	 * Fails if the map does not contain exactly the keys and values in order.
	 */
	public static <K, V> void ordered(Map<K, V> subject, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
		V v3, K k4, V v4) {
		ordered(subject, Immutable.listOf(k0, k1, k2, k3, k4),
			Immutable.listOf(v0, v1, v2, v3, v4));
	}

	// strings

	/**
	 * Checks string representation is equal to given formatted string.
	 */
	public static void string(Object actual, String format, Object... objs) {
		if (format == null) equal(actual, null);
		else string(String.valueOf(actual), Strings.format(format, objs));
	}

	/**
	 * Checks string representation split into lines.
	 */
	public static void lines(Object actual, String... expectedLineArray) {
		var actualLines = Regex.Split.LINE.list(String.valueOf(actual));
		var expectedLines = Arrays.asList(expectedLineArray);
		int lines = Math.max(actualLines.size(), expectedLines.size());
		for (int i = 0; i < lines; i++) {
			var actualLine = Lists.at(actualLines, i, "");
			var expectedLine = Lists.at(expectedLines, i, "");
			if (Objects.equals(actualLine, expectedLine)) continue;
			throw failure("Line %d%nExpected: %s%n  actual: %s%n%nExpected:%n%s%n%nActual:%n%s%n",
				i + 1, expectedLine.trim(), actualLine.trim(), limitedLines(expectedLines, i),
				limitedLines(actualLines, i));
		}
	}

	/**
	 * Checks multi-line text, with line-specific failure info.
	 */
	public static void text(Object actual, String expected) {
		lines(actual, Regex.Split.LINE.array(expected));
	}

	/**
	 * Checks string representation contains the formatted string.
	 */
	public static void contains(Object actual, String format, Object... objs) {
		var s = String.valueOf(actual);
		var text = Strings.format(format, objs);
		if (s.contains(text)) return;
		throw failure("%sNot contained in string\nString: %s", nl(text), s);
	}

	/**
	 * Checks regex match against the string.
	 */
	public static void match(Object actual, String pattern, Object... objs) {
		match(actual, Regex.compile(pattern, objs));
	}

	/**
	 * Checks regex match against the string.
	 */
	public static void match(Object actual, Pattern pattern) {
		match(actual, pattern, null);
	}

	/**
	 * Checks regex match against the string.
	 */
	public static void match(Object actual, Pattern pattern, String format, Object... args) {
		if (pattern.matcher(String.valueOf(actual)).matches()) return;
		throw failure("%sDoes not match regex \"%s\"\nString: %s", nl(format, args),
			pattern.pattern(), actual);
	}

	/**
	 * Checks regex does not match against the string.
	 */
	public static void noMatch(Object actual, String pattern, Object... objs) {
		noMatch(actual, Regex.compile(pattern, objs));
	}

	/**
	 * Checks regex does not match against the string.
	 */
	public static void noMatch(Object actual, Pattern pattern) {
		noMatch(actual, pattern, null);
	}

	/**
	 * Checks regex does not match against the string.
	 */
	public static void noMatch(Object actual, Pattern pattern, String format, Object... args) {
		var s = String.valueOf(actual);
		var m = pattern.matcher(s);
		if (!m.matches()) return;
		throw failure("%sRegex match \"%s\"\nString: %s", nl(format, args), pattern.pattern(), s);
	}

	/**
	 * Checks regex find against the string.
	 */
	public static void find(Object actual, String pattern, Object... objs) {
		find(actual, Regex.compile(pattern, objs));
	}

	/**
	 * Checks regex find against the string.
	 */
	public static void find(Object actual, Pattern pattern) {
		find(actual, pattern, null);
	}

	/**
	 * Checks regex find against the string.
	 */
	public static void find(Object actual, Pattern pattern, String format, Object... args) {
		if (pattern.matcher(String.valueOf(actual)).find()) return;
		throw failure("%sNot found by regex \"%s\"\nString: %s", nl(format, args),
			pattern.pattern(), actual);
	}

	/**
	 * Checks regex not found against the string.
	 */
	public static void notFound(Object actual, String pattern, Object... objs) {
		notFound(actual, Regex.compile(pattern, objs));
	}

	/**
	 * Checks regex not found against the string.
	 */
	public static void notFound(Object actual, Pattern pattern) {
		notFound(actual, pattern, null);
	}

	/**
	 * Checks regex not found against the string.
	 */
	public static void notFound(Object actual, Pattern pattern, String format, Object... args) {
		String s = String.valueOf(actual);
		Matcher m = pattern.matcher(s);
		if (!m.find()) return;
		throw failure("%sFound by regex \"%s\"\nString: %s (%s)", nl(format, args),
			pattern.pattern(), s, m.group());
	}

	/**
	 * Check ascii read from byte reader.
	 */
	public static void ascii(ByteReader reader, String s) {
		var actual = reader.readAscii(s.length());
		equal(actual, s);
	}

	// other types

	/**
	 * Fails if the type value fields do not equal the given values.
	 */
	public static <T> void typeValue(TypeValue<T> t, T type, int value) {
		Assert.equal(t.intValue(), value);
		Assert.equal(t.type(), type);
	}

	/**
	 * Fails if the type value fields do not equal the given values.
	 */
	public static <T> void typeValue(TypeValue<T> t, T type, int value, String namePattern,
		Object... args) {
		Assert.equal(t.intValue(), value);
		Assert.equal(t.type(), type);
		if (namePattern == null) Assert.isNull(t.name());
		else Assert.match(t.name(), namePattern, args);
	}

	// I/O

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(CharBuffer buffer, char... array) {
		array(Buffers.CHAR.get(buffer), array);
	}

	/**
	 * Checks buffer values against string.
	 */
	public static void buffer(CharBuffer buffer, String s) {
		buffer(buffer, s.toCharArray());
	}

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(ByteBuffer buffer, int... array) {
		array(Buffers.BYTE.get(buffer), array);
	}

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(ShortBuffer buffer, int... array) {
		array(Buffers.SHORT.get(buffer), array);
	}

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(IntBuffer buffer, int... array) {
		array(Buffers.INT.get(buffer), array);
	}

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(LongBuffer buffer, long... array) {
		array(Buffers.LONG.get(buffer), array);
	}

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(FloatBuffer buffer, double... array) {
		array(Buffers.FLOAT.get(buffer), array);
	}

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(DoubleBuffer buffer, double... array) {
		array(Buffers.DOUBLE.get(buffer), array);
	}

	/**
	 * Checks bytes read from input stream.
	 */
	public static void read(InputStream in, ByteProvider bytes) throws IOException {
		read(in, bytes.copy(0));
	}

	/**
	 * Checks bytes read from input stream.
	 */
	public static void read(InputStream in, int... bytes) throws IOException {
		read(in, Array.BYTE.of(bytes));
	}

	/**
	 * Checks bytes read from input stream.
	 */
	public static void read(InputStream in, byte[] bytes) throws IOException {
		array(in.readNBytes(bytes.length), bytes);
	}

	/**
	 * Checks if file exists.
	 */
	public static void exists(Path path, boolean exists) {
		if (Files.exists(path) == exists) return;
		throw failure(exists ? "Path does not exist: %s" : "Path exists: %s", path);
	}

	/**
	 * Checks if file exists.
	 */
	public static void dir(Path path, boolean isDir) {
		if (Files.isDirectory(path) == isDir) return;
		throw failure(isDir ? "Path is not a directory: %s" : "Path is a directory: %s", path);
	}

	/**
	 * Checks contents of two directories are equal, with specific failure information if not.
	 */
	public static void dir(Path lhsDir, Path rhsDir) throws IOException {
		var lhsPathsRelative = PathList.of(lhsDir).relative().sort().list();
		var rhsPathsRelative = PathList.of(rhsDir).relative().sort().list();
		ordered(lhsPathsRelative, rhsPathsRelative);
		for (var path : lhsPathsRelative) {
			var lhsFile = lhsDir.resolve(path);
			var rhsFile = rhsDir.resolve(path);
			boolean rhsIsDir = Files.isDirectory(rhsFile);
			dir(lhsFile, rhsIsDir);
			if (!rhsIsDir) file(lhsFile, rhsFile);
		}
	}

	/**
	 * Checks contents of two files are equal, with specific failure information if not.
	 */
	public static void file(Path actual, Path expected) throws IOException {
		equal(Files.size(actual), Files.size(expected), "Invalid file size");
		long pos = Files.mismatch(actual, expected);
		if (pos >= 0) throw failure("File byte mismatch at index %d", pos);
	}

	/**
	 * Checks contents of the files matches bytes, with specific failure information if not.
	 */
	public static void file(Path actual, int... bytes) throws IOException {
		file(actual, ByteProvider.of(bytes));
	}

	/**
	 * Checks contents of the files matches bytes, with specific failure information if not.
	 */
	public static void file(Path actual, byte[] bytes) throws IOException {
		file(actual, ByteProvider.of(bytes));
	}

	/**
	 * Checks contents of the files matches bytes, with specific failure information if not.
	 */
	public static void file(Path actual, ByteProvider byteProvider) throws IOException {
		equal(Files.size(actual), (long) byteProvider.length(), "Invalid file size");
		byte[] actualBytes = Files.readAllBytes(actual);
		for (int i = 0; i < actualBytes.length; i++)
			if (actualBytes[i] != byteProvider.getByte(i))
				throw failure("File byte mismatch at index %d", i);
	}

	/**
	 * Checks the paths are the same.
	 */
	public static void path(Path actual, String expected, String... more) {
		if (expected == null) equal(actual, null);
		else equal(actual, Paths.newPath(actual, expected, more));
	}

	/**
	 * Assert a collection of paths in non-specific order, using the first path's file system.
	 */
	public static void paths(Collection<Path> actual, String... paths) {
		@SuppressWarnings("resource")
		var fs = Paths.fs(Iterables.first(actual));
		var expected = Streams.of(paths).map(fs::getPath).collect(Collectors.toList());
		unordered(actual, expected);
	}

	// support

	private static void deepEquals(Object lhs, Object rhs, String format, Object... args) {
		if (Objects.deepEquals(lhs, rhs)) return;
		throw failure("%sExpected: %s\n  actual: %s", nl(format, args), str(rhs), str(lhs));
	}

	private static AssertionError unexpected(Object actual, Object expected, String format,
		Object... args) {
		return failure("%sExpected: %s\n  actual: %s", nl(format, args), str(expected),
			str(actual));
	}

	private static void constructorIsPrivate(Class<?> cls) {
		try {
			var constructor = cls.getDeclaredConstructor();
			yes(Modifier.isPrivate(constructor.getModifiers()), "Constructor is not private: %s()",
				cls.getSimpleName());
			constructor.setAccessible(true);
			constructor.newInstance();
			constructor.setAccessible(false);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}

	private static boolean doubleEqual(double value, double other) {
		return Double.doubleToLongBits(value) == Double.doubleToLongBits(other);
	}

	private static Object ensureArray(Object array) {
		yes(array.getClass().isArray(), "Expected an array: %s", array.getClass());
		return array;
	}

	private static void rawArray(Object lhs, Object rhs) {
		if (rhs == null) isNull(lhs);
		else {
			notNull(lhs);
			rawArray(lhs, rhs, DEEP_EQUALS_ITEM);
		}
	}

	private static void rawArray(Object lhs, Object rhs, Item itemAssert) {
		int lhsLen = RawArray.length(ensureArray(lhs));
		int rhsLen = RawArray.length(ensureArray(rhs));
		equal(lhsLen, rhsLen, "Invalid array size");
		for (int i = 0; i < lhsLen; i++)
			index(i, RawArray.get(lhs, i), true, RawArray.get(rhs, i), true, itemAssert);
	}

	private static <T> void list(List<? extends T> lhs, int lhsOffset, List<? extends T> rhs,
		int rhsOffset, int len, Item itemAssert) {
		int lhsLen = lhs.size();
		int rhsLen = rhs.size();
		for (int i = 0; i < len; i++) {
			boolean hasLhs = lhsOffset + i < lhsLen;
			T lhsVal = hasLhs ? lhs.get(lhsOffset + i) : null;
			boolean hasRhs = rhsOffset + i < rhsLen;
			T rhsVal = hasRhs ? rhs.get(rhsOffset + i) : null;
			index(lhsOffset + i, lhsVal, hasLhs, rhsVal, hasRhs, itemAssert);
		}
	}

	private static <T> void index(int i, T lhs, boolean hasLhs, T rhs, boolean hasRhs,
		Item itemAssert) {
		if (!hasLhs && !hasRhs) throw failure("No value at index %d", i);
		if (!hasLhs) throw failure("No value at index %d, expected: %s", i, str(rhs));
		if (!hasRhs) throw failure("Unexpected value at index %d: %s", i, str(lhs));
		itemAssert.item(lhs, rhs, "Index %d", i);
	}

	private static <K, V> void unordered(Map<K, V> subject, List<K> keys, List<V> values) {
		for (int i = 0; i < keys.size(); i++)
			entry(subject, keys.get(i), values.get(i));
		unordered(subject.keySet(), keys);
		unordered(subject.values(), values);
	}

	private static <K, V> void ordered(Map<K, V> subject, List<K> keys, List<V> values) {
		for (int i = 0; i < keys.size(); i++)
			equal(subject.get(keys.get(i)), values.get(i));
		ordered(subject.keySet(), keys);
		ordered(subject.values(), values);
	}

	private static String limitedLines(List<String> lines, int index) {
		int start = Math.max(0, index - LINE_COUNT);
		int end = Math.min(lines.size(), index + LINE_COUNT + 1);
		return Text.addLineNumbers(Lists.sub(lines, start, end - start), start + 1);
	}

	private static void string(String actual, String expected) {
		if (Objects.equals(actual, expected)) return;
		for (int i = 0;; i++) {
			if (i >= actual.length())
				throw unexpected(actual, expected, "Expected [%d]: %s", i, chr(expected, i));
			if (i >= expected.length())
				throw unexpected(actual, expected, "Unexpected [%d]: %s", i, chr(actual, i));
			if (actual.charAt(i) != expected.charAt(i)) throw unexpected(actual, expected,
				"Expected [%d] %s: %s", i, chr(expected, i), chr(actual, i));
		}
	}

	private static String nl(String format, Object... args) {
		var s = Strings.format(format, args);
		return s.isEmpty() ? "" : s + '\n';
	}

	private static String str(Object obj) {
		return switch (obj) {
			case Byte _ -> String.format("%d (0x%1$02x)", obj);
			case Short _ -> String.format("%d (0x%1$04x)", obj);
			case Integer _ -> String.format("%d (0x%1$08x)", obj);
			case Long _ -> String.format("%dL (0x%1$016x)", obj);
			case Float _ -> String.format("%sf", obj);
			case null -> Strings.NULL;
			default -> String.valueOf(obj);
		};
	}

	private static String chr(String s, int index) {
		var c = Chars.at(s, index);
		if (Chars.isPrintable(c)) return String.format("%c (\\u%04x)", c, (int) c);
		return String.format("\\u%04x", (int) c);
	}
}
