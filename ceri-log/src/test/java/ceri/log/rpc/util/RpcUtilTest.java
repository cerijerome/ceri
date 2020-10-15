package ceri.log.rpc.util;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrowable;
import static ceri.common.text.RegexUtil.finder;
import static org.hamcrest.CoreMatchers.is;
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
		assertThat(RpcUtil.int32(Integer.MAX_VALUE).getValue(), is(Integer.MAX_VALUE));
		assertThat(RpcUtil.int32(Integer.MIN_VALUE).getValue(), is(Integer.MIN_VALUE));
	}

	@Test
	public void testUint32Value() {
		assertThat(RpcUtil.uint32(Integer.MAX_VALUE).getValue(), is(Integer.MAX_VALUE));
		assertThat(RpcUtil.uint32(Integer.MIN_VALUE).getValue(), is(Integer.MIN_VALUE));
	}

	@Test
	public void testInt64Value() {
		assertThat(RpcUtil.int64(Long.MAX_VALUE).getValue(), is(Long.MAX_VALUE));
		assertThat(RpcUtil.int64(Long.MIN_VALUE).getValue(), is(Long.MIN_VALUE));
	}

	@Test
	public void testUint64Value() {
		assertThat(RpcUtil.uint64(Long.MAX_VALUE).getValue(), is(Long.MAX_VALUE));
		assertThat(RpcUtil.uint64(Long.MIN_VALUE).getValue(), is(Long.MIN_VALUE));
	}

	@Test
	public void testBool() {
		assertThat(RpcUtil.bool(true).getValue(), is(true));
		assertThat(RpcUtil.bool(false).getValue(), is(false));
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
		assertThat(stop.get(2), is(e));
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
