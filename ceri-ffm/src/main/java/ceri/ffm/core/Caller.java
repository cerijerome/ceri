package ceri.ffm.core;

import ceri.common.concurrent.Concurrent;
import ceri.common.except.Exceptions;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.text.Joiner;
import ceri.common.text.Strings;
import ceri.common.text.Transformer;
import ceri.ffm.clib.ffm.CErrNo;
import ceri.ffm.clib.ffm.CException;

/**
 * Utility to call native methods and check status codes.
 */
public class Caller<E extends Exception, T> {
	private final Transformer transformer;
	private final int generalErrorCode;
	private final ToException<E> exceptionFn;
	private final Functions.Supplier<T> lib;

	/**
	 * Converts an error code and message to an exception.
	 */
	public interface ToException<E extends Exception> {
		/**
		 * Returns an exception instance from the error code and message.
		 */
		E apply(int code, String message);
	}

	/**
	 * Provides a message from call name and arguments.
	 */
	public interface Message {
		/**
		 * Returns a message from call name and arguments.
		 */
		String accept(String name, Object... args);

		default Object fmt(String format, Object... args) {
			return new Formatted(format, args);
		}
	}

	/**
	 * Formatting wrapper to prevent application of transforms.
	 */
	private record Formatted(String format, Object... args) {
		@Override
		public String toString() {
			return Strings.format(format, args);
		}
	}

	/**
	 * Context to support execution of calls.
	 */
	public class Context {
		private int code = 0;
		private Exception cause = null;

		/**
		 * Provides the call library.
		 */
		public T lib() {
			return lib.get();
		}

		/**
		 * Verifies the last error if the result indicates an error; allowed errors will pass,
		 * otherwise an exception is generated on call completion.
		 */
		public int verifyInt(int result, int error, CErrNo... allowedErrNos) {
			if (result == error) verify(allowedErrNos);
			return result;
		}

		/**
		 * Verifies the last error; zero and allowed errors will pass, otherwise an exception is
		 * generated on call completion.
		 */
		public void verify(CErrNo... allowedErrNos) {
			int code = errNo();
			if (code == LastError.OK) return;
			for (var allowedErrNo : allowedErrNos)
				if (code == allowedErrNo.code) return;
			fail(code);
		}

		/**
		 * Registers a failure code, which will generate an exception on call completion.
		 */
		public void fail(int code) {
			fail(code, null);
		}

		/**
		 * Registers a failure code and cause, which will generate an exception on call completion.
		 */
		public void fail(int code, Exception cause) {
			this.code = code;
			this.cause = cause;
		}

		/**
		 * Returns the last error code.
		 */
		public int errNo() {
			return LastError.get();
		}
	}

	/**
	 * Creates caller configuration with exception adapter.
	 */
	public static <T> Caller<CException, T> of(Functions.Supplier<T> lib) {
		return of(CException::full, CException.GENERAL_ERROR_CODE, lib);
	}

	/**
	 * Creates caller configuration with argument formatter and exception adapter.
	 */
	public static <E extends Exception, T> Caller<E, T> of(ToException<E> exceptionFn,
		int generalErrorCode, Functions.Supplier<T> lib) {
		return of(Formats.COMPACT, exceptionFn, generalErrorCode, lib);
	}

	/**
	 * Creates caller configuration with argument formatter and exception adapter.
	 */
	public static <E extends Exception, T> Caller<E, T> of(Transformer transformer,
		ToException<E> exceptionFn, int generalErrorCode, Functions.Supplier<T> lib) {
		return new Caller<>(transformer, exceptionFn, generalErrorCode, lib);
	}

	private Caller(Transformer transformer, ToException<E> exceptionFn, int generalErrorCode,
		Functions.Supplier<T> lib) {
		this.transformer = transformer;
		this.exceptionFn = exceptionFn;
		this.generalErrorCode = generalErrorCode;
		this.lib = lib;
	}

	/**
	 * Executes the call with contextual support.
	 */
	public void call(Excepts.Consumer<?, Context> call, String name, Object... args) throws E {
		call(call, m -> m.accept(name, args));
	}

