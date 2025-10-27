package ceri.common.text;

import org.junit.Test;
import ceri.common.stream.IntStream;
import ceri.common.test.Assert;

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
		Assert.privateConstructor(Strings.class);
		Assert.privateConstructor(Strings.Filter.class);
	}

	@Test
	public void testFilters() throws Exception {
		Assert.equal(Strings.Filter.nonEmpty().test(null), false);
		Assert.equal(Strings.Filter.nonEmpty().test(b("")), false);
		Assert.equal(Strings.Filter.nonEmpty().test(b(" ")), true);
		Assert.equal(Strings.Filter.nonBlank().test(null), false);
		Assert.equal(Strings.Filter.nonBlank().test(" "), false);
		Assert.equal(Strings.Filter.of(null).test(S), true);
		Assert.equal(Strings.Filter.of(_ -> true).test(null), false);
		Assert.equal(Strings.Filter.eq(false, s).test(S), true);
		Assert.equal(Strings.Filter.eq(true, s).test(S), false);
		Assert.equal(Strings.Filter.eq(true, null).test(S), false);
		Assert.equal(Strings.Filter.eq(true, null).test(null), true);
		Assert.equal(Strings.Filter.contains(null).test(null), true);
		Assert.equal(Strings.Filter.contains(null).test(""), false);
		Assert.equal(Strings.Filter.contains("").test(null), false);
		Assert.equal(Strings.Filter.contains("").test(""), true);
		Assert.equal(Strings.Filter.contains(b("\0A")).test(null), false);
		Assert.equal(Strings.Filter.contains(b("\0A")).test(S), true);
		Assert.equal(Strings.Filter.contains(b("\0a")).test(S), false);
		Assert.equal(Strings.Filter.contains(false, null).test(null), true);
		Assert.equal(Strings.Filter.contains(false, null).test(""), false);
		Assert.equal(Strings.Filter.contains(false, "\0a").test(null), false);
		Assert.equal(Strings.Filter.contains(false, "\0a").test(S), true);
		Assert.equal(Strings.Filter.contains(true, "\0a").test(S), false);
	}

	@Test
	public void testSafe() {
		Assert.string(Strings.safe(null), "");
		Assert.string(Strings.safe(""), "");
		Assert.same(Strings.safe(S), S);
	}

	@Test
	public void testOfCodePoints() {
		Assert.string(Strings.of(stream()), "\0A\u00a9\u2103\ud835\udc00");
		Assert.string(Strings.of(stream().iterator()), "\0A\u00a9\u2103\ud835\udc00");
	}

	@Test
	public void testIsBlank() {
		Assert.equal(Strings.isBlank(null), true);
		Assert.equal(Strings.isBlank(""), true);
		Assert.equal(Strings.isBlank("\r\n\t"), true);
		Assert.equal(Strings.isBlank("\0"), false);
		Assert.equal(Strings.isBlank(S), false);
	}

	@Test
	public void testNonBlank() {
		Assert.equal(Strings.nonBlank(null), false);
		Assert.equal(Strings.nonBlank(""), false);
		Assert.equal(Strings.nonBlank("\r\n\t"), false);
		Assert.equal(Strings.nonBlank("\0"), true);
		Assert.equal(Strings.nonBlank(S), true);
	}

	@Test
	public void testLower() {
		Assert.string(Strings.lower(null), "");
		Assert.string(Strings.lower(""), "");
		Assert.string(Strings.lower("\0aBcD"), "\0abcd");
	}

	@Test
	public void testUpper() {
		Assert.string(Strings.upper(null), "");
		Assert.string(Strings.upper(""), "");
		Assert.string(Strings.upper("\0aBcD"), "\0ABCD");
	}

	@Test
	public void testRepeat() {
		Assert.string(Strings.repeat('\0', -1), "");
		Assert.string(Strings.repeat('\0', 0), "");
		Assert.string(Strings.repeat('\0', 1), "\0");
		Assert.string(Strings.repeat('\0', 3), "\0\0\0");
		Assert.string(Strings.repeat(null, -1), "");
		Assert.string(Strings.repeat(null, 0), "");
		Assert.string(Strings.repeat("", 3), "");
		Assert.string(Strings.repeat("abc", 0), "");
		Assert.string(Strings.repeat("abc", 1), "abc");
		Assert.string(Strings.repeat("abc", 3), "abcabcabc");
	}

	@Test
	public void testReverse() {
		Assert.string(Strings.reverse(null), "");
		Assert.string(Strings.reverse(""), "");
		Assert.string(Strings.reverse("\u0000\u00ff\uffff"), "\uffff\u00ff\u0000");
	}

	@Test
	public void testCompact() {
		Assert.string(Strings.compact(null), "");
		Assert.string(Strings.compact(""), "");
		Assert.string(Strings.compact("  \t\rt\r   \ne s\tt"), "t e s t");
	}

	@Test
	public void testCompactFloatingPoint() {
		Assert.string(Strings.compact(0.15f), "0.15");
		Assert.string(Strings.compact(100.0f), "100");
		Assert.string(Strings.compact(1.2), "1.2");
		Assert.string(Strings.compact(11.0), "11");
		Assert.string(Strings.compact(0.1555555555, 3), "0.156");
		Assert.string(Strings.compact(0.9999, 3), "1");
	}

	@Test
	public void testSub() {
		Assert.string(Strings.sub(null, 0), "");
		Assert.string(Strings.sub("", 1), "");
		Assert.same(Strings.sub(S, 0), S);
		Assert.string(Strings.sub(S, 0, 0), "");
		Assert.string(Strings.sub(S, 2, 3), "\u00a9\u2103\ud835");
	}

	@Test
	public void testPad() {
		Assert.string(Strings.pad(null, 0, " "), "");
		Assert.string(Strings.pad(null, 1, ""), "");
		Assert.string(Strings.pad(null, -1, ""), "");
		Assert.string(Strings.pad(null, 1, " "), " ");
		Assert.string(Strings.pad(null, -1, " "), " ");
		Assert.same(Strings.pad(s, 0, " "), s);
		Assert.same(Strings.pad(s, 10, ""), s);
		Assert.same(Strings.pad(s, -10, ""), s);
		Assert.same(Strings.pad(s, 1, " "), s);
		Assert.same(Strings.pad(s, -1, " "), s);
		Assert.string(Strings.pad(s, 8, " "), "  " + s);
		Assert.string(Strings.pad(s, -8, " "), s + "  ");
		Assert.string(Strings.pad(s, 8, "<>"), "<>" + s);
		Assert.string(Strings.pad(s, 9, "<>"), "<>" + s);
		Assert.string(Strings.pad(s, 10, "<>"), "<><>" + s);
	}

	@Test
	public void testPadWithRatio() {
		Assert.equal(Strings.pad(s, 10, "<>", 0.5), "<>" + s + "<>");
		Assert.equal(Strings.pad(s, 12, "<>", 0.5), "<><>" + s + "<>");
		Assert.equal(Strings.pad(s, 12, "<>", 0.4), "<>" + s + "<><>");
	}

	@Test
	public void testHash() {
		Assert.equal(Strings.hash(null), 0);
		Assert.equal(Strings.hash(""), 0);
		Assert.equal(Strings.hash("abc"), 0x1ecc1);
	}

	@Test
	public void testEquals() {
		Assert.equal(Strings.equals(null, null), true);
		Assert.equal(Strings.equals(null, 0, null, 1), true);
		Assert.equal(Strings.equals(null, ""), false);
		Assert.equal(Strings.equals("", null), false);
		Assert.equal(Strings.equals("", ""), true);
		Assert.equal(Strings.equals(b(S), S), true);
		Assert.equal(Strings.equals(S, 2, b(S), 2), true);
		Assert.equal(Strings.equals(S, 1, b(S), 1, 3), true);
		Assert.equal(Strings.equals(b(S), S + "\0"), false);
		Assert.equal(Strings.equals(S, 1, S, 1, 2), true);
		Assert.equal(Strings.equals(S, 0, S, 1, 2), false);
	}

	@Test
	public void testEqualsWithCase() {
		Assert.equal(Strings.equals(true, null, null), true);
		Assert.equal(Strings.equals(true, null, ""), false);
		Assert.equal(Strings.equals(true, "", null), false);
		Assert.equal(Strings.equals(true, "", ""), true);
		Assert.equal(Strings.equals(true, S, S), true);
		Assert.equal(Strings.equals(true, s, S), false);
		Assert.equal(Strings.equals(false, null, null), true);
		Assert.equal(Strings.equals(false, null, ""), false);
		Assert.equal(Strings.equals(false, "", null), false);
		Assert.equal(Strings.equals(false, "", ""), true);
		Assert.equal(Strings.equals(false, S, S), true);
		Assert.equal(Strings.equals(false, s, S), true);
		Assert.equal(Strings.equals(true, null, 0, null, 1), true);
		Assert.equal(Strings.equals(true, null, 0, "", 1), false);
		Assert.equal(Strings.equals(true, "", 0, null, 1), false);
		Assert.equal(Strings.equals(true, s, 1, S, 1), false);
		Assert.equal(Strings.equals(false, s, 1, S, 1), true);
		Assert.equal(Strings.equals(false, s, 1, S + "\0", 1), false);
	}

	@Test
	public void testEqualsAt() {
		Assert.equal(Strings.equalsAt(null, 0, null, 0), true);
		Assert.equal(Strings.equalsAt(null, 0, "", 0), false);
		Assert.equal(Strings.equalsAt("", 0, null, 0), false);
		Assert.equal(Strings.equalsAt("", 0, "", 0), true);
		Assert.equal(Strings.equalsAt(b("abcde"), 1, "bcd"), true);
		Assert.equal(Strings.equalsAt(b("abcde"), 1, "bCd"), false);
		Assert.equal(Strings.equalsAt(b("abcde"), 0, "bcd"), false);
	}

	@Test
	public void testEqualsAtWithCase() {
		Assert.equal(Strings.equalsAt(false, null, 0, null, 0), true);
		Assert.equal(Strings.equalsAt(false, null, 0, "", 0), false);
		Assert.equal(Strings.equalsAt(false, "", 0, null, 0), false);
		Assert.equal(Strings.equalsAt(false, "", 0, "", 0), true);
		Assert.equal(Strings.equalsAt(false, "abcde", 1, "bcd"), true);
		Assert.equal(Strings.equalsAt(false, "abcde", 1, "bCd"), true);
		Assert.equal(Strings.equalsAt(true, "abcde", 1, "bCd"), false);
		Assert.equal(Strings.equalsAt(false, "abcde", 0, "bcd"), false);
	}

	@Test
	public void testStartsWith() {
		Assert.equal(Strings.startsWith(null, ""), false);
		Assert.equal(Strings.startsWith("", null), false);
		Assert.equal(Strings.startsWith("", b("")), true);
		Assert.equal(Strings.startsWith(b(""), ""), true);
		Assert.equal(Strings.startsWith(b("abc"), "ab"), true);
		Assert.equal(Strings.startsWith(b("abc"), "bc"), false);
	}

	@Test
	public void testStartsWithCase() {
		Assert.equal(Strings.startsWith(false, null, ""), false);
		Assert.equal(Strings.startsWith(false, "", null), false);
		Assert.equal(Strings.startsWith(false, "", ""), true);
		Assert.equal(Strings.startsWith(true, "", ""), true);
		Assert.equal(Strings.startsWith(false, "abc", "aB"), true);
		Assert.equal(Strings.startsWith(true, "abc", "aB"), false);
		Assert.equal(Strings.startsWith(true, "abc", "ab"), true);
		Assert.equal(Strings.startsWith(false, "abc", "bc"), false);
		Assert.equal(Strings.startsWith(true, "abc", "bc"), false);
	}

	@Test
	public void testEndsWith() {
		Assert.equal(Strings.endsWith(null, ""), false);
		Assert.equal(Strings.endsWith("", null), false);
		Assert.equal(Strings.endsWith("", b("")), true);
		Assert.equal(Strings.endsWith(b(""), ""), true);
		Assert.equal(Strings.endsWith(b("abc"), "bc"), true);
		Assert.equal(Strings.endsWith(b("abc"), "ab"), false);
	}

	@Test
	public void testEndsWithCase() {
		Assert.equal(Strings.endsWith(false, null, ""), false);
		Assert.equal(Strings.endsWith(false, "", null), false);
		Assert.equal(Strings.endsWith(false, "", ""), true);
		Assert.equal(Strings.endsWith(true, "", ""), true);
		Assert.equal(Strings.endsWith(false, "abc", "Bc"), true);
		Assert.equal(Strings.endsWith(true, "abc", "Bc"), false);
		Assert.equal(Strings.endsWith(true, "abc", "bc"), true);
		Assert.equal(Strings.endsWith(false, "abc", "ab"), false);
		Assert.equal(Strings.endsWith(true, "abc", "ab"), false);
	}

	@Test
	public void testContains() {
		Assert.equal(Strings.contains(null, null), false);
		Assert.equal(Strings.contains("", null), false);
		Assert.equal(Strings.contains(null, ""), false);
		Assert.equal(Strings.contains(b(S), "A\u00a9"), true);
		Assert.equal(Strings.contains(b(S), "A\u00a8"), false);
		Assert.equal(Strings.contains(S, b("A\u00a9")), true);
		Assert.equal(Strings.contains(S, b("A\u00a8")), false);
		Assert.equal(Strings.contains(S, 1, b(S), 2), true);
		Assert.equal(Strings.contains(S, 2, b(S), 1), false);
	}

	@Test
	public void testContainsWithCase() {
		Assert.equal(Strings.contains(false, null, null), false);
		Assert.equal(Strings.contains(false, "", null), false);
		Assert.equal(Strings.contains(false, null, ""), false);
		Assert.equal(Strings.contains(true, S, "a\u00a9"), false);
		Assert.equal(Strings.contains(false, S, "a\u00a9"), true);
		Assert.equal(Strings.contains(true, S, 1, s, 3), true);
		Assert.equal(Strings.contains(true, S, 0, s, 1), false);
		Assert.equal(Strings.contains(false, S, 0, s, 1), true);
	}

	@Test
	public void testIsNameBoundary() {
		Assert.equal(Strings.isNameBoundary(null, 0), false);
		Assert.equal(Strings.isNameBoundary("", 0), true);
		Assert.equal(Strings.isNameBoundary("abc", 0), true);
		Assert.equal(Strings.isNameBoundary("abc", 1), false);
		Assert.equal(Strings.isNameBoundary("abc", 2), false);
		Assert.equal(Strings.isNameBoundary("abc", 3), true);
		Assert.equal(Strings.isNameBoundary("abCde", 1), false);
		Assert.equal(Strings.isNameBoundary("abCde", 2), true);
		Assert.equal(Strings.isNameBoundary("abCde", 3), false);
		Assert.equal(Strings.isNameBoundary("ab123", 1), false);
		Assert.equal(Strings.isNameBoundary("ab123", 2), true);
		Assert.equal(Strings.isNameBoundary("ab123", 3), false);
		Assert.equal(Strings.isNameBoundary("ab__de", 1), false);
		Assert.equal(Strings.isNameBoundary("ab__de", 2), true);
		Assert.equal(Strings.isNameBoundary("ab__de", 3), false);
		Assert.equal(Strings.isNameBoundary("ab__de", 4), true);
	}

	@Test
	public void testDecimalFormat() {
		Assert.string(Strings.decimalFormat(0).format(0.01), "0");
		Assert.string(Strings.decimalFormat(0).format(0.50001), "1");
		Assert.string(Strings.decimalFormat(2).format(0.5), "0.5");
		Assert.string(Strings.decimalFormat(2).format(10), "10");
		Assert.string(Strings.decimalFormat(2).format(10.011), "10.01");
	}

	@Test
	public void testPrintable() {
		Assert.string(Strings.printable(null), "");
		Assert.string(Strings.printable(""), "");
		Assert.string(Strings.printable("ab\0c\u2081\uffff"), "ab.c\u2081.");
	}

	@Test
	public void testReplaceChars() {
		Assert.string(Strings.replaceChars(null, _ -> '.'), "");
		Assert.string(Strings.replaceChars("", _ -> '.'), "");
		Assert.string(Strings.replaceChars("abc", _ -> '.'), "...");
		Assert.same(Strings.replaceChars(S, null), S);
		Assert.same(Strings.replaceChars(S, c -> c), S);
	}

	@Test
	public void testPrinted() {
		Assert.string(Strings.printed(out -> out.print("test")), "test");
	}
}
