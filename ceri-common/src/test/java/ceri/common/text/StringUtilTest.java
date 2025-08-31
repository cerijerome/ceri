package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.util.Align;

public class StringUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(StringUtil.class);
	}

	@Test
	public void testReplaceUnprintable() {
		assertNull(StringUtil.printable(null));
		assertEquals(StringUtil.printable(""), "");
		assertEquals(StringUtil.printable("ab\0c\u2081"), "ab.c\u2081");
		assertNull(StringUtil.replaceUnprintable(null, _ -> "."));
		assertEquals(StringUtil.replaceUnprintable("ab\0\1\2c", c -> String.valueOf((int) c)),
			"ab012c");
	}

	@Test
	public void testEqualsIgnoreCase() {
		assertTrue(StringUtil.equalsIgnoreCase(null, null));
		assertFalse(StringUtil.equalsIgnoreCase(null, "abc"));
		assertFalse(StringUtil.equalsIgnoreCase(new StringBuilder(), null));
		assertTrue(StringUtil.equalsIgnoreCase(new StringBuilder("AbC"), "abc"));
	}

	@Test
	public void testStartsWith() {
		assertFalse(StringUtil.startsWith(new StringBuilder("x"), null));
		assertFalse(StringUtil.startsWith(new StringBuilder("x"), 0, null));
		assertTrue(StringUtil.startsWith(new StringBuilder("abc"), ""));
		assertTrue(StringUtil.startsWith(new StringBuilder("abc"), 1, ""));
		assertFalse(StringUtil.startsWith(new StringBuilder("abc"), 4, ""));
		assertTrue(StringUtil.startsWith(new StringBuilder("abc"), "a"));
		assertTrue(StringUtil.startsWith(new StringBuilder("abc"), "ab"));
		assertTrue(StringUtil.startsWith(new StringBuilder("abc"), "abc"));
		assertTrue(StringUtil.startsWith(new StringBuilder("abc"), 1, "b"));
		assertTrue(StringUtil.startsWith(new StringBuilder("abc"), 1, "bc"));
	}

	@Test
	public void testStartsWithIgnoreCase() {
		assertFalse(StringUtil.startsWithIgnoreCase((String) null, null));
		assertFalse(StringUtil.startsWithIgnoreCase((String) null, ""));
		assertFalse(StringUtil.startsWithIgnoreCase((String) null, "abCDeF"));
		assertFalse(StringUtil.startsWithIgnoreCase("", null));
		assertTrue(StringUtil.startsWithIgnoreCase("", ""));
		assertFalse(StringUtil.startsWithIgnoreCase("", "abCDeF"));
		assertFalse(StringUtil.startsWithIgnoreCase("abcd", null));
		assertTrue(StringUtil.startsWithIgnoreCase("abcd", ""));
		assertFalse(StringUtil.startsWithIgnoreCase("abcd", "abCDeF"));
		assertTrue(StringUtil.startsWithIgnoreCase("abcdef", "abCDeF"));
		assertTrue(StringUtil.startsWithIgnoreCase("ABCdefGHI", "abCDeF"));
		assertFalse(StringUtil.startsWithIgnoreCase(new StringBuilder("ABCdefGHI"), null));
		assertTrue(StringUtil.startsWithIgnoreCase(new StringBuilder("ABCdefGHI"), "abCDeF"));
		assertTrue(StringUtil.startsWithIgnoreCase(new StringBuilder("ABCdefGHI"), 3, "DeF"));
	}

	@Test
	public void testMatchAt() {
		assertEquals(StringUtil.matchAt(null, 0, ""), false);
		assertEquals(StringUtil.matchAt("", 0, null), false);
		assertEquals(StringUtil.matchAt("abc", -1, ""), false);
		assertEquals(StringUtil.matchAt("abc", 0, ""), true);
		assertEquals(StringUtil.matchAt("abc", 1, ""), true);
		assertEquals(StringUtil.matchAt("abc", 3, ""), true);
		assertEquals(StringUtil.matchAt("abc", 4, ""), false);
		assertEquals(StringUtil.matchAt("ab", 0, "abc"), false);
		assertEquals(StringUtil.matchAt("ab", 0, "ab"), true);
		assertEquals(StringUtil.matchAt("abc", 0, "bc"), false);
		assertEquals(StringUtil.matchAt("abc", 1, "bc"), true);
	}

	@Test
	public void testRegionMatches() {
		assertFalse(StringUtil.regionMatches(null, false, 0, null, 0, 0));
		assertFalse(StringUtil.regionMatches(new StringBuilder(), false, 0, null, 0, 0));
		assertFalse(StringUtil.regionMatches(new StringBuilder("x"), false, 2, "", 0, 0));
		assertFalse(StringUtil.regionMatches(new StringBuilder("xx"), false, 0, "x", 0, 2));
		assertTrue(StringUtil.regionMatches(new StringBuilder("abcdef"), true, 1, "xBcD", 1, 3));
		assertFalse(StringUtil.regionMatches(new StringBuilder("abcdef"), false, 1, "xBcD", 1, 3));
	}

	@Test
	public void testPaddingNumbers() {
		assertEquals(StringUtil.pad(100, 5), "00100");
		assertEquals(StringUtil.pad(-100, 5), "-0100");
		assertEquals(StringUtil.pad(Long.MAX_VALUE, 0), "9223372036854775807");
		assertEquals(StringUtil.pad(Long.MAX_VALUE, 20), "09223372036854775807");
		assertEquals(StringUtil.pad(Long.MIN_VALUE, 20), "-9223372036854775808");
		assertEquals(StringUtil.pad(Long.MIN_VALUE, 25), "-000009223372036854775808");
	}

	@Test
	public void testPaddingString() {
		assertEquals(StringUtil.pad("", 0), "");
		assertEquals(StringUtil.pad("\uffff", 2), " \uffff");
		assertEquals(StringUtil.pad("\u1fffhello\u2fff", 10, "\u3fff", Align.H.left),
			"\u1fffhello\u2fff\u3fff\u3fff\u3fff");
		assertEquals(StringUtil.pad(null, 5, "aa", Align.H.left), "aaaa");
		assertEquals(StringUtil.pad("aaa", 5, null, Align.H.left), "aaa");
		assertEquals(StringUtil.pad("aaa", 5, Align.H.center), " aaa ");
	}
}
