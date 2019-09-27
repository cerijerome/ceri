package ceri.common.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

public class StringTypeBehavior {
	private static final String STRING = "abcabc";
	private static StringType s;
	private static StringType sb;
	private static StringType cs;

	@BeforeClass
	public static void init() {
		s = StringType.of(STRING);
		sb = StringType.of(new StringBuilder(STRING));
		cs = new StringType() {
			@Override
			public int length() {
				return STRING.length();
			}

			@Override
			public char charAt(int index) {
				return STRING.charAt(index);
			}

			@Override
			public CharSequence subSequence(int start, int end) {
				return STRING.subSequence(start, end);
			}
		};
	}

	@Test
	public void shouldReturnWrappedToString() {
		assertThat(s.toString(), is("abcabc"));
		assertThat(sb.toString(), is("abcabc"));
	}

	@Test
	public void shouldDetermineIfRegionMatchesString() {
		assertTrue(s.regionMatches(2, "bcabc", 1, 4));
		assertFalse(s.regionMatches(1, "bcabc", 1, 4));
		assertTrue(sb.regionMatches(2, "bcabc", 1, 4));
		assertFalse(sb.regionMatches(1, "bcabc", 1, 4));
		assertTrue(cs.regionMatches(2, "bcabc", 1, 4));
		assertFalse(cs.regionMatches(1, "bcabc", 1, 4));
	}

	@Test
	public void shouldDetermineIfTypeEqualsString() {
		assertTrue(s.equalsString("abcabc"));
		assertFalse(s.equalsString("AbCaBc"));
		assertTrue(s.equalsIgnoreCase("AbCaBc"));
		assertTrue(sb.equalsString("abcabc"));
		assertFalse(sb.equalsString("AbCaBc"));
		assertTrue(sb.equalsIgnoreCase("AbCaBc"));
		assertTrue(cs.equalsString("abcabc"));
		assertFalse(cs.equalsString("AbCaBc"));
		assertTrue(cs.equalsIgnoreCase("AbCaBc"));
	}

	@Test
	public void shouldDetermineIfTypeStartsWithString() {
		assertTrue(s.startsWith("abc"));
		assertTrue(s.startsWith("cab", 2));
		assertFalse(s.startsWith("AbC"));
		assertTrue(s.startsWithIgnoreCase("AbC"));
		assertTrue(s.startsWithIgnoreCase("cAB", 2));
		assertTrue(sb.startsWith("abc"));
		assertTrue(sb.startsWith("cab", 2));
		assertFalse(sb.startsWith("AbC"));
		assertTrue(sb.startsWithIgnoreCase("AbC"));
		assertTrue(sb.startsWithIgnoreCase("cAB", 2));
		assertTrue(cs.startsWith("abc"));
		assertTrue(cs.startsWith("cab", 2));
		assertFalse(cs.startsWith("AbC"));
		assertTrue(cs.startsWithIgnoreCase("AbC"));
		assertTrue(cs.startsWithIgnoreCase("cAB", 2));
	}

	@Test
	public void shouldReturnSubstring() {
		assertThat(s.substring(2), is("cabc"));
		assertThat(s.substring(1, 4), is("bca"));
		assertThat(s.subSequence(1, 4), is("bca"));
		assertThat(sb.substring(2), is("cabc"));
		assertThat(sb.substring(1, 4), is("bca"));
		assertThat(sb.subSequence(1, 4), is("bca"));
		assertThat(cs.substring(2), is("cabc"));
		assertThat(cs.substring(1, 4), is("bca"));
		assertThat(cs.subSequence(1, 4), is("bca"));
	}

	@Test
	public void shouldDetermineIndexOfString() {
		assertThat(s.indexOf("bc"), is(1));
		assertThat(s.indexOf("bc", 1), is(1));
		assertThat(s.indexOf("bc", 2), is(4));
		assertThat(sb.indexOf("bc"), is(1));
		assertThat(sb.indexOf("bc", 1), is(1));
		assertThat(sb.indexOf("bc", 2), is(4));
		assertThat(cs.indexOf("bc"), is(1));
		assertThat(cs.indexOf("bc", 1), is(1));
		assertThat(cs.indexOf("bc", 2), is(4));
	}

	@Test
	public void shouldDetermineLength() {
		assertThat(s.length(), is(6));
		assertThat(sb.length(), is(6));
		assertThat(cs.length(), is(6));
	}

	@Test
	public void shouldDetermineIfEmpty() {
		assertFalse(s.isEmpty());
		assertFalse(sb.isEmpty());
		assertFalse(cs.isEmpty());
	}

	@Test
	public void shouldDetermineCharAtIndex() {
		assertThat(s.charAt(4), is('b'));
		assertThat(sb.charAt(4), is('b'));
		assertThat(cs.charAt(4), is('b'));
	}

}
