package ceri.log;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;
import ceri.log.binary.BinaryLogInputStreamBehavior;
import ceri.log.binary.BinaryLogOutputStreamBehavior;
import ceri.log.binary.BinaryPrinterBehavior;
import ceri.log.concurrent.LoopingExecutorBehavior;
import ceri.log.concurrent.SocketListenerBehavior;
import ceri.log.util.LogUtilTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// binary
	BinaryLogInputStreamBehavior.class,
	BinaryLogOutputStreamBehavior.class,
	BinaryPrinterBehavior.class,
	// concurrent
	LoopingExecutorBehavior.class,
	SocketListenerBehavior.class,
	// util
	LogUtilTest.class, })
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
