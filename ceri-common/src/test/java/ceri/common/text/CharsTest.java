package ceri.common.text;

import org.junit.After;
import org.junit.Test;
import ceri.common.test.Assert;

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
		Assert.privateConstructor(Chars.class);
		Assert.privateConstructor(Chars.Escape.class);
	}

	@Test
	public void testSafe() {
		Assert.string(Chars.safe(null), "");
		Assert.same(Chars.safe(b("")), b);
	}

	@Test
	public void testAt() {
		Assert.equal(Chars.at(null, 0), null);
		Assert.equal(Chars.at("", 0), null);
		Assert.equal(Chars.at("test", -1), null);
		Assert.equal(Chars.at("test", 4), null);
		Assert.equal(Chars.at("test", 0), 't');
		Assert.equal(Chars.at(null, 0, 'x'), 'x');
		Assert.equal(Chars.at("", 0, 'x'), 'x');
		Assert.equal(Chars.at("test", -1, 'x'), 'x');
		Assert.equal(Chars.at("test", 4, 'x'), 'x');
		Assert.equal(Chars.at("test", 2, 'x'), 's');
	}

	@Test
	public void testLower() {
		Assert.string(Chars.lower(null), "");
		Assert.string(Chars.lower(""), "");
		Assert.string(Chars.lower("_Aa\u03a9\u03c9"), "_aa\u03c9\u03c9");
	}

	@Test
	public void testUpper() {
		Assert.string(Chars.upper(null), "");
		Assert.string(Chars.upper(""), "");
		Assert.string(Chars.upper("_Aa\u03a9\u03c9"), "_AA\u03a9\u03a9");
	}

	@Test
	public void testEquals() {
		Assert.equal(Chars.equals(null, 0, null, 0), false);
		Assert.equal(Chars.equals(null, 0, "", 0), false);
		Assert.equal(Chars.equals("", 0, null, 0), false);
		Assert.equal(Chars.equals("", 0, "", 0), false);
		Assert.equal(Chars.equals("a", -1, "a", 0), false);
		Assert.equal(Chars.equals("a", 1, "a", 0), false);
		Assert.equal(Chars.equals("a", 0, "a", -1), false);
		Assert.equal(Chars.equals("a", 0, "a", 1), false);
		Assert.equal(Chars.equals("abc", 1, "abc", 1), true);
		Assert.equal(Chars.equals("abc", 1, "aBc", 1), false);
	}

	@Test
	public void testIsPrintable() {
		Assert.equal(Chars.isPrintable(null, 0), false);
		Assert.equal(Chars.isPrintable("A", 0), true);
		Assert.equal(Chars.isPrintable("A", 1), false);
		Assert.equal(Chars.isPrintable(Chars.NUL), false);
		Assert.equal(Chars.isPrintable('A'), true);
		Assert.equal(Chars.isPrintable('\u1c00'), true);
		Assert.equal(Chars.isPrintable(Character.MAX_HIGH_SURROGATE), true);
		Assert.equal(Chars.isPrintable('\uffff'), false);
	}

	@Test
	public void testIsNameBoundary() {
		Assert.equal(Chars.isNameBoundary(ch0, ch0), false);
		Assert.equal(Chars.isNameBoundary(ch0, 'a'), true);
		Assert.equal(Chars.isNameBoundary(ch0, 'A'), true);
		Assert.equal(Chars.isNameBoundary(ch0, '1'), true);
		Assert.equal(Chars.isNameBoundary(ch0, '_'), false);
		Assert.equal(Chars.isNameBoundary('a', ch0), true);
		Assert.equal(Chars.isNameBoundary('a', 'a'), false);
		Assert.equal(Chars.isNameBoundary('a', 'A'), true);
		Assert.equal(Chars.isNameBoundary('a', '1'), true);
		Assert.equal(Chars.isNameBoundary('a', '_'), true);
		Assert.equal(Chars.isNameBoundary('A', ch0), true);
		Assert.equal(Chars.isNameBoundary('A', 'a'), false);
		Assert.equal(Chars.isNameBoundary('A', 'A'), false);
		Assert.equal(Chars.isNameBoundary('A', '1'), true);
		Assert.equal(Chars.isNameBoundary('A', '_'), true);
		Assert.equal(Chars.isNameBoundary('1', ch0), true);
		Assert.equal(Chars.isNameBoundary('1', 'a'), true);
		Assert.equal(Chars.isNameBoundary('1', 'A'), true);
		Assert.equal(Chars.isNameBoundary('1', '1'), false);
		Assert.equal(Chars.isNameBoundary('1', '_'), true);
		Assert.equal(Chars.isNameBoundary('_', ch0), false);
		Assert.equal(Chars.isNameBoundary('_', 'a'), true);
		Assert.equal(Chars.isNameBoundary('_', 'A'), true);
		Assert.equal(Chars.isNameBoundary('_', '1'), true);
		Assert.equal(Chars.isNameBoundary('_', '_'), false);
	}

	@Test
	public void testEscape() {
		Assert.string(Chars.escape(ch0), "\\0");
		Assert.string(Chars.escape('\\'), "\\\\");
		Assert.string(Chars.escape('\177'), "\\u007f");
		Assert.string(Chars.escape('a'), "a");
		Assert.string(Chars.escape(null), "");
		Assert.string(Chars.escape("\\ \b \u001b \f \r \n \t \0 \1 \177 \377 a"),
			"\\\\ \\b \\e \\f \\r \\n \\t \\0 \\u0001 \\u007f \377 a");
		var s = "abc ";
		Assert.same(Chars.escape(s), s);
	}

	@Test
	public void testUnEscape() {
		Assert.string(Chars.unescape(null), "");
		Assert.string(Chars.unescape( //
			"\\\\ \\b \\e \\f \\r \\n \\t \\0 \\000 \\u0001 \\u007f \377 a \\x00 \\xff"),
			"\\ \b \u001b \f \r \n \t \0 \0 \1 \177 \377 a \0 \377");
		Assert.string(Chars.unescape("\\\\\\b\\e\\f\\r\\n\\t\\0\\000\\u0001\\u007f\377a\\x00\\xff"),
			"\\\b\u001b\f\r\n\t\0\0\1\177\377a\0\377");
		Assert.string(Chars.unescape("\\x\\x0"), "\\x\\x0");
		Assert.string(Chars.unescape("\\8\\18\\378\\477"), "\\8\u00018\u001f8\u00277");
		Assert.string(Chars.unescape("\\u\\u0\\u00\\u000"), "\\u\\u0\\u00\\u000");
		var s = "abc ";
		Assert.same(Chars.unescape(s), s);
	}
}
