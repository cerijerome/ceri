package ceri.ci.web;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static ceri.common.test.TestUtil.assertThat;
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
		assertThat(actor.hashCode(), is(actor2.hashCode()));
		assertThat(actor.toString(), is(actor2.toString()));
		assertThat(actor.compareTo(actor2), is(0));
	}

}
