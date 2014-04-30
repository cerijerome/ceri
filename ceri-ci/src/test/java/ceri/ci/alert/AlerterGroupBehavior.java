package ceri.ci.alert;

import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.io.IOException;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import ceri.ci.build.BuildTestUtil;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import ceri.ci.common.Alerter;
import ceri.ci.common.TestAlerter;
import ceri.ci.x10.X10Alerter;
import ceri.ci.zwave.TestZWaveAlerter;
import ceri.ci.zwave.ZWaveAlerter;
import ceri.ci.zwave.ZWaveController;
import ceri.common.test.TestState;
import ceri.common.util.BasicUtil;
import ceri.x10.util.X10Controller;

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
		alerter0 = new TestAlerter() {
			@Override
			protected void common() {
				state.waitFor(1);
				state.set(2);
			}
		};
		alertGroup =
			AlerterGroup.builder().alerters(alerter0, alerter1).shutdownTimeoutMs(1).build();
	}

	@Test
	public void shouldExecuteUpdateInParallel() {
		alertGroup.update(new Builds());
		assertThat(state.get(), is(3));
	}

	@Test
	public void shouldExecuteClearInParallel() {
		alertGroup.update(new Builds());
		assertThat(state.get(), is(3));
	}

	@Test
	public void shouldExecuteRemindInParallel() {
		alertGroup.update(new Builds());
		assertThat(state.get(), is(3));
	}

}
