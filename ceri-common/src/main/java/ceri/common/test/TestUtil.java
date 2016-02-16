package ceri.common.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.hamcrest.Matcher;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsSame;
import org.junit.runner.JUnitCore;
import ceri.common.concurrent.ExceptionRunnable;
import ceri.common.io.IoUtil;
import ceri.common.math.MathUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.PrimitiveUtil;

public class TestUtil {
	private static final int SMALL_BUFFER_SIZE = 1024;
	private static final int BUFFER_SIZE = 1024 * 32;
	private static final int APPROX_DECIMAL_PLACES_DEF = 3;
	private static final Random RND = new Random();

	private TestUtil() {}

	/**
	 * Calls private constructor. Useful for code coverage of utility classes.
	 */
	public static void assertPrivateConstructor(Class<?> cls) {
		try {
			Constructor<?> constructor = cls.getDeclaredConstructor();
			assertTrue("Constructor is not private", Modifier.isPrivate(constructor.getModifiers()));
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
		assertTrue(t0.equals(t1));
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
	 * Checks a double value is correct to 3 decimal places.
	 */
	public static void assertApprox(double value, double expected) {
		assertApprox(value, expected, APPROX_DECIMAL_PLACES_DEF);
	}

	/**
	 * Checks a double value is correct to given number of decimal places.
	 */
	public static void assertApprox(double value, double expected, int decimalPlaces) {
		double approxValue = MathUtil.simpleRound(value, decimalPlaces);
		double approxExpected = MathUtil.simpleRound(expected, decimalPlaces);
		assertThat(approxValue, is(approxExpected));
	}

	/**
	 * Checks double values are correct to 3 decimal places.
	 */
	public static void assertApprox(double[] values, double... expecteds) {
		assertApproxPlaces(values, APPROX_DECIMAL_PLACES_DEF, expecteds);
	}
	
	/**
	 * Checks double values are correct to given number of decimal places.
	 */
	public static void assertApproxPlaces(double[] values, int decimalPlaces, double... expecteds) {
		assertThat("Array size", values.length, is(expecteds.length));
		for (int i = 0; i < values.length; i++) {
			double approxValue = MathUtil.simpleRound(values[i], decimalPlaces);
			double approxExpected = MathUtil.simpleRound(expecteds[i], decimalPlaces);
			assertThat("Expected " + approxExpected + " but value at index " + i + " was " +
				approxValue, approxValue, is(approxExpected));

		}
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
	public static <T> void
		assertCollection(Collection<? extends T> lhs, Collection<? extends T> rhs) {
		int i = 0;
		for (T t : lhs) {
			assertTrue("Unexpected element at position " + i + ": " + t, rhs.contains(t));
			i++;
		}
		for (T t : rhs)
			assertTrue("Missing element: " + t, lhs.contains(t));
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
		assertThat("Expected " + rhs + " but value at index " + index + " was " + lhs, lhs, is(rhs));
	}

	/**
	 * Use this for more flexibility than adding @Test(expected=...)
	 */
	public static void assertException(ExceptionRunnable<Exception> runnable) {
		assertException(Exception.class, runnable);
	}

	/**
	 * Use this for more flexibility than adding @Test(expected=...)
	 */
	public static void assertException(Class<? extends Exception> exceptionCls,
		ExceptionRunnable<?> runnable) {
		try {
			runnable.run();
		} catch (Throwable e) {
			if (exceptionCls.isAssignableFrom(e.getClass())) return;
			throw new AssertionError("Should throw " + exceptionCls.getSimpleName() +
				" but threw " + e);
		}
		throw new AssertionError("Should throw " + exceptionCls.getSimpleName() +
			" but nothing thrown");
	}

	/**
	 * Checks contents of two directories are equal, with specific failure information if not.
	 */
	public static void assertDir(File lhsDir, File rhsDir) throws IOException {
		List<String> lhsFilenames = IoUtil.getFilenames(lhsDir);
		List<String> rhsFilenames = IoUtil.getFilenames(rhsDir);
		assertList(lhsFilenames, rhsFilenames);
		for (String filename : lhsFilenames) {
			File lhsFile = new File(lhsDir, filename);
			File rhsFile = new File(rhsDir, filename);
			assertFile(lhsFile, rhsFile);
		}
	}

	/**
	 * Checks contents of two files are equal, with specific failure information if not.
	 */
	public static void assertFile(File lhsFile, File rhsFile) throws IOException {
		if (lhsFile.isDirectory() && rhsFile.isDirectory()) return;
		assertThat("Expected file size " + rhsFile.length() + " for " + lhsFile + " but was " +
			lhsFile.length(), lhsFile.length(), is(rhsFile.length()));
		byte[] lhsBuffer = new byte[BUFFER_SIZE];
		byte[] rhsBuffer = new byte[BUFFER_SIZE];
		try (InputStream lhsIn = new BufferedInputStream(new FileInputStream(lhsFile));
			InputStream rhsIn = new BufferedInputStream(new FileInputStream(rhsFile));) {
			int totalCount = 0;
			while (true) {
				int lhsCount = IoUtil.fillBuffer(lhsIn, lhsBuffer);
				int rhsCount = IoUtil.fillBuffer(rhsIn, rhsBuffer);
				if (lhsCount == 0 && rhsCount == 0) break;
				assertThat("Expected read count " + (totalCount + rhsCount) + " for file " +
					lhsFile + " but was " + (totalCount + lhsCount), totalCount + lhsCount,
					is(totalCount + rhsCount));
				for (int i = 0; i < lhsCount; i++)
					assertIndex(lhsBuffer[i], rhsBuffer[i], (totalCount + i));
				totalCount += lhsCount;
			}
		}
	}

	/**
	 * Convenience method for creating a regex matcher.
	 */
	public static <T> Matcher<T> matchesRegex(String regex) {
		return new RegexMatcher<>(regex);
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
	public static List<String> toUnixFromFile(Collection<File> files) {
		List<String> unixPaths = new ArrayList<>();
		for (File file : files)
			unixPaths.add(IoUtil.unixPath(file));
		return unixPaths;
	}

	/**
	 * Convert collection of files to list of unix-format paths
	 */
	public static List<String> toUnixFromPath(Collection<String> paths) {
		List<String> unixPaths = new ArrayList<>();
		for (String path : paths)
			unixPaths.add(IoUtil.unixPath(path));
		return unixPaths;
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
			if (!StringUtil.printable(b.charAt(i))) b.setCharAt(i, unreadableChar);
			//if (b.charAt(i) < ' ') b.setCharAt(i, readableChar);
		}
		return b.toString();
	}

}
