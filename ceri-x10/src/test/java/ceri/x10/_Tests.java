package ceri.x10;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;
import ceri.x10.cm17a.CommandsBehavior;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// cm11a
	// cm11a.protocol
	// cm17a
	CommandsBehavior.class,
	// command
	// type
	// util
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
