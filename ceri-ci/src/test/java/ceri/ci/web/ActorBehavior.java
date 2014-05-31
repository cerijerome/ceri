package ceri.ci.web;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ActorBehavior {

	@Test
	public void shouldConformToEqualsContract() {
		Actor actor = new Actor("name",  "build",  "job");
		assertFalse(actor.equals(null));
		assertFalse(actor.equals(new Actor("name0",  "build",  "job")));
		assertFalse(actor.equals(new Actor("name",  "build0",  "job")));
		assertFalse(actor.equals(new Actor("name",  "build",  "job0")));
		assertTrue(actor.equals(actor));
		Actor actor2 = new Actor("name",  "build",  "job");
		assertTrue(actor.equals(actor2));
		assertThat(actor.hashCode(), is(actor2.hashCode()));
		assertThat(actor.toString(), is(actor2.toString()));
		assertThat(actor.compareTo(actor2), is(0));
	}

}
