package ceri.common.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.hamcrest.Matcher;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsSame;
import org.junit.runner.JUnitCore;
import ceri.common.io.IoUtil;
import ceri.common.util.BasicUtil;

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
	 * Executes tests and prints names in readable phrases.
	 */
	public static void exec(PrintStream out, Class<?> classes) {
		JUnitCore core = new JUnitCore();
		TestPrinter tp = new TestPrinter();
		core.addListener(tp);
		core.run(classes);
		tp.print(out);
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(Object lhs, Object rhs) {
		assertThat("Array lengths are not equal", Array.getLength(lhs), is(Array.getLength(rhs)));
		assertArray(lhs, 0, rhs, 0, Array.getLength(lhs));
	}

	/**
	 * Checks two arrays are equal, with specific failure information if not.
	 */
	public static void assertArray(Object lhs, int lhsOffset, Object rhs, int rhsOffset, int len) {
		for (int i = 0; i < len; i++) {
			Object lhsVal = Array.get(lhs, lhsOffset + i);
			Object rhsVal = Array.get(rhs, rhsOffset + i);
			assertThat("Value at index " + (lhsOffset + i) + " doesn't match value at index " +
				(rhsOffset + i), lhsVal, is(rhsVal));
		}
	}

	/**
	 * Checks two lists are equal, with specific failure information if not.
	 */
	public static <T> void assertList(List<? extends T> lhs, List<? extends T> rhs) {
		assertThat("List lengths are not equal", lhs.size(), is(rhs.size()));
		assertList(lhs, 0, rhs, 0, lhs.size());
	}

	/**
	 * Checks two lists are equal, with specific failure information if not.
	 */
	public static <T> void assertList(List<? extends T> lhs, int lhsOffset, List<? extends T> rhs,
		int rhsOffset, int len) {
		for (int i = 0; i < len; i++) {
			T lhsVal = lhs.get(lhsOffset + i);
			T rhsVal = rhs.get(rhsOffset + i);
			assertThat("Value at index " + (lhsOffset + i) + " doesn't match value at index " +
				(rhsOffset + i), lhsVal, is(rhsVal));
		}
	}

	/**
	 * Use this for more flexibility than adding @Test(expected=...)
	 */
	public static void assertException(Class<? extends Exception> exceptionCls, Runnable runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			if (exceptionCls.isAssignableFrom(e.getClass())) return;
			fail("Should throw " + exceptionCls.getSimpleName() + " but threw " + e);
		}
		fail("Should throw " + exceptionCls.getSimpleName());
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
		assertThat("Sizes are not equal for files " + lhsFile + " and " + rhsFile,
			lhsFile.length(), is(rhsFile.length()));
		byte[] lhsBuffer = new byte[BUFFER_SIZE];
		byte[] rhsBuffer = new byte[BUFFER_SIZE];
		InputStream lhsIn = new BufferedInputStream(new FileInputStream(lhsFile));
		InputStream rhsIn = new BufferedInputStream(new FileInputStream(rhsFile));
		try {
			int totalCount = 0;
			while (true) {
				int lhsCount = IoUtil.fillBuffer(lhsIn, lhsBuffer);
				int rhsCount = IoUtil.fillBuffer(rhsIn, rhsBuffer);
				if (lhsCount == 0 && rhsCount == 0) break;
				assertThat("Read counts do not match for files " + lhsFile + " and " + rhsFile,
					totalCount + lhsCount, is(totalCount + rhsCount));
				for (int i = 0; i < lhsCount; i++) {
					assertThat("Bytes at index " + (totalCount + lhsCount + i) +
						" do not match for files " + lhsFile + " and " + rhsFile, lhsBuffer[i],
						is(rhsBuffer[i]));
				}
				totalCount += lhsCount;
			}
		} finally {
			IoUtil.close(lhsIn);
			IoUtil.close(rhsIn);
		}
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
	 * Create a random string of given size.
	 */
	public static String randomString(long size) {
		StringBuilder b = new StringBuilder();
		while (size-- > 0) {
			char ch = (char)(RND.nextInt(64) + ' ');
			b.append(ch);
		}
		return b.toString();
	}
	
	/**
	 * Converts a byte array to string, with non-visible chars converted to
	 * given char.
	 */
	public static String toReadableString(byte[] array) {
		return toReadableString(array, 0, array.length);
	}

	/**
	 * Converts a byte array to string, with non-visible chars converted to
	 * given char.
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
			if (b.charAt(i) < ' ') b.setCharAt(i, readableChar);
		}
		return b.toString();
	}

}
