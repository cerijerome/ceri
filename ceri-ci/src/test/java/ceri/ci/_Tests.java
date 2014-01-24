package ceri.ci;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.ci.build.EventBehavior;
import ceri.ci.build.SummaryBehavior;
import ceri.ci.x10.X10AlerterBehavior;
import ceri.common.test.TestUtil;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// build.event
	EventBehavior.class,
	SummaryBehavior.class,
	// x10
	X10AlerterBehavior.class,
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
