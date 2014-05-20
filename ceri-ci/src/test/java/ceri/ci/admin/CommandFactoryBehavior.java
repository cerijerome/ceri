package ceri.ci.admin;

import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.ci.admin.Params.BuildJob;
import ceri.ci.alert.AlertService;

public class CommandFactoryBehavior {
	@Mock private Serializer serializer;
	@Mock private Params params;
	@Mock private AlertService service;
	private CommandFactory factory;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		factory = new CommandFactory(serializer);
	}

	@Test
	public void shouldCreateViewCommands() throws Exception {
		BuildJob buildJob = new BuildJob(null, null);
		when(params.action()).thenReturn(Action.view);
		when(params.buildJob()).thenReturn(buildJob);
		Command command = factory.create(params);
		Response response = command.execute(service);
		verify(serializer).fromBuilds(null);
		assertThat(response.success, is(true));
	}

	@Test
	public void shouldCreatePurgeCommands() throws Exception {
		when(params.action()).thenReturn(Action.purge);
		Command command = factory.create(params);
		Response response = command.execute(service);
		verify(service).purge();
		assertThat(response.success, is(true));
	}

	@Test
	public void shouldCreateDeleteCommands() throws Exception {
		BuildJob buildJob = new BuildJob("b0", null);
		when(params.action()).thenReturn(Action.delete);
		when(params.buildJob()).thenReturn(buildJob);
		Command command = factory.create(params);
		Response response = command.execute(service);
		verify(service).delete("b0", null);
		assertThat(response.success, is(true));
	}

	@Test
	public void shouldCreateClearCommands() throws Exception {
		BuildJob buildJob = new BuildJob("b0", "j0");
		when(params.action()).thenReturn(Action.clear);
		when(params.buildJob()).thenReturn(buildJob);
		Command command = factory.create(params);
		Response response = command.execute(service);
		verify(service).clear("b0", "j0");
		assertThat(response.success, is(true));
	}

}
