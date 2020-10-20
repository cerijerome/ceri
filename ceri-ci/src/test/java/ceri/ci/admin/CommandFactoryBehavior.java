package ceri.ci.admin;

import static ceri.common.test.AssertUtil.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.ci.admin.Params.BuildJob;
import ceri.ci.alert.AlertService;
import ceri.ci.build.Build;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.Builds;
import ceri.ci.build.Job;

public class CommandFactoryBehavior {
	@Mock
	private Serializer serializer;
	@Mock
	private Params params;
	@Mock
	private AlertService service;
	@Mock
	private Builds builds;
	@Mock
	private Build build;
	@Mock
	private Job job;
	private CommandFactory factory;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		factory = new CommandFactory(serializer);
		when(service.builds()).thenReturn(builds);
		when(service.build(any())).thenReturn(build);
		when(service.job(any(), any())).thenReturn(job);
	}

	@Test
	public void shouldCreateProcessCommands() throws Exception {
		Collection<BuildEvent> buildEvents = Collections.emptyList();
		when(params.action()).thenReturn(Action.process);
		when(params.buildEvents()).thenReturn(buildEvents);
		Command command = factory.create(params);
		Response response = command.execute(service);
		verify(service).process(buildEvents);
		assertTrue(response.success);
	}

	@Test
	public void shouldCreateViewCommandForBlankBuildAndJob() throws Exception {
		BuildJob buildJob = new BuildJob(null, null);
		when(params.action()).thenReturn(Action.view);
		when(params.buildJob()).thenReturn(buildJob);
		Command command = factory.create(params);
		Response response = command.execute(service);
		verify(serializer).fromBuilds(builds);
		assertTrue(response.success);
	}

	@Test
	public void shouldCreateViewCommandForBuild() throws Exception {
		BuildJob buildJob = new BuildJob("b0", null);
		when(params.action()).thenReturn(Action.view);
		when(params.buildJob()).thenReturn(buildJob);
		Command command = factory.create(params);
		Response response = command.execute(service);
		verify(serializer).fromBuild(build);
		assertTrue(response.success);
	}

	@Test
	public void shouldCreateViewCommandForBuildAndJob() throws Exception {
		BuildJob buildJob = new BuildJob("b0", "j0");
		when(params.action()).thenReturn(Action.view);
		when(params.buildJob()).thenReturn(buildJob);
		Command command = factory.create(params);
		Response response = command.execute(service);
		verify(serializer).fromJob(job);
		assertTrue(response.success);
	}

	@Test
	public void shouldCreatePurgeCommands() throws Exception {
		when(params.action()).thenReturn(Action.purge);
		Command command = factory.create(params);
		Response response = command.execute(service);
		verify(service).purge();
		assertTrue(response.success);
	}

	@Test
	public void shouldCreateDeleteCommands() throws Exception {
		BuildJob buildJob = new BuildJob("b0", null);
		when(params.action()).thenReturn(Action.delete);
		when(params.buildJob()).thenReturn(buildJob);
		Command command = factory.create(params);
		Response response = command.execute(service);
		verify(service).delete("b0", null);
		assertTrue(response.success);
	}

	@Test
	public void shouldCreateClearCommands() throws Exception {
		BuildJob buildJob = new BuildJob("b0", "j0");
		when(params.action()).thenReturn(Action.clear);
		when(params.buildJob()).thenReturn(buildJob);
		Command command = factory.create(params);
		Response response = command.execute(service);
		verify(service).clear("b0", "j0");
		assertTrue(response.success);
	}

}
