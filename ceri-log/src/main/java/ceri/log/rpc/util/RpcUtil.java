package ceri.log.rpc.util;

import java.util.function.Consumer;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import ceri.common.function.Excepts;
import ceri.common.reflect.Reflect;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class RpcUtil {
	public static final Empty EMPTY = Empty.getDefaultInstance();
	private static final StreamObserver<Object> NULL_OBSERVER = observer(null, null, null);

	private RpcUtil() {}

	/**
	 * Value construction convenience method.
	 */
	public static BoolValue bool(boolean value) {
		return BoolValue.of(value);
	}

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
		return BytesValue.of(value);
	}

	/**
	 * Value construction convenience method.
	 */
	public static Int32Value int32(int value) {
		return Int32Value.of(value);
	}

	/**
	 * Value construction convenience method.
	 */
	public static Int32Value int32(boolean value) {
		return int32(value ? 1 : 0);
	}

	/**
	 * Value construction convenience method.
	 */
	public static UInt32Value uint32(int value) {
		return UInt32Value.of(value);
	}

	/**
	 * Value construction convenience method.
	 */
	public static Int64Value int64(long value) {
		return Int64Value.of(value);
	}

	/**
	 * Value construction convenience method.
	 */
	public static UInt64Value uint64(long value) {
		return UInt64Value.of(value);
	}

	/**
	 * Value construction convenience method.
	 */
	public static StringValue string(String value) {
		return value == null ? null : StringValue.of(value);
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
		transformObserver(Excepts.Function<E, T, U> fn, StreamObserver<U> observer) {
		return observer(t -> onNext(t, fn, observer), observer::onCompleted, observer::onError);
	}

	private static <E extends Exception, T, U> void onNext(T t, Excepts.Function<E, T, U> fn,
		StreamObserver<U> observer) {
		try {
			observer.onNext(t == null ? null : fn.apply(t));
		} catch (Exception e) {
			observer.onError(e);
		}
	}

	/**
	 * A no-op observer.
	 */
	public static <T> StreamObserver<T> nullObserver() {
		return Reflect.unchecked(NULL_OBSERVER);
	}

	/**
	 * Looks for the first cause after StatusRuntimeExceptions.
	 */
	public static Throwable cause(Throwable t) {
		if (t == null) return null;
		Throwable t0 = t;
		while (t0 != null && !(t0 instanceof StatusRuntimeException))
			t0 = t0.getCause();
		while (t0 instanceof StatusRuntimeException)
			t0 = t0.getCause();
		return t0 == null ? t : t0;
	}
}
