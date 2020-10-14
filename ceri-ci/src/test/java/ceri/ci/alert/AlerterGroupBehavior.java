package ceri.ci.alert;

import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.ci.build.Builds;
import ceri.ci.common.Alerter;
import ceri.ci.common.TestAlerter;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.ValueCondition;

public class AlerterGroupBehavior {
	ValueCondition<Integer> sync;
	private AlerterGroup alertGroup;

	@Before
	public void init() {
		sync = ValueCondition.of();
		Alerter alerter0 = new TestAlerter() {
			@Override
			protected void common() {
				sync.signal(1);
				await(sync, 2);
				sync.signal(3);
			}
		};
		Alerter alerter1 = new TestAlerter() {
			@Override
			protected void common() {
				await(sync, 1);
				sync.signal(2);
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
		assertThat(sync.value(), is(3));
	}

	@Test
	public void shouldExecuteClearInParallel() {
		alertGroup.clear();
		assertThat(sync.value(), is(3));
	}

	@Test
	public void shouldExecuteRemindInParallel() {
		alertGroup.remind();
		assertThat(sync.value(), is(3));
	}

	private static void await(ValueCondition<Integer> sync, int i) {
		ConcurrentUtil.executeInterruptible(() -> sync.await(i));
	}
}
