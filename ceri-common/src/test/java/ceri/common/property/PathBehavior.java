package ceri.common.property;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import org.junit.Test;

public class PathBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Path key = PathFactory.dot.path("a");
		Path key1 = PathFactory.dot.path(key.value);
		Path key2 = PathFactory.dot.path("a.");
		Path key3 = PathFactory.dot.path(".a");
		exerciseEquals(key, key1, key2, key3);
		assertNotEquals(key, PathFactory.dash.path("a"));
		assertNotEquals(key, PathFactory.dot.path("a.b"));
	}

	@Test
	public void shouldReturnFirstPart() {
		Path key = PathFactory.dot.path("a", "b", "c");
		assertEquals(key.firstPart(), "a");
	}

	@Test
	public void shouldReturnLastPart() {
		Path key = PathFactory.dot.path("a", "b", "c");
		assertEquals(key.lastPart(), "c");
	}

	@Test
	public void shouldIterateParts() {
		Path key = PathFactory.dot.path("a", "b", "c");
		Iterator<String> i = key.partIterator();
		assertEquals(i.next(), "a");
		assertEquals(i.next(), "b");
		assertEquals(i.next(), "c");
	}

	@Test
	public void shouldAllowPrefixParts() {
		Path key = PathFactory.dot.path("a.b", "c");
		assertEquals(key.value, "a.b.c");
		assertEquals(PathFactory.dot.path("a", "b.c"), key);
	}

	@Test
	public void shouldNavigateParents() {
		Path key = PathFactory.dot.path("a", "b", "c");
		assertEquals(key.value, "a.b.c");
		assertFalse(key.isRoot());
		key = key.parent();
		assertEquals(key.value, "a.b");
		assertFalse(key.isRoot());
		key = key.parent();
		assertEquals(key.value, "a");
		assertTrue(key.isRoot());
		key = key.parent();
		assertSame(key, PathFactory.dot.emptyPath);
		assertTrue(key.isRoot());
	}

	@Test
	public void shouldNavigateChildren() {
		Path key = PathFactory.dot.path("a", "b", "c");
		assertEquals(key.value, "a.b.c");
		key = key.child("d");
		assertEquals(key.value, "a.b.c.d");
		key = key.child(Arrays.asList("e", "f"));
		assertEquals(key.value, "a.b.c.d.e.f");
	}

	@Test
	public void shouldNavigateOrphans() {
		Path key = PathFactory.dot.path("a", "b", "c");
		key = key.orphan();
		assertEquals(key.value, "b.c");
		key = key.orphan();
		assertEquals(key.value, "c");
		key = key.orphan();
		assertSame(key, PathFactory.dot.emptyPath);
		key = key.orphan();
		assertSame(key, PathFactory.dot.emptyPath);
	}

	@Test
	public void shouldSplitParts() {
		Path key = PathFactory.dot.path("a", "b", "c");
		assertEquals(key.asParts(), Arrays.asList("a", "b", "c"));
		assertEquals(key.parts(), 3);
		key = PathFactory.dot.path("a");
		assertEquals(key.asParts(), Arrays.asList("a"));
		key = PathFactory.dot.path("");
		assertTrue(key.isEmpty());
		assertEquals(key.asParts(), Collections.<String>emptyList());
		assertEquals(PathFactory.dot.emptyPath.asParts(), Collections.<String>emptyList());
	}

}
