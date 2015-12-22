package ceri.common.text;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertElements;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.text.RegexUtil;

public class RegexUtilTest {
	private static final Pattern LSTRING_PATTERN = Pattern.compile("([a-z]+)");
	private static final Pattern USTRING_PATTERN = Pattern.compile("([A-Z]+)");
	private static final Pattern INT_PATTERN = Pattern.compile("(\\d+)");
	private static final Pattern MULTI_PATTERN = Pattern.compile("(\\w+)\\W+(\\w+)\\W+(\\w+)");

	@Test
	public void testPrivateConstructor() {
		assertPrivateConstructor(RegexUtil.class);
	}

	@Test
	public void testGroups() {
		assertElements(RegexUtil.groups(MULTI_PATTERN, " ab CD--ef"), "ab", "CD", "ef");
		assertElements(RegexUtil.groups(MULTI_PATTERN, ""));
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
		assertCollection(RegexUtil.findAllBooleans(LSTRING_PATTERN, "abc123DEF456ghi789JKL"),
			false, false);
	}

	@Test
	public void testFindAllBytes() {
		assertCollection(RegexUtil.findAllBytes(INT_PATTERN, "abc123DEF127ghi0000JKL"), (byte) 123,
			(byte) 127, (byte) 0);
		assertException(() -> RegexUtil.findAllBytes(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindAllShorts() {
		assertCollection(RegexUtil.findAllShorts(INT_PATTERN, "abc123DEF456ghi789JKL"),
			(short) 123, (short) 456, (short) 789);
		assertException(() -> RegexUtil.findAllShorts(LSTRING_PATTERN, "abc123DEF456ghi789JKL"));
	}

	@Test
	public void testFindAllInts() {
		assertCollection(RegexUtil.findAllInts(INT_PATTERN, "abc123DEF456ghi789JKL"), 123, 456, 789);
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
