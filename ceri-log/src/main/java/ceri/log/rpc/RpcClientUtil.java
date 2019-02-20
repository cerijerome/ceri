package ceri.log.rpc;

import java.io.IOException;
import java.util.function.Function;
import java.util.regex.Pattern;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;
import ceri.common.util.BasicUtil;
import io.grpc.StatusRuntimeException;

public class RpcClientUtil {
	private static final Pattern SHUTDOWN_MSG_REGEX = Pattern.compile("(?i)channel shutdown");
	
	private RpcClientUtil() {}

	public static boolean isChannelShutdown(Throwable t) {
		StatusRuntimeException ex = BasicUtil.castOrNull(StatusRuntimeException.class, t);
		if (ex == null) return false;
		String msg = ex.getMessage();
		if (msg == null || msg.isEmpty()) return false;
		return SHUTDOWN_MSG_REGEX.matcher(msg).find();
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
