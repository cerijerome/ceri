package ceri.ci.web;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import org.junit.Test;

public class ActorBehavior {

	@Test
	public void shouldConformToEqualsContract() {
		Actor actor = new Actor("name", "build", "job");
		assertNotEquals(null, actor);
		assertNotEquals(actor, new Actor("name0", "build", "job"));
		assertNotEquals(actor, new Actor("name", "build0", "job"));
		assertNotEquals(actor, new Actor("name", "build", "job0"));
		assertEquals(actor, actor);
		Actor actor2 = new Actor("name", "build", "job");
		assertEquals(actor, actor2);
		assertEquals(actor.hashCode(), actor2.hashCode());
		assertEquals(actor.toString(), actor2.toString());
		assertEquals(actor.compareTo(actor2), 0);
	}

}
