package ceri.log;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;

/**
 * Generated test suite for ceri-log
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// concurrent
	ceri.log.concurrent.CloseableExecutorBehavior.class, //
	ceri.log.concurrent.DispatcherBehavior.class, //
	ceri.log.concurrent.LoopingExecutorBehavior.class, //
	ceri.log.concurrent.ShutdownListenerBehavior.class, //
	ceri.log.concurrent.SocketListenerBehavior.class, //
	// io
	ceri.log.io.LogPrintStreamBehavior.class, //
	ceri.log.io.SelfHealingSocketConfigBehavior.class, //
	ceri.log.io.SelfHealingSocketConnectorBehavior.class, //
	ceri.log.io.SocketConnectorBehavior.class, //
	ceri.log.io.SocketParamsBehavior.class, //
	// io.test
	ceri.log.io.test.EchoServerSocketBehavior.class, //
	ceri.log.io.test.TestSocketConnectorBehavior.class, //
	// rpc
	ceri.log.rpc.RpcBehavior.class, //
	// rpc.client
	ceri.log.rpc.client.RpcChannelBehavior.class, //
	ceri.log.rpc.client.RpcChannelConfigBehavior.class, //
	ceri.log.rpc.client.RpcClientNotifierBehavior.class, //
	ceri.log.rpc.client.RpcClientNotifierConfigBehavior.class, //
	ceri.log.rpc.client.RpcClientUtilTest.class, //
	// rpc.service
	ceri.log.rpc.service.RpcServerBehavior.class, //
	ceri.log.rpc.service.RpcServerConfigBehavior.class, //
	ceri.log.rpc.service.RpcServiceNotifierBehavior.class, //
	ceri.log.rpc.service.RpcServiceUtilTest.class, //
	// rpc.test
	ceri.log.rpc.test.TestObserverBehavior.class, //
	// rpc.util
	ceri.log.rpc.util.RpcStreamerBehavior.class, //
	ceri.log.rpc.util.RpcUtilTest.class, //
	// test
	ceri.log.test.ContainerTestHelperBehavior.class, //
	ceri.log.test.LogModifierBehavior.class, //
	// util
	ceri.log.util.LogUtilTest.class, //
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
