package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.text.NonMatchResultBehavior.assertNonMatchResult;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.Test;

public class NonMatcherBehavior {

	@Test
	public void shouldReplaceAllMatches() {
		NonMatcher m = nonMatcher("[a-c]+", "abc");
		assertEquals(m.replaceAll("X"), "abc");
		assertEquals(m.replaceAll(r -> String.valueOf(r.end() - r.start())), "abc");
		m = nonMatcher("[a-c]+", "AaBBbbCCCcccDDDDdddd");
		assertEquals(m.replaceAll("X"), "XaXbbXcccX");
		assertEquals(m.replaceAll(r -> String.valueOf(r.end() - r.start())), "1a2bb3ccc8");
	}

	@Test
	public void shouldReplaceFirstMatch() {
		NonMatcher m = nonMatcher("[a-c]+", "abc");
		assertEquals(m.replaceFirst("X"), "abc");
		assertEquals(m.replaceFirst(r -> String.valueOf(r.end() - r.start())), "abc");
		m = nonMatcher("[a-c]+", "abcABCabABaA");
		assertEquals(m.replaceFirst("X"), "abcXabABaA");
		assertEquals(m.replaceFirst(r -> String.valueOf(r.end() - r.start())), "abc3abABaA");
	}

	@Test
	public void shouldFindNonMatchingText() {
		NonMatcher m = nonMatcher("[a-c]", "AaBbCcDd");
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
		NonMatcher m = nonMatcher("[a-c]", "AaBbCcDd");
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
		NonMatcher m = nonMatcher("[a-c]", "aBbbCccDdc");
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
		NonMatcher m = nonMatcher("[a-c]", "aBbbCccDdc");
		assertTrue(m.find(2));
		assertNonMatcher(m, "C", 4, 5);
		assertTrue(m.find());
		assertNonMatcher(m, "Dd", 7, 9);
		assertFalse(m.find());
	}

	@Test
	public void shouldNotFindFromEndPosition() {
		NonMatcher m = nonMatcher("[a-c]", "abcDEF");
		assertFalse(m.find(6));
		m = nonMatcher("[a-c]", "abc");
		assertFalse(m.find(3));
		m = nonMatcher("[a-c]", "abc");
		assertFalse(m.find(2));
	}

	@Test
	public void shouldFailWhenFindingFromPositionOutOfRange() {
		NonMatcher m = nonMatcher("[a-c]", "abcDEF");
		assertThrown(() -> m.find(-1));
		assertThrown(() -> m.find(7));
	}

	@Test
	public void shouldIgnoreRegionWhenFindingFromPosition() {
		NonMatcher m = nonMatcher("[a-c]", "ABCabc").region(2, 6);
		assertTrue(m.find(1));
		assertNonMatcher(m, "BC", 1, 3);
	}

	@Test
	public void shouldMatchIffPatternMatches() {
		NonMatcher m = nonMatcher("[a-c]+", "abc");
		assertFalse(m.matches());
		m = nonMatcher("[a-c]", "abcDEF");
		assertTrue(m.matches());
		assertNonMatcher(m, "abcDEF", 0, 6);
	}

	@Test
	public void shouldNotFindAfterMatches() {
		NonMatcher m = nonMatcher("[a-c]+", "abc");
		assertFalse(m.matches());
		assertFalse(m.find());
		m = nonMatcher("[a-c]+", "Aabc");
		assertTrue(m.matches());
		assertFalse(m.find());
	}

	@Test
	public void shouldFailToReadMatchIfNoMatch() {
		NonMatcher m = nonMatcher("[a-c]+", "ABC");
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
		NonMatcher m = nonMatcher("[a-c]+", "abBCcDd");
		assertTrue(m.find());
		assertNonMatcher(m, "BC", 2, 4);
		m.reset();
		assertTrue(m.find());
		assertNonMatcher(m, "BC", 2, 4);
	}

	@Test
	public void shouldResetStateWithNewText() {
		NonMatcher m = nonMatcher("[a-c]+", "abBCcDd");
		assertTrue(m.find());
		assertNonMatcher(m, "BC", 2, 4);
		m.reset("aBbCcDd");
		assertTrue(m.find());
		assertNonMatcher(m, "B", 1, 2);
	}

	@Test
	public void shouldConvertToResult() {
		NonMatcher m = nonMatcher("[a-c]+", "abBCcDd");
		NonMatchResult r0 = m.toResult();
		assertThrown(() -> r0.group());
		m.find();
		assertEquals(m.toResult().group(), "BC");
	}

	@Test
	public void shouldStreamResults() {
		NonMatcher m = nonMatcher("[a-c]+", "abBCcDd");
		Stream<NonMatchResult> results = m.results();
		Iterator<NonMatchResult> i = results.iterator();
		assertNonMatchResult(i.next(), "BC", 2, 4);
		assertNonMatchResult(i.next(), "Dd", 5, 7);
		assertThrown(NoSuchElementException.class, () -> i.next());
		assertFalse(i.hasNext());
	}

	@Test
	public void shouldReturnStringRepresentationOfState() {
		NonMatcher m0 = nonMatcher("[a-c]+", "ABC").region(1, 2);
		NonMatcher m1 = nonMatcher("[a-c]+", "ABC").region(1, 2);
		NonMatcher m2 = nonMatcher("[a-c]+", "ABC");
		assertEquals(m0.toString(), m1.toString());
		assertNotEquals(m0.toString(), m2.toString());
		assertTrue(m0.find());
		assertNotEquals(m0.toString(), m1.toString());
		assertTrue(m1.find());
		assertEquals(m0.toString(), m1.toString());
	}

	public static void assertNonMatcher(NonMatcher m, String group, int start, int end) {
		assertEquals(m.group(), group, "group");
		assertEquals(m.start(), start, "start");
		assertEquals(m.end(), end, "end");
	}

	private static NonMatcher nonMatcher(String pattern, String text) {
		return NonMatcher.of(Pattern.compile(pattern), text);
	}

}
