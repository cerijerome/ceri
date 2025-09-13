package ceri.common.text;

import static ceri.common.validation.ValidationUtil.validateMin;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collector;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.stream.Collect;
import ceri.common.util.Basics;
import ceri.common.util.Truth;

/**
 * Utility for joining a sequence of objects as a string.
 */
public class Joiner implements Collector<Object, Joiner.Composer.Collecting, String> {
	/** No separator, prefix or suffix. */
	public static final Joiner NONE = of("");
	/** Array-style joining. */
	public static final Joiner ARRAY = of("[", ", ", "]");
	/** Array-style joining, without spaces. */
	public static final Joiner ARRAY_COMPACT = of("[", ",", "]");
	/** List-style joining. */
	public static final Joiner LIST = of("{", ", ", "}");
	/** List-style joining, without spaces. */
	public static final Joiner LIST_COMPACT = of("{", ",", "}");
	/** Parameter-style joining. */
	public static final Joiner PARAM = of("(", ", ", ")");
	/** Parameter-style joining, without spaces. */
	public static final Joiner PARAM_COMPACT = of("(", ",", ")");
	/** Quote each item, separated by comma. */
	public static final Joiner QUOTE = of("\"", "\", \"", "\"");
	/** Comma separator without prefix or suffix. */
	public static final Joiner COMMA = of(",");
	/** Pipe separator without prefix or suffix. */
	public static final Joiner OR = of("|");
	/** Colon separator without prefix or suffix. */
	public static final Joiner COLON = of(":");
	/** Space separator without prefix or suffix. */
	public static final Joiner SPACE = of(" ");
	/** Native line joiner. */
	public static final Joiner EOL = of(Strings.EOL);
	public final String prefix;
	public final String suffix;
	public final String separator;
	public final Integer max;
	public final String remainder;
	public final Truth showCount;
	public final String countFormat;

	public static Joiner of(String separator) {
		return of("", separator, "");
	}

	public static Joiner of(String prefix, String separator, String suffix) {
		return of(prefix, separator, suffix, null);
	}

	public static Joiner of(String prefix, String separator, String suffix, Integer max) {
		return builder().prefix(prefix).suffix(suffix).separator(separator).max(max).build();
	}

	public static class Builder {
		String prefix = "";
		String suffix = "";
		String separator = ", ";
		Integer max = null;
		String remainder = "...";
		Truth showCount = Truth.maybe; // show count if max exceeded
		String countFormat = "(%d)";

		private Builder() {}

		public Builder prefix(String prefix) {
			this.prefix = Basics.def(prefix, "");
			return this;
		}

		public Builder suffix(String suffix) {
			this.suffix = Basics.def(suffix, "");
			return this;
		}

		public Builder separator(String separator) {
			this.separator = Basics.def(separator, "");
			return this;
		}

		public Builder max(Integer max) {
			this.max = max;
			return this;
		}

		public Builder remainder(String remainder) {
			this.remainder = Basics.def(remainder, "");
			return this;
		}

		public Builder showCount(Truth showCount) {
			this.showCount = Basics.def(showCount, Truth.maybe);
			return this;
		}

		public Builder countFormat(String countFormat) {
			this.countFormat = Basics.def(countFormat, "");
			return this;
		}

