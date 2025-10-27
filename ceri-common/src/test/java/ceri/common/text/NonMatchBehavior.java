package ceri.common.text;

import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.test.Assert;

public class NonMatchBehavior {

	@Test
	public void testRemoveAll() {
		Assert.string(NonMatch.removeAll(Regex.EMPTY, ""), "");
		Assert.string(NonMatch.removeAll(Pattern.compile("[a-c]"), "AaBbCcDd"), "abc");
		Assert.string(NonMatch.removeAll(Pattern.compile("[a-c]"), "abc"), "abc");
		Assert.string(NonMatch.removeAll(Pattern.compile("[a-c]"), "def"), "");
		Assert.string(NonMatch.removeAll(Pattern.compile("^"), "abc"), "");
		Assert.string(NonMatch.removeAll(Pattern.compile("$"), "abc"), "");
	}

	@Test
	public void testReplaceAll() {
		Assert.string(NonMatch.replaceAll(null, "abc", "d"), "abc");
		Assert.string(NonMatch.replaceAll(Regex.EMPTY, null, "d"), "");
		Assert.string(NonMatch.replaceAll(Regex.EMPTY, "abc", null), "");
		Assert.string(NonMatch.replaceAll(Regex.EMPTY, "", ""), "");
		Assert.string(NonMatch.replaceAll(Pattern.compile("[a-c]"), "AaBbCcDd", "x"), "xaxbxcx");
		Assert.string(NonMatch.replaceAll(Pattern.compile("[a-c]"), "abc", "x"), "abc");
		Assert.string(NonMatch.replaceAll(Pattern.compile("[a-c]"), "def", "x"), "x");
		Assert.string(NonMatch.replaceAll(Pattern.compile("[a-c]"), "def", ""), "");
		Assert.string(NonMatch.replaceAll(Pattern.compile("[a-c]"), "def", null), "");
		Assert.string(NonMatch.replaceAll(Pattern.compile("^"), "abc", "x"), "x");
		Assert.string(NonMatch.replaceAll(Pattern.compile("$"), "abc", "x"), "x");
		Assert.string(NonMatch.replaceAll(Pattern.compile("$"), "abc", "x"), "x");
		Assert.string(NonMatch.replaceAll(b("_"), Pattern.compile("$"), "abc", "x"), "_x");
	}

	@Test
	public void testAppendAll() {
		Assert.string(NonMatch.appendAll(null, "abc", (b, _) -> b.append('x')), "abc");
		Assert.string(NonMatch.appendAll(Regex.EMPTY, null, (b, _) -> b.append('x')), "");
		Assert.string(NonMatch.appendAll(Regex.EMPTY, "abc", null), "");
		Assert.string(NonMatch.appendAll(Pattern.compile("[a-c]+"), "abcdefbca",
			(b, m) -> b.append(m.group().toUpperCase())), "abcDEFbca");
		Assert.string(NonMatch.appendAll(null, Regex.EMPTY, "abc", null), null);
		Assert.string(NonMatch.appendAll(b("_"), Regex.EMPTY, "abc", null), "_");
	}

	@Test
	public void testOf() {
		Assert.equal(NonMatch.of(null, ""), null);
	}

	@Test
	public void shouldProvideToResultString() {
		var m = NonMatch.of(Pattern.compile("[a-c]"), "abcDEF");
		Assert.no(m.toResult().toString().isEmpty());
		Assert.yes(m.find());
		Assert.no(m.toResult().toString().isEmpty());
	}

	@Test
	public void shouldReplaceAllMatches() {
		var m = nonMatcher("[a-c]+", "abc");
		Assert.equal(m.replaceAll("X"), "abc");
		Assert.equal(m.replaceAll(r -> String.valueOf(r.end() - r.start())), "abc");
		m = nonMatcher("[a-c]+", "AaBBbbCCCcccDDDDdddd");
		Assert.equal(m.replaceAll("X"), "XaXbbXcccX");
		Assert.equal(m.replaceAll(r -> String.valueOf(r.end() - r.start())), "1a2bb3ccc8");
	}

	@Test
	public void shouldReplaceFirstMatch() {
		var m = nonMatcher("[a-c]+", "abc");
		Assert.equal(m.replaceFirst("X"), "abc");
		Assert.equal(m.replaceFirst(r -> String.valueOf(r.end() - r.start())), "abc");
		m = nonMatcher("[a-c]+", "abcABCabABaA");
		Assert.equal(m.replaceFirst("X"), "abcXabABaA");
		Assert.equal(m.replaceFirst(r -> String.valueOf(r.end() - r.start())), "abc3abABaA");
	}

	@Test
	public void shouldFindNonMatchingText() {
		var m = nonMatcher("[a-c]", "AaBbCcDd");
		Assert.yes(m.find());
		assertNonMatcher(m, "A", 0, 1);
		Assert.yes(m.find());
		assertNonMatcher(m, "B", 2, 3);
		Assert.yes(m.find());
		assertNonMatcher(m, "C", 4, 5);
		Assert.yes(m.find());
		assertNonMatcher(m, "Dd", 6, 8);
		Assert.no(m.find());
	}

	@Test
	public void shouldFindNonMatchingTextFromPosition() {
		var m = nonMatcher("[a-c]", "AaBbCcDd");
		Assert.yes(m.find(2));
		assertNonMatcher(m, "B", 2, 3);
		Assert.yes(m.find());
		assertNonMatcher(m, "C", 4, 5);
		Assert.yes(m.find());
		assertNonMatcher(m, "Dd", 6, 8);
		Assert.no(m.find());
	}

