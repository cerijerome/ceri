package ceri.common.text;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertMap;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

public class RegexUtilTest {
	private static final Pattern LSTRING_PATTERN = Pattern.compile("([a-z]+)");
	private static final Pattern USTRING_PATTERN = Pattern.compile("([A-Z]+)");
	private static final Pattern INT_PATTERN = Pattern.compile("(\\d+)");
	private static final Pattern MULTI_PATTERN = Pattern.compile("(\\w+)\\W+(\\w+)\\W+([\\w\\.]+)");

	@Test
	public void testPrivateConstructor() {
		assertPrivateConstructor(RegexUtil.class);
	}

	@Test
	public void testFinder() {
		assertThat(RegexUtil.finder(INT_PATTERN).test(null), is(false));
		assertThat(RegexUtil.finder(INT_PATTERN).test("abc123def456"), is(true));
		assertThat(RegexUtil.finder(LSTRING_PATTERN).test("abc123def456"), is(true));
		assertThat(RegexUtil.finder(USTRING_PATTERN).test("abc123def456"), is(false));
	}

	@Test
	public void testMatcher() {
		assertThat(RegexUtil.matcher(INT_PATTERN).test(null), is(false));
		assertThat(RegexUtil.matcher(INT_PATTERN).test("123"), is(true));
		assertThat(RegexUtil.matcher(INT_PATTERN).test("123def456"), is(false));
		assertThat(RegexUtil.matcher(LSTRING_PATTERN).test("abc"), is(true));
		assertThat(RegexUtil.matcher(USTRING_PATTERN).test("abc"), is(false));
	}

