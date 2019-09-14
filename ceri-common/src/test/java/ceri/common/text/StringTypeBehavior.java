package ceri.common.text;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StringTypeBehavior {
	private static StringType s;
	private static StringType sb;

	@BeforeClass
	public static void init() {
		s = StringType.of("abcabc");
		sb = StringType.of(new StringBuilder("abcabc"));
	}

	@Test
	public void shouldReturnSubstring() {
		assertThat(s.substring(2), is("cabc"));
		assertThat(s.substring(1, 4), is("bca"));
		assertThat(s.subSequence(1, 4), is("bca"));
		assertThat(sb.substring(2), is("cabc"));
		assertThat(sb.substring(1, 4), is("bca"));
		assertThat(sb.subSequence(1, 4), is("bca"));
	}

	@Test
	public void shouldDetermineIndexOfString() {
		assertThat(s.indexOf("bc"), is(1));
		assertThat(s.indexOf("bc", 1), is(1));
		assertThat(s.indexOf("bc", 2), is(4));
		assertThat(sb.indexOf("bc"), is(1));
		assertThat(sb.indexOf("bc", 1), is(1));
		assertThat(sb.indexOf("bc", 2), is(4));
	}

	@Test
	public void shouldDetermineLength() {
		assertThat(s.length(), is(6));
		assertThat(sb.length(), is(6));
	}

	@Test
	public void shouldDetermineIfEmpty() {
		assertFalse(s.isEmpty());
		assertTrue(StringType.Applicator.STRING.isEmpty(""));
		assertFalse(StringType.Applicator.STRING.isEmpty("abc"));
		assertFalse(sb.isEmpty());
		assertTrue(StringType.Applicator.STRING_BUILDER.isEmpty(new StringBuilder("")));
		assertFalse(StringType.Applicator.STRING_BUILDER.isEmpty(new StringBuilder("abc")));
	}

	@Test
	public void shouldDetermineCharAtIndex() {
		assertThat(s.charAt(4), is('b'));
		assertThat(sb.charAt(4), is('b'));
	}

}
