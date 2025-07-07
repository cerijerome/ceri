package ceri.log.rpc.client;

import static ceri.common.text.RegexUtil.finder;
import java.io.IOException;
import java.util.function.Function;
import java.util.regex.Pattern;
import ceri.common.exception.ExceptionUtil;
import ceri.common.function.Excepts.Runnable;
import ceri.common.function.Excepts.Supplier;
import io.grpc.StatusRuntimeException;

public class RpcClientUtil {
	private static final Pattern HALF_CLOSED_MSG_REGEX = Pattern.compile("(?i)already half-closed");
	private static final Pattern SHUTDOWN_MSG_REGEX = Pattern.compile("(?i)channel shutdown");

	private RpcClientUtil() {}

	public static boolean isHalfClosedCall(Throwable t) {
		return ExceptionUtil.matches(t, IllegalStateException.class, finder(HALF_CLOSED_MSG_REGEX));
	}

	public static boolean isChannelShutdown(Throwable t) {
		return ExceptionUtil.matches(t, StatusRuntimeException.class, finder(SHUTDOWN_MSG_REGEX));
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
	public static <E extends Exception> void execute(Runnable<E> runnable) throws E {
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
	public static void wrap(Runnable<IOException> runnable) throws IOException {
		wrap(runnable, IOException::new);
	}

	/**
	 * Converts StatusRuntimeException into an IOException.
	 */
	public static <T> T wrapReturn(Supplier<IOException, T> supplier) throws IOException {
		return wrapReturn(supplier, IOException::new);
	}

	/**
	 * Converts StatusRuntimeException into a new exception.
	 */
	public static <E extends Exception> void wrap(Runnable<E> runnable,
		Function<String, E> exceptionFn) throws E {
		try {
			runnable.run();
		} catch (StatusRuntimeException e) {
			throw exceptionFn.apply(e.getMessage());
		}
	}

	/**
	 * Converts StatusRuntimeException into a new exception.
	 */
	public static <E extends Exception, T> T wrapReturn(Supplier<E, T> supplier,
		Function<String, E> exceptionFn) throws E {
		try {
			return supplier.get();
		} catch (StatusRuntimeException e) {
			throw exceptionFn.apply(e.getMessage());
		}
	}

}
