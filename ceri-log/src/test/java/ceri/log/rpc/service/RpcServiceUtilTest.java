package ceri.log.rpc.service;

import static ceri.common.test.TestUtil.throwIt;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import ceri.common.util.BasicUtil;
import ceri.log.test.LogModifier;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class RpcServiceUtilTest {

	@Test
	public void testStatusExceptionPassThrough() {
		StatusRuntimeException e = Status.CANCELLED.withDescription("test").asRuntimeException();
		assertThat(RpcServiceUtil.statusException(e), is(e));
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
		StreamObserver<String> observer = BasicUtil.uncheckedCast(mock(StreamObserver.class));
		RpcServiceUtil.respond(observer, () -> "test");
		verify(observer).onNext("test");
		verify(observer).onCompleted();
	}

	@Test
	public void testRespondSupplierWithException() {
		LogModifier.run(() -> {
			StreamObserver<String> observer = BasicUtil.uncheckedCast(mock(StreamObserver.class));
			IOException e = new IOException("test");
			RpcServiceUtil.respond(observer, () -> throwIt(e));
			verify(observer).onError(any(StatusRuntimeException.class));
		}, Level.OFF, RpcServiceUtil.class);
	}

	@Test
	public void testRespondRunnable() {
		StreamObserver<String> observer = BasicUtil.uncheckedCast(mock(StreamObserver.class));
		RpcServiceUtil.respond(observer, "test", () -> {});
		verify(observer).onNext("test");
		verify(observer).onCompleted();
	}

	@Test
	public void testRespondRunnableWithException() {
		LogModifier.run(() -> {
			StreamObserver<String> observer = BasicUtil.uncheckedCast(mock(StreamObserver.class));
			IOException e = new IOException("test");
			RpcServiceUtil.respond(observer, "test", () -> throwIt(e));
			verify(observer).onError(any(StatusRuntimeException.class));
		}, Level.OFF, RpcServiceUtil.class);
	}

}
