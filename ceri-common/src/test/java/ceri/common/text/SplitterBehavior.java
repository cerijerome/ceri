package ceri.common.text;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
import ceri.common.text.Splitter.Extraction;
import ceri.common.text.Splitter.Extractor;

public class SplitterBehavior {

	@Test
	public void shouldNotBreachExtractionEqualsContract() {
		Extraction t = Extraction.of("test", 7);
		Extraction eq0 = Extraction.of("test", 7);
		Extraction ne0 = Extraction.NULL;
		Extraction ne1 = Extraction.of("Test", 7);
		Extraction ne2 = Extraction.of("test", 0);
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldDetermineNullExtraction() {
		Assert.yes(Extraction.NULL.isNull());
		Assert.yes(Extraction.of("", 0).isNull());
		Assert.no(Extraction.of("x", 0).isNull());
		Assert.no(Extraction.of("", 1).isNull());
	}

	@Test
	public void shouldReturnStateOfSplitter() {
		Splitter sp = Splitter.of("a b c");
		Assert.equal(sp.position(), 0);
		Assert.equal(sp.remainder(), "a b c");
		Assert.equal(sp.text(), "a b c");
		Assert.equal(sp.extract(Extractor.byWidth(2)), Extraction.of("a", 2));
		Assert.equal(sp.position(), 2);
		Assert.equal(sp.remainder(), "b c");
		Assert.equal(sp.text(), "a b c");
		Assert.equal(sp.extract(Extractor.byWidth(2)), Extraction.of("b", 2));
		Assert.equal(sp.position(), 4);
		Assert.equal(sp.remainder(), "c");
		Assert.equal(sp.text(), "a b c");
		Assert.equal(sp.extract(Extractor.byWidth(2)), Extraction.of("c", 1));
		Assert.equal(sp.position(), 5);
		Assert.equal(sp.remainder(), "");
		Assert.equal(sp.text(), "a b c");
	}

	@Test
	public void shouldSplitByTabs() {
		Assert.equal(Splitter.of("abc\td\t\t e \tf").extract(Extractor.byTabs()),
			Extraction.of("abc", 4));
		Assert.ordered(Splitter.of("abc\td\t\t e \tf").extractAll(Extractor.byTabs()),
			Extraction.of("abc", 4));
		Assert.ordered(Splitter.of("abc\td\t\t e \tf").extractToCompletion(Extractor.byTabs()),
			Extraction.of("abc", 4), Extraction.of("d", 3), Extraction.of(" e ", 4),
			Extraction.of("f", 1));
	}

	@Test
	public void shouldSplitByFixedWidths() {
		Assert.equal(Splitter.of("abcdefgh  ").extract(Extractor.byWidth(1)),
			Extraction.of("a", 1));
		Assert.ordered(Splitter.of("abcdefgh  ").extractAll(Extractor.byWidths(1, 2, 3)),
			Extraction.of("a", 1), Extraction.of("bc", 2), Extraction.of("def", 3));
		Assert.ordered(Splitter.of("abcdefgh  ").extractToCompletion(Extractor.byWidths(1, 2, 3)),
			Extraction.of("a", 1), Extraction.of("bc", 2), Extraction.of("def", 3),
			Extraction.of("gh", 3), Extraction.of("", 1));
	}

	@Test
	public void shouldTreatZeroWidthAsRemainder() {
		Assert.ordered(Splitter.of("abcde").extractAll(Extractor.byWidths(2, 0)),
			Extraction.of("ab", 2), Extraction.of("cde", 3));
	}

	@Test
	public void shouldNotOverflowWidth() {
		Assert.ordered(Splitter.of("abcde").extractAll(Extractor.byWidths(2, Integer.MAX_VALUE)),
			Extraction.of("ab", 2), Extraction.of("cde", 3));
	}

	@Test
	public void shouldSplitByRegex() {
		String s = "";
		String r = "";
		Assert.equal(Splitter.of(s).extract(Extractor.byRegex(r)), Extraction.NULL);
		Assert.ordered(Splitter.of(s).extractAll(Extractor.byRegex(r)), Extraction.NULL);
		Assert.ordered(Splitter.of(s).extractToCompletion(Extractor.byRegex(r)));
		s = "abc";
		r = "";
		Assert.equal(Splitter.of(s).extract(Extractor.byRegex(r)), Extraction.NULL);
		Assert.ordered(Splitter.of(s).extractAll(Extractor.byRegex(r)), Extraction.NULL);
		Assert.ordered(Splitter.of(s).extractToCompletion(Extractor.byRegex(r)));
		s = "abc";
		r = "123";
		Assert.equal(Splitter.of(s).extract(Extractor.byRegex(r)), Extraction.NULL);
		Assert.ordered(Splitter.of(s).extractAll(Extractor.byRegex(r)), Extraction.NULL);
		Assert.ordered(Splitter.of(s).extractToCompletion(Extractor.byRegex(r)));
		s = "  a\tbc \nd";
		r = "(.*?)(?:\\s+|$)";
		Assert.equal(Splitter.of(s).extract(Extractor.byRegex(r)), Extraction.of("", 2));
		Assert.ordered(Splitter.of(s).extractAll(Extractor.byRegex(r)), Extraction.of("", 2));
		Assert.ordered(Splitter.of(s).extractToCompletion(Extractor.byRegex(r)),
			Extraction.of("", 2), Extraction.of("a", 2), Extraction.of("bc", 4),
			Extraction.of("d", 1));
	}

	@Test
	public void shouldSplitByRemainder() {
		String s = "a b c";
		Assert.equal(Splitter.of(s).extract(Extractor.byRemainder()), Extraction.of("a b c", 5));
		Assert.ordered(Splitter.of(s).extractAll(Extractor.byWidth(2), Extractor.byRemainder()),
			Extraction.of("a", 2), Extraction.of("b c", 3));
		Assert.ordered(
			Splitter.of(s).extractToCompletion(Extractor.byWidth(2), Extractor.byRemainder()),
			Extraction.of("a", 2), Extraction.of("b c", 3));
	}

	@Test
	public void shouldStopAtEndOfText() {
		Extractor ex = Extractor.byRegex("\\w");
		Assert.ordered(Splitter.of("abc").extractAll(ex, ex, ex, ex), Extraction.of("a", 1),
			Extraction.of("b", 1), Extraction.of("c", 1), Extraction.NULL);
		Assert.ordered(Splitter.of("abc").extractToCompletion(ex), Extraction.of("a", 1),
			Extraction.of("b", 1), Extraction.of("c", 1));
	}

}
