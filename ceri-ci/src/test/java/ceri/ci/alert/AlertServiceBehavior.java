package ceri.ci.alert;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.ci.build.Builds;
import ceri.common.concurrent.BooleanCondition;

public class AlertServiceBehavior {
	Alerters alerters;
	BooleanCondition sync;
	AlertService service;
	
	@Before
	public void init() {
		alerters = mock(Alerters.class);
		sync = new BooleanCondition();
		service = new AlertService(createAlerters(), 0, 1000);
	}
	
	@After
	public void close() throws IOException {
		service.close();
	}
	
	@Test
	public void shouldClearAll() throws InterruptedException {
		service.clear();
		sync.await();
		verify(alerters).clear();
	}

	@Test
	public void shouldClearBuild() throws InterruptedException {
		service.clear("b0");
		sync.await();
		verify(alerters).alert((Builds)any());
	}

	@Test
	public void shouldClearJob() throws InterruptedException  {
		service.clear("b0", "j0");
		sync.await();
		verify(alerters).alert((Builds)any());
	}

	@Test
	public void shouldBreak() throws InterruptedException  {
		service.broken("b0", "j0", "n000");
		sync.await();
		verify(alerters).alert((Builds)any());
	}

	@Test
	public void shouldFix() throws InterruptedException  {
		service.fixed("b0", "j0", "n000");
		sync.await();
		verify(alerters).alert((Builds)any());
	}

	@Test
	public void shouldPurge()  {
		service.purge();
		verifyZeroInteractions(alerters);
	}

	private Alerters createAlerters() {
		return new Alerters(null, null, null, null) {
			@Override
			public void alert(Builds builds) {
				alerters.alert(builds);
				sync.signal();
			}
			@Override
			public void clear() {
				alerters.clear();
				sync.signal();
			}
			@Override
			public void remind() {
				alerters.remind();
				sync.signal();
			}
		};
	}
	
}
