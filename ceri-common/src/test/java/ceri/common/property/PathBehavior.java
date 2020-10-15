package ceri.common.property;

import static ceri.common.test.TestUtil.assertEquals;
import static ceri.common.test.TestUtil.assertFalse;
import static ceri.common.test.TestUtil.assertNotEquals;
import static ceri.common.test.TestUtil.assertSame;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
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
		assertThat(key.firstPart(), is("a"));
	}

	@Test
	public void shouldReturnLastPart() {
		Path key = PathFactory.dot.path("a", "b", "c");
		assertThat(key.lastPart(), is("c"));
	}

	@Test
	public void shouldIterateParts() {
		Path key = PathFactory.dot.path("a", "b", "c");
		Iterator<String> i = key.partIterator();
		assertThat(i.next(), is("a"));
		assertThat(i.next(), is("b"));
		assertThat(i.next(), is("c"));
	}

	@Test
	public void shouldAllowPrefixParts() {
		Path key = PathFactory.dot.path("a.b", "c");
		assertThat(key.value, is("a.b.c"));
		assertEquals(PathFactory.dot.path("a", "b.c"), key);
	}

	@Test
	public void shouldNavigateParents() {
		Path key = PathFactory.dot.path("a", "b", "c");
		assertThat(key.value, is("a.b.c"));
		assertFalse(key.isRoot());
		key = key.parent();
		assertThat(key.value, is("a.b"));
		assertFalse(key.isRoot());
		key = key.parent();
		assertThat(key.value, is("a"));
		assertTrue(key.isRoot());
		key = key.parent();
		assertSame(key, PathFactory.dot.emptyPath);
		assertTrue(key.isRoot());
	}

	@Test
	public void shouldNavigateChildren() {
		Path key = PathFactory.dot.path("a", "b", "c");
		assertThat(key.value, is("a.b.c"));
		key = key.child("d");
		assertThat(key.value, is("a.b.c.d"));
		key = key.child(Arrays.asList("e", "f"));
		assertThat(key.value, is("a.b.c.d.e.f"));
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
		assertThat(key.asParts(), is(Arrays.asList("a", "b", "c")));
		assertThat(key.parts(), is(3));
		key = PathFactory.dot.path("a");
		assertThat(key.asParts(), is(Arrays.asList("a")));
		key = PathFactory.dot.path("");
		assertTrue(key.isEmpty());
		assertThat(key.asParts(), is(Collections.<String>emptyList()));
		assertThat(PathFactory.dot.emptyPath.asParts(), is(Collections.<String>emptyList()));
	}

}
