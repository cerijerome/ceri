package ceri.log.rpc;

import com.google.protobuf.Empty;
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

}
