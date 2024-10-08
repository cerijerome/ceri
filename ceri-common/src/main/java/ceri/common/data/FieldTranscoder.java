package ceri.common.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import ceri.common.data.TypeTranscoder.Remainder;

/**
 * Combines an integer field accessor with a type transcoder to allow callers to get/set types on a
 * field that is a plain integer. Typically instantiated from TypeTranscoder.field.
 */
public class FieldTranscoder<E extends Exception, T> {
	private final ValueField<E> field;
	private final TypeTranscoder<T> xcoder;

	public static <E extends Exception, T> FieldTranscoder<E, T> of(ValueField<E> field,
		TypeTranscoder<T> xcoder) {
		return new FieldTranscoder<>(field, xcoder);
	}

	FieldTranscoder(ValueField<E> field, TypeTranscoder<T> xcoder) {
		this.field = field;
		this.xcoder = xcoder;
	}

	public ValueField<E> field() {
		return field;
	}

	@SafeVarargs
	public final FieldTranscoder<E, T> remove(T... ts) throws E {
		return ts.length == 0 ? this : remove(Arrays.asList(ts));
	}

	public FieldTranscoder<E, T> remove(Collection<T> ts) throws E {
		if (!ts.isEmpty()) field().remove(xcoder.encode(ts));
		return this;
	}

	@SafeVarargs
	public final FieldTranscoder<E, T> add(T... ts) throws E {
		return ts.length == 0 ? this : add(Arrays.asList(ts));
	}

	public FieldTranscoder<E, T> add(Collection<T> ts) throws E {
		if (!ts.isEmpty()) field().add(xcoder.encode(ts));
		return this;
	}

	@SafeVarargs
	public final FieldTranscoder<E, T> set(T... ts) throws E {
		return set(Arrays.asList(ts));
	}

	public FieldTranscoder<E, T> set(Collection<T> ts) throws E {
		field().set(xcoder.encode(ts));
		return this;
	}

	public FieldTranscoder<E, T> set(Remainder<T> rem) throws E {
		field().set(xcoder.encode(rem));
		return this;
	}

	public T get() throws E {
		return xcoder.decode(field().get());
	}

	public Set<T> getAll() throws E {
		return xcoder.decodeAll(field().get());
	}

	public Remainder<T> getWithRemainder() throws E {
		return xcoder.decodeWithRemainder(field().get());
	}

	public boolean isValid() throws E {
		return xcoder.isValid(field().get());
	}

	public final boolean has(T t) throws E {
		return xcoder.has(field.get(), t);
	}

	@SafeVarargs
	public final boolean hasAny(T... ts) throws E {
		return hasAny(Arrays.asList(ts));
	}

	public boolean hasAny(Collection<T> ts) throws E {
		return xcoder.hasAny(field.get(), ts);
	}

	@SafeVarargs
	public final boolean hasAll(T... ts) throws E {
		return hasAll(Arrays.asList(ts));
	}

	public boolean hasAll(Collection<T> ts) throws E {
		return xcoder.hasAll(field.get(), ts);
	}

	public static class Typed<E extends Exception, S, T> {
		private final ValueField.Typed<E, S> field;
		private final TypeTranscoder<T> xcoder;

		public static <E extends Exception, S, T> Typed<E, S, T> of(ValueField.Typed<E, S> field,
			TypeTranscoder<T> xcoder) {
			return new Typed<>(field, xcoder);
		}

		Typed(ValueField.Typed<E, S> field, TypeTranscoder<T> xcoder) {
			this.field = field;
			this.xcoder = xcoder;
		}

		public ValueField.Typed<E, S> field() {
			return field;
		}

		@SafeVarargs
		public final Typed<E, S, T> remove(S s, T... ts) throws E {
			return ts.length == 0 ? this : remove(s, Arrays.asList(ts));
		}

		public Typed<E, S, T> remove(S s, Collection<T> ts) throws E {
			if (!ts.isEmpty()) field().remove(s, xcoder.encode(ts));
			return this;
		}

		@SafeVarargs
		public final Typed<E, S, T> add(S s, T... ts) throws E {
			return ts.length == 0 ? this : add(s, Arrays.asList(ts));
		}

		public Typed<E, S, T> add(S s, Collection<T> ts) throws E {
			if (!ts.isEmpty()) field().add(s, xcoder.encode(ts));
			return this;
		}

		@SafeVarargs
		public final Typed<E, S, T> set(S s, T... ts) throws E {
			return set(s, Arrays.asList(ts));
		}

		public Typed<E, S, T> set(S s, Collection<T> ts) throws E {
			field().set(s, xcoder.encode(ts));
			return this;
		}

		public Typed<E, S, T> set(S s, Remainder<T> rem) throws E {
			field().set(s, xcoder.encode(rem));
			return this;
		}

		public T get(S s) throws E {
			return xcoder.decode(field().get(s));
		}

		public Set<T> getAll(S s) throws E {
			return xcoder.decodeAll(field().get(s));
		}

		public Remainder<T> getWithRemainder(S s) throws E {
			return xcoder.decodeWithRemainder(field().get(s));
		}

		public boolean isValid(S s) throws E {
			return xcoder.isValid(field().get(s));
		}

		public boolean has(S s, T t) throws E {
			return xcoder.has(field.get(s), t);
		}

		@SafeVarargs
		public final boolean hasAny(S s, T... ts) throws E {
			return hasAny(s, Arrays.asList(ts));
		}

		public boolean hasAny(S s, Collection<T> ts) throws E {
			return xcoder.hasAny(field.get(s), ts);
		}

		@SafeVarargs
		public final boolean hasAll(S s, T... ts) throws E {
			return hasAll(s, Arrays.asList(ts));
		}

		public boolean hasAll(S s, Collection<T> ts) throws E {
			return xcoder.hasAll(field.get(s), ts);
		}
	}
}