	@Test
	public void testForEach() {
		List<String> list = new ArrayList<>();
		for (MatchResult result : RegexUtil.forEach(INT_PATTERN, "123abcA1B2C3"))
			list.add(result.group());
		assertIterable(list, "123", "1", "2", "3");
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
	public void testTypedGroups() {
		Matcher m = MULTI_PATTERN.matcher("123 true 4.5");
		assertTrue(m.find());
		assertThat(RegexUtil.booleanGroup(m, 2), is(true));
		assertThat(RegexUtil.byteGroup(m, 1), is((byte) 123));
		assertThat(RegexUtil.shortGroup(m, 1), is((short) 123));
		assertThat(RegexUtil.intGroup(m, 1), is(123));
		assertThat(RegexUtil.longGroup(m, 1), is(123L));
		assertThat(RegexUtil.floatGroup(m, 3), is(4.5f));
		assertThat(RegexUtil.doubleGroup(m, 3), is(4.5));
	}

	@Test
	public void testNamedGroup() {
		Pattern named = Pattern.compile("(?:(?<letter>[a-z]+)|(?<number>[0-9]+))");
		Matcher m = named.matcher("123abc45de6f");
		assertTrue(m.find());
		assertNull(RegexUtil.namedGroup(m, "test"));
		assertNull(RegexUtil.namedGroup(m, "letter"));
		assertThat(RegexUtil.namedGroup(m, "number"), is("123"));
		assertTrue(m.find());
		assertThat(RegexUtil.namedGroup(m, "letter"), is("abc"));
		assertNull(RegexUtil.namedGroup(m, "number"));
		assertTrue(m.find());
		assertNull(RegexUtil.namedGroup(m, "letter"));
		assertThat(RegexUtil.namedGroup(m, "number"), is("45"));
		assertTrue(m.find());
		assertThat(RegexUtil.namedGroup(m, "letter"), is("de"));
		assertNull(RegexUtil.namedGroup(m, "number"));
		assertTrue(m.find());
		assertNull(RegexUtil.namedGroup(m, "letter"));
		assertThat(RegexUtil.namedGroup(m, "number"), is("6"));
		assertTrue(m.find());
		assertThat(RegexUtil.namedGroup(m, "letter"), is("f"));
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
		assertThat(RegexUtil.found(INT_PATTERN, "abc123de45f6").group(), is("123"));
	}

	@Test
	public void testMatch() {
		Pattern p0 = Pattern.compile("(abc).*");
		Pattern p1 = Pattern.compile("abc.*");
		assertNull(RegexUtil.match(p0, null));
		assertNull(RegexUtil.match(p0, "ab"));
		assertThat(RegexUtil.match(p0, "abcdef"), is("abc"));
		assertThat(RegexUtil.match(p1, "abcdef"), is("abcdef"));
	}

	@Test
	public void testFind() {
		assertThat(RegexUtil.find(LSTRING_PATTERN, "abc123DEF456ghi789JKL"), is("abc"));
		assertThat(RegexUtil.find(USTRING_PATTERN, "abc123DEF456ghi789JKL"), is("DEF"));
		assertThat(RegexUtil.find(INT_PATTERN, "abc123DEF456ghi789JKL"), is("123"));
		assertNull(RegexUtil.find(INT_PATTERN, "abcDEFghiJKL"));
	}

	@Test
	public void testFindBoolean() {
		assertThat(RegexUtil.findBoolean(USTRING_PATTERN, "falseTRUE123"), is(true));
		assertThat(RegexUtil.findBoolean(INT_PATTERN, "abc123DEF456ghi789JKL"), is(false));
		assertThat(RegexUtil.findBoolean(LSTRING_PATTERN, "abc123DEF456ghi789JKL"), is(false));
	}

	@Test
	public void testFindByte() {
		assertThat(RegexUtil.findByte(INT_PATTERN, "abc123DEF456ghi789JKL"), is((byte) 123));
		assertException(() -> RegexUtil.findByte(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindShort() {
		assertThat(RegexUtil.findShort(INT_PATTERN, "abc123DEF456ghi789JKL"), is((short) 123));
		assertException(() -> RegexUtil.findShort(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindInt() {
		assertThat(RegexUtil.findInt(INT_PATTERN, "abc123DEF456ghi789JKL"), is(123));
		assertException(() -> RegexUtil.findInt(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindLong() {
		assertThat(RegexUtil.findLong(INT_PATTERN, "abc123DEF456ghi789JKL"), is(123L));
		assertException(() -> RegexUtil.findLong(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindFloat() {
		assertThat(RegexUtil.findFloat(INT_PATTERN, "abc123DEF456ghi789JKL"), is(123f));
		assertException(() -> RegexUtil.findFloat(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindDouble() {
		assertThat(RegexUtil.findDouble(INT_PATTERN, "abc123DEF456ghi789JKL"), is(123.0));
		assertException(() -> RegexUtil.findDouble(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
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
		assertException(() -> RegexUtil.findAllBytes(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindAllShorts() {
		assertCollection(RegexUtil.findAllShorts(INT_PATTERN, "abc123DEF456ghi789JKL"), (short) 123,
			(short) 456, (short) 789);
		assertException(() -> RegexUtil.findAllShorts(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindAllInts() {
		assertCollection(RegexUtil.findAllInts(INT_PATTERN, "abc123DEF456ghi789JKL"), 123, 456,
			789);
		assertException(() -> RegexUtil.findAllInts(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindAllLongs() {
		assertCollection(RegexUtil.findAllLongs(INT_PATTERN, "abc123DEF456ghi789JKL"), 123L, 456L,
			789L);
		assertException(() -> RegexUtil.findAllLongs(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindAllFloats() {
		assertCollection(RegexUtil.findAllFloats(INT_PATTERN, "abc123DEF456ghi789JKL"), 123f, 456f,
			789f);
		assertException(() -> RegexUtil.findAllFloats(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindAllDoubles() {
		assertCollection(RegexUtil.findAllDoubles(INT_PATTERN, "abc123DEF456ghi789JKL"), 123.0,
			456.0, 789.0);
		assertException(() -> RegexUtil.findAllDoubles(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

}
