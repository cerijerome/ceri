package ceri.common.util;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.util.Collections;
import org.junit.Test;
import ceri.common.util.StringUtil.Align;

public class StringUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(StringUtil.class);
	}

	@Test
	public void testToUnsignedString() {
		assertThat(StringUtil.toUnsignedString(0, 16, 2), is("00"));
		assertThat(StringUtil.toUnsignedString(0, 26, 16), is("0000000000000000"));
		assertThat(StringUtil.toUnsignedString(-1, 8, 5), is("77777"));
		assertThat(StringUtil.toUnsignedString(-1, 16, 18), is("00ffffffffffffffff"));
		assertThat(StringUtil.toUnsignedString(65535, 16, 10), is("000000ffff"));
		assertThat(StringUtil.toUnsignedString(Long.MAX_VALUE, 36, 14), is("01y2p0ij32e8e7"));
		assertThat(StringUtil.toUnsignedString(Long.MIN_VALUE, 8, 22), is("1000000000000000000000"));
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
		assertThat(StringUtil.count("abcaaabbbccc", 'a'), is(4));
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
