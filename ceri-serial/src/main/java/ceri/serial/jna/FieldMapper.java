package ceri.serial.jna;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public abstract class FieldMapper {
	final IntSupplier getFn;
	final IntConsumer setFn;

	public static class Single<T> extends FieldMapper {
		private final TypeTranscoder.Single<T> xcoder;
		
		Single(IntSupplier getFn, IntConsumer setFn,
			TypeTranscoder.Single<T> xcoder) {
			super(getFn, setFn);
			this.xcoder = xcoder;
		}

		public void set(T t) {
			setFn.accept(xcoder.encode(t));
		}

		public T get() {
			return xcoder.decode(getFn.getAsInt());
		}

		public boolean isValid() {
			return xcoder.isValid(getFn.getAsInt());
		}
	}
	
	public static class Flag<T> extends FieldMapper {
		private final TypeTranscoder.Flag<T> xcoder;
		
		Flag(IntSupplier getFn, IntConsumer setFn,
			TypeTranscoder.Flag<T> xcoder) {
			super(getFn, setFn);
			this.xcoder = xcoder;
		}

		@SafeVarargs
		public final void set(T...ts) {
			set(Arrays.asList(ts));
		}

		public void set(Collection<T> ts) {
			setFn.accept(xcoder.encode(ts));
		}

		public Set<T> get() {
			return xcoder.decode(getFn.getAsInt());
		}

		public boolean isValid() {
			return xcoder.isValid(getFn.getAsInt());
		}
	}
	
	public static <T> FieldMapper.Single<T> single(IntSupplier getFn, IntConsumer setFn,
		TypeTranscoder.Single<T> xcoder) {
		return new FieldMapper.Single<>(getFn, setFn, xcoder);
	}
	
	public static <T> FieldMapper.Flag<T> flag(IntSupplier getFn, IntConsumer setFn,
		TypeTranscoder.Flag<T> xcoder) {
		return new FieldMapper.Flag<>(getFn, setFn, xcoder);
	}
	
	FieldMapper(IntSupplier getFn, IntConsumer setFn) {
		this.getFn = getFn;
		this.setFn = setFn;
	}

	public int raw() {
		return getFn.getAsInt();
	}
	
}
