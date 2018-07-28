package ceri.common.property;

import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.common.test.TestUtil.isSame;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import java.util.Collection;
import org.junit.Test;

public class PathFactoryBehavior {

	@Test
	public void shouldObeyEqualsContract() {
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
		assertThat(PathFactory.dash.parentOf(null), is(PathFactory.dash.emptyPath));
		assertThat(PathFactory.dash.parentOf(""), is(PathFactory.dash.emptyPath));
	}

	@Test
	public void shouldReturnOrphanForPath() {
		assertThat(PathFactory.dash.orphanOf(null), is(PathFactory.dash.emptyPath));
		assertThat(PathFactory.dash.orphanOf(""), is(PathFactory.dash.emptyPath));
		assertThat(PathFactory.dash.orphanOf("abc"), is(PathFactory.dash.emptyPath));
	}

	@Test
	public void shouldReturnFirstPartOfPath() {
		assertThat(PathFactory.dash.firstPart(null), is(""));
		assertThat(PathFactory.dash.firstPart(""), is(""));
		assertThat(PathFactory.dash.firstPart("abc"), is("abc"));
		assertThat(PathFactory.dash.firstPart("abc-def"), is("abc"));
	}

	@Test
	public void shouldReturnLastPartOfPath() {
		assertThat(PathFactory.dash.lastPart(null), is(""));
		assertThat(PathFactory.dash.lastPart(""), is(""));
		assertThat(PathFactory.dash.lastPart("abc"), is("abc"));
		assertThat(PathFactory.dash.lastPart("abc-def"), is("def"));
	}

	@Test
	public void shouldNotCreateBlankFactory() {
		assertException(() -> PathFactory.create(null));
		assertException(() -> PathFactory.create(""));
	}

	@Test
	public void shouldCountPathParts() {
		PathFactory factory = PathFactory.create(" ");
		assertThat(factory.parts(null), is(0));
		assertThat(factory.parts(" abc d e "), is(3));
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
		assertThat(PathFactory.dash.path((Collection<String>) null).value, is(""));
		assertThat(PathFactory.dot.path((String) null, "", "a", null, "b", "c", "", null).value,
			is("a.b.c"));
	}

	@Test
	public void shouldNotCreateNewNullInstances() {
		Path key = PathFactory.dot.path((String) null);
		assertThat(key, isSame(PathFactory.dot.emptyPath));
		key = PathFactory.dot.path((String) null);
		assertThat(key, isSame(PathFactory.dot.emptyPath));
		key = PathFactory.dot.path((String[]) null);
		assertThat(key, isSame(PathFactory.dot.emptyPath));
		key = PathFactory.dot.path(PathFactory.dot.emptyPath.value);
		assertThat(key, isSame(PathFactory.dot.emptyPath));
		key = PathFactory.dot.emptyPath.parent();
		assertThat(key, isSame(PathFactory.dot.emptyPath));
		key = PathFactory.dot.path("");
		assertThat(key, isSame(PathFactory.dot.emptyPath));
		key = PathFactory.dot.path(PathFactory.dot.emptyPath.value, "", "");
		assertThat(key, isSame(PathFactory.dot.emptyPath));
		key = PathFactory.dot.path((String) null, (String[]) null);
		assertThat(key, isSame(PathFactory.dot.emptyPath));
	}

}
