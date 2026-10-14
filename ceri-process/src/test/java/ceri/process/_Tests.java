package ceri.process;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import ceri.common.test.Testing;

/**
 * Generated test suite for ceri-process
 */
@Suite
@SelectClasses({
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
		Testing.print(_Tests.class);
	}
}