		public Joiner build() {
			return new Joiner(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Stateful element renderer.
	 */
	public static class Composer<E extends Exception, T> {
		private final Joiner joiner;
		private final StringBuilder b;
		private final Excepts.BiObjIntConsumer<E, StringBuilder, T> appender;
		private final Integer count;
		private int i = -1;
		private T last = null;
		private boolean complete = false;

		public static class Collecting extends Composer<RuntimeException, Object> {
			private Collecting(Joiner joiner) {
				super(joiner, new StringBuilder(), (b, t, _) -> b.append(t), null);
			}
		}

		private Composer(Joiner joiner, StringBuilder b,
			Excepts.BiObjIntConsumer<E, StringBuilder, T> appender, Integer count) {
			this.joiner = joiner;
			this.b = b.append(joiner.prefix);
			this.appender = appender;
			this.count = count;
		}

		/**
		 * Render an item based on index.
		 */
		public boolean add() throws E {
			return add(null);
		}

		/**
		 * Render an item based on type and/or index.
		 */
		public boolean add(T t) throws E {
			if (complete) return false;
			i++;
			if (count != null && i >= count) return false;
			if (joiner.max == null || i < joiner.max - 1) append(t);
			else if (joiner.max <= 0) return false;
			else if (i == joiner.max - 1) last = t;
			else if (i == joiner.max) {
				if (i - 1 > 0) b.append(joiner.separator);
				b.append(joiner.remainder);
				return false;
			}
			return true;
		}

		/**
		 * Complete the join. No more additions are allowed.
		 */
		public StringBuilder complete() throws E {
			if (joiner.max != null && joiner.max > 0 && i == joiner.max - 1) append(last);
			b.append(joiner.suffix);
			appendCount(count != null ? count : i + 1);
			complete = true;
			return b;
		}

		private void append(T t) throws E {
			if (i > 0) b.append(joiner.separator);
			appender.accept(b, t, i);
		}

		private void appendCount(int count) {
			if (joiner.showCount.no()) return;
			if (joiner.showCount.yes() || (joiner.max != null && count > joiner.max))
				b.append(Strings.format(joiner.countFormat, count));
		}
	}

	private Joiner(Builder builder) {
		this.prefix = builder.prefix;
		this.suffix = builder.suffix;
		this.separator = builder.separator;
		this.max = builder.max;
		this.remainder = builder.remainder;
		this.showCount = builder.showCount;
		this.countFormat = builder.countFormat;
	}

	public Builder edit() {
		return new Builder().prefix(prefix).suffix(suffix).separator(separator).max(max)
			.remainder(remainder).showCount(showCount).countFormat(countFormat);
	}

	// stream collector

	@Override
	public Functions.Supplier<Composer.Collecting> supplier() {
		return () -> new Composer.Collecting(this);
	}

	@Override
	public Functions.BiConsumer<Composer.Collecting, Object> accumulator() {
		return (c, o) -> c.add(o);
	}

	@Override
	public Functions.BiOperator<Composer.Collecting> combiner() {
		return Collect.noCombiner();
	}

	@Override
	public Functions.Function<Composer.Collecting, String> finisher() {
		return c -> c.complete().toString();
	}

	@Override
	public Set<Characteristics> characteristics() {
		return Set.of();
	}

	// joining

	/**
	 * Join items based on index.
	 */
	public <E extends Exception> String joinIndex(Excepts.IntFunction<E, ?> indexFn, int count)
		throws E {
		return joinIndex(indexFn, 0, count);
	}

	/**
	 * Join items based on index.
	 */
	public <E extends Exception> String joinIndex(Excepts.IntFunction<E, ?> indexFn, int offset,
		int count) throws E {
		if (indexFn == null) return "";
		return joinIndex((b, i) -> b.append(indexFn.apply(i)), offset, count);
	}

	/**
	 * Join items based on index.
	 */
	public <E extends Exception> String
		joinIndex(Excepts.ObjIntConsumer<E, StringBuilder> indexAppender, int count) throws E {
		return joinIndex(indexAppender, 0, count);
	}

	/**
	 * Join items based on index.
	 */
	public <E extends Exception> String joinIndex(
		Excepts.ObjIntConsumer<E, StringBuilder> indexAppender, int offset, int count) throws E {
		return appendWithIndex(new StringBuilder(), indexAppender, offset, count).toString();
	}

	/**
	 * Join items based on type.
	 */
	@SafeVarargs
	public final <T> String joinAll(T... ts) {
		return joinAll(StringBuilder::append, ts);
	}

	/**
	 * Join items based on type.
	 */
	@SafeVarargs
	public final <E extends Exception, T> String joinAll(Excepts.Function<E, T, ?> stringFn,
		T... ts) throws E {
		if (stringFn == null) return "";
		return joinAll((b, t) -> b.append(stringFn.apply(t)), ts);
	}

	/**
	 * Join items based on type.
	 */
	@SafeVarargs
	public final <E extends Exception, T> String
		joinAll(Excepts.BiConsumer<E, StringBuilder, T> appender, T... ts) throws E {
		return appendAll(new StringBuilder(), appender, ts).toString();
	}

	/**
	 * Join items based on type.
	 */
	public <T> String join(Iterable<T> iterable) {
		return join(StringBuilder::append, iterable);
	}

	/**
	 * Join items based on type.
	 */
	public <E extends Exception, T> String join(Excepts.Function<E, T, ?> stringFn,
		Iterable<T> iterable) throws E {
		if (stringFn == null) return "";
		return join((b, t) -> b.append(stringFn.apply(t)), iterable);
	}

	/**
	 * Join items based on type.
	 */
	public <E extends Exception, T> String join(Excepts.BiConsumer<E, StringBuilder, T> appender,
		Iterable<T> collection) throws E {
		return append(new StringBuilder(), appender, collection).toString();
	}

	/**
	 * Join items based on type; count is not available.
	 */
	public <T> String join(Iterator<T> iterator) {
		return join(StringBuilder::append, iterator);
	}

	/**
	 * Join items based on type, up to count.
	 */
	public <T> String join(Iterator<T> iterator, int count) {
		return join(StringBuilder::append, iterator, count);
	}

	/**
	 * Join items based on type; count is not available.
	 */
	public <E extends Exception, T> String join(Excepts.Function<E, T, ?> stringFn,
		Iterator<T> iterator) throws E {
		if (stringFn == null) return "";
		return join((b, t) -> b.append(stringFn.apply(t)), iterator);
	}

	/**
	 * Join items based on type, up to count.
	 */
	public <E extends Exception, T> String join(Excepts.Function<E, T, ?> stringFn,
		Iterator<T> iterator, int count) throws E {
		if (stringFn == null) return "";
		return join((b, t) -> b.append(stringFn.apply(t)), iterator, count);
	}

	/**
	 * Join items based on type; count is not available.
	 */
	public <E extends Exception, T> String join(Excepts.BiConsumer<E, StringBuilder, T> appender,
		Iterator<T> iterator) throws E {
		return append(new StringBuilder(), appender, iterator).toString();
	}

	/**
	 * Join items based on type, up to count.
	 */
	public <E extends Exception, T> String join(Excepts.BiConsumer<E, StringBuilder, T> appender,
		Iterator<T> iterator, int count) throws E {
		return appendTo(new StringBuilder(), appender, iterator, count).toString();
	}

	// appending

	/**
	 * Append items based on index.
	 */
	public <E extends Exception> StringBuilder appendByIndex(StringBuilder sb,
		Excepts.IntFunction<E, ?> indexFn, int count) throws E {
		return appendByIndex(sb, indexFn, 0, count);
	}

	/**
	 * Append items based on index.
	 */
	public <E extends Exception> StringBuilder appendByIndex(StringBuilder sb,
		Excepts.IntFunction<E, ?> indexFn, int offset, int count) throws E {
		if (indexFn == null) return sb;
		return appendWithIndex(sb, (b, i) -> b.append(indexFn.apply(i)), offset, count);
	}

	/**
	 * Append items based on index.
	 */
	public <E extends Exception> StringBuilder appendWithIndex(StringBuilder sb,
		Excepts.ObjIntConsumer<E, StringBuilder> indexAppender, int count) throws E {
		return appendWithIndex(sb, indexAppender, 0, count);
	}

	/**
	 * Append items based on index.
	 */
	public <E extends Exception> StringBuilder appendWithIndex(StringBuilder sb,
		Excepts.ObjIntConsumer<E, StringBuilder> indexAppender, int offset, int count) throws E {
		if (sb == null || indexAppender == null) return sb;
		validateMin(offset, 0, "offset");
		validateMin(count, 0, "count");
		var composer =
			new Composer<>(this, sb, (b, _, i) -> indexAppender.accept(b, offset + i), count);
		for (int i = 0; i < count; i++)
			if (!composer.add(null)) break;
		composer.complete();
		return sb;
	}

	/**
	 * Append items based on type.
	 */
	@SafeVarargs
	public final <T> StringBuilder appendAll(StringBuilder sb, T... ts) {
		return appendAll(sb, StringBuilder::append, ts);
	}

	/**
	 * Append items based on type.
	 */
	@SafeVarargs
	public final <E extends Exception, T> StringBuilder appendAll(StringBuilder sb,
		Excepts.Function<E, T, ?> stringFn, T... ts) throws E {
		if (stringFn == null) return sb;
		return appendAll(sb, (b, t) -> b.append(stringFn.apply(t)), ts);
	}

	/**
	 * Append items based on type.
	 */
	@SafeVarargs
	public final <E extends Exception, T> StringBuilder appendAll(StringBuilder sb,
		Excepts.BiConsumer<E, StringBuilder, T> appender, T... ts) throws E {
		if (ts == null) return sb;
		return append(sb, appender, Arrays.asList(ts));
	}

	/**
	 * Append items based on type.
	 */
	public <T> StringBuilder append(StringBuilder sb, Iterable<T> iterable) {
		return append(sb, StringBuilder::append, iterable);
	}

	/**
	 * Append items based on type.
	 */
	public <E extends Exception, T> StringBuilder append(StringBuilder sb,
		Excepts.Function<E, T, ?> stringFn, Iterable<T> iterable) throws E {
		if (stringFn == null) return sb;
		return append(sb, (b, t) -> b.append(stringFn.apply(t)), iterable);
	}

	/**
	 * Append items based on type.
	 */
	public <E extends Exception, T> StringBuilder append(StringBuilder sb,
		Excepts.BiConsumer<E, StringBuilder, T> appender, Iterable<T> iterable) throws E {
		if (iterable == null) return sb;
		return appendTo(sb, appender, iterable.iterator(), null);
	}

	/**
	 * Append items based on type; count is not available.
	 */
	public <T> StringBuilder append(StringBuilder sb, Iterator<T> iterator) {
		return append(sb, StringBuilder::append, iterator);
	}

	/**
	 * Append items based on type; count is not available.
	 */
	public <E extends Exception, T> StringBuilder append(StringBuilder sb,
		Excepts.Function<E, T, ?> stringFn, Iterator<T> iterator) throws E {
		if (stringFn == null) return sb;
		return append(sb, (b, t) -> b.append(stringFn.apply(t)), iterator);
	}

	/**
	 * Append items based on type; count is not available.
	 */
	public <E extends Exception, T> StringBuilder append(StringBuilder sb,
		Excepts.BiConsumer<E, StringBuilder, T> appender, Iterator<T> iterator) throws E {
		return appendTo(sb, appender, iterator, null);
	}

	/**
	 * Append items based on type, up to count.
	 */
	public <T> StringBuilder append(StringBuilder sb, Iterator<T> iterator, int count) {
		return append(sb, StringBuilder::append, iterator, count);
	}

	/**
	 * Append items based on type, up to count.
	 */
	public <E extends Exception, T> StringBuilder append(StringBuilder sb,
		Excepts.Function<E, T, ?> stringFn, Iterator<T> iterator, int count) throws E {
		if (stringFn == null) return sb;
		return append(sb, (b, t) -> b.append(stringFn.apply(t)), iterator, count);
	}

	/**
	 * Append items based on type, up to count.
	 */
	public <E extends Exception, T> StringBuilder append(StringBuilder sb,
		Excepts.BiConsumer<E, StringBuilder, T> appender, Iterator<T> iterator, int count)
		throws E {
		return appendTo(sb, appender, iterator, count);
	}

	// Support methods

	private <E extends Exception, T> StringBuilder appendTo(StringBuilder sb,
		Excepts.BiConsumer<E, StringBuilder, T> appender, Iterator<T> iterator, Integer count)
		throws E {
		if (sb == null || iterator == null || appender == null) return sb;
		var composer = new Composer<E, T>(this, sb, (b, t, _) -> appender.accept(b, t), count);
		while (iterator.hasNext() && composer.add(iterator.next())) {}
		composer.complete();
		return sb;
	}
}
