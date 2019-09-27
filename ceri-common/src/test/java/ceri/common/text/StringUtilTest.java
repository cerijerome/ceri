package ceri.common.text;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.exerciseSwitch;
import static ceri.common.text.StringUtil.repeat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import ceri.common.util.HAlign;

public class StringUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(StringUtil.class);
	}

	@Test
	public void testReverse() {
		assertNull(StringUtil.reverse(null));
		assertThat(StringUtil.reverse(""), is(""));
		assertThat(StringUtil.reverse("\u0000\u00ff\uffff"), is("\uffff\u00ff\u0000"));
	}

	@Test
	public void testExtractBrackets() {
		assertNull(StringUtil.extractBrackets(null, '<', '>'));
		assertNull(StringUtil.extractBrackets("", '<', '>'));
		assertNull(StringUtil.extractBrackets("<", '<', '>'));
		assertNull(StringUtil.extractBrackets(">", '<', '>'));
		assertThat(StringUtil.extractBrackets("<>", '<', '>'), is("<>"));
		assertNull(StringUtil.extractBrackets("<<>", '<', '>'));
		assertThat(StringUtil.extractBrackets("<a<b>>", '<', '>'), is("<a<b>>"));
		assertThat(StringUtil.extractBrackets("a<b<>c>d", '<', '>'), is("<b<>c>"));
	}

	@Test
	public void testFormat() {
		StringBuilder b = new StringBuilder("abc");
		StringUtil.format(b, "%.2f", 3.3333);
		assertThat(b.toString(), is("abc3.33"));
	}

	@Test
	public void testDecimalFormat() {
		assertThat(StringUtil.decimalFormat(0).format(0.01), is("0"));
		assertThat(StringUtil.decimalFormat(0).format(0.50001), is("1"));
		assertThat(StringUtil.decimalFormat(2).format(0.5), is("0.5"));
		assertThat(StringUtil.decimalFormat(2).format(10), is("10"));
		assertThat(StringUtil.decimalFormat(2).format(10.011), is("10.01"));
	}

	@Test
	public void testToStringFromCodePoints() {
		assertThat(StringUtil.toString(new int[] {}), is(""));
		assertThat(StringUtil.toString(1, 2, 3), is("\u0001\u0002\u0003"));
	}

	@Test
	public void testRepeat() {
		assertNull(repeat(null, 0));
		assertNull(repeat(null, 10));
		assertThat(repeat("", 0), is(""));
		assertThat(repeat("", 10), is(""));
		assertThat(repeat("abc", 1), is("abc"));
		assertThat(repeat("abc", 3), is("abcabcabc"));
	}

	@Test
	public void testSplit() {
		assertIterable(StringUtil.split("Thisisatest", Arrays.asList(4, 6, 7, 20)), //
			"This", "is", "a", "test", "");
	}

	@Test
	public void testCompact() {
		assertNull(StringUtil.compact(null));
		assertThat(StringUtil.compact(""), is(""));
		assertThat(StringUtil.compact("  \t\rt\r   \ne s\tt"), is("t e s t"));
		assertThat(StringUtil.compact(0.15f), is("0.15"));
		assertThat(StringUtil.compact(100.0f), is("100"));
		assertThat(StringUtil.compact(1.2), is("1.2"));
		assertThat(StringUtil.compact(11.0), is("11"));
	}

	@Test
	public void testReplaceUnprintable() {
		assertNull(StringUtil.printable(null));
		assertThat(StringUtil.printable(""), is(""));
		assertThat(StringUtil.printable("ab\0c\u2081"), is("ab.c\u2081"));
		assertNull(StringUtil.replaceUnprintable(null, c -> "."));
		assertThat(StringUtil.replaceUnprintable("ab\0\1\2c", c -> String.valueOf((int) c)),
			is("ab012c"));
	}

	@Test
	public void testToLowerCase() {
		assertNull(StringUtil.toLowerCase(null));
		assertThat(StringUtil.toLowerCase(""), is(""));
		assertThat(StringUtil.toLowerCase("abCDeF"), is("abcdef"));
	}

	@Test
	public void testToUpperCase() {
		assertNull(StringUtil.toUpperCase(null));
		assertThat(StringUtil.toUpperCase(""), is(""));
		assertThat(StringUtil.toUpperCase("abCDeF"), is("ABCDEF"));
	}

	@Test
	public void testEqualsIgnoreCase() {
		assertTrue(StringUtil.equalsIgnoreCase(null, null));
		assertFalse(StringUtil.equalsIgnoreCase(null, "abc"));
		assertFalse(StringUtil.equalsIgnoreCase(new StringBuilder(), null));
		assertTrue(StringUtil.equalsIgnoreCase(new StringBuilder("AbC"), "abc"));
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
		assertTrue(StringUtil.startsWithIgnoreCase(new StringBuilder("ABCdefGHI"), "abCDeF"));
		assertTrue(StringUtil.startsWithIgnoreCase(new StringBuilder("ABCdefGHI"), 3, "DeF"));
	}

	@Test
	public void testUnEscape() {
		assertThat(StringUtil.unEscapeChar(null), is('\0'));
		exerciseSwitch(StringUtil::unEscapeChar, //
			"\\\\", "\\b", "\\e", "\\f", "\\n", "\\r", "\\t", "\\0");
		assertThat(StringUtil.unEscapeChar("\0\\\\"), is('\0'));
		assertThat(StringUtil.unEscape("\\z\\\\\\\\\\\\z"), is("\\z\\\\\\z"));
		assertThat(StringUtil.unEscape("\\\\\\b\\e\\f\\n\\r\\t"), is("\\\u0008\u001b\f\n\r\t"));
		assertThat(StringUtil.unEscape("abc\\0\\00\\000\\077\\0377def"), is("abc\0\0\0?\u00ffdef"
		));
		assertThat(StringUtil.unEscape("ABC\\x00\\xffDEF"), is("ABC\0\u00ffDEF"));
		assertThat(StringUtil.unEscape("xyz\\u0000\\u1234"), is("xyz\0\u1234"));
		assertThat(StringUtil.unEscape("\\x0\\u0\\u00\\u000"), is("\\x0\\u0\\u00\\u000"));
		assertThat(StringUtil.unEscape("\\0777"), is("?7"));
		assertThat(StringUtil.unEscape("\\\\\\\\\\\\"), is("\\\\\\"));
	}

	@Test
	public void testUnEscapeHexBytes() {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < 0x100; i++)
			b.append(String.format("\\x%02x", i));
		String s = StringUtil.unEscape(b.toString());
		byte[] bytes = s.getBytes(StandardCharsets.ISO_8859_1);
		for (int i = 0; i < bytes.length; i++)
			assertThat(bytes[i], is((byte) i));
	}

	@Test
	public void testEscape() {
		assertThat(StringUtil.escape("\\ \b \u001b \t \f \r \n \0 \1"),
			is("\\ \\b \\e \\t \\f \\r \\n \\0 \\u0001"));
		assertThat(StringUtil.escapeChar('\\'), is("\\\\"));
	}

	@Test
	public void testToHex() {
		byte[] bb = { 0, -1, 1, Byte.MAX_VALUE, Byte.MIN_VALUE };
		assertThat(StringUtil.toHexArray(bb), is("[0x00, 0xff, 0x01, 0x7f, 0x80]"));
		assertThat(StringUtil.toHexArray(new byte[] {}), is("[]"));
		assertThat(StringUtil.toHex(Long.MAX_VALUE), is("7fffffffffffffff"));
		assertThat(StringUtil.toHex(Integer.MAX_VALUE), is("7fffffff"));
		assertThat(StringUtil.toHex(Short.MAX_VALUE), is("7fff"));
		assertThat(StringUtil.toHex(bb), is("00ff017f80"));
		assertThat(StringUtil.toHex(new byte[] {}), is(""));
	}

	@Test
	public void testToBinary() {
		assertThat(StringUtil.toBinary(255, 8), is("11111111"));
		assertThat(StringUtil.toBinary(Byte.MIN_VALUE), is("10000000"));
		assertThat(StringUtil.toBinary(Short.MAX_VALUE), is("0111111111111111"));
		assertThat(StringUtil.toBinary(-1), is("11111111111111111111111111111111"));
		assertThat(StringUtil.toBinary(Long.MAX_VALUE),
			is("0111111111111111111111111111111111111111111111111111111111111111"));
	}

	@Test
	public void testToUnsignedString() {
		assertThat(StringUtil.toUnsigned(0, 16, 2), is("00"));
		assertThat(StringUtil.toUnsigned(0, 26, 16), is("0000000000000000"));
		assertThat(StringUtil.toUnsigned(-1, 8, 5), is("77777"));
		assertThat(StringUtil.toUnsigned(-1, 16, 18), is("00ffffffffffffffff"));
		assertThat(StringUtil.toUnsigned(65535, 16, 10), is("000000ffff"));
		assertThat(StringUtil.toUnsigned(Long.MAX_VALUE, 36, 14), is("01y2p0ij32e8e7"));
		assertThat(StringUtil.toUnsigned(Long.MIN_VALUE, 8, 22), is("1000000000000000000000"));
	}

	@Test
	public void testUrlEncode() {
		assertThat(StringUtil.urlEncode(""), is(""));
		assertThat(StringUtil.urlEncode("a b&c"), is("a+b%26c"));
		assertThat(StringUtil.urlDecode(""), is(""));
		assertThat(StringUtil.urlDecode("a+b%26c"), is("a b&c"));
	}

	@Test
	public void testPrintable() {
		assertThat(StringUtil.isPrintable(KeyEvent.CHAR_UNDEFINED), is(false));
		assertThat(StringUtil.isPrintable('\uffff'), is(false));
		assertThat(StringUtil.isPrintable('\u1c00'), is(true));
		assertThat(StringUtil.isPrintable(Character.MAX_HIGH_SURROGATE), is(true));
		assertThat(StringUtil.isPrintable('\0'), is(false));
	}

	@Test
	public void testCommaSplit() {
		assertCollection(StringUtil.commaSplit(null));
		assertCollection(StringUtil.commaSplit(""));
		assertCollection(StringUtil.commaSplit(" "));
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
		assertThat(StringUtil.join("|", 1, 2, 3), is("1|2|3"));
		assertThat(StringUtil.join("|", "{", "}", "Test1", "Test2", "Test3"),
			is("{Test1|Test2|Test3}"));
		assertThat(StringUtil.join("|", "{", "}", "Test"), is("{Test}"));
		assertThat(StringUtil.join("|", "{", "}", Collections.singleton("Test")), is("{Test}"));
		assertThat(StringUtil.join("|", Arrays.asList(1, 2, 3)), is("1|2|3"));
		assertThat(StringUtil.join("|", i -> repeat("x", i), 1, 2, 3), is("x|xx|xxx"));
		assertThat(StringUtil.join("|", "{", "}", i -> repeat("x", i), 1, 2, 3), is("{x|xx|xxx}"));
	}

	@Test
	public void testPaddingNumbers() {
		assertThat(StringUtil.pad(100, 5), is("00100"));
		assertThat(StringUtil.pad(-100, 5), is("-0100"));
		assertThat(StringUtil.pad(Long.MAX_VALUE, 0), is("9223372036854775807"));
		assertThat(StringUtil.pad(Long.MAX_VALUE, 20), is("09223372036854775807"));
		assertThat(StringUtil.pad(Long.MIN_VALUE, 20), is("-9223372036854775808"));
		assertThat(StringUtil.pad(Long.MIN_VALUE, 25), is("-000009223372036854775808"));
	}

	@Test
	public void testPaddingString() {
		assertThat(StringUtil.pad("", 0), is(""));
		assertThat(StringUtil.pad("\uffff", 2), is(" \uffff"));
		assertThat(StringUtil.pad("\u1fffhello\u2fff", 10, "\u3fff", HAlign.left),
			is("\u1fffhello\u2fff\u3fff\u3fff\u3fff"));
		assertThat(StringUtil.pad(null, 5, "aa", HAlign.left), is("aaaa"));
		assertThat(StringUtil.pad("aaa", 5, null, HAlign.left), is("aaa"));
		assertThat(StringUtil.pad("aaa", 5, HAlign.center), is(" aaa "));
	}

	@Test
	public void testSafeSubstring() {
		String s = "\u3fff\u3ffe\u3ffd";
		assertThat(StringUtil.safeSubstring(s, 0), is(s));
		assertThat(StringUtil.safeSubstring(s, 2), is("\u3ffd"));
		assertThat(StringUtil.safeSubstring(s, 4), is(""));
		assertThat(StringUtil.safeSubstring(s, -3, -1), is(s));
		assertThat(StringUtil.safeSubstring(s, -4, 5), is(s));
		assertThat(StringUtil.safeSubstring(null, 0, 0), is(""));
		assertThat(StringUtil.safeSubstring("abc", 0, 3), is("abc"));
		StringBuilder b = new StringBuilder(s);
		assertThat(StringUtil.safeSubstring(b, 0), is(s));
		assertThat(StringUtil.safeSubstring(b, 2), is("\u3ffd"));
		assertThat(StringUtil.safeSubstring(b, 4), is(""));
		assertThat(StringUtil.safeSubstring(b, -3, -1), is(s));
		assertThat(StringUtil.safeSubstring(b, -4, 5), is(s));
		assertThat(StringUtil.safeSubstring(null, 0, 0), is(""));
		assertThat(StringUtil.safeSubstring(new StringBuilder("abc"), 0, 3), is("abc"));
	}

	@Test
	public void testSubstring() {
		assertNull(StringUtil.substring(null, 0));
		assertThat(StringUtil.substring(new StringBuilder("test"), 2), is("st"));
	}

	@Test
	public void testSubSequence() {
		assertNull(StringUtil.subSequence(null, 0));
		assertThat(StringUtil.subSequence(new StringBuilder("test"), 2), is("st"));
	}

	@Test
	public void testPrint() {
		assertThat(StringUtil.print(out -> out.print("test")), is("test"));
	}

	@Test
	public void testAsPrintStream() {
		StringBuilder b = new StringBuilder();
		try (PrintStream p = StringUtil.asPrintStream(b)) {
			p.println("Testing1");
			p.write(0);
			p.println("\b\t\f\'\"");
			p.close();
			assertThat(b.toString(),
				is("Testing1" + System.lineSeparator() + "\0\b\t\f\'\"" + System.lineSeparator()));
		}
	}

	@Test
	public void testCount() {
		assertThat(StringUtil.count("abcaaabbbccc", 'a'), is(4));
		assertThat(StringUtil.count("AAAAAAaa", 'a'), is(2));
		assertThat(StringUtil.count("AAAAAAaa", "a"), is(2));
		assertThat(StringUtil.count("", 'a'), is(0));
		assertThat(StringUtil.count("", ""), is(0));
		assertThat(StringUtil.count("a", ""), is(0));
		assertThat(StringUtil.count("bcd", 'a'), is(0));
		assertThat(StringUtil.count("abcaaabbbccc", "ab"), is(2));
		assertThat(StringUtil.count("", "ab"), is(0));
	}

	@Test
	public void testPrefixLines() {
		assertThat(StringUtil.prefixLines("xxx", "abc"), is("xxxabc"));
		assertThat(StringUtil.prefixLines("xxx", ""), is("xxx"));
		assertThat(StringUtil.prefixLines("", ""), is(""));
		assertThat(StringUtil.prefixLines("", "abc"), is("abc"));
		assertThat(StringUtil.prefixLines("xxx", "a\r\nb"), is("xxxa\r\nxxxb"));
		assertThat(StringUtil.prefixLines("xxx", "a\r\n"), is("xxxa\r\nxxx"));
		assertThat(StringUtil.prefixLines("xxx", "\r\n"), is("xxx\r\nxxx"));
		assertThat(StringUtil.prefixLines("\t", "\n\r"), is("\t\n\t\r\t"));
		assertThat(StringUtil.prefixLines("\t", "\n\r\n\t"), is("\t\n\t\r\n\t\t"));
		assertThat(StringUtil.prefixLines("x", "a\nb\r\nc\rd"), is("xa\nxb\r\nxc\rxd"));
	}

}
