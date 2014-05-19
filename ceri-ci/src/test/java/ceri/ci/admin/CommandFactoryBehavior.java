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
	public void should() throws Exception {
		BuildJob buildJob = new BuildJob("b0", "j0");
		when(params.action()).thenReturn(Action.clear);
		when(params.buildJob()).thenReturn(buildJob);
		Command command = factory.create(params);
		Response response = command.execute(service);
		verify(service).clear("b0", "j0");
		assertThat(response.success, is(true));
	}

}
