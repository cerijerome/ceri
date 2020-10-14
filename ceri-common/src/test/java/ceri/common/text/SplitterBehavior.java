package ceri.common.text;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;
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
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldDetermineNullExtraction() {
		assertThat(Extraction.NULL.isNull(), is(true));
		assertThat(Extraction.of("", 0).isNull(), is(true));
		assertThat(Extraction.of("x", 0).isNull(), is(false));
		assertThat(Extraction.of("", 1).isNull(), is(false));
	}

	@Test
	public void shouldReturnStateOfSplitter() {
		Splitter sp = Splitter.of("a b c");
		assertThat(sp.position(), is(0));
		assertThat(sp.remainder(), is("a b c"));
		assertThat(sp.text(), is("a b c"));
		assertThat(sp.extract(Extractor.byWidth(2)), is(Extraction.of("a", 2)));
		assertThat(sp.position(), is(2));
		assertThat(sp.remainder(), is("b c"));
		assertThat(sp.text(), is("a b c"));
		assertThat(sp.extract(Extractor.byWidth(2)), is(Extraction.of("b", 2)));
		assertThat(sp.position(), is(4));
		assertThat(sp.remainder(), is("c"));
		assertThat(sp.text(), is("a b c"));
		assertThat(sp.extract(Extractor.byWidth(2)), is(Extraction.of("c", 1)));
		assertThat(sp.position(), is(5));
		assertThat(sp.remainder(), is(""));
		assertThat(sp.text(), is("a b c"));
	}

	@Test
	public void shouldSplitByTabs() {
		assertThat(Splitter.of("abc\td\t\t e \tf").extract(Extractor.byTabs()),
			is(Extraction.of("abc", 4)));
		assertIterable(Splitter.of("abc\td\t\t e \tf").extractAll(Extractor.byTabs()),
			Extraction.of("abc", 4));
		assertIterable(Splitter.of("abc\td\t\t e \tf").extractToCompletion(Extractor.byTabs()),
			Extraction.of("abc", 4), Extraction.of("d", 3), Extraction.of(" e ", 4),
			Extraction.of("f", 1));
	}

	@Test
	public void shouldSplitByFixedWidths() {
		assertThat(Splitter.of("abcdefgh  ").extract(Extractor.byWidth(1)),
			is(Extraction.of("a", 1)));
		assertIterable(Splitter.of("abcdefgh  ").extractAll(Extractor.byWidths(1, 2, 3)),
			Extraction.of("a", 1), Extraction.of("bc", 2), Extraction.of("def", 3));
		assertIterable(Splitter.of("abcdefgh  ").extractToCompletion(Extractor.byWidths(1, 2, 3)),
			Extraction.of("a", 1), Extraction.of("bc", 2), Extraction.of("def", 3),
			Extraction.of("gh", 3), Extraction.of("", 1));
	}

	@Test
	public void shouldTreatZeroWidthAsRemainder() {
		assertIterable(Splitter.of("abcde").extractAll(Extractor.byWidths(2, 0)),
			Extraction.of("ab", 2), Extraction.of("cde", 3));
	}

	@Test
	public void shouldNotOverflowWidth() {
		assertIterable(Splitter.of("abcde").extractAll(Extractor.byWidths(2, Integer.MAX_VALUE)),
			Extraction.of("ab", 2), Extraction.of("cde", 3));
	}

	@Test
	public void shouldSplitByRegex() {
		String s = "";
		String r = "";
		assertThat(Splitter.of(s).extract(Extractor.byRegex(r)), is(Extraction.NULL));
		assertIterable(Splitter.of(s).extractAll(Extractor.byRegex(r)), Extraction.NULL);
		assertIterable(Splitter.of(s).extractToCompletion(Extractor.byRegex(r)));
		s = "abc";
		r = "";
		assertThat(Splitter.of(s).extract(Extractor.byRegex(r)), is(Extraction.NULL));
		assertIterable(Splitter.of(s).extractAll(Extractor.byRegex(r)), Extraction.NULL);
		assertIterable(Splitter.of(s).extractToCompletion(Extractor.byRegex(r)));
		s = "abc";
		r = "123";
		assertThat(Splitter.of(s).extract(Extractor.byRegex(r)), is(Extraction.NULL));
		assertIterable(Splitter.of(s).extractAll(Extractor.byRegex(r)), Extraction.NULL);
		assertIterable(Splitter.of(s).extractToCompletion(Extractor.byRegex(r)));
		s = "  a\tbc \nd";
		r = "(.*?)(?:\\s+|$)";
		assertThat(Splitter.of(s).extract(Extractor.byRegex(r)), is(Extraction.of("", 2)));
		assertIterable(Splitter.of(s).extractAll(Extractor.byRegex(r)), Extraction.of("", 2));
		assertIterable(Splitter.of(s).extractToCompletion(Extractor.byRegex(r)),
			Extraction.of("", 2), Extraction.of("a", 2), Extraction.of("bc", 4),
			Extraction.of("d", 1));
	}

	@Test
	public void shouldSplitByRemainder() {
		String s = "a b c";
		assertThat(Splitter.of(s).extract(Extractor.byRemainder()), is(Extraction.of("a b c", 5)));
		assertIterable(Splitter.of(s).extractAll(Extractor.byWidth(2), Extractor.byRemainder()),
			Extraction.of("a", 2), Extraction.of("b c", 3));
		assertIterable(
			Splitter.of(s).extractToCompletion(Extractor.byWidth(2), Extractor.byRemainder()),
			Extraction.of("a", 2), Extraction.of("b c", 3));
	}

	@Test
	public void shouldStopAtEndOfText() {
		Extractor ex = Extractor.byRegex("\\w");
		assertIterable(Splitter.of("abc").extractAll(ex, ex, ex, ex), Extraction.of("a", 1),
			Extraction.of("b", 1), Extraction.of("c", 1), Extraction.NULL);
		assertIterable(Splitter.of("abc").extractToCompletion(ex), Extraction.of("a", 1),
			Extraction.of("b", 1), Extraction.of("c", 1));
	}

}
