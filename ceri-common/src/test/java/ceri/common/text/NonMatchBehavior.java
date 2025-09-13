package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import org.junit.Test;

public class NonMatchBehavior {

	@Test
	public void testRemoveAll() {
		assertString(NonMatch.removeAll(Regex.EMPTY, ""), "");
		assertString(NonMatch.removeAll(Pattern.compile("[a-c]"), "AaBbCcDd"), "abc");
		assertString(NonMatch.removeAll(Pattern.compile("[a-c]"), "abc"), "abc");
		assertString(NonMatch.removeAll(Pattern.compile("[a-c]"), "def"), "");
		assertString(NonMatch.removeAll(Pattern.compile("^"), "abc"), "");
		assertString(NonMatch.removeAll(Pattern.compile("$"), "abc"), "");
	}

	@Test
	public void testReplaceAll() {
		assertString(NonMatch.replaceAll(null, "abc", "d"), "abc");
		assertString(NonMatch.replaceAll(Regex.EMPTY, null, "d"), "");
		assertString(NonMatch.replaceAll(Regex.EMPTY, "abc", null), "");
		assertString(NonMatch.replaceAll(Regex.EMPTY, "", ""), "");
		assertString(NonMatch.replaceAll(Pattern.compile("[a-c]"), "AaBbCcDd", "x"), "xaxbxcx");
		assertString(NonMatch.replaceAll(Pattern.compile("[a-c]"), "abc", "x"), "abc");
		assertString(NonMatch.replaceAll(Pattern.compile("[a-c]"), "def", "x"), "x");
		assertString(NonMatch.replaceAll(Pattern.compile("[a-c]"), "def", ""), "");
		assertString(NonMatch.replaceAll(Pattern.compile("[a-c]"), "def", null), "");
		assertString(NonMatch.replaceAll(Pattern.compile("^"), "abc", "x"), "x");
		assertString(NonMatch.replaceAll(Pattern.compile("$"), "abc", "x"), "x");
	}

	@Test
	public void testAppendAll() {
		assertString(NonMatch.appendAll(null, "abc", (b, _) -> b.append('x')), "abc");
		assertString(NonMatch.appendAll(Regex.EMPTY, null, (b, _) -> b.append('x')), "");
		assertString(NonMatch.appendAll(Regex.EMPTY, "abc", null), "");
		assertString(NonMatch.appendAll(Pattern.compile("[a-c]+"), "abcdefbca",
			(b, m) -> b.append(m.group().toUpperCase())), "abcDEFbca");
	}

	@Test
	public void shouldProvideToResultString() {
		var m = NonMatch.of(Pattern.compile("[a-c]"), "abcDEF");
		assertFalse(m.toResult().toString().isEmpty());
		assertTrue(m.find());
		assertFalse(m.toResult().toString().isEmpty());
	}

	@Test
	public void shouldReplaceAllMatches() {
		var m = nonMatcher("[a-c]+", "abc");
		assertEquals(m.replaceAll("X"), "abc");
		assertEquals(m.replaceAll(r -> String.valueOf(r.end() - r.start())), "abc");
		m = nonMatcher("[a-c]+", "AaBBbbCCCcccDDDDdddd");
		assertEquals(m.replaceAll("X"), "XaXbbXcccX");
		assertEquals(m.replaceAll(r -> String.valueOf(r.end() - r.start())), "1a2bb3ccc8");
	}

	@Test
	public void shouldReplaceFirstMatch() {
		var m = nonMatcher("[a-c]+", "abc");
		assertEquals(m.replaceFirst("X"), "abc");
		assertEquals(m.replaceFirst(r -> String.valueOf(r.end() - r.start())), "abc");
		m = nonMatcher("[a-c]+", "abcABCabABaA");
		assertEquals(m.replaceFirst("X"), "abcXabABaA");
		assertEquals(m.replaceFirst(r -> String.valueOf(r.end() - r.start())), "abc3abABaA");
	}

	@Test
	public void shouldFindNonMatchingText() {
		var m = nonMatcher("[a-c]", "AaBbCcDd");
		assertTrue(m.find());
		assertNonMatcher(m, "A", 0, 1);
		assertTrue(m.find());
		assertNonMatcher(m, "B", 2, 3);
		assertTrue(m.find());
		assertNonMatcher(m, "C", 4, 5);
		assertTrue(m.find());
		assertNonMatcher(m, "Dd", 6, 8);
		assertFalse(m.find());
	}

	@Test
	public void shouldFindNonMatchingTextFromPosition() {
		var m = nonMatcher("[a-c]", "AaBbCcDd");
		assertTrue(m.find(2));
		assertNonMatcher(m, "B", 2, 3);
		assertTrue(m.find());
		assertNonMatcher(m, "C", 4, 5);
		assertTrue(m.find());
		assertNonMatcher(m, "Dd", 6, 8);
		assertFalse(m.find());
	}

