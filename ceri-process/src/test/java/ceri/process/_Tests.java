package ceri.process;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;
import ceri.process.nmcli.ConShowIdResultBehavior;
import ceri.process.nmcli.ConShowItemBehavior;
import ceri.process.nmcli.NmcliBehavior;
import ceri.process.scutil.NcListItemBehavior;
import ceri.process.scutil.NcShowBehavior;
import ceri.process.scutil.NcStatisticsBehavior;
import ceri.process.scutil.NcStatusBehavior;
import ceri.process.scutil.ParserBehavior;
import ceri.process.scutil.ScUtilBehavior;
import ceri.process.uptime.UptimeBehavior;

/**
 * Generated test suite for ceri-process
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// nmcli
	ConShowIdResultBehavior.class, //
	ConShowItemBehavior.class, //
	NmcliBehavior.class, //
	// scutil
	NcListItemBehavior.class, //
	NcShowBehavior.class, //
	NcStatisticsBehavior.class, //
	NcStatusBehavior.class, //
	ParserBehavior.class, //
	ScUtilBehavior.class, //
	// uptime
	UptimeBehavior.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
