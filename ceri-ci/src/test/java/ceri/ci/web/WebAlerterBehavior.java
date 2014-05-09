package ceri.ci.web;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import ceri.ci.build.BuildTestUtil;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;

public class WebAlerterBehavior {
	private static final Event broken = BuildTestUtil.event(Event.Type.failure, 1, "broken");
	private static final Event fixed = BuildTestUtil.event(Event.Type.success, 2, "fixed");
	private WebAlerter webAlerter;
	
	@Before
	public void init() {
		webAlerter = new WebAlerter();
	}

	
	@Test
	public void shouldReturnUpdateBuildsSnapshot() {
		Builds builds = new Builds();
		builds.build("build0").job("job0").events(broken);
		Builds refB0 = new Builds(builds);
		webAlerter.update(builds);
		Builds b0 = webAlerter.builds();
		builds.build("build0").job("job0").events(fixed);
		Builds refB1 = new Builds(builds);
		webAlerter.update(builds);
		Builds b1 = webAlerter.builds();
		assertThat(b0, is(refB0));
		assertThat(b1, is(refB1));
	}


}
