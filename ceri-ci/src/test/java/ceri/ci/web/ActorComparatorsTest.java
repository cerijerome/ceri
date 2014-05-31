package ceri.ci.web;

import static ceri.common.test.TestUtil.assertList;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class ActorComparatorsTest {
	private static final Actor a0 = new Actor("n0", "b0", "j0");
	private static final Actor a1 = new Actor("n1", "b0", "j0");
	private static final Actor a2 = new Actor("n0", "b0", "j1");
	private static final Actor a3 = new Actor("n0", "b1", "j0");
	private static final Actor a4 = new Actor("n1", "b1", "j1");

	@Test
	public void testForPrivateConstructor() {
		assertPrivateConstructor(ActorComparators.class);
	}

	@Test
	public void testDefaultComparator() {
		List<Actor> actors = new ArrayList<>();
		Collections.addAll(actors, a4, a2, a1, a3, a0);
		Collections.sort(actors, ActorComparators.DEFAULT);
		assertList(actors, Arrays.asList(a0, a1, a2, a3, a4));
	}

}
