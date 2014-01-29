package ceri.ci.service;

import static ceri.ci.build.BuildTestUtil.assertBuildNames;
import static ceri.ci.build.BuildTestUtil.assertEvents;
import static ceri.ci.build.BuildTestUtil.assertJobNames;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ceri.ci.alert.Alerters;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;

public class CiAlertServiceImplBehavior {

	@Test
	@SuppressWarnings("resource")
	public void should() {
		Alerters alerters = Mockito.mock(Alerters.class);
		CiAlertServiceImpl ci = new CiAlertServiceImpl(alerters, TimeUnit.MINUTES.toMillis(30) );
		ci.broken("b0", "j0", Arrays.asList("n0", "n1"));
		Builds builds = captureProcessAlertBuilds(alerters);
		assertBuildNames(builds.builds, "b0");
		assertJobNames(builds.build("b0").jobs, "j0");
		assertThat(builds.build("b0").job("j0").events.size(), is(1));
		assertEvents(builds.build("b0").job("j0").events, Event.broken("n0", "n1"));
	}

	private Builds captureProcessAlertBuilds(Alerters alerters) {
		ArgumentCaptor<Builds> argument = ArgumentCaptor.forClass(Builds.class);
		verify(alerters).alert(argument.capture());
		return argument.getValue();
	}

}
