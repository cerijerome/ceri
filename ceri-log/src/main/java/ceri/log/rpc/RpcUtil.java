package ceri.log.rpc;

import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class RpcUtil {
	public static final Empty EMPTY = Empty.getDefaultInstance();

	private RpcUtil() {}

	public static <T> StreamObserver<T> nullObserver() {
		return new StreamObserver<>() {
			@Override
			public void onNext(T value) {}

			@Override
			public void onCompleted() {}

			@Override
			public void onError(Throwable t) {}
		};
	}

	/**
	 * Looks for the first cause after StatusRuntimeExceptions.
	 */
	public static Throwable cause(Throwable t) {
		if (t == null) return null;
		Throwable t0 = t;
		while (t0 != null && !(t0 instanceof StatusRuntimeException)) t0 = t0.getCause(); 
		while (t0 != null && (t0 instanceof StatusRuntimeException)) t0 = t0.getCause();
		return t0 == null ? t : t0;
	}
	
}
