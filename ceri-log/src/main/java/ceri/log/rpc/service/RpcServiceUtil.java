package ceri.log.rpc.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class RpcServiceUtil {
	private static final Logger logger = LogManager.getLogger();
	private static final int MAX_MESSAGE_LEN = 128;

	private RpcServiceUtil() {}

	/**
	 * Responds to client with result of supplier call.
	 */
	public static <E extends Exception, T> void respond(StreamObserver<T> observer,
		ExceptionSupplier<E, T> supplier) {
		try {
			T t = supplier.get();
			observer.onNext(t);
			observer.onCompleted();
		} catch (Exception e) {
			logger.catching(e);
			observer.onError(statusException(e));
		}
	}

	/**
	 * Executes runnable then responds to client with given value.
	 */
	public static <E extends Exception, T> void respond(StreamObserver<T> observer, T t,
		ExceptionRunnable<E> runnable) {
		try {
			runnable.run();
			observer.onNext(t);
			observer.onCompleted();
		} catch (Exception e) {
			logger.catching(e);
			observer.onError(statusException(e));
		}
	}

	public static StatusRuntimeException statusException(Exception e) {
		if (e instanceof StatusRuntimeException) return (StatusRuntimeException) e;
		String message = e.toString();
		if (message.length() > MAX_MESSAGE_LEN) message = message.substring(0, MAX_MESSAGE_LEN);
		return new StatusRuntimeException(Status.UNAVAILABLE.withDescription(message));
	}

}
