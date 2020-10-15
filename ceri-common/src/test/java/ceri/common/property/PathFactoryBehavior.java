package ceri.common.property;

import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertNotEquals;
import static ceri.common.test.TestUtil.assertSame;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import java.util.Collection;
import org.junit.Test;
import ceri.common.test.TestUtil;

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
		TestUtil.assertThrown(() -> PathFactory.create(null));
		TestUtil.assertThrown(() -> PathFactory.create(""));
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
		assertThat(PathFactory.dot.path(null, "", "a", null, "b", "c", "", null).value,
			is("a.b.c"));
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
