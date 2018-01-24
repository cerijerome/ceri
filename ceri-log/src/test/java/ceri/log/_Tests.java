package ceri.log;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;
import ceri.log.concurrent.LoopingExecutorBehavior;
import ceri.log.concurrent.SocketListenerBehavior;
import ceri.log.util.LogUtilTest;

/**
 * Tests for ceri-log generated 2018-01-23
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// concurrent
	LoopingExecutorBehavior.class, //
	SocketListenerBehavior.class, //
	// util
	LogUtilTest.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
