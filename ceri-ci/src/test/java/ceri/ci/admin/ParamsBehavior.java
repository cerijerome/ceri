package ceri.ci.admin;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
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
		assertEquals(params.buildEvents(), events);
	}

	@Test
	public void shouldParseBuildAndJobFromRequestPathInfo() {
		BuildJob buildJob = params.buildJob();
		assertEquals(buildJob.build, (String) null);
		assertEquals(buildJob.job, (String) null);
		when(request.getPathInfo()).thenReturn("/b0/");
		buildJob = params.buildJob();
		assertEquals(buildJob.build, "b0");
		assertEquals(buildJob.job, (String) null);
		when(request.getPathInfo()).thenReturn("/b0/j0");
		buildJob = params.buildJob();
		assertEquals(buildJob.build, "b0");
		assertEquals(buildJob.job, "j0");
	}

	@Test
	public void shouldParseActionFromRequestParameter() {
		Params params = new Params(request, new Serializer());
		when(request.getParameter(Action.clear.name())).thenReturn("");
		assertEquals(params.action(), Action.clear);
	}

	@Test
	public void shouldParseViewAsDefaultAction() {
		Params params = new Params(request, new Serializer());
		assertEquals(params.action(), Action.view);
	}

}
