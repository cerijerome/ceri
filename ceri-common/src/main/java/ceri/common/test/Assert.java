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
	private static final int LINE_COUNT = 5;
	public static final int APPROX_PRECISION_DEF = 3;

	private Assert() {}

	/**
	 * Selective assertion of an indexed item.
	 */
	private interface IndexedAssert<T, U> {
		void accept(int index, T actualItem, U actual, T expectedItem, U expected);
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
		return new AssertionError(fmt(format, args), t);
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
		throw failure("Nothing thrown, expected: %s", name(superCls));
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
	public static <T extends Throwable> void throwable(Throwable t, Class<T> superCls,
		Functions.Consumer<? super T> test) {
		if (t == null && superCls == null && test == null) return;
		notNull(t, "Throwable");
		if (superCls != null) instance(t, superCls);
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
		if (condition) throw failure(def(format, args, "Expected false"));
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
		if (!condition) throw failure(def(format, args, "Expected true"));
	}

	/**
	 * Fail if the object is not an instance of the class.
	 */
	public static <T> T instance(Object actual, Class<T> expected) {
		if (nullCheck(actual, expected)) return null;
		if (expected.isInstance(actual)) return Reflect.unchecked(actual);
		throw failure("Expected instance of %s: %s", name(expected), name(actual));
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
		throw expected(actual, expected, def(format, args, "Not equal"));
	}

	/**
	 * Fails if the objects are not equal, expanding equality into arrays.
	 */
	public static <T> T deepEqual(T actual, Object expected) {
		return deepEqual(actual, expected, "");
	}

	/**
	 * Fails if the objects are not equal, expanding equality into arrays.
	 */
	public static <T> T deepEqual(T actual, Object expected, String format, Object... args) {
		if (Objects.deepEquals(actual, expected)) return actual;
		throw expected(actual, expected, def(format, args, "Not deep equal"));
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
		if (Objects.equals(unexpected, actual))
			throw failure("%s: %s", def(format, args, "Must not equal"), full(actual));
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
		if (expected != actual) throw expectedItem(Reflect.hashId(expected), expected,
			Reflect.hashId(actual), actual, def(format, args, "Not the same instance"));
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
		if (unexpected == actual)
			throw failure("%s: (%s) %s", def(format, args, "Must not be the same instance"),
				Reflect.hashId(actual), full(actual));
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
		if (actual != null)
			throw failure("%s: %s", def(format, args, "Must be null"), full(actual));
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
		if (actual == null) throw failure(def(format, args, "Must not be null"));
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
		throw expected(actual, expected, def(format, args, "Not equal"));
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
		throw failure("%s%nExpected: %s <= value <= %s%n  Actual: %s",
			def(format, args, "Not within range"), minInclusive, maxInclusive, actual);
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
		throw failure("%s%nExpected: %s <= value <= %s%n  Actual: %s",
			def(format, args, "Not within range"), minInclusive, maxInclusive, actual);
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
		throw failure("%s%nExpected: %s <= value < %s%n  Actual: %s",
			def(format, args, "Not within range"), minInclusive, maxExclusive, actual);
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
		return approx(actual, expected, APPROX_PRECISION_DEF, format, args);
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
		throw failure("%s%nExpected: %s (\u00b1%s)%n  Actual: %s",
			def(format, args, "Not approximately equal"), expected, diff, actual);
	}

	// arrays

	/**
	 * Fails if the array does not equal the given value array.
	 */
	@SafeVarargs
	public static <T> void array(T[] actual, T... expected) {
		rawArray(actual, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(boolean[] actual, boolean... expected) {
		rawArray(actual, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(char[] actual, char... expected) {
		rawArray(actual, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(byte[] actual, byte[] expected) {
		rawArray(actual, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(byte[] actual, int... expected) {
		array(actual, Array.BYTE.of(expected));
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(short[] actual, short[] expected) {
		rawArray(actual, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(short[] actual, int... expected) {
		array(actual, Array.SHORT.of(expected));
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(int[] actual, int... expected) {
		rawArray(actual, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(long[] actual, long... expected) {
		rawArray(actual, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(float[] actual, float... expected) {
		rawArray(actual, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(float[] actual, double... expected) {
		array(actual, Array.FLOAT.of(expected));
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(double[] actual, double... expected) {
		rawArray(actual, expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(ByteProvider actual, byte[] expected) {
		if (nullCheck(actual, expected)) return;
		array(actual.copy(0), expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(ByteProvider actual, int... expected) {
		array(actual, Array.BYTE.of(expected));
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(IntProvider actual, int... expected) {
		notNull(actual);
		array(actual.copy(0), expected);
	}

	/**
	 * Fails if the array does not equal the given value array.
	 */
	public static void array(LongProvider actual, long... expected) {
		notNull(actual);
		array(actual.copy(0), expected);
	}

	/**
	 * Fails if the array does not equal the given value array, with default value precision.
	 */
	public static void approxArray(double[] actual, double... expected) {
		approxArray(APPROX_PRECISION_DEF, actual, expected);
	}

	/**
	 * Fails if the array does not equal the given value array, with specified value precision.
	 */
	public static void approxArray(int precision, double[] actual, double... expected) {
		Assert.<Double, Object>rawArray(actual, expected,
			(i, ai, a, ei, e) -> indexApprox(i, precision, ai, a, ei, e));
	}

	/**
	 * Fails if the array does not equal the given value array, within specified value difference.
	 */
	public static void approxArray(double diff, double[] actual, double... expected) {
		Assert.<Double, Object>rawArray(actual, expected,
			(i, ai, a, ei, e) -> indexApprox(i, ai, a, ei, e, diff));
	}

	// collections

	/**
	 * Fails if the iterator does not have equal elements in order.
	 */
	@SafeVarargs
	public static <T> void iterator(Iterator<? extends T> actual, T... expected) {
		iterator(actual, Lists.wrap(expected));
	}

	/**
	 * Fails if the iterator does not have equal elements in order.
	 */
	public static <T> void iterator(Iterator<? extends T> actual, Iterable<? extends T> expected) {
		if (nullCheck(actual, expected)) return;
		list(Immutable.list(Iterables.of(actual)), Immutable.list(expected));
		no(actual.hasNext(), "Has more elements");
		noSuchElement(actual::next);
	}

	/**
	 * Fails if the list does not have equal elements in order.
	 */
	public static <T> void list(List<? extends T> actual, List<? extends T> expected) {
		if (nullCheck(actual, expected)) return;
		list(actual, 0, expected, 0, actual.size());
		size(actual.size(), actual, expected.size(), expected);
	}

	/**
	 * Fails if the list does not have equal elements in order.
	 */
	public static <T> void list(List<? extends T> actual, int actualOffset,
		List<? extends T> expected, int expectedOffset, int len) {
		if (nullCheck(actual, expected)) return;
		listItems(actual, actualOffset, expected, expectedOffset, len);
	}

	/**
	 * Fails if the map is null or has elements.
	 */
	public static <K, V> void map(Map<K, V> actual) {
		equal(actual, Map.of());
	}

	/**
	 * Fails if the map does not contain exactly the given elements.
	 */
	public static <K, V> void map(Map<K, V> actual, K k, V v) {
		equal(actual, Immutable.mapOf(k, v));
	}

	/**
	 * Fails if the map does not contain exactly the given elements.
	 */
	public static <K, V> void map(Map<K, V> actual, K k0, V v0, K k1, V v1) {
		unordered(actual, Immutable.listOf(k0, k1), Immutable.listOf(v0, v1));
	}

	/**
	 * Fails if the map does not contain exactly the given elements.
	 */
	public static <K, V> void map(Map<K, V> actual, K k0, V v0, K k1, V v1, K k2, V v2) {
		unordered(actual, Immutable.listOf(k0, k1, k2), Immutable.listOf(v0, v1, v2));
	}

	/**
	 * Fails if the map does not contain exactly the given elements.
	 */
	public static <K, V> void map(Map<K, V> actual, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
		V v3) {
		unordered(actual, Immutable.listOf(k0, k1, k2, k3), Immutable.listOf(v0, v1, v2, v3));
	}

	/**
	 * Fails if the map does not contain exactly the given elements.
	 */
	public static <K, V> void map(Map<K, V> actual, K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3,
		K k4, V v4) {
		unordered(actual, Immutable.listOf(k0, k1, k2, k3, k4),
			Immutable.listOf(v0, v1, v2, v3, v4));
	}

	/**
	 * Fails if the map does not contain the key/value entry.
	 */
	public static <K, V> void entry(Map<K, V> actual, K key, V value) {
		notNull(actual);
		if (!Objects.equals(actual.get(key), value)) throw expectedItem(actual.get(key), actual,
			value, Immutable.mapOf(key, value), "Unexpected value for key %s", key);
	}

	/**
	 * Fails if the iterator allows mutation.
	 */
	public static <T> void immutable(Iterator<T> actual) {
		notNull(actual);
		if (!actual.hasNext()) return;
		actual.next();
		unsupportedOp(() -> actual.remove());
	}

	/**
	 * Fails if the collection allows mutation.
	 */
	public static <T, C extends Collection<T>> C immutable(C actual) {
		notNull(actual);
		unsupportedOp(() -> actual.add(null));
		unsupportedOp(() -> actual.addAll(Immutable.setOf((T) null)));
		if (actual instanceof List<?> list) immutableList(list);
		if (actual.isEmpty()) return actual;
		var t = actual.iterator().next();
		unsupportedOp(() -> actual.remove(t));
		unsupportedOp(() -> actual.removeAll(Immutable.setOf(t)));
		unsupportedOp(() -> actual.retainAll(Set.of()));
		unsupportedOp(actual::clear);
		immutable(actual.iterator());
		return actual;
	}

	/**
	 * Fails if the map allows mutation.
	 */
	public static <K, V, M extends Map<K, V>> M immutable(M actual) {
		notNull(actual);
		unsupportedOp(() -> actual.put(null, null));
		unsupportedOp(() -> actual.putAll(Immutable.mapOf(null, null)));
		if (actual.isEmpty()) return actual;
		var k = actual.keySet().iterator().next();
		unsupportedOp(() -> actual.remove(k));
		unsupportedOp(actual::clear);
		immutable(actual.entrySet());
		immutable(actual.keySet());
		immutable(actual.values());
		return actual;
	}

	// streams

	/**
	 * Fails if the stream does does not contain exactly the values in order.
	 */
	@SafeVarargs
	public static <E extends Exception, T> void stream(Stream<E, T> actual, T... expected)
		throws E {
		notNull(actual);
		ordered(actual.toList(), expected);
	}

	/**
	 * Fails if the stream does does not contain exactly the values in order.
	 */
	public static <E extends Exception> void stream(IntStream<E> actual, int... expected) throws E {
		notNull(actual);
		array(actual.toArray(), expected);
	}

	/**
	 * Fails if the stream does does not contain exactly the values in order.
	 */
	public static <E extends Exception> void stream(LongStream<E> actual, long... expected)
		throws E {
		notNull(actual);
		array(actual.toArray(), expected);
	}

	/**
	 * Fails if the stream does does not contain exactly the values in order.
	 */
	public static <E extends Exception> void stream(DoubleStream<E> actual, double... expected)
		throws E {
		notNull(actual);
		array(actual.toArray(), expected);
	}

	// unordered

	/**
	 * Checks collection contains exactly given elements in any order, with specific failure
	 * information if not.
	 */
	@SafeVarargs
	public static <T> void unordered(T[] actual, T... expected) {
		notNull(actual);
		unordered(Lists.wrap(actual), expected);
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(boolean[] actual, boolean... expected) {
		unordered(Array.BOOL.list(actual), Array.BOOL.list(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(byte[] actual, byte... expected) {
		unordered(Array.BYTE.list(actual), Array.BYTE.list(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(byte[] actual, int... expected) {
		unordered(actual, Array.BYTE.of(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(char[] actual, char... expected) {
		unordered(Array.CHAR.list(actual), Array.CHAR.list(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(char[] actual, int... expected) {
		unordered(actual, Array.CHAR.of(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(short[] actual, short... expected) {
		unordered(Array.SHORT.list(actual), Array.SHORT.list(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(short[] actual, int... expected) {
		unordered(actual, Array.SHORT.of(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(int[] actual, int... expected) {
		unordered(Array.INT.list(actual), Array.INT.list(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(long[] actual, long... expected) {
		unordered(Array.LONG.list(actual), Array.LONG.list(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(float[] actual, float... expected) {
		unordered(Array.FLOAT.list(actual), Array.FLOAT.list(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(float[] actual, double... expected) {
		unordered(actual, Array.FLOAT.of(expected));
	}

	/**
	 * Fails if the array does not contain exactly the elements in any order.
	 */
	public static void unordered(double[] actual, double... expected) {
		unordered(Array.DOUBLE.list(actual), Array.DOUBLE.list(expected));
	}

	/**
	 * Fails if the collection does not contain exactly the elements in any order.
	 */
	@SafeVarargs
	public static <T> void unordered(Collection<T> actual, T... expected) {
		notNull(actual);
		unordered(actual, Arrays.asList(expected));
	}

	/**
	 * Fails if the collection does not contain exactly the elements in any order.
	 */
	public static <T> void unordered(Collection<? extends T> actual,
		Collection<? extends T> expected) {
		if (nullCheck(actual, expected)) return;
		int i = 0;
		for (T actualItem : actual) {
			if (!expected.contains(actualItem)) throw expected(actual, expected,
				"Unexpected element at position [%d]: %s", i, full(actualItem));
			i++;
		}
		for (T expectedItem : expected)
			if (!actual.contains(expectedItem))
				throw expected(actual, expected, "Missing element: %s", full(expectedItem));
		size(actual.size(), actual, expected.size(), expected);
	}

	// ordered

	/**
	 * Fails if the iterated values do not equal the elements in order.
	 */
	@SafeVarargs
	public static <T> void ordered(Iterable<T> actual, T... expected) {
		ordered(actual, Arrays.asList(expected));
	}

	/**
	 * Fails if the iterated values do not equal the elements in order.
	 */
	public static <T> void ordered(Iterable<T> actual, Iterable<T> expected) {
		if (nullCheck(actual, expected)) return;
		iterator(actual.iterator(), expected);
	}

	/**
	 * Fails if the map does not contain exactly the keys and values in order.
	 */
	public static <K, V> void ordered(Map<K, V> actual, K k, V v) {
		map(actual, k, v);
	}

	/**
	 * Fails if the map does not contain exactly the keys and values in order.
	 */
	public static <K, V> void ordered(Map<K, V> actual, K k0, V v0, K k1, V v1) {
		ordered(actual, Immutable.listOf(k0, k1), Immutable.listOf(v0, v1));
	}

	/**
	 * Fails if the map does not contain exactly the keys and values in order.
	 */
	public static <K, V> void ordered(Map<K, V> actual, K k0, V v0, K k1, V v1, K k2, V v2) {
		ordered(actual, Immutable.listOf(k0, k1, k2), Immutable.listOf(v0, v1, v2));
	}

	/**
	 * Fails if the map does not contain exactly the keys and values in order.
	 */
	public static <K, V> void ordered(Map<K, V> actual, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
		V v3) {
		ordered(actual, Immutable.listOf(k0, k1, k2, k3), Immutable.listOf(v0, v1, v2, v3));
	}

	/**
	 * Fails if the map does not contain exactly the keys and values in order.
	 */
	public static <K, V> void ordered(Map<K, V> actual, K k0, V v0, K k1, V v1, K k2, V v2, K k3,
		V v3, K k4, V v4) {
		ordered(actual, Immutable.listOf(k0, k1, k2, k3, k4), Immutable.listOf(v0, v1, v2, v3, v4));
	}

	// strings

	/**
	 * Checks string representation is equal to the formatted string.
	 */
	public static void string(Object actual, String format, Object... objs) {
		if (nullCheck(actual, format)) return;
		var actualStr = str(actual);
		var expectedStr = fmt(format, objs);
		if (!Objects.equals(actualStr, expectedStr)) throw expectedChar(actualStr, expectedStr);
	}

	/**
	 * Checks string representation split into lines.
	 */
	public static void lines(Object actual, String... expectedLineArray) {
		notNull(actual);
		var actualLines = Regex.Split.LINE.list(str(actual));
		var expectedLines = Arrays.asList(expectedLineArray);
		int lines = Math.max(actualLines.size(), expectedLines.size());
		for (int line = 0; line < lines; line++) {
			var actualLine = Lists.at(actualLines, line, "");
			var expectedLine = Lists.at(expectedLines, line, "");
			if (!Objects.equals(actualLine, expectedLine))
				throw expectedLine(line, actualLine, actualLines, expectedLine, expectedLines);
		}
	}

	/**
	 * Checks multi-line text, with line-specific failure info.
	 */
	public static void text(Object actual, String format, Object... objs) {
		if (nullCheck(actual, format)) return;
		lines(actual, Regex.Split.LINE.array(fmt(format, objs)));
	}

	/**
	 * Checks string representation contains the formatted string.
	 */
	public static void contains(Object actual, String format, Object... objs) {
		if (nullCheck(actual, format)) return;
		var actualStr = str(actual);
		var expectedStr = fmt(format, objs);
		if (!actualStr.contains(expectedStr))
			throw expected(actualStr, expectedStr, "Must contain string");
	}

	/**
	 * Checks regex match against the string.
	 */
	public static void match(Object actual, String pattern, Object... objs) {
		if (nullCheck(actual, pattern)) return;
		match(actual, Regex.compile(pattern, objs));
	}

	/**
	 * Checks regex match against the string.
	 */
	public static void match(Object actual, Pattern pattern) {
		if (nullCheck(actual, pattern)) return;
		var actualStr = str(actual);
		if (!pattern.matcher(actualStr).matches())
			throw pattern(actualStr, pattern, "Pattern must match");
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
		notNull(actual);
		var actualStr = str(actual);
		if (pattern.matcher(actualStr).matches())
			throw pattern(actualStr, pattern, "Pattern must not match");
	}

	/**
	 * Checks regex find against the string.
	 */
	public static void find(Object actual, String pattern, Object... objs) {
		if (nullCheck(actual, pattern)) return;
		find(actual, Regex.compile(pattern, objs));
	}

	/**
	 * Checks regex find against the string.
	 */
	public static void find(Object actual, Pattern pattern) {
		if (nullCheck(actual, pattern)) return;
		var actualStr = str(actual);
		if (!pattern.matcher(actualStr).find())
			throw pattern(actualStr, pattern, "Pattern not found");
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
		notNull(actual);
		var actualStr = str(actual);
		var matcher = pattern.matcher(actualStr);
		if (matcher.find()) throw pattern(actualStr, pattern, "Pattern found at index [%d]: %s",
			matcher.start(), full(matcher.group()));
	}

	/**
	 * Check ascii read from byte reader.
	 */
	public static void ascii(ByteReader actual, String format, Object... args) {
		if (nullCheck(actual, format)) return;
		var expected = fmt(format, args);
		equal(actual.readAscii(expected.length()), expected);
	}

	// other types

	/**
	 * Fails if the type value fields do not equal the given values.
	 */
	public static <T> void typeValue(TypeValue<T> actual, T type, int value) {
		notNull(actual);
		equal(actual.intValue(), value);
		equal(actual.type(), type);
	}

	/**
	 * Fails if the type value fields do not equal the given values.
	 */
	public static <T> void typeValue(TypeValue<T> actual, T type, int value, String namePattern,
		Object... args) {
		notNull(actual);
		equal(actual.intValue(), value);
		equal(actual.type(), type);
		if (namePattern == null) isNull(actual.name());
		else match(actual.name(), namePattern, args);
	}

	// I/O

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(CharBuffer actual, char... expected) {
		array(Buffers.CHAR.get(actual), expected);
	}

	/**
	 * Checks buffer values against string.
	 */
	public static void buffer(CharBuffer actual, String expected) {
		if (nullCheck(actual, expected)) return;
		buffer(actual, expected.toCharArray());
	}

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(ByteBuffer actual, byte[] expected) {
		array(Buffers.BYTE.get(actual), expected);
	}

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(ByteBuffer actual, int... expected) {
		array(Buffers.BYTE.get(actual), expected);
	}

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(ShortBuffer actual, short[] expected) {
		array(Buffers.SHORT.get(actual), expected);
	}

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(ShortBuffer actual, int... expected) {
		array(Buffers.SHORT.get(actual), expected);
	}

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(IntBuffer actual, int... expected) {
		array(Buffers.INT.get(actual), expected);
	}

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(LongBuffer actual, long... expected) {
		array(Buffers.LONG.get(actual), expected);
	}

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(FloatBuffer actual, float[] expected) {
		array(Buffers.FLOAT.get(actual), expected);
	}

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(FloatBuffer actual, double... expected) {
		array(Buffers.FLOAT.get(actual), expected);
	}

	/**
	 * Checks buffer values against given array.
	 */
	public static void buffer(DoubleBuffer actual, double... expected) {
		array(Buffers.DOUBLE.get(actual), expected);
	}

	/**
	 * Checks bytes read from input stream.
	 */
	public static void read(InputStream actual, ByteProvider expected) throws IOException {
		if (nullCheck(actual, expected)) return;
		read(actual, expected.copy(0));
	}

	/**
	 * Checks bytes read from input stream.
	 */
	public static void read(InputStream actual, int... expected) throws IOException {
		read(actual, Array.BYTE.of(expected));
	}

	/**
	 * Checks bytes read from input stream.
	 */
	public static void read(InputStream actual, byte[] expected) throws IOException {
		notNull(actual);
		array(actual.readNBytes(expected.length), expected);
	}

	/**
	 * Checks if file exists.
	 */
	public static void exists(Path actual, boolean exists) {
		notNull(actual);
		if (Files.exists(actual) == exists) return;
		throw failure(exists ? "Path does not exist: %s" : "Path exists: %s", actual);
	}

	/**
	 * Checks if file exists.
	 */
	public static void dir(Path actual, boolean isDir) {
		notNull(actual);
		if (Files.isDirectory(actual) == isDir) return;
		throw failure(isDir ? "Path is not a directory: %s" : "Path is a directory: %s", actual);
	}

	/**
	 * Checks contents of two directories are equal, with specific failure information if not.
	 */
	public static void dir(Path actual, Path expected) throws IOException {
		if (nullCheck(actual, expected)) return;
		var actualRelative = PathList.of(actual).relative().sort().list();
		var expectedRelative = PathList.of(expected).relative().sort().list();
		ordered(actualRelative, expectedRelative);
		for (var actualPath : actualRelative) {
			var actualFile = actual.resolve(actualPath);
			var expectedFile = expected.resolve(actualPath);
			boolean expectedIsDir = Files.isDirectory(expectedFile);
			dir(actualFile, expectedIsDir);
			if (!expectedIsDir) file(actualFile, expectedFile);
		}
	}

	/**
	 * Checks contents of two files are equal, with specific failure information if not.
	 */
	public static void file(Path actual, Path expected) throws IOException {
		if (nullCheck(actual, expected)) return;
		size(Files.size(actual), actual, Files.size(expected), expected);
		long pos = Files.mismatch(actual, expected);
		if (pos >= 0) throw expected(actual, expected, "File mismatch at offset [%d]", pos);
	}

	/**
	 * Checks contents of the files matches bytes, with specific failure information if not.
	 */
	public static void file(Path actual, int... expected) throws IOException {
		file(actual, ByteProvider.of(expected));
	}

	/**
	 * Checks contents of the files matches bytes, with specific failure information if not.
	 */
	public static void file(Path actual, byte[] expected) throws IOException {
		file(actual, ByteProvider.of(expected));
	}

	/**
	 * Checks contents of the files matches bytes, with specific failure information if not.
	 */
	public static void file(Path actual, ByteProvider expected) throws IOException {
		if (nullCheck(actual, expected)) return;
		size(Files.size(actual), actual, expected.length(), expected);
		var actualBytes = Files.readAllBytes(actual);
		for (int i = 0; i < actualBytes.length; i++)
			if (actualBytes[i] != expected.getByte(i)) throw expectedItem(actualBytes[i], actual,
				expected.getByte(i), expected, "File mismatch at offset [%d]", i);
	}

	/**
	 * Checks the paths are the same.
	 */
	public static void path(Path actual, String expected, String... more) {
		if (nullCheck(actual, expected)) return;
		equal(actual, Paths.newPath(actual, expected, more));
	}

	/**
	 * Assert a collection of paths in non-specific order, using the first path's file system.
	 */
	public static void paths(Collection<Path> actual, String... expected) {
		notNull(actual);
		@SuppressWarnings("resource")
		var fs = Paths.fs(Iterables.first(actual));
		var expectedPaths = Streams.of(expected).map(fs::getPath).collect(Collectors.toList());
		unordered(actual, expectedPaths);
	}

	// support

	private static void constructorIsPrivate(Class<?> cls) {
		try {
			var constructor = cls.getDeclaredConstructor();
			yes(Modifier.isPrivate(constructor.getModifiers()), "Constructor is not private: %s()",
				name(cls));
			// instantiate for code coverage
			constructor.setAccessible(true);
			constructor.newInstance();
			constructor.setAccessible(false);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}

	private static void rawArray(Object actual, Object expected) {
		rawArray(actual, expected, Assert::indexDeepEqual);
	}

	private static <T, U> void rawArray(U actual, U expected, IndexedAssert<T, U> indexedAssert) {
		if (nullCheck(actual, expected)) return;
		int actualLen = RawArray.length(ensureArray(actual));
		int expectedLen = RawArray.length(ensureArray(expected));
		if (actualLen != expectedLen) throw expectedItem(str(actualLen), actual, str(expectedLen),
			expected, "Mismatched array length");
		for (int i = 0; i < expectedLen; i++)
			indexedAssert.accept(i, RawArray.get(actual, i), actual, RawArray.get(expected, i),
				expected);
	}

	private static Object ensureArray(Object array) {
		yes(array.getClass().isArray(), "Expected an array: %s", array.getClass());
		return array;
	}

	private static void indexApprox(int index, int precision, double actualItem, Object actual,
		double expectedItem, Object expected) {
		if (!Double.isFinite(expectedItem))
			indexEqual(index, actualItem, actual, expectedItem, expected);
		else indexEqual(index, Maths.round(precision, actualItem), actual,
			Maths.round(precision, expectedItem), expected);
	}

	private static void indexApprox(int index, double actualItem, Object actual,
		double expectedItem, Object expected, double diff) {
		if (!Double.isFinite(expectedItem))
			indexEqual(index, actualItem, actual, expectedItem, expected);
		else if (Math.abs(expectedItem - actualItem) > diff) throw failure(
			"Not approximately equal at index [%d]%nExpected: %s (\u00b1%s)  %s%n  Actual: %s  %s",
			index, expectedItem, diff, full(expected), actualItem, full(actual));
	}

	private static void indexEqual(int index, Object actualItem, Object actual, Object expectedItem,
		Object expected) {
		if (!Objects.equals(actualItem, expectedItem)) throw expectedItem(actualItem, actual,
			expectedItem, expected, "Not equal at index [%d]", index);
	}

	private static void indexDeepEqual(int index, Object actualItem, Object actual,
		Object expectedItem, Object expected) {
		if (!Objects.deepEquals(actualItem, expectedItem)) throw expectedItem(actualItem, actual,
			expectedItem, expected, "Not equal at index [%d]", index);
	}

	private static <T> void listItems(List<? extends T> actual, int actualOffset,
		List<? extends T> expected, int expectedOffset, int len) {
		int actualLen = actual.size();
		int expectedLen = expected.size();
		for (int i = 0; i < len; i++) {
			var actualItem = Lists.at(actual, actualOffset + i);
			var expectedItem = Lists.at(expected, expectedOffset + i);
			if (!Maths.within(actualOffset + i, 0, actualLen - 1)) throw expectedItem(actualItem,
				actual, expectedItem, expected, "No value at index [%d]", actualOffset + i);
			if (!Maths.within(expectedOffset + i, 0, expectedLen - 1))
				throw expectedItem(actualItem, actual, expectedItem, expected,
					"Unexpected value at index [%d]", actualOffset + i);
			indexDeepEqual(actualOffset + i, actualItem, actual, expectedItem, expected);
		}
	}

	private static <K, V> void unordered(Map<K, V> actual, List<K> keys, List<V> values) {
		for (int i = 0; i < keys.size(); i++)
			entry(actual, keys.get(i), values.get(i));
		unordered(actual.keySet(), keys);
		unordered(actual.values(), values);
	}

	private static <K, V> void ordered(Map<K, V> actual, List<K> keys, List<V> values) {
		for (int i = 0; i < keys.size(); i++)
			equal(actual.get(keys.get(i)), values.get(i));
		ordered(actual.keySet(), keys);
		ordered(actual.values(), values);
	}

	private static void size(long actualSize, Object actual, long expectedSize, Object expected) {
		if (actualSize != expectedSize) throw expectedItem(str(actualSize), actual,
			str(expectedSize), expected, "Mismatched sizes");
	}

	private static boolean nullCheck(Object actual, Object expected) {
		if (expected == null) isNull(actual);
		else notNull(actual);
		return expected == null;
	}

	private static <T> void immutableList(List<T> actual) {
		notNull(actual);
		unsupportedOp(() -> actual.set(0, null));
		unsupportedOp(() -> actual.add(0, null));
		unsupportedOp(() -> actual.addAll(0, Immutable.setOf((T) null)));
		unsupportedOp(() -> actual.remove(0));
		immutable(actual.listIterator());
		immutable(actual.listIterator(0));
	}

	private static AssertionError pattern(String actual, Pattern pattern, String format,
		Object... args) {
		return failure("%s%nPattern: %s%n Actual: %s", fmt(format, args), full(pattern.pattern()),
			full(actual));
	}

	private static AssertionError expected(Object actual, Object expected, String format,
		Object... args) {
		return failure("%s%nExpected: %s%n  Actual: %s", fmt(format, args), full(expected),
			full(actual));
	}

	private static AssertionError expectedItem(Object actualItem, Object actual,
		Object expectedItem, Object expected, String format, Object... args) {
		return failure("%s%nExpected: %s  %s%n  Actual: %s  %s", fmt(format, args),
			full(expectedItem), full(expected), full(actualItem), full(actual));
	}

	private static AssertionError expectedChar(String actual, String expected) {
		for (int i = 0;; i++) {
			var actualChar = Chars.at(actual, i);
			var expectedChar = Chars.at(expected, i);
			if (Objects.equals(actualChar, expectedChar)) continue;
			return failure(
				"Mismatched char at index [%d]%nExpected: %-11s  %s%n  Actual: %-11s  %s", i,
				chr(expectedChar), full(expected), chr(actualChar), actual);
		}
	}

	private static AssertionError expectedLine(int line, String actualLine,
		List<String> actualLines, String expectedLine, List<String> expectedLines) {
		for (int i = 0;; i++) {
			var actualChar = Chars.at(actualLine, i);
			var expectedChar = Chars.at(expectedLine, i);
			if (Objects.equals(actualChar, expectedChar)) continue;
			return failure(
				"Mismatched char at line [%d] index [%d]%n"
					+ "Expected: %-11s  %s%n  Actual: %-11s  %s%n%n"
					+ "Expected passage:%n%s%n%nActual passage:%n%s%n",
				line, i, chr(expectedChar), full(expectedLine), chr(actualChar), full(actualLine),
				passage(expectedLines, line), passage(actualLines, line));
		}
	}

	private static String passage(List<String> lines, int index) {
		int start = Math.max(0, index - LINE_COUNT);
		int end = Math.min(lines.size(), index + LINE_COUNT + 1);
		var list = Lists.sub(lines, start, end - start);
		list = Lists.adapt(l -> full(l), list);
		return Text.addLineNumbers(list, start);
	}

	private static String def(String format, Object[] args, String formatDef, Object... argsDef) {
		return Strings.isEmpty(format) ? fmt(formatDef, argsDef) : fmt(format, args);
	}

	private static String name(Object obj) {
		if (obj instanceof Class<?> c) return Reflect.name(c);
		return Reflect.name(Reflect.getClass(obj));
	}

	private static String str(Object s) {
		return String.valueOf(s);
	}

	private static String fmt(String format, Object... args) {
		return Strings.format(format, args);
	}

	private static String chr(Character c) {
		if (c == null) return "none";
		var esc = Chars.escape(c);
		if (esc.length() > 2) return esc;
		return String.format("%s (\\u%04x)", esc, (int) c);
	}

	private static String full(Object obj) {
		// Distinguish strings shown when object equality fails
		return switch (obj) {
			case Byte _ -> String.format("%d (0x%1$02x)", obj);
			case Short _ -> String.format("%d (0x%1$04x)", obj);
			case Integer _ -> String.format("%d (0x%1$08x)", obj);
			case Long _ -> String.format("%dL (0x%1$016x)", obj);
			case Float _ -> String.format("%sf", obj);
			case CharSequence c -> Chars.escape(c);
			case null -> Strings.NULL;
			default -> RawArray.toString(obj);
		};
	}

	private static boolean doubleEqual(double value, double other) {
		return Double.doubleToLongBits(value) == Double.doubleToLongBits(other);
	}
}
