package ceri.log.rpc;

import java.io.Closeable;
import io.grpc.stub.StreamObserver;

/**
 * Wraps a stream observer as a closeable resource. Not thread-safe.
 */
public class RpcStreamer<T> implements Closeable {
	private final StreamObserver<T> observer;
	private boolean closed = false;

	public static <T> RpcStreamer<T> of(StreamObserver<T> observer) {
		return new RpcStreamer<>(observer);
	}

	private RpcStreamer(StreamObserver<T> observer) {
		this.observer = observer;
	}

	public void next(T t) {
		if (closed) throw new IllegalStateException("Stream is closed");
		observer.onNext(t);
	}

	public void error(Throwable t) {
		observer.onError(t);
		closed = true;
	}

	public boolean closed() {
		return closed;
	}
	
	@Override
	public void close() {
		if (!closed) observer.onCompleted();
		closed = true;
	}

}
