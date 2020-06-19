package ceri.common.data;

import static ceri.common.function.FunctionUtil.safeApply;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;
import ceri.common.function.ObjByteConsumer;
import ceri.common.function.ObjShortConsumer;
import ceri.common.function.ToByteFunction;
import ceri.common.function.ToShortFunction;

/**
 * Interface to get and set integer values within an object. The main interface holds a getter and
 * setter function.
 * <p/>
 * The Typed nested class requires a typed object to be passed in to a getter and setter. This
 * allows the typed accessor to be statically declared on types.
 * <p/>
 * A mask transcoder can be applied to either accessor type, to mask and shift bits when calling
 * access methods. This allows only a subset of bits to be affected during access.
 */
public interface IntAccessor {

	void set(int value);

	int get();

	/**
	 * Add value with bitwise-or.
	 */
	default int add(int value) {
		int added = get() | value;
		set(added);
		return added;
	}

	/**
	 * Remove value by masking bits.
	 */
	default int remove(int value) {
		int removed = get() & ~value;
		set(removed);
		return removed;
	}

	default IntAccessor mask(int mask) {
		return mask(MaskTranscoder.mask(mask, 0));
	}

	default IntAccessor mask(MaskTranscoder mask) {
		IntSupplier getFn = () -> mask.decodeInt(get());
		IntConsumer setFn = i -> set(mask.encodeInt(i, get()));
		return of(getFn, setFn);
	}

	static IntAccessor getter(IntSupplier getFn) {
		return of(getFn, null);
	}

	static IntAccessor setter(IntConsumer setFn) {
		return of(null, setFn);
	}

	/**
	 * Create accessor from get and set functions. Either may be null.
	 */
	static IntAccessor of(IntSupplier getFn, IntConsumer setFn) {
		return new IntAccessor() {
			@Override
			public void set(int value) {
				if (setFn == null) throw new UnsupportedOperationException();
				setFn.accept(value);
			}

			@Override
			public int get() {
				if (getFn == null) throw new UnsupportedOperationException();
				return getFn.getAsInt();
			}
		};
	}

	static <T> IntAccessor.Typed<T> typed(ToIntFunction<T> getFn, ObjIntConsumer<T> setFn) {
		return new Typed<>(getFn, setFn);
	}

	static <T> IntAccessor.Typed<T> typedUbyte(ToByteFunction<T> getFn, ObjByteConsumer<T> setFn) {
		return typed(safeApply(getFn, ToByteFunction::toUint),
			safeApply(setFn, ObjByteConsumer::toUintExact));
	}

	static <T> IntAccessor.Typed<T> typedUshort(ToShortFunction<T> getFn,
		ObjShortConsumer<T> setFn) {
		return typed(safeApply(getFn, ToShortFunction::toUint),
			safeApply(setFn, ObjShortConsumer::toUintExact));
	}

	/**
	 * Provides getter and setter access to an integer value, for a given typed object.
	 */
	static class Typed<T> {
		public final ToIntFunction<T> getFn;
		public final ObjIntConsumer<T> setFn;

		Typed(ToIntFunction<T> getFn, ObjIntConsumer<T> setFn) {
			this.getFn = getFn;
			this.setFn = setFn;
		}

		public IntAccessor from(T t) {
			return IntAccessor.of(() -> get(t), i -> set(t, i));
		}

		/**
		 * Apply a mask when accessing the value.
		 */
		public IntAccessor.Typed<T> mask(MaskTranscoder mask) {
			ToIntFunction<T> getFn = t -> mask.decodeInt(get(t));
			ObjIntConsumer<T> setFn = (t, i) -> set(t, mask.encodeInt(i, get(t)));
			return typed(getFn, setFn);
		}

		/**
		 * Get the value from the typed object.
		 */
		public int get(T value) {
			if (getFn == null) throw new UnsupportedOperationException();
			return getFn.applyAsInt(value);
		}

		/**
		 * Set the value on the typed object.
		 */
		public void set(T t, int value) {
			if (setFn == null) throw new UnsupportedOperationException();
			setFn.accept(t, value);
		}

		/**
		 * Add value with bitwise-or.
		 */
		public int add(T t, int value) {
			int added = get(t) | value;
			set(t, added);
			return added;
		}

		/**
		 * Remove value by masking bits.
		 */
		public int remove(T t, int value) {
			int removed = get(t) & ~value;
			set(t, removed);
			return removed;
		}

	}

}
