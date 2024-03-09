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
	private final ValueField field;
	private final TypeTranscoder<T> xcoder;

	public static <T> FieldTranscoder<T> of(ValueField field, TypeTranscoder<T> xcoder) {
		return new FieldTranscoder<>(field, xcoder);
	}

	FieldTranscoder(ValueField field, TypeTranscoder<T> xcoder) {
		this.field = field;
		this.xcoder = xcoder;
	}

	public ValueField field() {
		return field;
	}

	@SafeVarargs
	public final FieldTranscoder<T> remove(T... ts) {
		return ts.length == 0 ? this : remove(Arrays.asList(ts));
	}

	public FieldTranscoder<T> remove(Collection<T> ts) {
		if (!ts.isEmpty()) field().remove(xcoder.encode(ts));
		return this;
	}

	@SafeVarargs
	public final FieldTranscoder<T> add(T... ts) {
		return ts.length == 0 ? this : add(Arrays.asList(ts));
	}

	public FieldTranscoder<T> add(Collection<T> ts) {
		if (!ts.isEmpty()) field().add(xcoder.encode(ts));
		return this;
	}

	@SafeVarargs
	public final FieldTranscoder<T> set(T... ts) {
		return set(Arrays.asList(ts));
	}

	public FieldTranscoder<T> set(Collection<T> ts) {
		field().set(xcoder.encode(ts));
		return this;
	}

	public FieldTranscoder<T> set(Remainder<T> rem) {
		field().set(xcoder.encode(rem));
		return this;
	}

	public T get() {
		return xcoder.decode(field().get());
	}

	public Set<T> getAll() {
		return xcoder.decodeAll(field().get());
	}

	public Remainder<T> getWithRemainder() {
		return xcoder.decodeWithRemainder(field().get());
	}

	public boolean isValid() {
		return xcoder.isValid(field().get());
	}

	public final boolean has(T t) {
		return xcoder.has(field.get(), t);
	}

	@SafeVarargs
	public final boolean hasAny(T... ts) {
		return hasAny(Arrays.asList(ts));
	}

	public boolean hasAny(Collection<T> ts) {
		return xcoder.hasAny(field.get(), ts);
	}

	@SafeVarargs
	public final boolean hasAll(T... ts) {
		return hasAll(Arrays.asList(ts));
	}

	public boolean hasAll(Collection<T> ts) {
		return xcoder.hasAll(field.get(), ts);
	}

	public static class Typed<S, T> {
		private final ValueField.Typed<S> field;
		private final TypeTranscoder<T> xcoder;

		public static <S, T> Typed<S, T> of(ValueField.Typed<S> field, TypeTranscoder<T> xcoder) {
			return new Typed<>(field, xcoder);
		}

		Typed(ValueField.Typed<S> field, TypeTranscoder<T> xcoder) {
			this.field = field;
			this.xcoder = xcoder;
		}

		public ValueField.Typed<S> field() {
			return field;
		}

		@SafeVarargs
		public final Typed<S, T> remove(S s, T... ts) {
			return ts.length == 0 ? this : remove(s, Arrays.asList(ts));
		}

		public Typed<S, T> remove(S s, Collection<T> ts) {
			if (!ts.isEmpty()) field().remove(s, xcoder.encode(ts));
			return this;
		}

		@SafeVarargs
		public final Typed<S, T> add(S s, T... ts) {
			return ts.length == 0 ? this : add(s, Arrays.asList(ts));
		}

		public Typed<S, T> add(S s, Collection<T> ts) {
			if (!ts.isEmpty()) field().add(s, xcoder.encode(ts));
			return this;
		}

		@SafeVarargs
		public final Typed<S, T> set(S s, T... ts) {
			return set(s, Arrays.asList(ts));
		}

		public Typed<S, T> set(S s, Collection<T> ts) {
			field().set(s, xcoder.encode(ts));
			return this;
		}

		public Typed<S, T> set(S s, Remainder<T> rem) {
			field().set(s, xcoder.encode(rem));
			return this;
		}

		public T get(S s) {
			return xcoder.decode(field().get(s));
		}

		public Set<T> getAll(S s) {
			return xcoder.decodeAll(field().get(s));
		}

		public Remainder<T> getWithRemainder(S s) {
			return xcoder.decodeWithRemainder(field().get(s));
		}

		public boolean isValid(S s) {
			return xcoder.isValid(field().get(s));
		}

		public boolean has(S s, T t) {
			return xcoder.has(field.get(s), t);
		}

		@SafeVarargs
		public final boolean hasAny(S s, T... ts) {
			return hasAny(s, Arrays.asList(ts));
		}

		public boolean hasAny(S s, Collection<T> ts) {
			return xcoder.hasAny(field.get(s), ts);
		}

		@SafeVarargs
		public final boolean hasAll(S s, T... ts) {
			return hasAll(s, Arrays.asList(ts));
		}

		public boolean hasAll(S s, Collection<T> ts) {
			return xcoder.hasAll(field.get(s), ts);
		}
	}
}
