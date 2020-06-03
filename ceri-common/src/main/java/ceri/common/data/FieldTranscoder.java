package ceri.common.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import ceri.common.data.TypeTranscoder.Remainder;

/**
 * Combines an integer field accessor with a type transcoder to allow callers to get/set types on a
 * field that is a plain integer. Typically instantiated from TypeTranscoder.field.
 */
public class FieldTranscoder<T> {
	private final IntAccessor accessor;
	private final TypeTranscoder<T> xcoder;

	public static <T> FieldTranscoder<T> of(IntAccessor accessor, TypeTranscoder<T> xcoder) {
		return new FieldTranscoder<>(accessor, xcoder);
	}

	FieldTranscoder(IntAccessor accessor, TypeTranscoder<T> xcoder) {
		this.accessor = accessor;
		this.xcoder = xcoder;
	}

	public IntAccessor accessor() {
		return accessor;
	}

	@SafeVarargs
	public final FieldTranscoder<T> remove(T... ts) {
		return ts.length == 0 ? this : remove(Arrays.asList(ts));
	}

	public FieldTranscoder<T> remove(Collection<T> ts) {
		if (!ts.isEmpty()) accessor().remove(xcoder.encode(ts));
		return this;
	}

	@SafeVarargs
	public final FieldTranscoder<T> add(T... ts) {
		return ts.length == 0 ? this : add(Arrays.asList(ts));
	}

	public FieldTranscoder<T> add(Collection<T> ts) {
		if (!ts.isEmpty()) accessor().add(xcoder.encode(ts));
		return this;
	}

	@SafeVarargs
	public final FieldTranscoder<T> set(T... ts) {
		return set(Arrays.asList(ts));
	}

	public FieldTranscoder<T> set(Collection<T> ts) {
		accessor().set(xcoder.encode(ts));
		return this;
	}

	public FieldTranscoder<T> set(Remainder<T> rem) {
		accessor().set(xcoder.encode(rem));
		return this;
	}

	public T get() {
		return xcoder.decode(accessor().get());
	}

	public Set<T> getAll() {
		return xcoder.decodeAll(accessor().get());
	}

	public Remainder<T> getWithRemainder() {
		return xcoder.decodeWithRemainder(accessor().get());
	}

	public boolean isValid() {
		return xcoder.isValid(accessor().get());
	}

	public final boolean has(T t) {
		return xcoder.has(accessor.get(), t);
	}

	@SafeVarargs
	public final boolean hasAny(T... ts) {
		return hasAny(Arrays.asList(ts));
	}

	public boolean hasAny(Collection<T> ts) {
		return xcoder.hasAny(accessor.get(), ts);
	}

	@SafeVarargs
	public final boolean hasAll(T... ts) {
		return hasAll(Arrays.asList(ts));
	}

	public boolean hasAll(Collection<T> ts) {
		return xcoder.hasAll(accessor.get(), ts);
	}

	public static class Typed<S, T> {
		private final IntAccessor.Typed<S> accessor;
		private final TypeTranscoder<T> xcoder;

		public static <S, T> Typed<S, T> of(IntAccessor.Typed<S> accessor,
			TypeTranscoder<T> xcoder) {
			return new Typed<>(accessor, xcoder);
		}

		Typed(IntAccessor.Typed<S> accessor, TypeTranscoder<T> xcoder) {
			this.accessor = accessor;
			this.xcoder = xcoder;
		}

		public IntAccessor.Typed<S> accessor() {
			return accessor;
		}

		@SafeVarargs
		public final Typed<S, T> remove(S s, T... ts) {
			return ts.length == 0 ? this : remove(s, Arrays.asList(ts));
		}

		public Typed<S, T> remove(S s, Collection<T> ts) {
			if (!ts.isEmpty()) accessor().remove(s, xcoder.encode(ts));
			return this;
		}

		@SafeVarargs
		public final Typed<S, T> add(S s, T... ts) {
			return ts.length == 0 ? this : add(s, Arrays.asList(ts));
		}

		public Typed<S, T> add(S s, Collection<T> ts) {
			if (!ts.isEmpty()) accessor().add(s, xcoder.encode(ts));
			return this;
		}

		@SafeVarargs
		public final Typed<S, T> set(S s, T... ts) {
			return set(s, Arrays.asList(ts));
		}

		public Typed<S, T> set(S s, Collection<T> ts) {
			accessor().set(s, xcoder.encode(ts));
			return this;
		}

		public Typed<S, T> set(S s, Remainder<T> rem) {
			accessor().set(s, xcoder.encode(rem));
			return this;
		}

		public T get(S s) {
			return xcoder.decode(accessor().get(s));
		}

		public Set<T> getAll(S s) {
			return xcoder.decodeAll(accessor().get(s));
		}

		public Remainder<T> getWithRemainder(S s) {
			return xcoder.decodeWithRemainder(accessor().get(s));
		}

		public boolean isValid(S s) {
			return xcoder.isValid(accessor().get(s));
		}

		public boolean has(S s, T t) {
			return xcoder.has(accessor.get(s), t);
		}

		@SafeVarargs
		public final boolean hasAny(S s, T... ts) {
			return hasAny(s, Arrays.asList(ts));
		}

		public boolean hasAny(S s, Collection<T> ts) {
			return xcoder.hasAny(accessor.get(s), ts);
		}

		@SafeVarargs
		public final boolean hasAll(S s, T... ts) {
			return hasAll(s, Arrays.asList(ts));
		}

		public boolean hasAll(S s, Collection<T> ts) {
			return xcoder.hasAll(accessor.get(s), ts);
		}
	}

}
