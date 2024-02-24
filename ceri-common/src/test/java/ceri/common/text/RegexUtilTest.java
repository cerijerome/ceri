package ceri.common.text;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.text.StringUtil.reverse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.test.Captor;

public class RegexUtilTest {
	private static final Pattern LSTRING_PATTERN = Pattern.compile("([a-z]+)");
	private static final Pattern USTRING_PATTERN = Pattern.compile("([A-Z]+)");
	private static final Pattern INT_PATTERN = Pattern.compile("(\\d+)");
	private static final Pattern MULTI_PATTERN =
		Pattern.compile("(\\w+)\\W+(\\w+)\\W+([\\w\\" + ".]+)");

	@Test
	public void testPrivateConstructor() {
		assertPrivateConstructor(RegexUtil.class);
	}

	@Test
	public void testCommonDecodeInt() {
		assertEquals(RegexUtil.Common.decodeInt("0b001001"), 0b001001);
		assertEquals(RegexUtil.Common.decodeInt("0B111"), 0B111);
		assertEquals(RegexUtil.Common.decodeInt("0123"), 0123);
		assertEquals(RegexUtil.Common.decodeInt("0x0123"), 0x123);
		assertEquals(RegexUtil.Common.decodeInt("0X0123"), 0x123);
		assertEquals(RegexUtil.Common.decodeInt("#0123"), 0x123);
		assertEquals(RegexUtil.Common.decodeInt("123"), 123);
		assertEquals(RegexUtil.Common.decodeInt("0"), 0);
	}

	@Test
	public void testCommonDecodeLong() {
		assertEquals(RegexUtil.Common.decodeLong("0b001001"), 0b001001L);
		assertEquals(RegexUtil.Common.decodeLong("0B111"), 0B111L);
		assertEquals(RegexUtil.Common.decodeLong("0123"), 0123L);
		assertEquals(RegexUtil.Common.decodeLong("0x0123"), 0x123L);
		assertEquals(RegexUtil.Common.decodeLong("0X0123"), 0x123L);
		assertEquals(RegexUtil.Common.decodeLong("#0123"), 0x123L);
		assertEquals(RegexUtil.Common.decodeLong("123"), 123L);
		assertEquals(RegexUtil.Common.decodeLong("0"), 0L);
	}

	@Test
	public void testHashCode() {
		Pattern p0 = Pattern.compile("(?m).*");
		Pattern p1 = Pattern.compile("(?m).+");
		Pattern p2 = Pattern.compile("(?m).*", 1);
		assertEquals(RegexUtil.hashCode(null), RegexUtil.hashCode(null));
		assertEquals(RegexUtil.hashCode(p0), RegexUtil.hashCode(p0));
		assertEquals(RegexUtil.hashCode(p1), RegexUtil.hashCode(p1));
		assertEquals(RegexUtil.hashCode(p2), RegexUtil.hashCode(p2));
	}

	@Test
	public void testEquals() {
		Pattern p = Pattern.compile("test.*");
		assertTrue(RegexUtil.equals(null, null));
		assertFalse(RegexUtil.equals(p, null));
		assertFalse(RegexUtil.equals(null, p));
		assertTrue(RegexUtil.equals(p, p));
		assertTrue(RegexUtil.equals(p, Pattern.compile("test.*")));
		assertFalse(RegexUtil.equals(p, Pattern.compile("test.+")));
		assertTrue(RegexUtil.equals(Pattern.compile(".*", 1), Pattern.compile(".*", 1)));
		assertFalse(RegexUtil.equals(Pattern.compile(".*", 1), Pattern.compile(".*", 2)));
	}

	@Test
	public void testFinder() {
		assertFalse(RegexUtil.finder(INT_PATTERN).test(null));
		assertTrue(RegexUtil.finder("(\\d+)").test("abc123def456"));
		assertTrue(RegexUtil.finder(LSTRING_PATTERN).test("abc123def456"));
		assertFalse(RegexUtil.finder(USTRING_PATTERN).test("abc123def456"));
	}

