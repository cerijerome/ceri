package ceri.log.rpc.service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.protobuf.Empty;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.exception.ExceptionUtil;
import ceri.common.function.Excepts.Runnable;
import ceri.common.function.Excepts.Supplier;
import ceri.common.text.Patterns;
import ceri.log.rpc.util.RpcUtil;
import io.grpc.Server;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class RpcServiceUtil {
	private static final Logger logger = LogManager.getLogger();
	public static final Server NULL_SERVER = nullServer();
	public static final ServerServiceDefinition NULL_SERVICE_DEFINITION =
		ServerServiceDefinition.builder("Null").build();
	private static final int MAX_MESSAGE_LEN = 128;
	private static final Pattern CANCELLED_BEFORE_HALF_CLOSE_MSG_REGEX =
		Pattern.compile("(?i)cancelled before receiving half close");

	private RpcServiceUtil() {}

	/**
	 * Check to avoid logging common trivial errors.
	 */
	public static boolean isCancelledBeforeHalfClose(Throwable t) {
		return ExceptionUtil.matches(t, StatusRuntimeException.class,
			Patterns.Filter.find(CANCELLED_BEFORE_HALF_CLOSE_MSG_REGEX));
	}

	/**
	 * Only use to squash noisy errors, rather than for logic decisions, as exception types and
	 * messages may change.
	 */
	public static boolean ignorable(Throwable t) {
		return isCancelledBeforeHalfClose(t);
	}

	/**
	 * Converts an exception into a status exception useful for clients with message to indicate
	 * exception type and its message.
	 */
	public static StatusRuntimeException statusException(Exception e) {
		if (e instanceof StatusRuntimeException sre) return sre;
		String message = e.toString();
		if (message.length() > MAX_MESSAGE_LEN) message = message.substring(0, MAX_MESSAGE_LEN);
		return new StatusRuntimeException(Status.UNAVAILABLE.withDescription(message));
	}

	/**
	 * Executes runnable then responds with Empty to the client.
	 */
	public static <E extends Exception> void accept(StreamObserver<Empty> observer,
		Runnable<E> runnable) {
		respond(observer, RpcUtil.EMPTY, runnable);
	}

	/**
	 * Responds to client with result of supplier call.
	 */
	public static <E extends Exception, T> void respond(StreamObserver<T> observer,
		Supplier<E, T> supplier) {
		try {
			T t = supplier.get();
			observer.onNext(t);
			observer.onCompleted();
		} catch (Exception e) {
			ConcurrentUtil.interrupt(e);
			logger.catching(e);
			observer.onError(statusException(e));
		}
	}

	/**
	 * Executes runnable then responds to client with given value.
	 */
	public static <E extends Exception, T> void respond(StreamObserver<T> observer, T t,
		Runnable<E> runnable) {
		try {
			runnable.run();
			observer.onNext(t);
			observer.onCompleted();
		} catch (Exception e) {
			ConcurrentUtil.interrupt(e);
			logger.catching(e);
			observer.onError(statusException(e));
		}
	}

	private static Server nullServer() {
		return new Server() {
			@Override
			public void awaitTermination() throws InterruptedException {}

			@Override
			public boolean awaitTermination(long timeout, TimeUnit unit)
				throws InterruptedException {
				return true;
			}

			@Override
			public boolean isShutdown() {
				return false;
			}

			@Override
			public boolean isTerminated() {
				return false;
			}

			@Override
			public Server shutdown() {
				return this;
			}

			@Override
			public Server shutdownNow() {
				return shutdown();
			}

			@Override
			public Server start() throws IOException {
				return this;
			}
		};
	}
}
