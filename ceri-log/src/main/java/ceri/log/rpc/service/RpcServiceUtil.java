package ceri.log.rpc.service;

import static ceri.common.text.RegexUtil.finder;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;
import ceri.common.util.ExceptionUtil;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class RpcServiceUtil {
	private static final Logger logger = LogManager.getLogger();
	private static final int MAX_MESSAGE_LEN = 128;
	private static final Pattern CANCELLED_BEFORE_HALF_CLOSE_MSG_REGEX =
		Pattern.compile("(?i)cancelled before receiving half close");

	private RpcServiceUtil() {}

	public static boolean isCancelledBeforeHalfClose(Throwable t) {
		return ExceptionUtil.matches(t, StatusRuntimeException.class,
			finder(CANCELLED_BEFORE_HALF_CLOSE_MSG_REGEX));
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
		if (e instanceof StatusRuntimeException) return (StatusRuntimeException) e;
		String message = e.toString();
		if (message.length() > MAX_MESSAGE_LEN) message = message.substring(0, MAX_MESSAGE_LEN);
		return new StatusRuntimeException(Status.UNAVAILABLE.withDescription(message));
	}

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

}
