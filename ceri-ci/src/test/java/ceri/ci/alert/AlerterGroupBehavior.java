package ceri.ci.alert;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.ci.build.Builds;
import ceri.ci.common.Alerter;
import ceri.ci.common.TestAlerter;
import ceri.common.test.TestState;

public class AlerterGroupBehavior {
	TestState<Integer> state = new TestState<>();
	private Alerter alerter0;
	private Alerter alerter1;
	private AlerterGroup alertGroup;

	@Before
	public void init() {
		state = new TestState<>();
		alerter0 = new TestAlerter() {
			@Override
			protected void common() {
				state.set(1);
				state.waitFor(2);
				state.set(3);
			}
		};
		alerter1 = new TestAlerter() {
			@Override
			protected void common() {
				state.waitFor(1);
				state.set(2);
			}
		};
		alertGroup =
			AlerterGroup.builder().alerters(alerter0, alerter1).shutdownTimeoutMs(1).build();
	}

	@After
	public void end() {
		alertGroup.close();
	}
	
	@Test
	public void shouldExecuteUpdateInParallel() {
		alertGroup.update(new Builds());
		assertThat(state.get(), is(3));
	}

	@Test
	public void shouldExecuteClearInParallel() {
		alertGroup.clear();
		assertThat(state.get(), is(3));
	}

	@Test
	public void shouldExecuteRemindInParallel() {
		alertGroup.remind();
		assertThat(state.get(), is(3));
	}

}
