package ceri.common.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.hamcrest.Matcher;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsSame;
import org.junit.runner.JUnitCore;
import ceri.common.io.IoUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.StringUtil;

public class TestUtil {
	private static final int BUFFER_SIZE = 1024 * 32;
	private static final Random RND = new Random();

	private TestUtil() {}

	/**
	 * Executes tests and prints names in readable phrases to stdout.
	 */
	public static void exec(Class<?> classes) {
		exec(System.out, classes);
	}

	/**
	 * Executes tests and prints test names in readable phrases.
	 */
	public static void exec(PrintStream out, Class<?> classes) {
		JUnitCore core = new JUnitCore();
		TestPrinter tp = new TestPrinter();
		core.addListener(tp);
		core.run(classes);
		tp.print(out);
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
	public static void assertArray(Object lhs, Object rhs) {
		assertIsArray(lhs);
		assertIsArray(rhs);
		assertSize(Array.getLength(lhs), Array.getLength(rhs));
		assertArray(lhs, 0, rhs, 0, Array.getLength(lhs));
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(Object lhs, int lhsOffset, Object rhs, int rhsOffset, int len) {
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
	 * Checks two lists are equal, with specific failure information if not.
	 */
	public static <T> void assertList(List<? extends T> lhs, List<? extends T> rhs) {
		assertSize(lhs.size(), rhs.size());
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
	 * Checks two collections have equal elements, with specific failure
	 * information if not.
	 */
	public static <T> void assertCollection(Collection<T> lhs, Collection<T> rhs) {
		assertSize(lhs.size(), rhs.size());
		assertElements(lhs, rhs);
	}

	/**
	 * Checks iterable type against given list of items, with specific failure
	 * information if not.
	 */
	@SafeVarargs
	public static <T> void assertElements(Iterable<T> lhs, T...ts) {
		assertElements(lhs, Arrays.asList(ts));
	}
	
	/**
	 * Checks two iterable types have equal elements, with specific failure
	 * information if not.
	 */
	public static <T> void assertElements(Iterable<T> lhs, Iterable<T> rhs) {
		Iterator<T> iLhs = lhs.iterator();
		Iterator<T> iRhs = rhs.iterator();
		int i = 0;
		while (iLhs.hasNext()) {
			assertTrue("Expected maximum size of " + i, iRhs.hasNext());
			T lhsVal = iLhs.next();
			T rhsVal = iRhs.next();
			assertIndex(lhsVal, rhsVal, i++);
		}
		assertFalse("Expected minimum size of " + (i + 1), iRhs.hasNext());
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	@SafeVarargs
	public static <T> void assertElements(T[] lhs, T...ts) {
		assertElements(Arrays.asList(lhs), ts);
	}

	private static void assertSize(long lhsSize, long rhsSize) {
		assertThat("Expected size " + rhsSize + " but was " + lhsSize, lhsSize, is(rhsSize));
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
	public static void assertException(Runnable runnable) {
		assertException(Exception.class, runnable);
	}

	/**
	 * Use this for more flexibility than adding @Test(expected=...)
	 */
	public static void assertException(TestRunnable runnable) {
		assertException(Exception.class, runnable);
	}

	/**
	 * Use this for more flexibility than adding @Test(expected=...)
	 */
	public static void assertException(Class<? extends Exception> exceptionCls, Runnable runnable) {
		assertException(exceptionCls, runnable(runnable));
	}

	/**
	 * Use this for more flexibility than adding @Test(expected=...)
	 */
	public static void assertException(Class<? extends Exception> exceptionCls,
		TestRunnable runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			if (exceptionCls.isAssignableFrom(e.getClass())) return;
			fail("Should throw " + exceptionCls.getSimpleName() + " but threw " + e);
		}
		fail("Should throw " + exceptionCls.getSimpleName() + " but nothing thrown");
	}

	private static TestRunnable runnable(final Runnable runnable) {
		return new TestRunnable() {
			@Override
			public void run() throws Exception {
				runnable.run();
			}
		};
	}

	/**
	 * Checks contents of two directories are equal, with specific failure
	 * information if not.
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
	 * Checks contents of two files are equal, with specific failure information
	 * if not.
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
	 * Version of CoreMatchers.is(Class<T>) that checks for class, not instance
	 * of class.
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
	 * Converts a byte array to string, with non-visible chars converted to
	 * given char.
	 */
	public static String toReadableString(byte[] array, int offset, int len, String charset,
		char readableChar) {
		StringBuilder b = new StringBuilder();
		try {
			if (charset == null || charset.isEmpty()) b.append(new String(array, offset, len));
			b.append(new String(array, offset, len, charset));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
		for (int i = 0; i < b.length(); i++) {
			if(!StringUtil.printable(b.charAt(i))) b.setCharAt(i, readableChar);
			//if (b.charAt(i) < ' ') b.setCharAt(i, readableChar);
		}
		return b.toString();
	}

}
