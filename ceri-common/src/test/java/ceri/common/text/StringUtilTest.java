package ceri.common.text;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static java.lang.Character.toUpperCase;
import static java.lang.String.valueOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import ceri.common.text.StringUtil.Align;

public class StringUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(StringUtil.class);
	}

	@Test
	public void testUnEscape() {
		assertThat(StringUtil.unEscape("\\z\\\\\\\\\\\\z"), is("\\z\\\\\\z"));
		assertThat(StringUtil.unEscape("\\\\\\b\\e\\f\\n\\r\\t"), is("\\\u0008\u001b\f\n\r\t"));
		assertThat(StringUtil.unEscape("abc\\0\\00\\000\\077\\0377def"), is("abc\0\0\0?\u00ffdef"));
		assertThat(StringUtil.unEscape("ABC\\x00\\xffDEF"), is("ABC\0\u00ffDEF"));
		assertThat(StringUtil.unEscape("xyz\\u0000\\u1234"), is("xyz\0\u1234"));
		assertThat(StringUtil.unEscape("\\x0\\u0\\u00\\u000"), is("\\x0\\u0\\u00\\u000"));
		assertThat(StringUtil.unEscape("\\0777"), is("?7"));
		assertThat(StringUtil.unEscape("\\\\\\\\\\\\"), is("\\\\\\"));
	}

	@Test
	public void testReplaceAll() {
		String s = "abcdefghijklmnopqrstuvwxyz";
		s = StringUtil.replaceAll(s, "[aeiou]", m -> valueOf(toUpperCase(m.group().charAt(0))));
		assertThat(s, is("AbcdEfghIjklmnOpqrstUvwxyz"));
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
		assertThat(StringUtil.printable(KeyEvent.CHAR_UNDEFINED), is(false));
		assertThat(StringUtil.printable('\uffff'), is(false));
		assertThat(StringUtil.printable('\u1c00'), is(true));
		assertThat(StringUtil.printable(Character.MAX_HIGH_SURROGATE), is(true));
		assertThat(StringUtil.printable('\0'), is(false));
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
	public void testToString() {
		String toString = StringUtil.toString("{", "}", "|", "Test1", "Test2", "Test3");
		assertThat(toString, is("{Test1|Test2|Test3}"));
		toString = StringUtil.toString("{", "}", "|", "Test");
		assertThat(toString, is("{Test}"));
		toString = StringUtil.toString("{", "}", "|", Collections.singleton("Test"));
		assertThat(toString, is("{Test}"));
		toString = StringUtil.toString("|", Arrays.asList(1, 2, 3));
		assertThat(toString, is("1|2|3"));
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
		assertThat(StringUtil.pad("\u1fffhello\u2fff", 10, "\u3fff", Align.LEFT),
			is("\u1fffhello\u2fff\u3fff\u3fff\u3fff"));
		assertThat(StringUtil.pad(null, 5, "aa", Align.LEFT), is("aaaa"));
		assertThat(StringUtil.pad("aaa", 5, null, Align.LEFT), is("aaa"));
		assertThat(StringUtil.pad("aaa", 5, Align.CENTER), is(" aaa "));
	}

	@Test
	public void testSafeSubstring() {
		assertThat(StringUtil.safeSubstring("\u3fff\u3ffe\u3ffd", 0), is("\u3fff\u3ffe\u3ffd"));
		assertThat(StringUtil.safeSubstring("\u3fff\u3ffe\u3ffd", 2), is("\u3ffd"));
		assertThat(StringUtil.safeSubstring("\u3fff\u3ffe\u3ffd", 4), is(""));
		assertThat(StringUtil.safeSubstring("\u3fff\u3ffe\u3ffd", -3, -1), is("\u3fff\u3ffe\u3ffd"));
		assertThat(StringUtil.safeSubstring("\u3fff\u3ffe\u3ffd", -4, 5), is("\u3fff\u3ffe\u3ffd"));
		assertThat(StringUtil.safeSubstring(null, 0, 0), is(""));
		assertThat(StringUtil.safeSubstring("abc", 0, 3), is("abc"));
	}

	@Test
	public void testAsPrintStream() {
		StringBuilder b = new StringBuilder();
		try (PrintStream p = StringUtil.asPrintStream(b)) {
			p.println("Testing1");
			p.write(0);
			p.println("\b\t\f\'\"");
			p.close();
			assertThat(b.toString(), is("Testing1" + System.lineSeparator() + "\0\b\t\f\'\"" +
				System.lineSeparator()));
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
