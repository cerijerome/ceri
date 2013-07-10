package ceri.common.property;

import static ceri.common.test.TestUtil.isSame;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class KeyBehavior {
	
	@Test
	public void shouldNavigateParents() {
		Key key = Key.create("a", "b", "c");
		assertThat(key.value, is("a.b.c"));
		key = key.parent();
		assertThat(key.value, is("a.b"));
		key = key.parent();
		assertThat(key.value, is("a"));
		key = key.parent();
		assertThat(key, isSame(Key.NULL));
	}

	@Test
	public void shouldNavigateChildren() {
		Key key = Key.create("a", "b", "c");
		assertThat(key.value, is("a.b.c"));
		key = key.child("d");
		assertThat(key.value, is("a.b.c.d"));
		key = key.child("e");
		assertThat(key.value, is("a.b.c.d.e"));
	}

	@Test
	public void shouldNavigateOrphans() {
		Key key = Key.create("a", "b", "c");
		key = key.orphan();
		assertThat(key.value, is("b.c"));
		key = key.orphan();
		assertThat(key.value, is("c"));
		key = key.orphan();
		assertThat(key, isSame(Key.NULL));
		key = key.orphan();
		assertThat(key, isSame(Key.NULL));
	}

	@Test
	public void shouldSplitParts() {
		Key key = Key.create("a", "b", "c");
		assertThat(key.asParts(), is(Arrays.asList("a", "b", "c")));
		key = Key.create("a");
		assertThat(key.asParts(), is(Arrays.asList("a")));
		key = Key.create("");
		assertThat(key.asParts(), is(Collections.<String>emptyList()));
		assertThat(Key.NULL.asParts(), is(Collections.<String>emptyList()));
	}
	
	@Test
	public void shouldIgnoreBlankParts() {
		Key key = Key.create((String)null, "", "a", null, "b", "c", "", null);
		assertThat(key.value, is("a.b.c"));
	}

	@Test
	public void shouldNotCreateNewNullInstances() {
		Key key = Key.create((String)null);
		assertThat(key, isSame(Key.NULL));
		key = Key.create((Key)null);
		assertThat(key, isSame(Key.NULL));
		key = Key.create();
		assertThat(key, isSame(Key.NULL));
		key = Key.create(Key.NULL);
		assertThat(key, isSame(Key.NULL));
		key = Key.NULL.parent();
		assertThat(key, isSame(Key.NULL));
		key = Key.create("");
		assertThat(key, isSame(Key.NULL));
		key = Key.create(Key.NULL, "", "");
		assertThat(key, isSame(Key.NULL));
	}


}
