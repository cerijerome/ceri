package ceri.log.rpc.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.Functions;
import ceri.log.rpc.client.RpcClients;
import io.grpc.stub.StreamObserver;

/**
 * Wraps a stream observer as a closeable resource. Not thread-safe.
 */
public class RpcStreamer<T> implements Functions.Closeable {
	private static final Logger logger = LogManager.getLogger();
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
		if (closed) return;
		closed = true;
		try {
			observer.onCompleted();
		} catch (RuntimeException e) {
			if (!RpcClients.ignorable(e)) logger.warn(e);
		}
	}

}
