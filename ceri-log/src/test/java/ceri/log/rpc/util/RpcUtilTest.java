package ceri.log.rpc.util;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrowable;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.text.RegexUtil.finder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.google.protobuf.BytesValue;
import ceri.common.collection.ArrayUtil;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class RpcUtilTest {

	@Test
	public void testBytes() {
		BytesValue value = RpcUtil.bytes(ArrayUtil.bytes(1, 2, 3));
		assertArray(value.getValue().toByteArray(), 1, 2, 3);
	}

	@Test
	public void testInt32Value() {
		assertEquals(RpcUtil.int32(Integer.MAX_VALUE).getValue(), Integer.MAX_VALUE);
		assertEquals(RpcUtil.int32(Integer.MIN_VALUE).getValue(), Integer.MIN_VALUE);
	}

	@Test
	public void testUint32Value() {
		assertEquals(RpcUtil.uint32(Integer.MAX_VALUE).getValue(), Integer.MAX_VALUE);
		assertEquals(RpcUtil.uint32(Integer.MIN_VALUE).getValue(), Integer.MIN_VALUE);
	}

	@Test
	public void testInt64Value() {
		assertEquals(RpcUtil.int64(Long.MAX_VALUE).getValue(), Long.MAX_VALUE);
		assertEquals(RpcUtil.int64(Long.MIN_VALUE).getValue(), Long.MIN_VALUE);
	}

	@Test
	public void testUint64Value() {
		assertEquals(RpcUtil.uint64(Long.MAX_VALUE).getValue(), Long.MAX_VALUE);
		assertEquals(RpcUtil.uint64(Long.MIN_VALUE).getValue(), Long.MIN_VALUE);
	}

	@Test
	public void testBool() {
		assertTrue(RpcUtil.bool(true).getValue());
		assertFalse(RpcUtil.bool(false).getValue());
	}

	@Test
	public void testObserverWithStop() {
		List<String> next = new ArrayList<>();
		List<Throwable> stop = new ArrayList<>();
		IOException e = new IOException("test");
		StreamObserver<String> observer = RpcUtil.observer(next::add, stop::add);
		observer.onNext("test1");
		observer.onNext("test2");
		observer.onCompleted();
		observer.onError(e);
		assertIterable(next, "test1", "test2");
		assertIterable(stop, null, e);
	}

	@Test
	public void testObserver() {
		StreamObserver<String> observer = RpcUtil.observer(null, null, null);
		observer.onNext("test");
		observer.onCompleted();
		observer.onError(new IOException());
	}

	@Test
	public void testTransformObserver() {
		List<Integer> next = new ArrayList<>();
		List<Throwable> stop = new ArrayList<>();
		IOException e = new IOException("test");
		StreamObserver<Integer> iob = RpcUtil.observer(next::add, stop::add);
		StreamObserver<String> sob = RpcUtil.transformObserver(Integer::parseInt, iob);
		sob.onNext("123");
		sob.onNext(null);
		sob.onNext("456");
		sob.onNext("abc");
		sob.onCompleted();
		sob.onError(e);
		assertIterable(next, 123, null, 456);
		assertThrowable(stop.get(0), NumberFormatException.class);
		assertNull(stop.get(1));
		assertEquals(stop.get(2), e);
	}

	@Test
	public void testNullObserver() {
		StreamObserver<String> observer = RpcUtil.nullObserver();
		observer.onNext("test");
		observer.onCompleted();
		observer.onError(new IOException());
	}

	@Test
	public void testCause() {
		assertNull(RpcUtil.cause(null));
		assertThrowable(RpcUtil.cause(new IOException()), IOException.class);
		assertThrowable(RpcUtil.cause(statusException("test", null)), StatusRuntimeException.class);
		assertThrowable(RpcUtil.cause( //
			statusException("test1", statusException("test2", null))), //
			StatusRuntimeException.class, finder("test1"));
		assertThrowable(RpcUtil.cause( //
			statusException("test1", statusException("test2", new IOException("test3")))),
			IOException.class, finder("test3"));
	}

	private static StatusRuntimeException statusException(String message, Throwable cause) {
		return Status.CANCELLED.withDescription(message).withCause(cause).asRuntimeException();
	}

}