	@Test
	public void testMatcher() {
		assertFalse(RegexUtil.matcher(INT_PATTERN).test(null));
		assertTrue(RegexUtil.matcher(INT_PATTERN).test("123"));
		assertFalse(RegexUtil.matcher("(\\d+)").test("123def456"));
		assertTrue(RegexUtil.matcher(LSTRING_PATTERN).test("abc"));
		assertFalse(RegexUtil.matcher(USTRING_PATTERN).test("abc"));
	}

	@Test
	public void testCompileOr() {
		Pattern p = RegexUtil.compileOr(INT_PATTERN, LSTRING_PATTERN);
		assertEquals(p.pattern().toString(), "((\\d+)|([a-z]+))");
	}

	@Test
	public void testIgnoreCase() {
		Pattern ignoreCase = RegexUtil.ignoreCase("t.e.s.t");
		assertNotNull(RegexUtil.matched(ignoreCase, "T.e.s.T"));
		assertNull(RegexUtil.matched(ignoreCase, "T e s T"));
	}

	@Test
	public void testForEach() {
		List<String> list = new ArrayList<>();
		for (MatchResult result : RegexUtil.forEach(INT_PATTERN, "123abcA1B2C3"))
			list.add(result.group());
		assertIterable(list, "123", "1", "2", "3");
	}

	@Test
	public void testReplaceAllQuoted() {
		assertEquals(RegexUtil.replaceAllQuoted(Pattern.compile("\\d+"), "abc123de45f6", m -> null),
			"abc123de45f6");
		assertEquals(RegexUtil.replaceAllQuoted(Pattern.compile("\\d+"), "abc123de45f6", "N\\$"),
			"abcN\\$deN\\$fN\\$");
		assertEquals(RegexUtil.replaceAllQuoted(Pattern.compile("[\\\\$]+"), "abc$\\def\\$",
			m -> reverse(m.group())), "abc\\$def$\\");
	}

	@Test
	public void testReplaceAllQuotedWithIndex() {
		String s = "abcdef";
		s = RegexUtil.replaceAllQuoted(Pattern.compile("[a-f]"), s, (m, i) -> "$" + i);
		assertEquals(s, "$0$1$2$3$4$5");
	}

	@Test
	public void testReplaceAll() {
		String s = "abcdefghijklmnopqrstuvwxyz";
		s = RegexUtil.replaceAll(Pattern.compile("[aeiou]"), s, m -> m.group().toUpperCase());
		assertEquals(s, "AbcdEfghIjklmnOpqrstUvwxyz");
		Pattern p = Pattern.compile("((?<!\\\\)\".*?(?<!\\\\)\"|\\s+)");
		Function<MatchResult, String> fn = r -> r.group().charAt(0) == '\"' ? null : "";
		assertEquals(RegexUtil.replaceAll(p, "", fn), "");
		assertEquals(RegexUtil.replaceAll(p, "test", fn), "test");
		assertEquals(RegexUtil.replaceAll(p, "\"test\"", fn), "\"test\"");
		assertEquals(RegexUtil.replaceAll(p, "t e s t", fn), "test");
		assertEquals(RegexUtil.replaceAll(p, "\"t e s t\"", fn), "\"t e s t\"");
		assertEquals(RegexUtil.replaceAll(p, "\"t \\\"e s\\\" t\"", fn), "\"t \\\"e s\\\" t\"");
		assertEquals(RegexUtil.replaceAll(p, "{ \"_id\" : ObjectId(\"12345\")", fn),
			"{\"_id\":ObjectId(\"12345\")");
		assertEquals(RegexUtil.replaceAll(Pattern.compile("abc"), "ab", (m, i) -> ""), "ab");
		assertEquals(RegexUtil.replaceAll(Pattern.compile("^"), "ab", (m, i) -> "x"), "xab");
		assertEquals(RegexUtil.replaceAll(Pattern.compile("ab"), "ab", (m, i) -> ""), "");
	}