	@Test
	public void shouldSkipEmptyMatches() {
		var m = nonMatcher("[a-c]", "aBbbCccDdc");
		assertTrue(m.find());
		assertNonMatcher(m, "B", 1, 2);
		assertTrue(m.find());
		assertNonMatcher(m, "C", 4, 5);
		assertTrue(m.find());
		assertNonMatcher(m, "Dd", 7, 9);
		assertFalse(m.find());
	}

	@Test
	public void shouldSkipEmptyMatchesFromPosition() {
		var m = nonMatcher("[a-c]", "aBbbCccDdc");
		assertTrue(m.find(2));
		assertNonMatcher(m, "C", 4, 5);
		assertTrue(m.find());
		assertNonMatcher(m, "Dd", 7, 9);
		assertFalse(m.find());
	}

	@Test
	public void shouldNotFindFromEndPosition() {
		var m = nonMatcher("[a-c]", "abcDEF");
		assertFalse(m.find(6));
		m = nonMatcher("[a-c]", "abc");
		assertFalse(m.find(3));
		m = nonMatcher("[a-c]", "abc");
		assertFalse(m.find(2));
	}

	@Test
	public void shouldFailWhenFindingFromPositionOutOfRange() {
		var m = nonMatcher("[a-c]", "abcDEF");
		assertThrown(() -> m.find(-1));
		assertThrown(() -> m.find(7));
	}

	@Test
	public void shouldIgnoreRegionWhenFindingFromPosition() {
		var m = nonMatcher("[a-c]", "ABCabc").region(2, 6);
		assertTrue(m.find(1));
		assertNonMatcher(m, "BC", 1, 3);
	}

	@Test
	public void shouldMatchIffPatternMatches() {
		var m = nonMatcher("[a-c]+", "abc");
		assertFalse(m.matches());
		m = nonMatcher("[a-c]", "abcDEF");
		assertTrue(m.matches());
		assertNonMatcher(m, "abcDEF", 0, 6);
	}

	@Test
	public void shouldNotFindAfterMatches() {
		var m = nonMatcher("[a-c]+", "abc");
		assertFalse(m.matches());
		assertFalse(m.find());
		m = nonMatcher("[a-c]+", "Aabc");
		assertTrue(m.matches());
		assertFalse(m.find());
	}

	@Test
	public void shouldFailToReadMatchIfNoMatch() {
		var m = nonMatcher("[a-c]+", "ABC");
		assertThrown(() -> m.group());
		assertThrown(() -> m.start());
		assertThrown(() -> m.end());
		assertTrue(m.find());
		assertNonMatcher(m, "ABC", 0, 3);
		assertFalse(m.find());
		assertThrown(() -> m.group());
		assertThrown(() -> m.start());
		assertThrown(() -> m.end());
	}

	@Test
	public void shouldResetState() {
		var m = nonMatcher("[a-c]+", "abBCcDd");
		assertTrue(m.find());
		assertNonMatcher(m, "BC", 2, 4);
		m.reset();
		assertTrue(m.find());
		assertNonMatcher(m, "BC", 2, 4);
	}

	@Test
	public void shouldResetStateWithNewText() {
		var m = nonMatcher("[a-c]+", "abBCcDd");
		assertTrue(m.find());
		assertNonMatcher(m, "BC", 2, 4);
		m.reset("aBbCcDd");
		assertTrue(m.find());
		assertNonMatcher(m, "B", 1, 2);
	}

	@Test
	public void shouldConvertToResult() {
		var m = nonMatcher("[a-c]+", "abBCcDd");
		var r0 = m.toResult();
		assertThrown(() -> r0.group());
		m.find();
		assertEquals(m.toResult().group(), "BC");
	}

	@Test
	public void shouldStreamResults() {
		var i = nonMatcher("[a-c]+", "abBCcDd").results().iterator();
		assertNonMatchResult(i.next(), "BC", 2, 4);
		assertNonMatchResult(i.next(), "Dd", 5, 7);
		assertThrown(NoSuchElementException.class, () -> i.next());
		assertFalse(i.hasNext());
	}

	@Test
	public void shouldReturnStringRepresentationOfState() {
		var m0 = nonMatcher("[a-c]+", "ABC").region(1, 2);
		var m1 = nonMatcher("[a-c]+", "ABC").region(1, 2);
		var m2 = nonMatcher("[a-c]+", "ABC");
		assertEquals(m0.toString(), m1.toString());
		assertNotEquals(m0.toString(), m2.toString());
		assertTrue(m0.find());
		assertNotEquals(m0.toString(), m1.toString());
		assertTrue(m1.find());
		assertEquals(m0.toString(), m1.toString());
	}

	private static void assertNonMatcher(NonMatch.Matcher m, String group, int start, int end) {
		assertEquals(m.group(), group, "group");
		assertEquals(m.start(), start, "start");
		assertEquals(m.end(), end, "end");
	}

	private static void assertNonMatchResult(NonMatch.Result r, String group, int start, int end) {
		assertEquals(r.group(), group, "group");
		assertEquals(r.start(), start, "start");
		assertEquals(r.end(), end, "end");
	}

	private static NonMatch.Matcher nonMatcher(String pattern, String text) {
		return NonMatch.of(Pattern.compile(pattern), text);
	}
}
