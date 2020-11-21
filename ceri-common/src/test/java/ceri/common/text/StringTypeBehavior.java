package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;

public class StringTypeBehavior {
	private static final String STRING = "abcabc";
	private static final StringType s = StringType.of(STRING);
	private static final StringType sb = StringType.of(new StringBuilder(STRING));
	private static final StringType cs = custom();

	private static StringType custom() {
		return new StringType() {
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
		assertEquals(s.toString(), "abcabc");
		assertEquals(sb.toString(), "abcabc");
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
		assertEquals(s.substring(2), "cabc");
		assertEquals(s.substring(1, 4), "bca");
		assertEquals(s.subSequence(1, 4), "bca");
		assertEquals(sb.substring(2), "cabc");
		assertEquals(sb.substring(1, 4), "bca");
		assertEquals(sb.subSequence(1, 4), "bca");
		assertEquals(cs.substring(2), "cabc");
		assertEquals(cs.substring(1, 4), "bca");
		assertEquals(cs.subSequence(1, 4), "bca");
	}

	@Test
	public void shouldDetermineIndexOfString() {
		assertEquals(s.indexOf("bc"), 1);
		assertEquals(s.indexOf("bc", 1), 1);
		assertEquals(s.indexOf("bc", 2), 4);
		assertEquals(sb.indexOf("bc"), 1);
		assertEquals(sb.indexOf("bc", 1), 1);
		assertEquals(sb.indexOf("bc", 2), 4);
		assertEquals(cs.indexOf("bc"), 1);
		assertEquals(cs.indexOf("bc", 1), 1);
		assertEquals(cs.indexOf("bc", 2), 4);
	}

	@Test
	public void shouldDetermineIfUnfoundIndex() {
		assertEquals(s.indexOf("bd"), -1);
		assertEquals(sb.indexOf("bd"), -1);
		assertEquals(cs.indexOf("bd"), -1);
	}

	@Test
	public void shouldDetermineLength() {
		assertEquals(s.length(), 6);
		assertEquals(sb.length(), 6);
		assertEquals(cs.length(), 6);
	}

	@Test
	public void shouldDetermineIfEmpty() {
		assertFalse(s.isEmpty());
		assertFalse(sb.isEmpty());
		assertFalse(cs.isEmpty());
		assertTrue(StringType.of("").isEmpty());
	}

	@Test
	public void shouldDetermineCharAtIndex() {
		assertEquals(s.charAt(4), 'b');
		assertEquals(sb.charAt(4), 'b');
		assertEquals(cs.charAt(4), 'b');
	}

}
