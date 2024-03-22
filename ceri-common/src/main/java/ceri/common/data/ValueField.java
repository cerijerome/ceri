package ceri.common.data;

import static ceri.common.math.MathUtil.uint;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionIntSupplier;
import ceri.common.function.ExceptionLongConsumer;
import ceri.common.function.ExceptionLongSupplier;
import ceri.common.function.ExceptionObjIntConsumer;
import ceri.common.function.ExceptionObjLongConsumer;
import ceri.common.function.ExceptionToIntFunction;
import ceri.common.function.ExceptionToLongFunction;

/**
 * Interface to get and set values within an object. The main interface holds a getter and setter
 * function.
 * <p/>
 * The Typed nested class requires a typed object to be passed in to a getter and setter. This
 * allows the typed accessor to be statically declared on types.
 * <p/>
 * A mask transcoder can be applied to either accessor type, to mask and shift bits when calling
 * access methods. This allows only a subset of bits to be affected during access.
 */
public interface ValueField<E extends Exception> {

	long get() throws E;

	void set(long value) throws E;

	default int getInt() throws E {
		return (int) get();
	}

	default long getUint() throws E {
		return uint(get());
	}

	default void setUint(int value) throws E {
		set(uint(value));
	}

	/**
	 * Add value with bitwise-or.
	 */
	default ValueField<E> add(long value) throws E {
		set(get() | value);
		return this;
	}

	/**
	 * Remove value by masking bits.
	 */
	default ValueField<E> remove(long value) throws E {
		set(get() & ~value);
		return this;
	}

	default ValueField<E> mask(long mask) {
		return mask(MaskTranscoder.mask(mask, 0));
	}

	default ValueField<E> mask(MaskTranscoder mask) {
		ExceptionLongSupplier<E> getFn = () -> mask.decode(get());
		ExceptionLongConsumer<E> setFn = i -> set(mask.encode(i, get()));
		return of(getFn, setFn);
	}

	static <E extends Exception> ValueField<E> ofNull() {
		return of(() -> 0L, l -> {});
	}
	
	/**
	 * Create accessor from get and set functions. Either may be null.
	 */
	static <E extends Exception> ValueField<E> ofInt(ExceptionIntSupplier<E> getFn,
		ExceptionIntConsumer<E> setFn) {
		return of(getFn == null ? null : getFn::getAsInt,
			setFn == null ? null : l -> setFn.accept((int) l));
	}

	/**
	 * Create accessor from get and set functions. Either may be null.
	 */
	static <E extends Exception> ValueField<E> of(ExceptionLongSupplier<E> getFn,
		ExceptionLongConsumer<E> setFn) {
		return new ValueField<>() {
			@Override
			public void set(long value) throws E {
				if (setFn == null) throw new UnsupportedOperationException();
				setFn.accept(value);
			}

			@Override
			public long get() throws E {
				if (getFn == null) throw new UnsupportedOperationException();
				return getFn.getAsLong();
			}
		};
	}

	/**
	 * Provides getter and setter access to a long value, for a given typed object.
	 */
	static class Typed<E extends Exception, T> {
		public final ExceptionToLongFunction<E, T> getFn;
		public final ExceptionObjLongConsumer<E, T> setFn;

		public static <E extends Exception, T> ValueField.Typed<E, T> ofNull() {
			return of(t -> 0L, (t, l) -> {});
		}
		
		public static <E extends Exception, T> ValueField.Typed<E, T>
			ofInt(ExceptionToIntFunction<E, T> getFn, ExceptionObjIntConsumer<E, T> setFn) {
			return of(getFn == null ? null : getFn::applyAsInt,
				setFn == null ? null : (t, l) -> setFn.accept(t, (int) l));
		}

		public static <E extends Exception, T> ValueField.Typed<E, T>
			of(ExceptionToLongFunction<E, T> getFn, ExceptionObjLongConsumer<E, T> setFn) {
			return new Typed<>(getFn, setFn);
		}

		private Typed(ExceptionToLongFunction<E, T> getFn, ExceptionObjLongConsumer<E, T> setFn) {
			this.getFn = getFn;
			this.setFn = setFn;
		}

		public ValueField<E> from(T t) {
			return ValueField.of(() -> get(t), i -> set(t, i));
		}

		/**
		 * Apply a mask when accessing the value.
		 */
		public ValueField.Typed<E, T> mask(MaskTranscoder mask) {
			return of(t -> mask.decode(get(t)), (t, i) -> set(t, mask.encode(i, get(t))));
		}

		/**
		 * Get the value from the typed object.
		 */
		public long get(T value) throws E {
			if (getFn == null) throw new UnsupportedOperationException();
			return getFn.applyAsLong(value);
		}

		/**
		 * Get the value from the typed object.
		 */
		public int getInt(T value) throws E {
			return (int) get(value);
		}

		/**
		 * Set the value on the typed object.
		 */
		public void set(T t, long value) throws E {
			if (setFn == null) throw new UnsupportedOperationException();
			setFn.accept(t, value);
		}

		/**
		 * Add value with bitwise-or.
		 */
		public long add(T t, long value) throws E {
			var added = get(t) | value;
			set(t, added);
			return added;
		}

		/**
		 * Remove value by masking bits.
		 */
		public long remove(T t, long value) throws E {
			var removed = get(t) & ~value;
			set(t, removed);
			return removed;
		}
	}
}