	@Test
	public void shouldSkipEmptyMatches() {
		var m = nonMatcher("[a-c]", "aBbbCccDdc");
		Assert.yes(m.find());
		assertNonMatcher(m, "B", 1, 2);
		Assert.yes(m.find());
		assertNonMatcher(m, "C", 4, 5);
		Assert.yes(m.find());
		assertNonMatcher(m, "Dd", 7, 9);
		Assert.no(m.find());
	}

	@Test
	public void shouldSkipEmptyMatchesFromPosition() {
		var m = nonMatcher("[a-c]", "aBbbCccDdc");
		Assert.yes(m.find(2));
		assertNonMatcher(m, "C", 4, 5);
		Assert.yes(m.find());
		assertNonMatcher(m, "Dd", 7, 9);
		Assert.no(m.find());
	}

	@Test
	public void shouldNotFindFromEndPosition() {
		var m = nonMatcher("[a-c]", "abcDEF");
		Assert.no(m.find(6));
		m = nonMatcher("[a-c]", "abc");
		Assert.no(m.find(3));
		m = nonMatcher("[a-c]", "abc");
		Assert.no(m.find(2));
	}

	@Test
	public void shouldFailWhenFindingFromPositionOutOfRange() {
		var m = nonMatcher("[a-c]", "abcDEF");
		Assert.thrown(() -> m.find(-1));
		Assert.thrown(() -> m.find(7));
	}

	@Test
	public void shouldIgnoreRegionWhenFindingFromPosition() {
		var m = nonMatcher("[a-c]", "ABCabc").region(2, 6);
		Assert.yes(m.find(1));
		assertNonMatcher(m, "BC", 1, 3);
	}

	@Test
	public void shouldMatchIffPatternMatches() {
		var m = nonMatcher("[a-c]+", "abc");
		Assert.no(m.matches());
		m = nonMatcher("[a-c]", "abcDEF");
		Assert.yes(m.matches());
		assertNonMatcher(m, "abcDEF", 0, 6);
	}

	@Test
	public void shouldNotFindAfterMatches() {
		var m = nonMatcher("[a-c]+", "abc");
		Assert.no(m.matches());
		Assert.no(m.find());
		m = nonMatcher("[a-c]+", "Aabc");
		Assert.yes(m.matches());
		Assert.no(m.find());
	}

	@Test
	public void shouldFailToReadMatchIfNoMatch() {
		var m = nonMatcher("[a-c]+", "ABC");
		Assert.thrown(() -> m.group());
		Assert.thrown(() -> m.start());
		Assert.thrown(() -> m.end());
		Assert.yes(m.find());
		assertNonMatcher(m, "ABC", 0, 3);
		Assert.no(m.find());
		Assert.thrown(() -> m.group());
		Assert.thrown(() -> m.start());
		Assert.thrown(() -> m.end());
	}

	@Test
	public void shouldResetState() {
		var m = nonMatcher("[a-c]+", "abBCcDd");
		Assert.yes(m.find());
		assertNonMatcher(m, "BC", 2, 4);
		m.reset();
		Assert.yes(m.find());
		assertNonMatcher(m, "BC", 2, 4);
	}

	@Test
	public void shouldResetStateWithNewText() {
		var m = nonMatcher("[a-c]+", "abBCcDd");
		Assert.yes(m.find());
		assertNonMatcher(m, "BC", 2, 4);
		m.reset("aBbCcDd");
		Assert.yes(m.find());
		assertNonMatcher(m, "B", 1, 2);
	}

	@Test
	public void shouldConvertToResult() {
		var m = nonMatcher("[a-c]+", "abBCcDd");
		var r0 = m.toResult();
		Assert.thrown(() -> r0.group());
		m.find();
		Assert.equal(m.toResult().group(), "BC");
	}

	@Test
	public void shouldStreamResults() {
		var i = nonMatcher("[a-c]+", "abBCcDd").results().iterator();
		assertNonMatchResult(i.next(), "BC", 2, 4);
		assertNonMatchResult(i.next(), "Dd", 5, 7);
		Assert.thrown(NoSuchElementException.class, () -> i.next());
		Assert.no(i.hasNext());
	}

	@Test
	public void shouldReturnStringRepresentationOfState() {
		var m0 = nonMatcher("[a-c]+", "ABC").region(1, 2);
		var m1 = nonMatcher("[a-c]+", "ABC").region(1, 2);
		var m2 = nonMatcher("[a-c]+", "ABC");
		Assert.equal(m0.toString(), m1.toString());
		Assert.notEqual(m0.toString(), m2.toString());
		Assert.yes(m0.find());
		Assert.notEqual(m0.toString(), m1.toString());
		Assert.yes(m1.find());
		Assert.equal(m0.toString(), m1.toString());
	}

	private static void assertNonMatcher(NonMatch.Matcher m, String group, int start, int end) {
		Assert.equal(m.group(), group, "group");
		Assert.equal(m.start(), start, "start");
		Assert.equal(m.end(), end, "end");
	}

	private static void assertNonMatchResult(NonMatch.Result r, String group, int start, int end) {
		Assert.equal(r.group(), group, "group");
		Assert.equal(r.start(), start, "start");
		Assert.equal(r.end(), end, "end");
	}

	private static NonMatch.Matcher nonMatcher(String pattern, String text) {
		return NonMatch.of(Pattern.compile(pattern), text);
	}

	private static StringBuilder b(String s) {
		return new StringBuilder(s);
	}
}
