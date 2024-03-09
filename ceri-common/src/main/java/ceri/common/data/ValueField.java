package ceri.common.data;

import static ceri.common.math.MathUtil.uint;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

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
public interface ValueField {

	long get();

	void set(long value);

	default int getInt() {
		return (int) get();
	}

	default long getUint() {
		return uint(get());
	}

	default void setUint(int value) {
		set(uint(value));
	}

	/**
	 * Add value with bitwise-or.
	 */
	default ValueField add(long value) {
		set(get() | value);
		return this;
	}

	/**
	 * Remove value by masking bits.
	 */
	default ValueField remove(long value) {
		set(get() & ~value);
		return this;
	}

	default ValueField mask(long mask) {
		return mask(MaskTranscoder.mask(mask, 0));
	}

	default ValueField mask(MaskTranscoder mask) {
		LongSupplier getFn = () -> mask.decode(get());
		LongConsumer setFn = i -> set(mask.encode(i, get()));
		return of(getFn, setFn);
	}

	/**
	 * Create accessor from get and set functions. Either may be null.
	 */
	static ValueField ofInt(IntSupplier getFn, IntConsumer setFn) {
		return of(getFn == null ? null : getFn::getAsInt,
			setFn == null ? null : l -> setFn.accept((int) l));
	}

	/**
	 * Create accessor from get and set functions. Either may be null.
	 */
	static ValueField of(LongSupplier getFn, LongConsumer setFn) {
		return new ValueField() {
			@Override
			public void set(long value) {
				if (setFn == null) throw new UnsupportedOperationException();
				setFn.accept(value);
			}

			@Override
			public long get() {
				if (getFn == null) throw new UnsupportedOperationException();
				return getFn.getAsLong();
			}
		};
	}

	/**
	 * Provides getter and setter access to a long value, for a given typed object.
	 */
	static class Typed<T> {
		public final ToLongFunction<T> getFn;
		public final ObjLongConsumer<T> setFn;

		public static <T> ValueField.Typed<T> ofInt(ToIntFunction<T> getFn,
			ObjIntConsumer<T> setFn) {
			return of(getFn == null ? null : getFn::applyAsInt,
				setFn == null ? null : (t, l) -> setFn.accept(t, (int) l));
		}

		public static <T> ValueField.Typed<T> of(ToLongFunction<T> getFn,
			ObjLongConsumer<T> setFn) {
			return new Typed<>(getFn, setFn);
		}

		private Typed(ToLongFunction<T> getFn, ObjLongConsumer<T> setFn) {
			this.getFn = getFn;
			this.setFn = setFn;
		}

		public ValueField from(T t) {
			return ValueField.of(() -> get(t), i -> set(t, i));
		}

		/**
		 * Apply a mask when accessing the value.
		 */
		public ValueField.Typed<T> mask(MaskTranscoder mask) {
			return of(t -> mask.decode(get(t)), (t, i) -> set(t, mask.encode(i, get(t))));
		}

		/**
		 * Get the value from the typed object.
		 */
		public long get(T value) {
			if (getFn == null) throw new UnsupportedOperationException();
			return getFn.applyAsLong(value);
		}

		/**
		 * Get the value from the typed object.
		 */
		public int getInt(T value) {
			return (int) get(value);
		}

		/**
		 * Set the value on the typed object.
		 */
		public void set(T t, long value) {
			if (setFn == null) throw new UnsupportedOperationException();
			setFn.accept(t, value);
		}

		/**
		 * Add value with bitwise-or.
		 */
		public long add(T t, long value) {
			var added = get(t) | value;
			set(t, added);
			return added;
		}

		/**
		 * Remove value by masking bits.
		 */
		public long remove(T t, long value) {
			var removed = get(t) & ~value;
			set(t, removed);
			return removed;
		}
	}
}
