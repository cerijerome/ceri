package ceri.common.property;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;
import ceri.common.color.X11Color;

public class LocatorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Locator loc = Locator.of(getClass(), "ceri-abc-def.txt");
		Locator loc2 = Locator.of(loc.cls, loc.filename());
		Locator loc3 = Locator.builder(loc.cls, "ceri-abc-def").extension("txt").build();
		exerciseEquals(loc, loc2, loc3);
		assertAllNotEqual(loc, Locator.of(Locator.class, "ceri-abc-def.txt"));
		assertAllNotEqual(loc, Locator.of(getClass(), "ceri-abc-def.properties"));
		assertAllNotEqual(loc, Locator.of(getClass(), "ceri-abc.txt"));
		assertAllNotEqual(loc, Locator.builder(getClass(), "ceri-abc-def.txt").extension(""));
	}

	@Test
	public void shouldExtractAncestors() {
		assertIterable(Locator.of(getClass(), "abc.csv").ancestors(),
			Locator.of(getClass(), "abc.csv"));
		assertIterable(Locator.of(getClass(), "abc-de-f.csv").ancestors(),
			Locator.of(getClass(), "abc.csv"), //
			Locator.of(getClass(), "abc-de.csv"), //
			Locator.of(getClass(), "abc-de-f.csv"));
	}

	@Test
	public void shouldCreateChildLocators() {
		assertEquals(Locator.of(Object.class, "abc.txt").child("def").filename(), "abc-def.txt");
		assertEquals(Locator.of(Object.class, "a.txt").child("b", "c").filename(), "a-b-c.txt");
		assertEquals(
			Locator.of(Object.class, "a.txt").child(X11Color.cyan, X11Color.lime).filename(),
			"a-cyan-lime.txt");
	}

	@Test
	public void shouldDetermineIfRootPath() {
		assertTrue(Locator.of(getClass(), "abc.properties").isRoot());
		assertTrue(Locator.of(getClass(), ".properties").isRoot());
		assertTrue(Locator.of(getClass(), "abc.").isRoot());
		assertFalse(Locator.of(getClass(), "abc-def.").isRoot());
		assertFalse(Locator.of(getClass(), "abc-def.txt").isRoot());
	}

	@Test
	public void shouldDetermineIfNull() {
		assertTrue(Locator.NULL.isNull());
		assertTrue(Locator.of(Object.class, "abc").parent().isNull());
		assertFalse(Locator.of(Object.class, "abc").isNull());
	}

	@Test
	public void shouldCreateLocators() {
		assertEquals(Locator.of(Object.class).filename(), "Object.properties");
		assertEquals(Locator.of(Object.class, "test").filename(), "test.properties");
		assertEquals(Locator.builder(String.class).add(X11Color.aqua).build().filename(),
			"String-aqua.properties");
		assertThrown(() -> Locator.of(getClass(), ""));
	}

	@Test
	public void shouldReturnFilename() {
		assertEquals(Locator.of(getClass(), "abc").filename(), "abc.properties");
		assertEquals(Locator.builder(getClass(), "A").extension(null).build().filename(), "A");
		assertEquals(Locator.builder(getClass(), "A").extension("").build().filename(), "A");
		assertEquals(Locator.of(getClass(), ".A").filename(), ".A");
		assertEquals(Locator.of(getClass(), "A.").filename(), "A");
	}

}
