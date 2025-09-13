package ceri.common.text;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.test.TestUtil;
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
		TestUtil.exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldDetermineNullExtraction() {
		assertTrue(Extraction.NULL.isNull());
		assertTrue(Extraction.of("", 0).isNull());
		assertFalse(Extraction.of("x", 0).isNull());
		assertFalse(Extraction.of("", 1).isNull());
	}

	@Test
	public void shouldReturnStateOfSplitter() {
		Splitter sp = Splitter.of("a b c");
		assertEquals(sp.position(), 0);
		assertEquals(sp.remainder(), "a b c");
		assertEquals(sp.text(), "a b c");
		assertEquals(sp.extract(Extractor.byWidth(2)), Extraction.of("a", 2));
		assertEquals(sp.position(), 2);
		assertEquals(sp.remainder(), "b c");
		assertEquals(sp.text(), "a b c");
		assertEquals(sp.extract(Extractor.byWidth(2)), Extraction.of("b", 2));
		assertEquals(sp.position(), 4);
		assertEquals(sp.remainder(), "c");
		assertEquals(sp.text(), "a b c");
		assertEquals(sp.extract(Extractor.byWidth(2)), Extraction.of("c", 1));
		assertEquals(sp.position(), 5);
		assertEquals(sp.remainder(), "");
		assertEquals(sp.text(), "a b c");
	}

	@Test
	public void shouldSplitByTabs() {
		assertEquals(Splitter.of("abc\td\t\t e \tf").extract(Extractor.byTabs()),
			Extraction.of("abc", 4));
		assertOrdered(Splitter.of("abc\td\t\t e \tf").extractAll(Extractor.byTabs()),
			Extraction.of("abc", 4));
		assertOrdered(Splitter.of("abc\td\t\t e \tf").extractToCompletion(Extractor.byTabs()),
			Extraction.of("abc", 4), Extraction.of("d", 3), Extraction.of(" e ", 4),
			Extraction.of("f", 1));
	}

	@Test
	public void shouldSplitByFixedWidths() {
		assertEquals(Splitter.of("abcdefgh  ").extract(Extractor.byWidth(1)),
			Extraction.of("a", 1));
		assertOrdered(Splitter.of("abcdefgh  ").extractAll(Extractor.byWidths(1, 2, 3)),
			Extraction.of("a", 1), Extraction.of("bc", 2), Extraction.of("def", 3));
		assertOrdered(Splitter.of("abcdefgh  ").extractToCompletion(Extractor.byWidths(1, 2, 3)),
			Extraction.of("a", 1), Extraction.of("bc", 2), Extraction.of("def", 3),
			Extraction.of("gh", 3), Extraction.of("", 1));
	}

	@Test
	public void shouldTreatZeroWidthAsRemainder() {
		assertOrdered(Splitter.of("abcde").extractAll(Extractor.byWidths(2, 0)),
			Extraction.of("ab", 2), Extraction.of("cde", 3));
	}

	@Test
	public void shouldNotOverflowWidth() {
		assertOrdered(Splitter.of("abcde").extractAll(Extractor.byWidths(2, Integer.MAX_VALUE)),
			Extraction.of("ab", 2), Extraction.of("cde", 3));
	}

	@Test
	public void shouldSplitByRegex() {
		String s = "";
		String r = "";
		assertEquals(Splitter.of(s).extract(Extractor.byRegex(r)), Extraction.NULL);
		assertOrdered(Splitter.of(s).extractAll(Extractor.byRegex(r)), Extraction.NULL);
		assertOrdered(Splitter.of(s).extractToCompletion(Extractor.byRegex(r)));
		s = "abc";
		r = "";
		assertEquals(Splitter.of(s).extract(Extractor.byRegex(r)), Extraction.NULL);
		assertOrdered(Splitter.of(s).extractAll(Extractor.byRegex(r)), Extraction.NULL);
		assertOrdered(Splitter.of(s).extractToCompletion(Extractor.byRegex(r)));
		s = "abc";
		r = "123";
		assertEquals(Splitter.of(s).extract(Extractor.byRegex(r)), Extraction.NULL);
		assertOrdered(Splitter.of(s).extractAll(Extractor.byRegex(r)), Extraction.NULL);
		assertOrdered(Splitter.of(s).extractToCompletion(Extractor.byRegex(r)));
		s = "  a\tbc \nd";
		r = "(.*?)(?:\\s+|$)";
		assertEquals(Splitter.of(s).extract(Extractor.byRegex(r)), Extraction.of("", 2));
		assertOrdered(Splitter.of(s).extractAll(Extractor.byRegex(r)), Extraction.of("", 2));
		assertOrdered(Splitter.of(s).extractToCompletion(Extractor.byRegex(r)),
			Extraction.of("", 2), Extraction.of("a", 2), Extraction.of("bc", 4),
			Extraction.of("d", 1));
	}

	@Test
	public void shouldSplitByRemainder() {
		String s = "a b c";
		assertEquals(Splitter.of(s).extract(Extractor.byRemainder()), Extraction.of("a b c", 5));
		assertOrdered(Splitter.of(s).extractAll(Extractor.byWidth(2), Extractor.byRemainder()),
			Extraction.of("a", 2), Extraction.of("b c", 3));
		assertOrdered(
			Splitter.of(s).extractToCompletion(Extractor.byWidth(2), Extractor.byRemainder()),
			Extraction.of("a", 2), Extraction.of("b c", 3));
	}

	@Test
	public void shouldStopAtEndOfText() {
		Extractor ex = Extractor.byRegex("\\w");
		assertOrdered(Splitter.of("abc").extractAll(ex, ex, ex, ex), Extraction.of("a", 1),
			Extraction.of("b", 1), Extraction.of("c", 1), Extraction.NULL);
		assertOrdered(Splitter.of("abc").extractToCompletion(ex), Extraction.of("a", 1),
			Extraction.of("b", 1), Extraction.of("c", 1));
	}

}
