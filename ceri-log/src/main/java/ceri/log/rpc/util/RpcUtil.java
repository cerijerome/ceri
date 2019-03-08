package ceri.log.rpc.util;

import java.util.function.Consumer;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import ceri.common.function.ExceptionFunction;
import ceri.common.util.BasicUtil;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class RpcUtil {
	public static final Empty EMPTY = Empty.getDefaultInstance();
	private static final StreamObserver<Object> NULL_OBSERVER = observer(null, null, null);

	private RpcUtil() {}

	/**
	 * Value construction convenience method.
	 */
	public static BytesValue bytes(byte[] buffer) {
		return bytes(buffer, 0);
	}

	/**
	 * Value construction convenience method.
	 */
	public static BytesValue bytes(byte[] buffer, int offset) {
		return bytes(buffer, offset, buffer.length - offset);
	}

	/**
	 * Value construction convenience method.
	 */
	public static BytesValue bytes(byte[] buffer, int offset, int len) {
		return bytes(ByteString.copyFrom(buffer, offset, len));
	}

	/**
	 * Value construction convenience method.
	 */
	public static BytesValue bytes(ByteString value) {
		return BytesValue.newBuilder().setValue(value).build();
	}

	/**
	 * Value construction convenience method.
	 */
	public static Int32Value int32(int value) {
		return Int32Value.newBuilder().setValue(value).build();
	}

	/**
	 * Value construction convenience method.
	 */
	public static UInt32Value uint32(int value) {
		return UInt32Value.newBuilder().setValue(value).build();
	}

	/**
	 * Value construction convenience method.
	 */
	public static Int64Value int64(long value) {
		return Int64Value.newBuilder().setValue(value).build();
	}

	/**
	 * Value construction convenience method.
	 */
	public static UInt64Value uint64(long value) {
		return UInt64Value.newBuilder().setValue(value).build();
	}

	/**
	 * Value construction convenience method.
	 */
	public static BoolValue bool(boolean value) {
		return BoolValue.newBuilder().setValue(value).build();
	}

	/**
	 * Convenience method for creating an observer.
	 */
	public static <T> StreamObserver<T> observer(Consumer<T> onNext, Consumer<Throwable> onStop) {
		return observer(onNext, () -> onStop.accept(null), onStop);
	}

	/**
	 * Convenience method for creating an observer.
	 */
	public static <T> StreamObserver<T> observer(Consumer<T> onNext, Runnable onCompleted,
		Consumer<Throwable> onError) {
		return new StreamObserver<>() {
			@Override
			public void onNext(T value) {
				if (onNext != null) onNext.accept(value);
			}

			@Override
			public void onCompleted() {
				if (onCompleted != null) onCompleted.run();
			}

			@Override
			public void onError(Throwable t) {
				if (onError != null) onError.accept(t);
			}
		};
	}

	/**
	 * Transforms a steam observer type.
	 */
	public static <E extends Exception, T, U> StreamObserver<T>
		transformObserver(ExceptionFunction<E, T, U> fn, StreamObserver<U> observer) {
		return observer(t -> onNext(t, fn, observer), observer::onCompleted, observer::onError);
	}

	private static <E extends Exception, T, U> void onNext(T t, ExceptionFunction<E, T, U> fn,
		StreamObserver<U> observer) {
		try {
			observer.onNext(t == null ? null : fn.apply(t));
		} catch (Exception e) {
			observer.onError(e);
		}
	}

	/**
	 * A no-op oberver.
	 */
	public static <T> StreamObserver<T> nullObserver() {
		return BasicUtil.uncheckedCast(NULL_OBSERVER);
	}

	/**
	 * Looks for the first cause after StatusRuntimeExceptions.
	 */
	public static Throwable cause(Throwable t) {
		if (t == null) return null;
		Throwable t0 = t;
		while (t0 != null && !(t0 instanceof StatusRuntimeException))
			t0 = t0.getCause();
		while (t0 != null && (t0 instanceof StatusRuntimeException))
			t0 = t0.getCause();
		return t0 == null ? t : t0;
	}

}
