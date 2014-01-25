package ceri.ci;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.ci.build.BuildBehavior;
import ceri.ci.build.BuildUtilTest;
import ceri.ci.build.BuildsBehavior;
import ceri.ci.build.EventBehavior;
import ceri.ci.build.EventComparatorsTest;
import ceri.ci.build.JobBehavior;
import ceri.ci.x10.X10AlerterBehavior;
import ceri.ci.zwave.ZWaveAlerterBehavior;
import ceri.common.test.TestUtil;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// build.event
	BuildBehavior.class,
	BuildsBehavior.class,
	BuildUtilTest.class,
	EventBehavior.class,
	EventComparatorsTest.class,
	JobBehavior.class,
	//SummaryBehavior.class,
	// x10
	X10AlerterBehavior.class,
	// zwave
	ZWaveAlerterBehavior.class,
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