	@Test
	public void testReplaceAllOptimization() {
		String s = "a1b2c3d";
		assertTrue(RegexUtil.replaceAll(Pattern.compile("x"), s, (m, i) -> "X") == s);
		assertTrue(RegexUtil.replaceAll(Pattern.compile("\\d"), s, (m, i) -> null) == s);
	}

	@Test
	public void testReplaceAllWithIndex() {
		assertEquals(
			RegexUtil.replaceAll(Pattern.compile("[a-f]"), "abcdefg", (m, i) -> String.valueOf(i)),
			"012345g");
	}

	@Test
	public void testReplaceExcept() {
		assertEquals(RegexUtil.replaceExcept(Pattern.compile(""), "", ""), "");
		assertEquals(RegexUtil.replaceExcept(Pattern.compile("[a-c]"), "AaBbCcDd", "x"), "xaxbxcx");
		assertEquals(RegexUtil.replaceExcept(Pattern.compile("[a-c]"), "abc", "x"), "abc");
		assertEquals(RegexUtil.replaceExcept(Pattern.compile("[a-c]"), "def", "x"), "x");
		assertEquals(RegexUtil.replaceExcept(Pattern.compile("[a-c]"), "def", ""), "");
		assertEquals(RegexUtil.replaceExcept(Pattern.compile("[a-c]"), "def", (String) null),
			"def");
		assertEquals(RegexUtil.replaceExcept(Pattern.compile("^"), "abc", "x"), "x");
		assertEquals(RegexUtil.replaceExcept(Pattern.compile("$"), "abc", "x"), "x");
		assertEquals(RegexUtil.replaceExcept(Pattern.compile("[a-c]+"), "abcdefbca",
			m -> m.group().toUpperCase()), "abcDEFbca");
	}

	@Test
	public void testReplaceExceptWithIndex() {
		assertEquals(RegexUtil.replaceExcept(Pattern.compile("[a-c]"), "AaBbCcDd",
			(m, i) -> String.valueOf(i)), "0a1b2c3");
	}

	@Test
	public void testSplitBefore() {
		assertIterable(RegexUtil.splitBefore(INT_PATTERN, "123abcA1B2C3"), //
			"123abcA", "1B", "2C", "3");
		assertIterable(RegexUtil.splitBefore(Pattern.compile("(?=\\d++)"), "123abcA1B2C3"), //
			"1", "2", "3abcA", "1B", "2C", "3");
	}

	@Test
	public void testSplitAfter() {
		assertIterable(RegexUtil.splitAfter(INT_PATTERN, "123abcA1B2C3"), //
			"123", "abcA1", "B2", "C3");
		assertIterable(RegexUtil.splitAfter(Pattern.compile("(?=\\d++)"), "123abcA1B2C3"), //
			"", "1", "2", "3abcA", "1B", "2C", "3");
	}

	@Test
	public void testGroups() {
		assertIterable(RegexUtil.groups(MULTI_PATTERN, " ab CD--ef"), "ab", "CD", "ef");
		assertIterable(RegexUtil.groups(MULTI_PATTERN, ""));
		assertIterable(RegexUtil.groups(Pattern.compile("abc"), "abc"));
	}

	@Test
	public void testAcceptGroup() {
		var p = Pattern.compile("(\\w+?)(\\d+)?(\\w+?)");
		var captor = Captor.<String>of();
		RegexUtil.acceptGroup(RegexUtil.matched(p, "a123b"), 2, captor::accept);
		RegexUtil.acceptGroup(RegexUtil.matched(p, "ab"), 2, captor::accept);
		captor.verify("123");
	}

	@Test
	public void testApplyGroup() {
		var p = Pattern.compile("(\\w+?)(\\d+)?(\\w+?)");
		assertEquals(RegexUtil.applyGroup(RegexUtil.matched(p, "a123b"), 2, Integer::parseInt),
			123);
		assertEquals(RegexUtil.applyGroup(RegexUtil.matched(p, "ab"), 2, Integer::parseInt), null);
	}

