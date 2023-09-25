package ceri.common.text;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseSwitch;
import static ceri.common.text.StringUtil.repeat;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import ceri.common.util.Align;
import ceri.common.util.Align.H;

public class StringUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(StringUtil.class);
	}

	@Test
	public void testReverse() {
		assertNull(StringUtil.reverse(null));
		assertEquals(StringUtil.reverse(""), "");
		assertEquals(StringUtil.reverse("\u0000\u00ff\uffff"), "\uffff\u00ff\u0000");
	}

	@Test
	public void testExtractBrackets() {
		assertNull(StringUtil.extractBrackets(null, '<', '>'));
		assertNull(StringUtil.extractBrackets("", '<', '>'));
		assertNull(StringUtil.extractBrackets("<", '<', '>'));
		assertNull(StringUtil.extractBrackets(">", '<', '>'));
		assertEquals(StringUtil.extractBrackets("<>", '<', '>'), "<>");
		assertNull(StringUtil.extractBrackets("<<>", '<', '>'));
		assertEquals(StringUtil.extractBrackets("<a<b>>", '<', '>'), "<a<b>>");
		assertEquals(StringUtil.extractBrackets("a<b<>c>d", '<', '>'), "<b<>c>");
	}

	@Test
	public void testFormat() {
		StringBuilder b = new StringBuilder("abc");
		StringUtil.format(b, "%.2f", 3.3333);
		StringUtil.format(b, " %.2f"); // no args, string not processed
		assertEquals(b.toString(), "abc3.33 %.2f");
	}

	@Test
	public void testDecimalFormat() {
		assertEquals(StringUtil.decimalFormat(0).format(0.01), "0");
		assertEquals(StringUtil.decimalFormat(0).format(0.50001), "1");
		assertEquals(StringUtil.decimalFormat(2).format(0.5), "0.5");
		assertEquals(StringUtil.decimalFormat(2).format(10), "10");
		assertEquals(StringUtil.decimalFormat(2).format(10.011), "10.01");
	}

	@Test
	public void testToStringFromCodePoints() {
		assertEquals(StringUtil.toString(new int[0]), "");
		assertEquals(StringUtil.toString(1, 2, 3), "\u0001\u0002\u0003");
	}

	@Test
	public void testToStringFromChars() {
		assertEquals(StringUtil.toString(new char[0]), "");
		assertEquals(StringUtil.toString('a', '\0', 'b'), "a\0b");
	}

	@Test
	public void testAppend() {
		assertEquals(
			StringUtil.append(new StringBuilder(), "", String::valueOf, "", "a", "").toString(),
			"a");
		assertEquals(
			StringUtil.append(new StringBuilder(), ":", String::valueOf, 1, 2, 3).toString(),
			"1:2:3");
	}

	@Test
	public void testRepeat() {
		assertNull(repeat(null, 0));
		assertNull(repeat(null, 10));
		assertEquals(repeat("", 0), "");
		assertEquals(repeat("", 10), "");
		assertEquals(repeat("abc", 1), "abc");
		assertEquals(repeat("abc", 3), "abcabcabc");
		assertNull(repeat(null, "abd", 10));
		assertEquals(repeat(new StringBuilder("x"), "", 10).toString(), "x");
		assertEquals(repeat(new StringBuilder("x"), "abd", 0).toString(), "x");
		assertEquals(repeat(new StringBuilder("x"), null, 10).toString(), "x");
		assertEquals(repeat('x', 0), "");
		assertEquals(repeat('x', 5), "xxxxx");
		assertNull(repeat(null, 'y', 3));
		assertEquals(repeat(new StringBuilder("x"), 'y', 0).toString(), "x");
		assertEquals(repeat(new StringBuilder("x"), 'y', 2).toString(), "xyy");
	}

	@Test
	public void testSplit() {
		assertIterable(StringUtil.split("Thisisatest", Arrays.asList(4, 6, 7, 20)), //
			"This", "is", "a", "test", "");
	}

	@Test
	public void testSpacesToTabs() {
		assertNull(StringUtil.spacesToTabs(null, 1));
		assertEquals(StringUtil.spacesToTabs("    ", 0), "    ");
		assertEquals(StringUtil.spacesToTabs("", 4), "");
		assertEquals(StringUtil.spacesToTabs("a", 4), "a");
		assertEquals(StringUtil.spacesToTabs("ab", 4), "ab");
		assertEquals(StringUtil.spacesToTabs("abc", 4), "abc");
		assertEquals(StringUtil.spacesToTabs("abcd", 4), "abcd");
		assertEquals(StringUtil.spacesToTabs("a ", 4), "a ");
		assertEquals(StringUtil.spacesToTabs("ab ", 4), "ab ");
		assertEquals(StringUtil.spacesToTabs("abc ", 4), "abc\t");
		assertEquals(StringUtil.spacesToTabs("abcd ", 4), "abcd ");
		assertEquals(StringUtil.spacesToTabs("a  ", 4), "a  ");
		assertEquals(StringUtil.spacesToTabs("ab  ", 4), "ab\t");
		assertEquals(StringUtil.spacesToTabs("abc  ", 4), "abc\t ");
		assertEquals(StringUtil.spacesToTabs("abcd  ", 4), "abcd  ");
		assertEquals(StringUtil.spacesToTabs("a   ", 2), "a\t\t");
		assertEquals(StringUtil.spacesToTabs("ab   ", 2), "ab\t ");
		assertEquals(StringUtil.spacesToTabs("abc   ", 2), "abc\t\t");
		assertEquals(StringUtil.spacesToTabs("abcd   ", 2), "abcd\t ");
		assertEquals(StringUtil.spacesToTabs("       ab c   e ", 4), "\t   ab c\t  e\t");
		assertEquals(StringUtil.spacesToTabs("abcde   fgh      i", 4), "abcde\tfgh\t\t i");
		assertEquals(StringUtil.spacesToTabs("a\t b \t c", 4), "a\t b\t c");
	}

	@Test
	public void testTabsToSpaces() {
		assertNull(StringUtil.tabsToSpaces(null, 1));
		assertEquals(StringUtil.tabsToSpaces("    ", 0), "    ");
		assertEquals(StringUtil.tabsToSpaces("", 4), "");
		assertEquals(StringUtil.tabsToSpaces("a", 4), "a");
		assertEquals(StringUtil.tabsToSpaces("ab", 4), "ab");
		assertEquals(StringUtil.tabsToSpaces("abc", 4), "abc");
		assertEquals(StringUtil.tabsToSpaces("abcd", 4), "abcd");
		assertEquals(StringUtil.tabsToSpaces("\t", 4), "    ");
		assertEquals(StringUtil.tabsToSpaces("a\t", 4), "a   ");
		assertEquals(StringUtil.tabsToSpaces("ab\t", 4), "ab  ");
		assertEquals(StringUtil.tabsToSpaces("abc\t", 4), "abc ");
		assertEquals(StringUtil.tabsToSpaces("abcd\t", 4), "abcd    ");
		assertEquals(StringUtil.tabsToSpaces("\t\t", 2), "    ");
		assertEquals(StringUtil.tabsToSpaces("\t \t", 2), "    ");
		assertEquals(StringUtil.tabsToSpaces(" \t \t", 2), "    ");
		assertEquals(StringUtil.tabsToSpaces("\t\t ", 2), "     ");
		assertEquals(StringUtil.tabsToSpaces("abc\t\tdef  \tg", 4), "abc     def     g");
		assertEquals(StringUtil.tabsToSpaces("abcde\tfgh\t\ti", 4), "abcde   fgh     i");
	}

	@Test
	public void testCompact() {
		assertNull(StringUtil.compact(null));
		assertEquals(StringUtil.compact(""), "");
		assertEquals(StringUtil.compact("  \t\rt\r   \ne s\tt"), "t e s t");
	}

	@Test
	public void testCompactFloatingPoint() {
		assertEquals(StringUtil.compact(0.15f), "0.15");
		assertEquals(StringUtil.compact(100.0f), "100");
		assertEquals(StringUtil.compact(1.2), "1.2");
		assertEquals(StringUtil.compact(11.0), "11");
		assertEquals(StringUtil.compact(0.1555555555, 3), "0.156");
		assertEquals(StringUtil.compact(0.9999, 3), "1");
	}

	@Test
	public void testReplaceUnprintable() {
		assertNull(StringUtil.printable(null));
		assertEquals(StringUtil.printable(""), "");
		assertEquals(StringUtil.printable("ab\0c\u2081"), "ab.c\u2081");
		assertNull(StringUtil.replaceUnprintable(null, c -> "."));
		assertEquals(StringUtil.replaceUnprintable("ab\0\1\2c", c -> String.valueOf((int) c)),
			"ab012c");
	}

	@Test
	public void testToLowerCase() {
		assertNull(StringUtil.toLowerCase(null));
		assertEquals(StringUtil.toLowerCase(""), "");
		assertEquals(StringUtil.toLowerCase("abCDeF"), "abcdef");
	}

	@Test
	public void testToUpperCase() {
		assertNull(StringUtil.toUpperCase(null));
		assertEquals(StringUtil.toUpperCase(""), "");
		assertEquals(StringUtil.toUpperCase("abCDeF"), "ABCDEF");
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
	public void testRegionMatches() {
		assertFalse(StringUtil.regionMatches(null, false, 0, null, 0, 0));
		assertFalse(StringUtil.regionMatches(new StringBuilder(), false, 0, null, 0, 0));
		assertFalse(StringUtil.regionMatches(new StringBuilder("x"), false, 2, "", 0, 0));
		assertFalse(StringUtil.regionMatches(new StringBuilder("xx"), false, 0, "x", 0, 2));
		assertTrue(StringUtil.regionMatches(new StringBuilder("abcdef"), true, 1, "xBcD", 1, 3));
		assertFalse(StringUtil.regionMatches(new StringBuilder("abcdef"), false, 1, "xBcD", 1, 3));
	}

	@Test
	public void testUnEscape() {
		assertEquals(StringUtil.unEscapeChar(null), '\0');
		exerciseSwitch(StringUtil::unEscapeChar, //
			"\\\\", "\\b", "\\e", "\\f", "\\n", "\\r", "\\t", "\\0");
		assertEquals(StringUtil.unEscapeChar("\0\\\\"), '\0');
		assertEquals(StringUtil.unEscape("\\z\\\\\\\\\\\\z"), "\\z\\\\\\z");
		assertEquals(StringUtil.unEscape("\\\\\\b\\e\\f\\n\\r\\t"), "\\\u0008\u001b\f\n\r\t");
		assertEquals(StringUtil.unEscape("abc\\0\\00\\000\\077\\0377def"), "abc\0\0\0?\u00ffdef");
		assertEquals(StringUtil.unEscape("ABC\\x00\\xffDEF"), "ABC\0\u00ffDEF");
		assertEquals(StringUtil.unEscape("xyz\\u0000\\u1234"), "xyz\0\u1234");
		assertEquals(StringUtil.unEscape("\\x0\\u0\\u00\\u000"), "\\x0\\u0\\u00\\u000");
		assertEquals(StringUtil.unEscape("\\0777"), "?7");
		assertEquals(StringUtil.unEscape("\\\\\\\\\\\\"), "\\\\\\");
	}

	@Test
	public void testUnEscapeHexBytes() {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < 0x100; i++)
			b.append(String.format("\\x%02x", i));
		String s = StringUtil.unEscape(b.toString());
		byte[] bytes = s.getBytes(StandardCharsets.ISO_8859_1);
		for (int i = 0; i < bytes.length; i++)
			assertEquals(bytes[i], (byte) i);
	}

	@Test
	public void testEscape() {
		assertEquals(StringUtil.escape("\\ \b \u001b \t \f \r \n \0 \1"),
			"\\ \\b \\e \\t \\f \\r \\n \\0 \\u0001");
		assertEquals(StringUtil.escape('\\'), "\\");
		assertEquals(StringUtil.escape('\0'), "\\0");
		assertEquals(StringUtil.escapeChar('\\'), "\\\\");
	}

	@Test
	public void testToHex() {
		byte[] bb = { 0, -1, 1, Byte.MAX_VALUE, Byte.MIN_VALUE };
		assertEquals(StringUtil.toHex(Long.MAX_VALUE), "7fffffffffffffff");
		assertEquals(StringUtil.toHex(Integer.MAX_VALUE), "7fffffff");
		assertEquals(StringUtil.toHex(Short.MAX_VALUE), "7fff");
		assertEquals(StringUtil.toHex(bb), "00ff017f80");
		assertEquals(StringUtil.toHex(new byte[] {}), "");
	}

	@Test
	public void testToBinary() {
		assertEquals(StringUtil.toBinary(255, 8), "11111111");
		assertEquals(StringUtil.toBinary(Byte.MIN_VALUE), "10000000");
		assertEquals(StringUtil.toBinary(Short.MAX_VALUE), "0111111111111111");
		assertEquals(StringUtil.toBinary(-1), "11111111111111111111111111111111");
		assertEquals(StringUtil.toBinary(Long.MAX_VALUE),
			"0111111111111111111111111111111111111111111111111111111111111111");
	}

	@Test
	public void testToBinaryWithSeparator() {
		assertEquals(StringUtil.toBinary((byte) 0x7f, "_", 4, 2), "01_11_1111");
		assertEquals(StringUtil.toBinary((short) 0x7ff, "_", 4, 6, 2, 4), "0000_01_111111_1111");
		assertEquals(StringUtil.toBinary(0x7ff, "_", 4, 6, 2, 4),
			"0000_0000_0000_0000_0000_01_111111_1111");
		assertEquals(StringUtil.toBinary(0x7ffL, "_", 8, 6, 2, 8),
			"00000000_00000000_00000000_00000000_00000000_00000000_00_000111_11111111");
		assertEquals(StringUtil.toBinary(0x7ffL, 20, "_", 8, 6, 2, 8), "0000_00_000111_11111111");
	}

	@Test
	public void testToUnsignedString() {
		assertEquals(StringUtil.toUnsigned(0, 16, 2), "00");
		assertEquals(StringUtil.toUnsigned(0, 26, 16), "0000000000000000");
		assertEquals(StringUtil.toUnsigned(-1, 8, 5), "77777");
		assertEquals(StringUtil.toUnsigned(-1, 16, 18), "00ffffffffffffffff");
		assertEquals(StringUtil.toUnsigned(65535, 16, 10), "000000ffff");
		assertEquals(StringUtil.toUnsigned(Long.MAX_VALUE, 36, 14), "01y2p0ij32e8e7");
		assertEquals(StringUtil.toUnsigned(Long.MIN_VALUE, 8, 22), "1000000000000000000000");
	}

	@Test
	public void testUrlEncode() {
		assertEquals(StringUtil.urlEncode(""), "");
		assertEquals(StringUtil.urlEncode("a b&c"), "a+b%26c");
		assertEquals(StringUtil.urlDecode(""), "");
		assertEquals(StringUtil.urlDecode("a+b%26c"), "a b&c");
	}

	@Test
	public void testPrintable() {
		assertFalse(StringUtil.isPrintable(KeyEvent.CHAR_UNDEFINED));
		assertFalse(StringUtil.isPrintable('\uffff'));
		assertTrue(StringUtil.isPrintable('\u1c00'));
		assertTrue(StringUtil.isPrintable(Character.MAX_HIGH_SURROGATE));
		assertFalse(StringUtil.isPrintable('\0'));
	}

	@Test
	public void testCommaSplit() {
		assertCollection(StringUtil.commaSplit(null));
		assertCollection(StringUtil.commaSplit(""));
		assertCollection(StringUtil.commaSplit(" "), "");
		assertCollection(StringUtil.commaSplit("a"), "a");
		assertCollection(StringUtil.commaSplit(" a "), "a");
		assertCollection(StringUtil.commaSplit(",,a"), "", "", "a");
		assertCollection(StringUtil.commaSplit("a,,"), "a");
		assertCollection(StringUtil.commaSplit(" , a "), "", "a");
		assertCollection(StringUtil.commaSplit("a,b"), "a", "b");
		assertCollection(StringUtil.commaSplit(" a , b "), "a", "b");
	}

	@Test
	public void testWhitespaceSplit() {
		assertCollection(StringUtil.whiteSpaceSplit(null));
		assertCollection(StringUtil.whiteSpaceSplit(""));
		assertCollection(StringUtil.whiteSpaceSplit(" "));
		assertCollection(StringUtil.whiteSpaceSplit("a"), "a");
		assertCollection(StringUtil.whiteSpaceSplit(" a b "), "", "a", "b");
	}

	@Test
	public void testJoin() {
		assertEquals(StringUtil.join("|", "{", "}", "Test1", "Test2", "Test3"),
			"{Test1|Test2|Test3}");
		assertEquals(StringUtil.join("|", "{", "}", "Test"), "{Test}");
		assertEquals(StringUtil.join("|", "{", "}", Collections.singleton("Test")), "{Test}");
		assertEquals(StringUtil.join("|", Arrays.asList(1, 2, 3)), "1|2|3");
		assertEquals(StringUtil.join("|", i -> repeat("x", i), 1, 2, 3), "x|xx|xxx");
		assertEquals(StringUtil.join("|", "{", "}", i -> repeat("x", i), 1, 2, 3), "{x|xx|xxx}");
	}

	@Test
	public void testJoinAll() {
		assertEquals(StringUtil.joinAll("|", 1, 2, 3), "1|2|3");
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

	@Test
	public void testSeparate() {
		assertEquals(StringUtil.separate("", "-", H.left, 2), "");
		assertEquals(StringUtil.separate("abc", "", H.left), "abc");
		assertEquals(StringUtil.separate("abc", "", H.left, 2), "abc");
		assertThrown(() -> StringUtil.separate("abc", "-", H.center, 2));
		assertEquals(StringUtil.separate("abcdef", "-", H.left, 2, 1), "ab-c-d-e-f");
		assertEquals(StringUtil.separate("abcdef", "-", null, 2, 1), "ab-c-d-e-f");
		assertEquals(StringUtil.separate("abcdef", "-", H.right, 2, 1), "a-b-c-d-ef");
		assertEquals(StringUtil.separate("abcdef", "-", H.left, 4), "abcd-ef");
		assertEquals(StringUtil.separate("abc", "-", H.left, 4), "abc");
		assertEquals(StringUtil.separate("abc", "-", H.left, 2, 0), "ab-c");
	}

	@Test
	public void testEmpty() {
		assertTrue(StringUtil.empty((String) null));
		assertTrue(StringUtil.empty(""));
		assertFalse(StringUtil.empty(" \t\r\n"));
		assertFalse(StringUtil.empty("  _"));
	}

	@Test
	public void testNonEmpty() {
		assertFalse(StringUtil.nonEmpty((String) null));
		assertFalse(StringUtil.nonEmpty(""));
		assertTrue(StringUtil.nonEmpty(" \t\r\n"));
		assertTrue(StringUtil.nonEmpty("  _"));
	}

	@Test
	public void testBlank() {
		assertTrue(StringUtil.blank((String) null));
		assertTrue(StringUtil.blank(""));
		assertTrue(StringUtil.blank(" \t\r\n"));
		assertFalse(StringUtil.blank("  _"));
	}

	@Test
	public void testNonBlank() {
		assertFalse(StringUtil.nonBlank((String) null));
		assertFalse(StringUtil.nonBlank(""));
		assertFalse(StringUtil.nonBlank(" \t\r\n"));
		assertTrue(StringUtil.nonBlank("  _"));
	}

	@Test
	public void testSafeSubstring() {
		String s = "\u3fff\u3ffe\u3ffd";
		assertEquals(StringUtil.safeSubstring(s, 0), s);
		assertEquals(StringUtil.safeSubstring(s, 2), "\u3ffd");
		assertEquals(StringUtil.safeSubstring(s, 4), "");
		assertEquals(StringUtil.safeSubstring(s, -3, -1), s);
		assertEquals(StringUtil.safeSubstring(s, -4, 5), s);
		assertEquals(StringUtil.safeSubstring(null, 0, 0), "");
		assertEquals(StringUtil.safeSubstring("abc", 0, 3), "abc");
		StringBuilder b = new StringBuilder(s);
		assertEquals(StringUtil.safeSubstring(b, 0), s);
		assertEquals(StringUtil.safeSubstring(b, 2), "\u3ffd");
		assertEquals(StringUtil.safeSubstring(b, 4), "");
		assertEquals(StringUtil.safeSubstring(b, -3, -1), s);
		assertEquals(StringUtil.safeSubstring(b, -4, 5), s);
		assertEquals(StringUtil.safeSubstring(null, 0, 0), "");
		assertEquals(StringUtil.safeSubstring(new StringBuilder("abc"), 0, 3), "abc");
	}

	@Test
	public void testSubstring() {
		assertNull(StringUtil.substring(null, 0));
		assertNull(StringUtil.substring(null, 1, 3));
		assertEquals(StringUtil.substring(new StringBuilder("test"), 2), "st");
	}

	@Test
	public void testSubSequence() {
		assertNull(StringUtil.subSequence(null, 0));
		assertEquals(StringUtil.subSequence(new StringBuilder("test"), 2), "st");
	}

	@Test
	public void testPrint() {
		assertEquals(StringUtil.print(out -> out.print("test")), "test");
	}

	@Test
	public void testAsPrintStream() {
		StringBuilder b = new StringBuilder();
		try (PrintStream p = StringUtil.asPrintStream(b)) {
			p.println("Testing1");
			p.write(0);
			p.println("\b\t\f\'\"");
			p.close();
			assertEquals(b.toString(),
				"Testing1" + System.lineSeparator() + "\0\b\t\f\'\"" + System.lineSeparator());
		}
	}

	@Test
	public void testCount() {
		assertEquals(StringUtil.count("abcaaabbbccc", 'a'), 4);
		assertEquals(StringUtil.count("AAAAAAaa", 'a'), 2);
		assertEquals(StringUtil.count("AAAAAAaa", "a"), 2);
		assertEquals(StringUtil.count("", 'a'), 0);
		assertEquals(StringUtil.count("", ""), 0);
		assertEquals(StringUtil.count("a", ""), 0);
		assertEquals(StringUtil.count("bcd", 'a'), 0);
		assertEquals(StringUtil.count("abcaaabbbccc", "ab"), 2);
		assertEquals(StringUtil.count("", "ab"), 0);
	}

	@Test
	public void testLines() {
		assertIterable(StringUtil.lines(""));
		assertIterable(StringUtil.lines(" "), " ");
		assertIterable(StringUtil.lines("\n"));
		assertIterable(StringUtil.lines(" \n\t"), " ", "\t");
	}

	@Test
	public void testPrefixLines() {
		assertEquals(StringUtil.prefixLines("xxx", "abc"), "xxxabc");
		assertEquals(StringUtil.prefixLines("xxx", ""), "xxx");
		assertEquals(StringUtil.prefixLines("", ""), "");
		assertEquals(StringUtil.prefixLines("", "abc"), "abc");
		assertEquals(StringUtil.prefixLines("xxx", "a\r\nb"), "xxxa\r\nxxxb");
		assertEquals(StringUtil.prefixLines("xxx", "a\r\n"), "xxxa\r\nxxx");
		assertEquals(StringUtil.prefixLines("xxx", "\r\n"), "xxx\r\nxxx");
		assertEquals(StringUtil.prefixLines("\t", "\n\r"), "\t\n\t\r\t");
		assertEquals(StringUtil.prefixLines("\t", "\n\r\n\t"), "\t\n\t\r\n\t\t");
		assertEquals(StringUtil.prefixLines("x", "a\nb\r\nc\rd"), "xa\nxb\r\nxc\rxd");
	}

}
