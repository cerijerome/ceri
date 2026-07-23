package ceri.ffm.core;

import java.lang.foreign.MemorySegment;
import java.nio.Buffer;
import ceri.common.collect.Maps;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.except.Exceptions;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.io.Buffers;
import ceri.common.text.Chars;
import ceri.common.text.Joiner;
import ceri.common.text.Transformer;
import ceri.ffm.type.BufferType;
import ceri.ffm.type.Group;
import ceri.ffm.type.PointerType;

/**
 * Utility to call native methods and check status codes.
 */
public class Caller<E extends Exception, T> {
	private final Config<E> config;
	private final Functions.Supplier<T> lib;

	/**
	 * Caller configuration for generating formatted exceptions.
	 */
	public record Config<E extends Exception>(Transformer transformer, int generalCode,
		ToException<E> exceptionFn) {
		/**
		 * Creates a caller instance for the given library supplier.
		 */
		public <T> Caller<E, T> caller(Functions.Supplier<T> lib) {
			return new Caller<>(this, lib);
		}

		/**
		 * Creates a caller instance for the given library.
		 */
		public <T> Caller<E, T> callerOf(T lib) {
			return caller(() -> lib);
		}

		private E exception(int code, Functions.Function<CallDescriptor, String> callDesc,
			Throwable cause) {
			var message = callDesc.apply(this::failMessage);
			return Exceptions.initCause(exceptionFn().apply(code, message), cause);
		}

		private String failMessage(String name, Object... args) {
			return name + Joiner.PARAM.joinAll(transformer(), args) + " failed";
		}
	}

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
	public interface CallDescriptor {
		/**
		 * Returns a message from call name and arguments.
		 */
		String accept(String name, Object... args);
	}

	/**
	 * Transformation of arguments to strings.
	 */
	public static class Transform {
		/** Compact transforms for a single line. */
		public static Transformer COMPACT = compactTransformer(16, 5);
		/** Longer transforms with multiple lines. */
		public static Transformer FULL = fullTransformer();

		private Transform() {}

		/**
		 * Wrapper to prevent application of transforms.
		 */
		public record Raw(Object raw) {
			@Override
			public String toString() {
				return String.valueOf(raw());
			}
		}

		/**
		 * Shows escaped and quoted char sequences.
		 */
		public static String chars(CharSequence chars, int limit) {
			if (limit < 0 || chars.length() <= limit) return "\"" + Chars.escape(chars) + "\"";
			return "\"" + Chars.escape(chars.subSequence(0, Math.max(0, limit - 1))) + "..\"";
		}

		/**
		 * Shows struct and union member values as a map.
		 */
		public static String group(Transformer.Context context, Group<?, ?> group) {
			var map = Maps.<Raw, Object>link();
			Group.forEachMember(group, (m, t) -> map.put(new Raw(m.name()), t));
			return context.apply(map);
		}

		/**
		 * Shows typed pointer memory location and type instance.
		 */
		public static String typedPointer(Transformer.Context context,
			PointerType.Indexable<?, ?, ?> pointer) {
			var array = pointer.getArray(1, false);
			return context.apply(pointer.memory()) + context.apply(array);
		}

		/**
		 * Shows untyped pointer memory location.
		 */
		public static String pointer(Transformer.Context context, PointerType pointer) {
			return context.apply(pointer.memory());
		}

		/**
		 * Shows buffer content array.
		 */
		public static String buffer(Transformer.Context context, Buffer buffer) {
			var buffers = BufferType.from(buffer).buffers();
			var array = Buffers.apply(buffer, buffers::get);
			return context.apply(array);
		}
	}

	/**
	 * Creates caller configuration with exception adapter.
	 */
	public static <E extends Exception> Config<E> config(ToException<E> exceptionFn) {
		return config(Transform.COMPACT, -1, exceptionFn);
	}

	/**
	 * Creates caller configuration with argument formatter and exception adapter.
	 */
	public static <E extends Exception> Config<E> config(Transformer transformer, int generalCode,
		ToException<E> exceptionFn) {
		return new Config<>(transformer, generalCode, exceptionFn);
	}

	private Caller(Config<E> config, Functions.Supplier<T> lib) {
		this.config = config;
		this.lib = lib;
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
		public int lastErrorCode() {
			return LastError.get();
		}

		/**
		 * If the call result matches the given error value, the last error will generate an
		 * exception on call completion.
		 */
		public int lastError(int result, int error) {
			if (result == error) lastError();
			return result;
		}

		/**
		 * Verifies the last error; a non-zero code will generate an exception on call completion.
		 */
		public void lastError() {
			int code = lastErrorCode();
			if (code != LastError.OK) fail(code);
		}
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
		Functions.Function<CallDescriptor, String> callDesc) throws E {
		var context = new Context();
		exec(context, call);
		verify(context, callDesc);
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
		Functions.Function<CallDescriptor, String> callDesc) throws E {
		var context = new Context();
		int result = execInt(context, call);
		verify(context, callDesc);
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
		Functions.Function<CallDescriptor, String> callDesc) throws E {
		var context = new Context();
		R result = execType(context, call);
		verify(context, callDesc);
		return result;
	}

	// support

	private void verify(Context context, Functions.Function<CallDescriptor, String> callDesc)
		throws E {
		if (context.code != 0) throw config.exception(context.code, callDesc, context.cause);
	}

	private void exec(Context context, Excepts.Consumer<?, Context> call) {
		try {
			call.accept(context);
		} catch (RuntimeInterruptedException e) {
			throw e;
		} catch (Exception e) {
			context.fail(config.generalCode(), e);
		}
	}

	private int execInt(Context context, Excepts.ToIntFunction<?, Context> call) {
		try {
			return call.applyAsInt(context);
		} catch (RuntimeInterruptedException e) {
			throw e;
		} catch (Exception e) {
			context.fail(config.generalCode(), e);
			return 0;
		}
	}

	private <R> R execType(Context context, Excepts.Function<?, Context, R> call) {
		try {
			return call.apply(context);
		} catch (RuntimeInterruptedException e) {
			throw e;
		} catch (Exception e) {
			context.fail(config.generalCode(), e);
			return null;
		}
	}

	private static Transformer fullTransformer() {
		return Transformer.builder() //
			.add(CharSequence.class, (_, c) -> Transform.chars(c, -1)) //
			.add(Buffer.class, Transform::buffer) //
			.add(MemorySegment.class, (_, m) -> Segments.string(m)) //
			.add(PointerType.Indexable.class, Transform::typedPointer) //
			.add(PointerType.class, Transform::pointer) //
			.build();
	}

	private static Transformer compactTransformer(int stringSize, int sequenceSize) {
		return Transformer.builder() //
			.iterables(Transformer.joiner(Joiner.ARRAY, sequenceSize)) //
			.maps(Transformer.joiner(Joiner.LIST, sequenceSize), "=") //
			.add(CharSequence.class, (_, c) -> Transform.chars(c, stringSize)) //
			.add(Buffer.class, Transform::buffer) //
			.add(MemorySegment.class, (_, m) -> Segments.string(m)) //
			.add(PointerType.Indexable.class, Transform::typedPointer) //
			.add(PointerType.class, Transform::pointer) //
			.add(Group.class, Transform::group) //
			.build();
	}
}
