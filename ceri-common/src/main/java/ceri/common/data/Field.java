package ceri.common.data;

import static ceri.common.math.MathUtil.uint;
import static ceri.common.math.MathUtil.uintExact;
import static ceri.common.validation.ValidationUtil.validateSupported;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import ceri.common.function.ExceptionBiConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionLongUnaryOperator;
import ceri.common.function.ExceptionObjIntConsumer;
import ceri.common.function.ExceptionObjLongConsumer;
import ceri.common.function.ExceptionToIntFunction;
import ceri.common.function.ExceptionToLongFunction;
import ceri.common.math.MathUtil;

/**
 * Provides access to a field.
 */
public record Field<E extends Exception, T, U>(ExceptionFunction<E, T, U> getter,
	ExceptionBiConsumer<E, T, U> setter) {

	/**
	 * Create a no-op, stateless instance.
	 */
	public static <T, U> Field<RuntimeException, T, U> ofNull() {
		return new Field<>(t -> null, (t, v) -> {});
	}

	/**
	 * Create an instance with getter and setter for a long field.
	 */
	public static <E extends Exception, T, U> Field<E, T, U> of(ExceptionFunction<E, T, U> getter,
		ExceptionBiConsumer<E, T, U> setter) {
		return new Field<>(getter, setter);
	}

	/**
	 * Create an instance with getter and setter for an unsigned int field.
	 */
	public static <E extends Exception, T> Long<E, T> ofUint(ExceptionToIntFunction<E, T> getter,
		ExceptionObjIntConsumer<E, T> setter) {
		return ofLong(getter == null ? null : t -> uint(getter.applyAsInt(t)),
			setter == null ? null : (t, v) -> setter.accept(t, (int) v));
	}

	/**
	 * Create an instance with getter and setter for a long field.
	 */
	public static <E extends Exception, T> Long<E, T> ofLong(ExceptionToLongFunction<E, T> getter,
		ExceptionObjLongConsumer<E, T> setter) {
		return new Long<>(getter, setter);
	}

	/**
	 * Get source value.
	 */
	public U get(T source) throws E {
		return validateSupported(getter, "Get").apply(source);
	}

	/**
	 * Set source value.
	 */
	public Field<E, T, U> set(T source, U value) throws E {
		validateSupported(setter(), "Set").accept(source, value);
		return this;
	}

	/**
	 * A long field accessor. Covers all integral types.
	 */
	public record Long<E extends Exception, T>(ExceptionToLongFunction<E, T> getter,
		ExceptionObjLongConsumer<E, T> setter) {

		/**
		 * Create a no-op, stateless instance.
		 */
		public static <T> Long<RuntimeException, T> ofNull() {
			return new Long<>(t -> 0L, (t, v) -> {});
		}

		/**
		 * Apply an absolute mask and bit shift to current source access.
		 */
		public Long<E, T> masked(long mask, int shiftBits) {
			return masked(MaskTranscoder.mask(mask, shiftBits));
		}

		/**
		 * Apply the mask transcoder to current source access.
		 */
		public Long<E, T> masked(MaskTranscoder mask) {
			return new Long<>(t -> mask.decode(getter().applyAsLong(t)),
				(t, v) -> setter.accept(t, mask.encode(v, getter().applyAsLong(t))));
		}

		/**
		 * Provide a source transcoder for multiple typed values.
		 */
		public <U> Typed<E, T, U> typed(TypeTranscoder<U> xcoder) {
			return new Typed<>(this, xcoder);
		}

		/**
		 * Get source value.
		 */
		public long get(T source) throws E {
			return validateSupported(getter, "Get").applyAsLong(source);
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
			return uint(get(source));
		}

		/**
		 * Get unsigned int source value; throws exception if out of range.
		 */
		public int getUintExact(T source) throws E {
			return uintExact(get(source));
		}

		/**
		 * Set source value.
		 */
		public Long<E, T> set(T source, long value) throws E {
			validateSupported(setter(), "Set").accept(source, value);
			return this;
		}

		/**
		 * Set unsigned int source value.
		 */
		public Long<E, T> setUint(T source, long value) throws E {
			return set(source, MathUtil.uint(value));
		}

		/**
		 * Apply the bitwise operator to the source value.
		 */
		public Long<E, T> or(T source, long value) throws E {
			return apply(source, n -> n | value);
		}

		/**
		 * Apply the bitwise operator to the source value.
		 */
		public Long<E, T> and(T source, long value) throws E {
			return apply(source, n -> n & value);
		}

		/**
		 * Apply the operator to the source value.
		 */
		public Long<E, T> apply(T source, ExceptionLongUnaryOperator<E> operator) throws E {
			long value = get(source);
			long modified = operator.applyAsLong(value);
			if (modified != value) set(source, modified);
			return this;
		}
	}

	/**
	 * A field transcoder for multiple typed values.
	 */
	public record Typed<E extends Exception, T, U>(Long<E, T> field, TypeTranscoder<U> xcoder) {

		/**
		 * Sets the encoded types as the field value.
		 */
		@SafeVarargs
		public final Typed<E, T, U> set(T source, U... types) throws E {
			return set(source, Arrays.asList(types));
		}

		/**
		 * Sets the encoded types as the field value.
		 */
		public Typed<E, T, U> set(T source, Iterable<U> types) throws E {
			field().set(source, xcoder().encode(types));
			return this;
		}

		/**
		 * Adds the encoded types to the field value.
		 */
		@SafeVarargs
		public final Typed<E, T, U> add(T source, U... types) throws E {
			return add(source, Arrays.asList(types));
		}

		/**
		 * Adds the encoded types to the field value.
		 */
		public Typed<E, T, U> add(T source, Iterable<U> types) throws E {
			field().or(source, xcoder().encode(types));
			return this;
		}

		/**
		 * Removes the encoded types from the field value.
		 */
		@SafeVarargs
		public final Typed<E, T, U> remove(T source, U... types) throws E {
			return remove(source, Arrays.asList(types));
		}

		/**
		 * Removes the encoded types from the field value.
		 */
		public Typed<E, T, U> remove(T source, Iterable<U> types) throws E {
			field().and(source, ~xcoder().encode(types));
			return this;
		}

		/**
		 * Decode the field value into multiple types. Iteration over the types is in lookup entry
		 * order. Any remainder is discarded.
		 */
		public Set<U> get(T source) throws E {
			return xcoder().decodeAll(field.get(source));
		}

		/**
		 * Decode the field value into multiple types, and add to the given collection. Iteration
		 * over the types is in lookup entry order. Any remainder is discarded.
		 */
		public <C extends Collection<U>> C get(T source, C collection) throws E {
			return xcoder().decodeAll(collection, field.get(source));
		}

		/**
		 * Returns true if the field value has all the encoded types.
		 */
		@SafeVarargs
		public final boolean has(T source, U... types) throws E {
			return has(source, Arrays.asList(types));
		}

		/**
		 * Returns true if the field value has all the encoded types.
		 */
		public boolean has(T source, Iterable<U> types) throws E {
			return xcoder().hasAll(field.get(source), types);
		}
	}
}
