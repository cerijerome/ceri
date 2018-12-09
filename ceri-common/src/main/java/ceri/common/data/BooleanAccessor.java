package ceri.common.data;

import java.util.function.BooleanSupplier;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;
import ceri.common.function.BooleanConsumer;
import ceri.common.function.ObjBooleanConsumer;
import ceri.common.function.ObjByteConsumer;
import ceri.common.function.ObjShortConsumer;
import ceri.common.function.ToBooleanFunction;
import ceri.common.function.ToByteFunction;
import ceri.common.function.ToShortFunction;

public interface BooleanAccessor {

	void set(boolean value);

	boolean get();

	static BooleanAccessor of(BooleanSupplier getFn, BooleanConsumer setFn) {
		return new BooleanAccessor() {
			@Override
			public void set(boolean value) {
				setFn.accept(value);
			}

			@Override
			public boolean get() {
				return getFn.getAsBoolean();
			}
		};
	}

	static <T> BooleanAccessor.Typed<T> typed(ToBooleanFunction<T> getFn,
		ObjBooleanConsumer<T> setFn) {
		return new Typed<>(getFn, setFn);
	}

	static <T> BooleanAccessor.Typed<T> typedByte(ToByteFunction<T> getFn,
		ObjByteConsumer<T> setFn) {
		return new Typed<>(t -> getFn.applyAsByte(t) != 0,
			(t, b) -> setFn.accept(t, (byte) (b ? 1 : 0)));
	}

	static <T> BooleanAccessor.Typed<T> typedShort(ToShortFunction<T> getFn,
		ObjShortConsumer<T> setFn) {
		return new Typed<>(t -> getFn.applyAsShort(t) != 0,
			(t, b) -> setFn.accept(t, (short) (b ? 1 : 0)));
	}

	static <T> BooleanAccessor.Typed<T> typedInt(ToIntFunction<T> getFn, ObjIntConsumer<T> setFn) {
		return new Typed<>(t -> getFn.applyAsInt(t) != 0, (t, b) -> setFn.accept(t, b ? 1 : 0));
	}

	static class Typed<T> {
		public final ToBooleanFunction<T> getFn;
		public final ObjBooleanConsumer<T> setFn;

		Typed(ToBooleanFunction<T> getFn, ObjBooleanConsumer<T> setFn) {
			this.getFn = getFn;
			this.setFn = setFn;
		}

		public BooleanAccessor from(T t) {
			return BooleanAccessor.of(() -> getFn.applyAsBoolean(t), i -> setFn.accept(t, i));
		}

		public boolean get(T value) {
			return getFn.applyAsBoolean(value);
		}

		public void set(T t, boolean value) {
			setFn.accept(t, value);
		}
	}

}
