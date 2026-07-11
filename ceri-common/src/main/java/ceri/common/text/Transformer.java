package ceri.common.text;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ceri.common.array.RawArray;
import ceri.common.collect.Immutable;
import ceri.common.collect.Iterators;
import ceri.common.collect.Lists;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.util.Basics;
import ceri.common.util.Truth;

/**
 * Utility to transform objects into strings.
 */
public class Transformer implements Functions.Function<Object, String> {
	private static final Object OVERFLOW = null;
	private static final String REMAINDER = "..";
	private static final Joiner ITERABLE_JOINER = joiner(Joiner.ARRAY, 10);
	private static final Joiner MAP_JOINER = joiner(Joiner.LIST, 10);
	public static final Transformer DEFAULT = builder().build();
	private final List<Transform<Object, ?>> transforms;
	private final Functions.Supplier<?> nullSupplier;
	private final String remainder;
	private final int levels;

	/**
	 * A transforming context that keeps track of state.
	 */
	public interface Context extends Functions.Function<Object, String> {
		/**
		 * Returns true if the nested levels are exceeded.
		 */
		boolean overflow();
	}

	/**
	 * Encapsulates a transform of one typed object to another.
	 */
	public interface Transform<T, R> extends Functions.BiFunction<Context, T, R> {
		/** Simple string transformer. */
		Transform<Object, String> STRING = (_, t) -> String.valueOf(t);

		/**
		 * A typed no-op transform.
		 */
		static <T, R> Transform<T, R> none() {
			return (_, _) -> null;
		}

		/**
		 * Returns the function as a transform.
		 */
		static <T, R> Transform<T, R> of(Functions.Function<? super T, ? extends R> function) {
			if (function == null) return none();
			return (_, t) -> function.apply(t);
		}

		/**
		 * Casts the value or returns null.
		 */
		static <T> Transform<Object, T> cast(Class<? extends T> cls) {
			return (_, t) -> Reflect.castOrNull(cls, t);
		}

		/**
		 * Renders the object using a string format.
		 */
		static Transform<Object, String> format(String format) {
			return (_, t) -> t == null ? null : Strings.format(format, t);
		}

		/**
		 * Renders the value using a string format based on the value itself.
		 */
		static <T> Transform<T, String> formats(Functions.Function<? super T, String> formats) {
			return (_, t) -> t == null ? null : Strings.format(formats.apply(t), t);
		}

		/**
		 * Joins arrays.
		 */
		static Transform<Object, String> array(Joiner joiner) {
			return (c, t) -> RawArray.isArray(t) ? Transformer.array(c, joiner, t) : null;
		}

		/**
		 * Joins the iterable type.
		 */
		static Transform<Object, String> iterable(Joiner joiner) {
			return (c, t) -> (t instanceof Iterable<?> i) ? Transformer.iterable(c, joiner, i) :
				null;
		}

		/**
		 * Joins the map as an entry set.
		 */
		static Transform<Object, String> map(Joiner joiner) {
			return (c, t) -> (t instanceof Map<?, ?> m) ?
				Transformer.iterable(c, joiner, m.entrySet()) : null;
		}

		/**
		 * Renders the map entry.
		 */
		static Transform<Object, String> mapEntry(String separator) {
			return (c, t) -> (t instanceof Map.Entry<?, ?> e) ?
				Transformer.mapEntry(c, separator, e) : null;
		}

		/**
		 * Applies the transform, within the transformer context. Returns null if not applicable.
		 */
		@Override
		R apply(Context context, T t);

		/**
		 * Filters nulls before applying the transform.
		 */
		default R safeApply(Context context, T t) {
			return t == null ? null : apply(context, t);
		}

		/**
		 * Re-applies the transformed value.
		 */
		default Transform<T, String> re() {
			return (c, t) -> c.apply(safeApply(c, t));
		}

		/**
		 * Applies the transform only to types matching the predicate.
		 */
		default Transform<T, R> with(Functions.Predicate<? super T> predicate) {
			if (predicate == null) return this;
			return (c, t) -> (t == null || !predicate.test(t)) ? null : apply(c, t);
		}

		/**
		 * Applies the transform only to class type instances.
		 */
		default Transform<Object, R> with(Class<? extends T> cls) {
			return Transform.cast(cls).then(this);
		}

		/**
		 * Applies the given transform after this transform.
		 */
		default <S> Transform<T, S> then(Transform<? super R, ? extends S> transform) {
			if (transform == null) return none();
			return (c, t) -> transform.safeApply(c, safeApply(c, t));
		}

		/**
		 * Applies the function after this transform.
		 */
		default <S> Transform<T, S> then(Functions.Function<? super R, ? extends S> function) {
			return then(Transform.of(function));
		}
	}

	/**
	 * Builder for transforms.
	 */
	public static class Builder {
		final List<Transform<Object, ?>> transforms = Lists.of();
		Functions.Supplier<?> nullSupplier = () -> Strings.NULL;
		Joiner iterableJoiner = ITERABLE_JOINER;
		Joiner mapJoiner = MAP_JOINER;
		String kvSeparator = "=";
		String remainder = REMAINDER;
		int levels = 8;

