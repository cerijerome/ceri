package ceri.common.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public abstract class FieldTranscoder {
	private final IntAccessor accessor;

	public static class Single<T> extends FieldTranscoder {
		private final TypeTranscoder.Single<T> xcoder;

		Single(IntAccessor accessor, TypeTranscoder.Single<T> xcoder) {
			super(accessor);
			this.xcoder = xcoder;
		}

		public void set(T t) {
			accessor().set(xcoder.encode(t));
		}

		public T get() {
			return xcoder.decode(accessor().get());
		}

		public boolean isValid() {
			return xcoder.isValid(accessor().get());
		}
	}

	public static class Flag<T> extends FieldTranscoder {
		private final TypeTranscoder.Flag<T> xcoder;

		Flag(IntAccessor accessor, TypeTranscoder.Flag<T> xcoder) {
			super(accessor);
			this.xcoder = xcoder;
		}

		@SafeVarargs
		public final void set(T... ts) {
			set(Arrays.asList(ts));
		}

		public void set(Collection<T> ts) {
			accessor().set(xcoder.encode(ts));
		}

		public Set<T> get() {
			return xcoder.decode(accessor().get());
		}

		public boolean isValid() {
			return xcoder.isValid(accessor().get());
		}
	}

	public static <T> FieldTranscoder.Single<T> single(IntAccessor accessor,
		TypeTranscoder.Single<T> xcoder) {
		return new FieldTranscoder.Single<>(accessor, xcoder);
	}

	public static <T> FieldTranscoder.Flag<T> flag(IntAccessor accessor,
		TypeTranscoder.Flag<T> xcoder) {
		return new FieldTranscoder.Flag<>(accessor, xcoder);
	}

	FieldTranscoder(IntAccessor accessor) {
		this.accessor = accessor;
	}

	public IntAccessor accessor() {
		return accessor;
	}

}
