package ceri.common.util;

import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.regex.Pattern;
import org.junit.Test;

public class RegexUtilTest {
	private static final Pattern LSTRING_PATTERN = Pattern.compile("([a-z]+)");
	private static final Pattern USTRING_PATTERN = Pattern.compile("([A-Z]+)");
	private static final Pattern INT_PATTERN = Pattern.compile("(\\d+)");

	@Test
	public void testFind() {
		assertThat(RegexUtil.find(LSTRING_PATTERN, "abc123DEF456ghi789JKL"), is("abc"));
		assertThat(RegexUtil.find(USTRING_PATTERN, "abc123DEF456ghi789JKL"), is("DEF"));
		assertThat(RegexUtil.find(INT_PATTERN, "abc123DEF456ghi789JKL"), is("123"));
	}

	@Test
	public void testFindInt() {
		assertThat(RegexUtil.findInt(INT_PATTERN, "abc123DEF456ghi789JKL"), is(123));
	}

	@Test
	public void testFindAll() {
		assertCollection(RegexUtil.findAll(LSTRING_PATTERN, "abc123DEF456ghi789JKL"), "abc", "ghi");
		assertCollection(RegexUtil.findAll(USTRING_PATTERN, "abc123DEF456ghi789JKL"), "DEF", "JKL");
		assertCollection(RegexUtil.findAll(INT_PATTERN, "abc123DEF456ghi789JKL"), "123", "456",
			"789");
	}

	@Test
	public void testFindAllInts() {
		assertCollection(RegexUtil.findAllInts(INT_PATTERN, "abc123DEF456ghi789JKL"), 123, 456, 789);
	}

}