	@Test
	public void testTypedGroups() {
		Matcher m = MULTI_PATTERN.matcher("123 true 4.5");
		assertTrue(m.find());
		assertTrue(RegexUtil.booleanGroup(m, 2));
		assertEquals(RegexUtil.byteGroup(m, 1), (byte) 123);
		assertEquals(RegexUtil.shortGroup(m, 1), (short) 123);
		assertEquals(RegexUtil.intGroup(m, 1), 123);
		assertEquals(RegexUtil.longGroup(m, 1), 123L);
		assertEquals(RegexUtil.floatGroup(m, 3), 4.5f);
		assertEquals(RegexUtil.doubleGroup(m, 3), 4.5);
	}

	@Test
	public void testNamedGroup() {
		Pattern named = Pattern.compile("(?:(?<letter>[a-z]+)|(?<number>[0-9]+))");
		Matcher m = named.matcher("123abc45de6f");
		assertTrue(m.find());
		assertNull(RegexUtil.namedGroup(m, "test"));
		assertNull(RegexUtil.namedGroup(m, "letter"));
		assertEquals(RegexUtil.namedGroup(m, "number"), "123");
		assertTrue(m.find());
		assertEquals(RegexUtil.namedGroup(m, "letter"), "abc");
		assertNull(RegexUtil.namedGroup(m, "number"));
		assertTrue(m.find());
		assertNull(RegexUtil.namedGroup(m, "letter"));
		assertEquals(RegexUtil.namedGroup(m, "number"), "45");
		assertTrue(m.find());
		assertEquals(RegexUtil.namedGroup(m, "letter"), "de");
		assertNull(RegexUtil.namedGroup(m, "number"));
		assertTrue(m.find());
		assertNull(RegexUtil.namedGroup(m, "letter"));
		assertEquals(RegexUtil.namedGroup(m, "number"), "6");
		assertTrue(m.find());
		assertEquals(RegexUtil.namedGroup(m, "letter"), "f");
		assertNull(RegexUtil.namedGroup(m, "number"));
	}

	@Test
	public void testNamedGroups() {
		Pattern named = Pattern.compile("(?:(?<letter>[a-z]+)|(?<number>[0-9]+))");
		Matcher m = named.matcher("123abc45de6f");
		assertTrue(m.find());
		assertMap(RegexUtil.namedGroups(m), "letter", null, "number", "123");
		assertTrue(m.find());
		assertMap(RegexUtil.namedGroups(m), "letter", "abc", "number", null);
		assertMap(RegexUtil.namedGroups(null));
		assertCollection(RegexUtil.groupNames((Matcher) null));
		assertCollection(RegexUtil.groupNames((Pattern) null));
	}

	@Test
	public void testFound() {
		assertNull(RegexUtil.found(INT_PATTERN, null));
		assertNull(RegexUtil.found(INT_PATTERN, "abc"));
		assertEquals(RegexUtil.found(INT_PATTERN, "abc123de45f6").group(), "123");
	}

	@Test
	public void testMatch() {
		Pattern p0 = Pattern.compile("(abc).*");
		Pattern p1 = Pattern.compile("abc.*");
		assertNull(RegexUtil.match(p0, null));
		assertNull(RegexUtil.match(p0, "ab"));
		assertEquals(RegexUtil.match(p0, "abcdef"), "abc");
		assertEquals(RegexUtil.match(p1, "abcdef"), "abcdef");
	}

	@Test
	public void testFind() {
		assertEquals(RegexUtil.find(LSTRING_PATTERN, "abc123DEF456ghi789JKL"), "abc");
		assertEquals(RegexUtil.find(USTRING_PATTERN, "abc123DEF456ghi789JKL"), "DEF");
		assertEquals(RegexUtil.find(INT_PATTERN, "abc123DEF456ghi789JKL"), "123");
		assertNull(RegexUtil.find(INT_PATTERN, "abcDEFghiJKL"));
	}