	/**
	 * Executes the call with contextual support.
	 */
	public void call(Excepts.Consumer<?, Context> call,
		Functions.Function<Message, String> messaging) throws E {
		var context = new Context();
		exec(context, call);
		verify(context, messaging);
	}

	/**
	 * Executes the call with contextual support, returning an int value.
	 */
	public int callInt(Excepts.ToIntFunction<?, Context> call, String name, Object... args)
		throws E {
		return callInt(call, m -> m.accept(name, args));
	}

	/**
	 * Executes the call with contextual support, returning an int value.
	 */
	public int callInt(Excepts.ToIntFunction<?, Context> call,
		Functions.Function<Message, String> messaging) throws E {
		var context = new Context();
		int result = execInt(context, call);
		verify(context, messaging);
		return result;
	}

	/**
	 * Executes the call with contextual support, returning an int value.
	 */
	public long callLong(Excepts.ToLongFunction<?, Context> call, String name, Object... args)
		throws E {
		return callLong(call, m -> m.accept(name, args));
	}

	/**
	 * Executes the call with contextual support, returning an int value.
	 */
	public long callLong(Excepts.ToLongFunction<?, Context> call,
		Functions.Function<Message, String> messaging) throws E {
		var context = new Context();
		long result = execLong(context, call);
		verify(context, messaging);
		return result;
	}

	/**
	 * Executes the call with contextual support, returning a typed value.
	 */
	public <R> R callType(Excepts.Function<?, Context, R> call, String name, Object... args)
		throws E {
		return callType(call, m -> m.accept(name, args));
	}

	/**
	 * Executes the call with contextual support, returning a typed value.
	 */
	public <R> R callType(Excepts.Function<?, Context, R> call,
		Functions.Function<Message, String> messaging) throws E {
		var context = new Context();
		R result = execType(context, call);
		verify(context, messaging);
		return result;
	}

	/**
	 * Executes the call and returns an int, checking last error if the error code is returned.
	 */
	public int verifyInt(Excepts.ToIntFunction<?, T> call, int error, String name, Object... args)
		throws E {
		return verifyInt(call, error, m -> m.accept(name, args));
	}

	/**
	 * Executes the call and returns an int, checking last error if the error code is returned.
	 */
	public int verifyInt(Excepts.ToIntFunction<?, T> call, int error,
		Functions.Function<Message, String> messaging) throws E {
		return callInt(c -> c.verifyInt(call.applyAsInt(c.lib()), error), messaging);
	}

	// support

	private void verify(Context context, Functions.Function<Message, String> messaging) throws E {
		if (context.code != 0) throw exception(context.code, messaging, context.cause);
	}

	private void exec(Context context, Excepts.Consumer<?, Context> call) {
		try {
			call.accept(context);
		} catch (Exception e) {
			fail(context, e);
		}
	}

	private int execInt(Context context, Excepts.ToIntFunction<?, Context> call) {
		try {
			return call.applyAsInt(context);
		} catch (Exception e) {
			fail(context, e);
			return 0;
		}
	}

	private long execLong(Context context, Excepts.ToLongFunction<?, Context> call) {
		try {
			return call.applyAsLong(context);
		} catch (Exception e) {
			fail(context, e);
			return 0L;
		}
	}

	private <R> R execType(Context context, Excepts.Function<?, Context, R> call) {
		try {
			return call.apply(context);
		} catch (Exception e) {
			fail(context, e);
			return null;
		}
	}

	private void fail(Context context, Exception e) {
		Concurrent.checkRuntimeInterrupted(e);
		context.fail(generalErrorCode, e);
	}

	private E exception(int code, Functions.Function<Message, String> messaging, Throwable cause) {
		var message = messaging.apply(this::failMessage);
		return Exceptions.initCause(exceptionFn.apply(code, message), cause);
	}

	private String failMessage(String name, Object... args) {
		args = Reflect.flattenVarArgs(args);
		return name + Joiner.PARAM.joinAll(transformer, args) + " failed";
	}
}
