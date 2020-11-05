package ceri.log.rpc.service;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrowable;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.throwIt;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import ceri.log.rpc.test.TestStreamObserver;
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
			assertThrowable(observer.error.awaitAuto(), StatusRuntimeException.class);
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
			assertThrowable(observer.error.awaitAuto(), StatusRuntimeException.class);
		}, Level.OFF, RpcServiceUtil.class);
	}

}
