package ceri.common.property;

import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.common.test.TestUtil.isSame;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
		assertTrue(PathFactory.dot.path("a", "b.c").equals(key));
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
		assertThat(key, isSame(PathFactory.dot.emptyPath));
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
		assertThat(key.value, is("b.c"));
		key = key.orphan();
		assertThat(key.value, is("c"));
		key = key.orphan();
		assertThat(key, isSame(PathFactory.dot.emptyPath));
		key = key.orphan();
		assertThat(key, isSame(PathFactory.dot.emptyPath));
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
