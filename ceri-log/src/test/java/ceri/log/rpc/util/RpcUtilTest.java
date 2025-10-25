package ceri.log.rpc.util;

import static ceri.common.test.Assert.assertArray;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertFind;
import static ceri.common.test.Assert.assertOrdered;
import static ceri.common.test.Assert.assertTrue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.google.protobuf.BytesValue;
import ceri.common.array.ArrayUtil;
import ceri.common.collect.Lists;
import ceri.common.test.Assert;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class RpcUtilTest {

	@Test
	public void testBytes() {
		BytesValue value = RpcUtil.bytes(ArrayUtil.bytes.of(1, 2, 3));
		assertArray(value.getValue().toByteArray(), 1, 2, 3);
	}

	@Test
	public void testInt32Value() {
		assertEquals(RpcUtil.int32(Integer.MAX_VALUE).getValue(), Integer.MAX_VALUE);
		assertEquals(RpcUtil.int32(Integer.MIN_VALUE).getValue(), Integer.MIN_VALUE);
		assertEquals(RpcUtil.int32(true).getValue(), 1);
		assertEquals(RpcUtil.int32(false).getValue(), 0);
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
	public void testStringValue() {
		assertEquals(RpcUtil.string(null), null);
		assertEquals(RpcUtil.string("").getValue(), "");
		assertEquals(RpcUtil.string("test").getValue(), "test");
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
		assertOrdered(next, "test1", "test2");
		assertOrdered(stop, null, e);
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
		var next = Lists.<Integer>of();
		var stop = Lists.<Throwable>of();
		var e = new IOException("test");
		var iob = RpcUtil.<Integer>observer(next::add, stop::add);
		StreamObserver<String> sob = RpcUtil.transformObserver(Integer::parseInt, iob);
		sob.onNext("123");
		sob.onNext(null);
		sob.onNext("456");
		sob.onNext("abc");
		sob.onCompleted();
		sob.onError(e);
		assertOrdered(next, 123, null, 456);
		Assert.throwable(stop.get(0), NumberFormatException.class);
		Assert.isNull(stop.get(1));
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
		Assert.isNull(RpcUtil.cause(null));
		Assert.throwable(RpcUtil.cause(new IOException()), IOException.class);
		Assert.throwable(RpcUtil.cause(statusException("test", null)),
			StatusRuntimeException.class);
		Assert.throwable(RpcUtil.cause(statusException("test1", statusException("test2", null))),
			StatusRuntimeException.class, s -> assertFind(s, "test1"));
		Assert.throwable(
			RpcUtil.cause(
				statusException("test1", statusException("test2", new IOException("test3")))),
			IOException.class, s -> assertFind(s, "test3"));
	}

	private static StatusRuntimeException statusException(String message, Throwable cause) {
		return Status.CANCELLED.withDescription(message).withCause(cause).asRuntimeException();
	}
}
