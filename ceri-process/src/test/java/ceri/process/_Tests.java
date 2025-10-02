package ceri.process;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;

/**
 * Generated test suite for ceri-process
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// nmcli
	ceri.process.nmcli.NmcliBehavior.class, //
	// scutil
	ceri.process.scutil.ParserBehavior.class, //
	ceri.process.scutil.ScUtilBehavior.class, //
	// uptime
	ceri.process.uptime.UptimeBehavior.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
