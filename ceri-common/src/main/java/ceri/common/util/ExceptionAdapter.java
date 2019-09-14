package ceri.common.util;

import java.lang.reflect.Constructor;
import java.util.function.Function;
import ceri.common.reflect.ReflectUtil;

/**
 * Adapter that only wraps non-assignable exceptions.
 */
public class ExceptionAdapter<E extends Exception> implements Function<Throwable, E> {
	public static ExceptionAdapter<Exception> NULL = of(Exception.class, Exception::new);
	public static ExceptionAdapter<RuntimeException> RUNTIME =
		of(RuntimeException.class, RuntimeException::new);
	private final Class<E> cls;
	private final Function<Throwable, E> fn;

	public static <E extends Exception> ExceptionAdapter<E> of(Class<E> cls) {
		Constructor<E> constructor = ReflectUtil.constructor(cls, Throwable.class);
		return of(cls, e -> ReflectUtil.create(constructor, e));
	}

	public static <E extends Exception> ExceptionAdapter<E> of(Class<E> cls,
		Function<Throwable, E> fn) {
		return new ExceptionAdapter<>(cls, fn);
	}

	private ExceptionAdapter(Class<E> cls, Function<Throwable, E> fn) {
		this.cls = cls;
		this.fn = fn;
	}

	public E apply(Throwable t) {
		if (cls.isInstance(t)) return cls.cast(t);
		return fn.apply(t);
	}
}