	@Test
	public void testFindBoolean() {
		assertTrue(RegexUtil.findBoolean(USTRING_PATTERN, "falseTRUE123"));
		assertFalse(RegexUtil.findBoolean(INT_PATTERN, "abc123DEF456ghi789JKL"));
		assertFalse(RegexUtil.findBoolean(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindByte() {
		assertEquals(RegexUtil.findByte(INT_PATTERN, "abc123DEF456ghi789JKL"), (byte) 123);
		assertThrown(() -> RegexUtil.findByte(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindShort() {
		assertEquals(RegexUtil.findShort(INT_PATTERN, "abc123DEF456ghi789JKL"), (short) 123);
		assertThrown(() -> RegexUtil.findShort(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindInt() {
		assertEquals(RegexUtil.findInt(INT_PATTERN, "abc123DEF456ghi789JKL"), 123);
		assertThrown(() -> RegexUtil.findInt(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindLong() {
		assertEquals(RegexUtil.findLong(INT_PATTERN, "abc123DEF456ghi789JKL"), 123L);
		assertThrown(() -> RegexUtil.findLong(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindFloat() {
		assertEquals(RegexUtil.findFloat(INT_PATTERN, "abc123DEF456ghi789JKL"), 123f);
		assertThrown(() -> RegexUtil.findFloat(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindDouble() {
		assertEquals(RegexUtil.findDouble(INT_PATTERN, "abc123DEF456ghi789JKL"), 123.0);
		assertThrown(() -> RegexUtil.findDouble(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindAll() {
		assertCollection(RegexUtil.findAll(LSTRING_PATTERN, "abc123DEF456ghi789JKL"), "abc", "ghi");
		assertCollection(RegexUtil.findAll(USTRING_PATTERN, "abc123DEF456ghi789JKL"), "DEF", "JKL");
		assertCollection(RegexUtil.findAll(INT_PATTERN, "abc123DEF456ghi789JKL"), "123", "456",
			"789");
	}

	@Test
	public void testFindAllBooleans() {
		assertCollection(RegexUtil.findAllBooleans(LSTRING_PATTERN, "abcDEFtrue123TRUE456True"),
			false, true, false);
		assertCollection(RegexUtil.findAllBooleans(LSTRING_PATTERN, "abc123DEF456ghi789JKL"), false,
			false);
	}

	@Test
	public void testFindAllBytes() {
		assertCollection(RegexUtil.findAllBytes(INT_PATTERN, "abc123DEF127ghi0000JKL"), (byte) 123,
			(byte) 127, (byte) 0);
		assertThrown(() -> RegexUtil.findAllBytes(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindAllShorts() {
		assertCollection(RegexUtil.findAllShorts(INT_PATTERN, "abc123DEF456ghi789JKL"), (short) 123,
			(short) 456, (short) 789);
		assertThrown(() -> RegexUtil.findAllShorts(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindAllInts() {
		assertCollection(RegexUtil.findAllInts(INT_PATTERN, "abc123DEF456ghi789JKL"), 123, 456,
			789);
		assertThrown(() -> RegexUtil.findAllInts(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindAllLongs() {
		assertCollection(RegexUtil.findAllLongs(INT_PATTERN, "abc123DEF456ghi789JKL"), 123L, 456L,
			789L);
		assertThrown(() -> RegexUtil.findAllLongs(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindAllFloats() {
		assertCollection(RegexUtil.findAllFloats(INT_PATTERN, "abc123DEF456ghi789JKL"), 123f, 456f,
			789f);
		assertThrown(() -> RegexUtil.findAllFloats(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindAllDoubles() {
		assertCollection(RegexUtil.findAllDoubles(INT_PATTERN, "abc123DEF456ghi789JKL"), 123.0,
			456.0, 789.0);
		assertThrown(() -> RegexUtil.findAllDoubles(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

}
