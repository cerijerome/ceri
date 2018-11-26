package ceri.common.data;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;

public interface IntAccessor {
	
	void set(int value);
	int get();

	static IntAccessor of(IntSupplier getFn, IntConsumer setFn) {
		return new IntAccessor() {
			@Override
			public void set(int value) {
				setFn.accept(value);
			}
			@Override
			public int get() {
				return getFn.getAsInt();
			}
		};
	}

	static <T> IntAccessor.Typed<T> typed(ToIntFunction<T> getFn, ObjIntConsumer<T> setFn) {
		return new Typed<>(getFn, setFn);
	}

	static class Typed<T> {
		public final ToIntFunction<T> getFn;
		public final ObjIntConsumer<T> setFn;

		Typed(ToIntFunction<T> getFn, ObjIntConsumer<T> setFn) {
			this.getFn = getFn;
			this.setFn = setFn;
		}

		public IntAccessor from(T t) {
			return IntAccessor.of(() -> getFn.applyAsInt(t), i -> setFn.accept(t, i));
		}
		
		public int get(T value) {
			return getFn.applyAsInt(value);
		}
		
		public void set(T t, int value) {
			setFn.accept(t, value);
		}		
	}

}