		Builder() {}

		/**
		 * Sets the maximum nesting levels.
		 */
		public Builder levels(int levels) {
			this.levels = Math.max(0, levels);
			return this;
		}

		/**
		 * Sets the formatting for iterables and arrays.
		 */
		public Builder iterables(Joiner joiner) {
			iterableJoiner = Basics.def(joiner, iterableJoiner);
			return this;
		}

		/**
		 * Sets the formatting for map and entries.
		 */
		public Builder maps(Joiner joiner, String kvSeparator) {
			mapJoiner = Basics.def(joiner, mapJoiner);
			this.kvSeparator = Basics.def(kvSeparator, "=");
			return this;
		}

		/**
		 * Sets the null transform value.
		 */
		public Builder setNull(String nullVal) {
			return setNull(() -> nullVal);
		}

		/**
		 * Sets a transform for null values.
		 */
		public Builder setNull(Functions.Supplier<?> supplier) {
			if (supplier != null) nullSupplier = supplier;
			return this;
		}

		/**
		 * Applies the string format to sub-types.
		 */
		public Builder format(Class<?> cls, String format) {
			return add(Transform.format(format).with(cls));
		}

		/**
		 * Applies a string format to sub-type values based on the value itself.
		 */
		public <T> Builder formats(Class<T> cls, Functions.Function<? super T, String> formats) {
			return add(Transform.formats(formats).with(cls));
		}

		/**
		 * Applies the transform to sub-types.
		 */
		public <T> Builder add(Class<T> cls, Transform<? super T, ?> transform) {
			return add(Transform.cast(cls).then(transform));
		}

		/**
		 * Applies the function to sub-types.
		 */
		public <T> Builder add(Class<T> cls, Functions.Function<? super T, ?> function) {
			return add(cls, Transform.of(function));
		}

		/**
		 * Applies the transform to every value, stopping if non-null.
		 */
		public Builder add(Transform<Object, ?> transform) {
			transforms.add(transform);
			return this;
		}

		/**
		 * Creates the transformer.
		 */
		public Transformer build() {
			return new Transformer(fix(this), nullSupplier, remainder, levels);
		}
	}

	/**
	 * Modifies a joiner with maximum count.
	 */
	public static Joiner joiner(Joiner joiner, int max) {
		return joiner.edit().max(max).showCount(Truth.no).remainder(REMAINDER).build();
	}

	/**
	 * Returns a new builder.
	 */
	public static Builder builder() {
		return new Builder();
	}

	private Transformer(List<Transform<Object, ?>> transforms, Functions.Supplier<?> nullSupplier,
		String remainder, int levels) {
		this.transforms = transforms;
		this.nullSupplier = nullSupplier;
		this.remainder = remainder;
		this.levels = levels;
	}

	/**
	 * Transforms the given object to a string.
	 */
	@Override
	public String apply(Object arg) {
		return context().apply(arg);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, "rules=" + transforms.size(), "levels=" + levels);
	}

	// support

	private Object transform(Context context, Object arg) {
		if (arg == null) return nullSupplier.get();
		for (var transform : transforms) {
			var result = transform.apply(context, arg);
			if (result != null) return result;
		}
		return arg;
	}

	private Context context() {
		return new Context() {
			int level = 0;

			@Override
			public boolean overflow() {
				return level >= levels;
			}

			@Override
			public String apply(Object t) throws RuntimeException {
				if (overflow()) return remainder;
				level++;
				var s = String.valueOf(transform(this, t));
				level--;
				return s;
			}
		};
	}

	private static List<Transform<Object, ?>> fix(Builder b) {
		var copy = new ArrayList<>(b.transforms);
		copy.add(Transform.STRING.with(Path.class)); // prevent infinite iterables
		copy.add(Transform.array(b.iterableJoiner));
		copy.add(Transform.iterable(b.iterableJoiner));
		copy.add(Transform.mapEntry(b.kvSeparator));
		copy.add(Transform.map(b.mapJoiner));
		return Immutable.wrap(copy);
	}

	private static String mapEntry(Context context, String separator, Map.Entry<?, ?> entry) {
		if (context.overflow()) return context.apply(OVERFLOW);
		return context.apply(entry.getKey()) + separator + context.apply(entry.getValue());
	}

	private static String array(Context context, Joiner joiner, Object array) {
		if (context.overflow()) return joiner.joinAll(context, OVERFLOW);
		int size = RawArray.length(array);
		var iterator = Iterators.indexed(size, i -> RawArray.get(array, i));
		return joiner.join(context, iterator, size);
	}

	private static String iterable(Context context, Joiner joiner, Iterable<?> t) {
		if (context.overflow()) return joiner.joinAll(context, OVERFLOW);
		return joiner.join(context, t);
	}
}
