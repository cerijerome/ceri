package ceri.log.rpc.service;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertTrue;
import static ceri.common.test.Assert.throwIt;
import static ceri.common.test.Assert.throwable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import com.google.protobuf.Empty;
import ceri.log.rpc.test.TestStreamObserver;
import ceri.log.rpc.util.RpcUtil;
import ceri.log.test.LogModifier;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class RpcServiceUtilTest {

	@Test
	public void testStatusExceptionPassThrough() {
		StatusRuntimeException e = Status.CANCELLED.withDescription("test").asRuntimeException();
		assertEquals(RpcServiceUtil.statusException(e), e);
	}

	@Test
	public void testStatusExceptionConvert() {
		IOException iox = new IOException("i/o exception");
		StatusRuntimeException e = RpcServiceUtil.statusException(iox);
		assertTrue(e.getMessage().contains(iox.getMessage()));
		assertTrue(e.getMessage().contains(iox.getClass().getName()));
	}

	@Test
	public void testStatusExceptionWithLongMessage() {
		String msg = "abcdefghijklm".repeat(10);
		IOException iox = new IOException(msg);
		StatusRuntimeException e = RpcServiceUtil.statusException(iox);
		assertFalse(e.getMessage().contains(iox.getMessage()));
		assertTrue(e.getMessage().contains(iox.getMessage().substring(128)));
		assertTrue(e.getMessage().contains(iox.getClass().getName()));
	}

	@Test
	public void testAcceptRunnable() {
		TestStreamObserver<Empty> observer = TestStreamObserver.of();
		RpcServiceUtil.accept(observer, () -> {});
		observer.next.assertAuto(RpcUtil.EMPTY);
		observer.completed.awaitAuto();
	}

	@Test
	public void testRespondSupplier() {
		TestStreamObserver<String> observer = TestStreamObserver.of();
		RpcServiceUtil.respond(observer, () -> "test");
		observer.next.assertAuto("test");
		observer.completed.awaitAuto();
	}

	@Test
	public void testRespondSupplierWithException() {
		LogModifier.run(() -> {
			TestStreamObserver<String> observer = TestStreamObserver.of();
			IOException e = new IOException("test");
			RpcServiceUtil.respond(observer, () -> throwIt(e));
			throwable(observer.error.awaitAuto(), StatusRuntimeException.class);
		}, Level.OFF, RpcServiceUtil.class);
	}

	@Test
	public void testRespondRunnable() {
		TestStreamObserver<String> observer = TestStreamObserver.of();
		RpcServiceUtil.respond(observer, "test", () -> {});
		observer.next.assertAuto("test");
		observer.completed.awaitAuto();
	}

	@Test
	public void testRespondRunnableWithException() {
		LogModifier.run(() -> {
			TestStreamObserver<String> observer = TestStreamObserver.of();
			IOException e = new IOException("test");
			RpcServiceUtil.respond(observer, "test", () -> throwIt(e));
			throwable(observer.error.awaitAuto(), StatusRuntimeException.class);
		}, Level.OFF, RpcServiceUtil.class);
	}

	@Test
	public void testNullServer() throws InterruptedException, IOException {
		RpcServiceUtil.NULL_SERVER.awaitTermination();
		assertTrue(RpcServiceUtil.NULL_SERVER.awaitTermination(1, TimeUnit.MILLISECONDS));
		assertFalse(RpcServiceUtil.NULL_SERVER.isShutdown());
		assertFalse(RpcServiceUtil.NULL_SERVER.isTerminated());
		assertEquals(RpcServiceUtil.NULL_SERVER.shutdown(), RpcServiceUtil.NULL_SERVER);
		assertEquals(RpcServiceUtil.NULL_SERVER.shutdownNow(), RpcServiceUtil.NULL_SERVER);
		assertEquals(RpcServiceUtil.NULL_SERVER.start(), RpcServiceUtil.NULL_SERVER);
	}

}
