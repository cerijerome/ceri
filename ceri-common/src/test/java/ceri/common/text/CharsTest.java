package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertString;
import org.junit.After;
import org.junit.Test;

public class CharsTest {
	private static final char ch0 = 0;
	private StringBuilder b;

	private StringBuilder b(String s) {
		b = new StringBuilder(s);
		return b;
	}

	@After
	public void after() {
		b = null;
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Chars.class);
		assertPrivateConstructor(Chars.Escape.class);
	}

	@Test
	public void testSafe() {
		assertString(Chars.safe(null), "");
		assertSame(Chars.safe(b("")), b);
	}

	@Test
	public void testAt() {
		assertEquals(Chars.at(null, 0), null);
		assertEquals(Chars.at("", 0), null);
		assertEquals(Chars.at("test", -1), null);
		assertEquals(Chars.at("test", 4), null);
		assertEquals(Chars.at("test", 0), 't');
		assertEquals(Chars.at(null, 0, 'x'), 'x');
		assertEquals(Chars.at("", 0, 'x'), 'x');
		assertEquals(Chars.at("test", -1, 'x'), 'x');
		assertEquals(Chars.at("test", 4, 'x'), 'x');
		assertEquals(Chars.at("test", 2, 'x'), 's');
	}

	@Test
	public void testLower() {
		assertString(Chars.lower(null), "");
		assertString(Chars.lower(""), "");
		assertString(Chars.lower("_Aa\u03a9\u03c9"), "_aa\u03c9\u03c9");
	}

	@Test
	public void testUpper() {
		assertString(Chars.upper(null), "");
		assertString(Chars.upper(""), "");
		assertString(Chars.upper("_Aa\u03a9\u03c9"), "_AA\u03a9\u03a9");
	}

	@Test
	public void testEquals() {
		assertEquals(Chars.equals(null, 0, null, 0), false);
		assertEquals(Chars.equals(null, 0, "", 0), false);
		assertEquals(Chars.equals("", 0, null, 0), false);
		assertEquals(Chars.equals("", 0, "", 0), false);
		assertEquals(Chars.equals("a", -1, "a", 0), false);
		assertEquals(Chars.equals("a", 1, "a", 0), false);
		assertEquals(Chars.equals("a", 0, "a", -1), false);
		assertEquals(Chars.equals("a", 0, "a", 1), false);
		assertEquals(Chars.equals("abc", 1, "abc", 1), true);
		assertEquals(Chars.equals("abc", 1, "aBc", 1), false);
	}

	@Test
	public void testIsPrintable() {
		assertEquals(Chars.isPrintable(null, 0), false);
		assertEquals(Chars.isPrintable("A", 0), true);
		assertEquals(Chars.isPrintable("A", 1), false);
		assertEquals(Chars.isPrintable(Chars.NUL), false);
		assertEquals(Chars.isPrintable('A'), true);
		assertEquals(Chars.isPrintable('\u1c00'), true);
		assertEquals(Chars.isPrintable(Character.MAX_HIGH_SURROGATE), true);
		assertEquals(Chars.isPrintable('\uffff'), false);
	}

	@Test
	public void testIsNameBoundary() {
		assertEquals(Chars.isNameBoundary(ch0, ch0), false);
		assertEquals(Chars.isNameBoundary(ch0, 'a'), true);
		assertEquals(Chars.isNameBoundary(ch0, 'A'), true);
		assertEquals(Chars.isNameBoundary(ch0, '1'), true);
		assertEquals(Chars.isNameBoundary(ch0, '_'), false);
		assertEquals(Chars.isNameBoundary('a', ch0), true);
		assertEquals(Chars.isNameBoundary('a', 'a'), false);
		assertEquals(Chars.isNameBoundary('a', 'A'), true);
		assertEquals(Chars.isNameBoundary('a', '1'), true);
		assertEquals(Chars.isNameBoundary('a', '_'), true);
		assertEquals(Chars.isNameBoundary('A', ch0), true);
		assertEquals(Chars.isNameBoundary('A', 'a'), false);
		assertEquals(Chars.isNameBoundary('A', 'A'), false);
		assertEquals(Chars.isNameBoundary('A', '1'), true);
		assertEquals(Chars.isNameBoundary('A', '_'), true);
		assertEquals(Chars.isNameBoundary('1', ch0), true);
		assertEquals(Chars.isNameBoundary('1', 'a'), true);
		assertEquals(Chars.isNameBoundary('1', 'A'), true);
		assertEquals(Chars.isNameBoundary('1', '1'), false);
		assertEquals(Chars.isNameBoundary('1', '_'), true);
		assertEquals(Chars.isNameBoundary('_', ch0), false);
		assertEquals(Chars.isNameBoundary('_', 'a'), true);
		assertEquals(Chars.isNameBoundary('_', 'A'), true);
		assertEquals(Chars.isNameBoundary('_', '1'), true);
		assertEquals(Chars.isNameBoundary('_', '_'), false);
	}

	@Test
	public void testEscape() {
		assertString(Chars.escape(ch0), "\\0");
		assertString(Chars.escape('\\'), "\\\\");
		assertString(Chars.escape('\177'), "\\u007f");
		assertString(Chars.escape('a'), "a");
		assertString(Chars.escape(null), "");
		assertString(Chars.escape("\\ \b \u001b \f \r \n \t \0 \1 \177 \377 a"),
			"\\\\ \\b \\e \\f \\r \\n \\t \\0 \\u0001 \\u007f \377 a");
		var s = "abc ";
		assertSame(Chars.escape(s), s);
	}

	@Test
	public void testUnEscape() {
		assertString(Chars.unescape(null), "");
		assertString(Chars.unescape( //
			"\\\\ \\b \\e \\f \\r \\n \\t \\0 \\000 \\u0001 \\u007f \377 a \\x00 \\xff"),
			"\\ \b \u001b \f \r \n \t \0 \0 \1 \177 \377 a \0 \377");
		assertString(Chars.unescape("\\\\\\b\\e\\f\\r\\n\\t\\0\\000\\u0001\\u007f\377a\\x00\\xff"),
			"\\\b\u001b\f\r\n\t\0\0\1\177\377a\0\377");
		assertString(Chars.unescape("\\x\\x0"), "\\x\\x0");
		assertString(Chars.unescape("\\8\\18\\378\\477"), "\\8\u00018\u001f8\u00277");
		assertString(Chars.unescape("\\u\\u0\\u00\\u000"), "\\u\\u0\\u00\\u000");
		var s = "abc ";
		assertSame(Chars.unescape(s), s);
	}
}
