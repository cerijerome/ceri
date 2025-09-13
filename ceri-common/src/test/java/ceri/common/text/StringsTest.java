package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertString;
import org.junit.Test;
import ceri.common.stream.IntStream;

public class StringsTest {
	private static final int _1B = 'A';
	private static final int _2B = 0xa9; // copyright: UTF16=00a9, UTF8=c2+a9
	private static final int _3B = 0x2103; // degree celsius: UTF16=2103, UTF8=e2+84+83
	private static final int _4B = 0x1d400; // bold A: UTF16=d835+dc00, UTF8=f0+9d+90+80
	private static final String S = "\0A\u00a9\u2103\ud835\udc00";
	private static final String s = "\0a\u00a9\u2103\ud835\udc00";

	private static IntStream<RuntimeException> stream() {
		return IntStream.of(0, _1B, _2B, _3B, _4B);
	}

	private static StringBuilder b(String s) {
		return new StringBuilder(s);
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Strings.class);
		assertPrivateConstructor(Strings.Filter.class);
	}

	@Test
	public void testFilters() throws Exception {
		assertEquals(Strings.Filter.nonEmpty().test(null), false);
		assertEquals(Strings.Filter.nonEmpty().test(b("")), false);
		assertEquals(Strings.Filter.nonEmpty().test(b(" ")), true);
		assertEquals(Strings.Filter.nonBlank().test(null), false);
		assertEquals(Strings.Filter.nonBlank().test(" "), false);
		assertEquals(Strings.Filter.of(null).test(S), true);
		assertEquals(Strings.Filter.of(_ -> true).test(null), false);
		assertEquals(Strings.Filter.eq(false, s).test(S), true);
		assertEquals(Strings.Filter.eq(true, s).test(S), false);
		assertEquals(Strings.Filter.eq(true, null).test(S), false);
		assertEquals(Strings.Filter.eq(true, null).test(null), true);
		assertEquals(Strings.Filter.contains(null).test(null), true);
		assertEquals(Strings.Filter.contains(null).test(""), false);
		assertEquals(Strings.Filter.contains("").test(null), false);
		assertEquals(Strings.Filter.contains("").test(""), true);
		assertEquals(Strings.Filter.contains(b("\0A")).test(null), false);
		assertEquals(Strings.Filter.contains(b("\0A")).test(S), true);
		assertEquals(Strings.Filter.contains(b("\0a")).test(S), false);
		assertEquals(Strings.Filter.contains(false, null).test(null), true);
		assertEquals(Strings.Filter.contains(false, null).test(""), false);
		assertEquals(Strings.Filter.contains(false, "\0a").test(null), false);
		assertEquals(Strings.Filter.contains(false, "\0a").test(S), true);
		assertEquals(Strings.Filter.contains(true, "\0a").test(S), false);
	}

	@Test
	public void testSafe() {
		assertString(Strings.safe(null), "");
		assertString(Strings.safe(""), "");
		assertSame(Strings.safe(S), S);
	}

	@Test
	public void testOfCodePoints() {
		assertString(Strings.of(stream()), "\0A\u00a9\u2103\ud835\udc00");
		assertString(Strings.of(stream().iterator()), "\0A\u00a9\u2103\ud835\udc00");
	}

	@Test
	public void testIsBlank() {
		assertEquals(Strings.isBlank(null), true);
		assertEquals(Strings.isBlank(""), true);
		assertEquals(Strings.isBlank("\r\n\t"), true);
		assertEquals(Strings.isBlank("\0"), false);
		assertEquals(Strings.isBlank(S), false);
	}

	@Test
	public void testNonBlank() {
		assertEquals(Strings.nonBlank(null), false);
		assertEquals(Strings.nonBlank(""), false);
		assertEquals(Strings.nonBlank("\r\n\t"), false);
		assertEquals(Strings.nonBlank("\0"), true);
		assertEquals(Strings.nonBlank(S), true);
	}

	@Test
	public void testLower() {
		assertString(Strings.lower(null), "");
		assertString(Strings.lower(""), "");
		assertString(Strings.lower("\0aBcD"), "\0abcd");
	}

	@Test
	public void testUpper() {
		assertString(Strings.upper(null), "");
		assertString(Strings.upper(""), "");
		assertString(Strings.upper("\0aBcD"), "\0ABCD");
	}

	@Test
	public void testRepeat() {
		assertString(Strings.repeat('\0', -1), "");
		assertString(Strings.repeat('\0', 0), "");
		assertString(Strings.repeat('\0', 1), "\0");
		assertString(Strings.repeat('\0', 3), "\0\0\0");
		assertString(Strings.repeat(null, -1), "");
		assertString(Strings.repeat(null, 0), "");
		assertString(Strings.repeat("", 3), "");
		assertString(Strings.repeat("abc", 0), "");
		assertString(Strings.repeat("abc", 1), "abc");
		assertString(Strings.repeat("abc", 3), "abcabcabc");
	}

	@Test
	public void testReverse() {
		assertString(Strings.reverse(null), "");
		assertString(Strings.reverse(""), "");
		assertString(Strings.reverse("\u0000\u00ff\uffff"), "\uffff\u00ff\u0000");
	}

	@Test
	public void testCompact() {
		assertString(Strings.compact(null), "");
		assertString(Strings.compact(""), "");
		assertString(Strings.compact("  \t\rt\r   \ne s\tt"), "t e s t");
	}

	@Test
	public void testCompactFloatingPoint() {
		assertString(Strings.compact(0.15f), "0.15");
		assertString(Strings.compact(100.0f), "100");
		assertString(Strings.compact(1.2), "1.2");
		assertString(Strings.compact(11.0), "11");
		assertString(Strings.compact(0.1555555555, 3), "0.156");
		assertString(Strings.compact(0.9999, 3), "1");
	}

	@Test
	public void testSub() {
		assertString(Strings.sub(null, 0), "");
		assertString(Strings.sub("", 1), "");
		assertSame(Strings.sub(S, 0), S);
		assertString(Strings.sub(S, 2, 3), "\u00a9\u2103\ud835");
	}

	@Test
	public void testPad() {
		assertString(Strings.pad(null, 0, " "), "");
		assertString(Strings.pad(null, 1, ""), "");
		assertString(Strings.pad(null, -1, ""), "");
		assertString(Strings.pad(null, 1, " "), " ");
		assertString(Strings.pad(null, -1, " "), " ");
		assertSame(Strings.pad(s, 0, " "), s);
		assertSame(Strings.pad(s, 10, ""), s);
		assertSame(Strings.pad(s, -10, ""), s);
		assertSame(Strings.pad(s, 1, " "), s);
		assertSame(Strings.pad(s, -1, " "), s);
		assertString(Strings.pad(s, 8, " "), "  " + s);
		assertString(Strings.pad(s, -8, " "), s + "  ");
		assertString(Strings.pad(s, 8, "<>"), "<>" + s);
		assertString(Strings.pad(s, 9, "<>"), "<>" + s);
		assertString(Strings.pad(s, 10, "<>"), "<><>" + s);
	}

	@Test
	public void testPadWithRatio() {
		assertEquals(Strings.pad(s, 10, "<>", 0.5), "<>" + s + "<>");
		assertEquals(Strings.pad(s, 12, "<>", 0.5), "<><>" + s + "<>");
		assertEquals(Strings.pad(s, 12, "<>", 0.4), "<>" + s + "<><>");
	}
	
	@Test
	public void testEquals() {
		assertEquals(Strings.equals(null, null), true);
		assertEquals(Strings.equals(null, 0, null, 1), true);
		assertEquals(Strings.equals(null, ""), false);
		assertEquals(Strings.equals("", null), false);
		assertEquals(Strings.equals("", ""), true);
		assertEquals(Strings.equals(b(S), S), true);
		assertEquals(Strings.equals(S, 2, b(S), 2), true);
		assertEquals(Strings.equals(S, 1, b(S), 1, 3), true);
		assertEquals(Strings.equals(b(S), S + "\0"), false);
		assertEquals(Strings.equals(S, 1, S, 1, 2), true);
		assertEquals(Strings.equals(S, 0, S, 1, 2), false);
	}

	@Test
	public void testEqualsWithCase() {
		assertEquals(Strings.equals(true, null, null), true);
		assertEquals(Strings.equals(true, null, ""), false);
		assertEquals(Strings.equals(true, "", null), false);
		assertEquals(Strings.equals(true, "", ""), true);
		assertEquals(Strings.equals(true, S, S), true);
		assertEquals(Strings.equals(true, s, S), false);
		assertEquals(Strings.equals(false, null, null), true);
		assertEquals(Strings.equals(false, null, ""), false);
		assertEquals(Strings.equals(false, "", null), false);
		assertEquals(Strings.equals(false, "", ""), true);
		assertEquals(Strings.equals(false, S, S), true);
		assertEquals(Strings.equals(false, s, S), true);
		assertEquals(Strings.equals(true, null, 0, null, 1), true);
		assertEquals(Strings.equals(true, null, 0, "", 1), false);
		assertEquals(Strings.equals(true, "", 0, null, 1), false);
		assertEquals(Strings.equals(true, s, 1, S, 1), false);
		assertEquals(Strings.equals(false, s, 1, S, 1), true);
		assertEquals(Strings.equals(false, s, 1, S + "\0", 1), false);
	}

	@Test
	public void testEqualsAt() {
		assertEquals(Strings.equalsAt(null, 0, null, 0), true);
		assertEquals(Strings.equalsAt(null, 0, "", 0), false);
		assertEquals(Strings.equalsAt("", 0, null, 0), false);
		assertEquals(Strings.equalsAt("", 0, "", 0), true);
		assertEquals(Strings.equalsAt(b("abcde"), 1, "bcd"), true);
		assertEquals(Strings.equalsAt(b("abcde"), 1, "bCd"), false);
		assertEquals(Strings.equalsAt(b("abcde"), 0, "bcd"), false);
	}
	
	@Test
	public void testEqualsAtWithCase() {
		assertEquals(Strings.equalsAt(false, null, 0, null, 0), true);
		assertEquals(Strings.equalsAt(false, null, 0, "", 0), false);
		assertEquals(Strings.equalsAt(false, "", 0, null, 0), false);
		assertEquals(Strings.equalsAt(false, "", 0, "", 0), true);
		assertEquals(Strings.equalsAt(false, "abcde", 1, "bcd"), true);
		assertEquals(Strings.equalsAt(false, "abcde", 1, "bCd"), true);
		assertEquals(Strings.equalsAt(true, "abcde", 1, "bCd"), false);
		assertEquals(Strings.equalsAt(false, "abcde", 0, "bcd"), false);
	}
	
	@Test
	public void testStartsWith() {
		assertEquals(Strings.startsWith(null, ""), false);
		assertEquals(Strings.startsWith("", null), false);
		assertEquals(Strings.startsWith("", b("")), true);
		assertEquals(Strings.startsWith(b(""), ""), true);
		assertEquals(Strings.startsWith(b("abc"), "ab"), true);
		assertEquals(Strings.startsWith(b("abc"), "bc"), false);
	}

	@Test
	public void testStartsWithCase() {
		assertEquals(Strings.startsWith(false, null, ""), false);
		assertEquals(Strings.startsWith(false, "", null), false);
		assertEquals(Strings.startsWith(false, "", ""), true);
		assertEquals(Strings.startsWith(true, "", ""), true);
		assertEquals(Strings.startsWith(false, "abc", "aB"), true);
		assertEquals(Strings.startsWith(true, "abc", "aB"), false);
		assertEquals(Strings.startsWith(true, "abc", "ab"), true);
		assertEquals(Strings.startsWith(false, "abc", "bc"), false);
		assertEquals(Strings.startsWith(true, "abc", "bc"), false);
	}

	@Test
	public void testEndsWith() {
		assertEquals(Strings.endsWith(null, ""), false);
		assertEquals(Strings.endsWith("", null), false);
		assertEquals(Strings.endsWith("", b("")), true);
		assertEquals(Strings.endsWith(b(""), ""), true);
		assertEquals(Strings.endsWith(b("abc"), "bc"), true);
		assertEquals(Strings.endsWith(b("abc"), "ab"), false);
	}

	@Test
	public void testEndsWithCase() {
		assertEquals(Strings.endsWith(false, null, ""), false);
		assertEquals(Strings.endsWith(false, "", null), false);
		assertEquals(Strings.endsWith(false, "", ""), true);
		assertEquals(Strings.endsWith(true, "", ""), true);
		assertEquals(Strings.endsWith(false, "abc", "Bc"), true);
		assertEquals(Strings.endsWith(true, "abc", "Bc"), false);
		assertEquals(Strings.endsWith(true, "abc", "bc"), true);
		assertEquals(Strings.endsWith(false, "abc", "ab"), false);
		assertEquals(Strings.endsWith(true, "abc", "ab"), false);
	}

	@Test
	public void testContains() {
		assertEquals(Strings.contains(null, null), false);
		assertEquals(Strings.contains("", null), false);
		assertEquals(Strings.contains(null, ""), false);
		assertEquals(Strings.contains(b(S), "A\u00a9"), true);
		assertEquals(Strings.contains(b(S), "A\u00a8"), false);
		assertEquals(Strings.contains(S, b("A\u00a9")), true);
		assertEquals(Strings.contains(S, b("A\u00a8")), false);
		assertEquals(Strings.contains(S, 1, b(S), 2), true);
		assertEquals(Strings.contains(S, 2, b(S), 1), false);
	}

	@Test
	public void testContainsWithCase() {
		assertEquals(Strings.contains(false, null, null), false);
		assertEquals(Strings.contains(false, "", null), false);
		assertEquals(Strings.contains(false, null, ""), false);
		assertEquals(Strings.contains(true, S, "a\u00a9"), false);
		assertEquals(Strings.contains(false, S, "a\u00a9"), true);
		assertEquals(Strings.contains(true, S, 1, s, 3), true);
		assertEquals(Strings.contains(true, S, 0, s, 1), false);
		assertEquals(Strings.contains(false, S, 0, s, 1), true);
	}

	@Test
	public void testIsNameBoundary() {
		assertEquals(Strings.isNameBoundary(null, 0), false);
		assertEquals(Strings.isNameBoundary("", 0), true);
		assertEquals(Strings.isNameBoundary("abc", 0), true);
		assertEquals(Strings.isNameBoundary("abc", 1), false);
		assertEquals(Strings.isNameBoundary("abc", 2), false);
		assertEquals(Strings.isNameBoundary("abc", 3), true);
		assertEquals(Strings.isNameBoundary("abCde", 1), false);
		assertEquals(Strings.isNameBoundary("abCde", 2), true);
		assertEquals(Strings.isNameBoundary("abCde", 3), false);
		assertEquals(Strings.isNameBoundary("ab123", 1), false);
		assertEquals(Strings.isNameBoundary("ab123", 2), true);
		assertEquals(Strings.isNameBoundary("ab123", 3), false);
		assertEquals(Strings.isNameBoundary("ab__de", 1), false);
		assertEquals(Strings.isNameBoundary("ab__de", 2), true);
		assertEquals(Strings.isNameBoundary("ab__de", 3), false);
		assertEquals(Strings.isNameBoundary("ab__de", 4), true);
	}

	@Test
	public void testDecimalFormat() {
		assertString(Strings.decimalFormat(0).format(0.01), "0");
		assertString(Strings.decimalFormat(0).format(0.50001), "1");
		assertString(Strings.decimalFormat(2).format(0.5), "0.5");
		assertString(Strings.decimalFormat(2).format(10), "10");
		assertString(Strings.decimalFormat(2).format(10.011), "10.01");
	}

	@Test
	public void testPrintable() {
		assertString(Strings.printable(null), "");
		assertString(Strings.printable(""), "");
		assertString(Strings.printable("ab\0c\u2081\uffff"), "ab.c\u2081.");
	}

	@Test
	public void testReplaceChars() {
		assertString(Strings.replaceChars(null, _ -> '.'), "");
		assertString(Strings.replaceChars("", _ -> '.'), "");
		assertString(Strings.replaceChars("abc", _ -> '.'), "...");
		assertSame(Strings.replaceChars(S, null), S);
		assertSame(Strings.replaceChars(S, c -> c), S);
	}

	@Test
	public void testPrinted() {
		assertString(Strings.printed(out -> out.print("test")), "test");
	}
}
