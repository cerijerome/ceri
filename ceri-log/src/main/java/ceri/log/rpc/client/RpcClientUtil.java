package ceri.log.rpc.client;

import static ceri.common.text.RegexUtil.finder;
import static ceri.common.util.BasicUtil.matchesThrowable;
import java.io.IOException;
import java.util.function.Function;
import java.util.regex.Pattern;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;
import io.grpc.StatusRuntimeException;

public class RpcClientUtil {
	private static final Pattern HALF_CLOSED_MSG_REGEX =
		Pattern.compile("(?i)call already half-closed");
	private static final Pattern SHUTDOWN_MSG_REGEX = Pattern.compile("(?i)channel shutdown");

	private RpcClientUtil() {}

	public static boolean isHalfClosedCall(Throwable t) {
		return matchesThrowable(t, IllegalStateException.class, finder(HALF_CLOSED_MSG_REGEX));
	}

	public static boolean isChannelShutdown(Throwable t) {
		return matchesThrowable(t, StatusRuntimeException.class, finder(SHUTDOWN_MSG_REGEX));
	}

	/**
	 * Only use to squash noisy errors, rather than for logic decisions, as exception types and
	 * messages may change.
	 */
	public static boolean ignorable(Throwable t) {
		return isHalfClosedCall(t) || isChannelShutdown(t);
	}

	/**
	 * Executes runnable, squashing exceptions that can be ignored.
	 */
	public static <E extends Exception> void execute(ExceptionRunnable<E> runnable) throws E {
		try {
			runnable.run();
		} catch (Exception e) {
			if (ignorable(e)) return;
			throw e;
		}
	}

	/**
	 * Converts StatusRuntimeException into an IOException.
	 */
	public static void wrap(ExceptionRunnable<IOException> runnable) throws IOException {
		wrap(runnable, IOException::new);
	}

	/**
	 * Converts StatusRuntimeException into an IOException.
	 */
	public static <T> T wrapReturn(ExceptionSupplier<IOException, T> supplier) throws IOException {
		return wrapReturn(supplier, IOException::new);
	}

	/**
	 * Converts StatusRuntimeException into a new exception.
	 */
	public static <E extends Exception> void wrap(ExceptionRunnable<E> runnable,
		Function<Exception, E> exceptionFn) throws E {
		try {
			runnable.run();
		} catch (StatusRuntimeException e) {
			throw exceptionFn.apply(e);
		}
	}

	/**
	 * Converts StatusRuntimeException into a new exception.
	 */
	public static <E extends Exception, T> T wrapReturn(ExceptionSupplier<E, T> supplier,
		Function<Exception, E> exceptionFn) throws E {
		try {
			return supplier.get();
		} catch (StatusRuntimeException e) {
			throw exceptionFn.apply(e);
		}
	}

}
