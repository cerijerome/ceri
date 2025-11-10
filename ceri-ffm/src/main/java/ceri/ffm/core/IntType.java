package ceri.ffm.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.foreign.ValueLayout;
import java.util.Map;
import ceri.common.collect.Maps;
import ceri.common.except.Exceptions;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.reflect.Annotations;
import ceri.common.reflect.Reflect;
import ceri.common.text.Format;
import ceri.common.util.Validate;

/**
 * Represents an integer type of desired size and signedness.
 */
@SuppressWarnings("serial")
public abstract class IntType<T extends IntType<T>> extends Number implements Comparable<T> {
	private static Map<Class<?>, Functions.Supplier<?>> constructors = Maps.syncWeak();
	private volatile Config.Value config;
	private long value;

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER })
	public @interface Config {
		String value() default "";

		int size() default 0;

		boolean unsigned() default false;

		record Value(int size, boolean unsigned) {
			private static Map<Class<?>, Config.Value> configs = Maps.syncWeak();

			public static Config.Value of(Class<?> cls) {
				return configs.computeIfAbsent(cls,
					_ -> Config.Value.from(Annotations.annotation(cls, Config.class)));
			}

			public static Value from(Config config) {
				Validate.nonNull(config, "Annotated config");
				int size = config.size();
				if (size <= 0) size = Native.Size.lookup(config.value());
				return new Value(size, config.unsigned());
			}

			@Override
			public String toString() {
				return (unsigned() ? "u" : "s") + (size << 3);
			}
		}
	}

	// @Config(value = "size_t", unsigned = true)
	@Config(size = 2, unsigned = true)
	public static class size_t extends IntType<size_t> {
		public size_t() {}

		public size_t(int n) {
			this();
			set(n);
		}
	}

	@Config(value = "int", unsigned = false)
	public static class ssize_t extends IntType<ssize_t> {}

	public static void main(String[] args) {
		System.out.println(Config.Value.of(size_t.class));
		System.out.println(Config.Value.of(ssize_t.class));
		System.out.println(IntType.of(size_t.class, -1));
		System.out.println(IntType.of(ssize_t.class, -1));
	}

	public static <T extends IntType<T>> ValueLayout layout(Class<T> cls) {
		return Layouts.ofInt(Config.Value.of(cls).size());
	}

	public static <T extends IntType<T>> T from(Class<T> cls, Object nativeValue) {
		return of(cls).setNative(nativeValue);
	}

	public static <T extends IntType<T>> T of(Class<T> cls) {
		return constructor(cls).get();
	}

	public static <T extends IntType<T>> T of(Class<T> cls, int value) {
		return of(cls).set(value);
	}

	public static <T extends IntType<T>> T of(Class<T> cls, long value) {
		return of(cls).set(value);
	}

	protected IntType() {}

	public T set(int value) {
		var config = config();
		return set(config, config.unsigned() ? Maths.uint(value) : (long) value);
	}

	public T set(long value) {
		return set(config(), value);
	}

	public long get() {
		return get(config());
	}

	public T setNative(Object obj) {
		return set(Validate.instance(obj, Number.class).longValue());
	}

	public Number getNative() {
		return switch (config().size()) {
			case Byte.BYTES -> (byte) value;
			case Short.BYTES -> (short) value;
			case Integer.BYTES -> (int) value;
			default -> value;
		};
	}

	public T apply(Functions.LongOperator operator) {
		if (operator == null) return typedThis();
		return set(operator.applyAsLong(get()));
	}

	public long raw() {
		return value;
	}

	public Config.Value config() {
		var config = this.config;
		if (config == null) {
			config = Config.Value.of(getClass());
			this.config = config;
		}
		return config;
	}

	public ValueLayout layout() {
		return Layouts.ofInt(config().size());
	}

	@Override
	public int intValue() {
		return (int) get();
	}

	@Override
	public long longValue() {
		return get();
	}

	@Override
	public float floatValue() {
		return get();
	}

	@Override
	public double doubleValue() {
		return get();
	}

	@Override
	public int compareTo(T t) {
		return config().unsigned() ? Long.compareUnsigned(get(), t.get()) :
			Long.compare(get(), t.get());
	}

	@Override
	public String toString() {
		var config = config();
		return (isUlong(config) ? Long.toUnsignedString(value) : get()) + "|"
			+ Format.hex(value, "0x", 0, config.size() << 1);
	}

	protected T typedThis() {
		return Reflect.unchecked(this);
	}

	// support

	private boolean isUlong(Config.Value config) {
		return config.size() == Long.BYTES && config.unsigned();
	}

	private long get(Config.Value config) {
		if (!config.unsigned()) return value;
		return switch (config.size()) {
			case Byte.BYTES -> Maths.ubyte(value);
			case Short.BYTES -> Maths.ushort(value);
			case Integer.BYTES -> Maths.uint(value);
			default -> value;
		};
	}

	private T set(Config.Value config, long value) {
		this.value = switch (config.size()) {
			case Byte.BYTES -> (byte) value;
			case Short.BYTES -> (short) value;
			case Integer.BYTES -> (int) value;
			default -> value;
		};
		return typedThis();
	}

	private static <T extends IntType<T>> Functions.Supplier<T> constructor(Class<T> cls) {
		return Reflect.unchecked(constructors.computeIfAbsent(cls, _ -> findConstructor(cls)));
	}

	private static Functions.Supplier<Object> findConstructor(Class<?> cls) {
		var constructors = cls.getConstructors();
		for (var constructor : constructors) {
			int n = constructor.getParameterCount();
			if (n > 1) continue;
			if (n == 0) return () -> Reflect.create(constructor);
			var argType = constructor.getParameterTypes()[0];
			if (argType == long.class || argType == int.class)
				return () -> Reflect.create(constructor, 0);
		}
		throw Exceptions.illegalArg("No constructor %s(|long|int)", Reflect.name(cls));
	}
}
