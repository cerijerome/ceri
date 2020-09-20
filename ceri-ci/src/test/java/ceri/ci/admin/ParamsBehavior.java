package ceri.ci.admin;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.ci.admin.Params.BuildJob;
import ceri.ci.build.BuildEvent;

public class ParamsBehavior {
	@Mock
	private HttpServletRequest request;
	@Mock
	private Serializer serializer;
	private Params params;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		params = new Params(request, serializer);
	}

	@Test
	public void shouldParseEventsFromRequestParameter() {
		assertTrue(params.buildEvents().isEmpty());
		@SuppressWarnings("serial")
		Collection<BuildEvent> events = new ArrayList<>() {};
		when(request.getParameter("events")).thenReturn("test");
		when(serializer.toBuildEvents("test")).thenReturn(events);
		assertThat(params.buildEvents(), is(events));
	}

	@Test
	public void shouldParseBuildAndJobFromRequestPathInfo() {
		BuildJob buildJob = params.buildJob();
		assertThat(buildJob.build, is((String) null));
		assertThat(buildJob.job, is((String) null));
		when(request.getPathInfo()).thenReturn("/b0/");
		buildJob = params.buildJob();
		assertThat(buildJob.build, is("b0"));
		assertThat(buildJob.job, is((String) null));
		when(request.getPathInfo()).thenReturn("/b0/j0");
		buildJob = params.buildJob();
		assertThat(buildJob.build, is("b0"));
		assertThat(buildJob.job, is("j0"));
	}

	@Test
	public void shouldParseActionFromRequestParameter() {
		Params params = new Params(request, new Serializer());
		when(request.getParameter(Action.clear.name())).thenReturn("");
		assertThat(params.action(), is(Action.clear));
	}

	@Test
	public void shouldParseViewAsDefaultAction() {
		Params params = new Params(request, new Serializer());
		assertThat(params.action(), is(Action.view));
	}

}
