package ceri.common.property;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.util.Collection;
import org.junit.Test;

public class PathFactoryBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		PathFactory factory = PathFactory.create(";");
		PathFactory factory1 = PathFactory.create(";");
		PathFactory factory2 = PathFactory.create(factory.separator);
		exerciseEquals(factory, factory1, factory2);
		assertNotEquals(factory, PathFactory.dot);
		assertNotEquals(factory, PathFactory.dash);
		assertNotEquals(factory, PathFactory.create("-"));
		assertNotEquals(factory, PathFactory.create("."));
		assertNotEquals(factory, PathFactory.create(":"));
	}

	@Test
	public void shouldReturnParentForPath() {
		assertEquals(PathFactory.dash.parentOf(null), PathFactory.dash.emptyPath);
		assertEquals(PathFactory.dash.parentOf(""), PathFactory.dash.emptyPath);
	}

	@Test
	public void shouldReturnOrphanForPath() {
		assertEquals(PathFactory.dash.orphanOf(null), PathFactory.dash.emptyPath);
		assertEquals(PathFactory.dash.orphanOf(""), PathFactory.dash.emptyPath);
		assertEquals(PathFactory.dash.orphanOf("abc"), PathFactory.dash.emptyPath);
	}

	@Test
	public void shouldReturnFirstPartOfPath() {
		assertEquals(PathFactory.dash.firstPart(null), "");
		assertEquals(PathFactory.dash.firstPart(""), "");
		assertEquals(PathFactory.dash.firstPart("abc"), "abc");
		assertEquals(PathFactory.dash.firstPart("abc-def"), "abc");
	}

	@Test
	public void shouldReturnLastPartOfPath() {
		assertEquals(PathFactory.dash.lastPart(null), "");
		assertEquals(PathFactory.dash.lastPart(""), "");
		assertEquals(PathFactory.dash.lastPart("abc"), "abc");
		assertEquals(PathFactory.dash.lastPart("abc-def"), "def");
	}

	@Test
	public void shouldNotCreateBlankFactory() {
		assertThrown(() -> PathFactory.create(null));
		assertThrown(() -> PathFactory.create(""));
	}

	@Test
	public void shouldCountPathParts() {
		PathFactory factory = PathFactory.create(" ");
		assertEquals(factory.parts(null), 0);
		assertEquals(factory.parts(" abc d e "), 3);
	}

	@Test
	public void shouldSplitPaths() {
		PathFactory factory = PathFactory.create(" ");
		assertIterable(factory.split(null));
		assertIterable(factory.split(""));
		assertIterable(factory.split(" abc d e "), "abc", "d", "e");
	}

	@Test
	public void shouldIgnoreBlankParts() {
		assertEquals(PathFactory.dash.path((Collection<String>) null).value, "");
		assertEquals(PathFactory.dot.path(null, "", "a", null, "b", "c", "", null).value, "a.b.c");
	}

	@Test
	public void shouldNotCreateNewNullInstances() {
		Path key = PathFactory.dot.path((String) null);
		assertSame(key, PathFactory.dot.emptyPath);
		key = PathFactory.dot.path((String) null);
		assertSame(key, PathFactory.dot.emptyPath);
		key = PathFactory.dot.path((String[]) null);
		assertSame(key, PathFactory.dot.emptyPath);
		key = PathFactory.dot.path(PathFactory.dot.emptyPath.value);
		assertSame(key, PathFactory.dot.emptyPath);
		key = PathFactory.dot.emptyPath.parent();
		assertSame(key, PathFactory.dot.emptyPath);
		key = PathFactory.dot.path("");
		assertSame(key, PathFactory.dot.emptyPath);
		key = PathFactory.dot.path(PathFactory.dot.emptyPath.value, "", "");
		assertSame(key, PathFactory.dot.emptyPath);
		key = PathFactory.dot.path(null, (String[]) null);
		assertSame(key, PathFactory.dot.emptyPath);
	}

}
