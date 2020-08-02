package ceri.log;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;
import ceri.log.concurrent.CloseableExecutorBehavior;
import ceri.log.concurrent.LoopingExecutorBehavior;
import ceri.log.concurrent.ShutdownListenerBehavior;
import ceri.log.concurrent.SocketListenerBehavior;
import ceri.log.io.LogPrintStreamBehavior;
import ceri.log.io.SelfHealingSocketBehavior;
import ceri.log.rpc.RpcBehavior;
import ceri.log.rpc.client.RpcChannelBehavior;
import ceri.log.rpc.client.RpcChannelConfigBehavior;
import ceri.log.rpc.client.RpcClientNotifierBehavior;
import ceri.log.rpc.client.RpcClientNotifierConfigBehavior;
import ceri.log.rpc.client.RpcClientUtilTest;
import ceri.log.rpc.service.RpcServerBehavior;
import ceri.log.rpc.service.RpcServerConfigBehavior;
import ceri.log.rpc.service.RpcServiceNotifierBehavior;
import ceri.log.rpc.service.RpcServiceUtilTest;
import ceri.log.rpc.test.TestObserverBehavior;
import ceri.log.rpc.util.RpcStreamerBehavior;
import ceri.log.rpc.util.RpcUtilTest;
import ceri.log.test.ContainerTestHelperBehavior;
import ceri.log.test.LogModifierBehavior;
import ceri.log.util.LogUtilTest;

/**
 * Generated test suite for ceri-log
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// concurrent
	CloseableExecutorBehavior.class, //
	LoopingExecutorBehavior.class, //
	ShutdownListenerBehavior.class, //
	SocketListenerBehavior.class, //
	// io
	LogPrintStreamBehavior.class, //
	SelfHealingSocketBehavior.class, //
	// rpc
	RpcBehavior.class, //
	// rpc.client
	RpcChannelBehavior.class, //
	RpcChannelConfigBehavior.class, //
	RpcClientNotifierBehavior.class, //
	RpcClientNotifierConfigBehavior.class, //
	RpcClientUtilTest.class, //
	// rpc.service
	RpcServerBehavior.class, //
	RpcServerConfigBehavior.class, //
	RpcServiceNotifierBehavior.class, //
	RpcServiceUtilTest.class, //
	// rpc.test
	TestObserverBehavior.class, //
	// rpc.util
	RpcStreamerBehavior.class, //
	RpcUtilTest.class, //
	// test
	ContainerTestHelperBehavior.class, //
	LogModifierBehavior.class, //
	// util
	LogUtilTest.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
