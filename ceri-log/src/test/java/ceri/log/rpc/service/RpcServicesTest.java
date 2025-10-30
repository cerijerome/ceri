package ceri.log.rpc.service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import com.google.protobuf.Empty;
import ceri.common.test.Assert;
import ceri.log.rpc.test.TestStreamObserver;
import ceri.log.rpc.util.Rpc;
import ceri.log.test.LogModifier;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class RpcServicesTest {

	@Test
	public void testStatusExceptionPassThrough() {
		var e = Status.CANCELLED.withDescription("test").asRuntimeException();
		Assert.equal(RpcServices.statusException(e), e);
	}

	@Test
	public void testStatusExceptionConvert() {
		var iox = new IOException("i/o exception");
		var e = RpcServices.statusException(iox);
		Assert.yes(e.getMessage().contains(iox.getMessage()));
		Assert.yes(e.getMessage().contains(iox.getClass().getName()));
	}

	@Test
	public void testStatusExceptionWithLongMessage() {
		var msg = "abcdefghijklm".repeat(10);
		var iox = new IOException(msg);
		var e = RpcServices.statusException(iox);
		Assert.no(e.getMessage().contains(iox.getMessage()));
		Assert.yes(e.getMessage().contains(iox.getMessage().substring(128)));
		Assert.yes(e.getMessage().contains(iox.getClass().getName()));
	}

	@Test
	public void testAcceptRunnable() {
		var observer = TestStreamObserver.<Empty>of();
		RpcServices.accept(observer, () -> {});
		observer.next.assertAuto(Rpc.EMPTY);
		observer.completed.awaitAuto();
	}

	@Test
	public void testRespondSupplier() {
		var observer = TestStreamObserver.<String>of();
		RpcServices.respond(observer, () -> "test");
		observer.next.assertAuto("test");
		observer.completed.awaitAuto();
	}

	@Test
	public void testRespondSupplierWithException() {
		LogModifier.run(() -> {
			var observer = TestStreamObserver.<String>of();
			var e = new IOException("test");
			RpcServices.respond(observer, () -> Assert.throwIt(e));
			Assert.throwable(observer.error.awaitAuto(), StatusRuntimeException.class);
		}, Level.OFF, RpcServices.class);
	}

	@Test
	public void testRespondRunnable() {
		var observer = TestStreamObserver.<String>of();
		RpcServices.respond(observer, "test", () -> {});
		observer.next.assertAuto("test");
		observer.completed.awaitAuto();
	}

	@Test
	public void testRespondRunnableWithException() {
		LogModifier.run(() -> {
			var observer = TestStreamObserver.<String>of();
			var e = new IOException("test");
			RpcServices.respond(observer, "test", () -> Assert.throwIt(e));
			Assert.throwable(observer.error.awaitAuto(), StatusRuntimeException.class);
		}, Level.OFF, RpcServices.class);
	}

	@Test
	public void testNullServer() throws InterruptedException, IOException {
		RpcServices.NULL_SERVER.awaitTermination();
		Assert.yes(RpcServices.NULL_SERVER.awaitTermination(1, TimeUnit.MILLISECONDS));
		Assert.no(RpcServices.NULL_SERVER.isShutdown());
		Assert.no(RpcServices.NULL_SERVER.isTerminated());
		Assert.equal(RpcServices.NULL_SERVER.shutdown(), RpcServices.NULL_SERVER);
		Assert.equal(RpcServices.NULL_SERVER.shutdownNow(), RpcServices.NULL_SERVER);
		Assert.equal(RpcServices.NULL_SERVER.start(), RpcServices.NULL_SERVER);
	}
}
