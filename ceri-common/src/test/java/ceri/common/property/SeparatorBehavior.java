package ceri.common.property;

import org.junit.Test;
import ceri.common.test.Assert;

public class SeparatorBehavior {

	@Test
	public void shouldDetermineIfNull() {
		Assert.equal(Separator.NULL.isNull(), true);
		Assert.equal(new Separator("").isNull(), true);
		Assert.equal(new Separator(" ").isNull(), false);
		Assert.nullPointer(() -> new Separator(null));
	}

	@Test
	public void shouldProvideRootKey() {
		Assert.equal(Separator.NULL.root().value(), "");
		Assert.equal(Separator.DASH.root().value(), "");
	}

	@Test
	public void shouldChompStrings() {
		String[] pre = { "", ".a.", null };
		Assert.equal(Separator.DOT.chomp(pre, "", null, ".b..c.d."), "a.b..c.d");
		Assert.equal(Separator.NULL.chomp(pre, "", null, ".b..c.d."), ".a..b..c.d.");
	}

	@Test
	public void shouldNormalizeStrings() {
		String[] pre = { "", ".a.", null };
		Assert.equal(Separator.DOT.normalize(pre, "", null, ".b..c.d."), "a.b.c.d");
		Assert.equal(Separator.NULL.normalize(pre, "", null, ".b..c.d."), ".a..b..c.d.");
	}

	@Test
	public void shouldNormalizeStringsWithSeparator() {
		String[] pre = { "", ".a.", null };
		Assert.equal(Separator.DOT.normalize(Separator.SLASH, pre, "", null, ".b..c.d."),
			"a/b/c/d");
		Assert.equal(Separator.DOT.normalize(Separator.NULL, pre, "", null, ".b..c.d."), "abcd");
		Assert.equal(Separator.NULL.normalize(Separator.DOT, pre, "", null, ".b..c.d."),
			".a...b..c.d.");
	}

	@Test
	public void shouldMatchString() {
		Assert.equal(Separator.DOT.matches(null, 0), false);
		Assert.equal(Separator.DOT.matches(".abc", 0), true);
		Assert.equal(Separator.DOT.matches("a.b", 0), false);
		Assert.equal(Separator.DOT.matches("a.b", 3), false);
		Assert.equal(Separator.DOT.matches("a.b", 1), true);
	}

}
