package ceri.common.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.hamcrest.Matcher;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsSame;
import org.junit.runner.JUnitCore;
import ceri.common.collection.ByteProvider;
import ceri.common.collection.ImmutableUtil;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.FunctionUtil;
import ceri.common.io.IoUtil;
import ceri.common.math.MathUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.PrimitiveUtil;

public class TestUtil {
	private static final int SMALL_BUFFER_SIZE = 1024;
	public static final int APPROX_PRECISION_DEF = 3;
	private static final String LAMBDA_NAME = "[lambda]";
	private static final Random RND = new Random();

	private TestUtil() {}

	/**
	 * Calls private constructor. Useful for code coverage of utility classes.
	 */
	public static void assertPrivateConstructor(Class<?> cls) {
		try {
			Constructor<?> constructor = cls.getDeclaredConstructor();
			assertTrue("Constructor is not private",
				Modifier.isPrivate(constructor.getModifiers()));
			constructor.setAccessible(true);
			constructor.newInstance();
			constructor.setAccessible(false);
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException
			| InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Executes tests and prints names in readable phrases to stdout.
	 */
	public static void exec(Class<?>... classes) {
		exec(System.out, classes);
	}

	/**
	 * Executes tests and prints test names in readable phrases.
	 */
	public static void exec(PrintStream out, Class<?>... classes) {
		JUnitCore core = new JUnitCore();
		TestPrinter tp = new TestPrinter();
		core.addListener(tp);
		core.run(classes);
		tp.print(out);
	}

	public static String firstSystemPropertyName() {
		Set<Object> keys = System.getProperties().keySet();
		return BasicUtil.conditional(keys.isEmpty(), "", String.valueOf(keys.iterator().next()));
	}

	public static String firstSystemProperty() {
		return System.getProperty(firstSystemPropertyName());
	}

	public static String firstEnvironmentVariableName() {
		Set<String> keys = System.getenv().keySet();
		return BasicUtil.conditional(keys.isEmpty(), "", keys.iterator().next());
	}

	public static String firstEnvironmentVariable() {
		return System.getenv(firstEnvironmentVariableName());
	}

	/**
	 * Reads a string resource from the caller's package with given name.
	 */
	public static String resource(String name) {
		Class<?> cls = ReflectUtil.previousCaller(1).cls();
		return init(() -> IoUtil.resourceString(cls, name));
	}

	/**
	 * Used to initialize a variable without the need to handle checked exceptions.
	 */
	public static <T> T init(Callable<T> callable) {
		try {
			return callable.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Simple map creation with alternating keys and values.
	 */
	public static <K, V> Map<K, V> testMap(Object... objs) {
		Map<K, V> map = new LinkedHashMap<>();
		int i = 0;
		while (i < objs.length) {
			K key = BasicUtil.uncheckedCast(objs[i++]);
			V value = i < objs.length ? BasicUtil.uncheckedCast(objs[i++]) : null;
			map.put(key, value);
		}
		return map;
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
	 * Checks equals, hashCode and toString methods against first object.
	 */
	@SafeVarargs
	public static <T> void exerciseEquals(T t0, T... ts) {
		exerciseEqual(t0, t0);
		for (T t : ts)
			exerciseEqual(t0, t);
		assertNotEquals(t0, null);
		assertNotEquals(t0, new Object());
	}

	private static <T> void exerciseEqual(T t0, T t1) {
		assertEquals(t0, t1);
		assertThat(t0.hashCode(), is(t1.hashCode()));
		assertThat(t0.toString(), is(t1.toString()));
	}

	/**
	 * Call this for code coverage of enum hidden bytecode.
	 */
	public static void exerciseEnum(Class<? extends Enum<?>> enumClass) {
		try {
			for (Object o : (Object[]) enumClass.getMethod("values").invoke(null))
				enumClass.getMethod("valueOf", String.class).invoke(null, o.toString());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Call this for code coverage of switch with strings. Bytecode checks hashes then values, but
	 * the value check is only 1 of 2 branches. The methods calls the code using strings with same
	 * hashes but different values.
	 */
	public static <E extends Exception> void exerciseSwitch(ExceptionConsumer<E, String> consumer,
		String... strings) throws E {
		for (String s : strings)
			consumer.accept("\0" + s);
	}

	/**
	 * Reads a string from given stdin.
	 */
	public static String readString() {
		try {
			return readString(System.in);
		} catch (IOException e) {
			throw new RuntimeException("Shouldn't happen", e);
		}
	}

	/**
	 * Reads a string from given input stream.
	 */
	public static String readString(InputStream in) throws IOException {
		byte[] buffer = new byte[SMALL_BUFFER_SIZE];
		int n = in.read(buffer);
		if (n < 1) return "";
		return new String(buffer, 0, n).trim();
	}

	/**
	 * Checks a double value is NaN.
	 */
	public static void assertNaN(double value) {
		assertNaN("Expected NaN: " + value, value);
	}

	/**
	 * Checks a double value is NaN.
	 */
	public static void assertNaN(String reason, double value) {
		assertTrue(reason, Double.isNaN(value));
	}

	/**
	 * Checks a double value is correct to 3 decimal places.
	 */
	public static void assertApprox(double value, double expected) {
		assertApprox("", value, expected);
	}

	/**
	 * Checks a double value is correct to 3 decimal places.
	 */
	public static void assertApprox(String reason, double value, double expected) {
		assertApprox(reason, value, expected, APPROX_PRECISION_DEF);
	}

	/**
	 * Checks a double value is correct to given number of digits after the decimal separator.
	 */
	public static void assertApprox(double value, double expected, int precision) {
		assertApprox("", value, expected, precision);
	}

	/**
	 * Checks a double value is correct to given number of digits after the decimal separator.
	 */
	public static void assertApprox(String reason, double value, double expected, int precision) {
		if (!Double.isFinite(expected) || !Double.isFinite(value)) {
			assertThat(reason, value, is(expected));
		} else {
			double approxValue = MathUtil.simpleRound(value, precision);
			double approxExpected = MathUtil.simpleRound(expected, precision);
			assertThat(reason, approxValue, is(approxExpected));
		}
	}

	/**
	 * Checks double values are correct to 3 decimal places.
	 */
	public static void assertApprox(double[] values, double... expecteds) {
		assertApproxPrecision(values, APPROX_PRECISION_DEF, expecteds);
	}

	/**
	 * Checks double values are correct to given number of decimal places.
	 */
	public static void assertApproxPrecision(double[] values, int precision, double... expecteds) {
		assertThat("Array size", values.length, is(expecteds.length));
		for (int i = 0; i < values.length; i++)
			assertApprox("Index " + i, values[i], expecteds[i], precision);
	}

	/**
	 * Convenience method to check byte value.
	 */
	public static void assertByte(byte value, int expected) {
		assertThat(value, is((byte) expected));
	}

	/**
	 * Checks a value is within given range, with detailed failure information if not.
	 */
	public static void assertRange(long value, long min, long max) {
		assertTrue("Expected >= " + min + " but was " + value, value >= min);
		assertTrue("Expected <= " + max + " but was " + value, value <= max);
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
		assertMinSize(Array.getLength(lhs), lhsOffset + len);
		for (int i = 0; i < len; i++) {
			Object lhsVal = Array.get(lhs, lhsOffset + i);
			Object rhsVal = Array.get(rhs, rhsOffset + i);
			assertIndex(lhsVal, rhsVal, lhsOffset + i);
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
	public static void assertArray(byte[] array, byte... expected) {
		assertArrayObject(array, expected);
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(byte[] array, int... values) {
		byte[] expected = new byte[values.length];
		for (int i = 0; i < values.length; i++)
			expected[i] = (byte) values[i];
		assertArray(array, expected);
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(short[] array, short... expected) {
		assertArrayObject(array, expected);
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(short[] array, int... values) {
		short[] expected = new short[values.length];
		for (int i = 0; i < values.length; i++)
			expected[i] = (short) values[i];
		assertArray(array, expected);
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
	 * Checks two lists are equal, with specific failure information if not.
	 */
	public static <T> void assertList(List<? extends T> lhs, List<? extends T> rhs) {
		assertSize("List size", lhs.size(), rhs.size());
		assertList(lhs, 0, rhs, 0, lhs.size());
	}

	/**
	 * Checks two lists are equal, with specific failure information if not.
	 */
	public static <T> void assertList(List<? extends T> lhs, int lhsOffset, List<? extends T> rhs,
		int rhsOffset, int len) {
		assertMinSize(lhs.size(), lhsOffset + len);
		for (int i = 0; i < len; i++) {
			T lhsVal = lhs.get(lhsOffset + i);
			T rhsVal = rhs.get(rhsOffset + i);
			assertIndex(lhsVal, rhsVal, lhsOffset + i);
		}
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
		assertCollection(PrimitiveUtil.asList(lhs), PrimitiveUtil.asList(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(byte[] lhs, byte... expected) {
		assertCollection(PrimitiveUtil.asList(lhs), PrimitiveUtil.asList(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(byte[] lhs, int... values) {
		byte[] expected = new byte[values.length];
		for (int i = 0; i < values.length; i++)
			expected[i] = (byte) values[i];
		assertCollection(lhs, expected);
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(char[] lhs, char... expected) {
		assertCollection(PrimitiveUtil.asList(lhs), PrimitiveUtil.asList(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(short[] lhs, short... expected) {
		assertCollection(PrimitiveUtil.asList(lhs), PrimitiveUtil.asList(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(int[] lhs, int... expected) {
		assertCollection(PrimitiveUtil.asList(lhs), PrimitiveUtil.asList(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(long[] lhs, long... expected) {
		assertCollection(PrimitiveUtil.asList(lhs), PrimitiveUtil.asList(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(float[] lhs, float... expected) {
		assertCollection(PrimitiveUtil.asList(lhs), PrimitiveUtil.asList(expected));
	}

	/**
	 * Checks array contains exactly given elements in any order, with specific failure information
	 * if not.
	 */
	public static void assertCollection(double[] lhs, double... expected) {
		assertCollection(PrimitiveUtil.asList(lhs), PrimitiveUtil.asList(expected));
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
			assertTrue("Unexpected element at position " + i + ": " + t, rhs.contains(t));
			i++;
		}
		for (T t : rhs)
			assertTrue("Missing element: " + t, lhs.contains(t));
		assertEquals("Collection size", rhs.size(), lhs.size());
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
		assertThat(message, lhsSize, is(rhsSize));
	}

	private static void assertMinSize(long lhsSize, long minSize) {
		assertTrue("Expected size to be at least " + minSize + " but was " + lhsSize,
			lhsSize >= minSize);
	}

	private static void assertIsArray(Object array) {
		assertTrue("Expected an array but was " + array.getClass(), array.getClass().isArray());
	}

	private static <T> void assertIndex(T lhs, T rhs, int index) {
		assertThat(
			"Expected " + toString(rhs) + " but value at index " + index + " was " + toString(lhs),
			lhs, is(rhs));
	}

	private static <T> String toString(T t) {
		if (ReflectUtil.instanceOfAny(t, Byte.class, Short.class, Integer.class, Long.class))
			return String.format("%1$d (0x%1$02x)", t);
		return String.valueOf(t);
	}

	public static <K, V> void assertMap(Map<K, V> subject) {
		assertThat(subject, is(Map.of()));
	}

	public static <K, V> void assertMap(Map<K, V> subject, K k, V v) {
		assertThat(subject, is(ImmutableUtil.asMap(k, v)));
	}

	public static <K, V> void assertMap(Map<K, V> subject, K k0, V v0, K k1, V v1) {
		assertThat(subject, is(ImmutableUtil.asMap(k0, v0, k1, v1)));
	}

	@SafeVarargs
	public static <T> void assertStream(Stream<T> stream, T... ts) {
		assertArray(stream.toArray(), ts);
	}

	public static void assertStream(IntStream stream, int... is) {
		assertArray(stream.toArray(), is);
	}

	/**
	 * Capture and return any thrown exception.
	 */
	public static Throwable thrown(ExceptionRunnable<?> runnable) {
		try {
			runnable.run();
			return null;
		} catch (Throwable t) {
			return t;
		}
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
	public static void assertThrowable(Throwable t, Class<? extends Throwable> superCls,
		Predicate<String> messageTest) {
		if (t == null && superCls == null && messageTest == null) return;
		assertNotNull("Throwable is null", t);
		if (superCls != null && !superCls.isAssignableFrom(t.getClass())) throw new AssertionError(
			"Expected " + superCls.getName() + ": " + t.getClass().getName());
		if (messageTest == null) return;
		assertTrue(String.format("Unmatched message %s: %s", lambdaName(messageTest), //
			t.getMessage()), messageTest.test(t.getMessage()));
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
		throw new AssertionError("Nothing thrown, expected: " + superCls.getName());
	}

	/**
	 * Checks the string matches regex.
	 */
	public static void assertRegex(String actual, String pattern, Object... objs) {
		assertRegex(actual, RegexUtil.compile(pattern, objs));
	}

	/**
	 * Checks the string matches regex.
	 */
	public static void assertRegex(String actual, Pattern pattern) {
		assertThat(actual, matchesRegex(pattern));
	}

	/**
	 * Check if file exists.
	 */
	public static void assertExists(Path path, boolean exists) {
		assertThat("Path exists: " + path, Files.exists(path), is(exists));
	}
	
	/**
	 * Check if file exists.
	 */
	public static void assertDir(Path path, boolean isDir) {
		assertThat("Path is a directory: " + path, Files.isDirectory(path), is(isDir));
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
			assertThat(lhsFile + " is directory", lhsIsDir, is(rhsIsDir));
			if (!rhsIsDir) assertFile(lhsFile, rhsFile);
		}
	}

	/**
	 * Checks contents of two files are equal, with specific failure information if not.
	 */
	public static void assertFile(Path actual, Path expected) throws IOException {
		assertThat("File size", Files.size(actual), is(Files.size(expected)));
		long pos = Files.mismatch(actual, expected);
		if (pos >= 0) throw new AssertionError("Byte mismatch at index " + pos);
	}

	/**
	 * Checks contents of the files matches bytes, with specific failure information if not.
	 */
	public static void assertFile(Path actual, int... bytes) throws IOException {
		assertFile(actual, ByteProvider.wrap(bytes));
	}

	/**
	 * Checks contents of the files matches bytes, with specific failure information if not.
	 */
	public static void assertFile(Path actual, byte[] bytes) throws IOException {
		assertFile(actual, ByteProvider.wrap(bytes));
	}

	/**
	 * Checks contents of the files matches bytes, with specific failure information if not.
	 */
	public static void assertFile(Path actual, ByteProvider byteProvider) throws IOException {
		assertThat("File size", Files.size(actual), is((long) byteProvider.length()));
		byte[] actualBytes = Files.readAllBytes(actual);
		for (int i = 0; i < actualBytes.length; i++)
			if (actualBytes[i] != byteProvider.get(i))
				throw new AssertionError("Byte mismatch at index " + i);
	}

	/**
	 * Checks the paths are the same.
	 */
	public static void assertPath(Path actual, String expected, String... more) {
		assertThat(actual, is(actual.getFileSystem().getPath(expected, more)));
	}

	/**
	 * Assert a collection of paths, using the first path's file system.
	 */
	public static void assertPaths(Collection<Path> actual, String... paths) {
		if (actual.isEmpty()) {
			assertEquals("Path count", 0, paths.length);
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
	 * Convenience method for creating a regex matcher.
	 */
	public static <T> Matcher<T> matchesRegex(String format, Object... objs) {
		return matchesRegex(RegexUtil.compile(format, objs));
	}

	/**
	 * Convenience method for creating a regex matcher.
	 */
	public static <T> Matcher<T> matchesRegex(Pattern pattern) {
		return new RegexMatcher<>(pattern);
	}

	/**
	 * Convenience method for creating a same matcher.
	 */
	public static <T> Matcher<T> isSame(T t) {
		return new IsSame<>(t);
	}

	/**
	 * Convenience method for creating an array matcher.
	 */
	@SafeVarargs
	public static <T> Matcher<T[]> isArray(T... ts) {
		return Is.is(ts);
	}

	/**
	 * Convenience method for creating a list matcher.
	 */
	@SafeVarargs
	public static <T> Matcher<List<T>> isList(T... ts) {
		return Is.is(Arrays.asList(ts));
	}

	/**
	 * Version of CoreMatchers.is(Class<T>) that checks for class, not instance of class.
	 */
	public static <T> Matcher<T> isClass(Class<?> cls) {
		IsSame<?> isSame = new IsSame<>(cls);
		return BasicUtil.uncheckedCast(isSame);
	}

	/**
	 * Untyped version of CoreMatchers.is(T), useful for primitives.
	 */
	public static <T> Matcher<T> isObject(Object obj) {
		Matcher<Object> is = Is.is(obj);
		return BasicUtil.uncheckedCast(is);
	}

	/**
	 * Convert collection of files to list of unix-format paths
	 */
	public static List<String> pathsToUnix(Collection<Path> paths) {
		return paths.stream().map(IoUtil::pathToUnix).collect(Collectors.toList());
	}

	/**
	 * Convert collection of files to list of unix-format paths
	 */
	public static List<String> pathNamesToUnix(Collection<String> pathNames) {
		return pathNames.stream().map(IoUtil::pathToUnix).collect(Collectors.toList());
	}

	/**
	 * Creates an input stream based based on given data. Use a value of -1 for an early EOF, -2 to
	 * throw an IOException, -3 to throw a RuntimeException, otherwise (byte & 0xff). Buffered reads
	 * will often squash an IOException when it is not the first byte read. To be sure of a thrown
	 * exception, use -2, -2.
	 */
	public static InputStream inputStream(int... bytes) {
		return TestInputStream.of(bytes);
	}

	/**
	 * Returns "[lambda]" if anonymous lambda, otherwise toString.
	 */
	public static String lambdaName(Object lambda) {
		if (FunctionUtil.isAnonymousLambda(lambda)) return LAMBDA_NAME;
		return String.valueOf(lambda);
	}

	/**
	 * Create a random string of given size.
	 */
	public static String randomString(long size) {
		StringBuilder b = new StringBuilder();
		while (size-- > 0) {
			char ch = (char) (RND.nextInt(64) + ' ');
			b.append(ch);
		}
		return b.toString();
	}

	/**
	 * Converts a byte array to string, with non-visible chars converted to '?'.
	 */
	public static String toReadableString(byte[] array) {
		return toReadableString(array, 0, array.length);
	}

	/**
	 * Converts a byte array to string, with non-visible chars converted to '?'.
	 */
	public static String toReadableString(byte[] array, int offset, int len) {
		return toReadableString(array, offset, len, "UTF8", '?');
	}

	/**
	 * Converts a byte array to string, with non-visible chars converted to given char.
	 */
	public static String toReadableString(byte[] array, int offset, int len, String charset,
		char unreadableChar) {
		StringBuilder b = new StringBuilder();
		try {
			if (charset == null || charset.isEmpty()) b.append(new String(array, offset, len));
			else b.append(new String(array, offset, len, charset));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
		for (int i = 0; i < b.length(); i++) {
			if (!StringUtil.isPrintable(b.charAt(i))) b.setCharAt(i, unreadableChar);
			// if (b.charAt(i) < ' ') b.setCharAt(i, readableChar);
		}
		return b.toString();
	}

	private static Predicate<String> equalsPredicate(String s) {
		return FunctionUtil.named(Predicate.isEqual(s), "\"" + s + "\"");
	}

}
