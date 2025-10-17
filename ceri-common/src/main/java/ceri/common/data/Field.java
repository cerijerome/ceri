package ceri.common.data;

import java.util.Set;
import ceri.common.collect.Lists;
import ceri.common.except.Exceptions;
import ceri.common.function.Excepts;
import ceri.common.math.Maths;

/**
 * Provides access to a field.
 */
public record Field<E extends Exception, T, U>(Excepts.Function<E, T, U> getter,
	Excepts.BiConsumer<E, T, U> setter) {

	/**
	 * Create a no-op, stateless instance.
	 */
	public static <T, U> Field<RuntimeException, T, U> ofNull() {
		return new Field<>(_ -> null, (_, _) -> {});
	}

	/**
	 * Create an instance with getter and setter for a long field.
	 */
	public static <E extends Exception, T, U> Field<E, T, U> of(Excepts.Function<E, T, U> getter,
		Excepts.BiConsumer<E, T, U> setter) {
		return new Field<>(getter, setter);
	}

	/**
	 * Create an instance with getter and setter for an unsigned int field.
	 */
	public static <E extends Exception, T> Long<E, T> ofUint(Excepts.ToIntFunction<E, T> getter,
		Excepts.ObjIntConsumer<E, T> setter) {
		return ofLong(getter == null ? null : t -> Maths.uint(getter.applyAsInt(t)),
			setter == null ? null : (t, v) -> setter.accept(t, (int) v));
	}

	/**
	 * Create an instance with getter and setter for a long field.
	 */
	public static <E extends Exception, T> Long<E, T> ofLong(Excepts.ToLongFunction<E, T> getter,
		Excepts.ObjLongConsumer<E, T> setter) {
		return new Long<>(getter, setter);
	}

	/**
	 * Get source value.
	 */
	public U get(T source) throws E {
		return valid(getter, "Get").apply(source);
	}

	/**
	 * Set source value.
	 */
	public Field<E, T, U> set(T source, U value) throws E {
		valid(setter(), "Set").accept(source, value);
		return this;
	}

	/**
	 * A long field accessor. Covers all integral types.
	 */
	public record Long<E extends Exception, T>(Excepts.ToLongFunction<E, T> getter,
		Excepts.ObjLongConsumer<E, T> setter) {

		/**
		 * Create a no-op, stateless instance.
		 */
		public static <T> Long<RuntimeException, T> ofNull() {
			return new Long<>(_ -> 0L, (_, _) -> {});
		}

		/**
		 * Apply a mask of bit count and shift to current source access.
		 */
		public Long<E, T> bits(int shift, int count) {
			return mask(Mask.ofBits(shift, count));
		}

		/**
		 * Apply an absolute mask and bit shift to current source access.
		 */
		public Long<E, T> mask(int shift, long mask) {
			return mask(Mask.of(shift, mask));
		}

		/**
		 * Apply the mask transcoder to current source access.
		 */
		public Long<E, T> mask(Mask mask) {
			return new Long<>(t -> mask.decode(getter().applyAsLong(t)),
				(t, v) -> setter.accept(t, mask.encode(getter().applyAsLong(t), v)));
		}

		/**
		 * Provide a source transcoder for a single typed value.
		 */
		public <U> Type<E, T, U> type(Xcoder.Type<U> xcoder) {
			return new Type<>(this::get, this::set, xcoder);
		}

		/**
		 * Provide a source transcoder for a single typed value.
		 */
		public <U> Types<E, T, U> types(Xcoder.Types<U> xcoder) {
			return new Types<>(this::get, this::set, xcoder);
		}

		/**
		 * Get source value.
		 */
		public long get(T source) throws E {
			return valid(getter, "Get").applyAsLong(source);
		}

		/**
		 * Get source value as boolean.
		 */
		public boolean getBool(T source) throws E {
			return get(source) != 0L;
		}

		/**
		 * Get int source value.
		 */
		public int getInt(T source) throws E {
			return (int) get(source);
		}

		/**
		 * Get unsigned int source value.
		 */
		public long getUint(T source) throws E {
			return Maths.uint(get(source));
		}

		/**
		 * Get unsigned int source value; throws exception if out of range.
		 */
		public int getUintExact(T source) throws E {
			return Maths.uintExact(get(source));
		}

		/**
		 * Set source value.
		 */
		public Long<E, T> set(T source, long value) throws E {
			valid(setter(), "Set").accept(source, value);
			return this;
		}

		/**
		 * Set source value to all on or all off.
		 */
		public Long<E, T> set(T source, boolean allOn) throws E {
			return set(source, allOn ? -1L : 0L);
		}

		/**
		 * Set unsigned int source value.
		 */
		public Long<E, T> setUint(T source, long value) throws E {
			return set(source, Maths.uint(value));
		}

		/**
		 * Apply the operator to the source value.
		 */
		public Long<E, T> apply(T source, Excepts.LongOperator<E> operator) throws E {
			long value = get(source);
			long modified = operator.applyAsLong(value);
			if (modified != value) set(source, modified);
			return this;
		}
	}

	/**
	 * A single type field transcoder.
	 */
	public static class Type<E extends Exception, S, T> {
		private final Excepts.ToLongFunction<E, S> getter;
		private final Excepts.ObjLongConsumer<E, S> setter;
		private final Xcoder.Type<T> xcoder;

		private Type(Excepts.ToLongFunction<E, S> getter, Excepts.ObjLongConsumer<E, S> setter,
			Xcoder.Type<T> xcoder) {
			this.getter = getter;
			this.setter = setter;
			this.xcoder = xcoder;
		}

		/**
		 * Provides access to the type transcoder.
		 */
		public Xcoder.Type<T> xcoder() {
			return xcoder;
		}

		/**
		 * Returns the field int value.
		 */
		public long getValue(S source) throws E {
			return getter.applyAsLong(source);
		}

		/**
		 * Sets the field int value.
		 */
		public void setValue(S source, long value) throws E {
			setter.accept(source, value);
		}

		/**
		 * Sets the encoded type as the field value.
		 */
		public void set(S source, T t) throws E {
			setValue(source, xcoder().encode(t));
		}

		/**
		 * Returns the decoded type from the field value.
		 */
		public T get(S source) throws E {
			return xcoder().decode(getValue(source));
		}

		/**
		 * Returns the decoded type from the field value; fails if not exact.
		 */
		public T getValid(S source) throws E {
			return getValid(source, "");
		}

		/**
		 * Returns the decoded type from the field value; fails if not exact.
		 */
		public T getValid(S source, String format, Object... args) throws E {
			return xcoder().decodeValid(getValue(source), format, args);
		}

		/**
		 * Returns true if the type has all bits within the value.
		 */
		public boolean has(S source, T t) throws E {
			return xcoder().has(getValue(source), t);
		}
	}

	/**
	 * A multiple type field transcoder.
	 */
	public static class Types<E extends Exception, S, T> extends Type<E, S, T> {
		private Types(Excepts.ToLongFunction<E, S> getter, Excepts.ObjLongConsumer<E, S> setter,
			Xcoder.Types<T> xcoder) {
			super(getter, setter, xcoder);
		}

		@Override
		public Xcoder.Types<T> xcoder() {
			return (Xcoder.Types<T>) super.xcoder();
		}

		/**
		 * Adds encoded types to the field value.
		 */
		@SafeVarargs
		public final void add(S source, T... ts) throws E {
			add(source, Lists.wrap(ts));
		}

		/**
		 * Adds encoded types to the field value.
		 */
		public void add(S source, Iterable<T> ts) throws E {
			setValue(source, xcoder().add(getValue(source), ts));
		}

		/**
		 * Removes encoded types from the field value.
		 */
		@SafeVarargs
		public final void remove(S source, T... ts) throws E {
			remove(source, Lists.wrap(ts));
		}

		/**
		 * Removes encoded types from the field value.
		 */
		public void remove(S source, Iterable<T> ts) throws E {
			setValue(source, xcoder().remove(getValue(source), ts));
		}

		/**
		 * Sets the encoded types as the field value.
		 */
		@SafeVarargs
		public final void set(S source, T... ts) throws E {
			set(source, Lists.wrap(ts));
		}

		/**
		 * Sets the encoded types as the field value.
		 */
		public void set(S source, Iterable<T> ts) throws E {
			setValue(source, xcoder().encode(ts));
		}

		/**
		 * Gets the decoded types from the field value.
		 */
		public Set<T> getAll(S source) throws E {
			return xcoder().decodeAll(getValue(source));
		}

		/**
		 * Gets the decoded types from the field value; fails if not exact.
		 */
		public Set<T> getAllValid(S source) throws E {
			return getAllValid(source, "");
		}

		/**
		 * Gets the decoded types from the field value; fails if not exact.
		 */
		public Set<T> getAllValid(S source, String format, Object... args) throws E {
			return xcoder().decodeAllValid(getValue(source), format, args);
		}

		/**
		 * Returns true if the type bits are within the value, with possible remainder.
		 */
		@SafeVarargs
		public final boolean hasAll(S source, T... ts) throws E {
			return hasAll(source, Lists.wrap(ts));
		}

		/**
		 * Returns true if the type bits are within the value, with possible remainder.
		 */
		public boolean hasAll(S source, Iterable<T> ts) throws E {
			return xcoder().hasAll(getValue(source), ts);
		}
	}

	private static <T> T valid(T value, String name) {
		if (value != null) return value;
		throw Exceptions.unsupportedOp("%s is not supported", name);
	}
}
