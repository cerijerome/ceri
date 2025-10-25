package ceri.common.property;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.nullPointer;
import org.junit.Test;

public class SeparatorBehavior {

	@Test
	public void shouldDetermineIfNull() {
		assertEquals(Separator.NULL.isNull(), true);
		assertEquals(new Separator("").isNull(), true);
		assertEquals(new Separator(" ").isNull(), false);
		nullPointer(() -> new Separator(null));
	}

	@Test
	public void shouldProvideRootKey() {
		assertEquals(Separator.NULL.root().value(), "");
		assertEquals(Separator.DASH.root().value(), "");
	}

	@Test
	public void shouldChompStrings() {
		String[] pre = { "", ".a.", null };
		assertEquals(Separator.DOT.chomp(pre, "", null, ".b..c.d."), "a.b..c.d");
		assertEquals(Separator.NULL.chomp(pre, "", null, ".b..c.d."), ".a..b..c.d.");
	}

	@Test
	public void shouldNormalizeStrings() {
		String[] pre = { "", ".a.", null };
		assertEquals(Separator.DOT.normalize(pre, "", null, ".b..c.d."), "a.b.c.d");
		assertEquals(Separator.NULL.normalize(pre, "", null, ".b..c.d."), ".a..b..c.d.");
	}

	@Test
	public void shouldNormalizeStringsWithSeparator() {
		String[] pre = { "", ".a.", null };
		assertEquals(Separator.DOT.normalize(Separator.SLASH, pre, "", null, ".b..c.d."),
			"a/b/c/d");
		assertEquals(Separator.DOT.normalize(Separator.NULL, pre, "", null, ".b..c.d."), "abcd");
		assertEquals(Separator.NULL.normalize(Separator.DOT, pre, "", null, ".b..c.d."),
			".a...b..c.d.");
	}

	@Test
	public void shouldMatchString() {
		assertEquals(Separator.DOT.matches(null, 0), false);
		assertEquals(Separator.DOT.matches(".abc", 0), true);
		assertEquals(Separator.DOT.matches("a.b", 0), false);
		assertEquals(Separator.DOT.matches("a.b", 3), false);
		assertEquals(Separator.DOT.matches("a.b", 1), true);
	}

}
